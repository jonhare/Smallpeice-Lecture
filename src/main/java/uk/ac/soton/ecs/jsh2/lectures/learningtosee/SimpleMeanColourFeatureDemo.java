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

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.io.IOException;
import java.net.URL;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.math.util.FloatArrayStatsUtils;

/**
 * Demonstrate different colour spaces
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class SimpleMeanColourFeatureDemo extends ColourSpacesDemo
{
	private JTextField featureField;

	protected SimpleMeanColourFeatureDemo(URL bgImageUrl) throws IOException {
		super(bgImageUrl);
		this.colourSpaces = new ColourSpace[] {
				ColourSpace.RGB, ColourSpace.HSV, ColourSpace.H1H2, ColourSpace.HS, ColourSpace.H2S_2
		};
	}

	@Override
	public JPanel getComponent(int width, int height) throws IOException {
		final JPanel base = super.getComponent(width, height);

		featureField = new JTextField();
		featureField.setOpaque(false);
		featureField.setBorder(null);
		featureField.setFont(Font.decode("Monaco-48"));
		featureField.setHorizontalAlignment(JTextField.CENTER);
		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		base.add(featureField, c);

		return base;
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		super.beforeUpdate(frame);

		final double[] vector = computeMean(frame, colourSpace);

		featureField.setText(formatVector(vector));
	}

	/**
	 * Compute the mean of the image
	 *
	 * @param frame
	 * @param colourSpace
	 * @return
	 */
	public static double[] computeMean(MBFImage frame, ColourSpace colourSpace) {
		final double[] vector = new double[colourSpace.getNumBands()];

		for (int b = 0; b < colourSpace.getNumBands(); b++) {
			final float[][] pix = frame.getBand(b).pixels;

			vector[b] = FloatArrayStatsUtils.mean(pix);
		}
		return vector;
	}

	/**
	 * Format vector as a string
	 *
	 * @param vector
	 * @return
	 */
	public static String formatVector(double[] vector) {
		final StringBuffer sb = new StringBuffer();
		sb.append("[");
		sb.append(String.format("%1.3f", vector[0]));
		for (int i = 1; i < vector.length; i++)
			sb.append(String.format(", %1.3f", vector[i]));
		sb.append("]");
		return sb.toString();
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new SimpleMeanColourFeatureDemo(App.class.getResource("slides/slides.051.jpg")), 1024,
				768, App.getBackground());
	}
}
