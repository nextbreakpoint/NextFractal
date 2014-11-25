package com.nextbreakpoint.nextfractal.flux.grammar;

import java.text.DecimalFormat;

public class ASTColorARGB {
	private static final DecimalFormat format = new DecimalFormat("0.##");
	private final float[] components = new float[] { 0f, 0f, 0f, 0f };

	public ASTColorARGB(String argb) {
		int x = Integer.parseInt(argb, 16);
		components[0] = (0xFF & (x >> 24)) / 255;
		components[1] = (0xFF & (x >> 16)) / 255;
		components[2] = (0xFF & (x >> 8)) / 255;
		components[3] = (0xFF & (x >> 0)) / 255;
	}

	public ASTColorARGB(String a, String r, String g, String b) {
		components[0] = Float.parseFloat(a);
		components[1] = Float.parseFloat(r);
		components[2] = Float.parseFloat(g);
		components[3] = Float.parseFloat(b);
	}

	public float[] getComponents() {
		return components;
	}

	public int getARGB() {
		return ((0xFF & ((int)(components[0] * 255))) << 24) | ((0xFF & ((int)(components[1] * 255))) << 16) | ((0xFF & ((int)(components[2] * 255))) << 8) | ((0xFF & ((int)(components[3] * 255))) << 0);
	}

	@Override
	public String toString() {
		return "(" + format.format(components[0]) + "," + format.format(components[1]) + "," + format.format(components[2]) + "," + format.format(components[3]) + ")";
	}
}
