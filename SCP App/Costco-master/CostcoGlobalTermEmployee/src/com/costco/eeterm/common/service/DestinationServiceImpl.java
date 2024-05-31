package com.costco.eeterm.common.service;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.springframework.stereotype.Service;

import com.costco.eeterm.exceptions.DestinationNotFoundException;
import com.costco.eeterm.exceptions.DestinationReadingException;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

@Service
public class DestinationServiceImpl implements DestinationService {

    @Override
    public DestinationConfiguration getDestinationByName(String destName) 
            throws DestinationNotFoundException, DestinationReadingException {
        ConnectivityConfiguration configuration;
        try {
            configuration = getConnectivityConfiguration();
        } catch(NamingException e) {
            throw new DestinationReadingException("Error reading destination "+ destName, e);
        }
        DestinationConfiguration destConfiguration = configuration.getConfiguration(destName);
        if (destConfiguration == null) {
            throw new DestinationNotFoundException("Destination " + destName + " doesn't exist.");
        }
        return destConfiguration;
    }
    
    private ConnectivityConfiguration getConnectivityConfiguration() throws NamingException {
        Context ctx = new InitialContext();
        ConnectivityConfiguration configuration = 
                (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
        return configuration;
    }

}
