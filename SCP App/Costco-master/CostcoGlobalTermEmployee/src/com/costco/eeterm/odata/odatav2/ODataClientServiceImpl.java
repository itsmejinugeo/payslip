package com.costco.eeterm.odata.odatav2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.FeedMetadata;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.uri.ExpandSelectTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.costco.eeterm.gson.GsonHelper;
import com.costco.eeterm.odata.odatav2.pojo.ODataFunctionImportParameter;
import com.costco.eeterm.odata.odatav2.pojo.PostErrorResponse;
import com.costco.eeterm.odata.odatav2.pojo.UpsertResponse;
import com.costco.eeterm.odata.odatav2.pojo.UpsertResponseDetail;
import com.costco.eeterm.successfactors.pojo.EmployeeCentralConstant;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;
import com.sap.cloud.account.TenantContext;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.authentication.AuthenticationHeaderProvider;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

@Service
public class ODataClientServiceImpl implements ODataClientService {
	
	private static class ODataEntrySorter<E> implements Comparator<ODataEntry>, Serializable {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public int compare(ODataEntry o1, ODataEntry o2) {

			if (o1.getProperties().get(EmployeeCentralConstant.EFFECTIVESTARTDATE) != null) {

				Calendar effectiveStartDate1 = (GregorianCalendar) o1.getProperties().get(EmployeeCentralConstant.EFFECTIVESTARTDATE);

				Calendar effectiveStartDate2 = (GregorianCalendar) o2.getProperties().get(EmployeeCentralConstant.EFFECTIVESTARTDATE);

				return effectiveStartDate2.compareTo(effectiveStartDate1);
			} else {
				Calendar effectiveStartDate1 = (GregorianCalendar) o1.getProperties().get("startDate");

				Calendar effectiveStartDate2 = (GregorianCalendar) o2.getProperties().get("startDate");

				if (effectiveStartDate2.compareTo(effectiveStartDate1) != 0) {
					return effectiveStartDate2.compareTo(effectiveStartDate1);
				} else {
					Long seqnr1 = (Long) o1.getProperties().get("seqNumber");
					Long seqnr2 = (Long) o2.getProperties().get("seqNumber");

					return seqnr2.compareTo(seqnr1);
				}

			}

		}
	}
	
	private static Comparator<ODataEntry> oDataEntryComparator = new ODataEntrySorter<ODataEntry>();
	private static final String APPLICATION_JSON = "application/json; charset=utf-8";

	private static final String DESTINATION_NAME = "sap_hcmcloud_core_odata";
	private static final String DESTINATION_NAME_TECH = "sap_hcmcloud_core_odata_technical_user";
	private static final String HTTP_METHOD_PATCH = "PATCH";
	private static final String HTTP_METHOD_POST = "POST";
	private static final Logger logger = LoggerFactory.getLogger(ODataClientServiceImpl.class);
	protected static final String APPLICATION_XML = "application/xml";

	protected static final String HTTP_METHOD_DELETE = "DELETE";
	protected static final String HTTP_METHOD_GET = "GET";
	protected static final String HTTP_METHOD_PUT = "PUT";
	private static final String HTTP_METHOD_POST_MERGE = "POST_MERGE";
	protected static final String METADATA = "$metadata";
	protected static final String SEPARATOR = "/";

	protected static final String SERVICES = "services";

	private Map<String, TenantConnectionDetails> detailsForTenant = new HashMap<String, TenantConnectionDetails>();

