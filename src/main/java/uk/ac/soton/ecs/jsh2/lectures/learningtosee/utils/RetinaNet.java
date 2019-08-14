package uk.ac.soton.ecs.jsh2.lectures.learningtosee.utils;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.objectdetection.ObjectDetector;
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

public class RetinaNet implements Closeable, ObjectDetector<MBFImage, RetinaNet.RetinaNetDetection> {
	public static class RetinaNetDetection {
		public Rectangle bounds;
		public String clz;
		public double score;

		public RetinaNetDetection(Rectangle bounds, double score, String clz) {
			this.bounds = bounds;
			this.clz = clz;
			this.score = score;
		}
	}

	private Jep jep;
	boolean computeEmbedding = false;
	String[] labels_to_names = { "person", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat",
			"traffic light", "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat", "dog", "horse",
			"sheep", "cow", "elephant", "bear", "zebra", "giraffe", "backpack", "umbrella", "handbag", "tie", "suitcase",
			"frisbee", "skis", "snowboard", "sports ball", "kite", "baseball bat", "baseball glove", "skateboard",
			"surfboard", "tennis racket", "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana",
			"apple", "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair", "couch",
			"potted plant", "bed", "dining table", "toilet", "tv", "laptop", "mouse", "remote", "keyboard", "cell phone",
			"microwave", "oven", "toaster", "sink", "refrigerator", "book", "clock", "vase", "scissors", "teddy bear",
			"hair drier", "toothbrush" };

	public RetinaNet() throws JepException, IOException {
		final String[] facenetpy = FileUtils.readlines(RetinaNet.class.getResourceAsStream("retinanet.py"));

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

	private static Rectangle[] unpackBoxes(NDArray<float[]> boxes) {
		final int[] dims = boxes.getDimensions();
		final Rectangle[] rects = new Rectangle[dims[0]];
		final float[] data = boxes.getData();

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
	public List<RetinaNetDetection> detect(MBFImage image) {
		final NDArray<byte[]> bytes = new NDArray<byte[]>(image.toByteImage());
		final Rectangle[] boxes;
		final List<Double> scores;
		final List<Long> labels;
		final List<Object> result;

		try {
			final List<RetinaNetDetection> detections = new ArrayList<>();

			result = (List<Object>) jep.invoke("detect", image.getWidth(), image.getHeight(), bytes);

			boxes = unpackBoxes((NDArray<float[]>) result.get(0));
			scores = (List<Double>) result.get(1);
			labels = (List<Long>) result.get(2);

			for (int i = 0; i < boxes.length; i++) {
				if (scores.get(i) > 0.5) {
					final int idx = labels.get(i).intValue();
					String clz = "";
					if (idx >= 0)
						clz = labels_to_names[idx];
					System.out.println(clz);
					System.out.println(boxes[i]);
					final RetinaNetDetection det = new RetinaNetDetection(boxes[i], scores.get(i),
							clz);
					detections.add(det);
				}
			}

			return detections;
		} catch (final JepException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setROI(Rectangle roi) {

	}

	public static void main(String[] args) throws JepException, IOException {
		final RetinaNet fn = new RetinaNet();

		final VideoCapture vc = new VideoCapture(640, 480);
		for (final MBFImage img : vc) {
			final List<RetinaNetDetection> dets = fn.detect(img);

			for (final RetinaNetDetection r : dets) {
				img.drawShape(r.bounds, RGBColour.RED);
				img.drawText(r.clz, r.bounds.getTopLeft(), HersheyFont.ROMAN_SIMPLEX, 14, RGBColour.RED);
			}
			DisplayUtilities.displayName(img, "Detections");
		}

		vc.close();
		fn.close();
	}
}
