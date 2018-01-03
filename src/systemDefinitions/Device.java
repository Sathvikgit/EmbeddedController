package systemDefinitions;

/* This holds the information of HR_device class found  by the device finder*/
public class Device {
     public HR_Device info;
     
     // Each Device has a HTTP client that pull the information from Hall Research Web Site

    public Device(HR_Device dev) {
        this.info = dev;
    }

    /* This String will be acts as a name in Device Tree*/
    @Override
    public String toString() {
        return this.info.get_device_name()+"  ["+this.info.get_device_model()+"]";
    }
       
}
