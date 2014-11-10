/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.zoeker.services;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKTReader;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import nl.b3p.zoeker.configuratie.*;
import nl.b3p.zoeker.hibernate.MyEMFDatabase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.SchemaNotFoundException;
import org.geotools.data.wfs.v1_0_0.WFSCapabilities;
import org.geotools.data.wfs.v1_0_0.WFS_1_0_0_DataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.FilterCapabilities;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.sort.SortBy;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author Roy
 */
public class Zoeker {

    private static final int defaultMaxResults = 1000;
    private static final Log log = LogFactory.getLog(Zoeker.class);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", new Locale("NL"));

    public List<ZoekResultaat> zoek(Integer[] zoekConfiguratieIds, String searchStrings[], Integer maxResults) {
        List<ZoekResultaat> results = new ArrayList();

        Object identity = null;
        try {
            identity = MyEMFDatabase.createEntityManager(MyEMFDatabase.MAIN_EM);
            EntityManager em = MyEMFDatabase.getEntityManager(MyEMFDatabase.MAIN_EM);
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                String queryString = "from ZoekConfiguratie z where z.id IN (";
                for (int i = 0; i < zoekConfiguratieIds.length; i++) {
                    if (i != 0) {
                        queryString += ",";
                    }
                    queryString += zoekConfiguratieIds[i];
                }
                queryString += ") order by z.parentBron.volgorde";
                List zoekconfiguraties = em.createQuery(queryString).getResultList();
                for (int i = 0; i < zoekconfiguraties.size(); i++) {
                    ZoekConfiguratie zc = (ZoekConfiguratie) zoekconfiguraties.get(i);
                    results = zoekMetConfiguratie(zc, cleanStringArray(searchStrings), maxResults, results);

                    /* XY zoekresultaat toevoegen indien niet null */
                    ZoekResultaat zr = getXYZoekResultaat(zc, searchStrings);
                    if (zr != null) {
                        if (results == null) {
                            results = new ArrayList();
                        }

                        results.add(zr);
                    }

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

    private String[] cleanStringArray(String[] sa) {
        if (sa == null) {
            return null;
        }
        for (int i = 0; i < sa.length; i++) {
            sa[i] = sa[i].trim();
        }
        return sa;
    }

    // staan alle resultaatvelden in de zoekvelden?
    // zijn alle zoekvelden gelijk-aan-velden?
    // ja en ja dan direct ZoekResultaat terugsturen.
    protected ZoekResultaat resultaatInVraag(ZoekConfiguratie zc, String[] searchStrings) {
        Set<ResultaatAttribuut> rvs = zc.getResultaatVelden();
        Set<ZoekAttribuut> zvs = zc.getZoekVelden();
        Integer gelijkAanType = new Integer(Attribuut.GELIJK_AAN_TYPE);
        if (rvs != null && !rvs.isEmpty() && zvs != null && !zvs.isEmpty()
                && zvs.size() == searchStrings.length) {
            boolean allRvsAvailable = true;
            ZoekResultaat p = new ZoekResultaat();
            p.setZoekConfiguratie(zc);
            for (ResultaatAttribuut rv : rvs) {

                boolean foundRv = false;
                int loop = 0;
                for (ZoekAttribuut zv : zvs) {
                    if (rv.getAttribuutnaam().equals(zv.getAttribuutnaam())
                            && gelijkAanType.equals(zv.getType())) {

                        ZoekResultaatAttribuut zra = new ZoekResultaatAttribuut(rv);
                        zra.setWaarde(searchStrings[loop]);
                        p.addAttribuut(zra);

                        foundRv = true;
                        break;
                    }
                    loop++;
                }
                if (!foundRv) {
                    allRvsAvailable = false;
                }
            }
            if (allRvsAvailable) {
                return p;
            }
        }
        return null;
    }

    public List<ZoekResultaat> zoekMetConfiguratie(ZoekConfiguratie zc, String[] searchStrings, Integer maxResults, List<ZoekResultaat> results) {
        return zoekMetConfiguratie(zc, searchStrings, maxResults, results, false, 0, 0);
    }

    public List<ZoekResultaat> zoekMetConfiguratie(ZoekConfiguratie zc, String[] searchStrings, Integer maxResults, List<ZoekResultaat> results, boolean usePagination, int startIndex, int limit) {
        return zoekMetConfiguratie(zc, searchStrings, maxResults, results, false, 0, 0, null);
    }

    /**
     * Zoek moet configuratie (search Strings moet gelijk zijn aan aantal
     * zoekvelden in de ZoekConfiguratie:
     *
     * @param zc: ZoekConfiguratie waarmee gezocht moet worden
     * @param searchStrings Een array van gezochte waarden. (moet gelijk zijn
     * aan het aantal geconfigureerde zoekvelden en ook in die volgorde staan)
     * @param maxResults: Het maximaal aantal resultaten dat getoond moeten
     * worden.
     * @param results: De al gevonden resultaten (de nieuwe resultaten worden
     * hier aan toegevoegd.
     */
    public List<ZoekResultaat> zoekMetConfiguratie(ZoekConfiguratie zc, String[] searchStrings, Integer maxResults, List<ZoekResultaat> results, boolean usePagination, int startIndex, int limit, A11YResult currentA11YResult) {
        if (maxResults == null || maxResults.intValue() == 0) {
            maxResults = defaultMaxResults;
        }
        if (zc == null || searchStrings == null) {
            return results;
        }

        boolean calculateDistance = false;
        String locationWkt = null;

        List<ZoekResultaat> zoekResultaten = new ArrayList(results);
        // Controleer of de zoekvelden al voldoende info bevatten om zonder
        // nieuwe zoekactie de resultaatvelden te vullen
        ZoekResultaat zr = resultaatInVraag(zc, searchStrings);
        if (zr != null) {
            zoekResultaten.add(zr);
            log.debug("Result found in request, no new search action!");
            return zoekResultaten;
        }

        if (!zc.isResultListDynamic()) {
            //zoekresultaten worden uit cache gehaald.
            List<ZoekResultaat> cachedResultaten = ZoekConfiguratie.getCachedResultList(zc, searchStrings, maxResults);
            if (cachedResultaten != null && !cachedResultaten.isEmpty()) {
                zoekResultaten.addAll(cachedResultaten);
                Collections.sort(zoekResultaten);
                log.debug("Cached list used!");
                return zoekResultaten;
            }
            log.debug("Cached list not found!");
        }

        Bron bron = zc.getBron();
        DataStore ds = null;
        try {
            ds = getDataStore(bron);
            if (ds != null) {
                FeatureCollection fc = null;
                //FeatureReader reader = null;
                FeatureIterator fi = null;
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
                    //FilterFactory ff = CommonFactoryFinder.getFilterFactory(GeoTools.getDefaultHints());
                    if (zc.getZoekVelden() == null) {
                        throw new Exception("Fout in zoekconfiguratie. Er zijn geen zoekvelden gedefineerd");
                    }
                    if (zc.getZoekVelden().size() != searchStrings.length) {
                        throw new Exception("Fout in zoekconfiguratie. Het aantal zoekvelden (" + zc.getZoekVelden().size() + ") is ongelijk aan het aantal meegegeven strings(" + searchStrings.length + ")");
                    }
                    Iterator it = zc.getZoekVelden().iterator();
                    List filters = new ArrayList();

                    //maak de filters                    
                    try {
                        fs.getSchema();
                    } catch (NullPointerException npe) {
                        log.error("Kan het schema voor de zoekconfiguratie niet ophalen", npe);
                        throw npe;
                    }
                    ArrayList<String> properties = new ArrayList<String>();
                    for (int i = 0; it.hasNext(); i++) {
                        ZoekAttribuut zoekVeld = (ZoekAttribuut) it.next();
                        Filter filter = createFilter(ZoekAttribuut.setToZoekVeldenArray(zc.getZoekVelden()), searchStrings, i, ds, ff, fs.getSchema());
                        if (filter != null) {
                            filters.add(filter);
                        }

                        if (zoekVeld.getType() == Attribuut.LOCATIE_GEOM__TYPE) {
                            calculateDistance = true;
                            locationWkt = searchStrings[i];

                            /* Indien er een start locatie op de sessie is gezet deze gebruiken */
                            String startLocatie = getStartLocationFromSession();
                            if (startLocatie != null) {
                                locationWkt = startLocatie;
                            }

                            if (currentA11YResult != null) {
                                locationWkt = currentA11YResult.getStartWkt();
                            }
                        }
                    }
                    //maak een and filter of een single filter aan de hand van het aantal filters
                    Filter filter = null;
                    if (filters.size() == 1) {
                        filter = (Filter) filters.get(0);
                    } else {
                        filter = ff.and(filters);
                    }
                    //maak de query met de maxResults
                    DefaultQuery query;
                    if (filters.isEmpty()) {
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
                    //if log is in debug then check if all properties exists
                    if (log.isDebugEnabled()) {
                        FeatureType schema = fs.getSchema();
                        for (String prop : properties) {
                            if (prop != null && schema != null) {
                                PropertyDescriptor pd = schema.getDescriptor(prop);
                                if (pd == null) {
                                    log.debug("The property: '" + prop + "' that is configured "
                                            + "in the 'zoeker' is not available in the feature: " + zc.getFeatureType());
                                }
                            }

                        }
                    }

                    query.setPropertyNames(properties);

                    /* Pagination for FeatureCollection */
                    final FeatureSource fs2 = fs;

                    Integer count = null;
                    if (!(ds instanceof WFS_1_0_0_DataStore)) {
                        count = fs2.getCount(query);
                    }

                    if (count != null && count < limit) {
                        limit = count;
                    }

                    boolean startIndexSupported = fs.getQueryCapabilities().isOffsetSupported();
                    if (usePagination && startIndexSupported) {
                        query.setSortBy(SortBy.UNSORTED);
                        query.setStartIndex(startIndex);
                        query.setMaxFeatures(Math.min(limit + (startIndexSupported ? 0 : startIndex), maxResults));
                    }

                    //Haal de featureCollection met de query op.
                    fc = fs.getFeatures(query);
                    
                    //Maak de FeatureIterator aan (hier wordt het daad werkelijke verzoek gedaan.
                    fi = fc.features();
                    
                    //doorloop de features en maak de resultaten.
                    while (fi.hasNext()) {
                        Feature f = null;
                        try {
                            f = fi.next();
                        } catch (Exception e) {
                            log.error("Error getting next feature, probably Oracle stumbling over null geometry: " + e.getLocalizedMessage());
                            continue;
                        }
                        ZoekResultaat p = new ZoekResultaat();
                        Iterator rit = zc.getResultaatVelden().iterator();
                        while (rit.hasNext()) {
                            ResultaatAttribuut ra = (ResultaatAttribuut) rit.next();
                            if (f.getProperty(ra.getAttribuutLocalnaam()) != null) {
                                Object value = f.getProperty(ra.getAttribuutLocalnaam()).getValue();
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
                        } else {
                            log.debug("Can't set Bbox for result. No bounds set for feature by the server. And no Geometry given or configured as result in the search configuration");
                        }

                        /* Indien zoekveld type 110. Afstand berekenen en toevoegen */
                        if (calculateDistance && locationWkt != null && !locationWkt.equals("")) {
                            Geometry locationGeom = createGeomFromWkt(locationWkt);

                            if (f.getType().getGeometryDescriptor() != null) {
                                Geometry resultGeom = (Geometry) f.getDefaultGeometryProperty().getValue();

                                double distance = calcDistance(locationGeom, resultGeom);

                                p.addAttribuut(createAfstandResultaatAttribuut(distance, zc));
                                p.setZoekConfiguratie(zc);
                            }
                        }

                        p.setCount(count);

                        /* Niet ArrayList.contains() gebruiken omdat daar alle attributen worden gecontrolleerd.
                         * we willen alleen de id's controleren.
                         */
                        if (zc.isResultListDynamic()) {
                            zoekResultaten.add(p);
                        } else { // cache bekijken
                            boolean contains = containsResult(zoekResultaten, p);

                            if (!contains) {
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
                    log.error("Feature " + zc.getFeatureType() + " niet bekend bij bron, mogelijke features: " + typenames, snfe);
                } catch (Exception e) {
                    log.error("Fout tijdens het zoeken met een configuratie: ", e);
                } finally {
                    if (fc != null && fi != null) {
                        fi.close();
                    }

                    if (ds != null) {
                        ds.dispose();
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

        Collections.sort(zoekResultaten);
        if (!zc.isResultListDynamic()) {
            ZoekConfiguratie.setCachedResultList(zc, zoekResultaten, searchStrings, maxResults);
            log.debug("Cache filled.");
        }

        return zoekResultaten;
    }

    private String getStartLocationFromSession() {
        String locationWkt = null;

        WebContext ctx = WebContextFactory.get();
        if (ctx != null) {
            HttpServletRequest request = ctx.getHttpServletRequest();
            HttpSession session = request.getSession(true);
            A11YResult a11yResult = (A11YResult) session.getAttribute("a11yResult");

            if (a11yResult != null) {
                locationWkt = a11yResult.getStartWkt();

                log.debug("Zoeker startlocatie: " + locationWkt);
            }
        }

        return locationWkt;
    }

    private Integer getSearchRadiusFromSession() {
        Integer radius = null;

        WebContext ctx = WebContextFactory.get();
        if (ctx != null) {
            HttpServletRequest request = ctx.getHttpServletRequest();
            HttpSession session = request.getSession(true);

            radius = (Integer) session.getAttribute("defaultSearchRadius");
        }

        return radius;
    }

    private Geometry createGeomFromWkt(String wkt) {
        WKTReader wktreader = new WKTReader(new GeometryFactory(new PrecisionModel(), 28992));
        Geometry geom = null;
        try {
            geom = wktreader.read(wkt);
        } catch (Exception e) {
            log.error("Fout bij parsen wkt geometry", e);
        }

        return geom;
    }

    private ZoekResultaatAttribuut createAfstandResultaatAttribuut(double distance, ZoekConfiguratie zc) {
        ResultaatAttribuut afstandAttr = new ResultaatAttribuut();
        afstandAttr.setLabel("afstand");
        afstandAttr.setAttribuutnaam("afstand");
        afstandAttr.setType(Attribuut.TOON_TYPE);
        afstandAttr.setVolgorde(9999);

        distance /= 1000;

        DecimalFormat twoDForm = new DecimalFormat("#.##");
        String waarde = twoDForm.format(distance);

        if (waarde.contains(",")) {
            waarde = waarde.replace(",", ".");
        }

        ZoekResultaatAttribuut zra = new ZoekResultaatAttribuut(afstandAttr);
        zra.setWaarde(waarde + " km");

        return zra;
    }

    private double calcDistance(Geometry geom1, Geometry geometry2) {
        double distance = -1;

        if (geom1 == null || geometry2 == null) {
            return distance;
        }

        try {
            CoordinateReferenceSystem crs = CRS.decode("EPSG:28992");
            Coordinate start = new Coordinate(geom1.getCentroid().getX(), geom1.getCentroid().getY());
            Coordinate end = new Coordinate(geometry2.getCentroid().getX(), geometry2.getCentroid().getY());

            if (start != null && end != null) {
                distance = JTS.orthodromicDistance(start, end, crs);
            }
        } catch (Exception ex) {
            log.error("Error calculatin distance: ", ex);
        }

        return distance;
    }

    public static List getZoekConfiguraties() {
        Object identity = null;
        List returnList = null;

        try {
            identity = MyEMFDatabase.createEntityManager(MyEMFDatabase.MAIN_EM);
            EntityManager em = MyEMFDatabase.getEntityManager(MyEMFDatabase.MAIN_EM);
            EntityTransaction tx = em.getTransaction();
            tx.begin();

            try {
                returnList = em.createQuery("from ZoekConfiguratie z"
                        + " LEFT JOIN FETCH z.zoekVelden"
                        + " LEFT JOIN FETCH z.resultaatVelden"
                        + " LEFT JOIN FETCH z.parentBron ORDER BY z.naam DESC").getResultList();

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

        /* Eerst set maken om dubbele eruit te halen vanwege left join many to one 
         * 
         * TODO: Bij veel records in LEFT table kan snel uit de klauwen lopen. Dit 
         * resultaat cachen ? */

        return new ArrayList(new HashSet(returnList));
    }

    protected boolean containsResult(List<ZoekResultaat> zoekResultaten, ZoekResultaat p) {
        for (ZoekResultaat zoekresultaat : zoekResultaten) {
            if (zoekresultaat.getId() != null && p.getId() != null) {
                if (zoekresultaat.getId().equals(p.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Maakt een datastore dmv de bron
     *
     * @param b de Bron
     * @return een Datastore
     * @throws java.io.IOException
     * @deprecated: use b.toDatastore();
     */
    public static DataStore getDataStore(Bron b) throws IOException, Exception {
        return b.toDatastore();
    }

    /**
     * Maakt het filter voor het zoekveld met als value het zoek criterium. Geef
     * de alle zoekvelden mee en alle ingevulde strings omdat sommige zoekvelden
     * afhankelijk kunnen zijn van elkaar.
     */
    private Filter createFilter(ZoekAttribuut[] zoekVelden, String[] searchStrings, int index, DataStore ds, FilterFactory2 ff, FeatureType ft) throws Exception {
        String searchString = searchStrings[index];
        Integer searchRadius = null;
        String startLocatie = null;

        ZoekAttribuut zoekVeld = zoekVelden[index];
        if (zoekVeld.getType() == Attribuut.LOCATIE_GEOM__TYPE) {
            startLocatie = getStartLocationFromSession();
            searchRadius = getSearchRadiusFromSession();

            if (startLocatie != null) {
                searchString = startLocatie;
            }
        }

        if (searchString == null || searchString.length() == 0) {
            return null;
        }

        Filter filter = null;
        if (zoekVeld.getType() == Attribuut.GEOMETRY_TYPE
                || zoekVeld.getType() == Attribuut.LOCATIE_GEOM__TYPE) {

            WKTReader wktreader = new WKTReader(new GeometryFactory(new PrecisionModel(), 28992));
            try {
                Geometry geom = wktreader.read(searchString);
                Double straal = null;

                //zijn er nog zoekAttributen ingevuld die betrekking hebben op de geometry (zoals straal)
                for (int i = 0; i < zoekVelden.length; i++) {
                    //skip voor dit zoekveld
                    if (i == index) {
                        continue;
                    }
                    //bij straal maak een buffer.
                    if (zoekVelden[i].getType() == Attribuut.STRAAL_TYPE && searchStrings[i] != null && searchStrings[i].length() > 0) {
                        try {
                            straal = Double.parseDouble(searchStrings[i]);
                            geom = geom.buffer(straal);
                        } catch (NumberFormatException nfe) {
                            log.error("Ingevulde zoekopdracht " + zoekVelden[i].getNaam() + "moet een nummer zijn", nfe);
                        }
                    }
                }
                /* TODO: Indien de beheerder in de zoekingang geen straalveld heeft gemaakt maar wel
                 * een startlocatie zoekveld dan de standaard straal gebruiken uit de beheeromgeving */
                if (straal != null && straal > 0) {
                    filter = ff.within(ff.property(zoekVeld.getAttribuutnaam()), ff.literal(geom));
                }

                if (straal == null && searchRadius != null && searchRadius > 0
                        && startLocatie != null) {

                    geom = geom.buffer(searchRadius);
                    filter = ff.within(ff.property(zoekVeld.getAttribuutnaam()), ff.literal(geom));

                    log.debug("Using the default search radius of " + searchRadius);
                }

            } catch (Exception e) {
                log.error("Fout bij parsen wkt geometry", e);
            }
        } else if (zoekVeld.getType().intValue() == Attribuut.GROTER_DAN_TYPE) {
            filter = ff.greaterOrEqual(ff.property(zoekVeld.getAttribuutnaam()), ff.literal(searchString));
        } else if (zoekVeld.getType().intValue() == Attribuut.KLEINER_DAN_TYPE) {
            filter = ff.lessOrEqual(ff.property(zoekVeld.getAttribuutnaam()), ff.literal(searchString));
        } else if (zoekVeld.getType().intValue() == Attribuut.GROTER_DAN_DATUM_TYPE) {
            filter = ff.greaterOrEqual(ff.property(zoekVeld.getAttribuutnaam()), ff.literal(searchString));
        } else if (zoekVeld.getType().intValue() == Attribuut.KLEINER_DAN_DATUM_TYPE) {
            filter = ff.lessOrEqual(ff.property(zoekVeld.getAttribuutnaam()), ff.literal(searchString));
        } else if (zoekVeld.getType().intValue() == Attribuut.GELIJK_AAN_TYPE) {
            filter = ff.equals(ff.property(zoekVeld.getAttribuutnaam()), ff.literal(searchString));
        } else if (zoekVeld.isFilterMogelijk()) {
            String[] orStrings = searchString.split("\\|");
            List<Filter> orFilters = new ArrayList<Filter>();
            for (int i = 0; i < orStrings.length; i++) {
                Filter f = null;
                String sString = orStrings[i];
                if (ds instanceof WFS_1_0_0_DataStore) {
                    if (ft != null && zoekVeld != null && propertyIsNumber(ft.getDescriptor(zoekVeld.getAttribuutnaam()))) {
                        f = ff.equals(ff.property(zoekVeld.getAttribuutnaam()), ff.literal(sString));
                    } else {
                        f = ff.like(ff.property(zoekVeld.getAttribuutnaam()), sString);
                    }
                } else {
                    if (sString.length() > 0) {
                        if (propertyIsNumber(ft.getDescriptor(zoekVeld.getAttribuutnaam()))) {
                            f = ff.equals(ff.property(zoekVeld.getAttribuutnaam()), ff.literal(sString));
                        } else {
                            f = ff.like(ff.property(zoekVeld.getAttribuutnaam()), sString, "*", "?", "\\", false);
                        }
                    }
                }
                orFilters.add(f);
            }
            if (orFilters.size() == 1) {
                filter = orFilters.get(0);
            } else {
                filter = ff.or(orFilters);
            }
        }

        return filter;
    }

    private boolean propertyIsNumber(PropertyDescriptor descriptor) {
        if (descriptor == null || descriptor.getType() == null || descriptor.getType().getBinding() == null) {
            return false;
        }
        if (descriptor.getType().getBinding() == Integer.class || descriptor.getType().getBinding() == Double.class || descriptor.getType().getBinding() == BigInteger.class) {
            return true;
        }
        return false;
    }

    private ZoekResultaat getXYZoekResultaat(ZoekConfiguratie zc, String[] searchStrings) {
        /* Als er XY coordinaten als zoekstring zijn ingevuld deze als
         * POINT geom toevoegen aan zoek resultaten */
        try {
            WKTReader wktreader = new WKTReader(new GeometryFactory(new PrecisionModel(), 28992));

            if (searchStrings != null && searchStrings.length > 0) {
                String[] coords = searchStrings[searchStrings.length - 1].split(",");

                if (coords != null && coords.length == 2) {
                    String wkt = "POINT(" + coords[0] + " " + coords[1] + ")";
                    Geometry geom = wktreader.read(wkt);

                    if (geom != null) {
                        ResultaatAttribuut raToon = new ResultaatAttribuut();

                        ZoekResultaatAttribuut zra = new ZoekResultaatAttribuut(raToon);
                        zra.setType(Attribuut.ALLEEN_TOON_TYPE);
                        String waarde = "Zoom naar (" + coords[0] + " " + coords[1] + ")";
                        zra.setWaarde(waarde);

                        ZoekResultaat zrGeom = new ZoekResultaat();
                        zrGeom.addAttribuut(zra);
                        zrGeom.setZoekConfiguratie(zc);

                        int bufferSize = 50;

                        Envelope env = geom.getEnvelopeInternal();
                        zrGeom.setMinx(env.getMinX() - bufferSize);
                        zrGeom.setMiny(env.getMinY() + bufferSize);
                        zrGeom.setMaxx(env.getMaxX() + bufferSize);
                        zrGeom.setMaxy(env.getMaxY() - bufferSize);

                        return zrGeom;
                    }
                }
            }

        } catch (Exception e) {
            log.error("Fout tijdens omzetten XY zoekopdracht naar een zoekresultaat: ", e);
        }

        return null;
    }

    public ZoekConfiguratie getZoekConfiguratie(Integer id) {
        Object identity = null;
        ZoekConfiguratie zc = null;
        try {
            identity = MyEMFDatabase.createEntityManager(MyEMFDatabase.MAIN_EM);
            EntityManager em = MyEMFDatabase.getEntityManager(MyEMFDatabase.MAIN_EM);
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                zc = em.find(ZoekConfiguratie.class, id);
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
        return zc;
    }
}
