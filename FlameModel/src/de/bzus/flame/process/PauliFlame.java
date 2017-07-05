package de.bzus.flame.process;

import de.bzus.flame.common.Globals;
import de.bzus.flame.common.Logging;
import de.bzus.flame.common.WSGComplex;
import de.bzus.flame.common.WSGMatrix;
import de.bzus.flame.exceptions.InvalidTermException;

/**
 * @author Werner Siegfried Genreith
 *
 *         Pauli model considers particles as pauli matrices P and involves a
 *         observable A and a perspective v to derive a state by A P v
 * 
 */
public class PauliFlame extends Flame {
	public static final int flagEvMatrix = 0x0001;
	public static final int flagMetrTensor = 0x0002;
	public static final int flagState = 0x0004;
	public static final int flagTransMatrix = 0x0008;

	private long[][] flameData = new long[4][4];

	private boolean isCalculatedMetrTensor = false;
	private boolean isCalculatedState = false;
	private boolean isCalculatedTransMatrix = false;

	private WSGMatrix metrTensor = null;

	private WSGMatrix observable = null;
	private WSGMatrix perspektive = null;

	private WSGMatrix stateVector = null;

	private WSGMatrix transMatrix = null;

	public PauliFlame(long[][] evData) {
		this.flameData = new long[evData.length][4];
		for (int i = 0; i < flameData.length; i++)
			for (int j = 0; j < 4; j++)
				flameData[i][j] = evData[i][j];

		WSGComplex[][] field = { { new WSGComplex(1) }, { new WSGComplex(1) } };
		this.perspektive = new WSGMatrix(field);
	}

	public PauliFlame(long[][] evData, WSGMatrix complexField) {
		this.flameData = new long[evData.length][4];
		for (int i = 0; i < flameData.length; i++)
			for (int j = 0; j < 4; j++)
				flameData[i][j] = evData[i][j];

		if (complexField != null && complexField.getColumnDimension() == 1 && complexField.getRowDimension() == 2) {
			this.perspektive = complexField;
			this.observable = null;
		} else if (complexField != null && complexField.getColumnDimension() == 2
				&& complexField.getRowDimension() == 2) {
			this.observable = complexField;
			WSGComplex[][] field = { { new WSGComplex(1) }, { new WSGComplex(1) } };
			this.perspektive = new WSGMatrix(field);
		}
	}

	public PauliFlame(long[][] evData, WSGMatrix perspektive, WSGMatrix observable) {
		this.flameData = new long[evData.length][4];
		for (int i = 0; i < flameData.length; i++)
			for (int j = 0; j < 4; j++)
				flameData[i][j] = evData[i][j];

		this.perspektive = perspektive;
		this.observable = observable;
	}

	@Override
	public double absQ() {
		WSGMatrix sv = this.getState();
		return sv.getEntry(0, 0).absQ() + sv.getEntry(1, 0).absQ();
	}

	@Override
	public void addNullPairsIm(int pIndex, long delta) {
		flameData[pIndex][1] += delta;
		flameData[pIndex][3] += delta;
	}

	@Override
	public void addNullPairsRe(int pIndex, long delta) {
		flameData[pIndex][0] += delta;
		flameData[pIndex][2] += delta;
	}

	@Override
	public double excitement() {
		return excitement(0);
	}

	@Override
	public double excitement(int index) {
		WSGMatrix sv;
		sv = this.getState();
		double exc = 0.0;
		double sumB2 = this.absQ();
		if (sumB2 > 0) {
			double x2 = sv.getEntry(0, 0).absQ();
			double y2 = sv.getEntry(1, 0).absQ();
			exc = 4 * x2 * y2 / sumB2 / sumB2;

			if (exc < -Globals.minimalExcitement || exc > 1.0 + Globals.minimalExcitement)
				Logging.getErrorLog().showError("Anregung(" + index + ") fehlerhaft: " + exc, 1);

			return exc > 0.0 ? (exc < 1.0 ? exc : 1.0) : 0.0;
		} else {
			return 0.0;
		}
	}

	@Override
	public PauliFlame clone() {
		return new PauliFlame(this.flameData, perspektive, observable);
	}

	@Override
	public PauliFlame eliminateIm(int pIndex, long delta) {

		if (delta > 0) {
			if (flameData[pIndex][3] >= delta)
				flameData[pIndex][3] -= delta;
			else
				flameData[pIndex][3] = 0;
		} else {
			if (flameData[pIndex][1] >= -delta)
				flameData[pIndex][1] += delta;
			else
				flameData[pIndex][1] = 0;
		}
		invalidate();
		return this;

	}

	@Override
	public PauliFlame eliminateRe(int pIndex, long delta) {

		if (delta > 0) {
			if (flameData[pIndex][2] >= delta)
				flameData[pIndex][2] -= delta;
			else
				flameData[pIndex][2] = 0;
		} else {
			if (flameData[pIndex][0] >= -delta)
				flameData[pIndex][0] += delta;
			else
				flameData[pIndex][0] = 0;
		}
		invalidate();
		return this;

	}

