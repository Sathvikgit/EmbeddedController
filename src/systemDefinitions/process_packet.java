/*
 * File process_packet.java
 *  This file contains process_packet class which
 *  implements runnable interface. It processes responses 
 *  from all Hall Research devices on network. 
 *
 * Author Vishal <vishal@hallresearch.com>
 * 
 * Classname process_packet
 * 
 * Version 1.0
 * 
 * Date June 5, 2011, 3:39:59PM
 * 
 * Copyright © 2011-2012 Hall Research. All rights reserved.  
 */

package systemDefinitions;
import static embeddedcontroller.EmbeddedController.DEBUG;
import static embeddedcontroller.EmbeddedController.DF;
import java.net.*;
import java.util.logging.Level;

/**
 * Decodes packets from HR devices and read information then push it to
 * ArrayList in Device_info class. This class implements runnable and it runs
 * independently while other keeping GUI alive. This class decodes packets from
 * responding HR devices. It checks if packet has required MD5 ID and also
 * checks after decoding if packet has other necessary fields. Upon confirming
 * it is packet from Hall Research device, it creates instance of HR_Devices
 * class and assigns received values to its attributes. This HR_Devices instance
 * is added to the ArrayList in Device_info.
 */

public class process_packet implements Runnable{
    
    private DatagramPacket client_packet = null;
    private String Decrypted_data = null;
    private String Encrypted_data = null;
    private String Received_data = null;
    private int client_port = 0;
    private String client_ip = null;
    
    private String Packet_ID = null;
    private String Device_Password = null;
    
    private String packet_type;

    public process_packet(DatagramPacket P, String Packet_Type) {
        client_packet = P; 
        this.packet_type = Packet_Type;
    }

    /**
     * Decrypt Packet. This method decrypts packets received in response to scan
     * request.
     *
     * @return Decrypted message string
     */
    public String Decrypt_Data() {

        DEBUG.log(Level.INFO,"process_packet::Decrypting packet......");
        
        String key1 = responding_network_devices.key1;
        byte[] key1__chars = responding_network_devices.read_keys("1");
        byte[] key2__chars = responding_network_devices.read_keys("2");
        byte[] key3__chars = responding_network_devices.read_keys("3");

        /*
         * Holds byte values of encrypted data
         */
        byte[] data_chars = new byte[Encrypted_data.length() + 1];
        /*
         * Holds intermideate results in decryption process
         */
        byte[] decrypt_1 = new byte[Encrypted_data.length() + 1];
        byte[] decrypt_2 = new byte[Encrypted_data.length() + 1];
        /*
         * Stores final decrypted value
         */
        byte[] decrypt = new byte[Encrypted_data.length() + 1];

        /*
         * Create a copy of encrypted data for processing it
         */
        String y = Encrypted_data;

        /*
         * Get length of Encrypted String
         */
        int enc_data_len = y.length();

        /*
         * Grab bytes from Encrypted String
         */
        data_chars = y.getBytes();

        /*
         * Get length of key1
         */
        int key1_len = key1.length();

        /*
         * Decryption starts here
         */
        int k = 0, j = 0;
        while (k < enc_data_len) {

            if (j < key1_len) {
                decrypt_2[k] = (byte) (data_chars[k] ^ key3__chars[j]);
                decrypt_1[k] = (byte) (decrypt_2[k] ^ key2__chars[j]);
                decrypt[k] = (byte) (decrypt_1[k] ^ key1__chars[j]);
                k++;
                j++;
            } else {
                j = 0;
            }
        }

        y = new String(decrypt);     
        return y;
    }

    /**
     * Read packet and check if it is HR Device. This method checks decrypted
     * packet and port number to verify if this is packet from Hall Research
     * device. Read IP address, port packet and also separate packet_id, packet
     * password and encrypted data from packet. If packet ID matches and packet
     * password length is 32 return true else return false
     *
     * @return <b>true</b> if packet is from Hall Research Device.
     * <p><b>false</b>otherwise.
     */
    public boolean Verify_Packet() {
      
        
        String[] temp = null;
        /*
         * Read IP address, port and Data
         */
        client_ip = client_packet.getAddress().toString();
        client_ip = client_ip.substring(1);
        client_port = client_packet.getPort();

        DEBUG.log(Level.INFO,"process_packet::Reading packet.{0}",client_ip +":"+client_port);

        if (this.packet_type.regionMatches(0,"HR",0,2)) {
            /*
             * Read Received data
             */
            Received_data = new String(client_packet.getData(), 0, client_packet.getLength());
                      
            
            try {
                
                DEBUG.log(Level.INFO,"Packet data: {0}",Received_data); 
                
                temp = Received_data.split("\u001D", 3);
                Packet_ID = temp[0];
                Device_Password = temp[1];
                Encrypted_data = temp[2];
                
            } catch (Exception e) {
                DEBUG.log(Level.SEVERE,"{0}","process_packet::Exception in spliting data"+e.getMessage());
                return false;
            }

            if (Packet_ID.regionMatches(0, "2fae3af091b358426e15064175a896df", 0, 32)) {

                DEBUG.log(Level.INFO,"{0}","Packet ID Matches");
                if (Device_Password != null) {
                    return true;
                } else {
                    return false;
                }
            } else {                
                return false;
            }
        } else if (this.packet_type.regionMatches(0,"OTHER",0,5)) {
                        
            byte[] received_data = client_packet.getData();
            
            //verify if it's a Lantronix Packet.
            // response must start with hex 00 00 00 f7 .
            if(received_data[3]==(byte)0xf7){
                
                return true;
            } else {
                return false;
            }
            
            
        } else {
            // Dont know what packet it is
            return false;
        }

    }

