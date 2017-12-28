package model;

import static embeddedcontroller.EmbeddedController.DEBUG;
import java.util.logging.Level;

public class SystemDefinitions {
    // Types of devices 
    public static enum DEVICE_TYPE{
        IP_DEVICE,
        SERIAL_DEVICE
    }


    public static boolean validateIpAddress(String ip){
        if(ip.length()>0){
            //[TODO] Check IP address
            
            DEBUG.log(Level.INFO,"Valid IP Address");
            return true;
        }
        DEBUG.log(Level.WARNING,"Invalid IP Address");
        return false;
    }
}
