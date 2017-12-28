/*==========================================
  Author:       Sathvik Reddy Gaddam
  Application:  Embedded Controller  
 ===========================================*/
package embeddedcontroller;

import java.io.IOException;
import java.util.logging.*;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import model.EmailClient;
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
    
    
    // Applications 
    public static FTPserver myFTPServer;
    public static MyTelnetClient myTelnetClient;
    public static  EmailClient emailClient;
    public static HomeScreen hs;
    
    public static String DEF_LOOKANDFEEL ="Nimbus"; // this runs in all the platforms
  
    
    
    public static void main(String[] args) {
        // create Logger 
        createLogger();
        
        // Set the GUI look and Feel
        SetLookandFeel(DEF_LOOKANDFEEL);
        
        // Start HomeScreen
        java.awt.EventQueue.invokeLater(() -> {
            hs = new HomeScreen();
            // You have the set the form to visible
            hs.setVisible(true);
        });
        
        // start TCP server
        TCPServer echoServer = new TCPServer(5555, 15);
        
    }
    
    public static void SetLookandFeel(String LF){       
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (LF.equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    DEBUG.log(Level.INFO,"look and Feel set to : {0}", LF);
                    break;
                }
            }
        } catch (Exception e) {
            // If lF is not available, you can set the GUI to another look and feel.
            DEBUG.log(Level.INFO,"This L&F: {0}", LF+" is not available"+e.getMessage());
        }
    }
}
