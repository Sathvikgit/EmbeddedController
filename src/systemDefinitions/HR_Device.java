
package systemDefinitions;

public class HR_Device {
    private String Device_Name = null;
    private String Device_Ip_Address = null;
    private String Device_Location = null;
    private String Device_Mac_Address = null;
    private String Device_Model = null;
    private String Device_Description = null;
    private String Device_password_hash = null;
    private String Device_ip_type = null;
    
    public HR_Device(String D_ip, String D_ip_type , String D_mac, String D_model, String D_name, String D_loc, String D_des, String D_pass){
        
        /* Copy all information received about Device */
        this.Device_Ip_Address      = D_ip;
        this.Device_ip_type         = D_ip_type;
        this.Device_Mac_Address     = D_mac;
        this.Device_Model           = D_model;
        this.Device_Name            = D_name;
        this.Device_Location        = D_loc;
        this.Device_Description     = D_des;
        this.Device_password_hash   = D_pass;
        
    }

    @Override
    public String toString() {
        return "HR_Device{" + "Device_Name=" + Device_Name + ", Device_Ip_Address=" + Device_Ip_Address + ", Device_Location=" + Device_Location + ", Device_Mac_Address=" + Device_Mac_Address + ", Device_Model=" + Device_Model + ", Device_Description=" + Device_Description + ", Device_password_hash=" + Device_password_hash + ", Device_ip_type=" + Device_ip_type + '}';
    }
    
    /**
     * Read device name. 
     * @return String - Device Name 
     */
    public String get_device_name() {
        return Device_Name;
    }

    /**
     * Read IP address of device.
     * @return String - IP address
     */
    public String get_device_ip() {
        return Device_Ip_Address;
    }
    
    /**
     * Read device location.
     * @return String - Device Location
     */
    public String get_device_location() {
        return Device_Location;
    }
    
    /**
     * Read mac address of Device
     * @return String - mac address 
     */
    public String get_device_mac() {
        return Device_Mac_Address;
    }
    /**
     * Read Device model name. 
     * @return String - Device Model name
     */
    public String get_device_model() {
        return Device_Model;
    }
    
    /**
     * Read Device description/ firmware version.
     * @return String - Device description/firmware version
     */
    public String get_device_description() {
        return Device_Description;
    }
    
    /**
     * Read device password.
     * @return String - Device password. 
     */
    public String get_device_pass() {
        return Device_password_hash;
    }
    
    /**
     * Read Device IP Address type.
     * @return String - IP type STATIC/DHCP
     */
    public String get_device_iptype() {
        return Device_ip_type;
    }
    
    /**
     * Set Device user defined name.
     * @param name Device name String max limit 16 chars. 
     */
    public void set_device_name(String name) {
        Device_Name = name;
    }
    
    /**
     * Set Static IP address.
     * @param IP IP address string to set. 
     */
    public void set_device_ip(String IP) {
        Device_Ip_Address = IP;
    }
    
    /**
     * Set device location. 
     * @param location Device Location string max limit 20 chars.
     */
    public void set_device_location(String location) {
        Device_Location = location;
    }
    
    /**
     * Set device mac address.
     * @param mac mac address to set as String.
     */
    public void set_device_mac(String mac) {
        Device_Mac_Address = mac;
    }
    
    /**
     * Set device model name.
     * @param model Model name to set as a String. 
     */
    public void set_device_model(String model) {
        Device_Model = model;
    }
    
    /**
     * Set device description. 
     * @param Description Description to set as a string. 
     */
    public void set_device_description(String Description) {
        Device_Description = Description;
    }
    
    /**
     * Set device password.
     * @param pass  Device password to set.
     */
    public void set_device_password(String pass) {
        Device_password_hash = pass;
    }
}
