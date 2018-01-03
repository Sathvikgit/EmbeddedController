package systemDefinitions;

import embeddedcontroller.EmbeddedController;
import static embeddedcontroller.EmbeddedController.DEBUG;
import static embeddedcontroller.EmbeddedController.DF;
import java.io.IOException;
import java.util.Enumeration;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.lang.*;
import java.math.BigInteger;
import java.util.Timer;
import java.util.TimerTask;
import java.util.*;
import java.security.MessageDigest;
import java.util.logging.Level;
import javax.crypto.*;

/**
 * Listens for responses from Hall Research devices. This class listens for
 * responses from Hall Research devices. After sending scan request this thread
 * runs for 6 seconds to collect responses. <p> Upon receiving response it 1st
 * checks if response came on port number 6324. If its true then it checks if
 * response is coming from new device which is not yet processed. If both cases
 * are true then new thread is Spawned to process this response. <p> This thread
 * also creates keys for decrypting response.
*/

public class responding_network_devices implements Runnable {
    
    // Decalre Receiver Socket
    DatagramSocket Receiver_Socket = null;
    
    DatagramSocket Lantronix_Receiver_Socket = null;
    DatagramSocket Lantronix_Sender_Socket = null;
    // Flag to stop expecting responses after reaching 255 responses or on exception
    protected boolean more_requests = true;
    // counts number of requests received reqeust count is set to zero if 
    // scan button in GUI is pressed. 
    protected long requests_count = 0;
    // logs Ip addresses of devices responding to scan request on port 6324
    // list is cleared on each scan request
    protected ArrayList<InetAddress> listed_ips = null;
    
    //port to receive messages on
    static final int receiver_port = 6324;
    static final int lantronix_receiver_port = 48555;
   
    static final String key1 = "9eb232de1ded24659075";
    static byte[] key1_chars = new byte[key1.length()];
    static byte[] key2_chars = new byte[key1.length()];
    static byte[] key3_chars = new byte[key1.length()];
    String destination_ip_range = null;
    String sender_query_string = null;

    public responding_network_devices(String ipv4_address) {
      
        DEBUG.log(Level.INFO, "Responding to all HR devices on {0}",ipv4_address+":"+receiver_port);
        generate_keys();
        /*
         * Create sender query string
         */
        this.sender_query_string = this.create_sender_query();
        this.destination_ip_range = ipv4_address;
        
    }

    /**
     * Method generates keys for decryption.
     */
    private void generate_keys() {

        String format = "%$_+";
        int key1_len = key1.length();

        /*
         * Grab characters from key1 KEY
         */
        key1_chars = key1.getBytes();

        /*
         * Genarate key2 and key3
         */
        for (int i = 0; i < key1_len; i++) {

            if ((i % 2) == 0) {
                key2_chars[i] = (byte) format.charAt(0);
                key3_chars[i] = (byte) format.charAt(1);
            } else if ((i % 3) == 0) {
                key2_chars[i] = (byte) format.charAt(2);
                key3_chars[i] = (byte) format.charAt(3);
            } else {

                key2_chars[i] = key1_chars[i];
                key3_chars[i] = key2_chars[i];
            }
        }

    }

    /*
     * Method used to read Decryption keys
     */
    /**
     * Method returns keys for decryption.
     *
     * @param st Key number
     * @return byte array containing key.
     */
    public static byte[] read_keys(String st) {

        if (st.matches("1")) {
            return key1_chars;      //Return key1 chars
        } else if (st.matches("2")) {
            return key2_chars;   // Return Key2 chars
        } else {
            return key3_chars;   // Return Key3 chars
        }
    }

