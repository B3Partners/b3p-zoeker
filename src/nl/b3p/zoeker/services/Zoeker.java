/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.zoeker.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import nl.b3p.zoeker.configuratie.Bron;
import nl.b3p.zoeker.configuratie.ResultaatAttribuut;
import nl.b3p.zoeker.configuratie.ZoekAttribuut;
import nl.b3p.zoeker.configuratie.ZoekConfiguratie;
import nl.b3p.zoeker.hibernate.MyEMFDatabase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultFeatureResults;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.SchemaNotFoundException;
import org.geotools.data.oracle.OracleDataStoreFactory;
import org.geotools.data.ows.WFSCapabilities;
import org.geotools.data.postgis.PostgisDataStoreFactory;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.data.wfs.v1_0_0.WFS_1_0_0_DataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.FilterCapabilities;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

/**
 *
 * @author Roy
 */
public class Zoeker {

    private static final int topMaxResults = 1000;
    private static final Log log = LogFactory.getLog(Zoeker.class);
    private static final int TIMEOUT = 60000;

    public List zoek(Integer[] zoekConfiguratieIds, String searchStrings[], Integer maxResults) {
        if (maxResults == null || maxResults.intValue() == 0 || maxResults.intValue() > topMaxResults) {
            maxResults = topMaxResults;
        }
        List results = new ArrayList();
        Object identity = null;
        try {
            identity = MyEMFDatabase.createEntityManager(MyEMFDatabase.MAIN_EM);
            EntityManager em = MyEMFDatabase.getEntityManager(MyEMFDatabase.MAIN_EM);
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                for (int i = 0; i < zoekConfiguratieIds.length; i++) {
                    ZoekConfiguratie zc = null;
                    try {
                        zc = (ZoekConfiguratie) em.createQuery("from ZoekConfiguratie z where z.id = :id").setParameter("id", zoekConfiguratieIds[i]).getSingleResult();
                    } catch (NoResultException nre) {
                    }
                    results = zoekMetConfiguratie(zc, searchStrings, maxResults, results);
                }
                tx.commit();
            } catch (Exception ex) {
                log.error("Exception occured" + (tx.isActive() ? ", rollback" : "tx not active"), ex);
                if (tx.isActive()) {
                    tx.rollback();
                }
            }

        } catch (Throwable e) {
            log.error("Exception occured in search: ", e);
        } finally {
            log.debug("Closing entity manager .....");
            MyEMFDatabase.closeEntityManager(identity, MyEMFDatabase.MAIN_EM);
        }
        if (results != null) {
            Collections.sort(results);
        }
        return results;
    }

    /**
     * Zoek moet configuratie (search Strings moet gelijk zijn aan aantal zoekvelden in de ZoekConfiguratie:
     * @param zc: ZoekConfiguratie waarmee gezocht moet worden
     * @param searchStrings Een array van gezochte waarden. (moet gelijk zijn aan het aantal geconfigureerde zoekvelden en ook in die volgorde staan)
     * @param maxResults: Het maximaal aantal resultaten dat getoond moeten worden.
     * @param results: De al gevonden resultaten (de nieuwe resultaten worden hier aan toegevoegd.
     */
    private List zoekMetConfiguratie(ZoekConfiguratie zc, String[] searchStrings, Integer maxResults, List results) {
        if (zc == null || searchStrings == null) {
            return results;
        }
        Bron bron = zc.getBron();
        ArrayList zoekResultaten = new ArrayList(results);
        DataStore ds = null;
        try {
            ds = getDataStore(bron);
            if (ds != null) {
                FeatureCollection fc = null;
                FeatureReader reader = null;
                try {
                    if (ds instanceof WFS_1_0_0_DataStore) {
                        WFS_1_0_0_DataStore wfs100ds = (WFS_1_0_0_DataStore) ds;
                        WFSCapabilities wfscap = wfs100ds.getCapabilities();
                        FilterCapabilities filterCap = wfscap.getFilterCapabilities();
                        filterCap.addType(FilterCapabilities.FUNCTIONS);
                        wfscap.setFilterCapabilities(filterCap);
                    }
                    FeatureSource fs = ds.getFeatureSource(zc.getFeatureType());
                    FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
                    if (zc.getZoekVelden() == null) {
                        throw new Exception("Fout in zoekconfiguratie. Er zijn geen zoekvelden gedefineerd");
                    }
                    if (zc.getZoekVelden().size() != searchStrings.length) {
                        throw new Exception("Fout in zoekconfiguratie. Het aantal zoekvelden (" + zc.getZoekVelden().size() + ") is ongelijk aan het aantal meegegeven strings(" + searchStrings.length + ")");
                    }
                    Iterator it = zc.getZoekVelden().iterator();
                    List filters = new ArrayList();
                    Filter filter = null;
                    //omdat het and filter het niet doet kan je maar 1 filter meegegeven:
                    //for(int i=0; it.hasNext(); i++){
                    //de filterIndex geeft aan welk filter is meegezonden met het verzoek.
                    int filterIndex = -1;
                    ArrayList properties = new ArrayList();
                    for (int i = 0; it.hasNext() && filterIndex == -1; i++) {
                        ZoekAttribuut zoekVeld = (ZoekAttribuut) it.next();
                        if (ds instanceof WFS_1_0_0_DataStore) {
                            filters.add(ff.equal(ff.property(zoekVeld.getAttribuutnaam()), ff.literal(searchStrings[i]), false));
                        } else {
                            if (searchStrings[i].length() > 0) {
                                filters.add(ff.like(ff.property(zoekVeld.getAttribuutLocalnaam()), "*" + searchStrings[i] + "*"));
                                filterIndex = i;
                            }
                        }
                        //omdat het filter niet goed werkt moeten we met de hand controleren maar dan
                        //moeten we wel de bevraagde attributen ophalen
                        properties.add(zoekVeld.getAttribuutLocalnaam());
                    }
                    if (filters.size() == 1) {
                        filter = (Filter) filters.get(0);
                    } else {
                        filter = ff.and(filters);
                    }
                    DefaultQuery query;
                    if (filters.size() == 0) {
                        query = new DefaultQuery(zc.getFeatureType());
                    } else {
                        query = new DefaultQuery(zc.getFeatureType(), filter);
                    }
                    query.setMaxFeatures(maxResults.intValue());
                    //set de property namen die opgehaald moeten worden.
                    Iterator pit = zc.getResultaatVelden().iterator();
                    if (!pit.hasNext()) {
                        log.error("Geen resultaatvelden geconfigureerd voor zoekconfiguratie: " + zc.getNaam());
                        return null;
                    }
                    while (pit.hasNext()) {
                        ResultaatAttribuut pa = (ResultaatAttribuut) pit.next();
                        if (!properties.contains(pa.getAttribuutLocalnaam())) {
                            properties.add(pa.getAttribuutLocalnaam());
                        }
                    }
                    query.setPropertyNames(properties);
                    //maak reader aan.
                    DefaultFeatureResults fresults = (DefaultFeatureResults) fs.getFeatures(query);
                    reader = fresults.reader();
                    //fi=fc.iterator();
                    while (reader.hasNext()) {
                        Feature f = reader.next();
                        Iterator zit = zc.getZoekVelden().iterator();
                        boolean tonen = true;
                        for (int i = 0; zit.hasNext() && tonen; i++) {
                            ZoekAttribuut zak = (ZoekAttribuut) zit.next();
                            if (i == filterIndex) {//is al gechecked met het ophalen dus hoeft niet nog een keer gechecked te worden.
                            } else if (searchStrings[i] == null || searchStrings[i].length() == 0) {
                                //als searchstrings leeg is dan ook niet controleren.
                            } else {
                                if (f.getProperty(zak.getAttribuutLocalnaam()) == null) {
                                    tonen = false;
                                } else if (f.getProperty(zak.getAttribuutLocalnaam()).getValue() == null) {
                                    tonen = false;
                                } else if (!f.getProperty(zak.getAttribuutLocalnaam()).getValue().toString().matches(searchStrings[i])) {
                                    tonen = false;
                                }
                            }
                        }
                        if (tonen) {
                            ZoekResultaat p = new ZoekResultaat();
                            Iterator rit = zc.getResultaatVelden().iterator();
                            while (rit.hasNext()) {
                                ResultaatAttribuut ra = (ResultaatAttribuut) rit.next();
                                if (f.getProperty(ra.getAttribuutLocalnaam()) != null) {
                                    String value = null;
                                    if (f.getProperty(ra.getAttribuutLocalnaam()).getValue() != null) {
                                        value = f.getProperty(ra.getAttribuutLocalnaam()).getValue().toString();
                                    }
                                    ZoekResultaatAttribuut zra = new ZoekResultaatAttribuut(ra);
                                    zra.setWaarde(value);
                                    p.addAttribuut(zra);
                                    p.setZoekConfiguratie(zc);
                                } else {
                                    String attrTypes = "";
                                    Iterator pi = f.getProperties().iterator();
                                    while (pi.hasNext()) {
                                        Property pr = (Property) pi.next();
                                        attrTypes += pr.getType().getName().getLocalPart() + " ";
                                    }
                                    log.debug("Attribuut: " + ra.toString() + " niet gevonden. Mogelijke attributen: " + attrTypes);
                                }
                            }
                            if (f.getType().getGeometryDescriptor() != null && f.getDefaultGeometryProperty() != null && f.getDefaultGeometryProperty().getBounds() != null) {
                                p.setBbox(f.getDefaultGeometryProperty().getBounds());
                            }
                            if (!zoekResultaten.contains(p)) {
                                zoekResultaten.add(p);
                            }
                        }
                    }
                } catch (SchemaNotFoundException snfe) {
                    String typenames = "";
                    String[] tn = ds.getTypeNames();
                    for (int i = 0; i < tn.length; i++) {
                        if (typenames.length() != 0) {
                            typenames += "\n";
                        }
                        typenames += tn[i];
                    }
                    log.error("Feature niet bekend bij bron, mogelijke features: " + typenames, snfe);
                } catch (Exception e) {
                    log.error("Fout bij laden plannen: ", e);
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
            } else {
                log.error("Kan geen datastore maken van bron");
            }
        } catch (Exception ioe) {
            log.error("Fout bij laden van plannen: ", ioe);
        } finally {
            if (ds != null) {
                ds.dispose();
            }
        }
        return zoekResultaten;
    }
    public static List getZoekConfiguraties() {
        Object identity = null;
        List returnList=null;
        try {
            identity = MyEMFDatabase.createEntityManager(MyEMFDatabase.MAIN_EM);
            EntityManager em = MyEMFDatabase.getEntityManager(MyEMFDatabase.MAIN_EM);
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                returnList= em.createQuery("from ZoekConfiguratie").getResultList();
                tx.commit();
            } catch (Exception ex) {
                log.error("Exception occured" + (tx.isActive() ? ", rollback" : "tx not active"), ex);
                if (tx.isActive()) {
                    tx.rollback();
                }
            }

        } catch (Throwable e) {
            log.error("Exception occured in search: ", e);
        } finally {
            log.debug("Closing entity manager .....");
            MyEMFDatabase.closeEntityManager(identity, MyEMFDatabase.MAIN_EM);
        }
        return returnList;
    }

    /**
     * Maakt een datastore dmv de bron
     * @param b de Bron
     * @return een Datastore
     * @throws java.io.IOException
     */
    //jdbc:postgresql://localhost:5432/edamvolendam_gis
    public DataStore getDataStore(Bron b) throws IOException {
        if (b.getUrl() == null) {
            return null;
        }
        HashMap params = new HashMap();
        if (b.getUrl().toLowerCase().startsWith("jdbc:oracle:")) {
            //jdbc:oracle:thin:@b3p-demoserver:1521:ORCL
            int firstIndex;
            int lastIndex;
            firstIndex = b.getUrl().indexOf("@") + 1;
            lastIndex = b.getUrl().indexOf(":", firstIndex);
            String host = b.getUrl().substring(firstIndex, lastIndex);
            firstIndex = lastIndex + 1;
            lastIndex = b.getUrl().indexOf(":", firstIndex);
            String port = b.getUrl().substring(firstIndex, lastIndex);
            firstIndex = lastIndex + 1;
            lastIndex = b.getUrl().indexOf(".", firstIndex);
            String schema = null;
            if (lastIndex == -1) {
                lastIndex = b.getUrl().length();
            } else {
                schema = b.getUrl().substring(lastIndex + 1, b.getUrl().length());
            }
            String instance = b.getUrl().substring(firstIndex, lastIndex);
            params.put("host", host);
            params.put("port", port);
            if (schema != null) {
                params.put("schema", schema);
            }
            params.put("instance", instance);
            params.put("user", b.getGebruikersnaam());
            params.put("passwd", b.getWachtwoord());
            params.put("dbtype", "oracle");
            return (new OracleDataStoreFactory()).createDataStore(params);
        }
        if (b.getUrl().toLowerCase().startsWith("jdbc:")) {
            //jdbc:postgresql://localhost:5432/edamvolendam_gis
            int firstIndex;
            int lastIndex;
            firstIndex = b.getUrl().indexOf("//") + 2;
            lastIndex = b.getUrl().indexOf(":", firstIndex);
            String host = b.getUrl().substring(firstIndex, lastIndex);
            firstIndex = lastIndex + 1;
            lastIndex = b.getUrl().indexOf("/", firstIndex);
            String port = b.getUrl().substring(firstIndex, lastIndex);
            firstIndex = lastIndex + 1;
            String database = b.getUrl().substring(firstIndex, b.getUrl().length());
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
            if (b.getGebruikersnaam() != null) {
                params.put(PostgisDataStoreFactory.USER.key, b.getGebruikersnaam());
            }
            if (b.getWachtwoord() != null) {
                params.put(PostgisDataStoreFactory.PASSWD.key, b.getWachtwoord());
            }
        } else {
            String url = b.getUrl();
            if (b.getUrl().toLowerCase().indexOf("request=") == -1) {
                if (url.indexOf("?") > 0) {
                    url += "&";
                } else {
                    url += "?";
                }
                url += "request=GetCapabilities&service=WFS";
            }
            params.put(WFSDataStoreFactory.URL.key, url);
            if (b.getGebruikersnaam() != null) {
                params.put(WFSDataStoreFactory.USERNAME.key, b.getGebruikersnaam());
            }
            if (b.getWachtwoord() != null) {
                params.put(WFSDataStoreFactory.PASSWORD.key, b.getWachtwoord());
            }
            params.put(WFSDataStoreFactory.TIMEOUT.key, TIMEOUT);
        }
        return DataStoreFinder.getDataStore(params);
    }
}
