package ch.unibas.cs.dbis.cineast.core.descriptor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import boofcv.alg.feature.detect.edge.CannyEdge;
import boofcv.factory.feature.detect.edge.FactoryEdgeDetectors;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayS16;
import boofcv.struct.image.GrayU8;
import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.MultiImageFactory;

public class EdgeImg {

	private EdgeImg() {
	}

	private static final Logger LOGGER = LogManager.getLogger();

	private static final float THRESHOLD_LOW = 0.075f, THRESHOLD_HIGH = 0.3f;

	//private static final CannyEdge<ImageUInt8, ImageSInt16> canny = FactoryEdgeDetectors.canny(2, false, true, ImageUInt8.class, ImageSInt16.class);
	
	public static MultiImage getEdgeImg(MultiImage img) {
		LOGGER.entry();

		GrayU8 gray = ConvertBufferedImage.convertFrom(img.getBufferedImage(), (GrayU8) null);
		if(!isSolid(gray)){
			getCanny().process(gray, THRESHOLD_LOW, THRESHOLD_HIGH, gray);
		}

		BufferedImage bout = VisualizeBinaryData.renderBinary(gray, false, null);

		return LOGGER.exit(MultiImageFactory.newMultiImage(bout));
	}

	public static boolean[] getEdgePixels(MultiImage img, boolean[] out) {
		LOGGER.entry();

		if (out == null || out.length != img.getWidth() * img.getHeight()) {
			out = new boolean[img.getWidth() * img.getHeight()];
		}

		GrayU8 gray = ConvertBufferedImage.convertFrom(img.getBufferedImage(), (GrayU8) null);

		if(!isSolid(gray)){
			getCanny().process(gray, THRESHOLD_LOW, THRESHOLD_HIGH, gray);
			
		}

		for (int i = 0; i < gray.data.length; ++i) {
			out[i] = (gray.data[i] != 0);
		}

		LOGGER.exit();
		return out;
	}

	public static List<Boolean> getEdgePixels(MultiImage img, List<Boolean> out) {
		LOGGER.entry();
		if (out == null) {
			out = new ArrayList<Boolean>(img.getWidth() * img.getHeight());
		} else {
			out.clear();
		}
		
		BufferedImage withBackground = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics g = withBackground.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
		g.drawImage(img.getBufferedImage(), 0, 0, null);
		
		GrayU8 gray = ConvertBufferedImage.convertFrom(withBackground, (GrayU8) null);
		if(!isSolid(gray)){
			getCanny().process(gray, THRESHOLD_LOW, THRESHOLD_HIGH, gray);
		}

		for (int i = 0; i < gray.data.length; ++i) {
			out.add(gray.data[i] != 0);
		}
		LOGGER.exit();
		return out;
	}
	
	public static boolean isSolid(GrayU8 img){
		byte first = img.data[0];
		for(byte b : img.data){
			if(b != first){
				return false;
			}
		}
		return true;
	}
	
	//private static HashMap<Thread, CannyEdge<ImageUInt8, ImageSInt16>> cannies = new HashMap<Thread, CannyEdge<ImageUInt8,ImageSInt16>>();
	private static LoadingCache<Thread, CannyEdge<GrayU8, GrayS16>> cannies = CacheBuilder.newBuilder().maximumSize(Config.getExtractorConfig().getThreadPoolSize() * 2)
			.expireAfterAccess(10, TimeUnit.MINUTES).build(new CacheLoader<Thread, CannyEdge<GrayU8, GrayS16>>(){

				@Override
				public CannyEdge<GrayU8, GrayS16> load(Thread arg0){
					return FactoryEdgeDetectors.canny(2, false, true, GrayU8.class, GrayS16.class);
				}});
	private static synchronized CannyEdge<GrayU8, GrayS16> getCanny(){
		Thread current = Thread.currentThread();
		try {
			return cannies.get(current);
		} catch (ExecutionException e) {
			return null; //NEVER HAPPENS
		}
	}
}
