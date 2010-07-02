/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.zoeker.configuratie;

import java.io.IOException;
import java.util.HashMap;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.oracle.OracleDataStoreFactory;
import org.geotools.data.ows.WFSCapabilities;
import org.geotools.data.postgis.PostgisDataStoreFactory;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.data.wfs.v1_0_0.WFS_1_0_0_DataStore;
import org.geotools.filter.FilterCapabilities;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Roy
 */
public class Bron {

    private Integer id = null;
    private String naam = null;
    private String url = null;
    private String gebruikersnaam = null;
    private String wachtwoord = null;
    private Integer volgorde = null;
    private static final int TIMEOUT = 60000;

    public Bron() {
    }

    public Bron(Integer id, String naam, String url, String gebruikersnaam, String wachtwoord, Integer volgorde) {
        this.id = id;
        this.naam = naam;
        this.url = url;
        this.gebruikersnaam = gebruikersnaam;
        this.wachtwoord = wachtwoord;
        this.volgorde = volgorde;
    }

    public Bron(Integer id, String naam, String url) {
        this(id, naam, url, null, null, null);
    }

//getters and setters
    /**
     * @return the Id
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

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
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

    /**
     * @return the gebruikersnaam
     */
    public String getGebruikersnaam() {
        return gebruikersnaam;
    }

    /**
     * @param gebruikersnaam the gebruikersnaam to set
     */
    public void setGebruikersnaam(String gebruikersnaam) {
        this.gebruikersnaam = gebruikersnaam;
    }

    /**
     * @return the wachtwoord
     */
    public String getWachtwoord() {
        return wachtwoord;
    }

    /**
     * @param wachtwoord the wachtwoord to set
     */
    public void setWachtwoord(String wachtwoord) {
        this.wachtwoord = wachtwoord;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("naam", getNaam());
        json.put("url", getUrl());
        json.put("volgorde", getVolgorde());
        return json;
    }

    public DataStore toDatastore() throws IOException {

        if (this.getUrl() == null) {
            return null;
        }
        HashMap params = new HashMap();
        if (this.getUrl().toLowerCase().startsWith("jdbc:oracle:")) {
            //jdbc:oracle:thin:@b3p-demoserver:1521:ORCL
            int firstIndex;
            int lastIndex;
            firstIndex = this.getUrl().indexOf("@") + 1;
            lastIndex = this.getUrl().indexOf(":", firstIndex);
            String host = this.getUrl().substring(firstIndex, lastIndex);
            firstIndex = lastIndex + 1;
            lastIndex = this.getUrl().indexOf(":", firstIndex);
            String port = this.getUrl().substring(firstIndex, lastIndex);
            firstIndex = lastIndex + 1;
            lastIndex = this.getUrl().indexOf(".", firstIndex);
            String schema = null;
            if (lastIndex == -1) {
                lastIndex = this.getUrl().length();
            } else {
                schema = this.getUrl().substring(lastIndex + 1, this.getUrl().length());
            }
            String instance = this.getUrl().substring(firstIndex, lastIndex);
            params.put("host", host);
            params.put("port", port);
            if (schema != null) {
                params.put("schema", schema);
            }
            params.put("instance", instance);
            params.put("user", this.getGebruikersnaam());
            params.put("passwd", this.getWachtwoord());
            params.put("dbtype", "oracle");
            return (new OracleDataStoreFactory()).createDataStore(params);
        }
        if (this.getUrl().toLowerCase().startsWith("jdbc:")) {
            //jdbc:postgresql://localhost:5432/edamvolendam_gis
            int firstIndex;
            int lastIndex;
            firstIndex = this.getUrl().indexOf("//") + 2;
            lastIndex = this.getUrl().indexOf(":", firstIndex);
            String host = this.getUrl().substring(firstIndex, lastIndex);
            firstIndex = lastIndex + 1;
            lastIndex = this.getUrl().indexOf("/", firstIndex);
            String port = this.getUrl().substring(firstIndex, lastIndex);
            firstIndex = lastIndex + 1;
            String database = this.getUrl().substring(firstIndex, this.getUrl().length());
            String schema = "public";
            if (database.indexOf(".") >= 0) {
                String[] tokens = database.split("\\.");
                if (tokens.length == 2) {
                    schema = tokens[1];
                    database = tokens[0];
                }
            }
            params.put(PostgisDataStoreFactory.DBTYPE.key, "postgis");
            params.put(PostgisDataStoreFactory.HOST.key, host);
            params.put(PostgisDataStoreFactory.PORT.key, port);
            params.put(PostgisDataStoreFactory.SCHEMA.key, schema);
            params.put(PostgisDataStoreFactory.DATABASE.key, database);
            if (this.getGebruikersnaam() != null) {
                params.put(PostgisDataStoreFactory.USER.key, this.getGebruikersnaam());
            }
            if (this.getWachtwoord() != null) {
                params.put(PostgisDataStoreFactory.PASSWD.key, this.getWachtwoord());
            }
        } else {
            String url = this.getUrl();
            if (this.getUrl().toLowerCase().indexOf("request=") == -1) {
                if (url.indexOf("?") > 0) {
                    url += "&";
                } else {
                    url += "?";
                }
                url += "request=GetCapabilities&service=WFS";
                //temp hack: default use version 1.0.0
                if (url.toLowerCase().indexOf("version") == -1) {
                    url += "&Version=1.0.0";
                }
            }
            params.put(WFSDataStoreFactory.URL.key, url);
            if (this.getGebruikersnaam() != null) {
                params.put(WFSDataStoreFactory.USERNAME.key, this.getGebruikersnaam());
            }
            if (this.getWachtwoord() != null) {
                params.put(WFSDataStoreFactory.PASSWORD.key, this.getWachtwoord());
            }
            params.put(WFSDataStoreFactory.TIMEOUT.key, TIMEOUT);
        }
        DataStore ds = DataStoreFinder.getDataStore(params);
        /*omdat de WFS_1_0_0_Datastore niet met de opengis filters werkt even toevoegen dat
        er simpelle vergelijkingen kunnen worden gedaan. (de meeste servers kunnen dit natuurlijk);*/
        if (ds instanceof WFS_1_0_0_DataStore) {
            WFS_1_0_0_DataStore wfs100ds = (WFS_1_0_0_DataStore) ds;
            WFSCapabilities wfscap = wfs100ds.getCapabilities();
            FilterCapabilities filterCap = wfscap.getFilterCapabilities();
            filterCap.addAll(FilterCapabilities.SIMPLE_COMPARISONS_OPENGIS);
            boolean b=filterCap.supports(FilterCapabilities.SIMPLE_COMPARISONS_OPENGIS);
            wfscap.setFilterCapabilities(filterCap);
        }
        return ds;
    }

    public String toString() {
        String returnValue = "";
        if (getNaam() != null) {
            returnValue += getNaam() + " ";
        }
        if (getUrl() != null) {
            returnValue += "(" + getUrl() + ")";
        }
        return returnValue;
    }
}
