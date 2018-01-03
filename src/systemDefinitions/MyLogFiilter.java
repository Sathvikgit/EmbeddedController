package systemDefinitions;

import embeddedcontroller.EmbeddedController;
import static embeddedcontroller.EmbeddedController.DEBUG;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class MyLogFiilter implements Filter{

    public static boolean StopLogging = false;
    
    // Check if a given log record should be published.
    // return false: Will not be logged
    @Override
    public boolean isLoggable(LogRecord record) {
        if(StopLogging){
            return false;
        }else{
        
            return true;
        }
    }
    
    // Use this methd to set the log levels
    public static void SetLogLevel(Level l){
        DEBUG.setLevel(l);
    }
    
}
