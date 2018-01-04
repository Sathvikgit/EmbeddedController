package embeddedcontroller;

import static embeddedcontroller.EmbeddedController.DEBUG;
import static embeddedcontroller.SystemStatus.*;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.logging.Level;

public class Utilities {

    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x", b));
            builder.append(" ");
        }
        return builder.toString();
    }

    public static boolean get_system_ip() {
        DEBUG.log(Level.INFO,"Getting Sytem IP address");
        String _ipv4_host = "";
        InetAddress test_inet = null;
        NetworkInterface system_network_link;
        String networK_interface_name = null;

        try {
            // get localhost and host IP address
            test_inet = InetAddress.getLocalHost();

            if (test_inet != null) {
                DEBUG.log(Level.INFO,"Local Host Name: {0}",test_inet);
                
                _ipv4_address = test_inet.getHostAddress();
                // Save System IP Address
                ipv4_system = _ipv4_address;
                if (_ipv4_address != null) {
                    // Grab value till last . in ipaddress i.e if ip=192.168.123.157 then
                    // after this statement  _ipv4_address = 192.168.123
                    _ipv4_address = _ipv4_address.substring(0, _ipv4_address.lastIndexOf("."));
                    // add last . manually
                    _ipv4_address += "."; 
                    
                    DEBUG.log(Level.INFO,"IPV4 Address: {0}",ipv4_system);
                }

                system_network_link = NetworkInterface.getByInetAddress(test_inet);
                // get NetworkInterface name from IP address 
                if (system_network_link != null) {
                    networK_interface_name = system_network_link.getName();
                }
                
                 DEBUG.log(Level.INFO,"NetworkInterfaceName: {0}",networK_interface_name);

            }
        } catch (Exception e) {
            if (networK_interface_name == null) {
                DEBUG.log(Level.SEVERE,e.getMessage());
            }
            return false;
        }

        // check if IP address is local loop interface address. i.e. 127.0.0.1
        // Or if Interface is null; return false if any of those is true.
        if (test_inet != null) {
            if (networK_interface_name == null
                    || test_inet.getHostAddress().toString().contentEquals("127.0.0.1")) {
                DEBUG.log(Level.INFO,"Only local loop address found returning with error.");
                return false;
            }
        }
        return true;
    }
    
    
    public static BufferedImage imageResize(BufferedImage img, int newW, int newH) {
        
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return dimg;
    }
    
}
