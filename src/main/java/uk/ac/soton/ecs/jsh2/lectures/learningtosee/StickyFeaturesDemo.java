/**
 * Copyright (c) 2015, The University of Southampton.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.soton.ecs.jsh2.lectures.learningtosee;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.tracking.klt.Feature;
import org.openimaj.video.tracking.klt.FeatureList;
import org.openimaj.video.tracking.klt.KLTTracker;
import org.openimaj.video.tracking.klt.TrackingContext;

/**
 * Slide demonstrating "Corner" features
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class StickyFeaturesDemo extends SimpleCameraDemo implements VideoDisplayListener<MBFImage> {
	private KLTTracker tracker;
	private boolean firstframe = true;
	private FImage prevFrame;
	private BufferedImage bgImage;

	public StickyFeaturesDemo(URL bgImageUrl) throws IOException {
		super("FaceTime");

		bgImage = ImageIO.read(bgImageUrl);

		final int nFeatures = 200;
		final TrackingContext tc = new TrackingContext();
		final FeatureList fl = new FeatureList(nFeatures);
		tracker = new KLTTracker(tc, fl);
		tc.setSequentialMode(true);
		tc.setWriteInternalImages(false);
	}

	@Override
	public JPanel getComponent(int width, int height) throws IOException {
		final JPanel f = super.getComponent(width, height, bgImage);
		this.vc.getDisplay().addVideoListener(this);
		return f;
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// do nothing
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		final FImage currentFrame = ResizeProcessor.resizeMax(frame.flatten(), 640);
		final float sf = (float) ((double) frame.getWidth() / (double) currentFrame.width);

		if (firstframe) {
			tracker.selectGoodFeatures(currentFrame);
			firstframe = false;
		} else {
			tracker.trackFeatures(prevFrame, currentFrame);
			tracker.replaceLostFeatures(currentFrame);
		}
		this.prevFrame = currentFrame.clone();

		final Point2dImpl pt = new Point2dImpl();
		for (final Feature f : tracker.getFeatureList()) {
			if (f.val >= 0) {
				pt.x = f.x * sf;
				pt.y = f.y * sf;
				frame.drawPoint(pt, RGBColour.GREEN, 5);
			}
		}
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new StickyFeaturesDemo(App.class.getResource("slides/slides.011.jpg")), App.SLIDE_WIDTH,
				App.SLIDE_HEIGHT,
				App.getBackground());
	}
}
