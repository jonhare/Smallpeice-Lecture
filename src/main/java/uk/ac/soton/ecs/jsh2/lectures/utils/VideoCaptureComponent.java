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
package uk.ac.soton.ecs.jsh2.lectures.utils;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Closeable;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.video.ArrayBackedVideo;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplay.Mode;
import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

/**
 * A reusable swing component for video input
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class VideoCaptureComponent extends Box implements ItemListener, Closeable {
	private static final long serialVersionUID = 1L;
	private VideoDisplay<MBFImage> display;
	private JComboBox<String> sources;
	private int width;
	private int height;
	private Device currentDevice;

	/**
	 * Construct with the given dimensions
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @throws VideoCaptureException
	 */
	public VideoCaptureComponent(int width, int height) throws VideoCaptureException {
		this(width, height, "FaceTime");
	}

	/**
	 * Construct with the given dimensions
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @throws VideoCaptureException
	 */
	public VideoCaptureComponent(int width, int height, String devName) throws VideoCaptureException {
		super(BoxLayout.Y_AXIS);

		this.setOpaque(false);

		this.width = width;
		this.height = height;
		final List<Device> devices = VideoCapture.getVideoDevices();

		Video<MBFImage> vc = null;
		if (devices == null || devices.size() == 0) {
			currentDevice = null;

			final MBFImage[] frames = { new MBFImage(width, height, ColourSpace.RGB) };
			frames[0].fill(RGBColour.RED);
			vc = new ArrayBackedVideo<MBFImage>(frames);
		} else {
			for (final Device d : devices) {
				if (d.getNameStr().contains(devName)) {
					currentDevice = d;
					break;
				}
			}

			if (currentDevice == null)
				currentDevice = devices.get(0);

			vc = new VideoCapture(width, height, currentDevice);
		}

		final JPanel videoDisplayPanel = new JPanel();
		videoDisplayPanel.setOpaque(false);
		display = VideoDisplay.createVideoDisplay(vc, videoDisplayPanel);
		add(videoDisplayPanel);

		final JPanel sourcesPanel = new JPanel();
		sourcesPanel.setOpaque(false);
		sources = new JComboBox<String>();
		sources.setOpaque(false);
		if (devices == null || devices.size() == 0) {
			sources.addItem("No cameras found");
			sources.setEnabled(false);
		} else {
			for (final Device s : devices)
				sources.addItem(s.getNameStr());
		}

		sources.setSelectedItem(currentDevice.getNameStr());

		sources.addItemListener(this);
		sourcesPanel.add(sources);
		add(sourcesPanel);
	}

	/**
	 * Get the underlying video display
	 *
	 * @return the display
	 */
	public VideoDisplay<MBFImage> getDisplay() {
		return display;
	}

	@Override
	public void close() {
		this.display.close();
	}

	@Override
	public void itemStateChanged(ItemEvent event) {
		if (event.getStateChange() == ItemEvent.SELECTED) {
			final String item = (String) event.getItem();
			final Device d = VideoCapture.getVideoDevices().get(sources.getSelectedIndex());
			if (d.getNameStr().equals(item) && !currentDevice.equals(d)) {
				try {
					currentDevice = d;
					display.setMode(Mode.STOP);
					display.getVideo().close();
					display.changeVideo(new VideoCapture(width, height, currentDevice));
					display.setMode(Mode.PLAY);
				} catch (final VideoCaptureException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
