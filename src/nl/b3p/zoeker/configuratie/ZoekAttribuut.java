/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.zoeker.configuratie;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author Roy
 */
public class ZoekAttribuut extends Attribuut {

    public ZoekAttribuut() {
    }

    public ZoekAttribuut(Integer id, String naam, String attribuutnaam, String label, Integer type, Integer volgorde) {
        super(id, naam, attribuutnaam, label, type, volgorde);
    }

    public static ZoekAttribuut[] setToZoekVeldenArray(Set set) {
        ZoekAttribuut[] zoekattributen = new ZoekAttribuut[set.size()];
        Iterator it = set.iterator();
        for (int i = 0; it.hasNext(); i++) {
            zoekattributen[i] = (ZoekAttribuut) it.next();
        }
        return zoekattributen;
    }

    public boolean isFilterMogelijk() {
        if (this.getType() == null) {
            return true;
        }
        return this.getType() < 100;
    }
    
    static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());

    public static void main(String[] args) throws IOException, Exception {
        Bron b = new Bron(0, "buurten", "http://x5.b3p.nl/cgi-bin/mapserv_fwtools?map=/srv/maps/kaartenbalie.map&", null, null, 0);

        DataStore ds = b.toDatastore();

        String featureTypeName = "buurten_2006";
        String attributeName = "gm_naam";
        String sortAttributeName = "bu_naam";
        String geomAttributeName = "the_geom";
        List propNames = new ArrayList();
        propNames.add(attributeName);

        FeatureSource fs = ds.getFeatureSource(featureTypeName);


        Set<String> uniqueValues = new HashSet();
        Filter filter = null;
        DefaultQuery query = null;

        SortBy[] sortBy1 = new SortBy[]{ff.sort(sortAttributeName, SortOrder.ASCENDING)};
        SortBy[] sortBy2 = new SortBy[]{ff.sort(sortAttributeName, SortOrder.DESCENDING)};


//        21000,357000,283000,615000
        int minx = 21000;
        int maxx = 283000;
        int miny = 357000;
        int maxy = 615000;

// 459 gemeenten in 11646 buurten, maar gevonden:
//segmenten	max	aantal	loops	tijd
//      10	10	418	407	2:56
//      5	10	366	203	2:02
//      20	10	432	994	7:09
//      5	100	383	104	1:29
//      5	1000	387	53	1:46
//      10	1000	422	181	3:00
//      20	1000    434     673     6:42
//      1       10000   434     6       1:33
//      1       geen    434     6       1:40

        int aantalSegmenten = 10;
        int max = 100;

        int deltax = (maxx - minx) / aantalSegmenten;
        int deltay = (maxy - miny) / aantalSegmenten;


        int loopNum = 0;
        for (int xloop = 0; xloop < aantalSegmenten; xloop++) {
            int minxLoop = minx + xloop * deltax;
            int maxxLoop = minxLoop + deltax;

            for (int yloop = 0; yloop < aantalSegmenten; yloop++) {
                int minyLoop = miny + yloop * deltay;
                int maxyLoop = minyLoop + deltay;

try{
  //do what you want to do before sleeping
  Thread.currentThread().sleep(5000);//sleep for 1000 ms
  //do what you want to do after sleeptig
}
catch(InterruptedException ie){
//If this thread was intrrupted by another thread
}
                Filter geomFilter = null;
                if (aantalSegmenten > 0) {
                    CoordinateReferenceSystem crs = ds.getSchema(featureTypeName).getGeometryDescriptor().getCoordinateReferenceSystem();
                    ReferencedEnvelope bbox = new ReferencedEnvelope(minxLoop, maxxLoop, minyLoop, maxyLoop, crs);
                    geomFilter = ff.bbox(ff.property(geomAttributeName), bbox);
                }

                Set<String> uniqueValuesLoop = new HashSet();

                boolean found = false;
                do {
                    loopNum++;
                    found = false;
                    if (uniqueValuesLoop != null && uniqueValuesLoop.size() > 0) {
                        String[] values = uniqueValuesLoop.toArray(new String[uniqueValuesLoop.size()]);
                        filter = createAndNotEqualsFilter(attributeName, values);
                        query = new DefaultQuery(featureTypeName, createAndFilter(filter, geomFilter));
                    } else {
                        query = new DefaultQuery(featureTypeName, createAndFilter(null, geomFilter));
                    }
                    query.setMaxFeatures(max);
                    query.setPropertyNames(propNames);
                    if (loopNum%2 == 0) {
                        query.setSortBy(sortBy1);
                    } else {
                        query.setSortBy(sortBy2);
                   }

                    FeatureCollection fc = fs.getFeatures(query);
                    FeatureIterator fi = fc.features();
                    try {
                        while (fi.hasNext()) {
                            Feature f = (Feature) fi.next();
                            String v = (String) f.getProperty(attributeName).getValue();
                            System.out.println("gemeente  " + v);
                            if (!uniqueValuesLoop.contains(v)) {
                                found = true;
                                uniqueValuesLoop.add(v);
                                System.out.println("nieuwe gemeente  " + v);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("xxxxxx");
                        String mapserver4Hack = "msQueryByRect(): Search returned no results. No matching record(s) found.";
                        String message =  e.getMessage();
                        if (message!=null && message.contains(mapserver4Hack)) {
                            // mapserver 4 returns service exception when no hits, this is not compliant.
                        } else {
//                            found=true;
                            throw e;
                        }
                    }
                    System.out.println("-------");
                } while (found);
                uniqueValues.addAll(uniqueValuesLoop);
            }

            System.out.println("!!!!!!!! aantal: " + uniqueValues.size());
        }

        for (String uv : uniqueValues) {
            System.out.println(uv);
        }

        System.out.println("++++++++++++++ aantal: " + uniqueValues.size());
        System.out.println("++++++++++++++ loop num: " + loopNum);
    }

    public static Filter createAndNotEqualsFilter(String key, String[] values) {
        ArrayList<Filter> filters = new ArrayList();
        PropertyName pn = ff.property(key);
        for (int i = 0; i < values.length; i++) {
            String val = values[i];
            if (val.contains("'")) {
                val= val.replaceAll("'", "\\\\'");
            }
            filters.add(ff.equals(pn, ff.literal(val)));

        }
        if (filters.size() == 1) {
            return ff.not(filters.get(0));
        } else if (filters.size() > 1) {
            return ff.not(ff.or(filters));
        } else {
            return null;
        }

    }

    public static Filter createAndFilter(Filter f1, Filter f2) {
        ArrayList<Filter> filters = new ArrayList();
        if (f1 != null) {
            filters.add(f1);
        }
        if (f2 != null) {
            filters.add(f2);
        }
        if (filters.size() == 1) {
            return filters.get(0);
        } else if (filters.size() > 1) {
            return ff.and(filters);
        } else {
            return null;
        }
    }
}
