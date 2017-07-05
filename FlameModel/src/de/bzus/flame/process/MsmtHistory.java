package de.bzus.flame.process;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;

import de.bzus.flame.common.Globals;
import de.bzus.flame.common.Logging;
import de.bzus.flame.common.WSGMatrix;
import de.bzus.flame.interfaces.IFResultsList;

public class MsmtHistory implements IFResultsList {

	private final long[] actuals;
	private final double[] actualDistribution;
	private double[] actualSigma;
	private long[] avgNullPairs;
	private int convAmpl = 0;
	private final int sDim;
	private int divAmpl = 0;
	private String endTime;
	private double chiSquareActual = 0;
	private double[] chiTest;

	private double chiSquareCritical999;
	private double chiSquareCritical990;
	private double chiSquareCritical975;
	private double chiSquareCritical950;
	private double chiSquareCritical900;

	private final Flame[][] flameHistories;
	private final int iterations;
	private long[] maxNullPairs;

	private double maxSchwarmBetrag;

	private long maxSchwarmGroesse;

	private final int measurements;

	private final String methodSelected;

	private long[] minNullPairs;
	private double minSchwarmBetrag;

	private long minSchwarmGroesse;
	private double mittlererSchwarmBetrag;

	private long mittlereSchwarmGroesse;
	private final Flame[] msmtResults;
	private final int nHistories;

	private final Flame sourceFlame;
	private String startTime;

	private final double[] targetDistribution;

	private int targetIndexSum = 0;
	private double avgIterations = 0;
	private int maxIterations = Integer.MIN_VALUE;
	private int minIterations = Integer.MAX_VALUE;
	private double[] targetSigma;
	private long[] varNullPairs;

	private double varSchwarmBetrag;
	private long varSchwarmGroesse;

	public MsmtHistory(int msmts, int iter, String method, int nHistories, Flame flame) {
		this.nHistories = msmts < nHistories ? msmts : nHistories;
		this.msmtResults = new Flame[msmts];
		this.flameHistories = new Flame[this.nHistories][iter];
		this.methodSelected = method;
		this.iterations = iter;
		this.measurements = msmts;
		this.sourceFlame = flame.clone();

		this.sDim = this.sourceFlame.getStateDimension();
		this.targetDistribution = new double[sDim];
		this.actuals = new long[sDim];
		this.actualDistribution = new double[sDim];
	}

	public void addFlameHistory(Flame flame, int msmt, int iter) {

		if (msmt < flameHistories.length && iter < flameHistories[0].length)
			flameHistories[msmt][iter] = (Flame) flame.clone();
	}