	public boolean equals(PauliFlame compare) {
		boolean ret = true;
		for (int i = 0; i < this.flameData.length; i++) {
			for (int j = 0; j < this.flameData[0].length; j++)
				ret &= (this.flameData[i][j] == compare.flameData[i][j]);
		}

		ret &= (this.observable == null && compare.observable == null) || (this.observable != null
				&& compare.observable != null && this.observable.equals(compare.observable));
		ret &= (this.perspektive != null && compare.perspektive != null
				&& this.perspektive.equals(compare.perspektive));

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

		out.append(WSGMatrix.getCSVHeader(separator, prefix + "ob", getStateDimension(), getStateDimension()));
		for (int irow = 0; irow < flameData.length; irow++) {
			for (int icol = 0; icol < flameData[0].length; icol++) {
				out.append(String.format("%c%sP%dI%d", separator, prefix, irow, icol));
			}
		}
		out.append(WSGMatrix.getCSVHeader(separator, prefix, getStateDimension(), getStateDimension()));
		out.append(WSGMatrix.getCSVHeader(separator, prefix + "pe", getStateDimension(), 1));
		out.append(WSGMatrix.getCSVHeader(separator, prefix + "ev", getStateDimension(), getStateDimension()));
		out.append(WSGMatrix.getCSVHeader(separator, prefix + "ew", getStateDimension(), 1));

		return out.toString();
	}

	public WSGMatrix getMetrTensor() {
		if (!isCalculatedMetrTensor) {

			try {
				WSGMatrix tm = getTransMatrix();
				WSGMatrix mt = tm.adjungiert();
				metrTensor = mt.operate(tm);
				isCalculatedMetrTensor = true;
			} catch (InvalidTermException e) {
				Logging.getErrorLog().showError(e.toString(), 2);
			}
		}

		return metrTensor;
	}

	@Override
	public long getNullPairsIm(int pIndex) {
		long ret = flameData[pIndex][1];
		if (ret > flameData[pIndex][3])
			ret = flameData[pIndex][3];
		return ret;
	}

	@Override
	public long getNullPairsRe(int pIndex) {
		long ret = flameData[pIndex][0];
		if (ret > flameData[pIndex][2])
			ret = flameData[pIndex][2];
		return ret;
	}

	public WSGMatrix getObservable() throws InvalidTermException {
		if (observable != null)
			return observable;
		else
			return toWSGMatrix().getHermitian();

	}

	@Override
	public int getParticleDimension() {
		return flameData.length;
	}

	public WSGMatrix getPerspektive() {
		return perspektive;
	}

	@Override
	public WSGMatrix getState() {
		if (!isCalculatedState) {
			try {
				WSGMatrix stateRaw = toWSGMatrix().operate(perspektive);
				stateVector = getTransMatrix().operate(stateRaw);
				isCalculatedState = true;
			} catch (InvalidTermException e) {
				Logging.getErrorLog().showError(e.toString(), 2);
			}
		}
		return stateVector;
	}

	@Override
	public WSGComplex getStateAmplitude(int pIndex) {
		return getState().getColumn(0)[pIndex];
	}

	@Override
	public int getStateDimension() {
		return 2;
	}

	/**
	 * Die Funktion berechnet die Transformationsmatrix von Vektorkomponenten in
	 * kanonischer Basis in die neuen Komponenten bzgl. der Eigenvektorbasis
	 * 
	 * @return
	 */
	private WSGMatrix getTransMatrix() {
		if (!isCalculatedTransMatrix) {
			try {
				transMatrix = getObservable().getEigenVektoren().inverse();
				isCalculatedTransMatrix = true;
			} catch (InvalidTermException e) {
				Logging.getErrorLog().showError(e.toString(), 2);
			}
		}
		return transMatrix;
	}

	@Override
	public long getUnpairedIm(int pIndex) {
		return sizeIm(pIndex) - 2 * getNullPairsIm(pIndex);
	}

	@Override
	public long getUnpairedRe(int pIndex) {
		return sizeRe(pIndex) - 2 * getNullPairsRe(pIndex);
	}

	private void invalidate() {
		setCalculatedFalse(255);
	}

	private void setCalculated(int flags, boolean b) {
		if ((flags & flagMetrTensor) > 0)
			isCalculatedMetrTensor = b;
		if ((flags & flagState) > 0)
			isCalculatedState = b;
		if ((flags & flagTransMatrix) > 0)
			isCalculatedTransMatrix = b;
	}

	private void setCalculatedFalse(int flags) {
		setCalculated(flags, false);
	}

	@Override
	public long size() {
		long sz = 0;
		for (int i = 0; i < flameData.length; i++)
			for (int j = 0; j < flameData[i].length; j++)
				sz += flameData[i][j];
		return sz;
	}

	private long sizeIm(int pIndex) {
		if (pIndex < 4 && pIndex >= 0)
			return flameData[pIndex][1] + flameData[pIndex][3];
		return -1;
	}

