package org.vitrivr.cineast.api.rest.iiif;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
    public void addContent(String _scheme, String _server, String _prefix, String _identifier) {
        content.add(new IIIFObject(_scheme, _server, _prefix, _identifier));
    }
    // constructor
    public IIIFRequest() {
        content = new ArrayList<>();
    }





    // check somewhere what data is already in db
    // ditch invalid links? validate all urls in time for a response...


    // get images and start exporter
    public ObjectNode process() {

        // foo.resolve(this)
        // bar.extract(whatever)


        return null;
    }









}



class IIIFObject {
    private String scheme;
    private String server;
    private String prefix;
    private String identifier;

    public IIIFObject(String _scheme, String _server, String _prefix, String _identifier) {
        scheme = _scheme;
        server = _server;
        prefix = _prefix;
        identifier = _identifier;
        System.out.println(getBaseURI());
    }

    public String getScheme() { return scheme; }
    public String getServer() { return server; }
    public String getPrefix() { return prefix; }
    public String getIdentifier() { return identifier; }

    public String getBaseURI() {
        // RFC6570
        // {scheme}://{server}{/prefix}/{identifier}
        return scheme + "://" + server + prefix + "/" + identifier;
    }
}
