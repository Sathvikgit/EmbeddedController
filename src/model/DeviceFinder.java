package model;

import static embeddedcontroller.EmbeddedController.DEBUG;
import static embeddedcontroller.EmbeddedController.DF;
import static embeddedcontroller.EmbeddedController.hs;
import static embeddedcontroller.SystemStatus._ipv4_address;
import static embeddedcontroller.Utilities.get_system_ip;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import systemDefinitions.HR_Device;
import systemDefinitions.responding_network_devices;
import systemDefinitions.thread_timer;

public class DeviceFinder {
 
    // List of HR Devices found in the network
    private static ArrayList<HR_Device> hr_devices_record = null;
    private static ArrayList<HR_Device> probable_hr_devices_record = null;
    private static ArrayList<InetAddress> discovered_hr_devices_ips = null;
    
    
    //Threads
    private static Thread search_network_thread = null;
    private static Thread res_dev = null;
    public static responding_network_devices responded_devices_list = null;
    
    public DeviceFinder() {
        hr_devices_record = new ArrayList<HR_Device>(256);
        probable_hr_devices_record = new ArrayList<HR_Device>(256);
        discovered_hr_devices_ips = new ArrayList<InetAddress>(256);
        
        this.init_scan();
        this.run_background_network_scan();
        
    }
    
    public void init_scan(){
    // getting ready to scan
        DEBUG.log(Level.INFO," Initializing Scan ");
    
    }
    
    
    /**
     * Scans network for Hall Research devices. Method finds available network
     * interfaces on machine and if any available it creates threads to send
     * request, collect responses. It also initializes data structure to save
     * responses. It creates global objects from all other classes in projects.
     * If network interface is not found method shows a dialog giving user
     * option to retry looking for network interface.
     */
    public void run_background_network_scan() {
    
        DEBUG.log(Level.INFO,"Scanning Devices...");
         if (get_system_ip()) {
             
            // Start reponding to the HR devices on this Ip Address (UDP lsitener)
             responded_devices_list = new responding_network_devices(_ipv4_address);
             res_dev = new Thread(responded_devices_list);

             // Start searching network
             search_network_thread = new Thread(new Runnable() {
                 @Override
                 public void run() {
                     DEBUG.log(Level.INFO,"Scanning Network");
                     search_network();
                 }
             });
             search_network_thread.start();
             

         }else{
             
             DEBUG.log(Level.INFO,"System is not connected to the network");
            // No networkInterface found or IP is loopback address. 
            // Display a Dialog to tell user about Network Failure allow user to Retry..
            String msg_to_display = "System is not connected to network.\n"
                                    + "Click Yes to try again or No to close this window.\nRetry?";
            
            int user_input = JOptionPane.showConfirmDialog(hs, msg_to_display, 
                                "Network error!", 
                                JOptionPane.YES_NO_OPTION, 
                                JOptionPane.ERROR_MESSAGE);

            // User asked for retry
            if (user_input == JOptionPane.YES_OPTION) {           
                init_scan();
                run_background_network_scan();
            } else if (user_input == JOptionPane.NO_OPTION) {
                //network_failure_dialog.dispose();
            }
         }
    }
    
    private static void search_network() {

        // start the listener
        res_dev.start();
        /*
             * Keep checking if there is any device entry in Datastructure, for
             * 6 seconds (6000ms). If there is no item in list SwingWorker
             * thread sleeps for 100 miliseconds and reads list again. After
             * sleeping 60 times (100*60 = 6000ms) it calls done method
         */
        thread_timer t1 = new thread_timer(10);
        int count = 0;
        while (!(t1.istimeup())) {
            
            // Check if you found any HR devices
            if ( DF.record_size() > 0) {
                /* Get latest record from list */
                HR_Device new_item = DF.get_item();
                if(new_item != null){
                    DEBUG.log(Level.INFO,"HR Device: {0}",new_item.toString());
                    // show this device to the user
                                        
                }
            }else{
               // DEBUG.log(Level.INFO,"No HR devices");
            }
            
             // check if there are any Devices in probable HR Device record. 
            if (DF.probable_hr_devices_record_size() > 0) {
                /* Get the last entry of probable HR Device from list*/
                 HR_Device new_item = DF.get_probable_hr_device();
                 if(new_item != null){
                    DEBUG.log(Level.INFO,"Prob HR Device: {0}",new_item.toString());
                    // show this device to the user
                                        
                }           
            }else{
               // DEBUG.log(Level.INFO,"No Other devices");
            }
            
            
            
        }
    }
    
     /**
     * Add new item to list (Synchronized). <p> Method is used to add
     * information about found Hall Research device in List.
     *
     * @param x HR_Devices class instance which holds information about found
     * device if list has any element or null if list is empty.
     */
    public synchronized void add_item(HR_Device x) {

        // If list is full make thread to wait until it gets some Empty slot  
        while (hr_devices_record.size() >= 256) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        hr_devices_record.add(x);
        // Let other thead access List 
        notify();
    }

