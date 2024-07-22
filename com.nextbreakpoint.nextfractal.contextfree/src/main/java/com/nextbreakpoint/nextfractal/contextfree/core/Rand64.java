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
package com.nextbreakpoint.nextfractal.contextfree.core;

import java.util.Random;
import java.util.function.UnaryOperator;

import static org.apache.commons.math3.special.Gamma.logGamma;

public class Rand64 implements Cloneable {
	private static long RAND64_SEED  = 0x3DF41234;
	private static long RAND64_MULT = 2685821657736338717L;

    private static Random random = new Random();

    private long seed;

	public Rand64() {
		this(RAND64_SEED);
	}

	public Rand64(long seed) {
		this.seed = seed;
	}

	public static void initRandomSeed(long seed) {
		random.setSeed(seed);
	}

	public void xorString(String t, int[] i) {
	    for (int j = 0; j < t.length(); j++) {
	        xorChar(t.charAt(j), j);
	        discard(1);
	        i[0] = (i[0] + 1) & 7;
	    }
	}
    
    public void xorChar(char c, long i) {
        seed ^= ((long)c) << (i * 8);
	}

	private void discard(long z) {
		for (long i = 0; i < z; ++i) {
			// This is the xorshift64* PRNG.
			seed ^= seed >> 12;
			seed ^= seed << 25;
			seed ^= seed >> 27;
			seed *= RAND64_MULT;
		}
	}

	public Rand64 add(Rand64 rand) {
		seed ^= rand.seed;
		return this;
	}

	public double getDouble() {
		return random.nextDouble();
	}

	public double getDouble(long l, long u) {
		return l + (u - l) * random.nextDouble();
	}

	public long getDiscrete(int count, double[] weights) {
		if (count == 0) {
			return 0;
		}

		double[] w = new double[count];
		for (int i = 0; i < count; ++i) {
			w[i] = Math.abs(weights[i]);
		}

		int[] i = new int[] { 0 };

		return new DiscreteRand(count,0,1, x -> w[i[0]++]).generate();
	}

	public long getPoisson(double mean) {
		return new PoissonRand(pos(mean)).generate();
	}

	public long getGeometric(double p) {
		return new GeometricRand(prob(p)).generate();
	}

	public long getBinomial(long trials, double p) {
		return new BinomialRand(nat(trials), prob(p)).generate();
	}

	public long getNegativeBinomial(long trials, double p) {
		return new NegativeBinomialRand(nat(trials), prob(p)).generate();
	}

	public boolean getBernoulli(double p) {
		return new BernoulliRand(prob(p)).generate();
	}

	public double getExponential(double lambda) {
		return new ExponentialRand(pos(lambda)).generate();
	}

	public double getGamma(double alpha, double beta) {
		return new GammaRand(pos(alpha), pos(beta)).generate();
	}

	public double getWeibull(double a, double b) {
		return new WeibullRand(pos(a), pos(b)).generate();
	}

	public double getExtremeValue(double location, double scale) {
		return new ExtremeValueRand(location, pos(scale)).generate();
	}

	public double getNormal(double mean, double stddev) {
		return new NormalRand(mean, stddev).generate();
	}

	public double getLogNormal(double scale, double shape) {
		return new LogNormalRand(scale, shape).generate();
	}

	public double getChiSquared(double freedom) {
		return new ChiSquaredRand(degree(freedom)).generate();
	}

	public double getCauchy(double location, double scale) {
		return new CauchyRand(location, pos(scale)).generate();
	}

	public double getFisherF(double mfree, double nfree) {
		return new FisherRand(degree(mfree), degree(nfree)).generate();
	}

	public double getStudentT(double freedom) {
		return new StudentTRand(degree(freedom)).generate();
	}

	public abstract class Rand<T> {
		public abstract T min();
		public abstract T max();
		public abstract T generate();
	}

	public class StudentTRand extends Rand<Double> {
		private final double n;
		private final NormalRand normal;

		public StudentTRand() {
			this(1);
		}

