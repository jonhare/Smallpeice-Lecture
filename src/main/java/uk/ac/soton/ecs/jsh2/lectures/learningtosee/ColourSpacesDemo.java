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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

/**
 * Demonstrate different colour spaces
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class ColourSpacesDemo extends SimpleCameraDemo implements Slide, VideoDisplayListener<MBFImage>, ActionListener {
	protected ColourSpace[] colourSpaces;
	protected volatile ColourSpace colourSpace = ColourSpace.RGB;
	private BufferedImage bgImage;

	protected ColourSpacesDemo(URL bgImageUrl) throws IOException {
		super("FaceTime");
		bgImage = ImageIO.read(bgImageUrl);
		this.colourSpaces = new ColourSpace[] {
				ColourSpace.RGB, ColourSpace.HSV, ColourSpace.HUE, ColourSpace.H1H2
		};
	}

	@Override
	public JPanel getComponent(int width, int height) throws IOException {
		final JPanel base = super.getComponent(width, height, bgImage);

		// add a listener for new video frames
		vc.getDisplay().addVideoListener(this);

		// the controls panel
		final JPanel controlsPanel = new JPanel(new GridLayout(0, 1));
		controlsPanel.setOpaque(false);
		final ButtonGroup group = new ButtonGroup();

		for (final ColourSpace c : colourSpaces)
			createRadioButton(controlsPanel, group, c);

		base.add(controlsPanel);

		return base;
	}

	/**
	 * Create a radio button
	 *
	 * @param controlsPanel
	 *            the panel to add the button too
	 * @param group
	 *            the radio button group
	 * @param cs
	 *            the colourSpace that the button represents
	 */
	private void createRadioButton(final JPanel controlsPanel, final ButtonGroup group, final ColourSpace cs) {
		final String name = cs.name();
		final JRadioButton button = new JRadioButton(name);
		button.setActionCommand(name);
		controlsPanel.add(button);
		group.add(button);
		button.setSelected(cs == ColourSpace.RGB);
		button.addActionListener(this);
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// Do nothing
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		convertColours(frame, colourSpace);
	}

	/**
	 * Convert the colour space of an image, maintaining three bands so it can
	 * be displayed
	 *
	 * @param frame
	 * @param colourSpace
	 */
	public static void convertColours(MBFImage frame, ColourSpace colourSpace) {
		// update the frame from the camera by converting to the selected colour
		// space before display
		final MBFImage cvt = colourSpace.convert(frame);

		// if the converted image has fewer than 3 bands, add more so it can be
		// displayed as RGB.
		if (cvt.numBands() == 1) {
			// this makes a three-band grey-level image, where all the bands are
			// the same
			cvt.bands.add(cvt.getBand(0).clone());
			cvt.bands.add(cvt.getBand(0).clone());
		} else if (cvt.numBands() == 2) {
			// this adds a third zero band to a two-band image
			cvt.bands.add(new FImage(cvt.getWidth(), cvt.getHeight()));
		}

		// this sets the frame to the colour converted version
		frame.internalAssign(cvt);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// change the colour space to the one selected
		this.colourSpace = ColourSpace.valueOf(e.getActionCommand());
	}
}
