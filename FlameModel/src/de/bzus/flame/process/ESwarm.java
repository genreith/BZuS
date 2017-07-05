package de.bzus.flame.process;

import de.bzus.flame.common.Globals;
import de.bzus.flame.common.Logging;
import de.bzus.flame.common.WSGComplex;
import de.bzus.flame.common.WSGMatrix;
import de.bzus.flame.exceptions.InvalidTermException;

/**
 * @author Werner Siegfried Genreith
 *
 *         EV model considers particles as normalized eigen/base vectors and
 *         hence has no need for an observable or perspective
 * 
 */
public class ESwarm extends Flame {

	private long[][] baseData = null;
	private boolean baseDataCalculated = false;

	/**
	 * 
	 * evData is a N*4 matrix one row for each dimension representing normalized
	 * base vectors e_j. The 4 columns represent the positive counts of e_j, i*e_j.
	 * -e_j, -i e_j
	 * 
	 */
	private long[][] ideaData = null;
	private boolean ideaDataCalculated = false;
	private long[][] restData = null;
	private WSGMatrix toEnvironmentBase = null;
	private WSGMatrix toSwarmBase = null;

	public ESwarm(long[][] evData) {
		this.baseData = new long[evData.length][4];
		this.ideaData = new long[evData.length][4];
		for (int i = 0; i < this.ideaData.length; i++)
			for (int j = 0; j < 4; j++) {
				this.baseData[i][j] = evData[i][j];
				this.ideaData[i][j] = evData[i][j];
			}

		WSGComplex[][] envMatrix_0 = new WSGComplex[baseData.length][baseData.length];
		for (int idea = 0; idea < baseData.length; idea++) {
			for (int baseIndex = 0; baseIndex < baseData.length; baseIndex++) {
				envMatrix_0[idea][baseIndex] = (idea == baseIndex) ? WSGComplex.ONE : WSGComplex.ZERO;
			}
		}

		toSwarmBase = new WSGMatrix(envMatrix_0);
		toEnvironmentBase = new WSGMatrix(envMatrix_0);

		ideaDataCalculated = true;
		baseDataCalculated = true;
	}

	public ESwarm(long[][] baseData, long[][][] environment) {

		this.baseData = new long[baseData.length][4];
		for (int i = 0; i < this.baseData.length; i++)
			for (int j = 0; j < 4; j++)
				this.baseData[i][j] = baseData[i][j];

		WSGComplex[][] envMatrix_0 = new WSGComplex[baseData.length][baseData.length];

		for (int baseIndex = 0; baseIndex < baseData.length; baseIndex++) {
			for (int ideaIndex = 0; ideaIndex < baseData.length; ideaIndex++) {
				for (int iPart = 0; iPart < 2; iPart++) {
					envMatrix_0[baseIndex][ideaIndex] = new WSGComplex(environment[baseIndex][ideaIndex][0],
							environment[baseIndex][ideaIndex][1]);
				}
			}
		}
		toSwarmBase = new WSGMatrix(envMatrix_0);
		try {
			toEnvironmentBase = toSwarmBase.inverse();
		} catch (InvalidTermException e) {
			Logging.getErrorLog().showError("Invalid atom data", 2);
		}
		baseDataCalculated = true;
		getIdeaData();
	}

	@Override
	public double absQ() {
		double absValue = 0;
		for (int i = 0; i < ideaData.length; i++) {
			long re = ideaData[i][0] - ideaData[i][2];
			long im = ideaData[i][1] - ideaData[i][3];
			absValue += re * re + im * im;
		}
		return absValue;
	}

	@Override
	public void addNullPairsIm(int pIndex, long delta) {
		ideaData[pIndex][1] += delta;
		ideaData[pIndex][3] += delta;
		baseDataCalculated = false;
	}

	@Override
	public void addNullPairsRe(int pIndex, long delta) {
		ideaData[pIndex][0] += delta;
		ideaData[pIndex][2] += delta;
		baseDataCalculated = false;
	}

	@Override
	public ESwarm clone() {
		long[][] data = new long[this.ideaData.length][4];
		for (int i = 0; i < this.ideaData.length; i++)
			for (int j = 0; j < 4; j++)
				data[i][j] = this.ideaData[i][j];
		return new ESwarm(data);
	}

