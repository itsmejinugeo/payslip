package com.costco.eeterm.successfactors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.costco.eeterm.exceptions.ErrorGettingAddressPicklistException;
import com.costco.eeterm.exceptions.ErrorGettingCountryListException;
import com.costco.eeterm.odata.odatav2.ODataClientService;
import com.costco.eeterm.successfactors.pojo.AddressPicklist;
import com.costco.eeterm.successfactors.pojo.Country;
import com.costco.eeterm.successfactors.pojo.PickList;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * {@inheritDoc}
 */
@Service
public class AddressPicklistServiceImpl implements AddressPicklistService {
	
	Map<String, List<PickList>> cachedStatePicklist = null;

	// EC Picklist ID for State/Province
	private static final String AUS_STATE_ID = "STATE_AUS";
	private static final String CAN_STATE_ID = "PROVINCE_CAN";
	private static final String USA_STATE_ID = "STATE_USA";
	
	private static final List<String> availableStateList = Arrays.asList(AUS_STATE_ID,CAN_STATE_ID,USA_STATE_ID);
	
	
	private List<Country> cachedCountryPicklist = null;
	private static final String FOCOUNTRY_TABLE = "Territory";
	private static final String FOCOUNTRY_CODE = "territoryCode";
	private static final String FOCOUNTRY_NAME = "territoryName";

	// EC Country available for address change
	private static final List<String> availableCountryList = Arrays.asList("AUS", "CAN", "USA");
	
	AddressPicklist cachedPickList = null;
	
	@Autowired
	ODataClientService oDataService;
	
	@Autowired
	LegacyPicklistService pickListService;

	/**
	  * {@inheritDoc}
	  */
	@Override
	public AddressPicklist getAddressPicklist() throws ErrorGettingAddressPicklistException {
		if (cachedPickList == null) {
		    AddressPicklist addressPicklist = new AddressPicklist();
		    try {
    			List<Country> countryPickListValues = getCountryList();
    			Map<String,List<PickList>> statePickListValues = getStateList();
    			for (Country entryCountry : countryPickListValues) {
    				String countryCode = entryCountry.getCode();
        		    List<PickList> stateList = 
        		            statePickListValues.entrySet().stream()
        		                .filter(map -> map.getKey().equals(countryCode))
        		                .map(map -> map.getValue())
        		                .findAny()
        		                .get();
        			entryCountry.setStateList(stateList);
    			}
    			addressPicklist.setAddressPicklist(countryPickListValues);
    			cachedPickList = addressPicklist;
		    } catch (ErrorGettingCountryListException e) {
		        throw new ErrorGettingAddressPicklistException("Error getting country list from SF.", e);
		    }
		}
		return cachedPickList;
	}

	
	/**
	  * {@inheritDoc}
	  */
	@Override
	public void clearAllCachePicklist() {
		cachedPickList = null;
		clearCacheStatePicklist();
		clearCacheCountryPicklist();
	}


	private Map<String, List<PickList>> getStateList() {
		if (cachedStatePicklist == null) {
			Map<String, List<PickList>> availableStatePicklist = new HashMap<String, List<PickList>>();
			for(String state_id : availableStateList){
				List<PickList> statePicklist = pickListService.getLegacyPickListValues(state_id);	
				String country = state_id.substring(state_id.length() - 3);
				availableStatePicklist.put(country, statePicklist);
			}
			 
			cachedStatePicklist = availableStatePicklist;
		}
		return cachedStatePicklist;
	}


	private void clearCacheStatePicklist() {
		cachedStatePicklist = null;

	}


	private List<Country> getCountryList() throws ErrorGettingCountryListException {
	    try {
            ODataFeed oDataFeed =oDataService.backgroundReadFeed(
                    FOCOUNTRY_TABLE, null, filterCountry(availableCountryList), null);
            List<Country> countryList = oDataEntrytoCountry(oDataFeed);
            Collections.sort(countryList);
            return countryList;
        } catch (NamingException| IOException | ODataException e) {
            throw new ErrorGettingCountryListException(e);
        }
	}

	private List<Country> oDataEntrytoCountry(ODataFeed feed) {
		if (cachedCountryPicklist == null) {
		List<Country> countryDataList = new ArrayList<Country>();
		
			if (feed != null) {
				for (ODataEntry entry : feed.getEntries()) {
					
					Country countryData = new Country();
					Map<String, Object> entryDetails = entry.getProperties();
					countryData.setCode((String) entryDetails.get(FOCOUNTRY_CODE));
					countryData.setCountryName((String) entryDetails.get(FOCOUNTRY_NAME));
					countryDataList.add(countryData);

				}
			}
			
			cachedCountryPicklist = countryDataList;
	   }
		return cachedCountryPicklist;
	}
	

	private String filterCountry(List<String> countryList) {
		String filter = "";
		for(String country: countryList){
			if(filter.isEmpty()){
				filter = FOCOUNTRY_CODE + " eq '" + country + "'";
			} else{
				filter = filter + " or " + FOCOUNTRY_CODE + " eq '" + country + "'";
			}
		}
		return filter;
	}


	private void clearCacheCountryPicklist() {
		cachedCountryPicklist = null;
	}
}
