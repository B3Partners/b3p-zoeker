/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.b3p.zoeker.configuratie;

/**
 *
 * @author beurs
 */
public class Attribuut {
    public final static int GEEN_TYPE=0;
    public final static int ID_TYPE=1;
    public final static int TOON_TYPE=2;
    public final static int GEOMETRY_TYPE=3;
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
        this.zoekConfiguratie=a.getZoekConfiguratie();
    }
    public Attribuut(Integer id, String attribuutnaam, String label){
        this.id=id;
        this.attribuutnaam=attribuutnaam;
        this.label=label;
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
/*
    public JSONObject toJSONObject() throws JSONException{
        JSONObject json= new JSONObject();
        json.put("id", this.getId());
        json.put("attribuutnaam",this.getAttribuutLocalnaam());
        json.put("label",this.getLabel());
        json.put("type",this.getType());
        json.put("zoekConfigId",getZoekConfiguratie().getId());
        return json;
    }
*/
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
