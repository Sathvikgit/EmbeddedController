package embeddedcontroller;

import static embeddedcontroller.EmbeddedController.*;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.DefaultTableModel;
import org.apache.ftpserver.impl.FtpIoSession;
import org.apache.mina.core.session.IdleStatus;

public class SystemStatus {

    // System Info
    public static String ipv4_system, _ipv4_address;
    
    
    // Note: All the function in this class should run in the background
    // FTP Server
    public static void update_FTPServerStatus() {
        DEBUG.log(Level.INFO, "SystemStatus: {0}", "Updating FTP Server");
        Set<FtpIoSession> CurrentSessions = null;
        int sessionCount = 0;
        Object[][] sessionData; 
        
        //{"ID","IP","User","Login Time","Status","Action"};
        String[] SessionsActions = {"Close", "Close Now","Suspend Read/Write","Resume Read/Write"};
        JComboBox comboBox = new JComboBox(SessionsActions);
        DefaultTableModel model = (DefaultTableModel) hs.FTPClientTableData.getModel();
        hs.FTPClientTableData.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(comboBox));
        
        while (true) {
            // upadate the session informations
            CurrentSessions = myFTPServer.getActiveSessions();
            if (CurrentSessions != null) {
                sessionCount = CurrentSessions.size();
                hs.FTPClientCount.setText(Integer.toString(sessionCount));
                
                // clear all the data
                model.setRowCount(0);
                String status = "";
                for (FtpIoSession ses : CurrentSessions) {
                    
                    model.addRow(new Object[]{
                       ((UUID)ses.getSessionId()).toString(),
                       ses.getRemoteAddress().toString(),
                       ses.getUser().getName(),
                       ses.getLastAccessTime().toString(),
                       (ses.isIdle(IdleStatus.BOTH_IDLE)?"Idle":"Working"),
                       ""
                   });        
                }
            }else{
                // clear the rows 
                if(model.getRowCount() > 0){
                    model.setRowCount(0);
                }
                hs.FTPClientCount.setText(Integer.toString(0));
            }
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(SystemStatus.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
