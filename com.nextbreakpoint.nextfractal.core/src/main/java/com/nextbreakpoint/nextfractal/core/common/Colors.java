/*
 * NextFractal 2.3.2
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
package com.nextbreakpoint.nextfractal.core.common;

import java.awt.*;

/**
 * Utility class for colors manipulation.
 */
public class Colors {
	/**
	 * Extracts the alpha component.
	 * 
	 * @param argb the color in argb format.
	 * @return the alpha component.
	 */
	public static int alpha(final int argb) {
		return (argb >> 24) & 0xFF;
	}

	/**
	 * Extracts the red component.
	 * 
	 * @param argb the color in argb format.
	 * @return the red component.
	 */
	public static int red(final int argb) {
		return (argb >> 16) & 0xFF;
	}

	/**
	 * Extracts the green component.
	 * 
	 * @param argb the color in argb format.
	 * @return the green component.
	 */
	public static int green(final int argb) {
		return (argb >> 8) & 0xFF;
	}

	/**
	 * Extracts the blue component.
	 * 
	 * @param argb the color in argb format.
	 * @return the blue component.
	 */
	public static int blue(final int argb) {
		return argb & 0xFF;
	}

	/**
	 * @param argb
	 * @return
	 */
	public static float[] asFloats(final int argb) {
		final float[] argbArray = new float[4];
		argbArray[0] = (0xFF & (argb >> 24)) / 255f;
		argbArray[1] = (0xFF & (argb >> 16)) / 255f;
		argbArray[2] = (0xFF & (argb >> 8)) / 255f;
		argbArray[3] = (0xFF & argb) / 255f;
		return argbArray;
	}

	/**
	 * @param argb
	 * @return
	 */
	public static byte[] asBytes(final int argb) {
		final byte[] argbArray = new byte[] { 0, 0, 0, 0 };
		argbArray[0] = (byte) (0xFF & (argb >> 24));
		argbArray[1] = (byte) (0xFF & (argb >> 16));
		argbArray[2] = (byte) (0xFF & (argb >> 8));
		argbArray[3] = (byte) (0xFF & argb);
		return argbArray;
	}

	/**
	 * @param a
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public static int makeColor(final float a, final float r, final float g, final float b) {
		final int ca = ((int) Math.rint(a * 255)) & 0xFF;
		final int cr = ((int) Math.rint(r * 255)) & 0xFF;
		final int cg = ((int) Math.rint(g * 255)) & 0xFF;
		final int cb = ((int) Math.rint(b * 255)) & 0xFF;
		return (ca << 24) | (cr << 16) | (cg << 8) | cb;
	}

	/**
	 * @param argbArray
	 * @return
	 */
	public static int makeColor(final float[] argbArray) {
		final int ca = ((int) Math.rint(argbArray[0] * 255)) & 0xFF;
		final int cr = ((int) Math.rint(argbArray[1] * 255)) & 0xFF;
		final int cg = ((int) Math.rint(argbArray[2] * 255)) & 0xFF;
		final int cb = ((int) Math.rint(argbArray[3] * 255)) & 0xFF;
		return (ca << 24) | (cr << 16) | (cg << 8) | cb;
	}

	/**
	 * @param a
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public static int makeColor(final byte a, final byte r, final byte g, final byte b) {
		final int ca = ((int) a) & 255;
		final int cr = ((int) r) & 255;
		final int cg = ((int) g) & 255;
		final int cb = ((int) b) & 255;
		return (0xFF000000 & (ca << 24)) | (0xFF0000 & (cr << 16)) | (0xFF00 & (cg << 8)) | (0xFF & cb);
	}

	/**
	 * @param argbArray
	 * @return
	 */
	public static int makeColor(final byte[] argbArray) {
		final int ca = ((int) argbArray[0]) & 255;
		final int cr = ((int) argbArray[1]) & 255;
		final int cg = ((int) argbArray[2]) & 255;
		final int cb = ((int) argbArray[3]) & 255;
		return (0xFF000000 & (ca << 24)) | (0xFF0000 & (cr << 16)) | (0xFF00 & (cg << 8)) | (0xFF & cb);
	}

	/**
	 * @param alpha
	 * @return
	 */
	public static int makeColor(final byte alpha, final int argb) {
		final int ca = ((int) alpha) & 255;
		return (0xFF000000 & (ca << 24)) | (argb & 0xFFFFFF);
	}

	/**
	 * @param alpha
	 * @param grayLevel
	 * @return
	 */
	public static int makeColor(final byte alpha, final byte grayLevel) {
		return makeColor(alpha, grayLevel, grayLevel, grayLevel);
	}

