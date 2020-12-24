/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.device;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon.internal.utils.Utils;
import org.openhab.binding.insteon.internal.utils.Utils.ParsingException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class that loads the device feature templates from an xml stream
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
public class FeatureTemplateLoader {
    public static List<FeatureTemplate> readTemplates(InputStream input) throws IOException, ParsingException {
        List<FeatureTemplate> features = new ArrayList<>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            // Parse it!
            Document doc = dBuilder.parse(input);
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();

            NodeList nodes = root.getChildNodes();

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;
                    if (e.getTagName().equals("feature")) {
                        features.add(parseFeature(e));
                    }
                }
            }
        } catch (SAXException e) {
            throw new ParsingException("Failed to parse XML!", e);
        } catch (ParserConfigurationException e) {
            throw new ParsingException("Got parser config exception! ", e);
        }
        return features;
    }

    private static FeatureTemplate parseFeature(Element e) throws ParsingException {
        String name = e.getAttribute("name");
        boolean statusFeature = e.getAttribute("statusFeature").equals("true");
        FeatureTemplate feature = new FeatureTemplate(name, statusFeature, e.getAttribute("timeout"));

        NodeList nodes = e.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) node;
                if (child.getTagName().equals("message-handler")) {
                    parseMessageHandler(child, feature);
                } else if (child.getTagName().equals("command-handler")) {
                    parseCommandHandler(child, feature);
                } else if (child.getTagName().equals("message-dispatcher")) {
                    parseMessageDispatcher(child, feature);
                } else if (child.getTagName().equals("poll-handler")) {
                    parsePollHandler(child, feature);
                }
            }
        }

        return feature;
    }

    private static HandlerEntry makeHandlerEntry(Element e) throws ParsingException {
        String handler = e.getTextContent();
        if (handler == null) {
            throw new ParsingException("Could not find Handler for: " + e.getTextContent());
        }

        NamedNodeMap attributes = e.getAttributes();
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node n = attributes.item(i);
            params.put(n.getNodeName(), n.getNodeValue());
        }
        return new HandlerEntry(handler, params);
    }

    private static void parseMessageHandler(Element e, FeatureTemplate f) throws DOMException, ParsingException {
        HandlerEntry he = makeHandlerEntry(e);
        if (e.getAttribute("default").equals("true")) {
            f.setDefaultMessageHandler(he);
        } else {
            String attr = e.getAttribute("cmd");
            int command = (attr == null) ? 0 : Utils.from0xHexString(attr);
            f.addMessageHandler(command, he);
        }
    }

    private static void parseCommandHandler(Element e, FeatureTemplate f) throws ParsingException {
        HandlerEntry he = makeHandlerEntry(e);
        if (e.getAttribute("default").equals("true")) {
            f.setDefaultCommandHandler(he);
        } else {
            Class<? extends Command> command = parseCommandClass(e.getAttribute("command"));
            f.addCommandHandler(command, he);
        }
    }

    private static void parseMessageDispatcher(Element e, FeatureTemplate f) throws DOMException, ParsingException {
        HandlerEntry he = makeHandlerEntry(e);
        f.setMessageDispatcher(he);
    }

    private static void parsePollHandler(Element e, FeatureTemplate f) throws ParsingException {
        HandlerEntry he = makeHandlerEntry(e);
        f.setPollHandler(he);
    }

    private static Class<? extends Command> parseCommandClass(String c) throws ParsingException {
        if (c.equals("OnOffType")) {
            return OnOffType.class;
        } else if (c.equals("PercentType")) {
            return PercentType.class;
        } else if (c.equals("DecimalType")) {
            return DecimalType.class;
        } else if (c.equals("IncreaseDecreaseType")) {
            return IncreaseDecreaseType.class;
        } else {
            throw new ParsingException("Unknown Command Type");
        }
    }
}
