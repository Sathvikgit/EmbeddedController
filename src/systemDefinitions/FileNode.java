/* This object represents local FTPFile*/

package systemDefinitions;

import java.io.File;

public class FileNode {

    private File file= null;
    private String path= null;
    
    public FileNode( File f) {
        this.file = f;
        path = f.getAbsolutePath();
    }

    public File getFile(){
        return this.file;
    }
  
    public boolean isDirectory(){
        return (this.file.isDirectory());
    }
    public boolean isFile(){
        return (this.file.isFile());
    }
    public boolean isHidden(){
        return (this.file.isHidden());
    }
    
    @Override
    public String toString() {
        return file.getName();
    }
      
    
}
