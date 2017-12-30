package model;

import embeddedcontroller.EmbeddedController;
import static embeddedcontroller.EmbeddedController.DEBUG;
import static embeddedcontroller.EmbeddedController.hs;
import static embeddedcontroller.EmbeddedController.myTCPServer;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import systemDefinitions.logPacket;



public class TCPServer implements Runnable{

    private int serverPort;
    private int maxClient=10;
    private ServerSocket sock = null;
    public Socket client = null;
    
    public BufferedReader reader = null;
    public BufferedWriter writer = null;
    
    //log all the packets to this list 
    public List<logPacket> rxPackets = new ArrayList<>(); 
    public DefaultTableModel TCPserverModel = (DefaultTableModel) hs.TCPServerLog.getModel();
    
    public TCPServer(int port, int maxClient) {
        this.serverPort = port;
        this.maxClient = maxClient;
        DEBUG.log(Level.INFO, "Creating TCPserver: {0}", port);
        try {
             sock = new ServerSocket(port);
             rxPackets.clear();
             // start listening on new thread
             new Thread(this).start();
          
        } catch (IOException e) {
            DEBUG.log(Level.WARNING,"Failed to open server: {0}",port);
            
        }
    }

    @Override
    public void run() {
        while (true) {            
            DEBUG.log(Level.INFO, "listening...");
            try {
                
                this.client = sock.accept();
                DEBUG.log(Level.INFO, " {0} Connected", client.getInetAddress());
                
                reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                // we are not writing any thing
                writer =new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                
                // send welcome message
                //writer.write("*** Embedded TCP Server ***\r\n");
                //writer.write("*** Quit -> close connection *** \r\n");
                //writer.flush();
                String cmd = "";
                boolean closeClient = false;
                do{
                    if(client.isClosed() || !client.isConnected()){
                        closeClient = true;
                        DEBUG.log(Level.INFO, "Client socket Closed");
                    }else{
                        try{
                            cmd = reader.readLine().trim();
                            // log this packet
                            logPacket lp = new logPacket(new Date(),
                                    ((InetSocketAddress)client.getRemoteSocketAddress()).getAddress().toString().replace("/",""),
                                    client.getPort(),
                                    client.getLocalAddress().toString().replace("/",""),
                                    client.getLocalPort(),
                                    "", 
                                    cmd);
                            // log this packet 
                             tcpServerlogPacket(lp);
                             
                        }catch(Exception e){
                            closeClient = true;
                            DEBUG.log(Level.INFO, "Client Disconnected {0}", e.getMessage());
                            client.close();
                        }
                    }
                    
                }while(!closeClient);
                
            } catch (IOException e) {
                Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

 public void stop(){
     
 }   
 private static void tcpServerlogPacket(logPacket lp){
    myTCPServer.rxPackets.add(lp);
    // add this packet to the table 
    myTCPServer.TCPserverModel.addRow(lp.getRowData());
    System.out.println(lp.toString());
 }    

    
}