	private void calculateStats() {
		Logging.getErrorLog().showError("MsmtHistory calculateStats started", -2);

		convAmpl = 0;
		divAmpl = 0;
		int msmts = 0;

		WSGMatrix state = sourceFlame.getState();
		int pDim = sourceFlame.getParticleDimension();
		minNullPairs = new long[pDim];
		maxNullPairs = new long[pDim];
		avgNullPairs = new long[pDim];
		varNullPairs = new long[pDim];
		targetSigma = new double[sDim];
		actualSigma = new double[sDim];

		Logging.getErrorLog().showError("MsmtHistory calculateStats(10)", -2);

		for (int i = 0; i < actuals.length; i++) {
			actuals[i] = 0;
		}
		double absq = 0.0;
		for (int i = 0; i < sDim; i++)
			absq += state.getEntry(i, 0).absQ();

		if (absq > 0)
			for (int i = 0; i < targetDistribution.length; i++)
				targetDistribution[i] = state.getEntry(i, 0).absQ() / absq;
		Logging.getErrorLog().showError("MsmtHistory calculateStats(20)", -2);

		mittlereSchwarmGroesse = 0;
		varSchwarmGroesse = 0;
		maxSchwarmGroesse = Long.MIN_VALUE;
		minSchwarmGroesse = Long.MAX_VALUE;

		mittlererSchwarmBetrag = 0;
		varSchwarmBetrag = 0.0;
		maxSchwarmBetrag = Double.MIN_VALUE;
		minSchwarmBetrag = Double.MAX_VALUE;

		for (int i = 0; i < minNullPairs.length; i++) {
			minNullPairs[i] = Long.MAX_VALUE;
			minNullPairs[i] = Long.MAX_VALUE;
			avgNullPairs[i] = 0;
			varNullPairs[i] = 0;
		}
		Logging.getErrorLog().showError("MsmtHistory calculateStats(30)", -2);

		if (measurements == 0)
			Logging.getErrorLog().showError("Measurements = 0", 2);

		for (int i = 0; i < measurements; i++) {
			if (msmtResults[i] == null) {
				Logging.getErrorLog().showError("no msmt result found in position " + i, -2);
				continue;
			}
			long size = msmtResults[i].size();
			mittlereSchwarmGroesse += size;
			varSchwarmGroesse += size * size;
			maxSchwarmGroesse = maxSchwarmGroesse < size ? size : maxSchwarmGroesse;
			minSchwarmGroesse = minSchwarmGroesse > size ? size : minSchwarmGroesse;

			double absQ = msmtResults[i].absQ();
			double abs = Math.sqrt(absQ);
			mittlererSchwarmBetrag += abs;
			varSchwarmBetrag += absQ;
			maxSchwarmBetrag = maxSchwarmBetrag < abs ? abs : maxSchwarmBetrag;
			minSchwarmBetrag = minSchwarmBetrag > abs ? abs : minSchwarmBetrag;

			for (int k = 0; k < pDim; k++) {
				long np = msmtResults[i].getNullPairs(k);
				avgNullPairs[k] += np;
				varNullPairs[k] += np * np;
				maxNullPairs[k] = maxNullPairs[k] < np ? np : maxNullPairs[k];
				minNullPairs[k] = minNullPairs[k] > np ? np : minNullPairs[k];
			}

			WSGMatrix actualState = msmtResults[i].getState();

			double maxAmpl = actualState.getEntry(0, 0).absQ();
			double nextBest = actualState.getEntry(1, 0).absQ();
			int maxAmplIndex = 0;

			if (maxAmpl < nextBest) {
				maxAmplIndex = 1;
				double temp = maxAmpl;
				maxAmpl = nextBest;
				nextBest = temp;
			}

			for (int iDim = 2; iDim < actuals.length; iDim++) {
				double actualAmpl = actualState.getEntry(iDim, 0).absQ();
				if (actualAmpl > maxAmpl) {
					maxAmplIndex = iDim;
					nextBest = maxAmpl;
					maxAmpl = actualAmpl;
				}
			}
			if (maxAmpl >= nextBest * 100) {
				actuals[maxAmplIndex]++;
				convAmpl++;
			} else {
				divAmpl++;
			}
			msmts++;
		}
		Logging.getErrorLog().showError("MsmtHistory calculateStats(40)", -2);

		if (msmts != measurements) {
			Logging.getErrorLog().showError("executed msmts=" + msmts + " do not match scheduled msmts " + measurements,
					1);
		}

		if (msmts > 1 && convAmpl > 1) {
			for (int i = 0; i < actuals.length; i++) {
				actualDistribution[i] = ((double) actuals[i]) / (double) convAmpl;
			}
			for (int i = 0; i < sDim; i++) {
				targetSigma[i] = Math.sqrt(targetDistribution[i] * (1.0 - targetDistribution[i]) / convAmpl);
				actualSigma[i] = Math.sqrt(actualDistribution[i] * (1.0 - actualDistribution[i]) / convAmpl);
			}
			varSchwarmGroesse /= msmts;
			mittlereSchwarmGroesse /= msmts;

			varSchwarmBetrag /= msmts;
			mittlererSchwarmBetrag /= msmts;

			for (int k = 0; k < pDim; k++) {
				avgNullPairs[k] /= msmts;
				varNullPairs[k] /= msmts;
				varNullPairs[k] = (long) Math.sqrt(varNullPairs[k] - avgNullPairs[k] * avgNullPairs[k]);
			}
		}
		Logging.getErrorLog().showError("MsmtHistory calculateStats(50)", -2);

		varSchwarmGroesse = varSchwarmGroesse - mittlereSchwarmGroesse * mittlereSchwarmGroesse;
		varSchwarmBetrag = varSchwarmBetrag - mittlererSchwarmBetrag * mittlererSchwarmBetrag;
		chiSquareActual = 0;

		if (chiTest == null)
			chiTest = new double[targetDistribution.length];

		for (int i = 0; i < targetDistribution.length; i++) {
			double target = targetDistribution[i] * convAmpl;
			chiTest[i] = target > 0.0 ? (target - actuals[i]) * (target - actuals[i]) / target
					: (actuals[i] > 0 ? Double.POSITIVE_INFINITY : 0.0);
			chiSquareActual += chiTest[i];
		}

		Logging.getErrorLog().showError("MsmtHistory calculateStats(60)", -2);

		try {
			Class.forName("org.apache.commons.math3.distribution.ChiSquaredDistribution");
			ChiSquaredDistribution cqd = new ChiSquaredDistribution(targetDistribution.length - 1);
			chiSquareCritical999 = cqd.inverseCumulativeProbability(0.999);
			chiSquareCritical990 = cqd.inverseCumulativeProbability(0.99);
			chiSquareCritical975 = cqd.inverseCumulativeProbability(0.975);
			chiSquareCritical950 = cqd.inverseCumulativeProbability(0.95);
			chiSquareCritical900 = cqd.inverseCumulativeProbability(0.90);
		} catch (ClassNotFoundException e) {
			Logging.getErrorLog().showError(
					"ChiSquaredDistribution class not available: Copy commons-math3-3.6.1.jar into main Flame.jar directory",
					1);
		}
		
		avgIterations = (convAmpl + divAmpl) > 0 ? targetIndexSum / (convAmpl + divAmpl) : 0;

		Logging.getErrorLog().showError("MsmtHistory calculateStats ended", -2);

	}

