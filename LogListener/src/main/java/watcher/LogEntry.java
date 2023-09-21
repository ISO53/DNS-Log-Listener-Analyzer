package watcher;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogEntry {

    private final UUID id;                        // UUID
    private final String date;                    // 11/17/2021
    private final String time;                    // 6:00:00 AM
    private final String threadId;                // 0D0C
    private final String context;                 // PACKET
    private final String internalPacketId;        // 00000272D98DD0B0
    private final String udpTcpIndicator;         // UDP
    private final String sendReceiveIndicator;    // Rcv
    private final String remoteIp;                // 192.168.13.130
    private final String xidHex;                  // 0002
    private final String queryResponse;           // Q
    private final String opcode;                  // 0001
    private final String flagsHex;                // D
    private final String flagsChar;               // NOERROR
    private final String responseCode;            // A
    private final String questionType;            // (8)woshub(2)com(0)
    private final String questionName;            // woshub.com
    private String localIp;
    private String hostAddress;
    private String macAddress;

    // 11/17/2021 6:00:00 AM 0D0C PACKET 00000272D98DD0B0 UDP Rcv 192.168.13.130 0002 Q [0001 D NOERROR] A (8)woshub(2)com(0)
    // 08/24/2023 03:38:12 PM 000C21F0 PACKET 192.168.87.125 UDP Rcv 192.168.87.125 0002 Q [0001 D NOERROR] CNAME (15)ixutlvqgwnhzarq(0)

    public LogEntry(String[] informations) {
        this.id = UUID.randomUUID();
        this.date = informations[0];
        this.time = informations[1] + " " + informations[2];
        this.threadId = informations[3];
        this.context = informations[4];
        this.internalPacketId = informations[5];
        this.udpTcpIndicator = informations[6];
        this.sendReceiveIndicator = informations[7];
        this.remoteIp = informations[8];
        this.xidHex = informations[9];
        this.queryResponse = informations[10];
        this.opcode = informations[11].substring(1);
        this.flagsHex = informations[12];
        this.flagsChar = informations[13].substring(0, informations[13].length() - 1);
        this.responseCode = informations[14];
        this.questionType = informations[15];
        this.questionName = parseDNS(informations[15]);
        this.localIp = null;
        this.hostAddress = null;
        this.macAddress = null;
    }

    /**
     * This method takes a DNS string in the format "(d1)d2(d3)d4...", where "(d1)", "(d2)", "(d3)", etc., represent
     * numeric values enclosed in parentheses, followed by non-parenthesized domain components. It extracts and concatenates
     * the non-parenthesized domain components, separated by dots ('.'), and returns the result as a single string.
     *
     * @param dns The DNS string to be parsed.
     * @return A concatenated string of non-parenthesized domain components, separated by dots ('.').
     */
    public String parseDNS(String dns) {
        Pattern pattern = Pattern.compile("\\((\\d+)\\)([^()]+)");
        Matcher matcher = pattern.matcher(dns);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            result.append(matcher.group(2));

            if (matcher.find()) {
                result.append(".");
            }
        }

        return result.toString();
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public UUID getId() {
        return id;
    }

    public String getRemoteIp() {
        return remoteIp;
    }
}
