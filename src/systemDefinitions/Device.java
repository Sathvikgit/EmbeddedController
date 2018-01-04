package systemDefinitions;

import static embeddedcontroller.EmbeddedController.DEBUG;
import static embeddedcontroller.EmbeddedController.hs;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



/* This holds the information of HR_device class found  by the device finder*/
public class Device {
    
    public static final String HR_PRODUCT_URL = "http://www.hallresearch.com/page/Products/";
    public static final String HR_WEBSITE = "http://www.hallresearch.com";
    
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
               
               // JSOUP can make a GET request on its own
               // Document doc = Jsoup.connect("http://www.javatpoint.com").get();  
               
               // parse the html document
               Document doc  = Jsoup.parse(content);
               Elements deviceName = doc.getElementsByClass("productName");
               if(deviceName != null){
                   System.out.println("Device Name:"+deviceName.text());                  
               }
               Elements imageUrls = doc.getElementsByClass("ProductLeft").select("img");
               if(imageUrls!= null){
                   System.out.println("Images Found: "+imageUrls.size());                                        
                   System.out.println("Devie Image: "+imageUrls.attr("src"));
                   String imagePath  = HR_WEBSITE+imageUrls.attr("src");
                   URL deviceImageURL = new URL(imagePath);
                   
                   // downlaod the image 
                   try{
                    BufferedImage image = ImageIO.read(deviceImageURL);
                    System.out.println("Load image into frame...");
                    JLabel label = new JLabel(new ImageIcon(image));
                    
                    hs.DeviceProperties.add(label);
                    
                   }catch(Exception e){
                       System.out.println("Failed to download iamge from:"+deviceImageURL.toString());                      
                   }
                   
               }else{
                   System.out.println("No Images Found");
               }
               
               
               Element description = doc.getElementById("description");
               if(description != null){
                   System.out.println("Device Description:"+description.text());
               }
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
