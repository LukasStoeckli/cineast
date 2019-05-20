package org.vitrivr.cineast.api.rest.iiif;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;

/**
 * Created by Lukas on 02.04.19.
 */
public class IIIFRequest {
    // request info
    private String institution;
    private String endpoint;
    private ArrayList<IIIFObject> content;
    // error
    private ObjectNode requestError;
    private ObjectNode processError;
    // getters
    public String getInstitution() {return institution;}
    public String getEndpoint() {return endpoint;}
    public ArrayList<IIIFObject> getContent() {return content;}
    public ObjectNode getRequestError() {return requestError;}
    // setters
    public void setInstitution(String _institution) {institution = _institution;}
    public void setEndpoint(String _endpoint) {endpoint = _endpoint;}
    public void setRequestError(ObjectNode _requestError) {requestError = _requestError;}
    // add content
    public void addContent(String _baseURI, String _collection, String _manifest) {
        content.add(new IIIFObject(_baseURI, _collection, _manifest));
    }
    // constructor
    public IIIFRequest() {
        content = new ArrayList<>();
    }
}



class IIIFObject {
    private String baseURI;
    private String collection;
    private String manifest;

    public IIIFObject(String _baseURI, String _collection, String _manifest) {
        baseURI = _baseURI;
        collection = _collection;
        manifest = _manifest;
    }

    public String getBaseURI() {
        // RFC6570
        // {scheme}://{server}{/prefix}/{identifier}
        return baseURI;
    }

    public String getCollection() { return collection; }
    public String getManifest() { return manifest; }
}
