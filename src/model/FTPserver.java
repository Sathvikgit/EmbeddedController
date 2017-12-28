package model;

import static embeddedcontroller.EmbeddedController.DEBUG;
import embeddedcontroller.SystemStatus;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.apache.ftpserver.*;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.impl.FtpIoSession;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor;
import org.apache.ftpserver.usermanager.UserFactory;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.apache.log4j.PropertyConfigurator;


public class FTPserver {

    private String name = "myFTPServe";
    private int port = 21;
    
    // Factories created servers,listeners ....
    private FtpServerFactory serverFactory;
    private ListenerFactory listnerFactory;
    private PropertiesUserManagerFactory userManagerFactory;
    private UserFactory userFact;
    
    // products of factories
    private UserManager userManagement; 
    private FtpServer server; 
    private Listener listener;
    private User user;
    private List<Authority> authorities;
    
    private String USER_NAME= "";
    private String PASSWORD ="";
    private String HOME_DIR_PATH = "";
    
    private int MAX_LOGIN=10;
    private int MAX_LOGIN_PERIP=2;
    
    // Each server has this status updater running in the background
    public Thread StausUpdater;
    
    public FTPserver(String name, int port, String path, String username, String pass) {

        this.name = name;
        this.port = port;
        this.HOME_DIR_PATH =path;
        this.USER_NAME = username;
        this.PASSWORD = pass;
        
        // server properties are saved in this config file 
        PropertyConfigurator.configure("./src/model/log4j.properties"); 
        
        // Initilaize all the factories
        serverFactory= new FtpServerFactory();
        listnerFactory = new ListenerFactory();
        userManagerFactory= new PropertiesUserManagerFactory(); 
        userManagement= userManagerFactory.createUserManager();
        userFact= new UserFactory();
        
        
        // set the port of the listener
        listnerFactory.setPort(port);
        listener =listnerFactory.createListener();
        serverFactory.addListener("default",listener);
        
        // user properties are saved here 
        userManagerFactory.setFile(new File("./src/model/users.properties")); 
        userManagerFactory.setPasswordEncryptor(new SaltedPasswordEncryptor()); 
         
        userFact.setName(USER_NAME); 
        userFact.setPassword(PASSWORD); 
        userFact.setHomeDirectory(HOME_DIR_PATH);
        
        // set the permissions 
        authorities = new ArrayList<Authority>();
        authorities.add(new WritePermission());
        authorities.add(new ConcurrentLoginPermission(MAX_LOGIN, MAX_LOGIN_PERIP));
        
        userFact.setAuthorities(authorities);
        
        user = userFact.createUser();
        
        try { 
            userManagement.save(user);
        } catch (FtpException ex) {
           DEBUG.log(Level.SEVERE,getName()+": Failed to create user- {0}", ex.getMessage());
        }
        serverFactory.setUserManager(userManagement); 
        server = serverFactory.createServer();
        
        try {
            server.start();
        } catch (FtpException ex) {
            DEBUG.log(Level.SEVERE,getName()+": Fialed to start the server - {0}", ex.getMessage());
        }
        
        // start the status updater 
        StausUpdater = new Thread(new Runnable() {
            @Override
            public void run() {
              SystemStatus.update_FTPServerStatus();
            }
        });
        StausUpdater.start();
    }
    
    // Custome Authentication method
    public boolean StopServer(){
        server.stop();
        return true;
    }

    public String getName() {
        return name;
    }
    
    public Set<FtpIoSession> getActiveSessions(){
        if(server.isStopped()|| server.isSuspended()){
            return null;
        }else{
            return listener.getActiveSessions();
        }
    }
    
}
