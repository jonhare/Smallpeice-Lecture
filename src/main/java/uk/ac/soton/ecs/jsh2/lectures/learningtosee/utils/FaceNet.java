package uk.ac.soton.ecs.jsh2.lectures.learningtosee.utils;

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openimaj.feature.FloatFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.io.FileUtils;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.capture.VideoCapture;

import jep.Jep;
import jep.JepException;
import jep.MainInterpreter;
import jep.NDArray;
import jep.PyConfig;
import jep.SharedInterpreter;

public class FaceNet implements Closeable, FaceDetector<FaceNet.FaceNetDetectedFace, MBFImage> {
	public static class FaceNetDetectedFace extends DetectedFace {
		public float[] embeddingVector;

		/**
		 * @param bounds
		 * @param patch
		 * @param confidence
		 */
		public FaceNetDetectedFace(Rectangle bounds, float confidence) {
			super(bounds, null, confidence);
		}

	}

	private Jep jep;
	boolean computeEmbedding = false;

	public FaceNet() throws JepException, IOException {
		final String[] facenetpy = FileUtils.readlines(FaceNet.class.getResourceAsStream("facenet.py"));

		try {
			final PyConfig config = new PyConfig();
			config.setPythonHome("/anaconda3");
			MainInterpreter.setInitParams(config);
		} catch (final Exception e) {
			// ignore
		}

		jep = new SharedInterpreter();
		jep.setInteractive(true);
		for (final String line : facenetpy)
			jep.eval(line);
		jep.eval(null);
	}

	public FaceNet(boolean computeEmbedding) throws JepException, IOException {
		this();
		this.computeEmbedding = computeEmbedding;
	}

	private static Rectangle[] unpackBoxes(NDArray<double[]> boxes) {
		final int[] dims = boxes.getDimensions();
		final Rectangle[] rects = new Rectangle[dims[0]];
		final double[] data = boxes.getData();

		for (int i = 0; i < dims[0]; i++) {
			final double x1 = data[i * 4 + 0];
			final double y1 = data[i * 4 + 1];
			final double x2 = data[i * 4 + 2];
			final double y2 = data[i * 4 + 3];

			rects[i] = new Rectangle((float) x1, (float) y1, (float) (x2 - x1), (float) (y2 - y1));
		}

		return rects;
	}

	@Override
	public void close() throws IOException {
		try {
			jep.close();
		} catch (final JepException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void readBinary(DataInput in) throws IOException {

	}

	@Override
	public byte[] binaryHeader() {
		return null;
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FaceNetDetectedFace> detectFaces(MBFImage img) {
		final NDArray<byte[]> bytes = new NDArray<byte[]>(img.toByteImage());
		final Rectangle[] boxes;
		final List<Double> probs;
		final List<Object> result;

		try {
			final List<FaceNetDetectedFace> detections = new ArrayList<>();

			if (this.computeEmbedding) {
				result = (List<Object>) jep.invoke("detectEmbed", img.getWidth(), img.getHeight(), bytes);
			} else {
				result = (List<Object>) jep.invoke("detect", img.getWidth(), img.getHeight(), bytes);
			}

			if (result.get(0) == null) {
				return detections;
			}

			boxes = unpackBoxes((NDArray<double[]>) result.get(0));
			probs = (List<Double>) result.get(1);

			for (int i = 0; i < boxes.length; i++) {
				final FaceNetDetectedFace det = new FaceNetDetectedFace(boxes[i], probs.get(i).floatValue());
				detections.add(det);

				if (this.computeEmbedding)
					det.embeddingVector = extractEmbedding((NDArray<float[]>) result.get(2), i);
			}

			return detections;
		} catch (final JepException e) {
			throw new RuntimeException(e);
		}
	}

	private float[] extractEmbedding(NDArray<float[]> array, int idx) {
		final int[] shape = array.getDimensions();
		final int start = idx * shape[1] * shape[2];
		final int stop = start + shape[1] * shape[2];

		return Arrays.copyOfRange(array.getData(), start, stop);
	}

	public static void main(String[] args) throws JepException, IOException {
		final FaceNet fn = new FaceNet(false);

		final MBFImage j1 = ImageUtilities.readMBF(new File("/Users/jsh2/jon1.jpg"));
		final float[] je1 = fn.detectFaces(j1).get(0).embeddingVector;
		final MBFImage j2 = ImageUtilities.readMBF(new File("/Users/jsh2/jon2.jpg"));
		final float[] je2 = fn.detectFaces(j2).get(0).embeddingVector;

		final VideoCapture vc = new VideoCapture(640, 480);
		for (final MBFImage img : vc) {
			final List<FaceNetDetectedFace> dets = fn.detectFaces(img);

			for (final FaceNetDetectedFace r : dets) {
				img.drawShape(r.getBounds(), RGBColour.RED);
				final double d1 = FloatFVComparison.EUCLIDEAN.compare(je1,
						r.embeddingVector);
				final double d2 = FloatFVComparison.EUCLIDEAN.compare(je2,
						r.embeddingVector);

				if (d1 < 0.5 && d2 < 0.5) {
					img.drawText("Jon", r.getBounds().getTopLeft(), HersheyFont.ROMAN_SIMPLEX,
							14);
				}
			}
			DisplayUtilities.displayName(img, "Detections");
		}

		vc.close();
		fn.close();
	}
}
