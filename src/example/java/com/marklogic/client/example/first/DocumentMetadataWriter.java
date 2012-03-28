package com.marklogic.client.example.first;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.Authentication;
import com.marklogic.client.DocumentIdentifier;
import com.marklogic.client.XMLDocumentManager;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.InputStreamHandle;
import com.marklogic.client.io.DocumentMetadataHandle.Capability;

/**
 * DocumentMetadataWriter illustrates how to write metadata and content to a database document 
 * in a single request.
 */
public class DocumentMetadataWriter {

	public static void main(String[] args) throws IOException {
		Properties props = loadProperties();

		// connection parameters
		String         host     = props.getProperty("example.host");
		int            port     = Integer.parseInt(props.getProperty("example.port"));
		String         user     = props.getProperty("example.writer_user");
		String         password = props.getProperty("example.writer_password");
		Authentication authType = Authentication.valueOf(
				props.getProperty("example.authentication_type").toUpperCase()
				);

		run(host, port, user, password, authType);
	}

	public static void run(String host, int port, String user, String password, Authentication authType)
	throws IOException {
		String filename = "flipper.xml";

		// connect the client
		DatabaseClient client =
			DatabaseClientFactory.connect(host, port, user, password, authType);

		// acquire the content
		InputStream docStream = DocumentMetadataWriter.class.getClassLoader().getResourceAsStream(
			"data"+File.separator+filename);
		if (docStream == null)
			throw new RuntimeException("Could not read document example");

		// create a manager for XML documents
		XMLDocumentManager docMgr = client.newXMLDocumentManager();

		// create an identifier for the document
		DocumentIdentifier docId = client.newDocId("/example/"+filename);

		// create and initialize a handle on the metadata
		DocumentMetadataHandle metadataHandle = new DocumentMetadataHandle();
		metadataHandle.getCollections().addAll("products", "real-estate");
		metadataHandle.getPermissions().add("app-user", Capability.UPDATE, Capability.READ);
		metadataHandle.getProperties().put("reviewed", true);
		metadataHandle.setQuality(1);

		// create a handle on the content
		InputStreamHandle contentHandle = new InputStreamHandle(docStream);
		contentHandle.set(docStream);

		// write the document metadata and content
		docMgr.write(docId, metadataHandle, contentHandle);

		System.out.println("Wrote /example/"+filename+" metadata and content");

		tearDownExample(docMgr, docId);

		// release the client
		client.release();
	}

	// clean up by deleting the document that the example wrote
	public static void tearDownExample(XMLDocumentManager docMgr, DocumentIdentifier docId) {
		docMgr.delete(docId);
	}

	// get the configuration for the example
	public static Properties loadProperties() throws IOException {
		String propsName = "Example.properties";
		InputStream propsStream =
			DocumentMetadataWriter.class.getClassLoader().getResourceAsStream(propsName);
		if (propsStream == null)
			throw new RuntimeException("Could not read example properties");

		Properties props = new Properties();
		props.load(propsStream);

		return props;
	}

}