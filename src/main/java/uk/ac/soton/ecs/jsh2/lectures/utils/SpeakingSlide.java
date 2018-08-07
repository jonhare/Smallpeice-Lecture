package uk.ac.soton.ecs.jsh2.lectures.utils;

import java.awt.Component;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.openimaj.content.slideshow.PictureSlide;
import org.openimaj.image.MBFImage;

public class SpeakingSlide extends PictureSlide {
	final static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

	private String toSay;

	public SpeakingSlide(MBFImage mbfImage, String toSay) {
		super(mbfImage);
		this.toSay = toSay;
	}

	public SpeakingSlide(URL mbfImage, String toSay) throws IOException {
		super(mbfImage);
		this.toSay = toSay;
	}

	@Override
	public Component getComponent(int width, int height) throws IOException {
		final Component comp = super.getComponent(width, height);

		executor.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					Runtime.getRuntime().exec("say " + toSay).waitFor();
				} catch (InterruptedException | IOException e) {
					e.printStackTrace();
				}
			}
		}, 300, TimeUnit.MILLISECONDS);

		return comp;
	}
}