    //@Override
    public void run() {

        int Socket_timeout_count = 0;
        // create list of ips to collect ip addresses of responded devices
        listed_ips = new ArrayList<InetAddress>(256);
        int socket_create_exception_count = 0;
        // Holds data received from socket 
        byte[] receiver_buffer = new byte[3000];
        byte[] lantronix_receiver_buffer = new byte[512];

        do {
            // Create Socket listening on port 6324 to 
            // receive Data from Hall Research Devices and other matchports 
            try {
                Receiver_Socket = new DatagramSocket(receiver_port);
                DEBUG.log(Level.INFO,"Receiver_Socket: {0}",Receiver_Socket.getPort());
            } catch (SocketException e) {
                
                DEBUG.log(Level.SEVERE, "resonding_network_devices:: Cought exception in opening or Binding socket.{0}",e.getMessage());
                     
                socket_create_exception_count++;
                thread_timer t1 = new thread_timer(1);
                while (!(t1.istimeup())) {
                }
            } catch (SecurityException e) {
                
                DEBUG.log(Level.SEVERE,"responding_network_devices:: Security manager exists which blocks socket creation or bind operation {0}", e.getMessage());
                socket_create_exception_count++;
            }

        } while ((Receiver_Socket == null) && socket_create_exception_count < 10);

        // Receive requests packet
        DatagramPacket receiver_packet = new DatagramPacket(receiver_buffer, receiver_buffer.length);

        /*
         * Send scan request packets
         */
        this.send_packet(Receiver_Socket, this.destination_ip_range);

        if (this.listed_ips != null) {
            this.listed_ips.clear();
        }
        // Clear discovered ip list
        DF.clear_ip_list();
        

        while (true) {
            try {

                Receiver_Socket.setSoTimeout(2000);
                Receiver_Socket.receive(receiver_packet);

            } catch (java.net.SocketTimeoutException ex) {
                Socket_timeout_count++;
                DEBUG.log(Level.INFO,"SocketTimeoutCount = {0}", Socket_timeout_count);
                
                // listen for requests till 6 seconds and after that 
                // Clear listed_ ips and Close socket and break out of while loop
                if (Socket_timeout_count >= 3) {
                    
                     DEBUG.log(Level.WARNING,"Cought SocketTimeOut in receiving packet");
                                     
                    if (Receiver_Socket != null) {
                        Receiver_Socket.close();
                    }
                    break;
                } else {
                    DEBUG.log(Level.INFO,"sending scan request again");
                    this.send_packet(Receiver_Socket, this.destination_ip_range);

                }
            } catch (java.net.PortUnreachableException po) {
                
                DEBUG.log(Level.WARNING,"Cought Port unreachable in receiving packet");
                
                if (Receiver_Socket != null) {
                    Receiver_Socket.close();
                }

                break;
            } catch (java.nio.channels.IllegalBlockingModeException bo) {
                
                DEBUG.log(Level.WARNING,"Cought IllegalBlockingModeException in receiving packet");
                
                if (Receiver_Socket != null) {
                    Receiver_Socket.close();
                }
                break;

            } catch (IOException io) {
                
                DEBUG.log(Level.WARNING,"Cought IO in receiving packet");
                
                if (Receiver_Socket != null) {
                    Receiver_Socket.close();
                }
                break;
            } catch (java.lang.NullPointerException np) {
                
                DEBUG.log(Level.WARNING,"Cought Null POinter Exception in receiving packet");
                
                if (Receiver_Socket != null) {
                    Receiver_Socket.close();
                }
                break;
            }

            /*
             * If data is received on port 6324 and length of Data is greater
             * than 0 then create new thread to process this data to grab
             * information only if response from this device has not already
             * been processed.
             */

            if (((receiver_packet.getLength() > 0) && (receiver_packet.getPort() == receiver_port))) {
               
                DEBUG.log(Level.INFO,"new packet received from: {0}", receiver_packet.getAddress().toString());
                // If Ip address is not found in list then add IP to list and process packet 
              
                if (!(DF.search_client_ip(receiver_packet.getAddress()))) {
                
                    Thread T = new Thread(new process_packet(receiver_packet,"HR"));
                    T.start();
                    
                }else{
                    DEBUG.log(Level.INFO,"Duplicate");
                }
                receiver_buffer = new byte[3000];
                receiver_packet = new DatagramPacket(receiver_buffer, receiver_buffer.length);
            }
        }

        socket_create_exception_count = 0;
        do {
            // Create Socket listening on port 48555 to 
            // receive Data from other matchports 


            try {               
                Lantronix_Receiver_Socket = new DatagramSocket(48555);

            } catch (SocketException e) {
               
                DEBUG.log(Level.WARNING,"resonding_network_devices:: Cought exception in opening or Binding Lantronix socket.");
                socket_create_exception_count++;
                thread_timer t1 = new thread_timer(1);
                while (!(t1.istimeup())) {
                }
            } catch (SecurityException e) {
                
                DEBUG.log(Level.WARNING,"responding_network_devices:: Security manager exists which blocks Lantronix socket creation or bind operation");
                socket_create_exception_count++;
            }
        } while ((Lantronix_Receiver_Socket == null) && socket_create_exception_count < 10);

        // Receive requests packet
       
        DatagramPacket lantronix_receiver_packet = new DatagramPacket(lantronix_receiver_buffer, lantronix_receiver_buffer.length);
        
        /*
         * Send lantronix scan request packets
         */       
        this.send_lantronix_packet(Lantronix_Receiver_Socket, this.destination_ip_range);

        while (true) {
            try {

                Lantronix_Receiver_Socket.setSoTimeout(2000);
                Lantronix_Receiver_Socket.receive(lantronix_receiver_packet);

            } catch (java.net.SocketTimeoutException x) {
                Socket_timeout_count++;

                DEBUG.log(Level.INFO,"SocketTimeoutCount= {0}",Socket_timeout_count );
                // listen for requests till 6 seconds and after that 
                // Clear listed_ ips and Close socket and break out of while loop
                if (Socket_timeout_count >2) {
                   
                    DEBUG.log(Level.INFO,"{0}","Cought SocketTimeOut in receiving packet");
                    
                    if (listed_ips != null) {
                        listed_ips.clear();
                    }
                     // Clear discovered ip list
                    DF.clear_ip_list();
                    if (Lantronix_Receiver_Socket != null) {
                        Lantronix_Receiver_Socket.close();
                    }
                    break;
                } else {                   
                    DEBUG.log(Level.INFO,"{0}","sending scan request again");
                    this.send_lantronix_packet(Lantronix_Receiver_Socket, this.destination_ip_range);
                }
            } catch (java.net.PortUnreachableException po) {
               
                DEBUG.log(Level.WARNING,"{0}","Cought Port unreachable in receiving packet");
                if (listed_ips != null) {
                    listed_ips.clear();
                }
               
                 // Clear discovered ip list
                 DF.clear_ip_list();
                 
                if (Lantronix_Receiver_Socket != null) {
                    Lantronix_Receiver_Socket.close();
                }
                break;
            } catch (java.nio.channels.IllegalBlockingModeException bo) {
                
                DEBUG.log(Level.WARNING,"{0}","Cought IllegalBlockingModeException in receiving packet");
                if (listed_ips != null) {
                    listed_ips.clear();
                }
                
                 // Clear discovered ip list
                    DF.clear_ip_list();
                if (Lantronix_Receiver_Socket != null) {
                    Lantronix_Receiver_Socket.close();
                }

                break;
            } catch (IOException io) {
                
                DEBUG.log(Level.WARNING,"{0}","Cought IO in receiving packet");
                if (listed_ips != null) {
                    listed_ips.clear();
                }
                 // Clear discovered ip list
                    DF.clear_ip_list();
              
                if (Lantronix_Receiver_Socket != null) {
                    Lantronix_Receiver_Socket.close();
                }
                break;
            } catch (java.lang.NullPointerException np) {
                
                DEBUG.log(Level.WARNING,"{0}","Cought Null POinter Exception in receiving packet");
                if (listed_ips != null) {
                    listed_ips.clear();
                }
                 // Clear discovered ip list
                    DF.clear_ip_list();
                if (Lantronix_Receiver_Socket != null) {
                    Lantronix_Receiver_Socket.close();
                }
                break;
            }

            /*
             * If data is received on port 6324 and length of Data is greater
             * than 0 then create new thread to process this data to grab
             * information only if response from this device has not already
             * been processed.
             */

            if (((lantronix_receiver_packet.getLength() > 0) && (lantronix_receiver_packet.getPort() == 30718))) {
               
                DEBUG.log(Level.INFO,"{0}","responding_network_devices::new packet received from " + lantronix_receiver_packet.getAddress().toString()+"::"+lantronix_receiver_packet.getPort() );
                //
                // If Ip address is not found in list then add IP to list and process packet 
                // If IP Address is preset just wait till next request
                //if (!(listed_ips.contains(lantronix_receiver_packet.getAddress()))) {
                //    listed_ips.add(lantronix_receiver_packet.getAddress());
                if (!(DF.search_client_ip(lantronix_receiver_packet.getAddress()))) {    
                    Thread T = new Thread(new process_packet(lantronix_receiver_packet, "OTHER"));
                    T.start();
                }
                
                lantronix_receiver_buffer = new byte[512];
                lantronix_receiver_packet = new DatagramPacket(lantronix_receiver_buffer, lantronix_receiver_buffer.length);

            }
        }
    }

