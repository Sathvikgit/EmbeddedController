package model;
import static embeddedcontroller.EmbeddedController.DEBUG;
import java.util.logging.Level;
import model.SystemDefinitions.*;
import static model.SystemDefinitions.DEVICE_TYPE.*;
import static model.SystemDefinitions.*;

public class Device {
    
    private String DeviceName;
    private DEVICE_TYPE Type;    
    private String DeviceLocation;
    private String DeviceInfo;
    
    // you have to raise this flag afer creating a device 
    private boolean validDevice = false;
    // This Device how to control this device
    private Object Control;
    

    private Device(String name, DEVICE_TYPE type, String location, String info) {
        this.DeviceName = name;
        this.Type = type;
        this.DeviceInfo = info;
        this.DeviceLocation=location;
    }

    // Creating an IP Device 
    public Device(String name, DEVICE_TYPE type, String ip, int port){
        this(name, type,"", "");
        if(type == IP_DEVICE){
            createIpControl(ip, port);
        }else{
                DEBUG.log(Level.WARNING,"Invalid IP Device");
        }
    }
    
  
    private void createIpControl(String ip, int port){
       //Validate Ip address
        if(validateIpAddress(ip)){
            Control = new IPDeviceControl(ip,port);
         }
    }

    
}
