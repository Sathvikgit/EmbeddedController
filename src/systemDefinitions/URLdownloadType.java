/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package systemDefinitions;

import java.util.ArrayList;

public class URLdownloadType {
    
    public String name;
    public ArrayList<String> url;

    public URLdownloadType(String name, ArrayList<String> url) {
        this.name = name;
        this.url = url;
    }

    @Override
    public String toString() {
        return this.name; //To change body of generated methods, choose Tools | Templates.
    }
    
}