    /**
     * Method creates scan request query string.
     *
     * @return String Scan request query string.
     */
    public String create_sender_query() {
        String query_string = null;
        Cipher cipher = null;
        String default_mac = "ff:ff:ff:ff:ff:ff";
        String md5hash_to_send = null;
        /*
         * MD5 of authentication message is sent to all network devices. All
         * Devices on Network which are listening on specified port will respond
         * but only HR Device will have this MD5. HR Device will compare it with
         * one it has and send reply to Device Finder.
         */
        byte[] authentication_msg = "RUHRDevice?".getBytes();
        byte[] md5hash = null;  /*
         * Holds MD5 of RUHRDEVICE? message
         */
        try {
            /*
             * Calculate MD5
             */
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(authentication_msg);
            md5hash = md.digest(); /*
             * Holds MD5 hash in byte format
             */
        } catch (Exception e) {
            DEBUG.log(Level.WARNING,"devices_to_request:: MD5 Algorithm does not exists");
            md5hash = null;
        }
        if (md5hash != null) {
            /*
             * Convert byte MD5 to hexstring
             */
            StringBuffer authentication_string = new StringBuffer();
            for (int i = 0; i < md5hash.length; i++) {
                String hex = Integer.toHexString(0xff & md5hash[i]);
                if (hex.length() == 1) {
                    authentication_string.append('0');
                }
                authentication_string.append(hex);
                md5hash_to_send = authentication_string.toString();
            }
            /*
             * create sender query string
             */
            query_string = default_mac + "\u00A7" + "RUHR?" + "\u00A7" + md5hash_to_send + "\u00A7" + "\u00A7";

            DEBUG.log(Level.INFO,"devices_to_request:: scan request_string: {0}", query_string);
        }
        return query_string;
    }

