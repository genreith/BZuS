package de.bzus.flame.process;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import de.bzus.flame.common.Globals;
import de.bzus.flame.common.Logging;
import de.bzus.flame.common.MsmtThreadPool;
import de.bzus.flame.common.WSGComplex;
import de.bzus.flame.common.WSGMatrix;
import de.bzus.flame.exceptions.InvalidTermException;
import de.bzus.flame.interfaces.IFHandleRecord;

public class RunMeasurementSample {

	public class HandleIterations implements Runnable {

		private Flame flame;
		private int iterationsCompleted = 0;

		public HandleIterations(Flame flame) {
			this.flame = flame;
		}

		public int getDim() {
			return flame.getStateDimension();
		}

		public long[][] getEvswmData() {
			return flameData;
		}

		public String getFlameType() {
			return flameType;
		}

		public IFHandleRecord getHandleRecord() {
			return hRec;
		}

		public int getIterations() {
			return iter;
		}

		public int getIterationsCompleted() {
			return iterationsCompleted;
		}

		public String getMethodSelected() {
			return methodSelected.getText();
		}

		public int getMsmts() {
			return msmts;
		}

		public int getMsmtsCompleted() {
			return msmtsCompleted;
		}

		@Override
		public void run() {
			Logging.getErrorLog().showError("HandleIterations thread started", -1);

			int msmtIndex = getNextMsmtIndex();

			while (msmtIndex >= 0) {

				Flame wflame = flame.clone();

				for (iterationsCompleted = 0; iterationsCompleted < iter && wflame.size() < Globals.maxSwarmSize
						&& wflame.excitement() > Globals.minimalExcitement; iterationsCompleted++) {

					mPool.requestFocusHandle(this);

					while (isPaused) {
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							break;
						}
					}

					if (isStopped)
						break;

					methodSelected.selectNext(wflame);
					msmtHist.addFlameHistory(wflame, msmtIndex, iterationsCompleted);
				}

				msmtHist.increaseTargetIndexSum(iterationsCompleted);
				msmtHist.setMsmtResults(msmtIndex, wflame);

				msmtIndex = getNextMsmtIndex();
			}

