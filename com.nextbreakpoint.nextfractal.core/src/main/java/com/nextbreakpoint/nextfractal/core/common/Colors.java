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
package com.nextbreakpoint.nextfractal.core.common;

import java.awt.*;

/**
 * Utility class for colors manipulation.
 * 
 * @author Andrea Medeghini
 */
public class Colors {
	/**
	 * Extracts the alpha component.
	 * 
	 * @param argb the color in argb format.
	 * @return the alpha component.
	 */
	public static int getAlpha(final int argb) {
		return (argb >> 24) & 0xFF;
	}

	/**
	 * Extracts the red component.
	 * 
	 * @param argb the color in argb format.
	 * @return the red component.
	 */
	public static int getRed(final int argb) {
		return (argb >> 16) & 0xFF;
	}

	/**
	 * Extracts the green component.
	 * 
	 * @param argb the color in argb format.
	 * @return the green component.
	 */
	public static int getGreen(final int argb) {
		return (argb >> 8) & 0xFF;
	}

	/**
	 * Extracts the blue component.
	 * 
	 * @param argb the color in argb format.
	 * @return the blue component.
	 */
	public static int getBlue(final int argb) {
		return (argb >> 0) & 0xFF;
	}

	/**
	 * @param rgb
	 * @param hsbvals
	 */
	public static void RGBtoHSB(int rgb, float[] hsbvals) {
		Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, (rgb >> 0) & 0xFF, hsbvals);
	}

	/**
	 * @param alpha
	 * @param hsbvals
	 * @return
	 */
	public static int HSBtoRGB(int alpha, float[] hsbvals) {
		return ((alpha & 0xFF) << 24) | Color.HSBtoRGB(hsbvals[0], hsbvals[1], hsbvals[2]);
	}

	/**
	 * @param components
	 * @return
	 */
	public static int toARGB(float[] components) {
		return ((0xFF & ((int)(components[0] * 255))) << 24) | ((0xFF & ((int)(components[1] * 255))) << 16) | ((0xFF & ((int)(components[2] * 255))) << 8) | ((0xFF & ((int)(components[3] * 255))) << 0);
	}

	/**
	 * Mixs two colors.
	 * 
	 * @param color1 the first color in argb format.
	 * @param color2 the second color in argb format.
	 * @param alpha the alpha component.
	 * @return the mixed color.
	 */
	public static int mixColors(final int color1, final int color2, final int alpha) {
		if (alpha == 0) {
			return color1;
		}
		if (alpha == 255) {
			return color2;
		}
		if (alpha == 127) {
			return (((color1 & 0xFEFEFEFE) >> 1) + ((color2 & 0xFEFEFEFE) >> 1));
		}
		final int a = ((alpha * (((color2 >> 24) & 255) - ((color1 >> 24) & 255))) >> 8) + ((color1 >> 24) & 255);
		final int r = ((alpha * (((color2 >> 16) & 255) - ((color1 >> 16) & 255))) >> 8) + ((color1 >> 16) & 255);
		final int g = ((alpha * (((color2 >> 8) & 255) - ((color1 >> 8) & 255))) >> 8) + ((color1 >> 8) & 255);
		final int b = ((alpha * ((color2 & 255) - (color1 & 255))) >> 8) + (color1 & 255);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	/**
	 * @param components
	 * @return
	 */
	public static int color(final float[] components) {
		final int a = ((int)(components[0] * 255)) & 0xFF;
		final int r = ((int)(components[1] * 255)) & 0xFF;
		final int g = ((int)(components[2] * 255)) & 0xFF;
		final int b = ((int)(components[3] * 255)) & 0xFF;
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	/**
	 * @param alpha
	 * @return
	 */
	public static int color(final int alpha, final int rgb) {
		final int a = alpha & 255;
		return (a << 24) | (rgb & 0xFFFFFF);
	}

	/**
	 * @param a
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public static int color(final byte a, final byte r, final byte g, final byte b) {
		return (0xFF000000 & ((a) << 24)) | (0xFF0000 & ((r) << 16)) | (0xFF00 & ((g) << 8)) | (0xFF & (b));
	}

	/**
	 * @param alpha
	 * @return
	 */
	public static int gray(final int alpha, final int level) {
		final int a = alpha & 255;
		final int l = level & 255;
		return (a << 24) | (l << 16) | (l << 8) | l;
	}

	/**
	 * @param argb
	 * @return
	 */
	public static float[] color(final int argb) {
		float[] components = new float[] { 0f, 0f, 0f, 0f };
		components[0] = (0xFF & (argb >> 24)) / 255f;
		components[1] = (0xFF & (argb >> 16)) / 255f;
		components[2] = (0xFF & (argb >> 8)) / 255f;
		components[3] = (0xFF & (argb >> 0)) / 255f;
		return components;
	}

	/**
	 * Fills the colors table.
	 * 
	 * @param table the colors table to fill.
	 * @param offset the offset.
	 * @param length the length.
	 * @param color0 the first color.
	 * @param color1 the last color.
	 * @return the colors table.
	 */
	public static int[] fillTable(final int[] table, final int offset, final int length, final int color0, final int color1) {
		final double[] delta = new double[4];
		final int[] value = new int[4];
		delta[0] = ((double) (Colors.getAlpha(color1) - Colors.getAlpha(color0))) / (double) length;
		delta[1] = ((double) (Colors.getRed(color1) - Colors.getRed(color0))) / (double) length;
		delta[2] = ((double) (Colors.getGreen(color1) - Colors.getGreen(color0))) / (double) length;
		delta[3] = ((double) (Colors.getBlue(color1) - Colors.getBlue(color0))) / (double) length;
		assert (table.length < offset + length);
		for (int k = 0; k < length; k++) {
			value[0] = (int) Math.round(delta[0] * k);
			value[1] = (int) Math.round(delta[1] * k);
			value[2] = (int) Math.round(delta[2] * k);
			value[3] = (int) Math.round(delta[3] * k);
			value[0] = (value[0] < 0) ? 0 : ((value[0] > 255) ? 255 : value[0]);
			value[1] = (value[1] < 0) ? 0 : ((value[1] > 255) ? 255 : value[1]);
			value[2] = (value[2] < 0) ? 0 : ((value[2] > 255) ? 255 : value[2]);
			value[3] = (value[3] < 0) ? 0 : ((value[3] > 255) ? 255 : value[3]);
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
	 * @param color0 the first color.
	 * @param color1 the last color.
	 * @param AV the alpha component modulation (same size as length parameter).
	 * @param RV the red component modulation (same size as length parameter).
	 * @param GV the green component modulation (same size as length parameter).
	 * @param BV the blue component modulation (same size as length parameter).
	 * @return the colors table.
	 */
	public static int[] fillTable(final int[] table, final int offset, final int length, final int color0, final int color1, final double[] AV, final double[] RV, final double[] GV, final double[] BV) {
		final double[] delta = new double[4];
		final int[] value = new int[4];
		delta[0] = (Colors.getAlpha(color1) - Colors.getAlpha(color0));
		delta[1] = (Colors.getRed(color1) - Colors.getRed(color0));
		delta[2] = (Colors.getGreen(color1) - Colors.getGreen(color0));
		delta[3] = (Colors.getBlue(color1) - Colors.getBlue(color0));
		assert (AV.length == length);
		assert (RV.length == length);
		assert (GV.length == length);
		assert (BV.length == length);
		assert (table.length >= offset + length);
		final int a = Colors.getAlpha(color0);
		final int r = Colors.getRed(color0);
		final int g = Colors.getGreen(color0);
		final int b = Colors.getBlue(color0);
		for (int k = 0; k < length; k++) {
			value[0] = (int) Math.rint(a + delta[0] * AV[k]);
			value[1] = (int) Math.rint(r + delta[1] * RV[k]);
			value[2] = (int) Math.rint(g + delta[2] * GV[k]);
			value[3] = (int) Math.rint(b + delta[3] * BV[k]);
			value[0] = (value[0] < 0) ? 0 : ((value[0] > 255) ? 255 : value[0]);
			value[1] = (value[1] < 0) ? 0 : ((value[1] > 255) ? 255 : value[1]);
			value[2] = (value[2] < 0) ? 0 : ((value[2] > 255) ? 255 : value[2]);
			value[3] = (value[3] < 0) ? 0 : ((value[3] > 255) ? 255 : value[3]);
			table[offset + k] = (value[0] << 24) | (value[1] << 16) | (value[2] << 8) | value[3];
		}
		return table;
	}
}
