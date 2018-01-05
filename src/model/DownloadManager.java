package model;

import static embeddedcontroller.EmbeddedController.DEBUG;
import static embeddedcontroller.EmbeddedController.hs;
import java.awt.Component;
import java.net.URI;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import utilities.DownloadTask;

public class DownloadManager {

    // you always need to check this flag before working with Download Manager
    public static boolean inUse = false;

    private static JLabel download_status = hs.dowloadingFileName;
    private static JPanel download_files = hs.downloadFileListContainer;
    private static JTextField download_save_path = hs.downloadFilePath;
    private static JProgressBar download_progress = hs.downloadFileProgress;
    private static JDialog download_dialog = hs.downloadFIleDialog;

    private static int fileToDownloadCount = 0;
    private static int currentFileDNIndx = 0;
    public static boolean downloadInProgress = false;
    public static boolean selectingDownloadPath = false;

    private static ArrayList<String> download_urls;
    private static ArrayList<String> downloading_urls = new ArrayList<>();
    private static String DownloadPath=".";
    private static DownloadTask[] downloadTasks;

    public DownloadManager(ArrayList<String> files) {
        download_urls = files;
        selectingDownloadPath = false;
        
        if (download_urls.size() > 0) {
            // open the dialog
            download_dialog.setVisible(true);
            download_progress.setValue(0);
            download_status.setText("");
             // show the list of files to download
            showFilestoDownload();
            inUse = true;
            System.out.println("Download Manager inUSE");
        }
    }

    public static void showDialog(){
        download_dialog.setVisible(true);
    }
    public static void setDownloadPath(String path){
        DownloadPath = path;
        DEBUG.log(Level.INFO, "Download Path Set: {0}", path);
           
    }
    
    public static void showFilestoDownload(){
    
        // clear the list of files first
        download_files.removeAll();
       
        for(String url: download_urls){
            download_files.add(new JCheckBox(url,true));
            System.out.println("Adding file to DM:"+url);
        }
        download_files.revalidate();
    }
    public static void setFileInfo(String file_name, int file_size) {
        // you will get this information when HTTP is Connection  done with the url provided
        download_status.setText("File(" + currentFileDNIndx + "/" + fileToDownloadCount + ")" + " Size:" + Integer.toString(file_size) + " Name:" + file_name);
    }

    public static void setDownloadProgress(int per) {
        download_progress.setValue(per);

    }

    public static void CancelDownload() {
        if(selectingDownloadPath){
            return;
        }
        System.out.println("Cancel Download ...");
        if (inUse && downloadInProgress) {
            int files_left = fileToDownloadCount - currentFileDNIndx;
            if (files_left != 0) {
                System.out.println("There are :" + files_left + "files ready to download in the Queue");
                // close all the connections
                
                
                downloadInProgress = false;
                
            } else {
                System.out.println("All the files are downloaded");
                downloadInProgress = false;
            }
        }
        
        if (!downloadInProgress) {
            // free the resourses
            download_dialog.setVisible(false);
            inUse = false;
            System.out.println("Downlaod Manager not in Use");
        }
    }

    public static void StartDownload() {
        if (inUse) {
            System.out.println("Starting Download...");
            downloading_urls.clear();
            // get the list of files to download and start donaloding one by one
            Component[] files = download_files.getComponents();
            for(Component f: files){
                if((f instanceof JCheckBox)){
                    JCheckBox chk = (JCheckBox)f;
                    if(chk.isSelected()){
                        String u = chk.getText();
                        try {
                            URI myUri = new URI("http","www.hallresearch.com",u, null);
                            downloading_urls.add(myUri.toASCIIString());
                        } catch (Exception ex) {
                            DEBUG.log(Level.WARNING,"Invalid URL: {0}",u+" : "+ex.getMessage());                           
                        } 
                    }
                }
            }
            
            if(downloading_urls.size() >0){
                System.out.println("Preparing to downlaod"+downloading_urls.toString());
                for (int i=0; i< 1;i++){
                    System.out.println("Downloading:"+downloading_urls.get(i)+" => "+DownloadPath);
                    download(downloading_urls.get(i),DownloadPath);
                }
            }else{
                System.out.println("Threre is nothing to download...");
            }
            
        }
    }
    
    private static boolean download(String URL, String location){
    
        // Start the download 
        DEBUG.log(Level.INFO,"Downloading:  {0}", URL+" -> "+ location);
            try {
                // testing
                DownloadTask task = new DownloadTask(URL, location);
                task.addPropertyChangeListener((evt) -> {
                    if (evt.getPropertyName().equals("progress")) {
                        int progress = (Integer) evt.getNewValue();
                        System.out.println("%" + Integer.toString(progress));
                    }
                });
                task.execute();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(hs,
                        "Error executing upload task: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
    
        return true;
    }

}
