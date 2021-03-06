/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.zoeker.configuratie;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.oracle.OracleNGDataStoreFactory;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.data.wfs.v1_0_0.WFSCapabilities;
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
    public static final String TYPE_JDBC = "jdbc";
    public static final String TYPE_ORACLE = "oracle";
    public static final String TYPE_WFS = "wfs";
    public static final String TYPE_EMPTY = "unknown";

    static protected Map<Map, WFSDataStore> perParameterSetDataStoreCache = new HashMap();
    static protected long dataStoreTimestamp = 0l;
    private static long dataStoreLifecycle = 0l;
    public static final String LIFECYCLE_CACHE_PARAM = "cachelifecycle";

    private static final Log log = LogFactory.getLog(Bron.class);

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

    public String getType() {
        if (checkType(TYPE_JDBC)) {
            return TYPE_JDBC;
        }
        if (checkType(TYPE_WFS)) {
            return TYPE_WFS;
        }
        return TYPE_EMPTY;
    }

    public boolean checkType(String type) {
        if (this.getUrl() == null || type == null || type.length() == 0) {
            return false;
        }
        if (this.getUrl().toLowerCase().startsWith("jdbc:")) {
            if (this.getUrl().toLowerCase().startsWith("jdbc:oracle:")) {
                if (type.equals(TYPE_ORACLE)) {
                    return true;
                }
            }
            if (type.equals(TYPE_JDBC)) {
                return true;
            }
        } else if (type.equals(TYPE_WFS)) {
            return true;
        }
        return false;
    }

    public DataStore toDatastore() throws IOException, Exception {
        if (this.getUrl() == null) {
            return null;
        }

        HashMap params = new HashMap();
        if (checkType(TYPE_ORACLE)) {
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
            params.put(OracleNGDataStoreFactory.HOST.key, host);
            params.put(OracleNGDataStoreFactory.PORT.key, port);
            if (schema != null) {
                params.put(OracleNGDataStoreFactory.SCHEMA.key, schema);
            }
            params.put(OracleNGDataStoreFactory.FETCHSIZE.key, 50);
            params.put(OracleNGDataStoreFactory.DATABASE.key, instance);
            params.put(OracleNGDataStoreFactory.USER.key, this.getGebruikersnaam());
            params.put(OracleNGDataStoreFactory.PASSWD.key, this.getWachtwoord());
            params.put(OracleNGDataStoreFactory.DBTYPE.key, "oracle");
            params.put(OracleNGDataStoreFactory.EXPOSE_PK.key, Boolean.TRUE);
            return (new OracleNGDataStoreFactory()).createDataStore(params);
        }

        if (checkType(TYPE_JDBC)) {
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
            params.put(PostgisNGDataStoreFactory.FETCHSIZE.key, 50);
            params.put(PostgisNGDataStoreFactory.DBTYPE.key, "postgis");
            params.put(PostgisNGDataStoreFactory.HOST.key, host);
            params.put(PostgisNGDataStoreFactory.PORT.key, port);
            params.put(PostgisNGDataStoreFactory.SCHEMA.key, schema);
            params.put(PostgisNGDataStoreFactory.DATABASE.key, database);
            if (this.getGebruikersnaam() != null) {
                params.put(PostgisNGDataStoreFactory.USER.key, this.getGebruikersnaam());
            }
            if (this.getWachtwoord() != null) {
                params.put(PostgisNGDataStoreFactory.PASSWD.key, this.getWachtwoord());
            }
            params.put(PostgisNGDataStoreFactory.EXPOSE_PK.key, Boolean.TRUE);
        }

        if (checkType(TYPE_WFS)) {
            String url = this.getUrl();
            if (this.getUrl().toLowerCase().indexOf("request=") == -1) {
                if (url.indexOf('?') < 0) {
                    url += "?request=GetCapabilities&service=WFS";
                } else if (url.indexOf('?') == url.length() - 1) {
                    url += "request=GetCapabilities&service=WFS";
                } else if (url.lastIndexOf('&') == url.length() - 1) {
                    url += "request=GetCapabilities&service=WFS";
                } else {
                    url += "&request=GetCapabilities&service=WFS";
                }
                if (url.toLowerCase().indexOf("version") == -1) {
                    url += "&Version=1.1.0";
                }
            }
            params.put(WFSDataStoreFactory.URL.key, url);
            if (this.getGebruikersnaam() != null && this.getWachtwoord() != null) {
                params.put(WFSDataStoreFactory.USERNAME.key, this.getGebruikersnaam());
                params.put(WFSDataStoreFactory.PASSWORD.key, this.getWachtwoord());
            }
            params.put(WFSDataStoreFactory.TIMEOUT.key, TIMEOUT);
            params.put(WFSDataStoreFactory.PROTOCOL.key, Boolean.TRUE);
            
            //Geotools zoekt Geoserver automatisch obv "geoserver" in url.
            //Daarnaast worden ionic, arcgis en cubewerx gevonden.
            //Mapserver moeten we dus zelf uitzoeken.
            if (url.contains("mapserv") && url.contains(".map")) {
                // als mapserver dus verborgen zit achter een algemene url, jammer!
                params.put(WFSDataStoreFactory.WFS_STRATEGY.key, "mapserver");
            }

            DataStore ds = getWfsCache(params);

            if (ds == null) {
                if (!perParameterSetDataStoreCache.containsKey(params)) {
                    ds = (new WFSDataStoreFactory()).createDataStore(params);
                    putWfsCache(params, (WFSDataStore) repairDataStore(ds));
                }
            }

            return ds;
 
        }
        return createDataStoreFromParams(params);
    }

    public static synchronized void flushWfsCache() {
        perParameterSetDataStoreCache = new HashMap();

        log.info("Cache WFS leeggemaakt.");
    }
    public static synchronized void putWfsCache(HashMap p, WFSDataStore ds) {
        perParameterSetDataStoreCache.put(p, ds);
    }
    public static synchronized WFSDataStore getWfsCache(HashMap p) {        
        if (isCacheExpired()) {
            flushWfsCache();

            return null;
        }

        if (perParameterSetDataStoreCache.containsKey(p)) {
            return perParameterSetDataStoreCache.get(p);
        }

        return null;
    }

    public static boolean isCacheExpired() {
        long nowTimestamp = (new Date()).getTime();
        
        if (getDataStoreLifecycle() > 0 && (nowTimestamp - dataStoreTimestamp) > getDataStoreLifecycle()) {
            dataStoreTimestamp = nowTimestamp;

            return true;
        }

        return false;
    }
    
    /**
     * @return the dataStoreLifecycle
     */
    public static long getDataStoreLifecycle() {
        return dataStoreLifecycle;
    }

    /**
     * @param aDataStoreLifecycle the dataStoreLifecycle to set
     */
    public static void setDataStoreLifecycle(long aDataStoreLifecycle) {
        dataStoreLifecycle = aDataStoreLifecycle;
    }


    public static DataStore createDataStoreFromParams(Map params) throws IOException, Exception {        
        DataStore ds = null;
        try {
            ds = repairDataStore(DataStoreFinder.getDataStore(params));
        } catch (IOException ex) {
            throw new Exception("Connectie naar gegevensbron mislukt. Controleer de bron instellingen.");
        }
        return ds;
    }

    private static DataStore repairDataStore(DataStore ds) throws Exception {        
        if (ds instanceof WFS_1_0_0_DataStore) {
            WFS_1_0_0_DataStore wfs100ds = (WFS_1_0_0_DataStore) ds;
            WFSCapabilities wfscap = wfs100ds.getCapabilities();
            // wfs 1.0.0 haalt prefix er niet af en zet de namespace niet
            // wfs 1.1.0 doet dit wel en hier fixen we dit.
//            List<FeatureSetDescription> fdsl = wfscap.getFeatureTypes();
//            for (FeatureSetDescription fds : fdsl) {
//                if (fds.getNamespace() != null) {
//                    continue;
//                }
//
//                String[] lna = fds.getName().split(":");
//                if (lna.length > 1) {
//                    String localName = lna[1];
//                    String nsPrefix = lna[0];
//                    URI nsUri = null;
//                    if (!nsPrefix.isEmpty()) {
//                        Schema[] schemas = SchemaFactory.getSchemas(nsPrefix);
//                        if (schemas.length > 0) {
//                            nsUri = schemas[0].getTargetNamespace();
//                        } else {
//                            try {
//                                nsUri = new URI(nsPrefix);
//                            } catch (URISyntaxException ex) {}
//                        }
//                    }
//                    fds.setName(localName);
//                    fds.setNamespace(nsUri);
//                }
//            }

            //omdat de WFS_1_0_0_Datastore niet met de opengis filters werkt even toevoegen dat
            //er simpele vergelijkingen kunnen worden gedaan. (de meeste servers kunnen dit natuurlijk);
            FilterCapabilities filterCap = wfscap.getFilterCapabilities();
            filterCap.addAll(FilterCapabilities.SIMPLE_COMPARISONS_OPENGIS);
            boolean b = filterCap.supports(FilterCapabilities.SIMPLE_COMPARISONS_OPENGIS);
            wfscap.setFilterCapabilities(filterCap);
        }
        
        return ds;
    }

    @Override
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
