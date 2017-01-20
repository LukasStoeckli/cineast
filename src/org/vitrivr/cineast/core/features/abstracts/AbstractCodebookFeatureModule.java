package org.vitrivr.cineast.core.features.abstracts;

import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.alg.scene.FeatureToWordHistogram_F64;
import boofcv.io.UtilIO;
import boofcv.struct.feature.BrightFeature;
import boofcv.struct.image.GrayF32;

import org.ddogleg.clustering.AssignCluster;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;

/**
 * An abstract feature module that leverages a named codebook and a set of features to obtain
 * a histogram of codewords. It remains to the implementer which codebook and what descriptors to use.
 * Once features have been obtained, use the histogram() method to get the histogram given the corpus.
 *
 * All codebooks should be placed in the ./resources/codebooks folder.
 *
 * This class currently requires BoofCV.
 *
 * TODO: Use CSV based format for codebooks.
 *
 * @author rgasser
 * @version 1.0
 * @created 20.01.17
 */
public abstract class AbstractCodebookFeatureModule extends AbstractFeatureModule {
    /** The Assignment used for the codebook. */
    private AssignCluster<double[]> assignment;

    /** The folder that contains the Codebook(s). */
    private static String CODEBOOK_FOLDER = "./resources/codebooks/";

    /* Histogram calculator used to obtain a feature-to-word histogram. */
    private FeatureToWordHistogram_F64 featuresToHistogram;

    /**
     *
     * @param tableName
     * @param maxDist
     */
    protected AbstractCodebookFeatureModule(String tableName, float maxDist) {
        super(tableName, maxDist);
    }

    /**
     * Initializer for Extraction - must load the codebook.
     *
     * @param phandlerSupply
     */
    public final void init(PersistencyWriterSupplier phandlerSupply) {
        super.init(phandlerSupply);

        /* Load the Codebook. */
        this.assignment = UtilIO.load(CODEBOOK_FOLDER + this.codebook());
    }

    /**
     * Initializer for Retrieval - must load the codebook.
     *
     * @param selectorSupply
     */
    public final void init(DBSelectorSupplier selectorSupply) {
        super.init(selectorSupply);

        /* Load the Codebook. */
        this.assignment = UtilIO.load(CODEBOOK_FOLDER + this.codebook());
    }

    /**
     * Returns a histogram given the provided descriptors and the assignment object loaded
     * from the codebook.
     *
     * @param hard Indicates whether to use hard or soft assignment.
     * @param descriptors Feature descriptors.
     * @return float[] array with codebook
     */
    protected final float[] histogram(boolean hard, DetectDescribePoint<GrayF32, BrightFeature> descriptors) {
        /* Reset the Histogram-Calculator. */
        if (this.featuresToHistogram != null) {
            this.featuresToHistogram.reset();
        } else {
            this.featuresToHistogram = new FeatureToWordHistogram_F64(this.assignment, hard);
        }

        /* Add the features to the Histogram-Calculator... */
        for (int i=0;i<descriptors.getNumberOfFeatures();i++) {
            this.featuresToHistogram.addFeature(descriptors.getDescription(i));
        }

        /* ... and calculates and returns the histogram. */
        this.featuresToHistogram.process();
        return this.floatToDoubleArray(featuresToHistogram.getHistogram());
    }

    /**
     * Converts a double array into a float array of the same size.
     *
     * @param dbl double array to be converted..
     * @return float array
     */
    protected final float[] floatToDoubleArray(double[] dbl) {
        float[] flt = new float[dbl.length];
        for (int i=0;i<dbl.length;i++) {
            flt[i] = (float)dbl[i];
        }
        return flt;
    }

    /**
     * Returns the full name of the codebook to use. All codebook be placed in the
     * ./resources/codebooks folder.
     *
     * @return
     */
    protected abstract String codebook();
}
