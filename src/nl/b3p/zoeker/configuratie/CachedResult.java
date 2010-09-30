package nl.b3p.zoeker.configuratie;

import java.util.Date;
import java.util.List;
import nl.b3p.zoeker.services.ZoekResultaat;

/**
 *
 * @author Chris
 */
class CachedResult {
    
    public static final long LIFECYCLE_CACHE = 21600000l; //6 uur
    public static final int MAX_LIFECYCLE_CACHE_REQUEST = 28; // 1 week

    private ZoekConfiguratie zc;
    private List<ZoekResultaat> resultList;
    private String[] searchStrings;
    private Integer maxResults;
    private long collectionTS;
    private int numOfRequests;

    CachedResult(ZoekConfiguratie zc, List<ZoekResultaat> resultList,
            String[] searchStrings, Integer maxResults) {
        this.zc = zc;
        this.resultList = resultList;
        this.searchStrings = searchStrings;
        this.maxResults = maxResults;
        this.collectionTS = (new Date()).getTime();
        this.numOfRequests = 1;
    }

    public boolean isExpired() {
        // if cache expired, clean up elsewhere
        long now = (new Date()).getTime();
        int calcRequests = Math.min(numOfRequests, MAX_LIFECYCLE_CACHE_REQUEST);
        if ((now - collectionTS) > (calcRequests * LIFECYCLE_CACHE)) {
            return true;
        }
        return false;
    }

    public List<ZoekResultaat> getCachedResultList(ZoekConfiguratie zc,
            String[] searchStrings, Integer maxResults) {

        if (zc.isResultListDynamic()) {
            // dynamic, so no cache!
            return null;
        }

        if (isExpired()) {
            return null;
        }

        // reset cache, if maxresults have changed, clean up elsewhere
        if (maxResults == null && this.maxResults != null) {
            return null;
        }
        if (this.maxResults == null && maxResults != null) {
            return null;
        }
        if (maxResults != null && this.maxResults != null && !this.maxResults.equals(maxResults)) {
            return null;
        }

        // reset cache, if wrong zc, clean up elsewhere
        if (!this.zc.getId().equals(zc.getId())) {
            return null;
        }

        // reset cache, if searchStrings have changed, clean up elsewhere
        if (searchStrings == null || this.searchStrings == null) {
            return null;
        }
        if (searchStrings.length != this.searchStrings.length) {
            return null;
        }
        for (int i = 0; i < searchStrings.length; i++) {
            if (!this.searchStrings[i].equals(searchStrings[i])) {
                return null;
            }
        }

        // finally return list
        numOfRequests++; // extend cache life
        return resultList;
    }
}
