/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.zoeker.configuratie;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Roy
 */
public class ZoekConfiguratie {
    /*
     * String[] featureTypes= {"app:Plangebied"};
     * String[] searchPropertys={"app:overheidscode"};
     */

    private Integer id = null;
    // de te tonen search naam.
    private String naam = null;
    // tabel naam/feature type.
    private String featureType = null;
    // kolom waarop gezocht moet worden.
    private Set<ZoekAttribuut> zoekVelden = null;
    // alle resultaat velden.
    private Set<ResultaatAttribuut> resultaatVelden = null;
    // kolom /attribuut waar het id van het gezochte object in staat.
    private ZoekConfiguratie parentZoekConfiguratie = null;
    // bron
    private Bron parentBron = null;


    /**
     * cachedResultList bevat alle resultaten die de zoekconfiguratie kan hebben.
     * Het is een Map met de resultaatvelden.
     * De applicatie bepaalt of de resultaten opgehaald worden of
     * dat de cachedResultList wordt gebruikt.
     * De cachedInputList wordt via een caching mechanisme tijdens de configuratie
     * in een hulptabel bij deze zoekconfiguratie geladen.
     */
    private static Set<Map> cachedResultList = new HashSet();
    /**
     * cachedResultList geeft aan dat de lijst klaar is voor gebruik.
     * Alleen toepasselijk bij niet-dynamische lijsten.
     */
    private static boolean cachedResultListReady = false;
    /**
     * Als een lijst dynamisch is, dan wordt de lijst bij elke vraag
     * weer opnieuw opgevraagd. Niet-dynamische lijsten, dus statisch,
     * worden vooraf gecached. Als een gecachete lijst niet klaar is,
     * wordt gehandeld alsof er geen lijst is.
     */
    private boolean resultListDynamic = true;


    public ZoekConfiguratie() {
    }

    public ZoekConfiguratie(Integer id, String naam, String featureType, Bron parentBron, ZoekConfiguratie parentZoekConfiguratie) {
        this.id = id;
        this.naam = naam;
        this.featureType = featureType;
        this.parentBron = parentBron;
        this.parentZoekConfiguratie = parentZoekConfiguratie;
    }

    /**
     * @return the featureType
     */
    public String getFeatureType() {
        return featureType;
    }

    /**
     * @param featureType the featureType to set
     */
    public void setFeatureType(String featureType) {
        this.featureType = featureType;
    }

    /**
     * @return the searchProperty
     */
    public Set<ZoekAttribuut> getZoekVelden() {
        return zoekVelden;
    }

    /**
     * @param searchProperty the searchProperty to set
     */
    public void setZoekVelden(Set zoekVelden) {
        this.zoekVelden = zoekVelden;
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the parentZoekConfiguratie
     */
    public ZoekConfiguratie getParentZoekConfiguratie() {
        return parentZoekConfiguratie;
    }

    /**
     * @param parentZoekConfiguratie the parentZoekConfiguratie to set
     */
    public void setParentZoekConfiguratie(ZoekConfiguratie parentZoekConfiguratie) {
        this.parentZoekConfiguratie = parentZoekConfiguratie;
    }

    /**
     * @return the parentBron
     */
    public Bron getParentBron() {
        return parentBron;
    }

    /**
     * @param parentBron the parentBron to set
     */
    public void setParentBron(Bron parentBron) {
        this.parentBron = parentBron;
    }

    /**
     * @return the naam
     */
    public String getNaam() {
        return naam;
    }

    /**
     * @param naam the naam to set
     */
    public void setNaam(String naam) {
        this.naam = naam;
    }

    public Bron getBron() {
        if (getParentBron() != null) {
            return getParentBron();
        } else if (getParentZoekConfiguratie() != null) {
            return getParentZoekConfiguratie().getBron();
        } else {
            return null;
        }
    }

    /**
     * @return the resultaatVelden
     */
    public Set<ResultaatAttribuut> getResultaatVelden() {
        return resultaatVelden;
    }

    /**
     * @param resultaatVelden the resultaatVelden to set
     */
    public void setResultaatVelden(Set resultaatVelden) {
        this.resultaatVelden = resultaatVelden;
    }

    /**
     * Voeg een zoekAttribuut toe
     */
    public void addZoekAttribuut(ZoekAttribuut zoekAttribuut) {
        if (zoekVelden == null) {
            zoekVelden = new HashSet();
        }
        zoekAttribuut.setZoekConfiguratie(this);
        zoekVelden.add(zoekAttribuut);
    }

    /**
     * Voeg een ResultaatAttribuut toe
     */
    public void addResultaatAttribuut(ResultaatAttribuut resultaatAttribuut) {
        if (resultaatVelden == null) {
            resultaatVelden = new HashSet();
        }
        resultaatAttribuut.setZoekConfiguratie(this);
        resultaatVelden.add(resultaatAttribuut);
    }

    /**
     * maak een Json object van dit object
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", getId());
        json.put("naam", getNaam());
        json.put("featureType", getFeatureType());
        if (getZoekVelden() != null) {
            Iterator it = getZoekVelden().iterator();
            JSONArray jsonZoekVelden = null;
            while (it.hasNext()) {
                if (jsonZoekVelden == null) {
                    jsonZoekVelden = new JSONArray();
                }
                ZoekAttribuut zoekVeld = (ZoekAttribuut) it.next();
                jsonZoekVelden.put(zoekVeld.toJSON());
            }
            json.put("zoekVelden", jsonZoekVelden);
        }
        if (getResultaatVelden() != null) {
            Iterator it = getResultaatVelden().iterator();
            JSONArray jsonResultaatVelden = null;
            while (it.hasNext()) {
                if (jsonResultaatVelden == null) {
                    jsonResultaatVelden = new JSONArray();
                }
                ResultaatAttribuut resultaatVeld = (ResultaatAttribuut) it.next();
                jsonResultaatVelden.put(resultaatVeld.toJSON());
            }
            json.put("resultaatVelden", jsonResultaatVelden);
        }
        if (getParentZoekConfiguratie() != null) {
            json.put("parentZoekConfiguratieId", getParentZoekConfiguratie().getId());
        }
        if (getBron() != null) {
            json.put("bron", getParentBron().toJSON());
        }
        return json;

    }

    public String toString() {
        String returnValue = "";
        if (getNaam() != null) {
            returnValue += getNaam() + " ";
        }
        if (getFeatureType() != null) {
            returnValue += getFeatureType();
        }
        if (getBron() != null) {
            returnValue += " Bron: " + getBron().toString();
        }
        return returnValue;
    }

    /**
     * @return the cachedResultList
     */
    public static Set<Map> getCachedResultList() {
        return cachedResultList;
    }

    /**
     * @param cachedResultList the cachedResultList to set
     */
    public static void setCachedResultList(Set<Map> crl) {
        cachedResultList = crl;
    }

    /**
     * @return the cachedResultListReady
     */
    public static boolean isCachedResultListReady() {
        return cachedResultListReady;
    }

    /**
     * @param cachedResultListReady the cachedResultListReady to set
     */
    public static void setCachedResultListReady(boolean crlReady) {
        cachedResultListReady = crlReady;
    }

    /**
     * @return the resultListDynamic
     */
    public boolean isResultListDynamic() {
        return resultListDynamic;
    }

    /**
     * @param resultListDynamic the resultListDynamic to set
     */
    public void setResultListDynamic(boolean resultListDynamic) {
        this.resultListDynamic = resultListDynamic;
    }
}
