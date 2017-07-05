package de.bzus.flame.common;

public class MsmtThreadPool {
	private static MsmtThreadPool mPool;

	public static MsmtThreadPool getInstance() {
		if (mPool == null)
			mPool = new MsmtThreadPool();
		return mPool;
	}

	private Runnable focusRunnable;
	private Thread focusThread;
	private Runnable[] msmtRun = new Runnable[Globals.noOfBatchFileThreads];

	private Thread[] msmtThread = new Thread[Globals.noOfBatchFileThreads];

	public Runnable getFocusRunnable() {
		return focusRunnable;
	}

	public Thread getFocusThread() {
		return focusThread;
	}

	public int getFocusThreadIndex() {
		for (int i = 0; i < msmtThread.length && msmtThread[i] != null && focusThread != null; i++)
			if (msmtThread[i].equals(focusThread))
				return i;
		return -1;
	}

	public synchronized boolean isRunning() {
		boolean isRunning = false;
		for (int i = 0; i < msmtThread.length; i++)
			isRunning |= isRunning(i);
		if (!isRunning)
			Logging.getErrorLog().showError("threadpool empty", -1);
		return isRunning;
	}

	public boolean isRunning(int i) {
		if (i < msmtThread.length && msmtThread[i] != null)
			return msmtThread[i].isAlive();
		else
			return false;
	}

	public synchronized void releaseFocusHandle(Runnable fRun) {
		if (getFocusRunnable() != null && getFocusRunnable().equals(fRun))
			setFocusByRunnable(null);
	}

	public synchronized void requestFocusHandle(Runnable fRun) {
		if (getFocusRunnable() == null && fRun != null)
			setFocusByRunnable(fRun);
	}

	public synchronized Thread requestThread(Runnable hdl) {
		Logging.getErrorLog().showError("thread requested for " + hdl.getClass().getSimpleName(), -1);
		for (int i = 0; i < msmtThread.length; i++)
			if (msmtThread[i] == null || !msmtThread[i].isAlive()) {
				msmtThread[i] = new Thread(hdl);
				msmtRun[i] = hdl;
				msmtThread[i].setName("PoolThread-" + i + "_" + hdl.getClass().getSimpleName());
				msmtThread[i].start();
				Logging.getErrorLog().showError(msmtThread[i].getName() + " started", -1);

				return msmtThread[i];
			}
		return null;
	}

	public void setFocusByIndex(int i) {
		if (i >= 0 && i < msmtThread.length) {
			this.focusThread = msmtThread[i];
			this.focusRunnable = msmtRun[i];
			Logging.getErrorLog().showError(msmtThread[i].getName() + " is focusThread", -1);

		} else {
			this.focusThread = null;
			this.focusRunnable = null;
		}
	}

	public void setFocusByRunnable(Runnable fRun) {
		this.focusRunnable = fRun;
		if (fRun == null)
			this.focusThread = null;
		else
			for (int i = 0; i < msmtThread.length; i++)
				if (msmtRun[i] != null && msmtRun[i].equals(fRun)) {
					this.focusThread = msmtThread[i];
					this.focusRunnable = msmtRun[i];
					Logging.getErrorLog().showError(msmtThread[i].getName() + " is focusThread", -1);
				}
	}

	public int size() {
		return msmtThread.length;
	}
}
