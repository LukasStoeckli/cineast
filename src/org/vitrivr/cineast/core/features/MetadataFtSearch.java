package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.SolrTextRetriever;

public class MetadataFtSearch extends SolrTextRetriever {

  public static final String METADATA_FT_TABLE_NAME = "features_metadataft";

  /**
   * Default constructor for {@link MetadataFtSearch}.
   */
  public MetadataFtSearch() {
    super(MetadataFtSearch.METADATA_FT_TABLE_NAME);
  }

  @Override
  protected String[] generateQuery(SegmentContainer sc, ReadableQueryConfig qc) {
    return sc.getText().split(" ");
  }
}