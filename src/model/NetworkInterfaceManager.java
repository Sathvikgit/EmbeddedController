
package model;

import static embeddedcontroller.EmbeddedController.DEBUG;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;


public class NetworkInterfaceManager {

    public static String HOST_NAME= null;
    public static String LOCAL_HOST_ADDRESS= null;
    public static Enumeration<NetworkInterface> nets = null;
    
    public NetworkInterfaceManager() {
        if(listNetworkInterface()){
            for(NetworkInterface ni : Collections.list(nets)){
                
                System.out.println("Index: "+ni.getIndex());
                System.err.println("Name: "+ni.getName());
                System.err.println("Display Name: "+ni.getDisplayName());
                try {
                    System.err.println("Harware Address : "+Arrays.toString(ni.getHardwareAddress()));
                } catch (SocketException ex) {
                    System.out.println("Harware Address : Failed! -"+ex.getMessage());
                }
                System.out.println("More Info: "+ni.toString());                
            }
            
        }else{
            throw new NullPointerException("Failed to enumerate network interfaces");
        }
    }
    
    private static boolean listNetworkInterface(){
         InetAddress ipaddress;
        try {
            ipaddress = InetAddress.getLocalHost();
            HOST_NAME = ipaddress.getHostName();
            LOCAL_HOST_ADDRESS = ipaddress.getHostAddress();
            
        } catch (UnknownHostException ex) {
            DEBUG.log(Level.WARNING,"Unknown Host: {0}",ex.getMessage());
        }
         
        
        try {
            nets = NetworkInterface.getNetworkInterfaces();
            return true;
        } catch (SocketException ex) {
            DEBUG.log(Level.SEVERE,"Failed to enumerate Network Interfaces: {0}", ex.getMessage());
            return false;
        }
    }
    
}
