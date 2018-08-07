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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.renderer.RenderHints;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

import uk.ac.soton.ecs.jsh2.lectures.utils.VideoCaptureComponent;

/**
 * Demonstration of Linear classification (with the Perceptron) using simple
 * average colour features for classify green vs red tomatoes.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class PerceptronBadTomatoDemo implements Slide, ActionListener, VideoDisplayListener<MBFImage> {
	/**
	 * A really simple perceptron
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 */
	private static class SimplePerceptron {
		double alpha = 0.01;
		double[] w;

		/**
		 * Train the perceptron
		 *
		 * @param pts
		 *            the data points (2d)
		 * @param classes
		 *            the classes (0/1)
		 */
		public void train(List<double[]> pts, List<Integer> classes) {
			this.w = new double[] { 1, 0, 0 };

			for (int i = 0; i < 1000; i++) {
				iteration(pts, classes);

				final double error = error(pts, classes);
				if (error < 0.01)
					break;
			}
		}

		private double error(List<double[]> pts, List<Integer> classes) {
			double error = 0;

			for (int i = 0; i < pts.size(); i++) {
				error += Math.abs(predict(pts.get(i)) - classes.get(i));
			}

			return error / pts.size();
		}

		private void iteration(List<double[]> pts, List<Integer> classes) {
			for (int i = 0; i < pts.size(); i++)
				update(pts.get(i), classes.get(i));
		}

		private void update(double[] pt, int clazz) {
			final int y = predict(pt);

			w[0] = w[0] + alpha * (clazz - y);
			w[1] = w[1] + alpha * (clazz - y) * pt[0];
			w[2] = w[2] + alpha * (clazz - y) * pt[1];
		}

		/**
		 * Predict the class of the given point
		 *
		 * @param pt
		 *            the point
		 * @return 0 or 1
		 */
		public int predict(double[] pt) {
			return w[0] + pt[0] * w[1] + pt[1] * w[2] > 0 ? 1 : 0;
		}

		/**
		 * Compute y-ordinate of a point on the hyperplane given the x-ordinate
		 *
		 * @param x
		 *            the x-ordinate
		 * @return the y-ordinate
		 */
		public double computeHyperplanePoint(double x) {
			return (w[0] + w[1] * x) / -w[2];
		}
	}

	private static final int POINT_SIZE = 10;

	private static final int GRAPH_WIDTH = 600;
	private static final int GRAPH_HEIGHT = 600;
	private static final int AXIS_WIDTH = GRAPH_WIDTH - 100;
	private static final int AXIS_HEIGHT = GRAPH_HEIGHT - 100;
	private static final int AXIS_OFFSET_X = 50;
	private static final int AXIS_OFFSET_Y = 50;
	private static final int AXIS_EXTENSION = 5;

	private static final int VIDEO_WIDTH = App.getVideoWidth(GRAPH_WIDTH);
	private static final int VIDEO_HEIGHT = App.getVideoHeight(GRAPH_WIDTH);

	private static final String[] CLASSES = { "RIPE", "UNRIPE" };
	// private static final Float[][] COLOURS = { RGBColour.RED, RGBColour.GREEN
	// };

	private VideoCaptureComponent vc;
	private ColourSpace colourSpace = ColourSpace.H1H2;
	private JTextField featureField;
	private MBFImage image;
	private ImageComponent imageComp;
	private BufferedImage bimg;
	private JComboBox<String> classType;
	private double[] lastMean;
	private JTextField guess;

	private volatile List<double[]> points;
	private volatile List<Integer> classes;
	private volatile SimplePerceptron classifier;
	private Circle circle;
	private BufferedImage bgImage;

	public PerceptronBadTomatoDemo(URL bgImageUrl) throws IOException {
		bgImage = ImageIO.read(bgImageUrl);
	}

	@Override
	public Component getComponent(final int width, final int height) throws IOException {
		points = new ArrayList<double[]>();
		classes = new ArrayList<Integer>();
		classifier = new SimplePerceptron();

		circle = new Circle(VIDEO_WIDTH / 2, VIDEO_HEIGHT / 2, VIDEO_HEIGHT / 8);

		vc = new VideoCaptureComponent(VIDEO_WIDTH, VIDEO_HEIGHT);
		vc.getDisplay().addVideoListener(this);

		// the main panel
		final JPanel base = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

				super.paintComponent(g);

				if (bgImage != null)
					g.drawImage(bgImage, 0, 0, width, height, null);
			}
		};

		base.setOpaque(false);
		base.setPreferredSize(new Dimension(width, height));
		base.setLayout(new GridBagLayout());

		// left hand side (video, features)
		final Box videoCtrls = Box.createVerticalBox();
		videoCtrls.setOpaque(false);
		videoCtrls.add(vc);
		videoCtrls.add(Box.createVerticalStrut(10));
		final JPanel colourspacesPanel = createColourSpaceButtons();
		videoCtrls.add(colourspacesPanel);
		createFeatureField();
		videoCtrls.add(Box.createVerticalStrut(10));
		videoCtrls.add(featureField);
		base.add(videoCtrls);

		// right hand box
		final Box rightPanel = Box.createVerticalBox();
		rightPanel.setOpaque(false);
		image = new MBFImage(GRAPH_WIDTH, GRAPH_HEIGHT, ColourSpace.RGB);
		image.fill(RGBColour.WHITE);
		imageComp = new DisplayUtilities.ImageComponent(true, false);
		imageComp.setShowPixelColours(false);
		imageComp.setShowXYPosition(false);
		imageComp.setAllowZoom(false);
		imageComp.setAllowPanning(false);
		rightPanel.add(imageComp);
		final JPanel classCtrlsCnt = new JPanel(new GridLayout(1, 2));

		// learning controls
		final JPanel learnCtrls = new JPanel(new GridLayout(0, 1));
		learnCtrls.setOpaque(false);
		classType = new JComboBox<String>();
		for (final String c : CLASSES)
			classType.addItem(c);
		learnCtrls.add(classType);
		final JButton learnButton = new JButton("Learn");
		learnButton.setActionCommand("button.learn");
		learnButton.addActionListener(this);
		learnCtrls.add(learnButton);
		classCtrlsCnt.add(learnCtrls);

		// classification controls
		final JPanel classCtrls = new JPanel(new GridLayout(0, 1));
		classCtrls.setOpaque(false);
		guess = new JTextField(8);
		guess.setOpaque(false);
		guess.setFont(Font.decode("Monaco-24"));
		guess.setHorizontalAlignment(JTextField.CENTER);
		guess.setEditable(false);
		classCtrls.add(guess);
		classCtrlsCnt.add(classCtrls);

		rightPanel.add(classCtrlsCnt);

		base.add(rightPanel);

		redraw();
		return base;
	}

	private void createFeatureField() {
		featureField = new JTextField();
		featureField.setOpaque(false);
		featureField.setFont(Font.decode("Monaco-24"));
		featureField.setHorizontalAlignment(JTextField.CENTER);
		featureField.setEditable(false);
		featureField.setBorder(null);
	}

	private JPanel createColourSpaceButtons() {
		final JPanel colourspacesPanel = new JPanel();
		colourspacesPanel.setOpaque(false);
		colourspacesPanel.setLayout(new BoxLayout(colourspacesPanel, BoxLayout.X_AXIS));
		colourspacesPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		final ButtonGroup group = new ButtonGroup();
		createRadioButton(colourspacesPanel, group, ColourSpace.HUE);
		createRadioButton(colourspacesPanel, group, ColourSpace.HS);
		createRadioButton(colourspacesPanel, group, ColourSpace.H1H2);
		return colourspacesPanel;
	}

	/**
	 * Create a radio button
	 *
	 * @param colourspacesPanel
	 *            the panel to add the button too
	 * @param group
	 *            the radio button group
	 * @param cs
	 *            the colourSpace that the button represents
	 */
	private void createRadioButton(final JPanel colourspacesPanel, final ButtonGroup group, final ColourSpace cs) {
		final String name = cs.name();
		final JRadioButton button = new JRadioButton(name);
		button.setActionCommand("ColourSpace." + name);
		colourspacesPanel.add(button);
		group.add(button);
		button.setSelected(cs == this.colourSpace);
		button.addActionListener(this);
	}

	@Override
	public void close() {
		vc.close();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final String rawcmd = e.getActionCommand();
		final String cmd = rawcmd.substring(rawcmd.indexOf(".") + 1);

		if (rawcmd.startsWith("ColourSpace.")) {
			// change the colour space to the one selected
			this.colourSpace = ColourSpace.valueOf(cmd);
			this.classes.clear();
			this.points.clear();
		} else if (rawcmd.startsWith("button.")) {
			if (cmd.equals("learn")) {
				doLearn(lastMean, this.classType.getSelectedIndex());
			}
		}
	}

	private void doClassify(double[] mean) {
		final HashSet<Integer> clzCount = new HashSet<Integer>();
		clzCount.addAll(classes);

		if (points.size() > 0 && clzCount.size() == 2) {
			final double[] p1 = new double[] { 0, 0 };
			p1[1] = (float) classifier.computeHyperplanePoint(0);

			final double[] p2 = new double[] { 1, 0 };
			p2[1] = (float) classifier.computeHyperplanePoint(1);

			image.drawLine(projectPoint(p1), projectPoint(p2), 3, RGBColour.BLACK);

			imageComp.setImage(bimg = ImageUtilities.createBufferedImageForDisplay(image, bimg));

			guess.setText(this.classType.getItemAt(classifier.predict(mean)));
			return;
		}
		guess.setText("unknown");
	}

	/**
	 * Project a point to draw it on the graph
	 *
	 * @param pt
	 *            the point
	 * @return the projected point in image coords
	 */
	private Point2dImpl projectPoint(final double[] pt) {
		final Point2dImpl pti = new Point2dImpl();

		pti.x = AXIS_OFFSET_X + (float) (AXIS_WIDTH * pt[0]);
		if (pt.length == 2)
			pti.y = (AXIS_OFFSET_Y + AXIS_HEIGHT) - (float) (AXIS_HEIGHT * pt[1]);
		else
			pti.y = AXIS_OFFSET_Y + AXIS_HEIGHT;

		return pti;
	}

	private void doLearn(double[] mean, int clazz) {
		this.points.add(mean);
		this.classes.add(clazz);

		final HashSet<Integer> clzCount = new HashSet<Integer>();
		clzCount.addAll(classes);

		if (points.size() > 0 && clzCount.size() == 2) {
			classifier.train(points, classes);
		}

		redraw();
	}

	private void redraw() {
		this.image.fill(RGBColour.WHITE);

		// draw saved points
		// for (int i = 0; i < points.size(); i++) {
		// final double[] pt = points.get(i);
		// image.drawPoint(projectPoint(pt), COLOURS[classes.get(i)],
		// POINT_SIZE);
		// }

		// draw current point
		if (lastMean != null) {
			image.drawPoint(projectPoint(lastMean), RGBColour.MAGENTA, POINT_SIZE);
		}

		// draw y-axes
		image.drawLine(AXIS_OFFSET_X, AXIS_OFFSET_Y - AXIS_EXTENSION, AXIS_OFFSET_X, AXIS_OFFSET_Y + AXIS_HEIGHT
				+ AXIS_EXTENSION, RGBColour.BLACK);
		image.drawLine(AXIS_OFFSET_X + 1 * AXIS_WIDTH / 4, AXIS_OFFSET_Y - AXIS_EXTENSION, AXIS_OFFSET_X + 1 * AXIS_WIDTH
				/ 4, AXIS_OFFSET_Y + AXIS_HEIGHT + AXIS_EXTENSION, RGBColour.GRAY);
		image.drawLine(AXIS_OFFSET_X + 2 * AXIS_WIDTH / 4, AXIS_OFFSET_Y - AXIS_EXTENSION, AXIS_OFFSET_X + 2 * AXIS_WIDTH
				/ 4, AXIS_OFFSET_Y + AXIS_HEIGHT + AXIS_EXTENSION, RGBColour.GRAY);
		image.drawLine(AXIS_OFFSET_X + 3 * AXIS_WIDTH / 4, AXIS_OFFSET_Y - AXIS_EXTENSION, AXIS_OFFSET_X + 3 * AXIS_WIDTH
				/ 4, AXIS_OFFSET_Y + AXIS_HEIGHT + AXIS_EXTENSION, RGBColour.GRAY);
		image.drawLine(AXIS_OFFSET_X + 4 * AXIS_WIDTH / 4, AXIS_OFFSET_Y - AXIS_EXTENSION, AXIS_OFFSET_X + 4 * AXIS_WIDTH
				/ 4, AXIS_OFFSET_Y + AXIS_HEIGHT + AXIS_EXTENSION, RGBColour.GRAY);

		// draw x-axes
		image.drawLine(AXIS_OFFSET_X - AXIS_EXTENSION, AXIS_OFFSET_Y + AXIS_HEIGHT,
				AXIS_OFFSET_X + AXIS_WIDTH + AXIS_EXTENSION, AXIS_OFFSET_Y + AXIS_HEIGHT, RGBColour.BLACK);
		image.drawLine(AXIS_OFFSET_X - AXIS_EXTENSION, AXIS_OFFSET_Y + 0 * AXIS_HEIGHT / 4,
				AXIS_OFFSET_X + AXIS_WIDTH + AXIS_EXTENSION, AXIS_OFFSET_Y + 0 * AXIS_HEIGHT / 4, RGBColour.GRAY);
		image.drawLine(AXIS_OFFSET_X - AXIS_EXTENSION, AXIS_OFFSET_Y + 1 * AXIS_HEIGHT / 4,
				AXIS_OFFSET_X + AXIS_WIDTH + AXIS_EXTENSION, AXIS_OFFSET_Y + 1 * AXIS_HEIGHT / 4, RGBColour.GRAY);
		image.drawLine(AXIS_OFFSET_X - AXIS_EXTENSION, AXIS_OFFSET_Y + 2 * AXIS_HEIGHT / 4,
				AXIS_OFFSET_X + AXIS_WIDTH + AXIS_EXTENSION, AXIS_OFFSET_Y + 2 * AXIS_HEIGHT / 4, RGBColour.GRAY);
		image.drawLine(AXIS_OFFSET_X - AXIS_EXTENSION, AXIS_OFFSET_Y + 3 * AXIS_HEIGHT / 4,
				AXIS_OFFSET_X + AXIS_WIDTH + AXIS_EXTENSION, AXIS_OFFSET_Y + 3 * AXIS_HEIGHT / 4, RGBColour.GRAY);

		// update the image
		imageComp.setImage(bimg = ImageUtilities.createBufferedImageForDisplay(image, bimg));
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// do nothing
	}

	/**
	 * Compute the mean of the image
	 *
	 * @param frame
	 * @param colourSpace
	 * @return
	 */
	public double[] computeMean(MBFImage frame, ColourSpace colourSpace) {
		final Circle hc = circle.clone();
		hc.scale(0.5f);
		final Rectangle bounds = hc.calculateRegularBoundingBox();

		frame = ResizeProcessor.halfSize(frame);

		final MBFImage cvt = colourSpace.convert(frame);
		final double[] vector = new double[colourSpace.getNumBands()];

		final SummaryStatistics stats = new SummaryStatistics();
		final Pixel pt = new Pixel();
		for (int b = 0; b < colourSpace.getNumBands(); b++) {
			stats.clear();

			final float[][] pix = cvt.getBand(b).pixels;

			for (pt.y = (int) bounds.y; pt.y < bounds.y + bounds.height; pt.y++) {
				for (pt.x = (int) bounds.x; pt.x < bounds.x + bounds.width; pt.x++) {
					if (hc.isInside(pt)) {
						stats.addValue(pix[pt.y][pt.x]);
					}
				}
			}

			vector[b] = stats.getMean();
		}
		return vector;
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		frame.flipX();

		lastMean = computeMean(frame, colourSpace);
		featureField.setText(formatVector(lastMean));

		redraw();
		doClassify(lastMean);

		frame.createRenderer(RenderHints.ANTI_ALIASED).drawShape(circle, 3, RGBColour.MAGENTA);
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
		new SlideshowApplication(new PerceptronBadTomatoDemo(App.class.getResource("slides/slides.005.jpg")),
				App.SLIDE_WIDTH, App.SLIDE_HEIGHT, App.getBackground());
	}
}
