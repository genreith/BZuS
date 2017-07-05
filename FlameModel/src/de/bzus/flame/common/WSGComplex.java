package de.bzus.flame.common;

import java.text.NumberFormat;
import java.text.ParseException;

import de.bzus.flame.exceptions.InvalidTermException;

/*
 * Copyright 2008, 2009 Siegfried Genreith
 */

/**
 * Complex is a central class in the DualReality model.
 * It handles all complex numbers operations and 
 * displays numbers in a readable way 
 * 
 * @author Werner Siegfried Genreith
 */
public class WSGComplex {
	public static final WSGComplex ImagUNIT = new WSGComplex(0.0, 1.0);
	public static final WSGComplex MinusImagUNIT = new WSGComplex(0.0, - 1.0);
	public static final WSGComplex MinusONE = new WSGComplex(-1.0);
	public static final WSGComplex ONE = new WSGComplex(1.0);
	public static final double PId2 = Math.PI / 2.0;
	public static final double PId4 = Math.PI / 4.0;
	public static final double SQRT2 = Math.sqrt(2);
	public static final double SQRT3 = Math.sqrt(3);
	public static final WSGComplex ZERO = new WSGComplex(0.0);

	public static String getCSVHeader(char separator) {
		return String.format("%1$c%2$sreal%1$c%2$simag", separator, "C_");
	}

	public static String getCSVHeader(char separator, String prefix) {
		return String.format("%1$c%2$sreal%1$c%2$simag", separator, prefix);
	}

	public static WSGComplex valueOf(String xterm) {
		NumberFormat nf = NumberFormat.getInstance();

		StringBuffer buf = new StringBuffer(xterm);
		for (int i = xterm.length() - 1; i >= 0; i--)
			switch (xterm.charAt(i)) {
			case ' ':
			case '(':
			case ')':
				buf.deleteCharAt(i);
				break;
			case '+':
			case '-':
			case 'i':
				break;
			default:
				if (xterm.charAt(i) < 0 || xterm.charAt(i) > '9')
					throw new NumberFormatException(xterm);
				else
					break;
			}

		String term = buf.toString();

		if (term == null || term.length() < 1)
			return new WSGComplex(0.0, 0.0);
		double re = Double.NaN;
		double im = Double.NaN;
		String termA = null;
		String termB = null;

		int pos = 0;
		for (pos = 1; pos < term.length(); pos++) {
			switch (term.charAt(pos)) {
			case '+':
			case '-':
				termA = term.substring(0, pos).trim();
				termB = term.substring(pos, term.length()).trim();
				break;
			default:
			}
		}
		if (termA == null) {
			termA = term.trim();
			if (termA.charAt(termA.length() - 1) == 'i')
				termB = "0.0";
			else
				termB = "0.0i";
		}

		if (termA.charAt(termA.length() - 1) == 'i') {
			try {
				im = nf.parse(termA.substring(0, termA.length() - 1)).doubleValue();
			} catch (ParseException e) {
				String tA = termA.substring(0, termA.length() - 1);
				if (tA.length() == 0)
					im = 1.0;
				else if (tA.startsWith("+"))
					im = +1.0;
				else if (tA.startsWith("-"))
					im = -1.0;
				else
					throw new NumberFormatException(xterm);
			}
			try {
				re = nf.parse(termB).doubleValue();
			} catch (ParseException e) {
				String tA = termA.substring(0, termA.length() - 1);
				if (tA.length() == 0)
					im = 1.0;
				else if (tA.startsWith("+"))
					im = +1.0;
				else if (tA.startsWith("-"))
					im = -1.0;
				else
					throw new NumberFormatException(xterm);
			}
		} else if (termB.charAt(termB.length() - 1) == 'i') {
			try {
				im = nf.parse(termB.substring(0, termB.length() - 1)).doubleValue();
			} catch (ParseException e) {
				String tB = termB.substring(0, termB.length() - 1);
				if (tB.length() == 0)
					im = 1.0;
				else if (tB.startsWith("+"))
					im = +1.0;
				else if (tB.startsWith("-"))
					im = -1.0;
				else
					throw new NumberFormatException(xterm);
			}
			try {
				re = nf.parse(termA).doubleValue();
			} catch (ParseException e) {
				String tA = termA.substring(0, termA.length() - 1);
				if (tA.length() == 0)
					im = 1.0;
				else if (tA.startsWith("+"))
					im = +1.0;
				else if (tA.startsWith("-"))
					im = -1.0;
				else
					throw new NumberFormatException(xterm);
			}
		}
		if (re == Double.NaN && im == Double.NaN)
			throw new NumberFormatException(xterm);

		return new WSGComplex(re, im);
	}

	public static WSGComplex valueOfTerm(String xterm) {
		Term t = new Term(xterm);
		try {
			return t.valueOf();
		} catch (InvalidTermException e) {
			return WSGComplex.ZERO;
		}
	}

	private double abs = 0.0;

	private double absQ = 0.0;

	private double imaginary = 0.0;

	private boolean isAbs2Calculated = false;

	private boolean isAbsCalculated = false;

	private boolean isPhiCalculated = false;

