package model;

import static embeddedcontroller.EmbeddedController.DEBUG;
import static embeddedcontroller.EmbeddedController.hs;
import java.awt.Component;
import java.util.logging.Level;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import static model.FTPclient.userLog;
import model.SystemDefinitions.*;
import static model.SystemDefinitions.DEVICE_TYPE.*;
import static model.SystemDefinitions.*;
import org.apache.commons.net.ftp.FTPFile;
import systemDefinitions.Device;
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
        
        devTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        wireDeviceTreeEventListeners();
        
        // for Testing: Add some temp Devices 
        //addNewDevice(new HR_Device("192.168.1.12", "STATIC","a0:a1:a2:a3:a4:a5:a6:a7:a8","CNT-IP-2","FHD Controller","Hall Research","admin","pass"));
        
        
    }

    // Device will the send you the device information
    public static void addNewDevice(HR_Device dev) {
        root.add(new DefaultMutableTreeNode(new Device(dev)));
        devTree.expandRow(0);
    }

    private void wireDeviceTreeEventListeners() {

        //Always Expand the tree
        devTree.expandRow(0);
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
                            if (select instanceof Device) {
                                Device f = (Device) select;
                                setIcon(UIManager.getIcon("FileView.computerIcon"));
                            }
                        }
                    }
                }
                return this;
            }

        });
        
        // Device Selection Event Handlers
            devTree.addTreeSelectionListener(new TreeSelectionListener() {
                @Override
                public void valueChanged(TreeSelectionEvent e) {
                    // get Selcted device 
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)devTree.getLastSelectedPathComponent();
                    if(node == null){return;}
                    if (node.isRoot()) {return;}
                    Object nodeObject = node.getUserObject();
                    if(nodeObject instanceof Device){
                        Device device = (Device) nodeObject;
                        device.showDeviceInfo();
                    }
                }
            });

    }

}
