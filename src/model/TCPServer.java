package model;

import static embeddedcontroller.EmbeddedController.DEBUG;
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;



public class TCPServer implements Runnable{

    private int serverPort;
    private int maxClient=10;
    private ServerSocket sock = null;
    public Socket client = null;
    
    public BufferedReader reader = null;
    public BufferedWriter writer = null;
    
    public TCPServer(int port, int maxClient) {
        this.serverPort = port;
        this.maxClient = maxClient;
        DEBUG.log(Level.INFO, "Creating TCPserver: {0}", port);
        try {
             sock = new ServerSocket(port);
             // start listening on new thread
             new Thread(this).start();
          
        } catch (IOException e) {
            DEBUG.log(Level.WARNING,"Failed to open server: {0}",port);
            
        }
    }

    @Override
    public void run() {
        while (true) {            
            try {
                DEBUG.log(Level.INFO, "listening...");
                this.client = sock.accept();
                DEBUG.log(Level.INFO, " {0} Connected", client.getInetAddress());
                try {
                    reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    writer =new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                    
                    // send welcome message 
                    writer.write("*** Embedded TCP Server ***\r\n");            
		    writer.write("*** Quit -> close connection *** \r\n");
		    writer.flush();
		    String cmd = "";
                    boolean closeClient = false;
                    do{
                        if(client.isClosed() || !client.isConnected()){
                            closeClient = true;
                            DEBUG.log(Level.INFO, "Client socket Closed");
                        }else{
                            try{
                                cmd = reader.readLine().trim();
                                 if("Quit".equals(cmd)){
                                    writer.write("Closing Connection.. \r\n");
                                    writer.flush();
                                    client.close();
                                }else{
                                    // process the commands
                                    writer.write("Echo>"+cmd+"\r\n"); 
                                    writer.flush();
                                }
                            }catch(Exception e){
                                closeClient = true;
                                DEBUG.log(Level.INFO, "Connection Terminated by client");
                                client.close();
                            }
                        }
                        
                    }while(!closeClient);
                    
                } catch (IOException e) {
                    Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, e);
                }
            } catch (IOException ex) {
                Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    

    
}