		public StudentTRand(double n) {
			this.n = n;
			normal = new NormalRand();
		}

		@Override
		public Double min() {
			return Double.NEGATIVE_INFINITY;
		}

		@Override
		public Double max() {
			return Double.POSITIVE_INFINITY;
		}

		@Override
		public Double generate() {
			return normal.generate() * Math.sqrt(n / getGamma(n * 0.5, 2));
		}
	}

	public class NormalRand extends Rand<Double> {
		private final double mean;
		private final double stddev;
		private double V = 0;
		private boolean Vhot = false;

		public NormalRand() {
			this(0, 1);
		}

		public NormalRand(double mean, double stddev) {
			this.mean = mean;
			this.stddev = stddev;
		}

		@Override
		public Double min() {
			return Double.NEGATIVE_INFINITY;
		}

		@Override
		public Double max() {
			return Double.POSITIVE_INFINITY;
		}

		@Override
		public Double generate() {
			double up = 0;
			if (Vhot) {
				Vhot = false;
				up = V;
			} else {
				double u;
				double v;
				double s;
				do {
					u = getDouble(-1, 1);
					v = getDouble(-1, 1);
					s = u * u + v * v;
				} while (s > 1 || s == 0);
				double fp = Math.sqrt(-2 * Math.log(s) / s);
				V = v * fp;
				Vhot = true;
				up = u * fp;
			}
			return up * stddev + mean;
		}
	}

	public class GammaRand extends Rand<Double> {
		private final double alpha;
		private final double beta;

		public GammaRand() {
			this(1, 1);
		}

		public GammaRand(double alpha, double beta) {
			this.alpha = alpha;
			this.beta = beta;
		}

		@Override
		public Double min() {
			return 0.0;
		}

		@Override
		public Double max() {
			return Double.POSITIVE_INFINITY;
		}

		@Override
		public Double generate() {
			double a = alpha;
			double x;
			if (a == 1) {
				x = getExponential(1);
			} else if (a > 1) {
				double b = a - 1;
				double c = 3 * a - 0.75;
				while (true) {
					double u = getDouble();
					double v = getDouble();
					double w = u * (1 - u);
					if (w != 0) {
						double y = Math.sqrt(c / w) * (u - 0.5);
						x = b + y;
						if (x >= 0) {
							double z = 64 * w * w * w * v * v;
							if (z <= 1 - 2 * y * y / x) {
								break;
							}
							if (Math.log(z) <= 2 * (b * Math.log(x / b) - y)) {
								break;
							}
						}
					}
				}
			}
			else {
				while (true) {
					double u = getDouble();
					double es = getDouble();
					if (u <= 1 - a) {
						x = Math.pow(u, 1 / a);
						if (x <= es) {
							break;
						}
					} else {
						double e = -Math.log((1 - u) / a);
						x = Math.pow(1 - a + a * e, 1 / a);
						if (x <= e + es) {
							break;
						}
					}
				}
			}
			return x * beta;
		}
	}

	public class FisherRand extends Rand<Double> {
		private final double m;
		private final double n;

		public FisherRand() {
			this(1, 1);
		}

		public FisherRand(double m, double n) {
			this.m = m;
			this.n = n;
		}

		@Override
		public Double min() {
			return 0.0;
		}

		@Override
		public Double max() {
			return Double.POSITIVE_INFINITY;
		}

		@Override
		public Double generate() {
			return n * getGamma(m * 0.5, 1) / m * getGamma(n * 0.5, 1);
		}
	}

	public class CauchyRand extends Rand<Double> {
		private final double a;
		private final double b;

		public CauchyRand() {
			this(0, 1);
		}

		public CauchyRand(double a, double b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public Double min() {
			return Double.NEGATIVE_INFINITY;
		}

		@Override
		public Double max() {
			return Double.POSITIVE_INFINITY;
		}

		@Override
		public Double generate() {
			// purposefully let tan arg get as close to pi/2 as it wants, tan will return p finite
			return a + b * Math.tan(3.1415926535897932384626433832795 * getDouble());
		}
	}

