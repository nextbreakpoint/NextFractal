package com.nextbreakpoint.nextfractal.flux;

public class Number {
	private double r;
	private double i;

	public Number() {
		this(0, 0);
	}

	public Number(int r) {
		this(r, 0);
	}

	public Number(double r) {
		this(r, 0);
	}

	public Number(double r, double i) {
		this.r = r;
		this.i = i;
	}

	public double r() {
		return r;
	}

	public double i() {
		return i;
	}

	public int n() {
		return (int)r;
	}

	public boolean isReal() {
		return i == 0;
	}

	public boolean isInteger() {
		return i == 0 && r == (int)r;
	}

	public void set(Number x) {
		this.r = x.r;
		this.i = x.i;
	}
}