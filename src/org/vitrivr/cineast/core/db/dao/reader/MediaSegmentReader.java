package org.vitrivr.cineast.core.db.dao.reader;

import static org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor.FIELDNAMES;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;

public class MediaSegmentReader extends AbstractEntityReader {

  private static final ConcurrentHashMap<String, MediaSegmentDescriptor> CACHE =
      new ConcurrentHashMap<>();
  private static final MediaSegmentDescriptor NULL = new MediaSegmentDescriptor();
  private static final Logger LOGGER = LogManager.getLogger();
  private static boolean USE_CACHE = true; // TODO expose to config

  /**
   * Default constructor.
   */
  public MediaSegmentReader() {
    this(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
  }

  /**
   * Constructor for MediaSegmentReader
   *
   * @param dbSelector DBSelector to use for the MediaObjectMetadataReader instance.
   */
  public MediaSegmentReader(DBSelector dbSelector) {
    super(dbSelector);
    this.selector.open(MediaSegmentDescriptor.ENTITY);
  }

  private static Optional<MediaSegmentDescriptor> propertiesToDescriptor(
      Map<String, PrimitiveTypeProvider> properties) {

    if (properties.containsKey(FIELDNAMES[0])
        && properties.containsKey(FIELDNAMES[1])
        && properties.containsKey(FIELDNAMES[2])
        && properties.containsKey(FIELDNAMES[3])
        && properties.containsKey(FIELDNAMES[4])
        && properties.containsKey(FIELDNAMES[5])
        && properties.containsKey(FIELDNAMES[6])) {

      return Optional.of(
          new MediaSegmentDescriptor(
              properties.get(FIELDNAMES[1]).getString(),
              properties.get(FIELDNAMES[0]).getString(),
              properties.get(FIELDNAMES[2]).getInt(),
              properties.get(FIELDNAMES[3]).getInt(),
              properties.get(FIELDNAMES[4]).getInt(),
              properties.get(FIELDNAMES[5]).getFloat(),
              properties.get(FIELDNAMES[6]).getFloat()));

    } else {
      return Optional.empty();
    }
  }

  public Optional<MediaSegmentDescriptor> lookUpSegment(String segmentId) {

    if (USE_CACHE && CACHE.containsKey(segmentId)) {
      MediaSegmentDescriptor msd = CACHE.get(segmentId);
      if (msd == NULL) {
        return Optional.empty();
      } else {
        return Optional.of(msd);
      }
    }

    Stream<MediaSegmentDescriptor> descriptors =
        this.lookUpSegmentsByField(FIELDNAMES[0], segmentId);

    if (USE_CACHE) {
      Optional<MediaSegmentDescriptor> msdo = descriptors.findFirst();
      if (msdo.isPresent()) {
        CACHE.put(segmentId, msdo.get());
        return Optional.of(msdo.get());
      } else {
        CACHE.put(segmentId, NULL);
        return Optional.empty();
      }
    } else {
      return descriptors.findFirst();
    }
  }

  public Map<String, MediaSegmentDescriptor> lookUpSegments(Iterable<String> segmentIds) {

    Map<String, MediaSegmentDescriptor> _return = new HashMap<>();

    ArrayList<String> idsToFetch = new ArrayList<>();

    if (USE_CACHE) {
      for (String segmentId : segmentIds) {
        if (CACHE.containsKey(segmentId)) {
          MediaSegmentDescriptor msd = CACHE.get(segmentId);
          if (msd != NULL) {
            _return.put(segmentId, msd);
          }
        } else {
          idsToFetch.add(segmentId);
        }
      }
    } else {
      segmentIds.forEach(idsToFetch::add);
    }

    if (idsToFetch.isEmpty()) {
      return _return;
    }

    Stream<MediaSegmentDescriptor> descriptors =
        this.lookUpSegmentsByField(FIELDNAMES[0], segmentIds);
    // this implicitly deduplicates the stream

    descriptors.forEach(
        msd -> {
          _return.put(msd.getSegmentId(), msd);
          if (USE_CACHE) {
            CACHE.put(msd.getSegmentId(), msd);
          }
        });
    return _return;
  }

  public List<MediaSegmentDescriptor> lookUpSegmentsOfObject(String objectId) {

    if (USE_CACHE) {
      ArrayList<MediaSegmentDescriptor> _return = new ArrayList<>();
      for (MediaSegmentDescriptor msd : CACHE.values()) {
        if (msd.getObjectId().equals(objectId)) {
          _return.add(msd);
        }
      }
      return _return;
    }

    Stream<MediaSegmentDescriptor> descriptors =
        this.lookUpSegmentsByField(FIELDNAMES[1], objectId);
    return descriptors.collect(Collectors.toList());
  }

  public ListMultimap<String, MediaSegmentDescriptor> lookUpSegmentsOfObjects(
      Iterable<String> objectIds) {

    List<String> uncachedIds = new ArrayList<>();
    if (USE_CACHE) {
      ListMultimap<String, MediaSegmentDescriptor> _return = ArrayListMultimap.create();
      for (String objectId : objectIds) {
        boolean cached = false;
        for (MediaSegmentDescriptor msd : CACHE.values()) {
          if (msd.getObjectId().equals(objectId)) {
            _return.put(objectId, msd);
            cached = true;
          }
        }
        if (!cached) {
          uncachedIds.add(objectId);
        }
      }
      Stream<MediaSegmentDescriptor> descriptors =
          this.lookUpSegmentsByField(FIELDNAMES[1], uncachedIds);
      descriptors.forEach(el -> {
        CACHE.put(el.getObjectId(), el);
        _return.put(el.getObjectId(), el);
      });
      return _return;
    }

    Stream<MediaSegmentDescriptor> descriptors =
        this.lookUpSegmentsByField(FIELDNAMES[1], objectIds);
    return Multimaps.index(descriptors.iterator(), MediaSegmentDescriptor::getObjectId);
  }

  private Stream<MediaSegmentDescriptor> lookUpSegmentsByField(
      String fieldName, String fieldValue) {
    return lookUpSegmentsByField(fieldName, Collections.singletonList(fieldValue));
  }

  private Stream<MediaSegmentDescriptor> lookUpSegmentsByField(
      String fieldName, Iterable<String> fieldValues) {
    Set<String> uniqueFieldValues = new HashSet<>();
    fieldValues.forEach(value -> uniqueFieldValues.add(value));

    List<Map<String, PrimitiveTypeProvider>> segmentsProperties =
        this.selector.getRows(fieldName, uniqueFieldValues);
    return segmentsProperties
        .stream()
        .map(MediaSegmentReader::propertiesToDescriptor)
        .filter(Optional::isPresent)
        .map(Optional::get);
  }

  public static void warmUpCache() {
    if (!USE_CACHE) {
      return;
    }
    DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();
    selector.open(MediaSegmentDescriptor.ENTITY);
    List<Map<String, PrimitiveTypeProvider>> all = selector.getAll();
    for (Map<String, PrimitiveTypeProvider> map : all) {
      Optional<MediaSegmentDescriptor> msd = propertiesToDescriptor(map);
      if (msd.isPresent()) {
        CACHE.put(msd.get().getSegmentId(), msd.get());
      }
    }
    System.gc();
  }
}
