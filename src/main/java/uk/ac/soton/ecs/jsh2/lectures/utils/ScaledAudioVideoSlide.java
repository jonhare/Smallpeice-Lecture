/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
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
package uk.ac.soton.ecs.jsh2.lectures.utils;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.JPanel;

import org.apache.commons.io.FileUtils;
import org.openimaj.content.slideshow.Slide;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplay.EndAction;
import org.openimaj.video.xuggle.XuggleAudio;
import org.openimaj.video.xuggle.XuggleVideo;

/**
 * Slide with audio and scaled video
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ScaledAudioVideoSlide implements Slide {

	private URL url;
	private EndAction endAction;
	private VideoDisplay<MBFImage> display;

	/**
	 * Construct with the given video and {@link EndAction}
	 *
	 * @param url
	 * @param endAction
	 * @throws IOException
	 */
	public ScaledAudioVideoSlide(URL url, EndAction endAction) throws IOException {
		if (url.getProtocol().startsWith("jar")) {
			final File tmp = File.createTempFile("movie", ".tmp");
			tmp.deleteOnExit();
			FileUtils.copyURLToFile(url, tmp);
			url = tmp.toURI().toURL();
		}

		this.url = url;
		this.endAction = endAction;
	}

	@Override
	public Component getComponent(int width, int height) throws IOException {
		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setPreferredSize(new Dimension(width, height));
		base.setLayout(new GridBagLayout());

		final ImageComponent ic = new DisplayUtilities.ScalingImageComponent();
		ic.setSize(base.getSize());
		ic.setPreferredSize(base.getPreferredSize());
		ic.setAllowZoom(false);
		ic.setAllowPanning(false);
		ic.setTransparencyGrid(false);
		ic.setShowPixelColours(false);
		ic.setShowXYPosition(false);
		base.add(ic);

		try {
			final XuggleVideo video = new XuggleVideo(this.url, true);
			final XuggleAudio audio = new XuggleAudio(this.url);

			display = new VideoDisplay<MBFImage>(video, audio, ic);
			display.setEndAction(this.endAction);
			new Thread(display).start();
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return base;
	}

	@Override
	public void close() {
		display.close();
	}
}
