package de.bzus.flame.process;

import java.util.Random;

import de.bzus.flame.common.WSGComplex;
import de.bzus.flame.common.WSGMatrix;

/**
 * @author Werner Siegfried Genreith
 * 
 *         The central class for the flame model holds all particles that belong
 *         to a given flame. Two models are actually derived from this super
 *         class: - Pauli model - EV model
 *
 */
public abstract class Flame {

	private Random burnRand = new Random(System.nanoTime());

	public abstract double absQ();

	public Flame addNullPairRandom() {
		int rP = burnRand.nextInt(getParticleDimension());
		addNullRings(rP, 1L);
		return this;
	}

	public abstract double testGlobalTargetIm(int pIndex, long delta);

	public abstract double testGlobalTargetRe(int pIndex, long delta);

	public Flame addNullPairRandom(int pIndex) {
		addNullPairsRandom(pIndex, 1L);
		return this;
	}

	public abstract double testTargetRe(int pIndex, long delta);

	public abstract double testTargetIm(int pIndex, long delta);

	public abstract long getUnpairedRe(int pIndex);

	public abstract long getUnpairedIm(int pIndex);

	public long getUnpaired(int pIndex) {
		return getUnpairedRe(pIndex) + getUnpairedIm(pIndex);
	}

	public abstract long getIdeaData(int pIndex, int iIndex);

	public abstract void addNullPairsIm(int pIndex, long delta);

	public void addNullPairsRandom(int pIndex, long delta) {
		for (int i = 0; i < delta; i++)
			if (burnRand.nextDouble() < 0.5)
				addNullPairsIm(pIndex, 1);
			else
				addNullPairsRe(pIndex, 1);
	}

	public abstract void addNullPairsRe(int pIndex, long delta);

	public void addNullRings(int pIndex, long delta) {
		addNullPairsRe(pIndex, delta);
		addNullPairsIm(pIndex, delta);
	}

	public void addNullRingsRandom(long delta) {
		for (int i = 0; i < delta; i++) {
			int pIndex = burnRand.nextInt(4);
			addNullPairsRe(pIndex, 1);
			addNullPairsIm(pIndex, 1);
		}
	}

	public double excitement() {
		double max = 0.0;
		for (int pIndex = 0; pIndex < getParticleDimension(); pIndex++) {
			double a = excitement(pIndex);
			max = a > max ? a : max;
		}
		return max;
	}

	public abstract double excitement(int pIndex);

	@Override
	public abstract Flame clone();

	public abstract Flame eliminateIm(int pIndex, long delta);

	public abstract Flame eliminateRe(int pIndex, long delta);

	public String getCSVHeader(char separator) {
		return getCSVHeader(separator, "F_");
	}

	public String getCSVHeader(char separator, String prefix) {

		StringBuffer retS = new StringBuffer();
		retS.append(separator + "FlameClass");

		retS.append(WSGMatrix.getCSVHeader(separator, prefix, getStateDimension(), 1));
		retS.append(String.format("%1$c%7$s%2$s" + "%1$c%7$s%3$s" + "%1$c%7$s%4$s" + "%1$c%7$s%5$s" + "%1$c%7$s%6$s",
				separator, "absQ", "size", "sDim", "pDim", "glb exc", prefix));
		for (int i = 0; i < getStateDimension(); i++)
			retS.append(String.format("%1$c%4$s%2$s[%3$2d]", separator, "loc exc", i + 1, prefix));

		int shift = 0;
		if (getParticleDimension() == getStateDimension())
			shift = 1;
		for (int i = 0; i < getParticleDimension(); i++)
			retS.append(String.format("%1$c%5$s%2$s[%4$2d]" + "%1$c%5$s%3$s[%4$2d]", separator, "NullPairsRe",
					"NullPairsIm", i + shift, prefix));
		return retS.toString();
	}

	public long getNullPairs() {
		long ret = 0;
		for (int j = 0; j < getParticleDimension(); j++)
			ret += getNullPairs(j);
		return ret;
	}

	public long getNullPairs(int pIndex) {
		return getNullPairsIm(pIndex) + getNullPairsRe(pIndex);
	}

	public long getNullRings(int pIndex) {

		return getNullPairsRe(pIndex) > getNullPairsIm(pIndex) ? getNullPairsIm(pIndex) : getNullPairsRe(pIndex);

	}

	public abstract long getNullPairsIm(int pIndex);

	public abstract long getNullPairsRe(int pIndex);

	public abstract int getParticleDimension();

	public abstract WSGMatrix getState();

	public abstract WSGComplex getStateAmplitude(int sIndex);

	public abstract int getStateDimension();

	public abstract long size();

	public double target() {
		return Math.sqrt(excitement());
	}

	public double target(int index) {
		return Math.sqrt(excitement(index));
	}

	public String toCSV(char separator) {
		StringBuffer retS = new StringBuffer();
		retS.append(separator + this.getClass().getName());

		retS.append(getState().toCSV(separator));
		retS.append(String.format("%1$c%2$5.0f" + "%1$c%3$d" + "%1$c%4$d" + "%1$c%5$d" + "%1$c%6$7.5f", separator,
				absQ(), size(), getStateDimension(), getParticleDimension(), excitement()));
		for (int i = 0; i < getStateDimension(); i++)
			retS.append(String.format("%1$c%2$7.5f", separator, excitement(i)));
		for (int i = 0; i < getParticleDimension(); i++)
			retS.append(String.format("%1$c%2$d" + "%1$c%3$d", separator, getNullPairsRe(i), getNullPairsIm(i)));

		return retS.toString();
	}

	@Override
	public String toString() {

		StringBuffer retS = new StringBuffer(this.getClass().getSimpleName() + "\n");

		retS.append("state :\n" + getState().toString());
		retS.append(
				String.format("%nabsQ=%5.0f; size=%d; stateDimension=%d; particleDimension=%d; global excitation=%7.5f",
						absQ(), size(), getStateDimension(), getParticleDimension(), excitement()));
		for (int i = 0; i < getStateDimension(); i++)
			retS.append(String.format("%nlocal excitation(%1$d)=%2$7.5f", i + 1, excitement(i)));

		int shift = 0;
		if (getStateDimension() == getParticleDimension())
			shift = 1;
		for (int i = 0; i < getParticleDimension(); i++)
			retS.append(String.format("%nnullCountRe(%1$d)=%2$d; nullCountIm(%1$d)=%3$d ", i + shift, getNullPairsRe(i),
					getNullPairsIm(i)));

		retS.append("\n\n");
		return retS.toString();

	}

}
