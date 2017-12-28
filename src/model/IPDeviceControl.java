
package model;

public class IPDeviceControl {
    private String IP;
    private int port;
    private int socket;
    
    // command list

    public IPDeviceControl(String IP, int port) {
        this.IP = IP;
        this.port = port;
    }

    public IPDeviceControl(String ip) {
        this.IP = ip;
    }
    
    
}
