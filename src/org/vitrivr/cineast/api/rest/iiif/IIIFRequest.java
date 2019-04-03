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
    private ArrayList<Content> content;
    // error
    private ObjectNode requestError;
    private ObjectNode processError;
    // getters
    public String getInstitution() {return institution;}
    public String getEndpoint() {return endpoint;}
    public ArrayList<Content> getContent() {return content;}
    public ObjectNode getRequestError() {return requestError;}
    // setters
    public void setInstitution(String _institution) {institution = _institution;}
    public void setEndpoint(String _endpoint) {endpoint = _endpoint;}
    public void setRequestError(ObjectNode _requestError) {requestError = _requestError;}
    // add content
    public void addContent(String _project, String _image, String _meta) {
        content.add(new Content(_project, _image, _meta));
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



class Content {
    private String project;
    private String image;
    private String meta;

    public Content(String _project, String _image, String _meta) {
        project = _project;
        image = _image;
        meta = _meta;
    }

    public String getProject() {return project;}
    public String getImage() {return image;}
    public String getMeta() {return meta;}
}
