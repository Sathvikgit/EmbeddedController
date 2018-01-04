package systemDefinitions;

import static embeddedcontroller.EmbeddedController.DEBUG;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;



/* This holds the information of HR_device class found  by the device finder*/
public class Device {
    
    public static final String HR_PRODUCT_URL = "http://www.hallresearch.com/page/Products/";
    
     public HR_Device info;
     public String hrProduct_url = "";
     public HttpClient httpClient;
     public HttpResponse httpresp;
     
     // Each Device has a HTTP client that pull the information from Hall Research Web Site
     
    public Device(HR_Device dev) {
        this.info = dev;
        this.hrProduct_url = HR_PRODUCT_URL+"CNT-IP-2";                //dev.get_device_model();
        this.httpClient = HttpClientBuilder.create().build();
        
        HttpGet request = null;

        try {
           request = new HttpGet(hrProduct_url); 
           request.addHeader("User-Agent", "Embedded Controller");
           
           httpresp = httpClient.execute(request);
            //parse reponse
           if(httpresp!= null){
               HttpEntity entity = httpresp.getEntity();
               String content = EntityUtils.toString(entity);
               System.out.println(content);
           }

           
        } catch (IOException ex) {
            DEBUG.log(Level.SEVERE,"Failed to send get req to : {0}",this.hrProduct_url);
        }finally{
            if (request != null) {
                request.releaseConnection();
            }
        }
        
    }

    /* This String will be acts as a name in Device Tree*/
    @Override
    public String toString() {
        return this.info.get_device_name()+"  ["+this.info.get_device_model()+"]";
    }
       
}
