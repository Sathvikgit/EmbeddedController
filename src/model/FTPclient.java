package model;

import static embeddedcontroller.EmbeddedController.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

public class FTPclient {

    // FTP client manager
    public static int FTPclient_Count = 0;
    
    
    private String name = "FTPClient";
    private String server = "www.myserver.com";
    private int port = 21;
    private String user = "user";
    private String pass = "pass";
    
    private FTPClient ftpClient = null;
    
    // local files
    private String LOCAL_FILE_PATH = "";
    private File LocalFile =null;
    private InputStream LocalFileStream = null;
    // remote files
    private String REMOTE_FILE_PATH = "";
    private OutputStream RemoteFileStream = null;

    
    public FTPclient(String server, int port, String user, String pass) {
    // if their is no name of this lcient, lets give it a name
        this("noNameFTPClient",server,port,user,pass);
    }
    public FTPclient(String name, String server, int port, String user, String pass) {
    
        this.name = name;
        this.server =server;
        this.port = port;
        this.user = this.user;
        this.pass = pass;
        
        // set Local and remote default path
        LOCAL_FILE_PATH = "C:/Users/gsath/Desktop/Ftptest.txt";
        REMOTE_FILE_PATH = "Ftptest.txt";
        
        // UI for this client. Each client has their own UI
        JPanel UI;
        // create a FTP client
        ftpClient = new FTPClient();
        try {
            // cnnecting to the server 
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            // passive mode: client connects to the open port in the server 
            // active mode : cilent opens a port and server try to connect (usually this is blocked by your firewall)
            ftpClient.enterLocalPassiveMode();
            // this file type works of the file types
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            
            DEBUG.log(Level.INFO,"FTPClient Creatted {0}",this.toString());
            FTPclient_Count++;
            DEBUG.log(Level.INFO,"Total number of FTP clients : {0}",FTPclient_Count);
            
        } catch (Exception e) {
            DEBUG.log(Level.WARNING,"Failed to create FTP client {0}",this.toString());
        }
    }
    
    // uploading Files{true: if sucess full}
    private Boolean uploadFile(String localFile, String remoteFile){
        boolean done = false;
        this.LOCAL_FILE_PATH = localFile;
        this.REMOTE_FILE_PATH = remoteFile;
        // open local file
        LocalFile = new File(LOCAL_FILE_PATH);
        try {
            // get the local file stream
            LocalFileStream = new FileInputStream(LocalFile);
        } catch (FileNotFoundException ex) {
            DEBUG.log(Level.SEVERE,getName()+"{0}", ex.getMessage());
            DEBUG.log(Level.SEVERE,getName()+": {0}", "Sending failed "+LOCAL_FILE_PATH+"->"+REMOTE_FILE_PATH);
            return false;
        }
        
        // Sending File
        try {
            DEBUG.log(Level.SEVERE,getName()+": {0}", "Sending file "+LOCAL_FILE_PATH+"->"+REMOTE_FILE_PATH);
            // Approach 1: Sends the file with limited control
            //done = ftpClient.storeFile(REMOTE_FILE_PATH, LocalFileStream);
            
            
            //Approach 2: Send with more control. we can show number bytes we are sending..
            // get the file stream
            RemoteFileStream =ftpClient.storeFileStream(REMOTE_FILE_PATH);
            byte[] bytesIn = new byte[4096];
            int read = 0;
 
            while ((read = LocalFileStream.read(bytesIn)) != -1) {
                RemoteFileStream.write(bytesIn, 0, read);
            }
            
            // confirmation
            done = ftpClient.completePendingCommand();

            
        } catch (IOException ex) {
            DEBUG.log(Level.SEVERE,getName()+"{0}", ex.getMessage());
        }finally{
           
            // free the resources 
            try {
                LocalFileStream.close();
                RemoteFileStream.close();
                DEBUG.log(Level.INFO,getName()+"{0}", "File Streams closed");
            } catch (IOException ex) {
                DEBUG.log(Level.SEVERE,getName()+"{0}", ex.getMessage());
            }
            
            // close the connection
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
              } catch (IOException ex) {
                 DEBUG.log(Level.SEVERE,getName()+"{0}", ex.getMessage());
              }
        }
            
        if(done){
            DEBUG.log(Level.INFO,getName()+": {0}", "File sent "+LOCAL_FILE_PATH+"->"+REMOTE_FILE_PATH);
            return true;
        }else{
            return false;
        }
    }
    
    
    public String getName(){
        return this.name;
    }
            
    @Override
    public String toString() {
        return "FTPClient: "+ this.name+"{" + "server=" + server + ", port=" + port + ", user=" + user + ", pass=" + pass + '}';
    }
}
