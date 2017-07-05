package de.bzus.flame.common;

/**
 * WSGMath provides some mathematical methods. It only defines static methods
 * and some global constants.
 * 
 * @author Werner Siegfried Genreith
 */
public class WSGMath {

	public static double SQRT2 = Math.sqrt(2);

	public static double SQRT3 = Math.sqrt(3);

	public static double pow(double b, int e) {
		double ret = 1.0;
		for (int i = 0; i < e; i++)
			ret *= b;
		return ret;
	}

	public static long pow(long b, int e) {
		long ret = 1;
		for (int i = 0; i < e; i++)
			ret *= b;
		return ret;
	}

	public static WSGComplex pow(WSGComplex b, int e) {
		WSGComplex ret = WSGComplex.ONE;
		for (int i = 0; i < e; i++)
			ret = ret.multiply(b);
		return ret;
	}

	public static WSGComplex round(WSGComplex c) {
		return new WSGComplex(Math.round(c.getReal()), Math.round(c.getImaginary()));
	}

	public static String[] sortStrings(String[] in) {
		return sortStrings(in, 0, in.length);
	}

	private static String[] sortStrings(String[] in, int from, int len) {
		if (from + len > in.length || from < 0 || len < 0) {
			from = 0;
			len = in.length;
		}
		if (len < 2 || in.length < 2)
			return in;

		String[] ret = new String[len];
		String[] retLt = new String[len];
		String[] retGe = new String[len];
		int j = 0;
		int k = 0;
		for (int i = 1; i < len; i++)
			if (in[i].compareToIgnoreCase(in[0]) < 0)
				retLt[j++] = in[i];
			else
				retGe[k++] = in[i];
		String[] lt = sortStrings(retLt, 0, j);
		String[] ge = sortStrings(retGe, 0, k);
		int index = 0;
		for (int i = 0; i < j; i++)
			ret[index++] = lt[i];
		ret[index++] = in[0];
		for (int i = 0; i < k; i++)
			ret[index++] = ge[i];

		return ret;
	}
}