	public class ChiSquaredRand extends Rand<Double> {
		private final double n;

		public ChiSquaredRand() {
			this(1);
		}

		public ChiSquaredRand(double n) {
			this.n = n;
		}

		@Override
		public Double min() {
			return 0.0;
		}

		@Override
		public Double max() {
			return Double.POSITIVE_INFINITY;
		}

		@Override
		public Double generate() {
			return getGamma(n / 2, 2);
		}
	}

	public class LogNormalRand extends Rand<Double> {
		private final double m;
		private final double s;
		private final NormalRand normal;

		public LogNormalRand() {
			this(0, 1);
		}

		public LogNormalRand(double m, double s) {
			this.m = m;
			this.s = s;
			normal = new NormalRand(m, s);
		}

		@Override
		public Double min() {
			return 0.0;
		}

		@Override
		public Double max() {
			return Double.POSITIVE_INFINITY;
		}

		@Override
		public Double generate() {
			return Math.exp(normal.generate());
		}
	}

	public class ExtremeValueRand extends Rand<Double> {
		private final double a;
		private final double b;

		public ExtremeValueRand() {
			this(0, 1);
		}

		public ExtremeValueRand(double a, double b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public Double min() {
			return Double.NEGATIVE_INFINITY;
		}

		@Override
		public Double max() {
			return Double.POSITIVE_INFINITY;
		}

		@Override
		public Double generate() {
			return a - b * Math.log(-Math.log(1 - getDouble()));
		}
	}

	public class WeibullRand extends Rand<Double> {
		private final double a;
		private final double b;

		public WeibullRand() {
			this(1, 1);
		}

		public WeibullRand(double a, double b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public Double min() {
			return 0.0;
		}

		@Override
		public Double max() {
			return Double.POSITIVE_INFINITY;
		}

		@Override
		public Double generate() {
			return b * Math.pow(getExponential(1), 1 / a);
		}
	}

	public class ExponentialRand extends Rand<Double> {
		private final double lambda;

		public ExponentialRand() {
			this(1);
		}

		public ExponentialRand(double lambda) {
			this.lambda = lambda;
		}

		@Override
		public Double min() {
			return 0.0;
		}

		@Override
		public Double max() {
			return Double.POSITIVE_INFINITY;
		}

		@Override
		public Double generate() {
			return -Math.log(1 - getDouble()) / lambda;
		}
	}

	public class BernoulliRand extends Rand<Boolean> {
		private final double p;

		public BernoulliRand() {
			this(0.5);
		}

		public BernoulliRand(double p) {
			this.p = p;
		}

		@Override
		public Boolean min() {
			return Boolean.FALSE;
		}

		@Override
		public Boolean max() {
			return Boolean.TRUE;
		}

		@Override
		public Boolean generate() {
			return getDouble() < p;
		}
	}

	public class NegativeBinomialRand extends Rand<Long> {
		private final long k;
		private final double p;

		public NegativeBinomialRand() {
			this(1, 0.5);
		}

		public NegativeBinomialRand(long k, double p) {
			this.k = k;
			this.p = p;
		}

		@Override
		public Long min() {
			return 0L;
		}

		@Override
		public Long max() {
			return Long.MAX_VALUE;
		}

		@Override
		public Long generate() {
			if (k <= 21 * p) {
				long f = 0;
				long s = 0;
				while (s < k) {
					if (getBernoulli(p))
						++s;
					else
						++f;
				}
				return f;
			}
			return getPoisson(getGamma(k, (1 - p) / p));
		}
	}

	public class BinomialRand extends Rand<Long> {
		private final long t;
		private final double p;

		public BinomialRand() {
			this(1, 0.5);
		}

		public BinomialRand(long t, double p) {
			this.t = t;
			this.p = p;
		}

		@Override
		public Long min() {
			return 0L;
		}

		@Override
		public Long max() {
			return t;
		}

