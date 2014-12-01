package com.nextbreakpoint.nextfractal.flux;

public abstract class Fractal {
	private String sourceCode;

	public Fractal() {
	}

	public abstract Number compute(Number z, Number w);
	
	protected Number number(int n) {
		return new Number(n, 0);
	}
	
	protected Number number(double r, double i) {
		return new Number(r, i);
	}
	
	protected Number opAdd(Number a, Number b) {
		return new Number(a.r() + b.r(), a.i() + b.i());
	}
	
	protected Number opSub(Number a, Number b) {
		return new Number(a.r() - b.r(), a.i() - b.i());
	}
	
	protected Number opMul(Number a, Number b) {
		return new Number(a.r() * b.r() - a.i() * b.i(), a.r() * b.i() + a.r() * b.i());
	}
	
	protected Number opAdd(Number a, double b) {
		return new Number(a.r() + b, a.i());
	}
	
	protected Number opSub(Number a, double b) {
		return new Number(a.r() - b, a.i());
	}
	
	protected Number opMul(Number a, double b) {
		return new Number(a.r() * b, a.i() * b);
	}

	protected Number opDiv(Number a, double b) {
		return new Number(a.r() / b, a.i() / b);
	}
	
	protected Number opAdd(double a, Number b) {
		return new Number(a + b.r(), +b.i());
	}
	
	protected Number opSub(double a, Number b) {
		return new Number(a - b.r(), -b.i());
	}
	
	protected Number opMul(double a, Number b) {
		return new Number(a * b.r(), a * b.i());
	}

	protected Number opAdd(double a, double b) {
		return new Number(a + b, 0);
	}
	
	protected Number opSub(double a, double b) {
		return new Number(a - b, 0);
	}
	
	protected Number opMul(double a, double b) {
		return new Number(a * b, 0);
	}

	protected Number opNeg(Number a) {
		return new Number(-a.r(), -a.i());
	}
	
	protected Number opPos(Number a) {
		return new Number(+a.r(), +a.i());
	}

	protected Number opNeg(double a) {
		return new Number(-a, 0);
	}
	
	protected Number opPos(double a) {
		return new Number(+a, 0);
	}
	
	protected double opAddReal(double a, double b) {
		return a + b;
	}
	
	protected double opSubReal(double a, double b) {
		return a - b;
	}
	
	protected double opMulReal(double a, double b) {
		return a * b;
	}
	
	protected double opDivReal(double a, double b) {
		return a / b;
	}
	
	protected double opPowReal(double a, double b) {
		return Math.pow(a, b);
	}
	
	protected double opNegReal(double a) {
		return -a;
	}
	
	protected double opPosReal(double a) {
		return +a;
	}

	protected double opAddReal(Number a, double b) {
		if (a.isReal()) {
			return a.r() + b;
		} else {
			throw new RuntimeException("Number is not real");
		}
	}
	
	protected double opSubReal(Number a, double b) {
		if (a.isReal()) {
			return a.r() - b;
		} else {
			throw new RuntimeException("Number is not real");
		}
	}
	
	protected double opMulReal(Number a, double b) {
		if (a.isReal()) {
			return a.r() * b;
		} else {
			throw new RuntimeException("Number is not real");
		}
	}
	
	protected double opDivReal(Number a, double b) {
		if (a.isReal()) {
			return a.r() / b;
		} else {
			throw new RuntimeException("Number is not real");
		}
	}
	
	protected double opPowReal(Number a, double b) {
		if (a.isReal()) {
			return Math.pow(a.r(), b);
		} else {
			throw new RuntimeException("Number is not real");
		}
	}
	
	protected double opAddReal(double a, Number b) {
		if (b.isReal()) {
			return a + b.r();
		} else {
			throw new RuntimeException("Number is not real");
		}
	}
	
	protected double opSubReal(double a, Number b) {
		if (b.isReal()) {
			return a - b.r();
		} else {
			throw new RuntimeException("Number is not real");
		}
	}
	
	protected double opMulReal(double a, Number b) {
		if (b.isReal()) {
			return a * b.r();
		} else {
			throw new RuntimeException("Number is not real");
		}
	}
	
	protected double opDivReal(double a, Number b) {
		if (b.isReal()) {
			return a / b.r();
		} else {
			throw new RuntimeException("Number is not real");
		}
	}
	
	protected double opPowReal(double a, Number b) {
		if (b.isReal()) {
			return Math.pow(a, b.r());
		} else {
			throw new RuntimeException("Number is not real");
		}
	}
	
	protected double opAddReal(Number a, Number b) {
		if (a.isReal() && b.isReal()) {
			return a.r() + b.r();
		} else {
			throw new RuntimeException("Number is not real");
		}
	}
	
	protected double opSubReal(Number a, Number b) {
		if (a.isReal() && b.isReal()) {
			return a.r() - b.r();
		} else {
			throw new RuntimeException("Number is not real");
		}
	}
	
	protected double opMulReal(Number a, Number b) {
		if (a.isReal() && b.isReal()) {
			return a.r() * b.r();
		} else {
			throw new RuntimeException("Number is not real");
		}
	}
	
	protected double opDivReal(Number a, Number b) {
		if (a.isReal() && b.isReal()) {
			return a.r() / b.r();
		} else {
			throw new RuntimeException("Number is not real");
		}
	}
	
	protected double opPowReal(Number a, Number b) {
		if (a.isReal() && b.isReal()) {
			return Math.pow(a.r(), b.r());
		} else {
			throw new RuntimeException("Number is not real");
		}
	}

	protected double opNegReal(Number a) {
		if (a.isReal()) {
			return -a.r();
		} else {
			throw new RuntimeException("Number is not real");
		}
	}
	
	protected double opPosReal(Number a) {
		if (a.isReal()) {
			return +a.r();
		} else {
			throw new RuntimeException("Number is not real");
		}
	}
	
	protected double funcMod(Number x) {
		if (x.isReal()) {
			return Math.abs(x.r());
		} else {
			return Math.hypot(x.r(), x.i());
		}
	}

	protected double funcMod(double x) {
		return Math.abs(x);
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}
}
