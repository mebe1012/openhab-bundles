/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.philipstv.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.regex.Pattern;

/**
 * The {@link PowerUtil} is offering methods for powering on TVs via Wake-On-LAN.
 * @author Benjamin Meyer - Initial contribution
 */
public final class PowerUtil {

  private static final int WOL_PORT = 9;

  private static final Pattern MAC_PATTERN = Pattern.compile("([:\\-])");

  private PowerUtil() {
  }

  public static void wakeOnLan(String ip, String mac) throws IOException {
    InetAddress address = InetAddress.getByName(ip);
    byte[] macBytes = getMacBytes(mac);
    byte[] bytes = new byte[6 + (16 * macBytes.length)];
    for (int i = 0; i < 6; i++) {
      bytes[i] = (byte) 0xff;
    }
    for (int i = 6; i < bytes.length; i += macBytes.length) {
      System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
    }

    DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, WOL_PORT);
    try (DatagramSocket socket = new DatagramSocket()) {
      socket.send(packet);
    }
  }

  private static byte[] getMacBytes(String macStr) {
    byte[] bytes = new byte[6];
    String[] hex = MAC_PATTERN.split(macStr);
    if (hex.length != 6) {
      throw new IllegalArgumentException("Invalid MAC address.");
    }
    try {
      for (int i = 0; i < 6; i++) {
        bytes[i] = (byte) Integer.parseInt(hex[i], 16);
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid hex digit in MAC address.");
    }
    return bytes;
  }

  public static boolean isReachable(String ipAddress) throws IOException {
      InetAddress inetAddress = InetAddress.getByName(ipAddress);
     return inetAddress.isReachable(2000);
  }

}
