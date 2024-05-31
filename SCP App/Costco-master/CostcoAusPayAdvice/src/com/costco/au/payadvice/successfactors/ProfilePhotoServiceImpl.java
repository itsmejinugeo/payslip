package com.costco.au.payadvice.successfactors;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.costco.au.payadvice.successfactors.pojo.ProfilePhotoDetails;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
@Service
public class ProfilePhotoServiceImpl implements ProfilePhotoService {
	private static final Logger logger = LoggerFactory.getLogger(ProfilePhotoServiceImpl.class);
	private ProfilePhotoDetails profilePhotoDetails = new ProfilePhotoDetails();

	@Override
	public ProfilePhotoDetails getProfilePhotoURL() throws NamingException {
		String url;	
		try {
			url = profilePhotoDetails.getUrl();
			if (url == null) {

				// look up the connectivity configuration API
				// "connectivityConfiguration"
				Context ctx = new InitialContext();
				ConnectivityConfiguration configuration;

				configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");

				DestinationConfiguration destConfiguration = configuration.getConfiguration("sf_profile_photo");

				// get the sf profile photo URL
				url = destConfiguration.getProperty("URL");
                profilePhotoDetails.setUrl(url);
			}
		} catch (NamingException e) {
			logger.error(
					"something went wrong attempting to get the SF profile photo URL, probably an issue with configuration",
					e);
			throw e;
		}
		return profilePhotoDetails;
	}

}