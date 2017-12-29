
package model;


public class SerialCommunicator {
    GenericCommDriver m_Driver;

    public SerialCommunicator() 
    { 
        m_Driver = new GenericCommDriver();
               
    } 
 
    public CommDriver getDriver() 
    { 
        return m_Driver; 
    }    
}
