package model;

import static embeddedcontroller.EmbeddedController.hs;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class DownLoadManager {
    private static JLabel download_status = hs.dowloadingFileName;
    private static JPanel download_files = hs.downloadFileListContainer;
    private static JLabel download_save_path = hs.downloadFilePath;
    private static JProgressBar download_progress = hs.downloadFileProgress;
    
    public static void CancelDownload(){
    
    }
    public static void StartDownload(){
    
    }
    
}
