package com.nextbreakpoint.nextfractal.mandelbrot;

import java.util.Arrays;
import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name="mandelbrot")
public class MandelbrotData {
	private final static String version = "1.0";
	private double[] constant = new double[] { 0, 0 };
	private double[] traslation = new double[] { 0, 0, 1, 0 };
	private double[] rotation = new double[] { 0, 0, 0, 0 };
	private double[] scale = new double[] { 1, 1, 1, 1 };
	@XmlElement(name = "timestamp", required = true) 
	@XmlJavaTypeAdapter(DateAdapter.class)
	private Date timestamp = new Date();
	private String source = "";
	private boolean julia;
	private double time;

	public boolean isJulia() {
		return julia;
	}

	public void setJulia(boolean julia) {
		this.julia = julia;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public String getVersion() {
		return version;
	}

	public double[] getConstant() {
		return constant;
	}

	public void setConstant(double[] constant) {
		this.constant = constant;
	}

	public double[] getTraslation() {
		return traslation;
	}

	public void setTraslation(double[] traslation) {
		this.traslation = traslation;
	}

	public double[] getRotation() {
		return rotation;
	}

	public void setRotation(double[] rotation) {
		this.rotation = rotation;
	}

	public double[] getScale() {
		return scale;
	}

	public void setScale(double[] scale) {
		this.scale = scale;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}

	@Override
	public String toString() {
		return "[traslation=" + Arrays.toString(traslation)	+ ", rotation=" + Arrays.toString(rotation) + ", scale=" + Arrays.toString(scale) + ", julia=" + julia + ", constant=" + Arrays.toString(constant) + ", time=" + time + "]";
	}
}