	public String getCSVHeader(char separator, boolean useObservable) {
		StringBuffer retS = new StringBuffer();
		retS.append(String.format("%1$c%2$s" + "%1$c%3$s" + "%1$c%4$s" + "%1$c%5$s" + "%1$c%6$s" + "%7$s", separator,
				"started", "ended", "method", "msmts", "iter", sourceFlame.getCSVHeader(separator, "F_")));

		retS.append(String.format("%1$c%2$s" + "%1$c%3$s" + "%1$c%4$s" + "%1$c%5$s" + "%1$c%6$s", separator, "minIter",
				"maxIter", "avgIter", "conv", "div"));

		for (int i = 0; i < sDim; i++)
			retS.append(String.format("%c%s[%d]", separator, "target", i+1));

		for (int i = 0; i < sDim; i++)
			retS.append(String.format("%c%s[%d]", separator, "sigma", i+1));
		for (int i = 0; i < sDim; i++)
			retS.append(String.format("%c%s[%d]", separator, "dist", i+1));
		for (int i = 0; i < sDim; i++)
			retS.append(String.format("%c%s[%d]", separator, "actuals", i+1));

		for (int i = 0; i < sDim; i++)
			retS.append(String.format("%cchiTest[%2d]", separator, i+1));

		retS.append(String.format("%1$c%2$s" + "%1$c%3$s" + "%1$c%4$s" + "%1$c%5$s" + "%1$c%6$s", separator, "chi999",
				"chi990", "chi975", "chi950", "chi900"));

		retS.append(String.format(
				"%1$c%2$s" + "%1$c%3$s" + "%1$c%4$s" + "%1$c%5$s" + "%1$c%6$s" + "%1$c%7$s" + "%1$c%8$s" + "%1$c%9$s",
				separator, "minAbs", "maxAbs", "avgAbs", "sigmaAbs", "minSize", "maxSize", "avgSize", "sigmaSize"));

		int shift=0;
		if (sourceFlame.getParticleDimension() == sourceFlame.getStateDimension())
			shift=1;

		for (int i = 0; i < sourceFlame.getParticleDimension(); i++) {
			retS.append(String.format("%1$c%3$s%2$d" + "%1$c%4$s%2$d" + "%1$c%5$s%2$d" + "%1$c%6$s%2$d", separator, i+shift,
					"minNP", "maxNP", "avgNP", "sigmaNP"));
		}

		return retS.toString();

	}

	@Override
	public Flame[][] getHistories() {
		return flameHistories;
	}

	@Override
	public Flame[] getHistory(int msmt) {
		return flameHistories[msmt];
	}

