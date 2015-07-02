package uk.nhs.ciao.cda.builder;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

import uk.nhs.ciao.cda.builder.json.TransferOfCareDocument;
import uk.nhs.ciao.docs.parser.Document;
import uk.nhs.ciao.docs.parser.ParsedDocument;
import uk.nhs.interoperability.payloads.exceptions.MissingMandatoryFieldException;
import uk.nhs.interoperability.payloads.noncodedcdav2.ClinicalDocument;

public class JsonToNonCodedCDADocumentTransformer {
	private final ObjectMapper objectMapper;
	
	public JsonToNonCodedCDADocumentTransformer(final ObjectMapper objectMapper) {
		this.objectMapper = Preconditions.checkNotNull(objectMapper);
	}
	
	public ParsedDocument transform(final String json) throws IOException, MissingMandatoryFieldException {
		final TransferOfCareDocument transferOfCareDocument = objectMapper.readValue(json, TransferOfCareDocument.class);
		final ClinicalDocument clinicalDocument = transferOfCareDocument.createClinicalDocument();
		
		final ParsedDocument parsedDocument = objectMapper.readValue(json, ParsedDocument.class);

		// The original properties and filename from the incoming JSON are maintained in the outgoing document
		final String name = parsedDocument.getOriginalDocument().getName();
		final Map<String, Object> properties = parsedDocument.getProperties();
		
		return asParsedDocument(name, clinicalDocument, properties);
	}
	
	/**
	 * Creates a new ParsedDocument using an encoded clinical document as the payload
	 * and the specified properties
	 */
	private ParsedDocument asParsedDocument(final String name, final ClinicalDocument clinicalDocument,
			final Map<String, Object> properties) {
		final byte[] bytes = clinicalDocument.serialise().getBytes();
		final Document document = new Document(name, bytes, "text/xml");
		
		return new ParsedDocument(document, properties);
	}
}
