package org.vitrivr.cineast.core.db.dao.reader;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;

/**
 * Data access object that facilitates lookups in Cineast's metadata entity (cineast_metadata).
 * Methods in this class usually return MultimediaMetadataDescriptors.
 *
 * @see MediaObjectMetadataDescriptor
 * @author rgasser
 * @version 1.0
 * @created 10.02.17
 */
public class MediaObjectMetadataReader extends AbstractEntityReader {

  private static final ListMultimap<String, MediaObjectMetadataDescriptor> CACHE =
      ArrayListMultimap.create();
  private static final Logger LOGGER = LogManager.getLogger();
  private static boolean USE_CACHE = true; // TODO expose to config
  /** Default constructor. */
  public MediaObjectMetadataReader() {
    this(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
  }

  /**
   * Constructor for MediaObjectMetadataReader
   *
   * @param selector DBSelector to use for the MediaObjectMetadataReader instance.
   */
  public MediaObjectMetadataReader(DBSelector selector) {
    super(selector);
    this.selector.open(MediaObjectMetadataDescriptor.ENTITY);
  }

  public static void warmUpCache() {
    if (!USE_CACHE) {
      return;
    }

    DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();
    selector.open(MediaObjectMetadataDescriptor.ENTITY);
    List<Map<String, PrimitiveTypeProvider>> results = selector.getAll();
    selector.close();
    for (Map<String, PrimitiveTypeProvider> result : results) {
      try {
        MediaObjectMetadataDescriptor momd = new MediaObjectMetadataDescriptor(result);
        CACHE.put(momd.getObjectId(), momd);
      } catch (DatabaseLookupException exception) {
      }
    }
  }

  /**
   * Looks up the metadata for a specific multimedia object.
   *
   * @param objectid ID of the multimedia object for which metadata should be retrieved.
   * @return List of MediaObjectMetadataDescriptor object's. May be empty!
   */
  public List<MediaObjectMetadataDescriptor> lookupMultimediaMetadata(String objectid) {

    if (USE_CACHE) {
      if (CACHE.containsKey(objectid)) {
        return CACHE.get(objectid);
      } else {
        return Collections.emptyList();
      }
    }

    final List<Map<String, PrimitiveTypeProvider>> results =
        this.selector.getRows(MediaObjectMetadataDescriptor.FIELDNAMES[0], objectid);
    if (results.isEmpty()) {
      LOGGER.debug("Could not find MediaObjectMetadataDescriptor with ID {}", objectid);
      return new ArrayList<>(0);
    }

    final ArrayList<MediaObjectMetadataDescriptor> list = new ArrayList<>(results.size());
    results.forEach(
        r -> {
          try {
            list.add(new MediaObjectMetadataDescriptor(r));
          } catch (DatabaseLookupException exception) {
            LOGGER.fatal(
                "Could not map data returned for row {}. This is a programmer's error!", objectid);
          }
        });
    return list;
  }

  /**
   * Looks up the metadata for a multiple multimedia objects.
   *
   * @param objectids ID's of the multimedia object's for which metadata should be retrieved.
   * @return List of MediaObjectMetadataDescriptor object's. May be empty!
   */
  public List<MediaObjectMetadataDescriptor> lookupMultimediaMetadata(List<String> objectids) {

    if (USE_CACHE) {
      ArrayList<MediaObjectMetadataDescriptor> _return = new ArrayList<>();
      for (String objectid : objectids) {
        if (CACHE.containsKey(objectid)) {
          _return.addAll(CACHE.get(objectid));
        }
      }
      return _return;
    }

    final List<Map<String, PrimitiveTypeProvider>> results =
        this.selector.getRows(MediaObjectMetadataDescriptor.FIELDNAMES[0], objectids);
    if (results.isEmpty()) {
      LOGGER.debug("Could not find any MediaObjectMetadataDescriptor for provided ID's.");
      return new ArrayList<>(0);
    }

    final ArrayList<MediaObjectMetadataDescriptor> list = new ArrayList<>(results.size());
    results.forEach(
        r -> {
          try {
            list.add(new MediaObjectMetadataDescriptor(r));
          } catch (DatabaseLookupException exception) {
            LOGGER.fatal("Could not map data. This is a programmer's error!");
          }
        });
    return list;
  }
}
