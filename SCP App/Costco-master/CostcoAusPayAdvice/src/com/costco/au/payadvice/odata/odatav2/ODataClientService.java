package com.costco.au.payadvice.odata.odatav2;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataException;

import com.costco.au.payadvice.odata.odatav2.pojo.ODataFunctionImportParameter;
import com.google.gson.JsonObject;

/**
 * This interface provides methods to efficiently handles OData services
 */
public interface ODataClientService {

	/**
	 * Read OData feed
	 *
	 * @return {@link ODataFeed}
	 * @param entitySetName
	 *            Entityset Name
	 * @param expand
	 *            $expand query option
	 * @param filter
	 *            $filter query option
	 * @param selectFields
	 *            $select query option
	 * @throws IOException
	 *             I/O operations failed
	 * @throws ODataException
	 *             OData error
	 * @throws NamingException
	 *             Naming error
	 */
	ODataFeed readFeed(String entitySetName, String expand, String filter, String selectFields)
			throws IOException, ODataException, NamingException;

	/**
	 * Read OData feed
	 *
	 * @return {@link ODataFeed}
	 * @param entitySetName
	 *            Entityset Name
	 * @param expand
	 *            $expand query option
	 * @param filter
	 *            $filter query option
	 * @param selectFields
	 *            $select query option
	 * @param asOfDate
	 *            As of date
	 * @throws IOException
	 *             I/O operations failed
	 * @throws ODataException
	 *             OData error
	 * @throws NamingException
	 *             Naming error
	 */
	ODataFeed readFeed(String entitySetName, String expand, String filter, String selectFields, Date asOfDate)
			throws IOException, ODataException, NamingException;

	/**
	 * Read OData feed
	 *
	 * @return {@link ODataFeed}
	 * @param entitySetName
	 *            Entityset Name
	 * @param expand
	 *            $expand query option
	 * @param filter
	 *            $filter query option
	 * @param selectFields
	 *            $select query option
	 * @param orderBy
	 *            $orderby query option
	 * @param numberResults
	 *            Number of results
	 * @throws IOException
	 *             I/O operations failed
	 * @throws ODataException
	 *             OData error
	 * @throws NamingException
	 *             Naming error
	 */
	ODataFeed readFeed(String entitySetName, String expand, String filter, String selectFields, String orderBy, int numberResults)
			throws IOException, ODataException, NamingException;

	/**
	 * Count OData feed
	 *
	 * @return Number of feed
	 * @param entitySetName
	 *            Entityset Name
	 * @param filter
	 *            $filter Query option
	 * @param backgroundUser
	 *            $boolean Flag as background user
	 * @throws EdmException
	 *             Entity Data Model error
	 * @throws IOException
	 *             I/O operations failed
	 * @throws EntityProviderException
	 *             Entity Provider error
	 * @throws NamingException
	 *             Naming error
	 */
	Long countFeed(String entitySetName, String filter, boolean backgroundUser) throws EdmException, IOException, EntityProviderException, NamingException;

	/**
	 * Count OData feed
	 *
	 * @return Number of feed
	 * @param entitySetName
	 *            Entityset Name
	 * @param filter
	 *            $filter query option
	 * @param asOfDate
	 *            As of date
	 * @param backgroundUser
	 *            $boolean Flag as background user            
	 * @throws EdmException
	 *             Entity Data Model error
	 * @throws IOException
	 *             I/O operations failed
	 * @throws EntityProviderException
	 *             Entity Provider error
	 * @throws NamingException
	 *             Naming error
	 */
	Long countFeed(String entitySetName, String filter, Date asOfDate, boolean backgroundUser)
			throws EdmException, IOException, EntityProviderException, NamingException;

	/**
	 * Read OData feed
	 *
	 * @return {@link ODataFeed}
	 * @param entitySetName
	 *            Entityset Name
	 * @param expand
	 *            $expand query option
	 * @param filter
	 *            $filter query option
	 * @param selectFields
	 *            $select query option
	 * @param orderBy
	 *            $orderby query option
	 * @param numberResults
	 *            Number of results
	 * @param count
	 *            $count query option
	 * @throws IOException
	 *             I/O operations failed
	 * @throws ODataException
	 *             OData error
	 * @throws NamingException
	 *             Naming error
	 */
	ODataFeed readFeed(String entitySetName, String expand, String filter, String selectFields, String orderBy, int numberResults,
			boolean count) throws IOException, ODataException, NamingException;

