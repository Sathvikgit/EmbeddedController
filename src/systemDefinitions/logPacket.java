package systemDefinitions;


import static embeddedcontroller.Utilities.bytesToHex;
import java.util.Date;

public class logPacket {


    public Date date = null;
    public String fromIP = "";
    public int fromPort =0;
    public String toIP = "";
    public int toPort = 0;
    public String type = "";
    public String ASCIIdata="";
    public byte[] data = null;

    
    public logPacket(Date date, String fromIP, int fromPort, String toIP, int toPort, String type, byte[] data) {
        this.date = date;
        this.fromIP = fromIP;
        this.fromPort = fromPort;
        this.toIP = toIP;
        this.toPort = toPort;
        this.type = type;
        this.data = data;
        this.ASCIIdata = new String(data);
    }
         
        public logPacket(Date date, String fromIP, int fromPort, String toIP, int toPort, String type, String ASCIIdata){
        this.date = date;
        this.fromIP = fromIP;
        this.fromPort = fromPort;
        this.toIP = toIP;
        this.toPort = toPort;
        this.type = type;
        this.ASCIIdata = ASCIIdata;
        this.data = ASCIIdata.getBytes();
    }

    @Override
    public String toString() {
        return "logPacket{" + "date=" + date + ", fromIP=" + fromIP + ", fromPort=" + fromPort + ", toIP=" + toIP + ", toPort=" + toPort + ", type=" + type + ", ASCIIdata=" + ASCIIdata + '}';
    }
        
    public String[] getRowData(){
     String[] rowData = {this.date.toString(), this.fromIP, Integer.toString(this.fromPort), this.toIP,Integer.toString(this.toPort),this.type,this.ASCIIdata,bytesToHex(this.data)};
     return (rowData);
    }

}
