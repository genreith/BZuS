package de.bzus.flame.process;

import de.bzus.flame.common.Globals;
import de.bzus.flame.common.Logging;
import de.bzus.flame.common.WSGComplex;
import de.bzus.flame.common.WSGMatrix;

/**
 * @author Werner Siegfried Genreith
 *
 *         EV model considers particles as normalized eigen/base vectors and
 *         hence has no need for an observable or perspective
 * 
 */
public class EVFlame extends Flame {

	/**
	 * 
	 * evData is a N*4 matrix one row for each dimension representing normalized
	 * base vectors e_j. The 4 columns represent the positive counts of e_j,
	 * i*e_j. -e_j, -i e_j
	 * 
	 */
	private long[][] evData = null;

	public EVFlame(long[][] evData) {
		this.evData = new long[evData.length][4];
		for (int i = 0; i < this.evData.length; i++)
			for (int j = 0; j < 4; j++)
				this.evData[i][j] = evData[i][j];
	}

	@Override
	public double absQ() {
		double absValue = 0;
		for (int i = 0; i < evData.length; i++) {
			long re = evData[i][0] - evData[i][2];
			long im = evData[i][1] - evData[i][3];
			absValue += re * re + im * im;
		}
		return absValue;
	}

	@Override
	public void addNullPairsIm(int pIndex, long delta) {
		evData[pIndex][1] += delta;
		evData[pIndex][3] += delta;
	}

	@Override
	public void addNullPairsRe(int pIndex, long delta) {
		evData[pIndex][0] += delta;
		evData[pIndex][2] += delta;
	}

	@Override
	public double excitement() {
		double sumB2 = this.absQ();
		int N = getStateDimension();
		double sumB4 = 0.0;
		for (int i = 0; i < N; i++) {
			double b2 = this.getStateAmplitude(i).absQ();
			sumB4 += b2 * b2;
		}

		double exc = (1 - sumB4 / (sumB2 * sumB2)) * N / (N - 1);

		if (exc < -Globals.minimalExcitement || exc > 1.0 + Globals.minimalExcitement)
			Logging.getErrorLog().showError("Anregung fehlerhaft: " + exc, 1);

		return exc > 0.0 ? (exc < 1.0 ? exc : 1.0) : 0.0;
	}

	@Override
	public double excitement(int index) {
		double sumB2 = this.absQ();

		if (sumB2 > 0) {
			double bi2 = this.getStateAmplitude(index).absQ();
			double sumB2OhneI = sumB2 - bi2;
			double exc = 4.0 * (bi2 * sumB2OhneI / sumB2 / sumB2);

			if (exc < -Globals.minimalExcitement || exc > 1.0 + Globals.minimalExcitement)
				Logging.getErrorLog().showError("Anregung(" + index + ") fehlerhaft: " + exc, 1);

			return exc > 0.0 ? (exc < 1.0 ? exc : 1.0) : 0.0;
		} else {
			return 0.0;
		}
	}

	@Override
	public EVFlame clone() {
		long[][] data = new long[this.evData.length][4];
		for (int i = 0; i < this.evData.length; i++)
			for (int j = 0; j < 4; j++)
				data[i][j] = this.evData[i][j];
		return new EVFlame(data);
	}

	@Override
	public EVFlame eliminateIm(int pIndex, long delta) {

		// hier verbrenne ich jetzt delta Nullpaare, so dass die gewünschten
		// Elemente übrig bleiben.
		// Wenn also delta > 0 ist, dann entferne ich delta Elemente beim
		// Negativen Wert, sonst umgekehrt

		if (delta > 0) {
			if (evData[pIndex][3] >= delta) {
				evData[pIndex][3] -= delta;
			} else {
				evData[pIndex][3] = 0;
			}
		} else {
			if (evData[pIndex][1] >= -delta) {
				evData[pIndex][1] += delta;
			} else {
				evData[pIndex][1] = 0;
			}
		}
		return this;

	}

	@Override
	public EVFlame eliminateRe(int pIndex, long delta) {
		long jAbs = delta > 0 ? delta : -delta;
		EVFlame ret = this;

		if (delta > 0) {
			if (ret.evData[pIndex][2] >= jAbs) {
				ret.evData[pIndex][2] -= jAbs;
			} else {
				ret.evData[pIndex][2] = 0;
			}
		} else {
			if (ret.evData[pIndex][0] >= jAbs) {
				ret.evData[pIndex][0] -= jAbs;
			} else {
				ret.evData[pIndex][0] = 0;
			}
		}
		return ret;

	}

	public boolean equals(EVFlame compare) {
		boolean ret = true;
		for (int i = 0; i < this.evData.length; i++) {
			for (int j = 0; j < 4; j++)
				ret &= (this.evData[i][j] == compare.evData[i][j]);
		}
		return ret;
	}

	@Override
	public String getCSVHeader(char separator) {
		return getCSVHeader(separator, "F_");
	}