	private double phi = 0.0;

	private double real = 0.0;

	public WSGComplex() {
		real = 0.0;
		imaginary = 0.0;
	}

	public WSGComplex(double re) {
		real = re;
		imaginary = 0.0;
	}

	public WSGComplex(double re, double im) {
		real = re;
		imaginary = im;
	}

	public WSGComplex(String cString) {
		WSGComplex w = WSGComplex.valueOf(cString);
		real = w.getReal();
		imaginary = w.getImaginary();
	}

	public WSGComplex(WSGComplex dt) {
		real = dt.real;
		imaginary = dt.imaginary;
	}

	public double abs() {
		if (!isAbsCalculated) {
			abs = Math.sqrt(absQ());
			isAbsCalculated = true;
		}
		return abs;
	}

	public double absQ() {
		if (!isAbs2Calculated) {
			absQ = real * real + imaginary * imaginary;
			isAbs2Calculated = true;
		}
		return absQ;
	}

	public WSGComplex add(double fakt) {
		return new WSGComplex(fakt + this.real, this.imaginary);
	}

	public WSGComplex add(WSGComplex fakt) {
		return new WSGComplex(fakt.real + this.real, fakt.imaginary + this.imaginary);
	}

	public WSGComplex conjugate() {
		return new WSGComplex(real, -imaginary);
	}

	public WSGComplex divide(double divisor) throws InvalidTermException {

		if (divisor == 0)
			throw new InvalidTermException("Division by null");

		return new WSGComplex(real / divisor, imaginary / divisor);
	}

	public WSGComplex divide(WSGComplex divisor) throws InvalidTermException {

		if (divisor.absQ() == 0)
			throw new InvalidTermException("Division by null");

		return new WSGComplex((real * divisor.real + imaginary * divisor.imaginary) / divisor.absQ(),
				(-real * divisor.imaginary + imaginary * divisor.real) / divisor.absQ());
	}

	public boolean equals(WSGComplex comp) {
		return this.getReal() == comp.getReal() && this.getImaginary() == comp.getImaginary();
	}

	public double getImaginary() {
		return imaginary;
	}

	public double getReal() {
		return real;
	}

	public WSGComplex multiply(double fakt) {
		return new WSGComplex(real * fakt, imaginary * fakt);
	}

	public WSGComplex multiply(WSGComplex fakt) {
		return new WSGComplex(real * fakt.real - imaginary * fakt.imaginary,
				real * fakt.imaginary + imaginary * fakt.real);
	}

	public WSGComplex negate() {
		return new WSGComplex(-real, -imaginary);
	}

	public double phi() {

		if (!isPhiCalculated) {
			phi = Math.atan2(imaginary, real);
			// if (phi<0) phi+=2*Math.PI;
			isPhiCalculated = true;
		}
		return phi;
	}

	public WSGComplex pow(int n) {
		WSGComplex result = WSGComplex.ONE;
		for (int i = 0; i < n; i++)
			result = result.multiply(this);
		return result;
	}

	public WSGComplex reciprocal() throws InvalidTermException {
		if (absQ() == 0)
			throw new InvalidTermException("Division by null");

		return new WSGComplex(real / absQ(), -imaginary / absQ());
	}

	public WSGComplex sqrt() {
		double sqrtR = Math.sqrt(abs());

		double sqrtReal = 0.0;
		double sqrtImag = 0.0;
		if (phi() == 0) {
			sqrtReal = sqrtR;
			sqrtImag = 0;
		} else if (phi() == Math.PI) {
			sqrtReal = 0.0;
			sqrtImag = sqrtR;
		} else if (phi() == PId2) {
			sqrtReal = sqrtR / SQRT2;
			sqrtImag = sqrtReal;
		} else if (phi() == -PId2) {
			sqrtReal = sqrtR / SQRT2;
			sqrtImag = -sqrtReal;
		} else {
			sqrtReal = sqrtR * Math.cos(phi() / 2.0);
			sqrtImag = sqrtR * Math.sin(phi() / 2.0);
		}

		return new WSGComplex(sqrtReal, sqrtImag);
	}

	public WSGComplex subtract(double fakt) {
		return new WSGComplex(real - fakt, imaginary);
	}

	public WSGComplex subtract(WSGComplex fakt) {
		return new WSGComplex(real - fakt.real, imaginary - fakt.imaginary);
	}

	public String toCSV(char separator) {
		return String.format("%1$c%2$7.5f%1$c%3$7.5f", separator, getReal(), getImaginary());
	}

	@Override
	public String toString() {
		String liste = new String((getReal() != 0.0 ? String.format("%7.5f", getReal()) : "")
				+ (getImaginary() > 0.0 ? ((getReal() != 0.0 ? "+" : "")
						+ (getImaginary() == 1.0 ? "" : String.format("%7.5f", getImaginary())) + "i")
						: (getImaginary() < 0
								? "-" + (getImaginary() == -1.0 ? "" : String.format("%7.5f", -getImaginary())) + "i"
								: "")));
		if (liste.length() > 0)
			return "(" + liste + ")";
		else
			return "(" + String.format("%7.5f", 0.0) + ")";
	}

}
