
package systemDefinitions;

import embeddedcontroller.EmbeddedController;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;


public class MyLogFormatter extends Formatter{

    private static SimpleDateFormat localDateFormat = new SimpleDateFormat("HH:mm:ss");
    
   
    
    @Override
    public String format(LogRecord record) {
        String message=record.getMessage();
        Object[] parm = record.getParameters();
        if(parm != null){         
              message = message.replace("{0}", Arrays.toString(parm));            
        }
        
        if(record.getSequenceNumber() == 0){
            String logBanner  = "\r\n-------------------------------------------------\r\n"
                                +"Embedded Controller"
                                +"Version:"+EmbeddedController.SOFTWARE_VERSION+"\r\n"
                                +"Log Date:"+ new Date().toString()+"\r\n"+
                                "\r\n-------------------------------------------------\r\n"; 
            message = logBanner+"\r\n"+message;
        }
        
        return localDateFormat.format(new Date(record.getMillis()))+"  <"+record.getLevel()+">  "+
                            record.getSourceClassName()+
                            "("+record.getSourceMethodName()+"):   " 
                            +message+"\r\n";
    }
    
}
