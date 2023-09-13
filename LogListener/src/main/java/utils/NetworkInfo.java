package utils;

import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.apache.logging.log4j.Level;

public class NetworkInfo {

    private String ip;
    private String hostname;
    private String localIp;
    private String macAddress;

    public NetworkInfo(String ip) {
        this.ip = ip;
        this.hostname = findHostname();
        this.localIp = findLocalIp();
        this.macAddress = findMacAddress();
    }

    public NetworkInfo() {
        super();
    }

    private @Nullable String findHostname() {
        try {
            return InetAddress.getByName(this.ip).getHostName();
        } catch (UnknownHostException e) {
            GlobalLogger.getLoggerInstance().log(Level.WARN, "An error occurred trying to get hostname:", e);
        }

        return null;
    }

    private @Nullable String findLocalIp() {
        try {
            return InetAddress.getByName(ip).getHostAddress();
        } catch (UnknownHostException e) {
            GlobalLogger.getLoggerInstance().log(Level.WARN, "An error occurred trying to get local ip:", e);
        }

        return null;
    }

    private @Nullable String findMacAddress() {
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
                    GlobalLogger.getLoggerInstance().log(Level.WARN, "MAC Address: Not available (requires elevated privileges)");
                }
            } catch (SocketException e) {
                GlobalLogger.getLoggerInstance().log(Level.WARN, "SocketException - MAC Address: Not available (requires elevated privileges)", e);
            }
        } catch (Exception e) {
            GlobalLogger.getLoggerInstance().log(Level.WARN, "Exception - MAC Address: Not available (requires elevated privileges)", e);
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
