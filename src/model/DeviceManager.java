package model;

import static embeddedcontroller.EmbeddedController.hs;
import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import systemDefinitions.Device;
import systemDefinitions.HR_Device;
import systemDefinitions.URLdownloadType;

public class DeviceManager {

    public static JTree devTree; 
    public static JTree downloadTree;
    private static DefaultTreeModel dnModel;
    private static DefaultTreeModel model;
    private static DefaultMutableTreeNode root;
    private static DefaultMutableTreeNode downRoot;
            
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
        displayHttpDeviceInfo(false);
        
        
        downloadTree =  hs.hr_deviceDownloadsTree;    
        dnModel = (DefaultTreeModel) downloadTree.getModel();
        downRoot = (DefaultMutableTreeNode) dnModel.getRoot();
        downRoot.removeAllChildren();
        downRoot.setUserObject("Downloads");
        wireDownloadTreeActionListener();
        
        // for Testing: Add some temp Devices 
        addNewDevice(new HR_Device("192.168.1.12", "STATIC","a0:a1:a2:a3:a4:a5:a6:a7:a8","HSM-I-04-04","HDMI Matrix","Hall Research","admin","pass"));
        addNewDevice(new HR_Device("192.168.1.13", "STATIC","a0:a1:a2:a3:a4:a5:a6:a7:a8","VSA-51","AV Control System","Hall Research","admin","pass"));
        addNewDevice(new HR_Device("192.168.1.14", "STATIC","a0:a1:a2:a3:a4:a5:a6:a7:a8","UI-IP8-DP","Key Pad","Hall Research","admin","pass"));
        
        
    }

    // Device will the send you the device information
    public static void addNewDevice(HR_Device dev) {
        root.add(new DefaultMutableTreeNode(new Device(dev)));
        devTree.expandRow(0);
        model.reload();
    }
    
    public static void addDownloadItem(URLdownloadType d){
        downRoot.add(new DefaultMutableTreeNode(d));
        //System.out.println("Adding:"+d.toString());
        downloadTree.expandRow(0);
        dnModel.reload();
    }
    public static void clearDownloadTree(){
        downRoot.removeAllChildren();
        dnModel.reload();
    }
    
    private void wireDownloadTreeActionListener(){
       
       downloadTree.expandRow(0);
       downloadTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
       
       // Cell renderer
       downloadTree.setCellRenderer(new DefaultTreeCellRenderer(){
       
           @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if(value instanceof DefaultMutableTreeNode){
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                    if (node.isRoot()) {
                        setIcon(UIManager.getIcon("FileView.hardDriveIcon"));
                    }else {
                        Object select = node.getUserObject();
                        if (select != null) {
                            if (select instanceof URLdownloadType) {
                                //URLdownloadType f = (URLdownloadType) select;
                                setIcon(UIManager.getIcon("Tree.collapsedIcon"));
                            }
                        }
                    }                    
                }
                return this;
            }             
       });
       
        downloadTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                // get Selcted device 
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) downloadTree.getLastSelectedPathComponent();
                if (node == null) {
                    return;
                }
                if (node.isRoot()) {   
                    System.out.println("Root Selected");
                    return;
                }
                Object nodeObject = node.getUserObject();
                if (nodeObject instanceof URLdownloadType) {
                    URLdownloadType dn = (URLdownloadType) nodeObject;
                    System.out.println("Open URL:"+dn.url.toString());
                    
                    // open the url in default browser
                    
                }
            }
        });
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
                    if (node.isRoot()) {
                        displayHttpDeviceInfo(false);
                        return;
                    }
                    Object nodeObject = node.getUserObject();
                    if(nodeObject instanceof Device){
                        Device device = (Device) nodeObject;
                        device.showDeviceInfo();
                    }
                }
            });

    }

    
    public static void displayHttpDeviceInfo(boolean state){
        hs.deviceHttpInfoContainer.setVisible(state);
    }
   
    
}