		@Override
		public Long generate() {
			long r0 = 0;
			double pr = 0;
			double odds_ratio = 0;
			if (p > 0 && p < 1) {
				r0 = (long)((t + 1) * p);
				pr = Math.exp(logGamma(t + 1.) - logGamma(r0 + 1.) - logGamma(t - r0 + 1.) + r0 * Math.log(p) + (t - r0) * Math.log(1 - p));
				odds_ratio = p / (1 - p);
			}
			if (t == 0 || p == 0) {
				return 0L;
			}
			if (p == 1) {
				return t;
			}
			double u = getDouble() - pr;
			if (u < 0) {
				return r0;
			}
			double pu = pr;
			double pd = pu;
			long ru = r0;
			long rd = ru;
			while (true) {
				if (rd >= 1) {
					pd *= rd / (odds_ratio * (t - rd + 1));
					u -= pd;
					if (u < 0) {
						return rd - 1;
					}
				}
				--rd;
				++ru;
				if (ru <= t) {
					pu *= (t - ru + 1) * odds_ratio / ru;
					u -= pu;
					if (u < 0) {
						return ru;
					}
				}
			}
		}
	}

	public class GeometricRand extends Rand<Long> {
		private final double p;

		public GeometricRand() {
			this(0.5);
		}

		public GeometricRand(double p) {
			this.p = p;
		}

		@Override
		public Long min() {
			return 0L;
		}

		@Override
		public Long max() {
			return Long.MAX_VALUE;
		}

		@Override
		public Long generate() {
			return getNegativeBinomial(1, p);
		}
	}

	public class PoissonRand extends Rand<Long> {
		private final double mean;
		private final double s;
		private final double d;
		private final double l;
		private final double omega;
		private final double c3;
		private final double c2;
		private final double c1;
		private final double c0;
		private final double c;

		public PoissonRand() {
			this(0.5);
		}

		public PoissonRand(double mean) {
			this.mean = mean;
			if (mean < 10) {
				s = 0;
				d = 0;
				l = Math.exp(-mean);
				omega = 0;
				c3 = 0;
				c2 = 0;
				c1 = 0;
				c0 = 0;
				c = 0;
			} else {
				s = Math.sqrt(mean);
				d = 6 * mean * mean;
				l = mean - 1.1484;
				omega = .3989423 / s;
				double b1 = .4166667E-1 / mean;
				double b2 = .3 * b1 * b1;
				c3 = .1428571 * b1 * b2;
				c2 = b2 - 15. * c3;
				c1 = b1 - 6. * b2 + 45. * c3;
				c0 = 1. - b1 + 3. * b2 - 15. * c3;
				c = .1069 / mean;
			}
		}

		@Override
		public Long min() {
			return 0L;
		}

		@Override
		public Long max() {
			return Long.MAX_VALUE;
		}

		@Override
		public Long generate() {
			long x = 0;
			if (mean < 10) {
				x = 0;
				for (double p = getDouble(); p > l; ++x) {
					p *= getDouble();
				}
			} else {
				double difmuk = 0;
				double g = mean + s * getNormal(0, 1);
				double u = 0;
				if (g > 0) {
					x = (long)g;
					if (x >= l) {
						return x;
					}
					difmuk = mean - x;
					u = getDouble();
					if (d * u >= difmuk * difmuk * difmuk) {
						return x;
					}
				}
				for (boolean usingExpDist = false; true; usingExpDist = true) {
					double e = 0;
					if (usingExpDist || g < 0) {
						double t;
						do {
							e = getExponential(1);
							u = getDouble();
							u += u - 1;
							t = 1.8 + (u < 0 ? -e : e);
						} while (t <= -.6744);
						x = (long)(mean + s * t);
						difmuk = mean - x;
						usingExpDist = true;
					}
					double px = 0;
					double py = 0;
					if (x < 10) {
						long fac[] = {1, 1, 2, 6, 24, 120, 720, 5040, 40320, 362880};
						px = -mean;
						py = Math.pow(mean, (double)x) / fac[(int)x];
					} else {
						double del = .8333333E-1 / x;
						del -= 4.8 * del * del * del;
						double v = difmuk / x;
						if (Math.abs(v) > 0.25) {
							px = x * Math.log(1 + v) - difmuk - del;
						} else {
							px = x * v * v * (((((((.1250060 * v + -.1384794) * v + .1421878) * v + -.1661269) * v + .2000118) * v + -.2500068) * v + .3333333) * v + -.5) - del;
							py = .3989423 / Math.sqrt(x);
						}
					}
					double r = (0.5 - difmuk) / s;
					double r2 = r * r;
					double fx = -0.5 * r2;
					double fy = omega * (((c3 * r2 + c2) * r2 + c1) * r2 + c0);
					if (usingExpDist) {
						if (c * Math.abs(u) <= py * Math.exp(px + e) - fy * Math.exp(fx + e)) {
							break;
						}
					} else {
						if (fy - u * fy <= py * Math.exp(px - fx)) {
							break;
						}
					}
				}
			}
			return x;
		}
	}

