package com.costco.eeterm.successfactors;

import java.util.List;

import com.costco.eeterm.successfactors.pojo.PickList;
/**
 * This interface provides a method to efficiently read picklists configured in EC Picklist entity
 */
public interface LegacyPicklistService {
    /**
     * Retrieve picklist defined in EC legacy picklist entity.
     *
     * @return requests <tt>List</tt> of picklist
     * @param legacyPicklistId Id of the legacy picklist
     */
	List<PickList> getLegacyPickListValues(String legacyPicklistId);

}