	/**
	 * @param rgb
	 * @param hsbArray
	 */
	public static void toHSB(final int rgb, final float[] hsbArray) {
		Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, hsbArray);
	}

	/**
	 * @param hsbArray
	 * @return
	 */
	public static int fromHSB(final float[] hsbArray) {
		return (0xFF << 24) | Color.HSBtoRGB(hsbArray[0], hsbArray[1], hsbArray[2]);
	}

	/**
	 * Mixs two colors.
	 *
	 * @param argb1 the first color in argb format.
	 * @param argb2 the second color in argb format.
	 * @param alpha the alpha component.
	 * @return the mixed color.
	 */
	public static int mixColors(final int argb1, final int argb2, final int alpha) {
		if (alpha == 0) {
			return argb1;
		}
		if (alpha == 255) {
			return argb2;
		}
		if (alpha == 127) {
			return (((argb1 & 0xFEFEFEFE) >> 1) + ((argb2 & 0xFEFEFEFE) >> 1));
		}
		final int a = ((alpha * (((argb2 >> 24) & 255) - ((argb1 >> 24) & 255))) >> 8) + ((argb1 >> 24) & 255);
		final int r = ((alpha * (((argb2 >> 16) & 255) - ((argb1 >> 16) & 255))) >> 8) + ((argb1 >> 16) & 255);
		final int g = ((alpha * (((argb2 >> 8) & 255) - ((argb1 >> 8) & 255))) >> 8) + ((argb1 >> 8) & 255);
		final int b = ((alpha * ((argb2 & 255) - (argb1 & 255))) >> 8) + (argb1 & 255);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	/**
	 * Fills the colors table.
	 * 
	 * @param table the colors table to fill.
	 * @param offset the offset.
	 * @param length the length.
	 * @param argb1 the first color.
	 * @param argb2 the last color.
	 * @return the colors table.
	 */
	public static int[] makeTable(final int[] table, final int offset, final int length, final int argb1, final int argb2) {
		final double[] delta = new double[4];
		final int[] value = new int[4];
		delta[0] = ((double) (Colors.alpha(argb2) - Colors.alpha(argb1))) / (double) length;
		delta[1] = ((double) (Colors.red(argb2) - Colors.red(argb1))) / (double) length;
		delta[2] = ((double) (Colors.green(argb2) - Colors.green(argb1))) / (double) length;
		delta[3] = ((double) (Colors.blue(argb2) - Colors.blue(argb1))) / (double) length;
		assert (table.length < offset + length);
		for (int k = 0; k < length; k++) {
			value[0] = (int) Math.round(delta[0] * k);
			value[1] = (int) Math.round(delta[1] * k);
			value[2] = (int) Math.round(delta[2] * k);
			value[3] = (int) Math.round(delta[3] * k);
			value[0] = (value[0] < 0) ? 0 : Math.min(value[0], 255);
			value[1] = (value[1] < 0) ? 0 : Math.min(value[1], 255);
			value[2] = (value[2] < 0) ? 0 : Math.min(value[2], 255);
			value[3] = (value[3] < 0) ? 0 : Math.min(value[3], 255);
			table[offset + k] = (value[0] << 24) | (value[1] << 16) | (value[2] << 8) | value[3];
		}
		return table;
	}

	/**
	 * Fills the colors table.
	 * 
	 * @param table the colors table to fill.
	 * @param offset the offset.
	 * @param length the length.
	 * @param argb1 the first color.
	 * @param argb2 the last color.
	 * @param AV the alpha component modulation (same size as length parameter).
	 * @param RV the red component modulation (same size as length parameter).
	 * @param GV the green component modulation (same size as length parameter).
	 * @param BV the blue component modulation (same size as length parameter).
	 * @return the colors table.
	 */
	public static int[] makeTable(final int[] table, final int offset, final int length, final int argb1, final int argb2, final double[] AV, final double[] RV, final double[] GV, final double[] BV) {
		final double[] delta = new double[4];
		final int[] value = new int[4];
		delta[0] = (Colors.alpha(argb2) - Colors.alpha(argb1));
		delta[1] = (Colors.red(argb2) - Colors.red(argb1));
		delta[2] = (Colors.green(argb2) - Colors.green(argb1));
		delta[3] = (Colors.blue(argb2) - Colors.blue(argb1));
		assert (AV.length == length);
		assert (RV.length == length);
		assert (GV.length == length);
		assert (BV.length == length);
		assert (table.length >= offset + length);
		final int a = Colors.alpha(argb1);
		final int r = Colors.red(argb1);
		final int g = Colors.green(argb1);
		final int b = Colors.blue(argb1);
		for (int k = 0; k < length; k++) {
			value[0] = (int) Math.rint(a + delta[0] * AV[k]);
			value[1] = (int) Math.rint(r + delta[1] * RV[k]);
			value[2] = (int) Math.rint(g + delta[2] * GV[k]);
			value[3] = (int) Math.rint(b + delta[3] * BV[k]);
			value[0] = (value[0] < 0) ? 0 : Math.min(value[0], 255);
			value[1] = (value[1] < 0) ? 0 : Math.min(value[1], 255);
			value[2] = (value[2] < 0) ? 0 : Math.min(value[2], 255);
			value[3] = (value[3] < 0) ? 0 : Math.min(value[3], 255);
			table[offset + k] = (value[0] << 24) | (value[1] << 16) | (value[2] << 8) | value[3];
		}
		return table;
	}
}