	@Override
	public ESwarm eliminateIm(int pIndex, long delta) {

		// hier verbrenne ich jetzt delta Nullpaare, so dass die gewünschten
		// Elemente übrig bleiben.
		// Wenn also delta > 0 ist, dann entferne ich delta Elemente beim
		// Negativen Wert, sonst umgekehrt

		if (delta > 0) {
			if (ideaData[pIndex][3] >= delta) {
				ideaData[pIndex][3] -= delta;
			} else {
				ideaData[pIndex][3] = 0;
			}
		} else {
			if (ideaData[pIndex][1] >= -delta) {
				ideaData[pIndex][1] += delta;
			} else {
				ideaData[pIndex][1] = 0;
			}
		}
		baseDataCalculated = false;

		return this;

	}

	@Override
	public ESwarm eliminateRe(int pIndex, long delta) {
		long jAbs = delta > 0 ? delta : -delta;
		ESwarm ret = this;

		if (delta > 0) {
			if (ret.ideaData[pIndex][2] >= jAbs) {
				ret.ideaData[pIndex][2] -= jAbs;
			} else {
				ret.ideaData[pIndex][2] = 0;
			}
		} else {
			if (ret.ideaData[pIndex][0] >= jAbs) {
				ret.ideaData[pIndex][0] -= jAbs;
			} else {
				ret.ideaData[pIndex][0] = 0;
			}
		}
		baseDataCalculated = false;

		return ret;

	}

