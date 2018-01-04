package systemDefinitions;

import static embeddedcontroller.EmbeddedController.DEBUG;
import static embeddedcontroller.EmbeddedController.hs;
import embeddedcontroller.Utilities;
import static embeddedcontroller.Utilities.imageResize;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
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
public class Device implements Runnable{
    
    public static final String HR_PRODUCT_URL = "http://www.hallresearch.com/page/Products/";
    public static final String HR_WEBSITE = "http://www.hallresearch.com";
    
     public HR_Device info = null;
     public String hrProduct_url = "";
     public Document doc = null;
     public BufferedImage image_fit = null;
     public String deviceDescription= null;
     
     // raise this flag when you get all the information from HR Website
     public boolean httpFetchDone = false;
     public boolean httpFetchError = false;
     
     
     private HttpClient httpClient = null;
     private HttpResponse httpresp = null;
     
     // Each Device has a HTTP client that pull the information from Hall Research Web Site
    public Device(HR_Device dev) {
        this.info = dev;
    }

    /* This String will be acts as a name in Device Tree*/
    @Override
    public String toString() {
        return this.info.get_device_name()+"  ["+this.info.get_device_model()+"]";
    }

    public void showDeviceInfo(){
        new Thread(this).start();
    }
    
    @Override
    public void run() {
        
        // if already have the data about this device, then just show the information
        if(!this.httpFetchDone && !this.httpFetchError){
            
            DEBUG.log(Level.INFO,"Fetching Device info for {0}", this.toString());
            this.hrProduct_url = HR_PRODUCT_URL+"CNT-IP-2";                //dev.get_device_model();
            this.httpClient = HttpClientBuilder.create().build();
            HttpGet request = null;

            try {

               request = new HttpGet(hrProduct_url); 
               request.addHeader("User-Agent", "Embedded Controller");

               httpresp = httpClient.execute(request);
                //parse reponse
               if(httpresp!= null){
                   this.httpFetchDone = true;
                   
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
                   // Device image 
                   Elements imageUrls = doc.getElementsByClass("ProductLeft").select("img");
                   if(imageUrls!= null){
                       System.out.println("Images Found: "+imageUrls.size());                                        
                       System.out.println("Device Image: "+imageUrls.attr("src"));
                       String imagePath  = HR_WEBSITE+imageUrls.attr("src");
                       URL deviceImageURL = new URL(imagePath);
                       // downlaod the image 
                       try{
                           System.out.println("ImagePath:"+imagePath);   
                           BufferedImage image = ImageIO.read(deviceImageURL);
                           if(image != null){
                               // resize the image 
                               JLabel container = hs.hr_deviceImage;
                               this.image_fit = imageResize(image,container.getWidth(), container.getHeight());
                               DEBUG.log(Level.INFO,"Image Resized to {0}", "WxH ="+image_fit.getHeight()+"X"+image_fit.getHeight());                       
                           }else{
                               DEBUG.log(Level.INFO,"Failed to load device Image from {0}", imagePath);
                           }
                       }catch(Exception e){
                           System.out.println("Failed to download iamge from:"+deviceImageURL.toString());                      
                       }
                   }else{
                       System.out.println("No Images Found");
                   }

                   //Device Desciption
                   Element description = doc.getElementById("description");
                   if(description != null){                       
                       this.deviceDescription = description.text();
                       System.out.println("Device Description:"+this.deviceDescription);
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
        
        // If the HTTPfetch is already done with out any error just show this to the user when selected
        if(this.httpFetchDone && !this.httpFetchError){
            DEBUG.log(Level.INFO,"Showing HTTP Device Informaiton of {0}", this.toString());
            if(this.image_fit != null){
                DEBUG.log(Level.INFO,"Showing Image");
                hs.hr_deviceImage.setIcon(new ImageIcon(this.image_fit));
            }else{
                DEBUG.log(Level.WARNING,"No Image for {0}", this.toString());
            }
            
        }
        
    }
       
}