    /**
     * parse data from decrypted message. This method parses information from
     * decrypted packet.
     *
     * @param string_to_parse decrypted string to parse.
     * @return String array with all parsed information.
     */
    public String[] parse_data(String string_to_parse) {
        
        DEBUG.log(Level.INFO,"{0}","process_packet:: parsing decrypted messages");
        
        String input = string_to_parse;
        String[] parsed_data = new String[10];

        parsed_data = input.split("\u001D", 7);
        DEBUG.log(Level.INFO,"{0}","Packet ID=" + parsed_data[0] + "\nIP_type=" + parsed_data[1] + "\nmac=" + parsed_data[2] + "\nModel=" + parsed_data[3] + "\nname=" + parsed_data[4] + "\nlocation=" + parsed_data[5] + "\nFirmware Version=" + parsed_data[6]);
        return parsed_data;
    }

    //@Override
    public void run() {
        String[] data = new String[10];
       
        DEBUG.log(Level.INFO,"{0}","process_packet...");
        
        if (Verify_Packet()) {
            
            if(this.packet_type.regionMatches(0,"HR",0,2)){
                  Decrypted_data = Decrypt_Data();
       
             DEBUG.log(Level.INFO,"{0}","Decrypted_data_string = " + Decrypted_data);
            /*
             * data is array of split strings with 
             * data[0]= HRD -- reply ID
             * data[1]= IP Type 
             * data[2]= MAC ID 
             * data[3]= Model name 
             * data[4]= Device Name 
             * data[5]= Device Location 
             * data[6]= Device_Information
             */
            data = parse_data(Decrypted_data);

            /*
             * check if packet starts with HRD.
             */
            if (data[0].regionMatches(0, "HRD", 0, 3)) {
               
                DEBUG.log(Level.INFO,"{0}","Decrypted data packet ID(HRD) match Found");
                /*
                 * Create new object with String array of [ip, mac, model_name,
                 * Device_Name, Device_Location, Device_info, Device Password]
                 * Add this Item to Arraylist. add_item method has synchronized
                 * access to Arraylist
                 */
                /* If Ip address is not listed in discovered devices lists then
                 * add record to table records lists.  
                 */
                if(!(DF.search_client_ip(client_packet.getAddress()))){
                    DF.add_item(new HR_Device(client_ip, data[1], data[2], data[3], data[4], data[5], data[6].trim(), Device_Password));
                    // Add ip to discovered devices list. 
                    DF.add_client_ip(client_packet.getAddress());
                }
            }
            
            } else if(this.packet_type.regionMatches(0,"OTHER",0,5)){
                
                /* read and parse lantronix packet sent from query port*/
                byte[] read_data = this.client_packet.getData();
                
                /* read_data[24] - read_data[29] is mac address */
                byte[] mac = new byte[]{(byte)read_data[24], (byte)read_data[25], (byte)read_data[26], (byte)read_data[27], (byte)read_data[28], (byte)read_data[29]};
                
                StringBuffer mac_str = new StringBuffer();
                
                for(int i=0 ; i<mac.length; i++){
                    String hex = Integer.toHexString(0xFF & mac[i]);
                    if(hex.length() == 1){
                        mac_str.append('0');
                    }
                    mac_str.append(hex);
                    if(i< mac.length-1)
                    mac_str.append(':');
                }
                
                String[] network_data = new String[7];
                network_data[1] = "Unknown";   //IP_Type
                network_data[2] = mac_str.toString(); //MAC ID
                network_data[3] = "Unknown";  // Model name
                network_data[4] = "Unknown";  // Device Name
                network_data[5] = "Unknown";  // Device location
                network_data[6] = "Unknown";  // Device Information
                String password = "password"; //password
                
                /* If Ip address is not listed in discovered devices lists then
                 * add record to probable decvices table records lists.
                 */ 
                if(!(DF.search_client_ip(client_packet.getAddress()))){
                    DF.add_probable_hr_device(new HR_Device(client_ip, network_data[1], network_data[2], network_data[3], network_data[4], network_data[5], network_data[6], password));
                    // Add ip to discovered devices list. 
                    DF.add_client_ip(client_packet.getAddress());
                }
            }
        }else {       
            DEBUG.log(Level.INFO,"Packet verification failed");
        }
    }
}