    /**
     * Get last item from list (Synchronized). <p> This method returns last
     * element from the list. Returned element is displayed in table.
     *
     * @return last item in list which is information about Hall Research Device
     * to must be displayed in table.
     */
    public synchronized HR_Device get_item() {

        // if list is Empty make Thread to wait until new element is added to list 
        while (hr_devices_record.size() <= 0) {
            try {
                /*
                 * SwingWorker search_network is going to read this Reacord It
                 * waits for 100 miliseconds looking for new entry in record.
                 */
                wait(100);

            } catch (InterruptedException e) {
            }
        }

        // Let other thread access list
        notify();
        // Returns last element in the ArrayList
        if (hr_devices_record.size() > 0) {
            return hr_devices_record.remove((hr_devices_record.size() - 1));
        } else {
            return null;
        }
    }

    /**
     * Get current size of List (Synchronized). <p> This method returns current
     * size of list containing HR Devices. It returns 0 if list is empty.
     *
     * @return int - size of ArrayList which tells number of items in the list.
     */
    public synchronized int record_size() {
        return hr_devices_record.size();
    }

    /**
     * Add new probable HR Deivice item to list (Synchronized). <p> Method is
     * used to add information about found probable Hall Research device in
     * List.
     *
     * @param x HR_Devices class instance which holds information about found
     * device if list has any element or null if list is empty.
     */
    public synchronized void add_probable_hr_device(HR_Device x) {

        // If list is full make thread to wait until it gets some Empty slot  
        while (probable_hr_devices_record.size() >= 256) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        probable_hr_devices_record.add(x);
        // Let other thead access List 
        notify();
    }

    /**
     * Get last item from probable HR Devices Record list (Synchronized). 
     * <p> This method returns last
     * element from the list. Returned element is displayed in table.
     *
     * @return last item in list which is information about Hall Research Device
     * to must be displayed in table.
     */
    public synchronized HR_Device get_probable_hr_device() {

        // if list is Empty make Thread to wait until new element is added to list 
        while (probable_hr_devices_record.size() <= 0) {
            try {
                /*
                 * SwingWorker search_network is going to read this Reacord It
                 * waits for 100 miliseconds looking for new entry in record.
                 */
                wait(100);

            } catch (InterruptedException e) {
            }
        }

        // Let other thread access list
        notify();
        // Returns last element in the ArrayList
        if (probable_hr_devices_record.size() > 0) {
            return probable_hr_devices_record.remove((probable_hr_devices_record.size() - 1));
        } else {
            return null;
        }
    }
    
    /**
     * Get current size of List (Synchronized). <p> This method returns current
     * size of list containing HR Devices. It returns 0 if list is empty.
     *
     * @return int - size of ArrayList which tells number of items in the list.
     */
    public synchronized int probable_hr_devices_record_size() {
        return probable_hr_devices_record.size();
    }
    
    
    /**
     * Clean the list containing Ip addresses of
     * discovered HR devices. This list must be cleared 
     * on each search. 
     * @return  <b>true</b> if list is empty.
     * <p><b>false</b>if list is not empty. 
     */ 
    public synchronized boolean clear_ip_list(){
        // Clear list containg IP addresses. 
        discovered_hr_devices_ips.clear();
        // Check if empty
        if(discovered_hr_devices_ips.isEmpty()){
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Search for IP Address in list.
     * <p> Search for IP address in the 
     * discovered hr devices ips list. 
     * 
     * @param client_ip InetAddress object (Ip Address) to search for. 
     * @return  <b>true</b> if list contains IP address.
     * <p><b>false</b> Otherwise.
     */
    public synchronized boolean search_client_ip(InetAddress client_ip){        
        // Serch if IP address is listed in the discovered devices list. 
        if(discovered_hr_devices_ips.size() > 0) {
            if(discovered_hr_devices_ips.contains(client_ip)){
                return true;
            } else {
                return false;
            }
        } else {
         return false;   
        }
    }
    
    /**
     * Add IP address to the discovered hr devices list. 
     * <p> This method adds given ip address 
     * to the discovered HR devices list. Ip is only added if
     * list doesn't contains it already. 
     * @param client_ip InetAddress object IP address to add. 
     * @return  <b>true</b> if Added successfully.
     * <p><b>false</b> if list already contains IP.
     */
    public synchronized boolean add_client_ip(InetAddress client_ip){
        
        while (discovered_hr_devices_ips.size() >= 256) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        
        if(discovered_hr_devices_ips.contains(client_ip)){
            // Let other thead access List 
            DEBUG.log(Level.INFO,"List already contains {0}", client_ip.toString());
          
            notify();
            return false;
        } else {       
            DEBUG.log(Level.INFO,"Adding{0}", client_ip.toString() +"to Discovered devices list");
            discovered_hr_devices_ips.add(client_ip);
            // Let other thead access List 
            notify();
            return true;          
        }
    }
      
}