	private long sizeRe(int pIndex) {
		if (pIndex < 4 && pIndex >= 0)
			return flameData[pIndex][0] + flameData[pIndex][2];
		return -1;
	}

	@Override
	public double testGlobalTargetIm(int pIndex, long delta) {
		return testTargetIm(pIndex, delta);
	}

	@Override
	public double testGlobalTargetRe(int pIndex, long delta) {
		return testTargetRe(pIndex, delta);
	}

	@Override
	public synchronized double testTargetIm(int pIndex, long delta) {

		double tgt = 0.0;
		if (delta > 0) {
			flameData[pIndex][1] += delta;
			invalidate();
			tgt = target(pIndex);
			flameData[pIndex][1] -= delta;
			invalidate();
		} else {
			flameData[pIndex][3] -= delta;
			invalidate();
			tgt = target(pIndex);
			flameData[pIndex][3] += delta;
			invalidate();
		}
		return tgt;
	}

	@Override
	public synchronized double testTargetRe(int pIndex, long delta) {

		double tgt = 0.0;
		if (delta > 0) {
			flameData[pIndex][0] += delta;
			invalidate();
			tgt = target(pIndex);
			flameData[pIndex][0] -= delta;
			invalidate();
		} else {
			flameData[pIndex][2] -= delta;
			invalidate();
			tgt = target(pIndex);
			flameData[pIndex][2] += delta;
			invalidate();
		}
		return tgt;
	}

	@Override
	public String toCSV(char separator) {
		StringBuffer out = new StringBuffer();
		out.append(super.toCSV(separator));

		try {
			out.append(getObservable().toCSV(separator));
			for (int irow = 0; irow < flameData.length; irow++) {
				for (int icol = 0; icol < flameData[0].length; icol++) {
					out.append(String.format("%c%d", separator, flameData[irow][icol]));
				}
			}
			out.append(toWSGMatrix().toCSV(separator));
			out.append(getPerspektive().toCSV(separator));
			out.append(getObservable().getEigenVektoren().transponiert().toCSV(separator));
			for (int i = 0; i < getStateDimension(); i++)
				out.append(String.format("%s", getObservable().getEigenwerte()[i].toCSV(separator)));

		} catch (InvalidTermException e) {
			Logging.getErrorLog().showError("observable invalid;" + e.toString(), 2);
		}
		return out.toString();
	}

	@Override
	public String toString() {
		StringBuffer retS = new StringBuffer(super.toString());
		try {
			retS.append("Observable:\n" + getObservable());
		} catch (InvalidTermException e) {
			Logging.getErrorLog().showError("observable invalid;" + e.toString(), 2);
		}

		String[] cTitles = { "*(1)", "*(i)", "*(-1)", "(-i)" };

		if (flameData == null)
			retS.append("\tInvalid FlameData\n");
		else {

			for (int iP = 0; iP < flameData.length; iP++)
				retS.append(String.format("\tP_%d", iP));
			retS.append("\n");

			for (int iP = 0; iP < flameData[0].length; iP++) {
				retS.append(cTitles[iP] + ":");
				for (int iM = 0; iM < flameData.length; iM++) {
					retS.append("\t" + flameData[iM][iP]);
				}
				retS.append("\n");
			}
		}
		retS.append("Matrix :\n" + toWSGMatrix().toString());
		retS.append("Perspective:\n" + getPerspektive().toString());
		return retS.toString();
	}

	public WSGMatrix toWSGMatrix() {
		WSGComplex[][] flameMatrixData = new WSGComplex[2][2]; // Koeeffizienten

		flameMatrixData[0][0] = new WSGComplex(flameData[0][0] - flameData[0][2] + flameData[3][0] - flameData[3][2],
				flameData[0][1] - flameData[0][3] + flameData[3][1] - flameData[3][3]);
		flameMatrixData[0][1] = new WSGComplex(flameData[1][0] - flameData[1][2] + flameData[2][1] - flameData[2][3],
				flameData[1][1] - flameData[1][3] + flameData[2][2] - flameData[2][0]);
		flameMatrixData[1][0] = new WSGComplex(flameData[1][0] - flameData[1][2] + flameData[2][3] - flameData[2][1],
				flameData[1][1] - flameData[1][3] + flameData[2][0] - flameData[2][2]);
		flameMatrixData[1][1] = new WSGComplex(flameData[0][0] - flameData[0][2] - flameData[3][0] + flameData[3][2],
				flameData[0][1] - flameData[0][3] - flameData[3][1] + flameData[3][3]);

		return new WSGMatrix(flameMatrixData);
	}

	@Override
	public long getIdeaData(int pIndex, int iIndex) {
		if (flameData != null && flameData[0] != null && pIndex >= 0 && pIndex < flameData.length && iIndex >= 0
				&& iIndex < flameData[0].length)
			return flameData[pIndex][iIndex];
		else
			return -1;
	}

}
