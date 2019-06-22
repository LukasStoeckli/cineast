package org.vitrivr.cineast.api.rest.iiif;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.SessionExtractionContainer;
import org.vitrivr.cineast.core.run.ExtractionItemContainer;

import java.io.File;
import java.util.ArrayList;


/**
 * processor singleton, that downloads images, builds container and passes them to the extractor
 * also holds status of requests
 */
public class IIIFProcessor {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String IMGDIR = "/tmp/vitrivr/"; // move to config

    private static ArrayList<IIIFRequest> queue;
    private static int processID;

    static {
        processID = 0;
        queue = new ArrayList<>();
        System.setProperty("http.agent", "vitrivr");
    }



    public static int enqueue(IIIFRequest _iiif) {
        // create base directory
        File folder = new File(IMGDIR);
        if (!folder.exists()) {
            folder.mkdirs();
            LOGGER.debug("create base directory for file download = {}", folder.toString());
        }

        // add paths to SessionExtractionContainer
        ExtractionItemContainer[] items = _iiif.getContent().toArray(new ExtractionItemContainer[0]);
        SessionExtractionContainer.addPaths(items);

        // add request to queue
        _iiif.setProcessID(processID);
        queue.add(_iiif);
        return processID++;
    }


    public static IIIFRequest[] getProcessQueue() {
        return queue.toArray(new IIIFRequest[0]);
    }
}
