package org.vitrivr.cineast.api.rest.iiif;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.presets.opencv_core;
import org.vitrivr.cineast.api.SessionExtractionContainer;
import org.vitrivr.cineast.core.run.ExtractionItemContainer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * processor singleton, that downloads images, builds container and passes them to the extractor
 * also holds status of requests
 */
public class IIIFProcessor implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger();

    static {

    }


    @Override
    public void run() {

    }




    public static int enqueue(IIIFRequest _iiif) {


        // download images

        // maybe set maxsize of request array
        // or do it somehow dynamicly, downloading and deleting on need basis



        ExtractionItemContainer[] items = download(_iiif);




        // add paths to SessionExtractionContainer

        SessionExtractionContainer.addPaths(items);


        return 42;
    }


    private static ExtractionItemContainer[] download(IIIFRequest _iiif) {
        // create base directory
        final String IMGDIR = "/tmp/vitrivr/"; // move to config
        File folder = new File(IMGDIR + _iiif.getInstitution());
        if (!folder.exists()) { folder.mkdirs(); }
        LOGGER.debug("institution directory = {}", folder.toString());




        // check if images already in db



        for (IIIFObject object: _iiif.getContent()) {
            // create prefix directory
            folder = new File(IMGDIR + _iiif.getInstitution() + object.getPrefix());
            if (!folder.exists()) { folder.mkdirs(); }
            LOGGER.debug("prefix directory = {}", folder.toString());


            // retrieve info.json for available formats


            // build iiif image api url
            String imageURL = object.getBaseURI() + "/full/full/0/default.jpg"; // move to config or api
            String imageFile = IMGDIR + _iiif.getInstitution() + object.getPrefix() + "/" + object.getIdentifier();

            LOGGER.debug("imageURL = {}", imageURL);
            LOGGER.debug("imageFile = {}", imageFile);
            LOGGER.debug("prefix = {}", object.getPrefix());
            LOGGER.debug("baseURI = {}", object.getBaseURI());

            try(InputStream in = new URL(imageURL).openStream()){
                Files.copy(in, Paths.get(imageFile));



                // if success, build mediaaObjectDescriptor & MediaObjectMetaDataDescriptor
                // and add elements to ExtractionItemContainer




            } catch (MalformedURLException e) {
                LOGGER.error("Malformed URL for {}. SKipping object. {}", object.getBaseURI(), e.getMessage());
            } catch (IOException e) {
                LOGGER.error("Could not retrieve {}. Skipping object. {}", object.getBaseURI(), e.getMessage());
            }
        }



        ExtractionItemContainer[] items = null;



        return items;
    }
}
