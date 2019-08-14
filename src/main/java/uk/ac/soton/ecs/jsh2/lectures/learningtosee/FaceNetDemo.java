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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

import javax.swing.JPanel;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.feature.FloatFVComparison;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

import jep.JepException;
import uk.ac.soton.ecs.jsh2.lectures.learningtosee.utils.FaceNet;
import uk.ac.soton.ecs.jsh2.lectures.learningtosee.utils.FaceNet.FaceNetDetectedFace;
import uk.ac.soton.ecs.jsh2.lectures.utils.VideoCaptureComponent;

/**
 * Slide showing simple video capture and display
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class FaceNetDemo implements Slide, VideoDisplayListener<MBFImage> {
	class FaceNetWorker implements Runnable {
		private FaceNet fn;
		private SynchronousQueue<MBFImage> input = new SynchronousQueue<>();
		private SynchronousQueue<List<FaceNet.FaceNetDetectedFace>> output = new SynchronousQueue<>();
		private float[] je0;
		private float[] je1;
		private float[] je2;

		@Override
		public void run() {
			if (fn == null) {
				try {
					fn = new FaceNet(true);
					System.out.println("Model initialized " + Thread.currentThread());

					final MBFImage j0 = ImageUtilities.readMBF(FaceNet.class.getResource("jon0.png"));
					je0 = fn.detectFaces(j0).get(0).embeddingVector;
					final MBFImage j1 = ImageUtilities.readMBF(FaceNet.class.getResource("jon1.jpg"));
					je1 = fn.detectFaces(j1).get(0).embeddingVector;
					final MBFImage j2 = ImageUtilities.readMBF(FaceNet.class.getResource("jon2.jpg"));
					je2 = fn.detectFaces(j2).get(0).embeddingVector;
				} catch (JepException | IOException e) {
					e.printStackTrace();
				}
			}

			while (true) {
				MBFImage img;
				try {
					img = input.take();
					if (img != null) {
						output.put(fn.detectFaces(img));
					}
				} catch (final InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	protected VideoCaptureComponent vc;
	private String devName;
	private Thread bgThread;
	private FaceNetWorker worker;

	public FaceNetDemo(String devName) throws JepException, IOException {
		this.devName = devName;
		this.worker = new FaceNetWorker();
		this.bgThread = new Thread(worker);
		this.bgThread.start();
	}

	@Override
	public JPanel getComponent(int width, int height) throws IOException {
		return getComponent(width, height, null);
	}

	public JPanel getComponent(final int width, final int height, final BufferedImage bgImage) throws IOException {
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
		base.setOpaque(false);
		base.setPreferredSize(new Dimension(width, height));
		base.setLayout(new GridBagLayout());
		vc = new VideoCaptureComponent(App.getVideoWidth(0), App.getVideoHeight(0), devName);
		vc.getDisplay().addVideoListener(this);
		base.add(vc);

		return base;
	}

	@Override
	public void close() {
		vc.close();
	}

	public static void main(String[] args) throws IOException, JepException {
		new SlideshowApplication(new FaceNetDemo("FaceTime"), App.SLIDE_WIDTH, App.SLIDE_HEIGHT, App.getBackground());
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// do nothing
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		try {
			worker.input.put(frame);
			final List<FaceNetDetectedFace> dets = worker.output.take();

			for (final FaceNetDetectedFace r : dets) {
				frame.drawShape(r.getBounds(), RGBColour.RED);
				final double d0 = 0;// FloatFVComparison.EUCLIDEAN.compare(je0,
				// r.embeddingVector);
				final double d1 = FloatFVComparison.EUCLIDEAN.compare(worker.je1,
						r.embeddingVector);
				final double d2 = FloatFVComparison.EUCLIDEAN.compare(worker.je2,
						r.embeddingVector);

				if (d0 < 0.5 && d1 < 0.5 && d2 < 0.5) {
					frame.drawText("Jon", r.getBounds().getTopLeft(), HersheyFont.ROMAN_SIMPLEX, 14);
				}
			}
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}
}
