package org.vitrivr.cineast.api.rest.iiif;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.SessionExtractionContainer;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.run.ExtractionItemContainer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


/**
 * processor singleton, that downloads images, builds container and passes them to the extractor
 * also holds status of requests
 */
public class IIIFProcessor implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String IMGDIR = "/tmp/vitrivr/"; // move to config

    static {
        // create base directory
        File folder = new File(IMGDIR);
        if (!folder.exists()) { folder.mkdirs(); }
        LOGGER.debug("create base directory for file download = {}", folder.toString());


        System.setProperty("http.agent", "vitrivr");
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
        LOGGER.debug("create institution directory = {}", folder.toString());




        // check if images already in db

        // also make some buffer, to not overload filesystem
        // must be synced with extraction thread



        ArrayList<ExtractionItemContainer> items = new ArrayList<>();



        for (IIIFObject object: _iiif.getContent()) {
            // create prefix directory
            folder = new File(IMGDIR + _iiif.getInstitution() + object.getPrefix());
            if (!folder.exists()) { folder.mkdirs(); }
            LOGGER.debug("create prefix directory = {}", folder.toString());


            // retrieve info.json for available formats


            // build iiif image api url
            // use max size from config
            String imageURL = object.getBaseURI() + "/full/full/0/default.jpg"; // move to config or api
            String imageFile = IMGDIR + _iiif.getInstitution() + object.getPrefix() + "/" + object.getIdentifier();


            // just for debug purposes, make better
            // some identifiers have file extension, but probably add file extension from iiif image api anyway
            imageFile += ".jpg";

            LOGGER.debug("imageURL = {}", imageURL);
            LOGGER.debug("imageFile = {}", imageFile);

            try(InputStream in = new URL(imageURL).openStream()){
                Files.copy(in, Paths.get(imageFile));



                // if success, build mediaaObjectDescriptor & MediaObjectMetaDataDescriptor
                // and add elements to ExtractionItemContainer

                MediaObjectDescriptor mediaDescriptor = new MediaObjectDescriptor(Paths.get(object.getBaseURI()));
                MediaObjectMetadataDescriptor[] mediaMetaDescriptor = new MediaObjectMetadataDescriptor[1];
                mediaMetaDescriptor[0] = MediaObjectMetadataDescriptor.of(mediaDescriptor.getObjectId(), "iiif", "institution", _iiif.getInstitution());
                // maybe add more meta from request


                Path path = Paths.get(imageFile);



                items.add(new ExtractionItemContainer(mediaDescriptor, mediaMetaDescriptor, path));


            } catch (MalformedURLException e) {
                LOGGER.error("Malformed URL for {}. SKipping object. {}", object.getBaseURI(), e.getMessage());
            } catch (IOException e) {
                LOGGER.error("Could not retrieve {}. Skipping object. {}", object.getBaseURI(), e.getMessage());
                e.printStackTrace();
            }
        }





        return items.toArray(new ExtractionItemContainer[0]);
    }

    public static IIIFRequest[] getProcessData() {
        return new IIIFRequest[5];
    }
}
