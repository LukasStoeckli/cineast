package org.vitrivr.cineast.api.rest.iiif;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.SessionExtractionContainer;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.run.ExtractionItemContainer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;


/**
 * processor singleton, that downloads images, builds container and passes them to the extractor
 * also holds status of requests
 */
public class IIIFProcessor implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String IMGDIR = "/tmp/vitrivr/"; // move to config

    static {


        System.setProperty("http.agent", "vitrivr");
    }

    private static int processID = 0;

    @Override
    public void run() {

    }






    public static int enqueue(IIIFRequest _iiif) {


        // add to some arraylist or queue


        ExtractionItemContainer[] items = download(_iiif);




        // add paths to SessionExtractionContainer

        SessionExtractionContainer.addPaths(items);


        return processID++;
    }


    private static ExtractionItemContainer[] download(IIIFRequest _iiif) {
        // create base directory
        File folder = new File(IMGDIR);
        if (!folder.exists()) { folder.mkdirs(); }
        LOGGER.debug("create base directory for file download = {}", folder.toString());




        // check if images already in db

        // also make some buffer, to not overload filesystem
        // must be synced with extraction thread



        ArrayList<ExtractionItemContainer> items = new ArrayList<>();



        for (IIIFObject object: _iiif.getContent()) {

            // retrieve info.json for available formats

            // paths does not support http/https!!!!!!!
            MediaObjectDescriptor mediaDescriptor = new MediaObjectDescriptor(Paths.get(object.getBaseURI()));

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            MediaObjectMetadataDescriptor[] mediaMetaDescriptor = new MediaObjectMetadataDescriptor[2];
            mediaMetaDescriptor[0] = MediaObjectMetadataDescriptor.of(mediaDescriptor.getObjectId(), "iiif", "institution", _iiif.getInstitution());
            mediaMetaDescriptor[1] = MediaObjectMetadataDescriptor.of(mediaDescriptor.getObjectId(), "iiif", "createdAt", timestamp.toString());

            Path path = Paths.get("iiif.jpg");

            items.add(new ExtractionItemContainer(mediaDescriptor, mediaMetaDescriptor, path));
        }





        return items.toArray(new ExtractionItemContainer[0]);
    }

    public static IIIFRequest[] getProcessQueue() {

        // return the list of active processes / requests


        // iiifRequest needs fields for status of request, percentage done, status of single objects


        return new IIIFRequest[5];
    }
}