	@Override
	public String getCSVHeader(char separator, String prefix) {

		StringBuffer out = new StringBuffer();
		out.append(super.getCSVHeader(separator, prefix));

		for (int irow = 0; irow < evData.length; irow++) {
			for (int icol = 0; icol < evData[0].length; icol++) {
				out.append(String.format("%c%sE%dI%d", separator, prefix, irow+1, icol));
			}
		}
		return out.toString();
	}

	public WSGComplex[] getEvDataComplex() {
		WSGComplex[] retC = new WSGComplex[evData.length];
		for (int i = 0; i < evData.length; i++) {
			retC[i] = getStateAmplitude(i);
		}
		return retC;
	}

	@Override
	public long getNullPairsIm(int index) {
		long ret = evData[index][1];
		if (ret > evData[index][3])
			ret = evData[index][3];
		return ret;
	}

	@Override
	public long getNullPairsRe(int index) {
		long ret = evData[index][0];
		if (ret > evData[index][2])
			ret = evData[index][2];
		return ret;
	}

	@Override
	public int getParticleDimension() {
		return getStateDimension();
	}

	@Override
	public WSGMatrix getState() {
		return new WSGMatrix(getEvDataComplex());
	}

	@Override
	public WSGComplex getStateAmplitude(int pIndex) {
		return new WSGComplex(evData[pIndex][0] - evData[pIndex][2], evData[pIndex][1] - evData[pIndex][3]);
	}

	@Override
	public int getStateDimension() {
		if (evData == null)
			return 0;
		else
			return evData.length;
	}

	public long getUnpairedIm(int index) {
		return sizeIm(index) - 2 * getNullPairsIm(index);
	}

	public long getUnpairedRe(int index) {
		return sizeRe(index) - 2 * getNullPairsRe(index);
	}

	@Override
	public long size() {
		long sz = 0;
		for (int i = 0; i < evData.length; i++)
			for (int j = 0; j < evData[i].length; j++)
				sz += evData[i][j];
		return sz;
	}

	private long sizeIm(int i) {
		return evData[i][1] + evData[i][3];
	}

	private long sizeRe(int i) {
		return evData[i][0] + evData[i][2];
	}

	public synchronized double testTargetIm(int pIndex, long delta) {

		double tgt = 0.0;
		if (delta > 0) {
			evData[pIndex][1] += delta;
			tgt = target(pIndex);
			evData[pIndex][1] -= delta;
		} else {
			evData[pIndex][3] -= delta;
			tgt = target(pIndex);
			evData[pIndex][3] += delta;
		}
		return tgt;
	}

	public synchronized double testTargetRe(int pIndex, long delta) {

		double tgt = 0.0;
		if (delta > 0) {
			evData[pIndex][0] += delta;
			tgt = target(pIndex);
			evData[pIndex][0] -= delta;
		} else {
			evData[pIndex][2] -= delta;
			tgt = target(pIndex);
			evData[pIndex][2] += delta;
		}
		return tgt;
	}

	public synchronized double testGlobalTargetIm(int pIndex, long delta) {

		double tgt = 0.0;
		if (delta > 0) {
			evData[pIndex][1] += delta;
			tgt = target();
			evData[pIndex][1] -= delta;
		} else {
			evData[pIndex][3] -= delta;
			tgt = target();
			evData[pIndex][3] += delta;
		}
		return tgt;
	}

	public synchronized double testGlobalTargetRe(int pIndex, long delta) {

		double tgt = 0.0;
		if (delta > 0) {
			evData[pIndex][0] += delta;
			tgt = target();
			evData[pIndex][0] -= delta;
		} else {
			evData[pIndex][2] -= delta;
			tgt = target();
			evData[pIndex][2] += delta;
		}
		return tgt;
	}

	@Override
	public String toCSV(char separator) {

		StringBuffer out = new StringBuffer();
		out.append(super.toCSV(separator));

		for (int irow = 0; irow < evData.length; irow++) {
			for (int icol = 0; icol < evData[0].length; icol++) {
				out.append(String.format("%c%d", separator, evData[irow][icol]));
			}
		}
		return out.toString();
	}

	@Override
	public String toString() {

		StringBuffer retS = new StringBuffer(super.toString());
		String[] cTitles = { "*(1)", "*(i)", "*(-1)", "(-i)" };

		if (evData == null)
			retS.append("\tInvalid FlameData\n");
		else {

			for (int iP = 0; iP < evData.length; iP++)
				retS.append(String.format("\tE_%d", iP + 1));
			retS.append("\n");

			for (int iP = 0; iP < evData[0].length; iP++) {
				retS.append(cTitles[iP] + ":");
				for (int iM = 0; iM < evData.length; iM++) {
					retS.append("\t" + evData[iM][iP]);
				}
				retS.append("\n");
			}
		}
		return retS.toString();

	}

	@Override
	public long getIdeaData(int pIndex, int iIndex) {
		if (evData != null && evData[0] != null && pIndex >= 0 && pIndex < evData.length && iIndex >= 0
				&& iIndex < evData[0].length)
			return evData[pIndex][iIndex];
		else
			return -1;
	}

}
