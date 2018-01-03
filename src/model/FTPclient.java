package model;

import static embeddedcontroller.EmbeddedController.*;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

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
    // lists files and directories in the current working directory
    public FTPFile[] remoteFiles;
    public String CWR = "/";  // current working directory
    // remote tree
    public JTree remoteTree = hs.ftpClient_remoteFileTree;
    
    

    private String LOCAL_FILE_PATH = "";
    private File LocalFile = null;
    private InputStream LocalFileStream = null;
    // remote files
    private String REMOTE_FILE_PATH = "";
    private OutputStream RemoteFileStream = null;

    public FTPclient(String server, int port, String user, String pass) {
        // if their is no name of this lcient, lets give it a name
        this("noNameFTPClient", server, port, user, pass);
    }

    public FTPclient(String name, String server, int port, String user, String pass) {

        this.name = name;
        this.server = server;
        this.port = port;
        this.user = user;
        this.pass = pass;

        // set Local and remote default path
        LOCAL_FILE_PATH = "C:/Users/gsath/Desktop/Ftptest.txt";
        REMOTE_FILE_PATH = "/";

        // UI for this client. Each client has their own UI
        JPanel UI;
        // create a FTP client
        ftpClient = new FTPClient();

        try {
            // Connecting to the server
            ftpClient.connect(server, port);
            if (ftpClient.isConnected()) {
                userLog(Level.INFO, " Server Connected");
                showServerReply();
            }
        } catch (Exception ex) {
            userLog(Level.SEVERE, "Failed to connect: " + ex.getMessage());
        }
        if (ftpClient.isConnected()) {
            try {
                if (ftpClient.login(user, pass)) {
                    userLog(Level.INFO, " Login Sucessesful");
                } else {
                    userLog(Level.INFO, " Login Failed");
                }
                showServerReply();

            } catch (Exception ex) {
                userLog(Level.WARNING, "Failed to Login: " + ex.getMessage());
            }
        }

        try {
            // passive mode: client connects to the open port in the server 
            // active mode : cilent opens a port and server try to connect (usually this is blocked by your firewall)
            ftpClient.enterLocalPassiveMode();
            // this file type works of the all file types
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            userLog(Level.INFO, "Tranfer file type set to binary");
            showServerReply();

            // list the files int the current  working directory
            showJtreeforRemote();
            // Wire the event listener in the remote tree 
            wireRemoteTreeEventListeners();

        } catch (IOException ex) {
            userLog(Level.WARNING, "Failed to set file type: " + ex.getMessage());
        }

        //FTPclient_Count++;
        //DEBUG.log(Level.INFO, "Total number of FTP clients : {0}", FTPclient_Count);
       
    }
    
    private void wireRemoteTreeEventListeners(){
    
            
            // Cell render for tree nodes
            remoteTree.setCellRenderer(new DefaultTreeCellRenderer() {
                // change the icon of dir to folder
                @Override
                public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                    if (value instanceof DefaultMutableTreeNode) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                        if (node.isRoot()) {
                            setIcon(UIManager.getIcon("FileView.hardDriveIcon"));
                        }else{
                            Object select = node.getUserObject();
                            if (select != null) {
                                if (select instanceof FTPFile) {
                                    FTPFile f = (FTPFile) select;
                                    if (f.isDirectory()) {
                                        setIcon(UIManager.getIcon("FileView.directoryIcon"));
                                    }
                                } 
                            }
                        }
                    }
                    // customize the ICONS
                    return this;
                }    
            });
            
            //Selection event handler
            remoteTree.addTreeSelectionListener(new TreeSelectionListener() {
                @Override
                public void valueChanged(TreeSelectionEvent e) {
                   
                    // get multiple selections
                    TreePath[] rows = remoteTree.getSelectionPaths();
                    if(rows == null){return;}
                    
                    if (rows.length > 0){
                        Object root = remoteTree.getModel().getRoot();
                        for (TreePath row :rows){
                        // these are the actual rows selected 
                            int index = remoteTree.getRowForPath(row);
                            userLog("Row "+index+" Selected");
                            
                            if(index-1 <0){
                                // there is a root in the selection
                                continue;
                            }
                            // get the node with respect to root
                            DefaultMutableTreeNode node = (DefaultMutableTreeNode)remoteTree.getModel().getChild(root,index-1);
                            Object rowObj = node.getUserObject();
                            if(rowObj!= null){
                                if (rowObj instanceof FTPFile) {
                                    FTPFile remoteFile = (FTPFile) rowObj;
                                    remoteFileSelected(remoteFile);
                                }      
                            }else if (node.isRoot()){
                                userLog("Root select");
                            }
                        }   
                    }
                }
            });
            // remote tree Mouse Double click listener
            remoteTree.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {

                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) remoteTree.getLastSelectedPathComponent();
                        if (node != null) {
                            Object sel = node.getUserObject();
                            if (sel instanceof FTPFile) {
                                FTPFile f = (FTPFile) sel;
                                if (f.isDirectory()) {
                                    userLog("Changing CWR to ../" + f.getName());
                                    if(goToDir(f.getName())){
                                        showJtreeforRemote();
                                    }
                                }
                            } else if (node.isRoot()) {
                                // see if this is a root node
                                userLog("Root Double click");
                                //go one level back
                                if(changeCWD("..")){
                                    showJtreeforRemote();
                                }
                            }
                        }
                    }
                }
            });
    }

    // shows the device tree for current working dir
    public void showJtreeforRemote() {

        // get the file information for the current dir
        if (listCurrentDir()) {

            DefaultTreeModel model = (DefaultTreeModel) remoteTree.getModel();
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
            // clear all the nodes 
            root.removeAllChildren(); //this removes all nodes
                      
            root.setUserObject(CWR);
            model.nodeChanged(root); // paint this node again
            
            model.reload();
            // populate all the files as nodes under root -- each node save the FTPfileobject
            for (FTPFile f : remoteFiles) {
                if (f.isDirectory()) {
                    // create a BRANCH NODE-- node that can have children
                    MutableTreeNode dirNode = new DefaultMutableTreeNode(f, true);
                    // set this icon to a folder 
                    root.add(dirNode);
                } else if (f.isFile()) {
                    // create a LEAF NODE -- that cannot have a children
                    root.add(new DefaultMutableTreeNode(f));
                } else if (f.isSymbolicLink()) {
                    // LEAF NODE
                    root.add(new DefaultMutableTreeNode(f));
                } else if (f.isUnknown()) {
                    // LEAF NODE
                    root.add(new DefaultMutableTreeNode(f));
                }
            }
            // always expand root
            remoteTree.expandRow(0);
            
            // Add event Single select event listener to the tree
            remoteTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        }
    }

    private static void remoteFileSelected(FTPFile f) {
        System.err.println("Selected File: " + f.toString());
        if (f.isDirectory()) {
            userLog("Remote Dir Selected:" + f.getName());
        } else {
            userLog("File Selected: " + f.getName());
        }
    }

    
    private boolean changeCWD(String path){
        try {
                // change the CWD
                boolean status;
                status = ftpClient.changeWorkingDirectory(path);
                showServerReply();
                return status;
        } catch (Exception ex) {
            userLog(Level.SEVERE,"Failed to open : "+path);
            showServerReply();
            return false;
        }
    }
    private boolean  goToDir(String dirName){
        // this directory must be in current working path
        if(getCWR()){
            if(changeCWD(CWR+"/"+dirName)){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }
    
    private boolean getCWR(){
        try {
            // try to get the current working dir first
            CWR = ftpClient.printWorkingDirectory();
            userLog(Level.INFO, "Working Dir :" + CWR);
            hs.ftpClient_remoteCWR.setText(CWR);
            showServerReply();
            return true;
        } catch (IOException ex) {
            userLog(Level.SEVERE, "Failed to get CWR" + ex.getMessage());
            CWR = null;
            showServerReply();
            return false;
        }
    }
    
    // list the files/directories in current directory
    private boolean listCurrentDir() {

        int dirs = 0;
        int files = 0;
        int sbl = 0;
        int ukn = 0;

        if(!getCWR()){
            return false;
        }

        try {
            // get all the files in the current dir 
            remoteFiles = ftpClient.listFiles();
            for (FTPFile f : remoteFiles) {
                if (f.isDirectory()) {
                    dirs++;
                    userLog(Level.INFO, "D" + dirs + " :" + f.getName() + ":" + f.getSize());
                } else if (f.isFile()) {
                    files++;
                    userLog(Level.INFO, " F" + files + " :" + f.getName() + ":" + f.getSize());
                } else if (f.isSymbolicLink()) {
                    sbl++;
                    userLog(Level.INFO, " S" + sbl + " :" + f.getName() + ":" + f.getSize());
                } else if (f.isUnknown()) {
                    ukn++;
                    userLog(Level.INFO, " U" + ukn + " :" + f.getName() + ":" + f.getSize());
                }
            }
            showServerReply();

            // construct Jtree for the current dir
            return true;
        } catch (IOException ex) {
            Logger.getLogger(FTPclient.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

    }

    // Call this method to see the server reponse after each action
    private void showServerReply() {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String rply : replies) {
                userLog("SERVER: " + rply);
            }
        }
    }

    // uploading Files{true: if sucess full}
    private Boolean uploadFile(String localFile, String remoteFile) {
        boolean done = false;
        this.LOCAL_FILE_PATH = localFile;
        this.REMOTE_FILE_PATH = remoteFile;
        // open local file
        LocalFile = new File(LOCAL_FILE_PATH);
        try {
            // get the local file stream
            LocalFileStream = new FileInputStream(LocalFile);
        } catch (FileNotFoundException ex) {
            DEBUG.log(Level.SEVERE, getName() + "{0}", ex.getMessage());
            DEBUG.log(Level.SEVERE, getName() + ": {0}", "Sending failed " + LOCAL_FILE_PATH + "->" + REMOTE_FILE_PATH);
            return false;
        }

        // Sending File
        try {
            DEBUG.log(Level.SEVERE, getName() + ": {0}", "Sending file " + LOCAL_FILE_PATH + "->" + REMOTE_FILE_PATH);
            // Approach 1: Sends the file with limited control
            //done = ftpClient.storeFile(REMOTE_FILE_PATH, LocalFileStream);

            //Approach 2: Send with more control. we can show number bytes we are sending..
            // get the file stream
            RemoteFileStream = ftpClient.storeFileStream(REMOTE_FILE_PATH);
            byte[] bytesIn = new byte[4096];
            int read = 0;

            while ((read = LocalFileStream.read(bytesIn)) != -1) {
                RemoteFileStream.write(bytesIn, 0, read);
            }

            // confirmation
            done = ftpClient.completePendingCommand();

        } catch (IOException ex) {
            DEBUG.log(Level.SEVERE, getName() + "{0}", ex.getMessage());
        } finally {

            // free the resources 
            try {
                LocalFileStream.close();
                RemoteFileStream.close();
                DEBUG.log(Level.INFO, getName() + "{0}", "File Streams closed");
            } catch (IOException ex) {
                DEBUG.log(Level.SEVERE, getName() + "{0}", ex.getMessage());
            }

            // close the connection
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                DEBUG.log(Level.SEVERE, getName() + "{0}", ex.getMessage());
            }
        }

        if (done) {
            DEBUG.log(Level.INFO, getName() + ": {0}", "File sent " + LOCAL_FILE_PATH + "->" + REMOTE_FILE_PATH);
            return true;
        } else {
            return false;
        }
    }

    // this log will the shown to the user 
    public static void userLog(String msg) {
        hs.FTPserverCMDLog.append(msg + "\r\n");
        DEBUG.log(Level.INFO, "FtpCleint Userlog: {0}", msg);
    }

    public static void userLog(Level level, String msg) {
        hs.FTPserverCMDLog.append(msg + "\r\n");
        DEBUG.log(level, "FtpCleint: {0}", msg);
    }

    public String getName() {
        return this.name;
    }

    public boolean isConnected() {
        return ftpClient.isConnected();
    }

    @Override
    public String toString() {
        return "FTPClient: " + this.name + "{" + "server=" + server + ", port=" + port + ", user=" + user + ", pass=" + pass + '}';
    }
}