	public class DiscreteRand extends Rand<Long> {
		private double[] p;

		public DiscreteRand(double[] w) {
			this.p = w;
			init();
		}

		public DiscreteRand(int count, double xmin, double xmax, UnaryOperator<Double> fw) {
			p = new double[count];
			if (count > 1) {
				double d = (xmax - xmin) / count;
				double d2 = d / 2;
				for (int k = 0; k < count; ++k) {
					p[k] = fw.apply(xmin + k * d + d2).doubleValue();
				}
			}
			init();
		}

		@Override
		public Long min() {
			return 0L;
		}

		@Override
		public Long max() {
			return (long)p.length;
		}

		@Override
		public Long generate() {
			return (long)(p[upper_bound(p,0, p.length,getDouble() - p[0])]);
		}

		private void init() {
			if (p.length > 0) {
				if (p.length > 1) {
					double s = accumulate(p,0, p.length,0.0);
					for (int i = 0; i < p.length; ++i) {
						p[i] /= s;
					};
					double[] t = new double[p.length - 1];
					partial_sum(p, 0, p.length - 1, t);
					swap(p, t);
				} else {
					p = new double[0];
				}
			}
		}

		private double[] probabilities() {
			int n = p.length;
			double[] p = new double[n+1];
			adjacent_difference(p, 0, p.length, p);
			if (n > 0) {
				p[n] = 1 - p[n - 1];
			} else {
				p[0] = 1;
			}
			return p;
		}

		private int upper_bound(double[] in, int first, int last, double value) {
			for (int i = first; i < last; i++) {
				if (in[i] > value) {
					return i;
				}
			}
			return 0;
		}

		private double[] adjacent_difference(double[] in, int first, int last, double[] out) {
			if (out.length < last - first) {
				return out;
			}
			double prev = 0;
			for (int i = first, j = 0; i < last; i++, j++) {
				out[j] = in[i] - prev;
				prev = in[i];
			}
			return out;
		}

		private void swap(double[] a, double[] b) {
			if (a.length != b.length) {
				return;
			}
			for (int i = 0; i < a.length; i++) {
				double t = a[i];
				a[i] = b[i];
				b[i] = t;
			}
		}

		private double[] partial_sum(double[] in, int first, int last, double[] out) {
			if (out.length < last - first) {
				return out;
			}
			double acc = 0;
			for (int i = first, j = 0; i < last; i++, j++) {
				acc += in[i];
				out[j] = acc;
			}
			return out;
		}

		private double accumulate(double[] in, int first, int last, double acc) {
			for (int i = first; i < last; i++) {
				acc += in[i];
			}
			return acc;
		}
	}

	private double prob(double p) {
		return p < 0.0 ? 0.0 : p > 1.0 ? 1.0 : p;
	}

	private double pos(double p) {
		return p > 0.0 ? p : 2^-52;
	}

	private double degree(double n) {
		return n >= 1.0 ? Math.floor(n) : 1.0;
	}

	private long nat(long i) {
		return i < 1 ? 1 : i;
	}

	public Object clone() {
		return new Rand64(seed);
	}
}
