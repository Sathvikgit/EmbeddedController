package model;

import static embeddedcontroller.EmbeddedController.DEBUG;
import static embeddedcontroller.EmbeddedController.hs;
import java.awt.Component;
import java.util.logging.Level;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import model.SystemDefinitions.*;
import static model.SystemDefinitions.DEVICE_TYPE.*;
import static model.SystemDefinitions.*;
import org.apache.commons.net.ftp.FTPFile;
import systemDefinitions.HR_Device;

public class DeviceManager {

    private static JTree devTree; 
    private static DefaultTreeModel model;
    private static DefaultMutableTreeNode root;

    public DeviceManager() {

        devTree = hs.dfDeviceTree;
        model = (DefaultTreeModel) devTree.getModel();
        root = (DefaultMutableTreeNode) model.getRoot();
        root.removeAllChildren();
        root.setUserObject("Devices");
        
        model.nodeChanged(root); // paint this node again    
        model.reload();
        wireDeviceTreeEventListeners();
    }

    // Device will the send you the device information
    public static void addNewDevice(HR_Device dev) {
        root.add(new DefaultMutableTreeNode(dev));
        
    }

    private void wireDeviceTreeEventListeners() {

        // Cell render for tree nodes
        devTree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                    if (node.isRoot()) {
                        setIcon(UIManager.getIcon("FileView.homeFolderIcon"));
                    } else {
                        Object select = node.getUserObject();
                        if (select != null) {
                            if (select instanceof HR_Device) {
                                HR_Device f = (HR_Device) select;
                                setIcon(UIManager.getIcon("FileView.computerIcon"));
                            }
                        }
                    }
                }
                return this;
            }

        });
        
        

    }

}
