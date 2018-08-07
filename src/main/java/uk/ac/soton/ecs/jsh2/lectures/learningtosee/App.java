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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.content.slideshow.PictureSlide;
import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.content.slideshow.VideoSlide;
import org.openimaj.video.VideoDisplay.EndAction;

import uk.ac.soton.ecs.jsh2.lectures.utils.ScaledAudioVideoSlide;
import uk.ac.soton.ecs.jsh2.lectures.utils.SpeakingSlide;

/**
 * JCG Talk
 */
public class App {
	private static BufferedImage background = null;
	static int SLIDE_WIDTH;
	static int SLIDE_HEIGHT;

	static {
		final GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		final Dimension size = device.getDefaultConfiguration().getBounds().getSize();

		if (size.width >= 1024)
			SLIDE_WIDTH = 1024;
		else
			SLIDE_WIDTH = size.width;
		SLIDE_HEIGHT = SLIDE_WIDTH * 3 / 4;
	}

	/**
	 * Main method
	 *
	 * @param args
	 *            ignored
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final List<Slide> slides = new ArrayList<Slide>();

		slides.add(null);

		for (int i = 1; i <= 74; i++)
			slides.add(new PictureSlide(App.class.getResource(String.format("slides/slides.%03d.jpg", i))));

		slides.set(5, new BadTomatoDemo(App.class.getResource("slides/slides.005.jpg")));
		slides.set(6, new VideoSlide(App.class.getResource("tomato.mp4"), App.class.getResource("slides/slides.006.jpg"),
				EndAction.PAUSE_AT_END));
		slides.set(8, new VideoSlide(App.class.getResource("car.mp4"), App.class.getResource("slides/slides.008.jpg"),
				EndAction.PAUSE_AT_END));
		slides.set(9, new InmoovDemo(App.class.getResource("slides/slides.009.jpg")));
		slides.set(11, new ArtARDemo(App.class.getResource("slides/slides.011.jpg")));

		slides.set(34, new PerceptronBadTomatoDemo(App.class.getResource("slides/slides.034.jpg")));

		// slides.set(37,
		// new AudioVideoSlide(App.class.getResource("cat.mp4"),
		// App.class.getResource("slides/slides.037.jpg"),
		// EndAction.PAUSE_AT_END));
		slides.set(37, new ScaledAudioVideoSlide(App.class.getResource("cat.mp4"), EndAction.PAUSE_AT_END));
		// slides.set(42,
		// new VideoSlide(App.class.getResource("kitten1.mp4"),
		// App.class.getResource("slides/slides.042.jpg"),
		// EndAction.PAUSE_AT_END));
		// slides.set(43,
		// new VideoSlide(App.class.getResource("kitten2.mp4"),
		// App.class.getResource("slides/slides.043.jpg"),
		// EndAction.PAUSE_AT_END));
		slides.set(42,
				new ScaledAudioVideoSlide(App.class.getResource("kitten1.mp4"), EndAction.PAUSE_AT_END));
		slides.set(43,
				new ScaledAudioVideoSlide(App.class.getResource("kitten2.mp4"), EndAction.PAUSE_AT_END));

		slides.set(51, new SimpleMeanColourFeatureDemo(App.class.getResource("slides/slides.051.jpg")));

		slides.set(52, new StickyFeaturesDemo(App.class.getResource("slides/slides.052.jpg")));
		slides.set(53, new PDMDemo(App.class.getResource("slides/slides.053.jpg")));
		slides.set(54, new CLMDemo(App.class.getResource("slides/slides.054.jpg")));

		slides.set(65, new SpeakingSlide(App.class.getResource("slides/slides.065.jpg"),
				"a man is climbing up a rock face"));
		slides.set(66, new SpeakingSlide(App.class.getResource("slides/slides.066.jpg"),
				"a motorcycle racer is driving a turn on a racetrack"));
		slides.set(67, new SpeakingSlide(App.class.getResource("slides/slides.067.jpg"),
				"a basketball player in a red uniform is trying to score a player in the air"));
		slides.set(68, new SpeakingSlide(App.class.getResource("slides/slides.068.jpg"),
				"a man in a red shirt is riding a bike on a snowy hill"));
		slides.set(69, new SpeakingSlide(App.class.getResource("slides/slides.069.jpg"),
				"a surfer is jumping off a snowy hill"));

		slides.remove(0);

		new SlideshowApplication(slides, SLIDE_WIDTH, SLIDE_HEIGHT, getBackground());
	}

	/**
	 * @return the generic slide background
	 */
	public synchronized static BufferedImage getBackground() {
		if (background == null) {
			background = new BufferedImage(SLIDE_WIDTH, SLIDE_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
			final Graphics2D g = background.createGraphics();
			g.setColor(Color.WHITE);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.fillRect(0, 0, background.getWidth(), background.getHeight());
		}
		return background;
	}

	public static int getVideoWidth(int remainder) {
		final int avail = SLIDE_WIDTH - remainder;
		if (avail >= 640)
			return 640;
		return 320;
	}

	public static int getVideoHeight(int remainder) {
		final int width = getVideoWidth(remainder);
		switch (width) {
		case 640:
			return 480;
		}
		return 240;
	}
}
