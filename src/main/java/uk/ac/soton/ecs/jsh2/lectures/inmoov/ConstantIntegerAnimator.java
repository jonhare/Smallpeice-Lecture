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

import org.openimaj.content.animation.animator.AbstractValueAnimator;

/**
 * A {@link org.openimaj.content.animation.animator.ValueAnimator} that is
 * constant for a fixed period.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ConstantIntegerAnimator extends AbstractValueAnimator<Integer> {
	private int duration = -1;
	private int count = 0;
	private int value;

	public ConstantIntegerAnimator(int initial) {
		super(initial, 0, 0);
		this.value = initial;
	}

	public ConstantIntegerAnimator(int initial, int duration) {
		super(initial, 0, 0);
		this.duration = duration;
		this.value = initial;
	}

	public ConstantIntegerAnimator(int initial, int startWait, int stopWait) {
		super(initial, startWait, stopWait);
		this.value = initial;
	}

	public ConstantIntegerAnimator(int initial, int startWait, int stopWait, int duration) {
		super(initial, startWait, stopWait);
		this.duration = duration;
		this.value = initial;
	}

	@Override
	protected Integer makeNextValue() {
		count++;
		return value;
	}

	@Override
	protected void resetToInitial() {
		// Do nothing
	}

	@Override
	protected boolean complete() {
		if (duration > 0 && count >= duration)
			return true;
		return false;
	}
}
