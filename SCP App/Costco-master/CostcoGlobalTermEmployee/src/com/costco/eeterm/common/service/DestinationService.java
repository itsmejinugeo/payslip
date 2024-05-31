package com.costco.eeterm.common.service;

import com.costco.eeterm.exceptions.DestinationNotFoundException;
import com.costco.eeterm.exceptions.DestinationReadingException;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

public interface DestinationService {

    DestinationConfiguration getDestinationByName(String destName) 
            throws DestinationNotFoundException, DestinationReadingException;
    
}