	/**
	 * Read OData feed
	 *
	 * @return {@link ODataFeed}
	 * @param entitySetName
	 *            Entityset Name
	 * @param expand
	 *            $expand query option
	 * @param filter
	 *            $filter query option
	 * @param selectFields
	 *            $select query option
	 * @param orderBy
	 *            $orderby query option
	 * @param numberResults
	 *            Number of results
	 * @param count
	 *            $count query option
	 * @param readAtDate
	 *            Read at date
	 * @throws IOException
	 *             I/O operations failed
	 * @throws ODataException
	 *             OData error
	 * @throws NamingException
	 *             Naming error
	 */
	ODataFeed readFeed(String entitySetName, String expand, String filter, String selectFields, String orderBy, int numberResults,
			boolean count, Date readAtDate) throws IOException, ODataException, NamingException;

	/**
	 * Update OData entry
	 *
	 * @return {@link ODataEntry}
	 * @param id
	 *            Id
	 * @param entitySetName
	 *            Entityset Name
	 * @param data
	 *            {@link Map} collection of data
	 * @param deepData
	 *            {@link Map} collection of data
	 * @throws URISyntaxException
	 *             URI syntax error
	 * @throws IOException
	 *             I/O operations failed
	 * @throws ODataException
	 *             OData error
	 * @throws NamingException
	 *             Naming error
	 */
	ODataEntry updateEntry(String id, String entitySetName, Map<String, Object> data, Map<String, String> deepData)
			throws URISyntaxException, IOException, NamingException, ODataException;

	/**
	 * Upsert OData entry
	 *
	 * @return {@link ODataEntry}
	 * @param id
	 *            Id
	 * @param entitySetName
	 *            Entityset Name
	 * @param data
	 *            {@link Map} collection of data
	 * @throws URISyntaxException
	 *             URI syntax error
	 * @throws IOException
	 *             I/O operations failed
	 * @throws ODataException
	 *             OData error
	 * @throws NamingException
	 *             Naming error
	 */
	ODataEntry upsertEntry(String id, String entitySetName, Map<String, Object> data)
			throws URISyntaxException, IOException, NamingException, ODataException;

	/**
	 * Patch OData entry
	 *
	 * @return {@link ODataEntry}
	 * @param id
	 *            Id
	 * @param entitySetName
	 *            Entityset Name
	 * @param data
	 *            {@link Map} collection of data
	 * @throws URISyntaxException
	 *             URI syntax error
	 * @throws IOException
	 *             I/O operations failed
	 * @throws ODataException
	 *             OData error
	 * @throws NamingException
	 *             Naming error
	 */
	ODataEntry patchEntry(String id, String entitySetName, Map<String, Object> data)
			throws IOException, NamingException, URISyntaxException, ODataException;

	/**
	 * Read OData entry
	 *
	 * @return {@link ODataEntry}
	 * @param entitySetName
	 *            Entityset Name
	 * @param keyValue
	 *            Key value
	 * @param expandRelationName
	 *            $expand query option
	 * @param selectFields
	 *            $select query option
	 * @throws IOException
	 *             I/O operations failed
	 * @throws ODataException
	 *             OData error
	 * @throws NamingException
	 *             Naming error
	 */
	ODataEntry readEntry(String entitySetName, String keyValue, String expandRelationName, String selectFields)
			throws IOException, ODataException, NamingException;

	/**
	 * Delete OData entry
	 *
	 * @param id
	 *            Id
	 * @param entitySetName
	 *            Entityset Name
	 * @throws IOException
	 *             I/O operations failed
	 * @throws ODataException
	 *             OData error
	 * @throws NamingException
	 *             Naming error
	 */
	void deleteEntry(String id, String entitySetName) throws IOException, ODataException, NamingException;

	/**
	 * Read OData function import
	 *
	 * @return {@link Object}
	 * @param entitySetName
	 *            Entityset Name
	 * @param expand
	 *            $expand query option
	 * @param filter
	 *            $filter query option
	 * @param selectFields
	 *            $select query option
	 * @throws IOException
	 *             I/O operations failed
	 * @throws ODataException
	 *             OData error
	 * @throws NamingException
	 *             Naming error
	 */
	Object readFunctionImportData(String entitySetName, String expand, String filter, String selectFields)
			throws IOException, ODataException, NamingException;

