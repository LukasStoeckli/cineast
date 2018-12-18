package org.vitrivr.cineast.core.importer.vbs2019;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.db.dao.TagHandler;
import org.vitrivr.cineast.core.features.TagsFtSearch;
import org.vitrivr.cineast.core.importer.handlers.DataImportHandler;
import org.vitrivr.cineast.core.util.LogHelper;

public class TagFtCSVImportHandler extends DataImportHandler {

  private static final Logger LOGGER = LogManager.getLogger();

  public TagFtCSVImportHandler(int threads, int batchsize) {
    super(threads, batchsize);
  }

  @Override
  public void doImport(Path root) {
    try {
      LOGGER.info("Starting data import for CSV tag files in: {}", root.toString());
      Files.walk(root, 2).filter(p -> p.toString().toLowerCase().endsWith(".csv")).forEach(p -> {
        try {
          this.futures.add(this.service.submit(new DataImportRunner(new TagFtCSVImporter(p), TagsFtSearch.TAGS_FT_TABLE_NAME, "tag csv file")));
        } catch (IOException e) {
          LOGGER.fatal("Failed to open path at {} ", p);
          throw new RuntimeException(e);
        }
      });
      this.waitForCompletion();
      LOGGER.info("Completed data import with CSV Tag Import files in: {}", root.toString());
    } catch (IOException e) {
      LOGGER.error("Could not start data import process with path '{}' due to an IOException: {}. Aborting...", root.toString(), LogHelper.getStackTrace(e));
    }
  }
}
