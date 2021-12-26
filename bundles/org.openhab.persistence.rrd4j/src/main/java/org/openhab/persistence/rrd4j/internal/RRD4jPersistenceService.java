/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.persistence.rrd4j.internal;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.ItemUtil;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Ordering;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.PersistenceItemInfo;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.persistence.strategy.PersistenceCronStrategy;
import org.openhab.core.persistence.strategy.PersistenceStrategy;
import org.openhab.core.types.State;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the implementation of the RRD4j {@link PersistenceService}. To learn
 * more about RRD4j please visit their
 * <a href="https://github.com/rrd4j/rrd4j">website</a>.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Jan N. Klug - some improvements
 * @author Karel Goderis - remove TimerThread dependency
 */
@NonNullByDefault
@Component(service = { PersistenceService.class,
        QueryablePersistenceService.class }, configurationPid = "org.openhab.rrd4j", configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class RRD4jPersistenceService implements QueryablePersistenceService {

    private static final String DEFAULT_OTHER = "default_other";
    private static final String DEFAULT_NUMERIC = "default_numeric";
    private static final String DEFAULT_QUANTIFIABLE = "default_quantifiable";

    private static final Set<String> SUPPORTED_TYPES = Set.of(CoreItemFactory.SWITCH, CoreItemFactory.CONTACT,
            CoreItemFactory.DIMMER, CoreItemFactory.NUMBER, CoreItemFactory.ROLLERSHUTTER, CoreItemFactory.COLOR);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3,
            new NamedThreadFactory("RRD4j"));

    private final Map<String, RrdDefConfig> rrdDefs = new ConcurrentHashMap<>();

    private static final String DATASOURCE_STATE = "state";

    public static final String DB_FOLDER = getUserPersistenceDataFolder() + File.separator + "rrd4j";

    private final Logger logger = LoggerFactory.getLogger(RRD4jPersistenceService.class);

    private final Map<String, ScheduledFuture<?>> scheduledJobs = new HashMap<>();

    protected final ItemRegistry itemRegistry;

    @Activate
    public RRD4jPersistenceService(final @Reference ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    @Override
    public String getId() {
        return "rrd4j";
    }

    @Override
    public String getLabel(@Nullable Locale locale) {
        return "RRD4j";
    }

    @Override
    public synchronized void store(final Item item, @Nullable final String alias) {
        if (!isSupportedItemType(item)) {
            logger.trace("Ignoring item '{}' since its type {} is not supported", item.getName(), item.getType());
            return;
        }
        final String name = alias == null ? item.getName() : alias;
        RrdDb db = getDB(name);
        if (db != null) {
            ConsolFun function = getConsolidationFunction(db);
            long now = System.currentTimeMillis() / 1000;
            if (function != ConsolFun.AVERAGE) {
                try {
                    // we store the last value again, so that the value change
                    // in the database is not interpolated, but
                    // happens right at this spot
                    if (now - 1 > db.getLastUpdateTime()) {
                        // only do it if there is not already a value
                        double lastValue = db.getLastDatasourceValue(DATASOURCE_STATE);
                        if (!Double.isNaN(lastValue)) {
                            Sample sample = db.createSample();
                            sample.setTime(now - 1);
                            sample.setValue(DATASOURCE_STATE, lastValue);
                            sample.update();
                            logger.debug("Stored '{}' as value '{}' in rrd4j database (again)", name, lastValue);
                        }
                    }
                } catch (IOException e) {
                    logger.debug("Error storing last value (again): {}", e.getMessage());
                }
            }
            try {
                Sample sample = db.createSample();
                sample.setTime(now);

                Double value = null;

                if (item instanceof NumberItem && item.getState() instanceof QuantityType) {
                    NumberItem nItem = (NumberItem) item;
                    QuantityType<?> qState = (QuantityType<?>) item.getState();
                    Unit<? extends Quantity<?>> unit = nItem.getUnit();
                    if (unit != null) {
                        QuantityType<?> convertedState = qState.toUnit(unit);
                        if (convertedState != null) {
                            value = convertedState.doubleValue();
                        } else {
                            logger.warn(
                                    "Failed to convert state '{}' to unit '{}'. Please check your item definition for correctness.",
                                    qState, unit);
                        }
                    } else {
                        value = qState.doubleValue();
                    }
                } else {
                    DecimalType state = item.getStateAs(DecimalType.class);
                    if (state != null) {
                        value = state.toBigDecimal().doubleValue();
                    }
                }
                if (value != null) {
                    if (db.getDatasource(DATASOURCE_STATE).getType() == DsType.COUNTER) { // counter values must be
                                                                                          // adjusted by stepsize
                        value = value * db.getRrdDef().getStep();
                    }
                    sample.setValue(DATASOURCE_STATE, value);
                    sample.update();
                    logger.debug("Stored '{}' as value '{}' in rrd4j database", name, value);
                }
            } catch (IllegalArgumentException e) {
                String message = e.getMessage();
                if (message != null && message.contains("at least one second step is required")) {
                    // we try to store the value one second later
                    ScheduledFuture<?> job = scheduledJobs.get(name);
                    if (job != null) {
                        job.cancel(true);
                        scheduledJobs.remove(name);
                    }
                    job = scheduler.schedule(() -> store(item, name), 1, TimeUnit.SECONDS);
                    scheduledJobs.put(name, job);
                } else {
                    logger.warn("Could not persist '{}' to rrd4j database: {}", name, e.getMessage());
                }
            } catch (Exception e) {
                logger.warn("Could not persist '{}' to rrd4j database: {}", name, e.getMessage());
            }
            try {
                db.close();
            } catch (IOException e) {
                logger.debug("Error closing rrd4j database: {}", e.getMessage());
            }
        }
    }

    @Override
    public void store(Item item) {
        store(item, null);
    }

    @Override
    public Iterable<HistoricItem> query(FilterCriteria filter) {
        String itemName = filter.getItemName();

        RrdDb db = getDB(itemName);
        if (db == null) {
            logger.debug("Could not find item '{}' in rrd4j database", itemName);
            return List.of();
        }

        Item item = null;
        Unit<?> unit = null;
        try {
            item = itemRegistry.getItem(itemName);
            if (item instanceof NumberItem) {
                // we already retrieve the unit here once as it is a very costly operation,
                // see https://github.com/openhab/openhab-addons/issues/8928
                unit = ((NumberItem) item).getUnit();
            }
        } catch (ItemNotFoundException e) {
            logger.debug("Could not find item '{}' in registry", itemName);
        }

        long start = 0L;
        long end = filter.getEndDate() == null ? System.currentTimeMillis() / 1000
                : filter.getEndDate().toInstant().getEpochSecond();

        try {
            if (filter.getBeginDate() == null) {
                // as rrd goes back for years and gets more and more
                // inaccurate, we only support descending order
                // and a single return value
                // if there is no begin date is given - this case is
                // required specifically for the historicState()
                // query, which we want to support
                if (filter.getOrdering() == Ordering.DESCENDING && filter.getPageSize() == 1
                        && filter.getPageNumber() == 0) {
                    if (filter.getEndDate() == null) {
                        // we are asked only for the most recent value!
                        double lastValue = db.getLastDatasourceValue(DATASOURCE_STATE);
                        if (!Double.isNaN(lastValue)) {
                            HistoricItem rrd4jItem = new RRD4jItem(itemName, mapToState(lastValue, item, unit),
                                    ZonedDateTime.ofInstant(Instant.ofEpochMilli(db.getLastArchiveUpdateTime() * 1000),
                                            ZoneId.systemDefault()));
                            return List.of(rrd4jItem);
                        } else {
                            return List.of();
                        }
                    } else {
                        start = end;
                    }
                } else {
                    throw new UnsupportedOperationException("rrd4j does not allow querys without a begin date, "
                            + "unless order is descending and a single value is requested");
                }
            } else {
                start = filter.getBeginDate().toInstant().getEpochSecond();
            }

            FetchRequest request = db.createFetchRequest(getConsolidationFunction(db), start, end, 1);
            FetchData result = request.fetchData();

            List<HistoricItem> items = new ArrayList<>();
            long ts = result.getFirstTimestamp();
            long step = result.getRowCount() > 1 ? result.getStep() : 0;
            for (double value : result.getValues(DATASOURCE_STATE)) {
                if (!Double.isNaN(value) && (((ts >= start) && (ts <= end)) || (start == end))) {
                    RRD4jItem rrd4jItem = new RRD4jItem(itemName, mapToState(value, item, unit),
                            ZonedDateTime.ofInstant(Instant.ofEpochSecond(ts), ZoneId.systemDefault()));
                    items.add(rrd4jItem);
                }
                ts += step;
            }
            return items;
        } catch (IOException e) {
            logger.warn("Could not query rrd4j database for item '{}': {}", itemName, e.getMessage());
            return List.of();
        }
    }

    @Override
    public Set<PersistenceItemInfo> getItemInfo() {
        return Set.of();
    }

    protected synchronized @Nullable RrdDb getDB(String alias) {
        RrdDb db = null;
        File file = new File(DB_FOLDER + File.separator + alias + ".rrd");
        try {
            if (file.exists()) {
                // recreate the RrdDb instance from the file
                db = RrdDb.of(file.getAbsolutePath());
            } else {
                File folder = new File(DB_FOLDER);
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                RrdDef rrdDef = getRrdDef(alias, file);
                if (rrdDef != null) {
                    // create a new database file
                    db = RrdDb.of(rrdDef);
                } else {
                    logger.debug(
                            "Did not create rrd4j database for item '{}' since no rrd definition could be determined. This is likely due to an unsupported item type.",
                            alias);
                }
            }
        } catch (IOException e) {
            logger.error("Could not create rrd4j database file '{}': {}", file.getAbsolutePath(), e.getMessage());
        } catch (RejectedExecutionException e) {
            // this happens if the system is shut down
            logger.debug("Could not create rrd4j database file '{}': {}", file.getAbsolutePath(), e.getMessage());
        }
        return db;
    }

    private @Nullable RrdDefConfig getRrdDefConfig(String itemName) {
        RrdDefConfig useRdc = null;
        for (Map.Entry<String, RrdDefConfig> e : rrdDefs.entrySet()) {
            // try to find special config
            RrdDefConfig rdc = e.getValue();
            if (rdc.appliesTo(itemName)) {
                useRdc = rdc;
                break;
            }
        }
        if (useRdc == null) { // not defined, use defaults
            try {
                Item item = itemRegistry.getItem(itemName);
                if (!isSupportedItemType(item)) {
                    return null;
                }
                if (item instanceof NumberItem) {
                    NumberItem numberItem = (NumberItem) item;
                    useRdc = numberItem.getDimension() != null ? rrdDefs.get(DEFAULT_QUANTIFIABLE)
                            : rrdDefs.get(DEFAULT_NUMERIC);
                } else {
                    useRdc = rrdDefs.get(DEFAULT_OTHER);
                }
            } catch (ItemNotFoundException e) {
                logger.debug("Could not find item '{}' in registry", itemName);
                return null;
            }
        }
        logger.trace("Using rrd definition '{}' for item '{}'.", useRdc, itemName);
        return useRdc;
    }

    private @Nullable RrdDef getRrdDef(String itemName, File file) {
        RrdDef rrdDef = new RrdDef(file.getAbsolutePath());
        RrdDefConfig useRdc = getRrdDefConfig(itemName);
        if (useRdc != null) {
            rrdDef.setStep(useRdc.step);
            rrdDef.setStartTime(System.currentTimeMillis() / 1000 - 1);
            rrdDef.addDatasource(DATASOURCE_STATE, useRdc.dsType, useRdc.heartbeat, useRdc.min, useRdc.max);
            for (RrdArchiveDef rad : useRdc.archives) {
                rrdDef.addArchive(rad.fcn, rad.xff, rad.steps, rad.rows);
            }
            return rrdDef;
        } else {
            return null;
        }
    }

    public ConsolFun getConsolidationFunction(RrdDb db) {
        try {
            return db.getRrdDef().getArcDefs()[0].getConsolFun();
        } catch (IOException e) {
            return ConsolFun.MAX;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private State mapToState(double value, @Nullable Item item, @Nullable Unit unit) {
        if (item instanceof GroupItem) {
            item = ((GroupItem) item).getBaseItem();
        }

        if (item instanceof SwitchItem && !(item instanceof DimmerItem)) {
            return value == 0.0d ? OnOffType.OFF : OnOffType.ON;
        } else if (item instanceof ContactItem) {
            return value == 0.0d ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
        } else if (item instanceof DimmerItem || item instanceof RollershutterItem || item instanceof ColorItem) {
            // make sure Items that need PercentTypes instead of DecimalTypes do receive the right information
            return new PercentType((int) Math.round(value * 100));
        } else if (item instanceof NumberItem) {
            if (unit != null) {
                return new QuantityType(value, unit);
            } else {
                return new DecimalType(value);
            }
        }
        return new DecimalType(value);
    }

    private boolean isSupportedItemType(Item item) {
        if (item instanceof GroupItem) {
            final Item baseItem = ((GroupItem) item).getBaseItem();
            if (baseItem != null) {
                item = baseItem;
            }
        }

        return SUPPORTED_TYPES.contains(ItemUtil.getMainItemType(item.getType()));
    }

    private static String getUserPersistenceDataFolder() {
        return OpenHAB.getUserDataFolder() + File.separator + "persistence";
    }

    @Activate
    protected void activate(final Map<String, Object> config) {
        modified(config);
    }

    @Modified
    protected void modified(final Map<String, Object> config) {
        // clean existing definitions
        rrdDefs.clear();

        // add default configurations

        RrdDefConfig defaultNumeric = new RrdDefConfig(DEFAULT_NUMERIC);
        // use 10 seconds as a step size for numeric values and allow a 10 minute silence between updates
        defaultNumeric.setDef("GAUGE,600,U,U,10");
        // define 5 different boxes:
        // 1. granularity of 10s for the last hour
        // 2. granularity of 1m for the last week
        // 3. granularity of 15m for the last year
        // 4. granularity of 1h for the last 5 years
        // 5. granularity of 1d for the last 10 years
        defaultNumeric
                .addArchives("LAST,0.5,1,360:LAST,0.5,6,10080:LAST,0.5,90,36500:LAST,0.5,360,43800:LAST,0.5,8640,3650");
        rrdDefs.put(DEFAULT_NUMERIC, defaultNumeric);

        RrdDefConfig defaultQuantifiable = new RrdDefConfig(DEFAULT_QUANTIFIABLE);
        // use 10 seconds as a step size for numeric values and allow a 10 minute silence between updates
        defaultQuantifiable.setDef("GAUGE,600,U,U,10");
        // define 5 different boxes:
        // 1. granularity of 10s for the last hour
        // 2. granularity of 1m for the last week
        // 3. granularity of 15m for the last year
        // 4. granularity of 1h for the last 5 years
        // 5. granularity of 1d for the last 10 years
        defaultQuantifiable.addArchives(
                "AVERAGE,0.5,1,360:AVERAGE,0.5,6,10080:AVERAGE,0.5,90,36500:AVERAGE,0.5,360,43800:AVERAGE,0.5,8640,3650");
        rrdDefs.put(DEFAULT_QUANTIFIABLE, defaultQuantifiable);

        RrdDefConfig defaultOther = new RrdDefConfig(DEFAULT_OTHER);
        // use 5 seconds as a step size for discrete values and allow a 1h silence between updates
        defaultOther.setDef("GAUGE,3600,U,U,5");
        // define 4 different boxes:
        // 1. granularity of 5s for the last hour
        // 2. granularity of 1m for the last week
        // 3. granularity of 15m for the last year
        // 4. granularity of 4h for the last 10 years
        defaultOther.addArchives("LAST,0.5,1,720:LAST,0.5,12,10080:LAST,0.5,180,35040:LAST,0.5,2880,21900");
        rrdDefs.put(DEFAULT_OTHER, defaultOther);

        if (config.isEmpty()) {
            logger.debug("using default configuration only");
            return;
        }

        Iterator<String> keys = config.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();

            if (key.equals("service.pid") || key.equals("component.name")) {
                // ignore service.pid and name
                continue;
            }

            String[] subkeys = key.split("\\.");
            if (subkeys.length != 2) {
                logger.debug("config '{}' should have the format 'name.configkey'", key);
                continue;
            }

            Object v = config.get(key);
            if (v instanceof String) {
                String value = (String) v;
                String name = subkeys[0].toLowerCase();
                String property = subkeys[1].toLowerCase();

                if (value.isBlank()) {
                    logger.trace("Config is empty: {}", property);
                    continue;
                } else {
                    logger.trace("Processing config: {} = {}", property, value);
                }

                RrdDefConfig rrdDef = rrdDefs.get(name);
                if (rrdDef == null) {
                    rrdDef = new RrdDefConfig(name);
                    rrdDefs.put(name, rrdDef);
                }

                try {
                    if (property.equals("def")) {
                        rrdDef.setDef(value);
                    } else if (property.equals("archives")) {
                        rrdDef.addArchives(value);
                    } else if (property.equals("items")) {
                        rrdDef.addItems(value);
                    } else {
                        logger.debug("Unknown property {} : {}", property, value);
                    }
                } catch (IllegalArgumentException e) {
                    logger.warn("Ignoring illegal configuration: {}", e.getMessage());
                }
            }
        }

        for (RrdDefConfig rrdDef : rrdDefs.values()) {
            if (rrdDef.isValid()) {
                logger.debug("Created {}", rrdDef);
            } else {
                logger.info("Removing invalid definition {}", rrdDef);
                rrdDefs.remove(rrdDef.name);
            }
        }
    }

    private class RrdArchiveDef {
        public @Nullable ConsolFun fcn;
        public double xff;
        public int steps, rows;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(" " + fcn);
            sb.append(" xff = ").append(xff);
            sb.append(" steps = ").append(steps);
            sb.append(" rows = ").append(rows);
            return sb.toString();
        }
    }

    private class RrdDefConfig {
        public String name;
        public @Nullable DsType dsType;
        public int heartbeat, step;
        public double min, max;
        public List<RrdArchiveDef> archives;
        public List<String> itemNames;

        private boolean isInitialized;

        public RrdDefConfig(String name) {
            this.name = name;
            archives = new ArrayList<>();
            itemNames = new ArrayList<>();
            isInitialized = false;
        }

        public void setDef(String defString) {
            String[] opts = defString.split(",");
            if (opts.length != 5) { // check if correct number of parameters
                logger.warn("invalid number of parameters {}: {}", name, defString);
                return;
            }

            if (opts[0].equals("ABSOLUTE")) { // dsType
                dsType = DsType.ABSOLUTE;
            } else if (opts[0].equals("COUNTER")) {
                dsType = DsType.COUNTER;
            } else if (opts[0].equals("DERIVE")) {
                dsType = DsType.DERIVE;
            } else if (opts[0].equals("GAUGE")) {
                dsType = DsType.GAUGE;
            } else {
                logger.warn("{}: dsType {} not supported", name, opts[0]);
            }

            heartbeat = Integer.parseInt(opts[1]);

            if (opts[2].equals("U")) {
                min = Double.NaN;
            } else {
                min = Double.parseDouble(opts[2]);
            }

            if (opts[3].equals("U")) {
                max = Double.NaN;
            } else {
                max = Double.parseDouble(opts[3]);
            }

            step = Integer.parseInt(opts[4]);

            isInitialized = true; // successfully initialized

            return;
        }

        public void addArchives(String archivesString) {
            String splitArchives[] = archivesString.split(":");
            for (String archiveString : splitArchives) {
                String[] opts = archiveString.split(",");
                if (opts.length != 4) { // check if correct number of parameters
                    logger.warn("invalid number of parameters {}: {}", name, archiveString);
                    return;
                }
                RrdArchiveDef arc = new RrdArchiveDef();

                if (opts[0].equals("AVERAGE")) {
                    arc.fcn = ConsolFun.AVERAGE;
                } else if (opts[0].equals("MIN")) {
                    arc.fcn = ConsolFun.MIN;
                } else if (opts[0].equals("MAX")) {
                    arc.fcn = ConsolFun.MAX;
                } else if (opts[0].equals("LAST")) {
                    arc.fcn = ConsolFun.LAST;
                } else if (opts[0].equals("FIRST")) {
                    arc.fcn = ConsolFun.FIRST;
                } else if (opts[0].equals("TOTAL")) {
                    arc.fcn = ConsolFun.TOTAL;
                } else {
                    logger.warn("{}: consolidation function  {} not supported", name, opts[0]);
                }
                arc.xff = Double.parseDouble(opts[1]);
                arc.steps = Integer.parseInt(opts[2]);
                arc.rows = Integer.parseInt(opts[3]);
                archives.add(arc);
            }
        }

        public void addItems(String itemsString) {
            String splitItems[] = itemsString.split(",");
            for (String item : splitItems) {
                itemNames.add(item);
            }
        }

        public boolean appliesTo(String item) {
            return itemNames.contains(item);
        }

        public boolean isValid() { // a valid configuration must be initialized
            // and contain at least one function
            return isInitialized && !archives.isEmpty();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(name);
            sb.append(" = ").append(dsType);
            sb.append(" heartbeat = ").append(heartbeat);
            sb.append(" min/max = ").append(min).append("/").append(max);
            sb.append(" step = ").append(step);
            sb.append(" ").append(archives.size()).append(" archives(s) = [");
            for (RrdArchiveDef arc : archives) {
                sb.append(arc.toString());
            }
            sb.append("] ");
            sb.append(itemNames.size()).append(" items(s) = [");
            for (String item : itemNames) {
                sb.append(item).append(" ");
            }
            sb.append("]");
            return sb.toString();
        }
    }

    @Override
    public List<PersistenceStrategy> getDefaultStrategies() {
        return List.of(PersistenceStrategy.Globals.RESTORE, PersistenceStrategy.Globals.CHANGE,
                new PersistenceCronStrategy("everyMinute", "0 * * * * ?"));
    }
}