	public boolean equals(ESwarm compare) {
		boolean ret = true;
		for (int i = 0; i < this.ideaData.length; i++) {
			for (int j = 0; j < 4; j++)
				ret &= (this.ideaData[i][j] == compare.ideaData[i][j]);
		}
		return ret;
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

	private long[][] getBaseData() {

		if (!baseDataCalculated) {
			long[][] bData = getBaseData(ideaData, null);
			for (int iBase = 0; iBase < baseData.length; iBase++) {
				for (int iPot = 0; iPot < 4; iPot++) {
					baseData[iBase][iPot] = bData[iBase][iPot] + restData[iBase][iPot];
				}
			}
			baseDataCalculated = true;
		}
		return baseData;
	}

	private long[][] getBaseData(long[][] iData, long[][] bData) {

		if (bData == null)
			bData = new long[iData.length][4];

		WSGComplex[][] rawState = new WSGComplex[4][iData.length];
		for (int iOption = 0; iOption < iData.length; iOption++) {
			rawState[0][iOption] = new WSGComplex(iData[iOption][0]);
			rawState[1][iOption] = new WSGComplex(0, iData[iOption][1]);
			rawState[2][iOption] = new WSGComplex(-iData[iOption][2]);
			rawState[3][iOption] = new WSGComplex(0, -iData[iOption][3]);
		}

		try {

			WSGMatrix[] swarmBasedVectors = new WSGMatrix[4];
			for (int iPot = 0; iPot < 4; iPot++) {
				swarmBasedVectors[iPot] = toSwarmBase.operate(new WSGMatrix(rawState[iPot]));
				for (int iBase = 0; iBase < baseData.length; iBase++) {
					long re = (long) swarmBasedVectors[iPot].getEntry(iBase, 0).getReal();
					long im = (long) swarmBasedVectors[iPot].getEntry(iBase, 0).getImaginary();
					if (re > 0)
						bData[iBase][0] += re;
					else
						bData[iBase][2] -= re;
					if (im > 0)
						bData[iBase][1] += im;
					else
						bData[iBase][3] -= im;
				}
			}

		} catch (InvalidTermException e) {
			Logging.getErrorLog().showError(e.toString(), 2);
		}

		return bData;
	}

	@Override
	public String getCSVHeader(char separator) {
		return getCSVHeader(separator, "F_");
	}

	@Override
	public String getCSVHeader(char separator, String prefix) {

		StringBuffer out = new StringBuffer();
		out.append(super.getCSVHeader(separator, prefix));

		for (int irow = 0; irow < baseData.length; irow++) {
			for (int icol = 0; icol < baseData[0].length; icol++) {
				out.append(String.format("%c%sE%dI%d", separator, prefix, irow + 1, icol));
			}
		}
		for (int irow = 0; irow < baseData.length; irow++) {
			for (int icol = 0; icol < baseData[0].length; icol++) {
				out.append(String.format("%c%sE%dI%d", separator, prefix + "Env", irow + 1, icol));
			}
		}

		return out.toString();
	}

	public WSGComplex[] getEvDataComplex() {
		WSGComplex[] retC = new WSGComplex[ideaData.length];
		for (int i = 0; i < ideaData.length; i++) {
			retC[i] = getStateAmplitude(i);
		}
		return retC;
	}

	private long[][] getIdeaData() {
		if (!ideaDataCalculated) {
			restData = new long[baseData.length][4];
			ideaData = getIdeaData(baseData, ideaData, restData);
			ideaDataCalculated = true;
			
			long[][] rData = new long[baseData.length][4];
			long[][] iNPData = ideaNullpairs(rData);
			System.out.println(iNPData);
		}

		return ideaData;
	}

	private long[][] ideaNullpairs(long[][] rData) {
		long[][] inpData = new long[baseData.length][4];
		if (rData == null)
			rData = new long[baseData.length][4];

		try {
			WSGComplex[] rawNP = new WSGComplex[baseData.length];
			for (int iBase = 0; iBase < baseData.length; iBase++) {
				rawNP[iBase] = new WSGComplex(
						restData[iBase][0] > restData[iBase][2] ? restData[iBase][2] : restData[iBase][0],
						restData[iBase][1] > restData[iBase][3] ? restData[iBase][3] : restData[iBase][1]);
			}
			WSGMatrix ideaNP = toEnvironmentBase.operate(new WSGMatrix(rawNP));
			for (int iOption = 0; iOption < inpData.length; iOption++) {
				long re = (long) ideaNP.getEntry(iOption, 0).getReal();
				re = re > 0 ? re : -re;
				long im = (long) ideaNP.getEntry(iOption, 0).getImaginary();
				im = im > 0 ? im : -im;
				inpData[iOption][0] += re;
				inpData[iOption][2] += re;
				inpData[iOption][1] += im;
				inpData[iOption][3] += im;

			}
			rData = getRestData(baseData, inpData, rData);
		} catch (InvalidTermException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return inpData;
	}

	private long[][] getIdeaData(long[][] bData, long[][] iData, long[][] rData) {
		if (iData == null)
			iData = new long[bData.length][4];

		WSGComplex[] rawState = new WSGComplex[bData.length];
		for (int iOption = 0; iOption < bData.length; iOption++) {
			rawState[iOption] = new WSGComplex(bData[iOption][0] - bData[iOption][2],
					bData[iOption][1] - bData[iOption][3]);
		}
		try {
			WSGMatrix ideaState = toEnvironmentBase.operate(new WSGMatrix(rawState));
			for (int iOption = 0; iOption < bData.length; iOption++) {
				long rawValue = (long) ideaState.getEntry(iOption, 0).getReal();
				if (rawValue > 0) {
					iData[iOption][0] = rawValue;
					iData[iOption][2] = 0;
				} else {
					iData[iOption][0] = 0;
					iData[iOption][2] = -rawValue;
				}
				rawValue = (long) ideaState.getEntry(iOption, 0).getImaginary();
				if (rawValue > 0) {
					iData[iOption][1] = rawValue;
					iData[iOption][3] = 0;
				} else {
					iData[iOption][1] = 0;
					iData[iOption][3] = -rawValue;
				}
			}

			rData = getRestData(bData, iData, rData);

		} catch (InvalidTermException e) {
			e.printStackTrace();
		}
		return iData;

	}

	@Override
	public long getIdeaData(int pIndex, int iIndex) {
		if (ideaData != null && ideaData[0] != null && pIndex >= 0 && pIndex < ideaData.length && iIndex >= 0
				&& iIndex < ideaData[0].length)
			return ideaData[pIndex][iIndex];
		else
			return -1;
	}

	@Override
	public long getNullPairsIm(int index) {
		long ret = ideaData[index][1];
		if (ret > ideaData[index][3])
			ret = ideaData[index][3];
		return ret;
	}

	@Override
	public long getNullPairsRe(int index) {
		long ret = ideaData[index][0];
		if (ret > ideaData[index][2])
			ret = ideaData[index][2];
		return ret;
	}

	@Override
	public int getParticleDimension() {
		return getStateDimension();
	}

	private long[][] getRestData(long[][] bData, long[][] iData, long[][] rData) {

		if (rData == null)
			rData = new long[bData.length][4];

		long[][] rawData = getBaseData(iData, null);
		for (int iBase = 0; iBase < bData.length; iBase++) {
			for (int iPot = 0; iPot < 4; iPot++) {
				rData[iBase][iPot] = bData[iBase][iPot] - rawData[iBase][iPot];
			}
		}

		return rData;
	}

	@Override
	public WSGMatrix getState() {
		return new WSGMatrix(getEvDataComplex());
	}

	@Override
	public WSGComplex getStateAmplitude(int pIndex) {
		return new WSGComplex(ideaData[pIndex][0] - ideaData[pIndex][2], ideaData[pIndex][1] - ideaData[pIndex][3]);
	}

	@Override
	public int getStateDimension() {
		if (ideaData == null)
			return 0;
		else
			return ideaData.length;
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
		for (int i = 0; i < ideaData.length; i++)
			for (int j = 0; j < ideaData[i].length; j++)
				sz += ideaData[i][j];
		return sz;
	}

	private long sizeIm(int i) {
		return ideaData[i][1] + ideaData[i][3];
	}

	private long sizeRe(int i) {
		return ideaData[i][0] + ideaData[i][2];
	}

	public synchronized double testGlobalTargetIm(int pIndex, long delta) {

		double tgt = 0.0;
		if (delta > 0) {
			ideaData[pIndex][1] += delta;
			tgt = target();
			ideaData[pIndex][1] -= delta;
		} else {
			ideaData[pIndex][3] -= delta;
			tgt = target();
			ideaData[pIndex][3] += delta;
		}
		return tgt;
	}

	public synchronized double testGlobalTargetRe(int pIndex, long delta) {

		double tgt = 0.0;
		if (delta > 0) {
			ideaData[pIndex][0] += delta;
			tgt = target();
			ideaData[pIndex][0] -= delta;
		} else {
			ideaData[pIndex][2] -= delta;
			tgt = target();
			ideaData[pIndex][2] += delta;
		}
		return tgt;
	}

	public synchronized double testTargetIm(int pIndex, long delta) {

		double tgt = 0.0;
		if (delta > 0) {
			ideaData[pIndex][1] += delta;
			tgt = target(pIndex);
			ideaData[pIndex][1] -= delta;
		} else {
			ideaData[pIndex][3] -= delta;
			tgt = target(pIndex);
			ideaData[pIndex][3] += delta;
		}
		return tgt;
	}

	public synchronized double testTargetRe(int pIndex, long delta) {

		double tgt = 0.0;
		if (delta > 0) {
			ideaData[pIndex][0] += delta;
			tgt = target(pIndex);
			ideaData[pIndex][0] -= delta;
		} else {
			ideaData[pIndex][2] -= delta;
			tgt = target(pIndex);
			ideaData[pIndex][2] += delta;
		}
		return tgt;
	}

	@Override
	public String toCSV(char separator) {

		StringBuffer out = new StringBuffer();
		out.append(super.toCSV(separator));

		for (int irow = 0; irow < getBaseData().length; irow++) {
			for (int icol = 0; icol < baseData[0].length; icol++) {
				out.append(String.format("%c%d", separator, baseData[irow][icol]));
			}
		}
		for (int irow = 0; irow < ideaData.length; irow++) {
			for (int icol = 0; icol < ideaData[0].length; icol++) {
				out.append(String.format("%c%d", separator, ideaData[irow][icol]));
			}
		}

		return out.toString();
	}

	@Override
	public String toString() {

		StringBuffer retS = new StringBuffer(super.toString());
		String[] cTitles = { "*(1)", "*(i)", "*(-1)", "(-i)" };

		if (getBaseData() == null)
			retS.append("\tInvalid FlameData\n");
		else {

			try {
				retS.append("Environment (det=" + toSwarmBase.determinante() + "):\n" + toSwarmBase + "\n");

				for (int iP = 0; iP < getBaseData().length; iP++)
					retS.append(String.format("\tE_%d", iP + 1));
				for (int iP = 0; iP < baseData.length; iP++)
					retS.append(String.format("\tA_%d", iP + 1));
				retS.append("\n");

				for (int iP = 0; iP < baseData[0].length; iP++) {
					retS.append(cTitles[iP] + ":");
					for (int iM = 0; iM < baseData.length; iM++) {
						retS.append("\t" + baseData[iM][iP]);
					}
					for (int iM = 0; iM < getIdeaData().length; iM++) {
						retS.append("\t" + ideaData[iM][iP]);
					}
					restData = getRestData(baseData, ideaData, restData);
					for (int iM = 0; iM < getRestData(baseData, ideaData, restData).length; iM++) {
						retS.append("\t" + restData[iM][iP]);
					}
					retS.append("\n");
				}
			} catch (InvalidTermException e) {
				retS.append("Environment:\n" + toSwarmBase + "\n");
			}

		}

		return retS.toString();

	}

}
