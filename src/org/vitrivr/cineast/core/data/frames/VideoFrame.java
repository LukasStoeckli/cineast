package org.vitrivr.cineast.core.data.frames;


import org.vitrivr.cineast.core.data.MultiImage;


/**
 * Represents a single video-frame containing. Such a frame consist of a single image and, optionally, an AudioFrame
 * of arbitrary length.
 *
 * @see AudioFrame
 */
public class VideoFrame {
    public static final VideoFrame EMPTY_VIDEO_FRAME = new VideoFrame(0, MultiImage.EMPTY_MULTIIMAGE);
  
	/** ID of the VideoFrame. */
  	private final int id;

	/** MultiImage representing the current VideoFrame. */
	private MultiImage img;

	/** AudioFrame that is associated with the current frame. May be null! */
	private AudioFrame audioFrame = null;

	/**
	 * Constructor vor VideoFrame.
     *
	 * @param id Incremental ID from the frame (e.g. as returned by the decoder).
	 * @param image Image representing the VideoFrame.
	 */
	public VideoFrame(int id, MultiImage image){
		this.id = id;
		this.img = image;
	}

	/**
	 * Getter for ID.
     *
	 * @return ID of the current video frame.
	 */
	public int getId(){
		return this.id;
	}

	/**
	 * Getter for frame image.
     *
	 * @return MultiImage representing the current frame.
	 */
	public MultiImage getImage(){
		return this.img;
	}

    /**
     * Getter for frame audio.
     *
     * @return AudioFrame containing the sound of the current frame.
     */
    public final AudioFrame getAudio() {
        return this.audioFrame;
    }

    /**
     * Adds an AudioFrame to the current VideoFrame. The existing frame (if any)
     * and the new frame are concatenated during the process.
     *
     * @param frame AudioFrame to add to this VideoFrame.
     */
    public void addAudioFrame(AudioFrame frame) {
        if (this.audioFrame == null) {
            this.audioFrame = frame;
        } else {
            this.audioFrame.append(frame);
        }
    }

    /**
     * Returns true if the current VideoFrame has an AudioFrame associated with
     * it and false otherwise.
     *
     * @return True if the current VideoFrame has an AudioFrame associated with it and false otherwise
     */
    public final boolean hasAudio() {
        return this.audioFrame != null;
    }

    /**
	 * Clears the VideoFrame.
	 */
	public void clear(){
		this.img.clear();
		this.img = null;
		this.audioFrame = null;
	}
}
