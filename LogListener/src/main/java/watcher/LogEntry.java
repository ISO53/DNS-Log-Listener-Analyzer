package watcher;

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
        this.questionName = informations[16];
    }
}