    /*
     * Sends packets to the HR device.
     */
    public void send_packet(DatagramSocket Sender, String ip_address_range) {

        InetAddress destination_ipv4_address = null;
        DatagramPacket packet = null;

        String ip_address = null;
        if (this.sender_query_string != null) {
            for (int i = 0; i <= 2; i++) {
                
                // Look for Devices in all the possible networks
                
                if (i == 0) {
                    // Broadcast IP to request to all Subnets
                    ip_address = "255.255.255.255";
                } else if (i == 1) {
                    // Broadcast IP to request to all subnets in class C network
                    ip_address = "192.168.255.255";
                } else if (i == 2) {
                    // Broadcast IP to request to all devices on current subnet 
                    ip_address = ip_address_range + 255;
                }

                /*
                 * create destination IPv4 address
                 */
                try {
                    
                    destination_ipv4_address = InetAddress.getByName(ip_address);                  
                    DEBUG.log(Level.INFO,"{0}","responding_network_devices::send_packet:: Sending scan requests string to " + destination_ipv4_address.toString());
                
                } catch (UnknownHostException x) {
                    
                    DEBUG.log(Level.WARNING,"UnknownHostException in creating destination_ipv4_address");
                }

                /*
                 * Create Datagrampacket to send
                 */
                try {
                    packet = new DatagramPacket(this.sender_query_string.getBytes("UTF8"), this.sender_query_string.length(), destination_ipv4_address, receiver_port);
                } catch (UnsupportedEncodingException e) {                   
                    DEBUG.log(Level.WARNING,"{0}","responding_network_devices::send_packet:: UnsupportedEncoding UTF8");
                }

                /*
                 * send packet
                 */
                try {
                    if (Sender != null) {
                        Sender.send(packet);

                    }
                } catch (PortUnreachableException e) {
                   
                    DEBUG.log(Level.WARNING,"{0}","responding_network_devices::send_packet:: portunreachable exception");
                } catch (SecurityException x) {
                    
                    DEBUG.log(Level.WARNING,"{0}","responding_network_devices::send_packet:: Socket Security Exception");
                } catch (java.io.IOException z) {
                   
                    DEBUG.log(Level.WARNING,"{0}","responding_network_devices::send_packet:: Socket.send IO exception");
                } catch (java.nio.channels.IllegalBlockingModeException e) {
                    
                    DEBUG.log(Level.WARNING,"{0}","responding_network_devices::send_packet:: IllegalBlockingMode invoked on channel");
                }
            }
        }       
    }

