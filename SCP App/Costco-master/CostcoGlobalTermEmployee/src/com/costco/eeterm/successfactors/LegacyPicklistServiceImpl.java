package com.costco.eeterm.successfactors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.olingo.odata2.api.ep.entry.EntryMetadata;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.costco.eeterm.odata.odatav2.ODataClientService;
import com.costco.eeterm.successfactors.pojo.PickList;

@Service
public class LegacyPicklistServiceImpl implements LegacyPicklistService {

	@Autowired
	ODataClientService oDataService;
	private static final String PICKLIST_TABLE = "Picklist";
	private static final String PICKLIST_ID = "id";
	private static final String PICKLIST_CODE = "externalCode";
	private static final String PICKLIST_LABEL = "label";
	private static final String PICKLIST_STATUS = "status";
	private static final String PICKLIST_LOCALE = "locale";
	private static final String PICKLISTOPTIONS = "picklistOptions";
	private static final String PICKLISTLABELS = "picklistLabels";
	private static final String PICKLIST_EN_GB = "en_GB";
	
	private static final Logger logger = LoggerFactory.getLogger(LegacyPicklistServiceImpl.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PickList> getLegacyPickListValues( String legacyPicklistId) {

		List<PickList> pickListValues = new ArrayList<PickList>();

		try {
			ODataEntry legacyPicklistEntry = oDataService.backgroundReadEntry(PICKLIST_TABLE, "'" + legacyPicklistId + "'",
					"picklistOptions,picklistOptions/picklistLabels", "picklistOptions/id,picklistOptions/externalCode,picklistOptions/status,picklistOptions/picklistLabels/label,picklistOptions/picklistLabels/locale");

			ODataFeed optionsFeed = (ODataFeed) legacyPicklistEntry.getProperties().get(PICKLISTOPTIONS);
			pickListValues = oDataFeedtoPickListValues(optionsFeed);
			

		} catch (IOException | ODataException | NamingException e) {
			logger.error("problem setting legacy picklist values", e);
		}


		return pickListValues;
	}

	private List<PickList> oDataFeedtoPickListValues(ODataFeed feed) {

		List<PickList> pickListDataList = new ArrayList<PickList>();

		if (feed != null) {
				for (ODataEntry picklistOptionEntry : feed.getEntries()) {
					PickList pickListData = new PickList();
					
					EntryMetadata entryMetadata = picklistOptionEntry.getMetadata();
					pickListData.setUri(entryMetadata.getUri());
					
					Map<String, Object> picklistOptionDetails = picklistOptionEntry.getProperties();
					Long longId = (Long) picklistOptionDetails.get(PICKLIST_ID);
					pickListData.setPickListId(String.valueOf(longId));
					pickListData.setStatus((String) picklistOptionDetails.get(PICKLIST_STATUS));
					pickListData.setExternalCode((String) picklistOptionDetails.get(PICKLIST_CODE));
					
					ODataFeed picklistLabelFeed = (ODataFeed) picklistOptionDetails.get(PICKLISTLABELS);
					String picklistLabel = null;
					for (ODataEntry picklistLabelEntry : picklistLabelFeed.getEntries()) {
						Map<String, Object> labels = picklistLabelEntry.getProperties();
						String locale = (String) labels.get(PICKLIST_LOCALE);
						if (locale.equals(PICKLIST_EN_GB)) {
							picklistLabel = (String) labels.get(PICKLIST_LABEL);
							break;
						}
					}
					pickListData.setLabel(picklistLabel);
					pickListDataList.add(pickListData);
				}
			}
		return pickListDataList;
	}

}
