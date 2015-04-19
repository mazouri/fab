/*
 * Copyright 2015 Shell Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * File created: 2015-03-30 16:58:43
 */

package com.software.shell.fab;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.util.Log;

/**
 * A class responsible for drawing the <b>Action Button</b> ripple effect
 *
 * @author shell
 * @version 1.1.0
 * @since 1.1.0
 */
class RippleDrawer {

	/**
	 * Logging tag
	 */
	private static final String LOG_TAG = String.format("[FAB][%s]", RippleDrawer.class.getSimpleName());

	/**
	 * Default value, which current {@link #currentRadius} is incremented by
	 */
	private static final int RADIUS_INCREMENT = 5;

	/**
	 * Delay, which is used to complete the ripple drawing
	 */
	private static final long POST_INVALIDATION_DELAY_MS = 100;

	/**
	 * <b>Action Button</b> instance
	 */
	private final ActionButton actionButton;

	/**
	 * Current ripple effect radius
	 */
	private int currentRadius;

	/**
	 * Creates the {@link RippleDrawer} instance
	 *
	 * @param actionButton <b>Action Button</b> instance
	 */
	RippleDrawer(ActionButton actionButton) {
		this.actionButton = actionButton;
	}

	/**
	 * Checks whether <b>Action Button</b> is in
	 * {@link com.software.shell.fab.ActionButton.State#PRESSED} state
	 *
	 * @return true if <b>Action Button</b> is in
	 *         {@link com.software.shell.fab.ActionButton.State#PRESSED} state,
	 *         otherwise false
	 */
	boolean isPressed() {
		return actionButton.getState() == ActionButton.State.PRESSED;
	}

	/**
	 * Checks whether ripple effect drawing is in progress
	 *
	 * @return true if ripple effect drawing is in progress, otherwise false
	 */
	boolean isDrawingInProgress() {
		return currentRadius > 0 && !isDrawingFinished();
	}

	/**
	 * Checks whether ripple effect drawing is finished
	 *
	 * @return true if ripple effect drawing is finished, otherwise false
	 */
	boolean isDrawingFinished() {
		return currentRadius >= getEndRippleRadius();
	}

	/**
	 * Returns the end ripple effect radius
	 *
	 * @return end ripple radius
	 */
	private int getEndRippleRadius() {
		return (int) (actionButton.calculateCircleRadius() * 2);
	}

	/**
	 * Updates the ripple effect {@link #currentRadius}
	 */
	private void updateRadius() {
		if (isPressed()) {
			if (currentRadius <= getEndRippleRadius()) {
				currentRadius += RADIUS_INCREMENT;
			}
		} else {
			if (isDrawingInProgress()) {
				currentRadius = getEndRippleRadius();
			} else if (isDrawingFinished()) {
				currentRadius = 0;
			}
		}
		Log.v(LOG_TAG, "Ripple effect radius updated to: " + currentRadius);
	}

	/**
	 * Performs the entire ripple effect drawing frame by frame animating the process
	 * <p>
	 * Calls the {@link ActionButton#postInvalidate()} after each {@link #currentRadius} update
	 * to draw the current frame animating the ripple effect drawing
	 *
	 * @param canvas canvas, which the ripple effect is drawing on
	 */
	void draw(Canvas canvas) {
		updateRadius();
		drawRipple(canvas);
		Invalidator invalidator = actionButton.getInvalidator();
		if (isDrawingInProgress()) {
			invalidator.setInvalidationRequired(true);
			Log.v(LOG_TAG, "Ripple effect drawing in progress, invalidating the Action Button");
		} else if (isDrawingFinished() && !isPressed()) {
			invalidator.setInvalidationDelayedRequired(true);
			invalidator.setInvalidationDelay(POST_INVALIDATION_DELAY_MS);
			Log.v(LOG_TAG, "Ripple effect drawing finished, posting the last invalidate");
		}
	}

	/**
	 * Draws the single frame of the ripple effect depending on ripple effect
	 * {@link #currentRadius}
	 *
	 * @param canvas canvas, which the ripple effect is drawing on
	 */
	private void drawRipple(Canvas canvas) {
		canvas.clipPath(getCircleClipPath(), Region.Op.INTERSECT);
		TouchPoint point = actionButton.getTouchPoint();
		canvas.drawCircle(point.getLastX(), point.getLastY(), currentRadius, getPreparedPaint());
		canvas.restore();
	}

	/**
	 * Returns the clipped path, which clips the ripple circle so that it doesn't goes beyond
	 * the <b>Action Button</b> circle
	 *
	 * @return clipped path, which clips the ripple circle
	 */
	private Path getCircleClipPath() {
		Path path = new Path();
		path.addCircle(actionButton.calculateCenterX(), actionButton.calculateCenterY(),
				actionButton.calculateCircleRadius(), Path.Direction.CW);
		return path;
	}

	/**
	 * Returns the paint, which is prepared for ripple effect drawing
	 *
	 * @return paint, which is prepared for ripple effect drawing
	 */
	private Paint getPreparedPaint() {
		actionButton.resetPaint();
		Paint paint = actionButton.paint;
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(actionButton.getButtonColorRipple());
		return paint;
	}

}
