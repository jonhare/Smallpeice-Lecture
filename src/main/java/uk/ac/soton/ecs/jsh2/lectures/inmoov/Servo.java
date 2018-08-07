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

import org.openimaj.content.animation.animator.ValueAnimator;

/**
 * A simple servo
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class Servo {
	private int address;
	private int minPW = 544;
	private int maxPW = 2400;
	private int initialPW;
	private int pw;
	transient ValueAnimator<Integer> animator;

	public Servo(int address, int initialPW, int minPW, int maxPW) {
		super();
		this.address = address;
		this.initialPW = initialPW;
		this.minPW = minPW;
		this.maxPW = maxPW;
	}

	public int getInitialPW() {
		return initialPW;
	}

	public int getAddress() {
		return address;
	}

	public int getMinPW() {
		return minPW;
	}

	public int getMaxPW() {
		return maxPW;
	}

	int nextPW() {
		if (animator != null) {
			final int next = animator.nextValue();
			if (next == 0)
				return 0;
			pw = next;
		} else
			pw = initialPW;

		return pw;
	}

	public void setPW(int newPW) {
		if (newPW < minPW)
			newPW = minPW;
		if (newPW > maxPW)
			newPW = maxPW;

		animator = new ConstantIntegerAnimator(newPW);
	}

	public void setOff() {
		animator = new ConstantIntegerAnimator(0);
	}

	public void changePWRelative(int r) {
		final int newPW = pw + r;
		setPW(newPW);
	}

	public void increment() {
		increment(1);
	}

	public void increment(int i) {
		setPW(pw + i);
	}

	public void decrement() {
		decrement(1);
	}

	public void decrement(int i) {
		setPW(pw - i);
	}

	public int getPW() {
		return pw;
	}

	protected int randomAbsoluteValue() {
		return (int) ((Math.random() * (maxPW - minPW)) + minPW);
	}

	public void moveToRandom() {
		setPW(randomAbsoluteValue());
	}
}
