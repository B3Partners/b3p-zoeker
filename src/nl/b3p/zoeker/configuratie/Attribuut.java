/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.b3p.zoeker.configuratie;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author beurs
 */
public class Attribuut {
    public final static int GEEN_TYPE=0;
    public final static int ID_TYPE=1;
    public final static int TOON_TYPE=2;
    public final static int GEOMETRY_TYPE=3;
    //datum typen
    public final static int KLEINER_DAN_TYPE=4;
    public final static int GROTER_DAN_TYPE=5;

    //typen die betrekking hebben op een van de andere typen.
    public final static int STRAAL_TYPE=100;
    
    private Integer id=null;
    private String naam=null;
    private String attribuutnaam=null;
    private String label=null;
    private Integer type=GEEN_TYPE;
    private Integer volgorde;
    private ZoekConfiguratie zoekConfiguratie;
    
    public Attribuut(){}
    public Attribuut(Attribuut a){
        this.id=a.getId();
        this.naam=a.getNaam();
        this.attribuutnaam=a.getAttribuutnaam();
        this.label=a.getLabel();
        this.type=a.getType();
        this.volgorde=a.getVolgorde();
        this.zoekConfiguratie=a.getZoekConfiguratie();
    }
    public Attribuut(Integer id, String naam, String attribuutnaam, String label, Integer type, Integer volgorde){
        this.id=id;
        this.naam=naam;
        this.attribuutnaam=attribuutnaam;
        this.label=label;
        this.type=type;
        this.volgorde=volgorde;
    }
    public Attribuut(Integer id, String attribuutnaam, String label){
        this(id,null,attribuutnaam,label,null,null);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public String getAttribuutnaam() {
        return attribuutnaam;
    }

    public void setAttribuutnaam(String attribuutnaam) {
        this.attribuutnaam = attribuutnaam;
    }

    public String getAttribuutLocalnaam(){
        return removeXmlPrefix(this.getAttribuutnaam());
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the zoekConfiguratie
     */
    public ZoekConfiguratie getZoekConfiguratie() {
        return zoekConfiguratie;
    }

    /**
     * @param zoekConfiguratie the zoekConfiguratie to set
     */
    public void setZoekConfiguratie(ZoekConfiguratie zoekConfiguratie) {
        this.zoekConfiguratie = zoekConfiguratie;
    }

    /**
     * @return the type
     */
    public Integer getType() {
        if (type==null){
            return GEEN_TYPE;
        }
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(Integer type) {
        this.type = type;
    }
     /**
     * @return the volgorde
     */
    public Integer getVolgorde() {
        return volgorde;
    }

    /**
     * @param volgorde the volgorde to set
     */
    public void setVolgorde(Integer volgorde) {
        this.volgorde = volgorde;
    }

    public JSONObject toJSON() throws JSONException{
        JSONObject json = new JSONObject();
        json.put("id",getId());
        json.put("naam",getNaam());
        json.put("attribuutnaam",getAttribuutLocalnaam());
        json.put("label",getLabel());
        json.put("type",getType());
        json.put("volgorde",getVolgorde());
        return json;
    }
    
    public String toString(){
        String returnValue="";
        returnValue+=this.getId();
        returnValue+=". ";
        returnValue+=this.getAttribuutnaam();
        returnValue+=" label: ";
        returnValue+=this.getLabel();
        returnValue+=" type: ";
        returnValue+=this.getType();
        return returnValue;
    }
    /**
     * Removes the prefix.
     */
    public static String removeXmlPrefix(String name){
        if (name==null){
            return null;
        }
        String returnValue= new String(name);
        int first=returnValue.indexOf(":");
        if (first >=0){
            if (first+2==name.length())
                return null;
            returnValue = returnValue.substring(first+1,returnValue.length());
        }
        return returnValue;

    }
}
