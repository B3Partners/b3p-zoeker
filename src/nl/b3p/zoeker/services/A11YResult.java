/*
 * Copyright (C) 2012 b3partners
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.zoeker.services;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Boy de Wit
 */
public class A11YResult {
    
    private String startWkt;
    private Boolean hasNextStep = false;
    private String appCode;
    private Integer searchConfigId;
    private Map resultMap = new HashMap();
    
    public A11YResult() {        
    }
    
    public A11YResult(Map resultMap) {
        this.resultMap = resultMap;
    }
    
    public A11YResult(String startWkt, Map resultMap) {
        this.startWkt = startWkt;
        this.resultMap = resultMap;
    }
    
    public A11YResult(String startWkt, String appCode, Map resultMap) {
        this.startWkt = startWkt;
        this.appCode = appCode;
        this.resultMap = resultMap;
    }  
    
    public A11YResult(String startWkt, Boolean hasNextStep, String appCode, Map resultMap) {
        this.startWkt = startWkt;
        this.hasNextStep = hasNextStep;
        this.appCode = appCode;
        this.resultMap = resultMap;
    } 
    
    public A11YResult(String startWkt, Boolean hasNextStep, String appCode, 
            Integer searchConfigId, Map resultMap) {
        
        this.startWkt = startWkt;
        this.hasNextStep = hasNextStep;
        this.appCode = appCode;
        this.searchConfigId = searchConfigId;
        this.resultMap = resultMap;
    } 
    
    public void addResult(String key, String value) {
        if (resultMap != null) {
            resultMap.put(key, value);
        }
    }

    public String getStartWkt() {
        return startWkt;
    }

    public void setStartWkt(String startWkt) {
        this.startWkt = startWkt;
    }

    public Boolean getHasNextStep() {
        return hasNextStep;
    }

    public void setHasNextStep(Boolean hasNextStep) {
        this.hasNextStep = hasNextStep;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public Integer getSearchConfigId() {
        return searchConfigId;
    }

    public void setSearchConfigId(Integer searchConfigId) {
        this.searchConfigId = searchConfigId;
    }

    public Map getResultMap() {
        return resultMap;
    }

    public void setResultMap(Map resultMap) {
        this.resultMap = resultMap;
    }
}