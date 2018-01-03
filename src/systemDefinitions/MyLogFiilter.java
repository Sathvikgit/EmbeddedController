package systemDefinitions;

import embeddedcontroller.EmbeddedController;
import static embeddedcontroller.EmbeddedController.DEBUG;
import java.util.ArrayList;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class MyLogFiilter implements Filter{

    public static boolean StopLogging = false;

    // class in this list will not be logged
    public static ArrayList<String> filterClasses;

    public MyLogFiilter() {
        filterClasses = new ArrayList<>();
        
        // Filter the following Source classes
        filterClasses.add("systemDefinitions.responding_network_devices");
        filterClasses.add("systemDefinitions.process_packet");
        
        DEBUG.log(Level.INFO,"Log Filters:"+ filterClasses.toString()+"\r\n");
    }
    
    
    // Check if a given log record should be published.
    // return false: Will not be logged
    @Override
    public boolean isLoggable(LogRecord record) {
        if(StopLogging){
            return false;
        }
        
        if(filterClasses.contains(record.getSourceClassName())){
            return false;
        }
        
         return true;
    }
    
    // Use this methd to set the log levels
    public static void SetLogLevel(Level l){
        DEBUG.setLevel(l);
    }
    
}
