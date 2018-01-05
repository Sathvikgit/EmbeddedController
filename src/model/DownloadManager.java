package model;

import static embeddedcontroller.EmbeddedController.hs;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import utilities.DownloadTask;

public class DownloadManager {

    // you always need to check this flag before working with Download Manager
    public static boolean inUse = false;
    
    private static JLabel download_status = hs.dowloadingFileName;
    private static JPanel download_files = hs.downloadFileListContainer;
    private static JLabel download_save_path = hs.downloadFilePath;
    private static JProgressBar download_progress = hs.downloadFileProgress;
    private static JDialog download_dialog = hs.downloadFIleDialog;
    
    private static int fileToDownloadCount = 0;
    private static int currentFileDNIndx = 0;
    public static boolean downloadInProgress = false;
    
    private static DownloadTask[] downloadTasks;
    
    public DownloadManager() {
        // open the dialog
       download_dialog.setVisible(true);
       download_progress.setValue(0);
       inUse = true;
       System.out.println("Download Manager inUSE");
       
       // show the list of files to download
    }
    
    public static void setFileInfo(String file_name, int file_size){
        // you will get this information when HTTP is Connection  done with the url provided
        download_status.setText("File("+currentFileDNIndx+"/"+fileToDownloadCount+")"+" Size:"+Integer.toString(file_size)+" Name:"+file_name);
    }
    public static void setDownloadProgress(int per){
        download_progress.setValue(per);
    
    }
    public static void CancelDownload() {
        if(inUse && downloadInProgress){
            int files_left = fileToDownloadCount -currentFileDNIndx;
            if(files_left !=0){
                System.out.println("There are :"+files_left+ "files ready to download in the Queue");
            }else{
                System.out.println("All the files are downloaded");
                downloadInProgress = false;
            }
        }
        if(!downloadInProgress){
            // free the resourses
            download_dialog.setVisible(false);
            inUse = false;
            System.out.println("Downlaod Manager not in Use");
        }
    }

    public static void StartDownload() {
        if(inUse){
            System.out.println("Starting Download...");
            
            // get the list of files to download 

            // Start the download 
            try {
                // testing
                DownloadTask task = new DownloadTask("http://www.hallresearch.com/files/manuals/HSM0404_%20HSM0402.pdf", "C:\\Users\\gsath\\Desktop\\MyFTPServer\\Downloads");
                task.addPropertyChangeListener((evt) -> {
                  if (evt.getPropertyName().equals("progress")) {
                        int progress = (Integer) evt.getNewValue();
                        System.out.println("%"+Integer.toString(progress));
                    }
                });
                task.execute();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(hs,
                        "Error executing upload task: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            
        }
    }

}
