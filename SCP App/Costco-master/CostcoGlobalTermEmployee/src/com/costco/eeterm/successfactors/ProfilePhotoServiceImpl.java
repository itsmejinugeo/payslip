package com.costco.eeterm.successfactors;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.springframework.stereotype.Service;

import com.costco.eeterm.successfactors.pojo.ProfilePhotoDetails;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;
@Service
public class ProfilePhotoServiceImpl implements ProfilePhotoService {
	
	private ProfilePhotoDetails profilePhotoDetails = new ProfilePhotoDetails();

	@Override
	public ProfilePhotoDetails getProfilePhotoURL() throws NamingException {
		String url = profilePhotoDetails.getUrl();
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
		return profilePhotoDetails;
	}

}