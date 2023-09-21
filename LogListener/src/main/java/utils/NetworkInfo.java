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

    /**
     * This method attempts to perform a hostname lookup for the provided IP address. If successful, it returns the
     * hostname as a String. If the lookup fails or the IP address is not resolvable, it logs a warning message and
     * returns null.
     *
     * @return The retrieved hostname as a String if the lookup is successful, or null if the lookup fails or an error
     * occurs.
     */
    private @Nullable String findHostname() {
        try {
            return InetAddress.getByName(this.ip).getHostName();
        } catch (UnknownHostException e) {
            GlobalLogger.getLoggerInstance().log(Level.WARN, "An error occurred trying to get hostname:", e);
        }

        return null;
    }

    /**
     * This method attempts to obtain the local IP address of the host machine. If successful, it returns the local IP
     * address as a String. If an error occurs or the local IP address cannot be determined, it logs a warning message
     * and returns null.
     *
     * @return The local IP address as a String if retrieval is successful, or null if an error occurs or the local IP
     * address cannot be determined.
     */
    private @Nullable String findLocalIp() {
        try {
            return InetAddress.getByName(ip).getHostAddress();
        } catch (UnknownHostException e) {
            GlobalLogger.getLoggerInstance().log(Level.WARN, "An error occurred trying to get local ip:", e);
        }

        return null;
    }

    /**
     * This method attempts to obtain the MAC address of the network interface corresponding to the provided IP address.
     * If successful, it returns the MAC address as a formatted String. If the MAC address is not available due to
     * elevated privileges being required or if an error occurs during the retrieval process, it logs a warning message
     * and returns null.
     *
     * @return The MAC address as a formatted String if retrieval is successful, or null if the MAC address is not available
     * (requires elevated privileges) or an error occurs during retrieval.
     */
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
