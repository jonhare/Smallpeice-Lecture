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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.tracking.clm.CLMFaceTracker;
import org.openimaj.image.processing.face.tracking.clm.MultiTracker;
import org.openimaj.image.processing.face.tracking.clm.MultiTracker.TrackedFace;
import org.openimaj.image.renderer.RenderHints;
import org.openimaj.image.typography.FontStyle;
import org.openimaj.image.typography.FontStyle.HorizontalAlignment;
import org.openimaj.image.typography.general.GeneralFont;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

import uk.ac.soton.ecs.jsh2.lectures.utils.Simple3D;
import uk.ac.soton.ecs.jsh2.lectures.utils.VideoCaptureComponent;
import Jama.Matrix;

/**
 * Slide showing a 2.5D face tracker
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class CLMDemo implements Slide, VideoDisplayListener<MBFImage> {
	CLMFaceTracker tracker = new CLMFaceTracker();
	protected VideoCaptureComponent vc;
	private BufferedImage bgImage;

	public CLMDemo(URL bgImageUrl) throws IOException {
		this.bgImage = ImageIO.read(bgImageUrl);
	}

	@Override
	public JPanel getComponent(final int width, final int height) throws IOException {
		// the main panel
		final JPanel base = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent(Graphics g) {
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

				super.paintComponent(g);

				if (bgImage != null)
					g.drawImage(bgImage, 0, 0, width, height, null);
			}
		};
		base.setPreferredSize(new Dimension(width, height));
		base.setLayout(new GridBagLayout());
		vc = new VideoCaptureComponent(App.getVideoWidth(0), App.getVideoHeight(0), "FaceTime");
		vc.getDisplay().addVideoListener(this);
		base.add(vc);

		tracker.fcheck = true;
		tracker.model.getInitialVars().faceDetector.set_min_size(100);

		return base;
	}

	@Override
	public void close() {
		vc.close();
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// do nothing
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		tracker.track(frame);

		final FontStyle<Float[]> gfs = new GeneralFont("Courier", Font.PLAIN).createStyle(frame
				.createRenderer(RenderHints.ANTI_ALIASED));
		gfs.setFontSize(80);
		gfs.setColour(RGBColour.MAGENTA);
		gfs.setHorizontalAlignment(HorizontalAlignment.HORIZONTAL_CENTER);

		if (tracker.getTrackedFaces().size() > 0) {
			final TrackedFace tf = tracker.getTrackedFaces().get(0);
			drawFaceModel(frame, tf, tracker.triangles, tracker.connections);
			frame.drawText("TRACKING", frame.getWidth() / 2, frame.getHeight() - 10, gfs);
		} else {
			frame.drawText("SEARCHING", frame.getWidth() / 2, frame.getHeight() / 2, gfs);
		}
	}

	public static void drawFaceModel(final MBFImage image, final MultiTracker.TrackedFace f, final int[][] triangles,
			final int[][] connections)
	{
		final int n = f.shape.getRowDimension() / 2;
		final Matrix visi = f.clm._visi[f.clm.getViewIdx()];

		// draw connections
		for (int i = 0; i < connections[0].length; i++) {
			if (visi.get(connections[0][i], 0) == 0
					|| visi.get(connections[1][i], 0) == 0)
				continue;

			image.drawLine(
					new Point2dImpl((float) f.shape.get(connections[0][i], 0),
							(float) f.shape.get(connections[0][i] + n, 0)),
							new Point2dImpl((float) f.shape.get(connections[1][i], 0),
									(float) f.shape.get(connections[1][i] + n, 0)),
									5, RGBColour.BLUE);
		}

		final double[] shapeVector = f.clm._plocal.getColumnPackedCopy();

		final int middle = (int) (0.875 * image.getWidth());
		final int starty = 100;
		for (int i = 0; i < shapeVector.length; i++) {
			final int y = starty + i * 16;
			final int x = (int) (middle + shapeVector[i] * 6);
			image.drawLine(middle, y, x, y, 8, RGBColour.RED);
		}

		final double[] poseVector = f.clm._pglobl.getColumnPackedCopy();
		final double sc = poseVector[0];
		final double pitch = poseVector[1];
		final double roll = poseVector[2];
		final double yaw = poseVector[3];

		image.drawShape(new Rectangle(50, 100, image.getWidth() / 8, image.getHeight() / 8), 5, RGBColour.CYAN);
		image.drawShape(new Rectangle((int) (50 + poseVector[4] / 8 - 2 * sc), (int) (100 + poseVector[5] / 8 - 2 * sc),
				(int) (4 * sc), (int) (4 * sc)), 2, RGBColour.RED);

		final Matrix rpy = Simple3D.euler2Rot(pitch, roll, yaw);

		Line2d l1 = new Line2d(
				Simple3D.projectOrtho(rpy.times(new Matrix(new double[][] { { -50 }, { 0 }, { 0 } }))),
				Simple3D.projectOrtho(rpy.times(new Matrix(new double[][] { { 50 }, { 0 }, { 0 } })))
				);
		l1.translate(50 + image.getWidth() / 16, 300);
		image.drawLine(l1, 5, RGBColour.RED);

		l1 = new Line2d(
				Simple3D.projectOrtho(rpy.times(new Matrix(new double[][] { { 0 }, { -50 }, { 0 } }))),
				Simple3D.projectOrtho(rpy.times(new Matrix(new double[][] { { 0 }, { 50 }, { 0 } })))
				);
		l1.translate(50 + image.getWidth() / 16, 300);
		image.drawLine(l1, 5, RGBColour.BLUE);

		l1 = new Line2d(
				Simple3D.projectOrtho(rpy.times(new Matrix(new double[][] { { 0 }, { 0 }, { -50 } }))),
				Simple3D.projectOrtho(rpy.times(new Matrix(new double[][] { { 0 }, { 0 }, { 50 } })))
				);
		l1.translate(50 + image.getWidth() / 16, 300);
		image.drawLine(l1, 5, RGBColour.GREEN);
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new CLMDemo(App.class.getResource("slides/slides.014.jpg")), App.SLIDE_WIDTH,
				App.SLIDE_HEIGHT, App.getBackground());
	}
}