    /*
     * Sends packets to all matchports in the local network
     */
    public void send_lantronix_packet(DatagramSocket Lantronix_Socket, String ip_address_range) {

        InetAddress destination_ipv4_address = null;
        DatagramPacket packet = null;
        
        byte[] buffer = new byte[5];
        String ip_address = null;
        int lantronix_server_port = 30718;
        
        if (Lantronix_Socket != null) {
            for (int i = 0; i <= 2; i++) {
                if (i == 0) {
                    // Broadcast IP to request to all Subnets
                    ip_address = "255.255.255.255";
                } else if (i == 1) {
                    // Broadcast IP to request to all subnets in class C network
                    ip_address = "192.168.255.255";
                } else if (i == 2) {
                    // Broadcast IP to request to all devices on current subnet 
                    ip_address = ip_address_range + 255;
                }

                /*
                 * create destination IPv4 address
                 */
                try {                   
                    
                    destination_ipv4_address = InetAddress.getByName(ip_address);                    
                    DEBUG.log(Level.INFO,"{0}","responding_network_devices::send_lantronix_packet:: Sending scan requests string to " + destination_ipv4_address.toString());
                } catch (UnknownHostException x) {   
                    DEBUG.log(Level.WARNING,"{0}","responding_network_devices::send_lantronix_packet:: Cought UnknownHostException in creating destination_ipv4_address");
                }

               
                /* lantronix query string to send
                 * buffer = [0x00,0x00,0x00,0xf6]
                 */
                buffer[0] = buffer[1]=buffer[2] = (byte) 0x00;                
                buffer[3] = (byte) 0xf6;
                
                /* Create datagram packet*/
                packet = new DatagramPacket(buffer, buffer.length, destination_ipv4_address, lantronix_server_port);
         
                /*
                 * send packet
                 */
                try {
                    if (Lantronix_Socket != null) {
                        Lantronix_Socket.send(packet);

                    }
                } catch (PortUnreachableException e) {
                    
                    DEBUG.log(Level.WARNING,"{0}","responding_network_devices::send_lantronix_packet:: portunreachable exception");
                } catch (SecurityException x) {
                    
                    DEBUG.log(Level.WARNING,"{0}","responding_network_devices::send_lantronix_packet:: Socket Security Exception");
                } catch (java.io.IOException z) {
                    
                    DEBUG.log(Level.WARNING,"{0}","responding_network_devices::send_lantronix_packet:: Socket.send IO exception ");
                } catch (java.nio.channels.IllegalBlockingModeException e) {
                    
                    DEBUG.log(Level.WARNING,"{0}","responding_network_devices::send_lantronix_packet:: IllegalBlockingMode invoked on channel");
                }
            }
        } else {
            
            DEBUG.log(Level.WARNING,"{0}","esponding_network_devices::send_lantronix_packet:: Lantronix_Socket is null");
        }
        
    }
}
