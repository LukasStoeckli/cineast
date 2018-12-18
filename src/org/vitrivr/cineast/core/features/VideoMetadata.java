package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.score.ObjectScoreElement;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.SolrTextRetriever;

public class VideoMetadata extends SolrTextRetriever {

  /**
   * Name of the entity associated wiht {@link VideoMetadata}.
   */
  public static final String VIDEO_METADATA_ENTITY_NAME = "features_meta";


  /**
   * Default constructor for {@link VideoMetadata}.
   */
  public VideoMetadata() {
    super(VIDEO_METADATA_ENTITY_NAME);
  }

  @Override
  protected int getMaxResults() {
    return 100;
  }

  @Override
  protected ScoreElement generateScore(String id, double score) {
    return new ObjectScoreElement(id, score);
  }
}