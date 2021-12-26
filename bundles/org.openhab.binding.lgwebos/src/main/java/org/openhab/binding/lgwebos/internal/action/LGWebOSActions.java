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
package org.openhab.binding.lgwebos.internal.action;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSHandler;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSTVMouseSocket.ButtonType;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSTVSocket;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSTVSocket.State;
import org.openhab.binding.lgwebos.internal.handler.command.ServiceSubscription;
import org.openhab.binding.lgwebos.internal.handler.core.AppInfo;
import org.openhab.binding.lgwebos.internal.handler.core.ResponseListener;
import org.openhab.binding.lgwebos.internal.handler.core.TextInputStatusInfo;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * The {@link LGWebOSActions} defines the thing actions for the LGwebOS binding.
 *
 * @author Sebastian Prehn - Initial contribution
 * @author Laurent Garnier - new method invokeMethodOf + interface ILGWebOSActions
 */
@ThingActionsScope(name = "lgwebos")
@NonNullByDefault
public class LGWebOSActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(LGWebOSActions.class);
    private final ResponseListener<TextInputStatusInfo> textInputListener = createTextInputStatusListener();
    private @Nullable LGWebOSHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (LGWebOSHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    // a NonNull getter for handler
    private LGWebOSHandler getLGWebOSHandler() {
        LGWebOSHandler lgWebOSHandler = this.handler;
        if (lgWebOSHandler == null) {
            throw new IllegalStateException(
                    "ThingHandler must be set before any action may be invoked on LGWebOSActions.");
        }
        return lgWebOSHandler;
    }

    private enum Button {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        BACK,
        DELETE,
        ENTER,
        HOME,
        OK
    }

    @RuleAction(label = "@text/actionShowToastLabel", description = "@text/actionShowToastDesc")
    public void showToast(
            @ActionInput(name = "text", label = "@text/actionShowToastInputTextLabel", description = "@text/actionShowToastInputTextDesc") String text)
            throws IOException {
        getConnectedSocket().ifPresent(control -> control.showToast(text, createResponseListener()));
    }

    @RuleAction(label = "@text/actionShowToastWithIconLabel", description = "@text/actionShowToastWithIconDesc")
    public void showToast(
            @ActionInput(name = "icon", label = "@text/actionShowToastInputIconLabel", description = "@text/actionShowToastInputIconDesc") String icon,
            @ActionInput(name = "text", label = "@text/actionShowToastInputTextLabel", description = "@text/actionShowToastInputTextDesc") String text)
            throws IOException {
        BufferedImage bi = ImageIO.read(new URL(icon));
        try (ByteArrayOutputStream os = new ByteArrayOutputStream(); OutputStream b64 = Base64.getEncoder().wrap(os)) {
            ImageIO.write(bi, "png", b64);
            String string = os.toString(StandardCharsets.UTF_8.name());
            getConnectedSocket().ifPresent(control -> control.showToast(text, string, "png", createResponseListener()));
        }
    }

    @RuleAction(label = "@text/actionLaunchBrowserLabel", description = "@text/actionLaunchBrowserDesc")
    public void launchBrowser(
            @ActionInput(name = "url", label = "@text/actionLaunchBrowserInputUrlLabel", description = "@text/actionLaunchBrowserInputUrlDesc") String url) {
        getConnectedSocket().ifPresent(control -> control.launchBrowser(url, createResponseListener()));
    }

    private List<AppInfo> getAppInfos() {
        LGWebOSHandler lgWebOSHandler = getLGWebOSHandler();

        if (!this.getConnectedSocket().isPresent()) {
            return Collections.emptyList();
        }

        List<AppInfo> appInfos = lgWebOSHandler.getLauncherApplication()
                .getAppInfos(lgWebOSHandler.getThing().getUID());
        if (appInfos == null) {
            logger.warn("No AppInfos found for device with ThingID {}.", lgWebOSHandler.getThing().getUID());
            return Collections.emptyList();
        }
        return appInfos;
    }

    @RuleAction(label = "@text/actionLaunchApplicationLabel", description = "@text/actionLaunchApplicationDesc")
    public void launchApplication(
            @ActionInput(name = "appId", label = "@text/actionLaunchApplicationInputAppIDLabel", description = "@text/actionLaunchApplicationInputAppIDDesc") String appId) {
        Optional<AppInfo> appInfo = getAppInfos().stream().filter(a -> a.getId().equals(appId)).findFirst();
        if (appInfo.isPresent()) {
            getConnectedSocket()
                    .ifPresent(control -> control.launchAppWithInfo(appInfo.get(), createResponseListener()));
        } else {
            logger.warn("Device with ThingID {} does not support any app with id: {}.",
                    getLGWebOSHandler().getThing().getUID(), appId);
        }
    }

    @RuleAction(label = "@text/actionLaunchApplicationWithParamsLabel", description = "@text/actionLaunchApplicationWithParamsDesc")
    public void launchApplication(
            @ActionInput(name = "appId", label = "@text/actionLaunchApplicationInputAppIDLabel", description = "@text/actionLaunchApplicationInputAppIDDesc") String appId,
            @ActionInput(name = "params", label = "@text/actionLaunchApplicationInputParamsLabel", description = "@text/actionLaunchApplicationInputParamsDesc") String params) {
        try {
            JsonObject payload = (JsonObject) JsonParser.parseString(params);

            Optional<AppInfo> appInfo = getAppInfos().stream().filter(a -> a.getId().equals(appId)).findFirst();
            if (appInfo.isPresent()) {
                getConnectedSocket().ifPresent(
                        control -> control.launchAppWithInfo(appInfo.get(), payload, createResponseListener()));
            } else {
                logger.warn("Device with ThingID {} does not support any app with id: {}.",
                        getLGWebOSHandler().getThing().getUID(), appId);
            }
        } catch (JsonParseException ex) {
            logger.warn("Parameters value ({}) is not in a valid JSON format. {}", params, ex.getMessage());
            return;
        }
    }

    @RuleAction(label = "@text/actionSendTextLabel", description = "@text/actionSendTextDesc")
    public void sendText(
            @ActionInput(name = "text", label = "@text/actionSendTextInputTextLabel", description = "@text/actionSendTextInputTextDesc") String text) {
        getConnectedSocket().ifPresent(control -> {
            ServiceSubscription<TextInputStatusInfo> subscription = control.subscribeTextInputStatus(textInputListener);
            control.sendText(text);
            control.unsubscribe(subscription);
        });
    }

    @RuleAction(label = "@text/actionSendButtonLabel", description = "@text/actionSendButtonDesc")
    public void sendButton(
            @ActionInput(name = "text", label = "@text/actionSendButtonInputButtonLabel", description = "@text/actionSendButtonInputButtonDesc") String button) {
        try {
            switch (Button.valueOf(button)) {
                case UP:
                    getConnectedSocket().ifPresent(control -> control.executeMouse(s -> s.button(ButtonType.UP)));
                    break;
                case DOWN:
                    getConnectedSocket().ifPresent(control -> control.executeMouse(s -> s.button(ButtonType.DOWN)));
                    break;
                case LEFT:
                    getConnectedSocket().ifPresent(control -> control.executeMouse(s -> s.button(ButtonType.LEFT)));
                    break;
                case RIGHT:
                    getConnectedSocket().ifPresent(control -> control.executeMouse(s -> s.button(ButtonType.RIGHT)));
                    break;
                case BACK:
                    getConnectedSocket().ifPresent(control -> control.executeMouse(s -> s.button(ButtonType.BACK)));
                    break;
                case DELETE:
                    getConnectedSocket().ifPresent(control -> control.sendDelete());
                    break;
                case ENTER:
                    getConnectedSocket().ifPresent(control -> control.sendEnter());
                    break;
                case HOME:
                    getConnectedSocket().ifPresent(control -> control.executeMouse(s -> s.button("HOME")));
                    break;
                case OK:
                    getConnectedSocket().ifPresent(control -> control.executeMouse(s -> s.click()));
                    break;
            }
        } catch (IllegalArgumentException ex) {
            logger.warn("{} is not a valid value for button - available are: {}", button,
                    Stream.of(Button.values()).map(b -> b.name()).collect(Collectors.joining(", ")));
        }
    }

    @RuleAction(label = "@text/actionIncreaseChannelLabel", description = "@text/actionIncreaseChannelDesc")
    public void increaseChannel() {
        getConnectedSocket().ifPresent(control -> control.channelUp(createResponseListener()));
    }

    @RuleAction(label = "@text/actionDecreaseChannelLabel", description = "@text/actionDecreaseChannelDesc")
    public void decreaseChannel() {
        getConnectedSocket().ifPresent(control -> control.channelDown(createResponseListener()));
    }

    @RuleAction(label = "@text/actionSendRCButtonLabel", description = "@text/actionSendRCButtonDesc")
    public void sendRCButton(
            @ActionInput(name = "text", label = "@text/actionSendRCButtonInputTextLabel", description = "@text/actionSendRCButtonInputTextDesc") String rcButton) {
        getConnectedSocket().ifPresent(control -> control.executeMouse(s -> s.button(rcButton)));
    }

    private Optional<LGWebOSTVSocket> getConnectedSocket() {
        LGWebOSHandler lgWebOSHandler = getLGWebOSHandler();
        final LGWebOSTVSocket socket = lgWebOSHandler.getSocket();

        if (socket.getState() != State.REGISTERED) {
            logger.warn("Device with ThingID {} is currently not connected.", lgWebOSHandler.getThing().getUID());
            return Optional.empty();
        }

        return Optional.of(socket);
    }

    private ResponseListener<TextInputStatusInfo> createTextInputStatusListener() {
        return new ResponseListener<TextInputStatusInfo>() {

            @Override
            public void onError(@Nullable String error) {
                logger.warn("Response: {}", error);
            }

            @Override
            public void onSuccess(@Nullable TextInputStatusInfo info) {
                logger.debug("Response: {}", info == null ? "OK" : info.getRawData());
            }
        };
    }

    private <O> ResponseListener<O> createResponseListener() {
        return new ResponseListener<O>() {

            @Override
            public void onError(@Nullable String error) {
                logger.warn("Response: {}", error);
            }

            @Override
            public void onSuccess(@Nullable O object) {
                logger.debug("Response: {}", object == null ? "OK" : object.toString());
            }
        };
    }

    // delegation methods for "legacy" rule support

    public static void showToast(ThingActions actions, String text) throws IOException {
        ((LGWebOSActions) actions).showToast(text);
    }

    public static void showToast(ThingActions actions, String icon, String text) throws IOException {
        ((LGWebOSActions) actions).showToast(icon, text);
    }

    public static void launchBrowser(ThingActions actions, String url) {
        ((LGWebOSActions) actions).launchBrowser(url);
    }

    public static void launchApplication(ThingActions actions, String appId) {
        ((LGWebOSActions) actions).launchApplication(appId);
    }

    public static void launchApplication(ThingActions actions, String appId, String param) {
        ((LGWebOSActions) actions).launchApplication(appId, param);
    }

    public static void sendText(ThingActions actions, String text) {
        ((LGWebOSActions) actions).sendText(text);
    }

    public static void sendButton(ThingActions actions, String button) {
        ((LGWebOSActions) actions).sendButton(button);
    }

    public static void increaseChannel(ThingActions actions) {
        ((LGWebOSActions) actions).increaseChannel();
    }

    public static void decreaseChannel(ThingActions actions) {
        ((LGWebOSActions) actions).decreaseChannel();
    }

    public static void sendRCButton(ThingActions actions, String rcButton) {
        ((LGWebOSActions) actions).sendRCButton(rcButton);
    }
}
