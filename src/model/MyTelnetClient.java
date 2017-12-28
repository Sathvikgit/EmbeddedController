
package model;

import static embeddedcontroller.EmbeddedController.DEBUG;
import static embeddedcontroller.EmbeddedController.hs;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import org.apache.commons.net.telnet.EchoOptionHandler;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.SuppressGAOptionHandler;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TelnetInputListener;
import org.apache.commons.net.telnet.TelnetNotificationHandler;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;


public class MyTelnetClient implements Runnable, TelnetNotificationHandler, TelnetInputListener  {

    private String name ="myTelentClient";
    
    private TelnetClient tc =null;
    private String RemoteIp;
    private int port = 23;
    
    private OutputStream outputStream = null;
    private InputStream inputStream =null;
    
    // Timeo out 
    private int READ_TIMEOUT = 300000; //30sec
    
    // telent client options 
    TerminalTypeOptionHandler ttopt = null;
    EchoOptionHandler echoopt = null;
    SuppressGAOptionHandler gaopt = null;

    
    public MyTelnetClient(String RemoteIp, int port) {
        this.RemoteIp = RemoteIp;
        this.port = port;
        tc = new TelnetClient();
        
        // VT100 terminal type will be subnegotiated
        ttopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
        
        echoopt = new EchoOptionHandler(true, false, true, false);
        // WILL SUPPRESS-GA, DO SUPPRESS-GA options
        gaopt = new SuppressGAOptionHandler(true, true, true, true);
        
        try {
            tc.addOptionHandler(ttopt);
            tc.addOptionHandler(echoopt);
            tc.addOptionHandler(gaopt);
        } catch (InvalidTelnetOptionException ex) {
            DEBUG.log(Level.SEVERE,"myTelentClient: {0}",ex.getMessage());
        } catch (IOException ex) {
            DEBUG.log(Level.SEVERE,"myTelentClient: {0}",ex.getMessage());
        }
        
        try {
            // conenct to the server
            tc.connect(RemoteIp,port);
            tc.setSoTimeout(READ_TIMEOUT); 
            
            // Register event handlers
            tc.registerNotifHandler(this);
            tc.registerInputListener(this);
            
            // you need to enable this to get the read notifications
            tc.setReaderThread(true);
            
            // You can even attach a Spy strem to print all the transactions between client and server
            
            // This streams will be closed automatically when the connection is closed
            outputStream = tc.getOutputStream();
            inputStream = tc.getInputStream();
            
             
            //this.run();
        } catch (IOException ex) {
            DEBUG.log(Level.SEVERE,"myTelentClient: Connection Failed {0}",ex.getMessage());
            
            
            
        }
            
    }
    
    public boolean isConnected(){    
     return tc.isConnected();
    }
    
    // use this to send negotiatioin and subnegotiatioin commands 
    public void sendCmd(String cmd) {
        if (tc.isConnected()) {
            if (cmd.startsWith("AYT")) {
                try {
                    tc.sendAYT(5000);  // send Are you there? command 
                    DEBUG.log(Level.INFO, "myTelentClient: sendCommand = {0}", "AYT");
                } catch (IOException | IllegalArgumentException | InterruptedException ex) {
                    DEBUG.log(Level.SEVERE, "myTelentClient: sendCommand = {0}", ex.getMessage());
                }
            }else {
                byte[] neg_cmd = cmd.getBytes();
                try {
                    tc.sendCommand(neg_cmd[0]);
                } catch (IOException | IllegalArgumentException ex) {
                   DEBUG.log(Level.SEVERE, "myTelentClient: sendCommand = {0}", ex.getMessage());
                }         
            }
        }else{
            DEBUG.log(Level.INFO, "myTelentClient: Client Diconnected");
        }
    }
   
    public void send(String cmd) {
        if (tc.isConnected()) {
            try {
                outputStream.write(cmd.getBytes());
                
                DEBUG.log(Level.INFO, "myTelentClient: send = {0}", cmd);
                if(hs.telnetAddCR.isSelected()){
                    // add CR
                    outputStream.write(13);
                    
                }else if(hs.telnetAddLF.isSelected()){
                    // add LF
                    outputStream.write(10);
                }else if(hs.telnetAddCRLF.isSelected()){
                    //add CR+LF
                    outputStream.write(13);
                    outputStream.write(10);
                } 
                outputStream.flush();
            } catch (IOException ex) {
                DEBUG.log(Level.SEVERE, "myTelentClient: Send Failed = {0}", ex.getMessage());
            }
        }else{
            DEBUG.log(Level.INFO, "myTelentClient: Client Diconnected");
        }
    }
    
    public void stop(){
        try {
            tc.disconnect();
        } catch (IOException ex) {
            DEBUG.log(Level.SEVERE,"myTelentClient: {0}", ex.getMessage());
        }
    }


            
    /*
    *Callback method called when TelnetClient receives an option
    *negotiation has 3 parts (rep in 3 bytes)
    [IAC][COMMAND][OPTION]  // ref: https://support.biamp.com/Tesira/Control/Telnet_session_negotiation_in_Tesira
    
    * negotiation_code- type of negotiation command received
    *(RECEIVED_DO, RECEIVED_DONT, RECEIVED_WILL, RECEIVED_WONT, RECEIVED_COMMAND)
    
    * option_code - code of the option negotiated
    **/
    @Override
    public void receivedNegotiation(int CMD, int OPTION) {
      String command = null;
        switch (CMD) {
            case TelnetNotificationHandler.RECEIVED_DO:
                command = "DO";
                break;
            case TelnetNotificationHandler.RECEIVED_DONT:
                command = "DONT";
                break;
            case TelnetNotificationHandler.RECEIVED_WILL:
                command = "WILL";
                break;
            case TelnetNotificationHandler.RECEIVED_WONT:
                command = "WONT";
                break;
            case TelnetNotificationHandler.RECEIVED_COMMAND:
                command = "COMMAND";
                break;
            default:
                command = Integer.toString(CMD); // Should not happen
                break;
        }
        
        // Detials on more options here : https://www.iana.org/assignments/telnet-options/telnet-options.xhtml
        DEBUG.log(Level.INFO,"myTelentClient: {0}"," ReceivedNotification{ neg = "+command+",option ="+OPTION+"}");
        
        // Based on the CMD type and OPTION, you need to do something 
        
    }

   
    
    
    @Override
    public void run() {
       
        

    }

    
    @Override
    public void telnetInputAvailable() {
        // this methos is called by the reader thread for each byte you received from the server
        byte[] buff = new byte[1024];
        int ret_read = 0;
        try {
                ret_read = inputStream.read(buff);
                if (ret_read > 0) {
                   // DEBUG.log(Level.INFO,"TelentServer: {0}", new String(buff, 0, ret_read));
                    if(hs.telentClientTerminal != null){
                        hs.telentClientTerminal.append(new String(buff, 0, ret_read));
                    }
                }
            } catch (IOException ex) {
                    DEBUG.log(Level.WARNING,"myTelentClient: Failed to read from the input stream {0}",ex.getMessage());
            }
    }
}
