package watcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogEntry {

    private String date;                    // 11/17/2021
    private String time;                    // 6:00:00 AM
    private String threadId;                // 0D0C
    private String context;                 // PACKET
    private String internalPacketId;        // 00000272D98DD0B0
    private String udpTcpIndicator;         // UDP
    private String sendReceiveIndicator;    // Rcv
    private String remoteIp;                // 192.168.13.130
    private String xidHex;                  // 0002
    private String queryResponse;           // Q
    private String opcode;                  // 0001
    private String flagsHex;                // D
    private String flagsChar;               // NOERROR
    private String responseCode;            // A
    private String questionType;            // (8)woshub(2)com(0)
    private String questionName;            // woshub.com
    private String localIp;
    private String hostAddress;
    private String macAddress;

    // 11/17/2021 6:00:00 AM 0D0C PACKET 00000272D98DD0B0 UDP Rcv 192.168.13.130 0002 Q [0001 D NOERROR] A (8)woshub(2)com(0)
    // 08/24/2023 03:38:12 PM 000C21F0 PACKET 192.168.87.125 UDP Rcv 192.168.87.125 0002 Q [0001 D NOERROR] CNAME (15)ixutlvqgwnhzarq(0)

    public LogEntry(String[] informations) {
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
        this.opcode = informations[11];
        this.flagsHex = informations[12];
        this.flagsChar = informations[13];
        this.responseCode = informations[14];
        this.questionType = informations[15];
        this.questionName = parseDNS(informations[15]);
        this.localIp = null;
        this.hostAddress = null;
        this.macAddress = null;
    }

    public String parseDNS(String dns) {
        Pattern pattern = Pattern.compile("\\((\\d+)\\)([^\\(\\)]+)");
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

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setInternalPacketId(String internalPacketId) {
        this.internalPacketId = internalPacketId;
    }

    public void setUdpTcpIndicator(String udpTcpIndicator) {
        this.udpTcpIndicator = udpTcpIndicator;
    }

    public void setSendReceiveIndicator(String sendReceiveIndicator) {
        this.sendReceiveIndicator = sendReceiveIndicator;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public void setXidHex(String xidHex) {
        this.xidHex = xidHex;
    }

    public void setQueryResponse(String queryResponse) {
        this.queryResponse = queryResponse;
    }

    public void setOpcode(String opcode) {
        this.opcode = opcode;
    }

    public void setFlagsHex(String flagsHex) {
        this.flagsHex = flagsHex;
    }

    public void setFlagsChar(String flagsChar) {
        this.flagsChar = flagsChar;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public void setQuestionName(String questionName) {
        this.questionName = questionName;
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

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getThreadId() {
        return threadId;
    }

    public String getContext() {
        return context;
    }

    public String getInternalPacketId() {
        return internalPacketId;
    }

    public String getUdpTcpIndicator() {
        return udpTcpIndicator;
    }

    public String getSendReceiveIndicator() {
        return sendReceiveIndicator;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public String getXidHex() {
        return xidHex;
    }

    public String getQueryResponse() {
        return queryResponse;
    }

    public String getOpcode() {
        return opcode;
    }

    public String getFlagsHex() {
        return flagsHex;
    }

    public String getFlagsChar() {
        return flagsChar;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getQuestionType() {
        return questionType;
    }

    public String getQuestionName() {
        return questionName;
    }

    public String getLocalIp() {
        return localIp;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }
}