	/**
	 * Read OData function import (Background User)
	 *
	 * @return {@link Object}
	 * @param entitySetName
	 *            Entityset Name
	 * @param expand
	 *            $expand query option
	 * @param filter
	 *            $filter query option
	 * @param selectFields
	 *            $select query option
	 * @throws IOException
	 *             I/O operations failed
	 * @throws ODataException
	 *             OData error
	 * @throws NamingException
	 *             Naming error
	 */
	Object backgroundReadFunctionImportData(String entitySetName, String expand, String filter, String selectFields)
			throws IOException, ODataException, NamingException;

	/**
	 * Create OData entry
	 *
	 * @return {@link ODataEntry}
	 * @param entitySetName
	 *            Entityset Name
	 * @param data
	 *            {@link Map} collection of data
	 * @param deepData
	 *            {@link Map} collection of data
	 * @throws URISyntaxException
	 *             URI syntax error
	 * @throws IOException
	 *             I/O operations failed
	 * @throws ODataException
	 *             OData error
	 * @throws NamingException
	 *             Naming error
	 */
	ODataEntry createEntry(String entitySetName, Map<String, Object> data, Map<String, String> deepData)
			throws URISyntaxException, IOException, NamingException, ODataException;

	/**
	 * Read OData feed
	 *
	 * @return {@link ODataFeed}
	 * @param entitySetName
	 *            Entityset Name
	 * @param expand
	 *            $expand query option
	 * @param filter
	 *            $filter query option
	 * @param selectFields
	 *            $select query option
	 * @param fromDate
	 *            Query from date
	 * @param toDate
	 *            Query to date
	 * @throws IOException
	 *             I/O operations failed
	 * @throws ODataException
	 *             OData error
	 * @throws NamingException
	 *             Naming error
	 */
	ODataFeed readFeed(String entitySetName, String expand, String filter, String selectFields, Date fromDate, Date toDate)
			throws IOException, ODataException, NamingException;

	/**
	 * Read OData feed (Background User)
	 *
	 * @return {@link ODataFeed}
	 * @param entitySetName
	 *            Entityset Name
	 * @param expand
	 *            $expand query option
	 * @param filter
	 *            $filter query option
	 * @param selectFields
	 *            $select query option
	 * @throws IOException
	 *             I/O operations failed
	 * @throws ODataException
	 *             OData error
	 * @throws NamingException
	 *             Naming error
	 */
	ODataFeed backgroundReadFeed(String entitySetName, String expand, String filter, String selectFields)
			throws IOException, ODataException, NamingException;

	/**
	 * Read OData entry
	 *
	 * @return {@link ODataEntry}
	 * @param string
	 *            Entityset Name
	 * @param positionId
	 *            Position Id
	 * @param allExpandFields
	 *            $expand query option
	 * @param allSelectFields
	 *            $select query option
	 * @param fromDate
	 *            From date
	 * @param toDate
	 *            to Date
	 * @throws IOException
	 *             I/O operations failed
	 * @throws EntityProviderException
	 *             Entity Provider failed
	 * @throws EdmException
	 *             Edm failed
	 * @throws NamingException
	 *             Naming error
	 */
	ODataEntry readEntry(String string, String positionId, String allExpandFields, String allSelectFields, Date fromDate, Date toDate)
			throws IOException, EntityProviderException, EdmException, NamingException;

	/**
	 * Read OData entry with background user
	 *
	 * @return {@link ODataEntry}
	 * @param entitySetName
	 *            Entityset Name
	 * @param keyValue
	 *            Key value
	 * @param expandRelationName
	 *            $expand query option
	 * @param selectFields
	 *            $select query option
	 * @throws IOException
	 *             I/O operations failed
	 * @throws ODataException
	 *             OData error
	 * @throws NamingException
	 *             Naming error
	 */
	ODataEntry backgroundReadEntry(String entitySetName, String keyValue, String expandRelationName, String selectFields)
			throws IOException, ODataException, NamingException;

