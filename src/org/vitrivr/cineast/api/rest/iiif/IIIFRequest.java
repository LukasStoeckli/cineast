package org.vitrivr.cineast.api.rest.iiif;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bytedeco.javacpp.presets.opencv_core;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.run.ExtractionItemContainer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Created by Lukas on 02.04.19.
 */
public class IIIFRequest {
    // request info
    private String institution;
    private ArrayList<ExtractionItemContainer> content;
    private int processID;
    // error
    private ObjectNode requestError;
    private ArrayList<String> errors;
    // getters
    public String getInstitution() {return institution;}
    public ArrayList<ExtractionItemContainer> getContent() {return content;}
    public ObjectNode getRequestError() {return requestError;}
    public int getProcessID() { return processID; }
    // setters
    public void setInstitution(String _institution) {institution = _institution;}
    public void setRequestError(ObjectNode _requestError) {requestError = _requestError;}
    public void setProcessID(int _processID) { processID = _processID; }
    // add content
    public void addContent(String _baseURI, String _collection, String _manifest) {
        MediaObjectDescriptor mediaDescriptor = new MediaObjectDescriptor(_baseURI);

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        MediaObjectMetadataDescriptor[] mediaMetaDescriptor = new MediaObjectMetadataDescriptor[4];
        mediaMetaDescriptor[0] = MediaObjectMetadataDescriptor.of(mediaDescriptor.getObjectId(), "iiif", "institution", institution);
        mediaMetaDescriptor[1] = MediaObjectMetadataDescriptor.of(mediaDescriptor.getObjectId(), "iiif", "collection", _collection);
        mediaMetaDescriptor[2] = MediaObjectMetadataDescriptor.of(mediaDescriptor.getObjectId(), "iiif", "extractedAt", timestamp.toString());
        if (_manifest.equals("")) {
            mediaMetaDescriptor[3] = MediaObjectMetadataDescriptor.of(mediaDescriptor.getObjectId(), "iiif", "manifest", "unknown");
        } else {
            mediaMetaDescriptor[3] = MediaObjectMetadataDescriptor.of(mediaDescriptor.getObjectId(), "iiif", "manifest", _manifest);
        }

        Path path = Paths.get("iiif.jpg");

        ExtractionItemContainer container = new ExtractionItemContainer(mediaDescriptor, mediaMetaDescriptor, path);
        container.setIIIF(true);
        container.setStatus("pending");

        content.add(container);
    }
    // constructor
    public IIIFRequest() {
        content = new ArrayList<>();
    }

    public String getStatus() {

        int pending = 0;
        int done = 0;
        int failed = 0;
        int total = content.size();
        errors = new ArrayList<>();


        for (ExtractionItemContainer container: content) {
            String status = container.getStatus();
            if (status.equals("pending")) {
                pending++;
            } else if (status.equals("done")) {
                done++;
            } else if (status.startsWith("failed: ")) {
                failed++;
                errors.add(status.substring("failed: ".length()));
            } else if (status.equals("extracting")) {
                // do nothing
            } else if (status.equals("downloading")) {
                // do nothing
            } else {
                // log
                return "unknown: " + status;
            }
        }

        if (pending == total) { return "pending"; }
        if (done + failed == total) { return "done: added " + done + " resources"; }
        if (pending >= 0) { return "running: " + (done + failed) + " / " + total + " done"; }

        return "this is fine";
    }

    public String[] getErrors() {
        return errors.toArray(new String[0]);
    }
}
