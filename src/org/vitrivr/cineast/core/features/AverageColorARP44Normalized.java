package org.vitrivr.cineast.core.features;

import java.util.List;

import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatVector;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.ARPartioner;
import org.vitrivr.cineast.core.util.ImageHistogramEqualizer;

public class AverageColorARP44Normalized extends AbstractFeatureModule {

  public AverageColorARP44Normalized() {
    super("features_AverageColorARP44Normalized", 115854f / 4f);
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    if (!phandler.idExists(shot.getId())) {
      Pair<FloatVector, float[]> p = ARPartioner
          .partitionImage(ImageHistogramEqualizer.getEqualized(shot.getAvgImg()), 4, 4);
      persist(shot.getId(), p.first);
    }
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    Pair<FloatVector, float[]> p = ARPartioner
        .partitionImage(ImageHistogramEqualizer.getEqualized(sc.getAvgImg()), 4, 4);
    return getSimilar(ReadableFloatVector.toArray(p.first),
        new QueryConfig(qc).setDistanceWeights(p.second));
  }


}