	/**
	 * Sort OData entries
	 *
	 * @return List {@link ODataEntry}
	 * @param entries
	 *            List of {@link ODataEntry}
	 */
	List<ODataEntry> sort(List<ODataEntry> entries);

	/**
	 * Update OData entry using a post with X-HTTP-Method: MERGE
	 *
	 * @return {@link ODataEntry}
	 * @param id
	 *            Id
	 * @param entitySetName
	 *            Entityset Name
	 * @param data
	 *            {@link Map} collection of data
	 * @param deepData
	 *            {@link Map} collection of data
	 * @throws URISyntaxException
	 *             URI syntax error
	 * @throws IOException
	 *             I/O operations failed
	 * @throws ODataException
	 *             OData error
	 * @throws NamingException
	 *             Naming error
	 */
	ODataEntry mergeEntry(String id, String entitySetName, Map<String, Object> data, Map<String, String> deepData)
			throws URISyntaxException, IOException, NamingException, ODataException;

	/**
	 * Read function
	 *
	 * @return {@link com.google.gson.JsonObject}
	 * 
	 * 
	 * @param functionName
	 *            Name of function to be called
	 * @param parameters
	 *            {@link List} collection of parameters of (@link
	 *            ODataFunctionImportParameter}
	 * @throws IOException
	 *             I/O operations failed
	 * @throws ODataException
	 *             OData error
	 * @throws NamingException
	 *             Naming error
	 */
	JsonObject readFeed(String functionName, List<ODataFunctionImportParameter> parameters)
			throws IOException, ODataException, NamingException;

	/**
	 * Read function
	 *
	 * @return {@link com.google.gson.JsonObject}
	 * 
	 * @param functionName
	 *            Name of function to be called
	 * @param parameters
	 *            {@link List} collection of parameters of (@link
	 *            ODataFunctionImportParameter}
	 * @throws IOException
	 *             I/O operations failed
	 * @throws ODataException
	 *             OData error
	 * @throws NamingException
	 *             Naming error
	 */
	JsonObject backgroundReadFeed(String functionName, List<ODataFunctionImportParameter> parameters)
			throws IOException, ODataException, NamingException;

	/**
	 * Upsert OData entry
	 *
	 * @return {@link ODataEntry}
	 * @param id
	 *            Id
	 * @param entitySetName
	 *            Entityset Name
	 * @param data
	 *            {@link Map} collection of data
	 * @throws URISyntaxException
	 *             URI syntax error
	 * @throws IOException
	 *             I/O operations failed
	 * @throws ODataException
	 *             OData error
	 * @throws NamingException
	 *             Naming error
	 */
	ODataEntry backgroundUpsertEntry(String id, String entitySetName, Map<String, Object> data)
			throws URISyntaxException, IOException, NamingException, ODataException;

	/**
	 * Upsert OData multiple entry
	 *
	 * @return {@link ODataEntry}
	 * @param id
	 *            Id
	 * @param entitySetName
	 *            Entityset Name
	 * @param data
	 *            {@link List} collection of data
	 * @throws URISyntaxException
	 *             URI syntax error
	 * @throws IOException
	 *             I/O operations failed
	 * @throws ODataException
	 *             OData error
	 * @throws NamingException
	 *             Naming error
	 */
	ODataEntry upsertEntry(String id, String entitySetName, List<Map<String, Object>> data)
			throws URISyntaxException, IOException, NamingException, ODataException;
	
	/**
	 * Upsert OData multiple entry 
	 *
	 * @return {@link ODataEntry}
	 * @param id
	 *            Id
	 * @param entitySetName
	 *            Entityset Name
	 * @param data
	 *            {@link List} collection of data
	 * @throws URISyntaxException
	 *             URI syntax error
	 * @throws IOException
	 *             I/O operations failed
	 * @throws ODataException
	 *             OData error
	 * @throws NamingException
	 *             Naming error
	 */

	ODataEntry backgroundUpsertEntry(String id, String entitySetName, List<Map<String, Object>> data)
			throws URISyntaxException, IOException, NamingException, ODataException;

	ODataEntry backgroundCreateEntry(String entitySetName, Map<String, Object> data, Map<String, String> deepData)
			throws URISyntaxException, IOException, NamingException, ODataException;

}
