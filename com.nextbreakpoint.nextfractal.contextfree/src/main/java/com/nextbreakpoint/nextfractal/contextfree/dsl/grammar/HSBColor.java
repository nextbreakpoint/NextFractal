/*
 * NextFractal 2.3.1
 * https://github.com/nextbreakpoint/nextfractal
 *
 * Copyright 2015-2024 Andrea Medeghini
 *
 * This file is part of NextFractal.
 *
 * NextFractal is an application for creating fractals and other graphics artifacts.
 *
 * NextFractal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NextFractal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NextFractal.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.nextbreakpoint.nextfractal.contextfree.dsl.grammar;

import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.AssignmentType;

import java.util.Arrays;

public class HSBColor implements Cloneable {
	private static final double EQUALITY_THRESHOLD = 0.00001;

	private double[] values = new double[4];
	private double[] rgba;

	public HSBColor(double h, double s, double b, double a) {
		this.values[0] = h;
		this.values[1] = s;
		this.values[2] = b;
		this.values[3] = a;
	}

	public HSBColor(double[] rgb) {
		double min = myfmin(rgb[0], myfmin(rgb[1], rgb[2]));
		double max = myfmax(rgb[0], myfmax(rgb[1], rgb[2]));
		double delta = max - min;
		values[2] = max;
		if (delta < EQUALITY_THRESHOLD) {
			values[0] = values[1] = 0.0;
		} else {
			values[1] = delta / values[2];  // hsb.b can't be zero here

			// The == operator is normally useless for floats and doubles. But
			// since max is always assigned fromType either rgb.r/g/b we will take
			// a chance.
			double temp;
			if (rgb[0] == max) {
				temp = (rgb[1] - rgb[2]) / (delta);
			} else if (rgb[1] == max) {
				temp = 2 + ((rgb[2] - rgb[0]) / (delta));
			} else /* if (rgb[2] == max) */ {
				temp = 4 + ((rgb[0] - rgb[1]) / (delta));
			}

			// compute hue in the interval [0,360)
			temp *= 60;
			values[0] = temp < 0.0 ? ((temp + 360.0) % 360.0) : (temp % 360.0);
		}
		values[3] = rgb[3];
	}

	private static double myfmin(double x, double y) { return x < y ? x : y; }
	private static double myfmax(double x, double y) { return x > y ? x : y; }

	public double[] mymodf(double x, double[] v) {
		int intVal = (int)x;
		double remainder = x - intVal;

		v[0] = intVal;
		v[1] = remainder;

		return v;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		HSBColor hsbColor = (HSBColor) o;

		return Arrays.equals(values, hsbColor.values);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(values);
	}

	public double hue() {
		return values[0];
	}
	
	public double sat() {
		return values[1];
	}

	public double bright() {
		return values[2];
	}

	public double alpha() {
		return values[3];
	}

	public void setHue(double value) {
		rgba = null;
		values[0] = value;
	}

	public void setSat(double value) {
		rgba = null;
		values[1] = value;
	}

	public void setBright(double value) {
		rgba = null;
		values[2] = value;
	}

	public void setAlpha(double value) {
		rgba = null;
		values[3] = value;
	}

	public void addHue(double value) {
		rgba = null;
		values[0] += value;
	}

	public void addSat(double value) {
		rgba = null;
		values[1] += value;
	}

	public void addBright(double value) {
		rgba = null;
		values[2] += value;
	}

	public void addAlpha(double value) {
		rgba = null;
		values[3] += value;
	}

	public double[] values() {
		return values;
	}

	public void setValues(double[] values) {
		rgba = null;
		this.values = values;
	}

	public static double adjustHue(double base, double adjustment) {
		return adjustHue(base, adjustment, 0, 0);
	}

	public static double adjust(double base, double adjustment) {
		return adjust(base, adjustment, 0, 0);
	}

	public static double adjustHue(double base, double adjustment, int useTarget, double target) {
		if (adjustment == 0.0) {
			return base;
		}

		double h = 0;

		if (useTarget != 0) {
			// decrease or increase toward target. If the target hue does not
			// cooperate by being smaller (or larger) than the current hue then
			// add/subtract 360 to make it so. This only works if all hues are
			// within the interval [0,360).
			double t = target;
			if (adjustment < 0) {
				if (t > base) t -= 360;
				h = base + (base - t) * adjustment;
			} else {
				if (t < base) t += 360;
				h = base + (t - base) * adjustment;
			}
		} else {
			h = base + adjustment;
		}

		// Normalize result to the interval [0,360)
		return h < 0.0 ? ((h + 360.0) % 360.0) : (h % 360.0);
	}

	public static double adjust(double base, double adjustment, int useTarget, double target) {
		if (adjustment == 0.0) {
			return base;
		}

		if (useTarget != 0) {
			// If we are really close to the target then don't change, even if
			// the adjustment is negative (which way would we go?)
			if (adjustment > 0 && Math.abs(base - target) < EQUALITY_THRESHOLD) {
				return base;
			}

			// Otherwise move away fromType or toward the target
			double edge = base < target ? 0 : 1;
			if (adjustment < 0) {
				return base + (base - edge) * adjustment;
			} else {
				return base + (target - base) * adjustment;
			}
		} else {
			// Move toward 0 or toward 1
			if (adjustment < 0) {
				return base + base * adjustment;
			} else {
				return base + (1 - base) * adjustment;
			}
		}
	}

	public static void adjust(HSBColor dest, HSBColor destTarget, HSBColor adjustment, HSBColor adjustmentTarget, int assignment) {
		int current = 0;
		boolean twoValue = false;

		current = assignment & AssignmentType.HueMask.getType();
		twoValue = current == AssignmentType.Hue2Value.getType();
		dest.setHue(adjustHue(dest.hue(), adjustment.hue(), current, twoValue ? adjustmentTarget.hue() : destTarget.hue()));
		if (!twoValue) destTarget.setHue(adjustHue(destTarget.hue(), adjustmentTarget.hue()));

		current = assignment & AssignmentType.SaturationMask.getType();
		twoValue = current == AssignmentType.Saturation2Value.getType();
		dest.setSat(adjust(dest.sat(), adjustment.sat(), current, twoValue ? adjustmentTarget.sat() : destTarget.sat()));
		if (!twoValue) destTarget.setSat(adjust(destTarget.sat(), adjustmentTarget.sat()));

		current = assignment & AssignmentType.BrightnessMask.getType();
		twoValue = current == AssignmentType.Brightness2Value.getType();
		dest.setBright(adjust(dest.bright(), adjustment.bright(), current, twoValue ? adjustmentTarget.bright() : destTarget.bright()));
		if (!twoValue) destTarget.setBright(adjust(destTarget.bright(), adjustmentTarget.bright()));

		current = assignment & AssignmentType.AlphaMask.getType();
		twoValue = current == AssignmentType.Alpha2Value.getType();
		dest.setAlpha(adjust(dest.alpha(), adjustment.alpha(), current, twoValue ? adjustmentTarget.alpha() : destTarget.alpha()));
		if (!twoValue) destTarget.setAlpha(adjust(destTarget.alpha(), adjustmentTarget.alpha()));
	}

	public double[] getRGBA() {
		if (rgba != null) {
			return rgba;
		}

		double[] c = new double[4];

		// Determine which facet of the HSB hexcone we are in and how far we are into this hextant.
		double hue = values[0] / 60.0;
		double remainder = 0;
		double[] hex = new double[] {0, 0};

		for(int i = 0; i < 2; ++i) {
			// try splitting the hue into an integer hextant in [0,6) and a real remainder in [0,1)
			hex = mymodf(hue, hex);
			remainder = hex[1];
			if (hex[0] > -0.1 && hex[0] < 5.1 && remainder >= 0) {
				break;
			}

			// We didn't get the ranges that we wanted. Adjust hue and try again.
			hue = hue % 6.0;
			if (hue < 0.0) {
				hue += 6.0;
			}
		}

		int hextant = (int)(hex[0] + 0.5); // guaranteed to be in 0..5

		double b = values[2];
		double p = values[2] * (1 - values[1]);
		double q = values[2] * (1 - (values[1] * remainder));
		double t = values[2] * (1 - (values[1] * (1 - remainder)));

		c[3] = values[3];

		switch (hextant) {
			case 0:
				c[0] = b; c[1] = t; c[2] = p;
				return c;
			case 1:
				c[0] = q; c[1] = b; c[2] = p;
				return c;
			case 2:
				c[0] = p; c[1] = b; c[2] = t;
				return c;
			case 3:
				c[0] = p; c[1] = q; c[2] = b;
				return c;
			case 4:
				c[0] = t; c[1] = p; c[2] = b;
				return c;
			case 5:
				c[0] = b; c[1] = p; c[2] = q;
				return c;
			default:
				// this should never happen
				c[0] = 0; c[1] = 0; c[2] = 0; c[3] = 1;
				return c;
		}
	}

	public Object clone() {
		return new HSBColor(values[0], values[1], values[2], values[3]);
	}
}
