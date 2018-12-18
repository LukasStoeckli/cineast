package org.vitrivr.cineast.core.importer.vbs2019;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.dao.TagHandler;
import org.vitrivr.cineast.core.importer.Importer;

public class TagFtCSVImporter implements Importer<Pair<String, String>> {

  private static final Logger LOGGER = LogManager.getLogger();
  private final Iterator<CSVRecord> records;

  public TagFtCSVImporter(Path input) throws IOException {
    Reader in = new FileReader(input.toFile());
    records = CSVFormat.RFC4180.withFirstRecordAsHeader().withSkipHeaderRecord().parse(in).iterator();
  }

  private synchronized Optional<Pair<String, String>> nextPair() {
    if (records.hasNext()) {
      CSVRecord next = records.next();
      String id = next.get(0);
      String text = next.get(1);
      return Optional.of(new Pair<>(id, text));
    }
    return Optional.empty();
  }

  /**
   * @return Pair mapping a segmentID to a List of Descriptions
   */
  @Override
  public Pair<String, String> readNext() {
    try {
      Optional<Pair<String, String>> node = nextPair();
      return node.orElse(null);
    } catch (NoSuchElementException e) {
      return null;
    }
  }

  @Override
  public Map<String, PrimitiveTypeProvider> convert(Pair<String, String> data) {
    final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(2);
    map.put(SimpleFulltextFeatureDescriptor.FIELDNAMES[0], PrimitiveTypeProvider.fromObject(data.first));
    map.put(SimpleFulltextFeatureDescriptor.FIELDNAMES[1], PrimitiveTypeProvider.fromObject(data.second));
    return map;
  }
}
