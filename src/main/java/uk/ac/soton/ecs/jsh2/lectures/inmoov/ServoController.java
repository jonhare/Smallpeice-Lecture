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
package uk.ac.soton.ecs.jsh2.lectures.inmoov;

import java.io.Closeable;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jssc.SerialPort;

import org.openimaj.hardware.serial.SerialDevice;

/**
 * A simple servo controller that talks to an arduino.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ServoController implements Runnable, Closeable {
	public static final int FREQUENCY = 100; // aim for 100Hz
	private transient Map<Servo, Integer> servos = new HashMap<Servo, Integer>();
	protected transient boolean isRunning = true;
	private SerialDevice device;
	private Thread thread;

	public ServoController() throws Exception {
		this(findSerialPort());
	}

	private static String findSerialPort() {
		final String[] files = new File("/dev").list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("tty.usbmodem") || name.startsWith("tty.usbserial");
			}
		});

		return "/dev/" + files[0];
	}

	public ServoController(String port) throws Exception {
		device = new SerialDevice(port,
				115200,
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
	}

	public void start() {
		thread = new Thread(this);
		thread.setDaemon(true);
		thread.start();
	}

	public synchronized void registerServo(Servo servo) {
		final int init = servo.getInitialPW();
		servos.put(servo, 0);
		servo.setPW(init);
		// setServo(servo.getAddress(), init);
	}

	private synchronized void setServo(int address, int pw) {
		try {
			// System.out.println(String.format("%d %d\n", address, pw));
			final byte[] cmd = String.format("%d %d\n", address, pw).getBytes("US-ASCII");
			device.getOutputStream().write(cmd);
			device.getOutputStream().flush();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		final int allowedTime = 1000 / FREQUENCY;

		while (isRunning) {
			synchronized (this) {
				final long t1 = System.currentTimeMillis();
				for (final Entry<Servo, Integer> entry : servos.entrySet()) {
					final Servo servo = entry.getKey();
					final int lastPW = entry.getValue();
					final int newPW = servo.nextPW();
					if (lastPW != newPW) {
						entry.setValue(newPW);
						// System.out.println("setting");
						setServo(servo.getAddress(), newPW);
					}
				}
				final long t2 = System.currentTimeMillis();

				final long sleepTime = allowedTime - (t2 - t1);

				if (sleepTime > 0) {
					try {
						Thread.sleep(sleepTime);
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					// System.err.println("Sleeptime = " + sleepTime);
				}
			}
		}

		// close the device on stop
		try {
			System.out.println("device closing");
			device.close();
		} catch (final IOException e) {
		}
	}

	@Override
	public void close() {
		System.out.println("controller closing");
		isRunning = false;
	}
}