	public String getMD5Hash() {
		String md5Hash = "no Hash";
		String s = String.format("5d%d%s%s", measurements, iterations, sourceFlame, methodSelected);
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] bytesOfMessage = s.getBytes("UTF-8");
			byte[] thedigest = md.digest(bytesOfMessage);
			md5Hash = thedigest.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return md5Hash;
	}

	public Flame getMsmtResults(int resultIndex) {
		if (resultIndex >= 0 && msmtResults != null && resultIndex < msmtResults.length)
			return msmtResults[resultIndex];
		return null;
	}

	public void setMsmtResults(int resultIndex, Flame flame) {
		if (flame != null)
			msmtResults[resultIndex] = flame.clone();
	}

	public synchronized int increaseTargetIndexSum(int iterationsCompleted) {
		targetIndexSum += iterationsCompleted;
		maxIterations = maxIterations < iterationsCompleted ? iterationsCompleted : maxIterations;
		minIterations = minIterations > iterationsCompleted ? iterationsCompleted : minIterations;
		return targetIndexSum;
	}

	public void setEndTime() {
		this.endTime = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance());
	}

	public void setStartTime() {
		this.startTime = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance());
	}

	public String toCSV(char separator) {
		Logging.getErrorLog().showError("MsmtHistory toCSV started", -2);

		calculateStats();

		StringBuffer retS = new StringBuffer();
		retS.append(String.format("%1$c%2$s" + "%1$c%3$s" + "%1$c%4$s" + "%1$c%5$d" + "%1$c%6$d" + "%7$s", separator,
				startTime, endTime, methodSelected, measurements, iterations, sourceFlame.toCSV(separator)));
		
		retS.append(String.format("%1$c%2$d" + "%1$c%3$d" + "%1$c%4$d" + "%1$c%5$d" + "%1$c%6$d", separator,
				minIterations, maxIterations, (int)avgIterations, convAmpl, divAmpl));

		for (int i = 0; i < sDim; i++)
			retS.append(String.format("%c%7.5f", separator, targetDistribution[i]));

		for (int i = 0; i < sDim; i++)
			retS.append(String.format("%c%7.5f", separator, targetSigma[i]));
		for (int i = 0; i < sDim; i++)
			retS.append(String.format("%c%7.5f", separator, actualDistribution[i]));

		for (int i = 0; i < sDim; i++)
			retS.append(String.format("%c%d", separator, actuals[i]));

		for (int i = 0; i < sDim; i++)
			retS.append(String.format("%c%10.5f", separator, chiTest[i]));

		retS.append(String.format("%1$c%2$10.5f" + "%1$c%3$10.5f" + "%1$c%4$10.5f" + "%1$c%5$10.5f" + "%1$c%6$10.5f",
				separator, chiSquareCritical999, chiSquareCritical990, chiSquareCritical975, chiSquareCritical950,
				chiSquareCritical900));

		retS.append(String.format(
				"%1$c%2$d" + "%1$c%3$d" + "%1$c%4$d" + "%1$c%5$d" + "%1$c%6$d" + "%1$c%7$d" + "%1$c%8$d"
						+ "%1$c%9$5d",
				separator, (long) minSchwarmBetrag, (long) maxSchwarmBetrag, (long) mittlererSchwarmBetrag,
				(long) Math.sqrt(varSchwarmBetrag), (long) minSchwarmGroesse, (long) maxSchwarmGroesse,
				(long) mittlereSchwarmGroesse, (long) Math.sqrt(varSchwarmGroesse)));

		for (int i = 0; i < sourceFlame.getParticleDimension(); i++) {
			retS.append(String.format("%1$c%2$d" + "%1$c%3$d" + "%1$c%4$d" + "%1$c%5$d", separator, minNullPairs[i],
					maxNullPairs[i], avgNullPairs[i], (long) Math.sqrt(varNullPairs[i])));
		}
		Logging.getErrorLog().showError("MsmtHistory toCSV ended", -2);

		return retS.toString();

	}

	public String toString() {

		calculateStats();

		StringBuffer textSummary = new StringBuffer();
		textSummary.append("\n\nsource swarm " + sourceFlame);
		textSummary.append(String.format("%n%nresults from %s running %d measurements at %d iterations each:",
				methodSelected, measurements, iterations));
		int msmts = divAmpl + convAmpl;
		if (msmts > 0 && convAmpl > 0) {
			textSummary.append("\n\nrelative values:");
			textSummary.append("\ntargets=");
			for (int i = 0; i < this.targetDistribution.length; i++) {
				textSummary.append(String.format("\t%10.5f %%", this.targetDistribution[i] * 100.0));
			}

			textSummary.append("\nresults=");
			for (int i = 0; i < actuals.length; i++) {
				textSummary.append(String.format("\t%10.5f %%", actualDistribution[i] * 100.0));
			}

			textSummary.append("\nsigma=");
			for (int i = 0; i < actuals.length; i++) {
				double sigma = Math.sqrt(targetDistribution[i] * (1.0 - targetDistribution[i]) * msmts);
				textSummary.append(String.format("\t%10.5f %%", sigma * 100.0 / msmts));
			}
			double pKonvergent = 100.0 * convAmpl / msmts;
			double pDivergent = 100.0 - pKonvergent;
			textSummary.append(
					String.format("%ndivergent: %10.5f %%; convergent: %10.5f %% based on %10d measurements performed",
							pDivergent, pKonvergent, msmts));

		} else {
			textSummary.append("\nno results");
		}

		textSummary.append("\n\nabsolute values:");
		textSummary.append("\ntargets=");
		for (int i = 0; i < actuals.length; i++) {
			textSummary.append(String.format("\t%10.0f", this.targetDistribution[i] * convAmpl));
		}
		textSummary.append("\t(values rounded)");

		textSummary.append("\nresults=");
		for (int i = 0; i < actuals.length; i++) {
			textSummary.append(String.format("\t%10d", actuals[i]));
		}

		textSummary.append("\nsigma=");
		for (int i = 0; i < actuals.length; i++) {
			double sigma = Math.sqrt(targetDistribution[i] * (1 - targetDistribution[i]) * convAmpl);
			textSummary.append(String.format("\t%10.5f", sigma));
		}
		textSummary.append(String.format("%nmeasurements scheduled: %10d; divergent: %10d; convergent %10d",
				measurements, divAmpl, convAmpl));
		textSummary.append(String.format("%nmin iterations: %10d; max iterations: %10d; average iterations %10.1f%n",
				minIterations, maxIterations, avgIterations));

		textSummary.append(String.format("%n%nstatistics:%nmedium size=%5d\tsigma=%5d\tmax size=%5d\tmin size=%5d",
				(long) mittlereSchwarmGroesse, (long) Math.sqrt(varSchwarmGroesse), (long) maxSchwarmGroesse,
				(long) minSchwarmGroesse));

		textSummary.append(
				String.format("%nmedium abs=%5d\tsigma=%5d\tmax abs=%5d\tmin abs=%5d", (long) mittlererSchwarmBetrag,
						(long) Math.sqrt(varSchwarmBetrag), (long) maxSchwarmBetrag, (long) minSchwarmBetrag));

		if (chiSquareCritical999 > 0)
			textSummary.append(String.format(
					"%nchi test value: %10.5f" + "\tchi critical at 99.9%%: %10.5f" + "\tchi critical at 99%%: %10.5f"
							+ "\tchi critical at 97.5%%: %10.5f" + "\tchi critical at 95%%: %10.5f"
							+ "\tchi critical at 90%%: %10.5f",
					chiSquareActual, chiSquareCritical999, chiSquareCritical990, chiSquareCritical975,
					chiSquareCritical950, chiSquareCritical900));
		else
			textSummary.append(String.format(
					"%nchi test\t%10.5f\tchi square distribution not available: copy ./commons-math3-3.6.1.jar to classpath",
					chiSquareActual));

		return textSummary.toString();

	}

	public void writeNodeHistoryToLog(Logging historyLog, int nodeIndex) {
		if (historyLog == null)
			return;
		historyLog.writeLog(String.format(
				"%n%n" + "Start" + "%1$c%2$s" + "%1$cEnd" + "%1$c%3$s" + "%1$cInstance" + "%1$c%7$08X" + "%1$cMethod"
						+ "%1$c%4$s" + "%1$cMsmts" + "%1$c%5$d" + "%1$cIterations" + "%1$c%6$d",
				'\t', startTime, endTime, methodSelected, measurements, iterations, nodeIndex));
		historyLog.writeLog("\n" + Globals.toCSV('\t'));

		String hdr = String.format("%n" + "%1$c%2$s" + "%1$c%3$s" + "%4$s", '\t', "msmt", "iter",
				sourceFlame.getCSVHeader('\t'));

		for (int i = 0; i < measurements && flameHistories != null && i < flameHistories.length
				&& flameHistories[i] != null && flameHistories[i].length > 0 && flameHistories[i][0] != null; i++) {

			historyLog.setHeader(hdr, true);
			String fmtStr = "%n" + "%1$c%2$d" + "%1$c%3$d" + "%4$s";
			historyLog.writeLog(String.format(fmtStr.toString(), '\t', i, -1, sourceFlame.toCSV('\t')));

			for (int j = 0; j < iterations && flameHistories[i][j] != null; j++) {
				fmtStr = "%n" + "%1$c%2$d" + "%1$c%3$d" + "%4$s";
				historyLog.writeLog(String.format(fmtStr, '\t', i, j, flameHistories[i][j].toCSV('\t')));

			}
		}
	}

	public void writeNodeToLog(Logging batchLog, int nodeIndex) {
		batchLog.writeLog(String.format("%s\t%08X%n", toCSV('\t'), nodeIndex));
	}

}
