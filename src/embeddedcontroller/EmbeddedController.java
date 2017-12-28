/*==========================================
  Author:       Sathvik Reddy Gaddam
  Application:  Embedded Controller  
 ===========================================*/
package embeddedcontroller;

import java.io.IOException;
import java.util.logging.*;
import model.FTPserver;
import model.MyTelnetClient;
import model.TCPServer;

public class EmbeddedController {

    // All the classes must use this to log the messages 
    public static final Logger DEBUG = Logger.getLogger(EmbeddedController.class.getName());
    private static Handler log_Filehandler;
    private static void createLogger(){
        try {
            // All the debug messages will be logged to this file
            log_Filehandler = new FileHandler("./EmbeddedController.log", true); 
            // WARNING: Application is responsible for closing and deleting this file
            DEBUG.addHandler(log_Filehandler);
            DEBUG.log(Level.INFO,"Log File created");
        }catch (IOException | SecurityException ex) {
            DEBUG.log(Level.SEVERE,ex.getMessage());
        }
    }
    
    public static void closeApplication(){
        DEBUG.log(Level.INFO,"Closing Applcation");
    }
    /**
     * @param args the command line arguments
     */
    
    
    public static FTPserver myFTPServer;
    public static MyTelnetClient myTelnetClient;
    public static HomeScreen hs;
    
    public static void main(String[] args) {
        // create Logger 
        createLogger();
        // Start HomeScreen
        java.awt.EventQueue.invokeLater(() -> {
            hs = new HomeScreen();
            // You have the set the form to visible
            hs.setVisible(true);
        });
        
        // start TCP server
        TCPServer echoServer = new TCPServer(5555, 15);
        
    }
    
}