	private String currentTenantId = null;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ODataEntry backgroundReadEntry(String entitySetName, String keyValue, String expandRelationName, String selectFields)
			throws IOException, ODataException, NamingException {
		return readEntry(entitySetName, keyValue, expandRelationName, selectFields, null, null, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JsonObject backgroundReadFeed(String functionName, List<ODataFunctionImportParameter> parameters)
			throws IOException, ODataException, NamingException {
		getEdm(true);
		TenantConnectionDetails tenantConnectionDetails = getCurrentTenantConnectionDetails(true);
		String absolutUri = createUri(tenantConnectionDetails.getUrl(), functionName, parameters);

		return readODataFromURI(absolutUri);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ODataFeed backgroundReadFeed(String entitySetName, String expand, String filter, String selectFields)
			throws IOException, ODataException, NamingException {
		getEdm(true);
		TenantConnectionDetails tenantConnectionDetails = getCurrentTenantConnectionDetails(true);

		EdmEntityContainer entityContainer = tenantConnectionDetails.getEdm().getDefaultEntityContainer();
		String absolutUri = createUri(tenantConnectionDetails.getUrl(), entitySetName, null, expand, filter, filter != null, selectFields);

		return readODataFromUriCheckingNextLinks(entitySetName, entityContainer, absolutUri, true);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object backgroundReadFunctionImportData(String functionImport, String expand, String filter, String selectFields)
			throws IOException, ODataException, NamingException {

		getEdm(true);
		TenantConnectionDetails tenantConnectionDetails = getCurrentTenantConnectionDetails(true);

		EdmEntityContainer entityContainer = tenantConnectionDetails.getEdm().getDefaultEntityContainer();
		String absolutUri = createUriFunctionImport(tenantConnectionDetails.getUrl(), functionImport, null, expand, filter, selectFields);

		return readODataFromFunctionImport(functionImport, entityContainer, absolutUri, true);

	}

	@Override
	public ODataEntry backgroundUpsertEntry(String id, String entitySetName, Map<String, Object> data)
			throws URISyntaxException, IOException, NamingException, ODataException {
		TenantConnectionDetails tenantConnectionDetails = detailsForTenant.get(getDestinationId(true));
		String absolutUri = tenantConnectionDetails.getUrl() + "/upsert";
		return upsert(absolutUri, entitySetName, data, id, true);
	}

	@Override
	public ODataEntry backgroundUpsertEntry(String id, String entitySetName, List<Map<String, Object>> data)
			throws URISyntaxException, IOException, NamingException, ODataException {
		TenantConnectionDetails tenantConnectionDetails = detailsForTenant.get(getDestinationId(true));
		String absolutUri = tenantConnectionDetails.getUrl() + "/upsert";
		return upsert(absolutUri, entitySetName, data, id, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long countFeed(String entitySetName, String filter, boolean backgroundUser) throws EdmException, IOException, EntityProviderException, NamingException {
		return countFeed(entitySetName, filter, null, backgroundUser);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long countFeed(String entitySetName, String filter, Date asOfDate, boolean backgroundUser)
			throws EdmException, IOException, EntityProviderException, NamingException {
		getEdm(backgroundUser);
		TenantConnectionDetails tenantConnectionDetails = detailsForTenant.get(getDestinationId(backgroundUser));
		String encodedFilter = "";
		Long count = 0L;
		try {
			if (filter == null) {
				filter = "";
			}
			encodedFilter = URLEncoder.encode(filter, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("problem encoding OData URL");
			return count;
		}

		StringBuilder absolutUri = new StringBuilder(tenantConnectionDetails.getUrl()).append(SEPARATOR).append(entitySetName);
		absolutUri = absolutUri.append(SEPARATOR).append("$count");
		absolutUri.append("/?$filter=").append(encodedFilter);
		if (asOfDate != null) {
			SimpleDateFormat asOfFormat = new SimpleDateFormat("yyyy-MM-dd");
			String fromDateFilter = "&fromDate=" + asOfFormat.format(asOfDate);
			String toDateFilter = "&toDate=" + asOfFormat.format(asOfDate);
			absolutUri.append(fromDateFilter).append(toDateFilter);
		}
		InputStream content = execute(absolutUri.toString(), APPLICATION_JSON, HTTP_METHOD_GET, false);
		String countContent = IOUtils.toString(content);
		content.close();
		try {
			count = Long.valueOf(countContent);
		} catch (NumberFormatException e) {
			logger.error("count response is not as expected" + countContent);
		}
		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ODataEntry createEntry(String entitySetName, Map<String, Object> data, Map<String, String> deepData)
			throws URISyntaxException, IOException, NamingException, ODataException {
		getEdm();
		TenantConnectionDetails tenantConnectionDetails = detailsForTenant.get(getDestinationId(false));
		String absolutUri = createUri(tenantConnectionDetails.getUrl(), entitySetName, null, null, null, false, null);
		return writeEntity(absolutUri, entitySetName, data, deepData, APPLICATION_JSON, HTTP_METHOD_POST, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ODataEntry backgroundCreateEntry(String entitySetName, Map<String, Object> data, Map<String, String> deepData)
			throws URISyntaxException, IOException, NamingException, ODataException {
		getEdm(true);
		TenantConnectionDetails tenantConnectionDetails = detailsForTenant.get(getDestinationId(true));
		String absolutUri = createUri(tenantConnectionDetails.getUrl(), entitySetName, null, null, null, false, null);
		return writeEntity(absolutUri, entitySetName, data, deepData, APPLICATION_JSON, HTTP_METHOD_POST, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteEntry(String id, String entitySetName) throws IOException, ODataException, NamingException {
		getEdm();
		TenantConnectionDetails tenantConnectionDetails = detailsForTenant.get(getDestinationId(false));
		// create absolute uri based on service uri, entity set name with its
		// key property value and optional expanded relation name
		String absolutUri = createUri(tenantConnectionDetails.getUrl(), entitySetName, id, null, null, false, null);

		execute(absolutUri, APPLICATION_JSON, HTTP_METHOD_DELETE, false);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ODataEntry mergeEntry(String id, String entitySetName, Map<String, Object> data, Map<String, String> deepData)
			throws URISyntaxException, IOException, NamingException, ODataException {
		getEdm();
		TenantConnectionDetails tenantConnectionDetails = getCurrentTenantConnectionDetails(false);
		String absolutUri = createUri(tenantConnectionDetails.getUrl(), entitySetName, id, null, null, false, null);
		return writeEntity(absolutUri, entitySetName, data, deepData, APPLICATION_JSON, HTTP_METHOD_POST_MERGE, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ODataEntry patchEntry(String id, String entitySetName, Map<String, Object> data)
			throws IOException, NamingException, URISyntaxException, ODataException {
		getEdm();
		TenantConnectionDetails tenantConnectionDetails = detailsForTenant.get(getDestinationId(false));
		String absolutUri = createUri(tenantConnectionDetails.getUrl(), entitySetName, id, null, null, false, null);
		return writeEntity(absolutUri, entitySetName, data, null, APPLICATION_JSON, HTTP_METHOD_PATCH, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ODataEntry readEntry(String entitySetName, String keyValue, String expandRelationName, String selectFields)
			throws IOException, ODataException, NamingException {
		return readEntry(entitySetName, keyValue, expandRelationName, selectFields, null, null, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ODataEntry readEntry(String entitySetName, String keyValue, String expandRelationName, String selectFields, Date fromDate,
			Date toDate) throws IOException, EntityProviderException, EdmException, NamingException {
		return readEntry(entitySetName, keyValue, expandRelationName, selectFields, fromDate, toDate, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JsonObject readFeed(String functionName, List<ODataFunctionImportParameter> parameters)
			throws IOException, ODataException, NamingException {
		getEdm(true);
		TenantConnectionDetails tenantConnectionDetails = getCurrentTenantConnectionDetails(false);

		String absolutUri = createUri(tenantConnectionDetails.getUrl(), functionName, parameters);

		return readODataFromURI(absolutUri);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ODataFeed readFeed(String entitySetName, String expand, String filter, String selectFields)
			throws IOException, ODataException, NamingException {
		return readFeed(entitySetName, expand, filter, selectFields, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ODataFeed readFeed(String entitySetName, String expand, String filter, String selectFields, Date asOfDate)
			throws IOException, ODataException, NamingException {
		return readFeed(entitySetName, expand, filter, selectFields, null, 0, false, asOfDate);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ODataFeed readFeed(String entitySetName, String expand, String filter, String selectFields, Date fromDate, Date toDate)
			throws IOException, ODataException, NamingException {
		getEdm();
		TenantConnectionDetails tenantConnectionDetails = detailsForTenant.get(getDestinationId(false));

		EdmEntityContainer entityContainer = tenantConnectionDetails.getEdm().getDefaultEntityContainer();
		String absolutUri = createUri(tenantConnectionDetails.getUrl(), entitySetName, null, expand, filter, filter != null, selectFields);

		if (fromDate != null && toDate != null && (fromDate.before(toDate) || fromDate.equals(toDate))) {
			SimpleDateFormat asOfFormat = new SimpleDateFormat("yyyy-MM-dd");
			String fromDateFilter = "&fromDate=" + asOfFormat.format(fromDate);
			String toDateFilter = "&toDate=" + asOfFormat.format(toDate);
			absolutUri = absolutUri + fromDateFilter + toDateFilter;
		}

		return readODataFromUriCheckingNextLinks(entitySetName, entityContainer, absolutUri, false);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ODataFeed readFeed(String entitySetName, String expand, String filter, String selectFields, String orderBy, int numberResults)
			throws IOException, ODataException, NamingException {
		return readFeed(entitySetName, expand, filter, selectFields, orderBy, numberResults, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ODataFeed readFeed(String entitySetName, String expand, String filter, String selectFields, String orderBy, int numberResults,
			boolean count) throws IOException, ODataException, NamingException {
		return readFeed(entitySetName, expand, filter, selectFields, orderBy, numberResults, count, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ODataFeed readFeed(String entitySetName, String expand, String filter, String selectFields, String orderBy, int numberResults,
			boolean count, Date readAtDate) throws IOException, ODataException, NamingException {
		getEdm();
		TenantConnectionDetails tenantConnectionDetails = detailsForTenant.get(getDestinationId(false));

		boolean applyFilter = true;
		if (filter == null) {
			applyFilter = false;
		}

		EdmEntityContainer entityContainer = tenantConnectionDetails.getEdm().getDefaultEntityContainer();

		String absolutUri = createUri(tenantConnectionDetails.getUrl(), entitySetName, null, expand, filter, applyFilter, selectFields,
				orderBy, numberResults, false);

		if (readAtDate != null) {
			SimpleDateFormat asOfFormat = new SimpleDateFormat("yyyy-MM-dd");
			String atDateFilter = "&asOfDate=" + asOfFormat.format(readAtDate);
			absolutUri = absolutUri + atDateFilter;
		}

		return readODataFromUriCheckingNextLinks(entitySetName, entityContainer, absolutUri, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object readFunctionImportData(String functionImport, String expand, String filter, String selectFields)
			throws IOException, ODataException, NamingException {

		getEdm();
		TenantConnectionDetails tenantConnectionDetails = detailsForTenant.get(getDestinationId(false));

		EdmEntityContainer entityContainer = tenantConnectionDetails.getEdm().getDefaultEntityContainer();
		String absolutUri = createUriFunctionImport(tenantConnectionDetails.getUrl(), functionImport, null, expand, filter, selectFields);

		return readODataFromFunctionImport(functionImport, entityContainer, absolutUri, false);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ODataEntry> sort(List<ODataEntry> entries) {
		Collections.sort(entries, oDataEntryComparator);
		return entries;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ODataEntry updateEntry(String id, String entitySetName, Map<String, Object> data, Map<String, String> deepData)
			throws URISyntaxException, IOException, NamingException, ODataException {
		getEdm();
		TenantConnectionDetails tenantConnectionDetails = detailsForTenant.get(getDestinationId(false));
		String absolutUri = createUri(tenantConnectionDetails.getUrl(), entitySetName, id, null, null, false, null);
		return writeEntity(absolutUri, entitySetName, data, deepData, APPLICATION_JSON, HTTP_METHOD_PUT, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ODataEntry upsertEntry(String id, String entitySetName, Map<String, Object> data)
			throws URISyntaxException, IOException, NamingException, ODataException {
		getEdm();
		TenantConnectionDetails tenantConnectionDetails = detailsForTenant.get(getDestinationId(false));
		String absolutUri = tenantConnectionDetails.getUrl() + "/upsert";
		return upsert(absolutUri, entitySetName, data, id, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ODataEntry upsertEntry(String id, String entitySetName, List<Map<String, Object>> data)
			throws URISyntaxException, IOException, NamingException, ODataException {
		getEdm();
		TenantConnectionDetails tenantConnectionDetails = detailsForTenant.get(getDestinationId(false));
		String absolutUri = tenantConnectionDetails.getUrl() + "/upsert";
		return upsert(absolutUri, entitySetName, data, id, false);
	}

	private String createUri(String serviceUri, String functionName, List<ODataFunctionImportParameter> parameters) {
		CharSequence cs1 = "Long";
		final StringBuilder absolutUri = new StringBuilder(serviceUri).append(SEPARATOR).append(functionName);

		if (!parameters.isEmpty()) {
			int i = 0;
			for (ODataFunctionImportParameter param : parameters) {
				String value = param.getValue();
				if (!param.getParam().contains(cs1)) {
					value = "'" + param.getValue() + "'";
				}
				if (i == 0) {
					absolutUri.append("?").append(param.getParam()).append("=").append(value);
				} else {
					absolutUri.append("&" + param.getParam() + "=").append(value);
				}
				i++;
			}
		}
		return absolutUri.toString();
	}

	private String createUri(String serviceUri, String entitySetName, String id, String expand, String filter, boolean applyFilter,
			String select) {

		return createUri(serviceUri, entitySetName, id, expand, filter, applyFilter, select, null, 0, false);
	}

	private String createUri(String serviceUri, String entitySetName, String id, String expand, String filter, boolean applyFilter,
			String select, String orderBy, int numberResults, boolean returnCount) {

		String encodedFilter = null;
		if (applyFilter) {
			try {
				if (filter == null) {
					filter = "";
				}
				encodedFilter = URLEncoder.encode(filter, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.error("problem encoding OData URL");
				return null;
			}
		}

		final StringBuilder absolutUri = new StringBuilder(serviceUri).append(SEPARATOR).append(entitySetName);
		if (id != null) {
			absolutUri.append("(").append(id).append(")");
		}
		if (applyFilter) {
			absolutUri.append("/?$filter=").append(encodedFilter);
			if (expand != null) {
				absolutUri.append("&$expand=").append(expand);
			}
			if (select != null) {
				absolutUri.append("&$select=").append(select);
			}
			if (numberResults > 0) {
				absolutUri.append("&$top=").append(numberResults);
			}
			if (orderBy != null) {
				absolutUri.append("&$orderby=").append(orderBy);
			}
		} else {
			absolutUri.append("?");
			if (expand != null) {
				absolutUri.append("&$expand=").append(expand);
			}
			if (select != null) {
				absolutUri.append("&$select=").append(select);
			}
			if (numberResults > 0) {
				absolutUri.append("&$top=").append(numberResults);
			}
			if (orderBy != null) {
				absolutUri.append("&$orderby=").append(orderBy);
			}
		}
		if (returnCount) {
			if (applyFilter || (expand != null)) {
				absolutUri.append("&$inlinecount=allpages");
			} else {
				absolutUri.append("?&$inlinecount=allpages");
			}
		}
		return absolutUri.toString();
	}

	private String createUriFunctionImport(String serviceUri, String entitySetName, String id, String expand, String filter,
			String select) {

		final StringBuilder absolutUri = new StringBuilder(serviceUri).append(SEPARATOR).append(entitySetName);
		if (id != null) {
			absolutUri.append("(").append(id).append(")");
		}

		absolutUri.append("?");
		if (filter != null) {
			absolutUri.append(filter);
		}
		if (expand != null) {
			absolutUri.append("&$expand=").append(expand);
		}
		if (select != null) {
			absolutUri.append("&$select=").append(select);
		}

		return absolutUri.toString();
	}

	private TenantConnectionDetails getCurrentTenantConnectionDetails(boolean background) {
		String tenantId = getDestinationId(background);
		TenantConnectionDetails tenantConnectionDetails = detailsForTenant.get(tenantId);
		return tenantConnectionDetails;
	}

	private String getDestinationId(boolean backgroundUser) {
		String tenantId = "SingleTenant";
		if (backgroundUser) {
			tenantId = tenantId + "_technicalUser";
		}
		return tenantId;
	}

	private void getEdm() throws EntityProviderException, IOException, NamingException {
		getEdm(false);
	}

	private synchronized Edm getEdm(boolean backgroundUser) throws IOException, NamingException, EntityProviderException {

		TenantConnectionDetails tenantConnectionDetails = getCurrentTenantConnectionDetails(backgroundUser);
		Edm edm = null;
		if (tenantConnectionDetails == null) {
			tenantConnectionDetails = new TenantConnectionDetails();
		}
		try {
			edm = tenantConnectionDetails.getEdm();
			if (edm == null) {

				// look up the connectivity configuration API
				// "connectivityConfiguration"
				Context ctx = new InitialContext();
				ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");

				TenantContext tenantContext = (TenantContext) ctx.lookup("java:comp/env/TenantContext");
				currentTenantId = tenantContext.getTenant().getAccount().getId();
				// get destination configuration
				DestinationConfiguration destConfiguration;
				if (currentTenantId.equals("dev_default")) {
					// local testing
					destConfiguration = configuration.getConfiguration(DESTINATION_NAME);
				} else {
					if (backgroundUser) {
						destConfiguration = configuration.getConfiguration(currentTenantId, DESTINATION_NAME_TECH);
					} else {
						destConfiguration = configuration.getConfiguration(currentTenantId, DESTINATION_NAME);
					}
				}

				// get the sflms URL
				tenantConnectionDetails.setUrl(destConfiguration.getProperty("URL"));
				String tenantId = getDestinationId(backgroundUser);
				detailsForTenant.put(tenantId, tenantConnectionDetails);
				tenantConnectionDetails.setDestConfiguration(destConfiguration);

				InputStream content = execute(tenantConnectionDetails.getUrl() + SEPARATOR + METADATA, APPLICATION_XML, HTTP_METHOD_GET,
						backgroundUser);

				edm = EntityProvider.readMetadata(content, false);
				tenantConnectionDetails.setEdm(edm);
			}
		} catch (IOException | NamingException | EntityProviderException e) {
			logger.error("something went wrong attempting to get the EDM"
					+ ", probably an issue with Framework, likely other things are about to fail.", e);
			throw e;
		}
		if (edm == null) {
			// we have an issue
			logger.error("something went wrong attempting to get the EDM"
					+ ", probably an issue with framework, likely other things are about to fail.");
		}
		return edm;
	}

	private ODataEntry readEntry(String entitySetName, String keyValue, String expandRelationName, String selectFields, Date fromDate,
			Date toDate, boolean background) throws IOException, EntityProviderException, EdmException, NamingException {

		getEdm(background);
		TenantConnectionDetails tenantConnectionDetails = getCurrentTenantConnectionDetails(background);

		EdmEntityContainer entityContainer = tenantConnectionDetails.getEdm().getDefaultEntityContainer();

		// create absolute uri based on service uri, entity set name with its
		// key property value and optional expanded relation name
		String absolutUri = createUri(tenantConnectionDetails.getUrl(), entitySetName, keyValue, expandRelationName, null, false,
				selectFields);

		if (fromDate != null && toDate != null && fromDate.before(toDate)) {
			SimpleDateFormat asOfFormat = new SimpleDateFormat("yyyy-MM-dd");
			String fromDateFilter = "&fromDate=" + asOfFormat.format(fromDate);
			String toDateFilter = "&toDate=" + asOfFormat.format(toDate);
			absolutUri = absolutUri + fromDateFilter + toDateFilter;
		}

		int retryCount = 0;
		int maxTries = 5;
		boolean retry = true;
		ODataEntry result = null;
		while (retry) {

			InputStream content = execute(absolutUri, APPLICATION_JSON, HTTP_METHOD_GET, background);
			// just for logging
			byte[] returnedOData = IOUtils.toByteArray(content);
			content.close();
			InputStream responseContent = new ByteArrayInputStream(returnedOData);
			try {
				result = EntityProvider.readEntry(APPLICATION_JSON, entityContainer.getEntitySet(entitySetName), responseContent,
						EntityProviderReadProperties.init().build());
				retry = false;
				if (retryCount > 0) {
					logger.warn("Retry successful");
				}
			} catch (EntityProviderException e) {
				String entryContent = new String(returnedOData, "UTF-8");

				if (e.getCause() != null && e.getCause().getClass().equals(MalformedJsonException.class)) {
					logger.error("Error. SuccessFactors sent malformed JSON {}", entryContent);
					if (++retryCount == maxTries) {
						retry = false;
					} else {
						logger.warn("Retry number {}. Read entry from SuccessFactors:{}", retryCount, absolutUri);
					}
				} else {
					logger.error("Issue reading entry with URL: " + absolutUri + "/n returned values: " + entryContent);
					throw e;

				}
			}
		}

		return result;
	}

	private Object readODataFromFunctionImport(String functionImport, EdmEntityContainer entityContainer, String absolutUri,
			boolean background) throws IOException, EdmException, UnsupportedEncodingException, EntityProviderException {

		int retryCount = 0;
		int maxTries = 5;
		boolean retry = true;
		Object result = null;
		while (retry) {

			InputStream content = execute(absolutUri, APPLICATION_JSON, HTTP_METHOD_GET, background);
			// just for logging
			byte[] returnedOData = IOUtils.toByteArray(content);
			content.close();
			InputStream responseContent = null;
			// if (functionImport.equals("getUserRolesByUserId")) {
			// String returnedData = new String(returnedOData, "UTF-8");
			//
			//
			// returnedData = returnedData.substring(returnedData.indexOf(':')+1,
			// returnedData.length());
			// returnedData = returnedData.substring(0, returnedData.lastIndexOf("}")-1);
			// returnedData = returnedData.replaceFirst(".*\\bresults\\b.*", "\"d\":[");
			// responseContent = new ByteArrayInputStream(returnedData.getBytes());
			// } else {
			responseContent = new ByteArrayInputStream(returnedOData);
			// }

			try {

				result = EntityProvider.readFunctionImport(HttpContentType.APPLICATION_JSON,
						entityContainer.getFunctionImport(functionImport), responseContent, EntityProviderReadProperties.init().build());
				retry = false;
				if (retryCount > 0) {
					logger.warn("Retry successful");
				}
			} catch (EntityProviderException e) {
				String feedContent = new String(returnedOData, "UTF-8");

				if (e.getCause() != null && e.getCause().getClass().equals(MalformedJsonException.class)) {
					logger.error("Error. SuccessFactors sent malformed JSON {}", feedContent);
					if (++retryCount == maxTries) {
						retry = false;
					} else {
						logger.warn("Retry number {}. Read feed from SuccessFactors:{}", retryCount, absolutUri);
					}
				} else {
					logger.error("Issue reading feed with URL: " + absolutUri + "/n returned values: " + feedContent);
					throw e;

				}
			}
		}
		return result;
	}

	private JsonObject readODataFromURI(String absolutUri) throws IOException, ODataException {
		InputStream content = execute(absolutUri, APPLICATION_JSON, HTTP_METHOD_GET, true);
		byte[] returnedOData = IOUtils.toByteArray(content);
		content.close();
		JsonObject jobject = null;
		String feedContent = null;
		try {
			feedContent = new String(returnedOData, "UTF-8");
			JsonElement jelement = new JsonParser().parse(feedContent);
			jobject = jelement.getAsJsonObject();
		} catch (JsonParseException e) {
			logger.error("Issue reading feed with URL: {}", absolutUri);
			throw new ODataException(e);
		}
		return jobject;
	}

	private ODataFeed readODataFromURI(String entitySetName, EdmEntityContainer entityContainer, String absolutUri, boolean background)
			throws IOException, EdmException, UnsupportedEncodingException, EntityProviderException {

		int retryCount = 0;
		int maxTries = 5;
		boolean retry = true;
		ODataFeed feed = null;
		while (retry) {

			InputStream content = execute(absolutUri, APPLICATION_JSON, HTTP_METHOD_GET, background);

			byte[] returnedOData = IOUtils.toByteArray(content);
			content.close();
			String serverResponse = new String(returnedOData, "UTF-8");

			try {
				feed = EntityProvider.readFeed(APPLICATION_JSON, entityContainer.getEntitySet(entitySetName),
						new ByteArrayInputStream(returnedOData), EntityProviderReadProperties.init().build());
				retry = false;
				if (retryCount > 0) {
					logger.warn("Retry successful");
				}

			} catch (EntityProviderException | NullPointerException e) {

				if (e.getCause() != null && e.getCause().getClass().equals(MalformedJsonException.class)) {
					logger.error("Error. SuccessFactors sent malformed JSON {}", serverResponse);
					if (++retryCount == maxTries) {
						retry = false;
					} else {
						logger.warn("Retry number {}. Read feed from SuccessFactors : {}", retryCount, absolutUri);
					}
				} else {
					logger.error("Error reading feed from SuccessFactors server response: {}", serverResponse);
					throw e;

				}

			}
		}
		return feed;
	}

	private ODataFeed readODataFromUriCheckingNextLinks(String entitySetName, EdmEntityContainer entityContainer, String absolutUri,
			boolean background) throws IOException, EdmException, UnsupportedEncodingException, EntityProviderException {

		ODataFeed feed = null;

		feed = readODataFromURI(entitySetName, entityContainer, absolutUri, background);

		FeedMetadata metadata = feed.getFeedMetadata();
		String nextLink = metadata.getNextLink();
		if (nextLink != null) {
			ODataFeed nextFeed = readODataFromUriCheckingNextLinks(entitySetName, entityContainer, nextLink, background);
			feed.getEntries().addAll(nextFeed.getEntries());
		}

		return feed;
	}

	private void setAppToAppSSO(String uri, HttpRequest request) {
		try {
			Context ctx = new InitialContext();
			AuthenticationHeaderProvider authHeaderProvider = (AuthenticationHeaderProvider) ctx.lookup("java:comp/env/authHeaderProvider");
			AuthenticationHeader appToAppSSOHeader = authHeaderProvider.getAppToAppSSOHeader(uri);
			String sflmsHeaderValue = appToAppSSOHeader.getValue();
			request.setHeader(appToAppSSOHeader.getName(), sflmsHeaderValue);
			logger.debug("using authenication: " + sflmsHeaderValue);
		} catch (NamingException e) {
			logger.error("problem getting reference to context");
		}
	}

	private synchronized void setAuthenticationHeader(String uri, HttpRequest request, boolean background) throws UnsupportedEncodingException {
		TenantConnectionDetails tenantConnectionDetails = getCurrentTenantConnectionDetails(background);
	
		try {
			Context ctx = new InitialContext();
			
			if (currentTenantId.equals("dev_default")) {
				String basicAuthPreBase64 = tenantConnectionDetails.getDestConfiguration().getProperty("User") + ":"
						+ tenantConnectionDetails.getDestConfiguration().getProperty("Password");
				String basicAuth = "Basic " + Base64.encodeBase64String(basicAuthPreBase64.getBytes("UTF-8"));
				request.setHeader("Authorization", basicAuth);
				logger.debug("using authenication: {}", basicAuth);
			} else {
				String authType = tenantConnectionDetails.getDestConfiguration().getProperty("Authentication");
				if (authType.equals("OAuth2SAMLBearerAssertion")) {
					logger.debug("using authentication: OAuth2SAMLBearer");
					List<AuthenticationHeader> authHeaderList;					
					AuthenticationHeaderProvider authHeaderProvider = (AuthenticationHeaderProvider) ctx
							.lookup("java:comp/env/authHeaderProvider");
					authHeaderList = authHeaderProvider.getOAuth2SAMLBearerAssertionHeaders(tenantConnectionDetails.getDestConfiguration());

					for (AuthenticationHeader header : authHeaderList) {
						String sfHeaderValue = header.getValue();
						String sfHeaderName = header.getName();
						request.setHeader(sfHeaderName, sfHeaderValue);
						logger.debug("using authenication: {}:{}", sfHeaderName, sfHeaderValue);
					}
				} else if (authType.equals("AppToAppSSO")) {
					setAppToAppSSO(uri, request);
				}
			}

		} catch (NamingException e) {
			logger.error("problem getting reference to context, falling back to basic", e);
			String basicAuthPreBase64 = tenantConnectionDetails.getDestConfiguration().getProperty("User") + ":"
					+ tenantConnectionDetails.getDestConfiguration().getProperty("Password");
			String basicAuth = "Basic " + Base64.encodeBase64String(basicAuthPreBase64.getBytes("UTF-8"));
			request.setHeader("Authorization", basicAuth);
			logger.debug("using authenication: {}", basicAuth);
		}
	}

	private ODataEntry upsert(String absolutUri, String entitySetName, List<Map<String, Object>> dataEntries, String id,
			boolean backgroundUser) throws URISyntaxException, IOException, NamingException, ODataException {
		EdmEntityContainer entityContainer;
		EdmEntitySet entitySet;
		URI rootUri;
		ExpandSelectTreeNode node;
		TenantConnectionDetails tenantConnectionDetails = detailsForTenant.get(getDestinationId(backgroundUser));
		Gson gson = GsonHelper.adaptedGson;
		String upsertContent = "[";
		for (Map<String, Object> data : dataEntries) {
			try {
				entityContainer = tenantConnectionDetails.getEdm().getDefaultEntityContainer();
				entitySet = entityContainer.getEntitySet(entitySetName);
				rootUri = new URI(absolutUri.substring(0, absolutUri.lastIndexOf("/") + 1));
				node = ExpandSelectTreeNode.entitySet(entitySet).selectedProperties(new ArrayList<String>(data.keySet())).build();
			} catch (EdmException e) {
				// try refreshing edm may have added a new field
				tenantConnectionDetails.setEdm(null);
				getEdm();
				entityContainer = tenantConnectionDetails.getEdm().getDefaultEntityContainer();
				entitySet = entityContainer.getEntitySet(entitySetName);
				rootUri = new URI(absolutUri.substring(0, absolutUri.lastIndexOf("/") + 1));

				node = ExpandSelectTreeNode.entitySet(entitySet).selectedProperties(new ArrayList<String>(data.keySet())).build();

			}

			EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(rootUri).omitJsonWrapper(true)
					.contentOnly(true).expandSelectTree(node).build();

			try {
				// serialize data into ODataResponse object
				ODataResponse response = EntityProvider.writeEntry(APPLICATION_JSON, entitySet, data, properties);

				// get (http) entity which is for default Olingo implementation an
				// InputStream
				InputStream entity = response.getEntityAsStream();

				byte[] sendBuffer = IOUtils.toByteArray(entity);
				entity.close();
				String content = new String(sendBuffer, "UTF-8");

				upsertContent += "{\"__metadata\": {\"uri\": \"" + entitySetName + "\"}," + content.substring(1) + ",";

			} catch (EntityProviderException e) {
				logger.error("Error forming OData - using upsert {} with content {}", entitySetName, data.toString(), e);
				throw e;
			}
		}

		upsertContent = upsertContent.substring(0, upsertContent.length() - 1) + "]";
		logger.debug("Sending POST to Odata upsert: {}", upsertContent);

		String serverResponse = "";
		try (CloseableHttpClient client = HttpClients.custom().build()) {
			HttpEntityEnclosingRequestBase request;

			request = new HttpPost(absolutUri);

			setAuthenticationHeader(absolutUri, request, backgroundUser);

			request.setEntity(new StringEntity(upsertContent, "UTF-8"));
			request.setHeader("Accept", APPLICATION_JSON);
			request.setHeader("Cache-Control", "no-cache");
			request.setHeader("Content-Type", APPLICATION_JSON);
			logger.debug("Connecting to OData with query: {}", absolutUri);
			HttpResponse postResponse = client.execute(request);

			if (postResponse.getStatusLine().getStatusCode() == 200) {
				logger.debug("200 from server Upsert call was successful, check if body contains error message");

				InputStream content = postResponse.getEntity().getContent();
				byte[] responseBuffer = IOUtils.toByteArray(content);
				serverResponse = new String(responseBuffer, "UTF-8");
				logger.debug("Response from OData server : {}", serverResponse);

				UpsertResponse upsertResponse = gson.fromJson(serverResponse, UpsertResponse.class);

				if (upsertResponse != null && upsertResponse.getD() != null && !upsertResponse.getD().isEmpty()) {
					UpsertResponseDetail detail = upsertResponse.getD().get(0);

					if (detail.getStatus().equals("ERROR")) {
						// uh oh something gone wrong
						logger.warn("Upsert error: {}", detail.getMessage());
						throw new ODataException("Upsert error:" + detail.getMessage());
					}
				}

				return null;
			}

			if (postResponse.getStatusLine().getStatusCode() != 204) {
				InputStream content = postResponse.getEntity().getContent();
				byte[] responseBuffer = IOUtils.toByteArray(content);
				serverResponse = new String(responseBuffer, "UTF-8");
				logger.error("Non 200 or 204 status from server on POST/PUT - probable error: {}", serverResponse);
				logger.error("Request that caused error: {}\n Sending upsert to Odata: {}" + absolutUri, upsertContent);
				throw new ODataException();
			} else {
				logger.debug("204 from server upsert call was successful but returned no body");
			}

		} catch (IOException e) {
			logger.error("Error connecting to OData - using upsert {} with content {} and returning response: {}", absolutUri,
					upsertContent, serverResponse, e);

		}
		return null;

	}

	private ODataEntry upsert(String absolutUri, String entitySetName, Map<String, Object> data, String id, boolean backgroundUser)
			throws URISyntaxException, IOException, NamingException, ODataException {
		EdmEntityContainer entityContainer;
		EdmEntitySet entitySet;
		URI rootUri;
		ExpandSelectTreeNode node;
		TenantConnectionDetails tenantConnectionDetails = detailsForTenant.get(getDestinationId(backgroundUser));
		try {
			entityContainer = tenantConnectionDetails.getEdm().getDefaultEntityContainer();
			entitySet = entityContainer.getEntitySet(entitySetName);
			rootUri = new URI(absolutUri.substring(0, absolutUri.lastIndexOf("/") + 1));

			node = ExpandSelectTreeNode.entitySet(entitySet).selectedProperties(new ArrayList<String>(data.keySet())).build();

		} catch (EdmException e) {
			// try refreshing edm may have added a new field

			tenantConnectionDetails.setEdm(null);
			getEdm();
			entityContainer = tenantConnectionDetails.getEdm().getDefaultEntityContainer();
			entitySet = entityContainer.getEntitySet(entitySetName);
			rootUri = new URI(absolutUri.substring(0, absolutUri.lastIndexOf("/") + 1));

			node = ExpandSelectTreeNode.entitySet(entitySet).selectedProperties(new ArrayList<String>(data.keySet())).build();

		}

		EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(rootUri).omitJsonWrapper(true)
				.contentOnly(true).expandSelectTree(node).build();

		try {
			// serialize data into ODataResponse object
			ODataResponse response = EntityProvider.writeEntry(APPLICATION_JSON, entitySet, data, properties);

			// get (http) entity which is for default Olingo implementation an
			// InputStream
			InputStream entity = response.getEntityAsStream();

			byte[] sendBuffer = IOUtils.toByteArray(entity);
			entity.close();
			String upsertContent = new String(sendBuffer, "UTF-8");

			upsertContent = "{\"__metadata\": {\"uri\": \"" + entitySetName + "\"}," + upsertContent.substring(1);

			logger.debug("Sending POST to Odata upsert: {}", upsertContent);

			String serverResponse = "";
			try (CloseableHttpClient client = HttpClients.custom().build()) {
				HttpEntityEnclosingRequestBase request;

				request = new HttpPost(absolutUri);

				setAuthenticationHeader(absolutUri, request, backgroundUser);

				request.setEntity(new StringEntity(upsertContent, "UTF-8"));
				request.setHeader("Accept", APPLICATION_JSON);
				request.setHeader("Cache-Control", "no-cache");
				request.setHeader("Content-Type", APPLICATION_JSON);
				logger.debug("Connecting to OData with query: {}", absolutUri);
				HttpResponse postResponse = client.execute(request);

				if (postResponse.getStatusLine().getStatusCode() == 200) {
					logger.debug("200 from server Upsert call was successful, check if body contains error message");

					Gson gson = GsonHelper.adaptedGson;
					InputStream content = postResponse.getEntity().getContent();
					byte[] responseBuffer = IOUtils.toByteArray(content);
					serverResponse = new String(responseBuffer, "UTF-8");
					logger.debug("Response from OData server : {}", serverResponse);

					UpsertResponse upsertResponse = gson.fromJson(serverResponse, UpsertResponse.class);

					if (upsertResponse != null && upsertResponse.getD() != null && !upsertResponse.getD().isEmpty()) {
						UpsertResponseDetail detail = upsertResponse.getD().get(0);

						if (detail.getStatus().equals("ERROR")) {
							// uh oh something gone wrong
							logger.error("Upsert error: {}", detail.getMessage());
							throw new ODataException(detail.getMessage());
						}
					}

					return null;
				}

				if (postResponse.getStatusLine().getStatusCode() != 204) {
					InputStream content = postResponse.getEntity().getContent();
					byte[] responseBuffer = IOUtils.toByteArray(content);
					serverResponse = new String(responseBuffer, "UTF-8");
					logger.error("Non 200 or 204 status from server on POST/PUT - probable error: {}", serverResponse);
					logger.error("Request that caused error: {}\n Sending upsert to Odata: {}" + absolutUri, upsertContent);
					throw new ODataException();
				} else {
					logger.debug("204 from server upsert call was successful but returned no body");
				}

			} catch (IOException e) {
				logger.error("Error connecting to OData - using upsert {} with content {} and returning response: {}", absolutUri,
						upsertContent, serverResponse, e);

			}
			return null;
		} catch (EntityProviderException e) {
			logger.error("Error forming OData - using upsert {} with content {}", entitySetName, data.toString(), e);
			throw e;
		}
	}

	private ODataEntry writeEntity(String absolutUri, String entitySetName, Map<String, Object> data, Map<String, String> deepData,
			String contentType, String httpMethod, boolean backgroundUser)
			throws URISyntaxException, IOException, NamingException, ODataException {

		ODataEntry entry;
		TenantConnectionDetails tenantConnectionDetails = detailsForTenant.get(getDestinationId(backgroundUser));
		EdmEntityContainer entityContainer;
		EdmEntitySet entitySet;
		URI rootUri;
		ExpandSelectTreeNode node;
		try {
			entityContainer = tenantConnectionDetails.getEdm().getDefaultEntityContainer();
			entitySet = entityContainer.getEntitySet(entitySetName);
			rootUri = new URI(absolutUri.substring(0, absolutUri.lastIndexOf("/") + 1));

			if (deepData != null) {
				node = ExpandSelectTreeNode.entitySet(entitySet).selectedProperties(new ArrayList<String>(data.keySet()))
						.selectedLinks(new ArrayList<String>(deepData.keySet())).build();
			} else {
				node = ExpandSelectTreeNode.entitySet(entitySet).selectedProperties(new ArrayList<String>(data.keySet())).build();
			}
		} catch (EdmException e) {
			// try refreshing edm may have added a new field

			tenantConnectionDetails.setEdm(null);
			getEdm(backgroundUser);
			entityContainer = tenantConnectionDetails.getEdm().getDefaultEntityContainer();
			entitySet = entityContainer.getEntitySet(entitySetName);
			rootUri = new URI(absolutUri.substring(0, absolutUri.lastIndexOf("/") + 1));

			node = ExpandSelectTreeNode.entitySet(entitySet).selectedProperties(new ArrayList<String>(data.keySet())).build();

		}

		EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(rootUri).omitJsonWrapper(true)
				.contentOnly(true).expandSelectTree(node).build();

		try {
			// serialize data into ODataResponse object
			ODataResponse response = EntityProvider.writeEntry(contentType, entitySet, data, properties);

			// get (http) entity which is for default Olingo implementation an
			// InputStream
			InputStream entity = response.getEntityAsStream();

			byte[] sendBuffer = IOUtils.toByteArray(entity);
			entity.close();
			// just for logging
			String updateContent = new String(sendBuffer, "UTF-8");

			if (deepData != null && !deepData.isEmpty()) {
				// add deep data
				StringBuffer withDeep = new StringBuffer();
				withDeep.append("{");
				boolean firsttime = true;
				for (Map.Entry<String, String> deep : deepData.entrySet()) {
					if (!firsttime) {
						withDeep.append(",");
					}
					firsttime = false;
					withDeep.append("\"").append(deep.getKey()).append("\":").append(deep.getValue());
				}
				updateContent = withDeep.toString() + "," + updateContent.substring(1);
			}

			int retryCount = 0;
			int maxTries = 5;
			boolean retry = true;
			String serverResponse = "";
			while (retry) {
				try {
					logger.debug("Sending {} to Odata: {}", httpMethod, updateContent);
					try (CloseableHttpClient client = HttpClients.custom().build()) {
						HttpEntityEnclosingRequestBase request;
						switch (httpMethod) {
						default:
						case HTTP_METHOD_POST:
							request = new HttpPost(absolutUri);
							break;

						case HTTP_METHOD_POST_MERGE:
							request = new HttpPost(absolutUri);
							request.setHeader("X-HTTP-Method", "MERGE");
							break;

						case HTTP_METHOD_PATCH:
							request = new HttpPatch(absolutUri);
							break;

						case HTTP_METHOD_PUT:
							request = new HttpPut(absolutUri);
							break;
						}

						setAuthenticationHeader(absolutUri, request, backgroundUser);

						request.setEntity(new StringEntity(updateContent, "UTF-8"));
						request.setHeader("Accept", contentType);
						request.setHeader("Cache-Control", "no-cache");
						request.setHeader("Content-Type", contentType);
						logger.debug("Connecting to OData with query: {}", absolutUri);
						HttpResponse postResponse = client.execute(request);

						if (postResponse.getStatusLine().getStatusCode() == 200) {
							logger.debug("200 from server {} call was successful", httpMethod);
							return null;
						}

						if (postResponse.getStatusLine().getStatusCode() == 201) {
							InputStream content = postResponse.getEntity().getContent();
							byte[] responseBuffer = IOUtils.toByteArray(content);
							InputStream responseContent = new ByteArrayInputStream(responseBuffer);
							serverResponse = new String(responseBuffer, "UTF-8");
							logger.debug("Response from OData server : {}", serverResponse);

							entry = EntityProvider.readEntry(contentType, entitySet, responseContent,
									EntityProviderReadProperties.init().build());

							return entry;
						}
						if (postResponse.getStatusLine().getStatusCode() != 204) {
							InputStream content = postResponse.getEntity().getContent();
							byte[] responseBuffer = IOUtils.toByteArray(content);
							serverResponse = new String(responseBuffer, "UTF-8");
							logger.error("Non 201 or 204 status from server on POST/PUT - probable error: {}", serverResponse);
							logger.error("Request that caused error: {}\n Sending {} to Odata: {}" + absolutUri, httpMethod, updateContent);
							String errorMessage = null;
							Gson gson = GsonHelper.adaptedGson;
							try {
								PostErrorResponse errorResponse = gson.fromJson(serverResponse, PostErrorResponse.class);
								if (errorResponse != null && errorResponse.getError() != null
										&& errorResponse.getError().getMessage() != null
										&& errorResponse.getError().getMessage().getValue() != null) {
									errorMessage = errorResponse.getError().getMessage().getValue();
								} else {
									errorMessage = "Problem updating SuccessFactors";
									logger.error("problem parsing error message {}", serverResponse);
								}
							} catch (JsonSyntaxException e) {
								logger.error("Could not parse error message");
								errorMessage = "Problem updating SuccessFactors";
							}

							throw new ODataException(errorMessage);
						} else {
							logger.debug("204 from server {} call was successful but returned no body", httpMethod);
						}

					} catch (IOException e) {
						logger.error("Error connecting to OData - using {} {} with content {} and returning response: {}", httpMethod,
								absolutUri, updateContent, serverResponse, e);

					}
					retry = false;
				} catch (EntityProviderException e) {
					if (e.getCause() != null && e.getCause().getClass().equals(MalformedJsonException.class)) {
						logger.error("Error. SuccessFactors sent malformed JSON {}", serverResponse);
						if (++retryCount == maxTries) {
							retry = false;
						} else {
							logger.warn("Retry number {}. Sending update to SuccessFactors: {}", retryCount, absolutUri);
						}
					} else {
						throw e;
					}
				}
			}

			return null;
		} catch (EntityProviderException e) {
			logger.error("Error forming OData - using {} {} with content {}", contentType, entitySetName, data.toString(), e);
			throw e;
		}
	}

	protected InputStream execute(String relativeUri, String contentType, String httpMethod, boolean background) throws IOException {

		String serverResponse = "";
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpRequestBase request;

			switch (httpMethod) {
			case HTTP_METHOD_DELETE:
				request = new HttpDelete(relativeUri);
				break;
			case HTTP_METHOD_GET:
			default:
				request = new HttpGet(relativeUri);
				break;
			}

			request.setHeader("Accept", contentType);

			setAuthenticationHeader(relativeUri, request, background);

			logger.debug("Connecting to OData with query:" + httpMethod + " - " + relativeUri);

			HttpResponse response = client.execute(request);

			InputStream content = response.getEntity().getContent();
			byte[] buffer = IOUtils.toByteArray(content);
			content.close();
			serverResponse = new String(buffer, "UTF-8");
			logger.debug("Response from OData server :" + serverResponse);
			return new ByteArrayInputStream(buffer);

		} catch (IOException e) {
			logger.error(
					"Error connecting to XS OData - using " + httpMethod + " " + relativeUri + " and returning response: " + serverResponse,
					e);

		}

		return null;
	}

}
