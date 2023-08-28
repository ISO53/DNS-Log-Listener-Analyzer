package utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class NetworkInfo {

    String ip;
    String hostname;
    String localIp;
    String macAddress;

    public NetworkInfo(String ip) {
        this.ip = ip;
        this.hostname = findHostname();
        this.localIp = findLocalIp();
        this.macAddress = findMacAddress();
    }

    public String findHostname() {
        try {
            return InetAddress.getByName(this.ip).getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String findLocalIp() {
        try {
            return InetAddress.getByName(ip).getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String findMacAddress() {
        try {
            // MAC address (requires elevated privileges)
            try {
                NetworkInterface networkInterface = NetworkInterface.getByInetAddress(InetAddress.getByName(ip));
                byte[] macBytes = networkInterface.getHardwareAddress();
                StringBuilder macAddress = new StringBuilder();
                if (macBytes != null) {
                    for (byte b : macBytes) {
                        macAddress.append(String.format("%02X:", b));
                    }
                    macAddress.setLength(macAddress.length() - 1); // Remove the trailing colon
                    return macAddress.toString();
                } else {
                    System.out.println("MAC Address: Not available (requires elevated privileges)");
                }
            } catch (SocketException e) {
                System.out.println("SocketException - MAC Address: Not available (requires elevated privileges)");
            }
        } catch (Exception e) {
            System.out.println("Exception - MAC Address: Not available (requires elevated privileges)");
        }

        return null;
    }

    public String getIp() {
        return this.ip;
    }

    public String getHostname() {
        return hostname;
    }

    public String getLocalIp() {
        return localIp;
    }

    public String getMacAddress() {
        return macAddress;
    }
}