			mPool.releaseFocusHandle(this);
			Logging.getErrorLog().showError("HandleIterations thread ended", -1);

		}

	}

	public class RequestResources implements ActionListener {
		Flame flame;

		public RequestResources(Flame f) {
			flame = f;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (threadCount < rThread.length) {
				HandleIterations hdlIt = new HandleIterations(flame);
				Thread hdlItThreadt = mPool.requestThread(hdlIt);
				if (hdlItThreadt != null) {
					addHrThread(hdlItThreadt);
				}
			}
		}

	}

	public static boolean isPaused = false;
	public static boolean isStopped = false;

	private Logging batchLog;
	private final long[][] flameData;
	private final String flameType;
	private Logging historyLog;
	private IFHandleRecord hRec;
	private final int iter;
	private final String methodName;
	private final Referenzmethode methodSelected;

	private MsmtThreadPool mPool;
	private MsmtHistory msmtHist;
	private final int msmts;
	private int msmtsCompleted = 0;
	private final int nHistories;
	private final WSGComplex[][] observableData;
	private final WSGComplex[] perspectiveData;
	private final boolean useObservable;
	private RequestResources requestRes;

	public RunMeasurementSample(IFHandleRecord handleRec, long[][] flameData, String fType, WSGComplex[][] obData,
			WSGComplex[] pData, boolean useObs, String method, int msmts, int iter, int nHist, Logging bLog,
			Logging hLog) {
		Logging.getErrorLog().showError("RunMeasurementSample(" + handleRec + "," + flameData + "," + fType + ","
				+ method + "," + msmts + "," + iter + "," + nHist + "," + bLog + "," + hLog + ")", -1);
		this.hRec = handleRec;
		this.flameData = new long[flameData.length][flameData[0].length];

		for (int i = 0; i < flameData.length; i++)
			for (int j = 0; j < flameData[0].length; j++)
				this.flameData[i][j] = flameData[i][j];

		this.flameType = fType;
		this.iter = iter;
		this.msmts = msmts;
		this.observableData = obData;
		this.perspectiveData = pData;
		this.useObservable = useObs;
		this.nHistories = nHist;
		this.mPool = MsmtThreadPool.getInstance();
		this.methodName = method;
		this.methodSelected = Referenzmethode.getMethodByName(method);
		this.batchLog = bLog;
		this.historyLog = hLog;
	}

	public MsmtHistory getMsmtHistory() {
		return msmtHist;
	}

	public int getMsmtsCompleted() {
		return msmtsCompleted;
	}

	public synchronized int getNextMsmtIndex() {
		int nextIndex = -1;
		if (msmtsCompleted < msmts)
			nextIndex = msmtsCompleted++;
		return nextIndex;
	}

	private int threadCount = 0;
	private Thread[] rThread;

	public void resetHrThread() {
		threadCount = 0;
		rThread = new Thread[mPool.size()];
	}

	public int addHrThread(Thread hdlItThreadt) {
		if (threadCount < rThread.length) {
			rThread[threadCount++] = hdlItThreadt;
			Logging.getErrorLog()
					.showError("thread " + hdlItThreadt.getName() + " added on position" + (threadCount - 1)
							+ " for flameData " + flameData.hashCode() + " by thread "
							+ Thread.currentThread().getName(), -1);
			return threadCount - 1;
		} else
			return -1;
	}

	public MsmtHistory handleSample() throws InvalidTermException, InterruptedException {
		Logging.getErrorLog().showError("handleSample started", -1);

		if (methodSelected == null)
			throw new InvalidTermException("method " + methodName + " unknown");

		Flame flame;

		if (flameType.equalsIgnoreCase(Globals.EVModel)) {
			flame = new EVFlame(flameData);
		} else if (flameType.equalsIgnoreCase(Globals.PauliModel)) {
			if (useObservable && observableData != null && perspectiveData != null) {
				flame = new PauliFlame(flameData, new WSGMatrix(perspectiveData), new WSGMatrix(observableData));
			} else {
				if (perspectiveData == null)
					flame = new PauliFlame(flameData);
				else
					flame = new PauliFlame(flameData, new WSGMatrix(perspectiveData));
			}
		} else {
			throw new InvalidTermException("flame model " + flameType + " unknown");
		}

		if (flame.getStateDimension() > 1 && flame.getParticleDimension() > 1) {

			if (!methodSelected.isCompatibleWith(flame))
				throw new InvalidTermException(
						"method " + methodSelected + " not suitable for " + flame.getClass().getSimpleName());

			msmtHist = new MsmtHistory(msmts, iter, methodSelected.getText(), nHistories, flame);

			msmtHist.setStartTime();
			msmtsCompleted = 0;

			mPool.setFocusByRunnable(null);
			if (flame.absQ() > 0)
				while (msmtsCompleted < msmts) {
					Logging.getErrorLog().showError("handleSample msmts " + msmtsCompleted + "/" + msmts + " completed",
							-1);

					HandleIterations hdlIt = new HandleIterations(flame);
					Thread hdlItThreadt = mPool.requestThread(hdlIt);
					while (hdlItThreadt == null) {
						Thread.sleep(100);
						hdlItThreadt = mPool.requestThread(hdlIt);
					}

					resetHrThread();

					addHrThread(hdlItThreadt);
					requestRes = new RequestResources(flame);
					Timer tmr = new Timer(100, requestRes);

					tmr.start();
					for (int i = 0; i < threadCount; i++) {
						if (rThread[i] != null) {
							rThread[i].join();
							tmr.stop();
							Logging.getErrorLog().showError("handleSample thread[" + i + "] ended: " + rThread[i], -1);
						}
					}
					Logging.getErrorLog().showError("handleSample msmts " + msmtsCompleted + "/" + msmts + " completed",
							-1);
				}
			else
				Logging.getErrorLog().showError("flame has zero norm", 1);

			Logging.getErrorLog().showError("handleSample ending", -2);
			try {
				Logging.getErrorLog().showError("handleSample msmtHist.setEndTime()", -2);
				msmtHist.setEndTime();

				Logging.getErrorLog().showError("handleSample batchLog.setHeader", -2);
				batchLog.setHeader(msmtHist.getCSVHeader('\t', useObservable) + "\tnodeHash\n");

				Logging.getErrorLog().showError("handleSample msmtHist.writeNodeToLog(" + batchLog + ","
						+ Thread.currentThread().hashCode() + ")", -2);
				msmtHist.writeNodeToLog(batchLog, Thread.currentThread().hashCode());

				Logging.getErrorLog().showError("handleSample msmtHist.writeNodeHistoryToLog(" + historyLog + ","
						+ Thread.currentThread().hashCode() + ")", -2);
				msmtHist.writeNodeHistoryToLog(historyLog, Thread.currentThread().hashCode());

				Logging.getErrorLog().showError("handleSample ended", -1);

			} catch (Exception e) {
				Logging.getErrorLog().showError(e.toString(), 2);
			}
			return msmtHist;

		} else {
			throw new InvalidTermException("flame dimension invalid");

		}

	}

}
