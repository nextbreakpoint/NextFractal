<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<mandelbrot>
    <timestamp>2015-04-29 20:05:10</timestamp>
    <julia>false</julia>
    <point>1.5066666666666668</point>
    <point>0.2</point>
    <rotation>0.0</rotation>
    <rotation>0.0</rotation>
    <rotation>0.0</rotation>
    <rotation>0.0</rotation>
    <scale>1.0</scale>
    <scale>1.0</scale>
    <scale>1.0</scale>
    <scale>1.0</scale>
    <source>fractal {
	orbit [-2.5 - 2.5i,+2.5 + 2.5i] [x,n] {
		begin {
			x = &lt;pi / 2,0&gt;;
		}
		loop [0, 200] (abs(im(x)) &gt; 100) {
			xr = re(x);
			xi = im(x);
			wr = re(w);
			wi = im(w);
			ta = cos(xr);
			tb = sin(xr);
			tc = exp(+xi);
			td = exp(-xi);
			te = 0.5 * tb * (tc + td);
			tf = 0.5 * ta * (tc - td);
			zr = wr * te - wi * tf;
			zi = wi * te + wr * tf;			
			x = &lt;zr,zi&gt;;
			if (mod2(x) &lt; 0.0000000001) {
				stop;
			}
		}
	}
	color [(1,0,0,0)] {
		palette gradient {
			[#FFFF0A0A &gt; #FFFFFFFF, 20];
			[#FFFFFFFF &gt; #FF0042A9, 60];
			[#FF0042A9 &gt; #FF000000, 120];
		}
		rule (n = 0) [1] {
			1,0,0,0
		}
		rule (n &gt; 0) [1] {
			gradient[n - 1]
		}
	}
}
</source>
    <time>0.0</time>
    <traslation>0.0</traslation>
    <traslation>0.0</traslation>
    <traslation>1.0</traslation>
    <traslation>0.0</traslation>
</mandelbrot>
