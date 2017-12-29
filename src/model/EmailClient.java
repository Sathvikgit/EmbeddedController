
package model;

import static embeddedcontroller.EmbeddedController.DEBUG;
import java.util.logging.Level;
import org.apache.commons.mail.*;


public class EmailClient {
    

    private static final String HOST = "smtp.gmail.com";
    private static final int PORT = 465;
    private static final boolean SSL_FLAG = true; 
    
    private static final String userName = "halltesteng@gmail.com";
    private static final String password = "hall@123";
    
    private static final String fromAddress= "halltesteng@gmail.comm";
    
    // change this to support 
    private static final String toAddress =  "sathvik@hallresearch.com";
    
    private String subject = "Embedded Controller Support";
    private String message = "Hi this is a test mail...";
     
    private Email email;
    
    public EmailClient() {
      
            email = new SimpleEmail();
            email.setHostName(HOST);
            email.setSmtpPort(PORT);
            email.setAuthenticator(new DefaultAuthenticator(userName, password));
            email.setSSLOnConnect(SSL_FLAG);
        try {
            email.setFrom(fromAddress);
        } catch (EmailException ex) {
            DEBUG.log(Level.WARNING,"emailClient: {0}","failed to set FROM address"+ex.getMessage());
        }
        try {
            email.addTo(toAddress);
        } catch (EmailException ex) {
             DEBUG.log(Level.WARNING,"emailClient: {0}","failed to set TO address"+ex.getMessage());
        }
        
    }
    
    public void send(String subject, String message){  
        email.setSubject(subject);
        try {
            email.setMsg(message);
        } catch (EmailException ex) {
            DEBUG.log(Level.WARNING,"emailClient: {0}","failed to set message --"+ex.getMessage());
            return;
        }
        
        try {
            email.send();
        } catch (EmailException ex) {
            DEBUG.log(Level.WARNING,"emailClient: {0}", ex.getMessage());
            ex.printStackTrace();
        }
    
    }
}
