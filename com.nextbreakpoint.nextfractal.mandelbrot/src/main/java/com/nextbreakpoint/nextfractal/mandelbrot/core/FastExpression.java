/*
 * NextFractal 2.4.0
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
package com.nextbreakpoint.nextfractal.mandelbrot.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.math3.util.FastMath;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FastExpression {
	public static MutableNumber variable(ComplexNumber x) {
		return new MutableNumber(x);
	}
	
	public static MutableNumber variable(double x) {
		return new MutableNumber(x, 0);
	}
	
	public static MutableNumber variable(double r, double i) {
		return new MutableNumber(r, i);
	}

	public static ComplexNumber number(ComplexNumber x) {
		return new ComplexNumber(x);
	}

	public static ComplexNumber number(double r) {
		return new ComplexNumber(r, 0);
	}

	public static ComplexNumber number(double r, double i) {
		return new ComplexNumber(r, i);
	}

	public static double real(ComplexNumber x) {
		return x.r();
	}

	public static double real(double r) {
		return r;
	}

	public static ComplexNumber opAdd(MutableNumber out, ComplexNumber a, ComplexNumber b) {
		return out.set(a.r() + b.r(), a.i() + b.i());
	}

	public static ComplexNumber opSub(MutableNumber out, ComplexNumber a, ComplexNumber b) {
		return out.set(a.r() - b.r(), a.i() - b.i());
	}

	public static ComplexNumber opMul(MutableNumber out, ComplexNumber a, ComplexNumber b) {
		return out.set(a.r() * b.r() - a.i() * b.i(), a.r() * b.i() + a.i() * b.r());
	}

	public static ComplexNumber opDiv(MutableNumber out, ComplexNumber a, ComplexNumber b) {
		double m = b.r() * b.r() + b.i() * b.i();
		return out.set((a.r() * b.r() + a.i() * b.i()) / m, (-a.r() * b.i() + a.i() * b.r()) / m);
	}

	public static ComplexNumber opAdd(MutableNumber out, ComplexNumber a, double b) {
		return out.set(a.r() + b, a.i());
	}

	public static ComplexNumber opSub(MutableNumber out, ComplexNumber a, double b) {
		return out.set(a.r() - b, a.i());
	}

	public static ComplexNumber opMul(MutableNumber out, ComplexNumber a, double b) {
		return out.set(a.r() * b, a.i() * b);
	}

	public static ComplexNumber opDiv(MutableNumber out, ComplexNumber a, double b) {
		return out.set(a.r() / b, a.i() / b);
	}

	public static ComplexNumber opAdd(MutableNumber out, double a, ComplexNumber b) {
		return out.set(a + b.r(), +b.i());
	}

	public static ComplexNumber opSub(MutableNumber out, double a, ComplexNumber b) {
		return out.set(a - b.r(), -b.i());
	}

	public static ComplexNumber opMul(MutableNumber out, double a, ComplexNumber b) {
		return out.set(a * b.r(), a * b.i());
	}
	
	public static ComplexNumber opPow(MutableNumber out, ComplexNumber a, double b) {
		double m = Math.pow(FastMath.hypot(a.r(), a.i()), b);
		double f = Math.atan2(a.i(), a.r()) * b;
		return out.set(m * Math.cos(f), m * Math.sin(f));
	}
	
	public static ComplexNumber opNeg(MutableNumber out, ComplexNumber a) {
		return out.set(-a.r(), -a.i());
	}
	
	public static ComplexNumber opPos(MutableNumber out, ComplexNumber a) {
		return out.set(+a.r(), +a.i());
	}

	public static double opAdd(double a, double b) {
		return a + b;
	}

	public static double opSub(double a, double b) {
		return a - b;
	}

	public static double opMul(double a, double b) {
		return a * b;
	}

	public static double opDiv(double a, double b) {
		return a / b;
	}

	public static double opPow(double a, double b) {
		return Math.pow(a, b);
	}

	public static double funcMod(double x) {
		return Math.abs(x);
	}

	public static double funcMod2(double x) {
		return x * x;
	}

	public static double funcPha(double x) {
		return 0;
	}

	public static double funcAcos(double x) {
		return Math.acos(x);
	}

	public static double funcAsin(double x) {
		return Math.asin(x);
	}

	public static double funcAtan(double x) {
		return Math.atan(x);
	}

	public static double funcCos(double x) {
		return Math.cos(x);
	}

	public static double funcSin(double x) {
		return Math.sin(x);
	}

	public static double funcTan(double x) {
		return Math.tan(x);
	}

	public static double funcExp(double x) {
		return Math.exp(x);
	}

	public static double funcLog(double x) {
		return Math.log(x);
	}

	public static double funcSqrt(double x) {
		return Math.sqrt(x);
	}

	public static double funcAbs(double x) {
		return Math.abs(x);
	}

	public static double funcRe(double x) {
		return x;
	}

	public static double funcIm(double x) {
		return x;
	}

	public static double funcCeil(double x) {
		return Math.ceil(x);
	}

	public static double funcFloor(double x) {
		return Math.floor(x);
	}

	public static double funcPow(double x, double y) {
		return Math.pow(x, y);
	}

	public static double funcHypot(double x, double y) {
		return FastMath.hypot(x, y);
	}

	public static double funcAtan2(double x, double y) {
		return Math.atan2(y, x);
	}

	public static double funcMax(double x, double y) {
		return Math.max(y, x);
	}

	public static double funcMin(double x, double y) {
		return Math.min(y, x);
	}

	public static double funcMod(ComplexNumber x) {
		return FastMath.hypot(x.r(), x.i());
	}

	public static double funcMod2(ComplexNumber x) {
		return Math.pow(x.r(), 2) + Math.pow(x.i(), 2);
	}

	public static double funcPha(ComplexNumber x) {
		return Math.atan2(x.i(), x.r());
	}

	public static double funcRe(ComplexNumber n) {
		return n.r();
	}

	public static double funcIm(ComplexNumber n) {
		return n.i();
	}

	public static ComplexNumber funcSin(MutableNumber out, ComplexNumber x) {
		return out.set(Math.sin(x.r()) * Math.cosh(x.i()), +Math.cos(x.r()) * Math.sinh(x.i()));
	}

	public static ComplexNumber funcCos(MutableNumber out, ComplexNumber x) {
		return out.set(Math.cos(x.r()) * Math.cosh(x.i()), -Math.sin(x.r()) * Math.sinh(x.i()));
	}

	public static ComplexNumber funcTan(MutableNumber out, ComplexNumber x) {
		double d = Math.pow(Math.cos(x.r()), 2) + Math.pow(Math.sinh(x.i()), 2);
		return out.set((Math.sin(x.r()) * Math.cos(x.r())) / d, (Math.sinh(x.i()) * Math.cosh(x.i())) / d);
	}

	public static ComplexNumber funcExp(MutableNumber out, ComplexNumber x) {
		double d = Math.exp(x.r());
		return out.set(d * Math.cos(x.i()), d * Math.sin(x.i()));
	}

	public static ComplexNumber funcPow(MutableNumber out, ComplexNumber x, double e) {
		double d = Math.pow(FastMath.hypot(x.r(), x.i()), e);
		double f = Math.atan2(x.i(), x.r()) * e;
		return out.set(d * Math.cos(f), d * Math.sin(f));
	}

	public static ComplexNumber funcSqrt(MutableNumber out, ComplexNumber x) {
		double d = Math.sqrt(FastMath.hypot(x.r(), x.i()));
		double f = Math.atan2(x.i(), x.r()) * 0.5;
		return out.set(d * Math.cos(f), d * Math.sin(f));
	}

	public static double funcPulse(double x, double y) {
		double z = x - Math.floor(x);
		double w = y - Math.floor(y);
		return z < w ? 1 : 0;
	}

	public static double funcSquare(double x) {
		double y = x - Math.floor(x);
		return y < 0.5 ? 1 : 0;
	}

	public static double funcSaw(double x) {
		double y = x - Math.floor(x);
		return y < 0.5 ? 2 * y : 2 - 2 * y;
	}

	public static double funcRamp(double x) {
		double y = x - Math.floor(x);
		return y;
	}
}
