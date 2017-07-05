package de.bzus.graph3d;

import java.util.Vector;

import de.bzus.flame.common.Globals;
import de.bzus.flame.interfaces.IFResultsList;
import de.bzus.flame.process.EVFlame;
import de.bzus.flame.process.Flame;
import de.bzus.flame.process.PauliFlame;

/**
 * @author Werner Siegfried Genreith
 */
public abstract class Provider3D implements Runnable {
	private static Vector<Provider3D> providerList = new Vector<Provider3D>();

	public static Vector<Provider3D> getInstances() {
		return providerList;
	}
	
	public static Provider3D getMethodByIndex(int imethod) {
		if (imethod < providerList.size())
			return providerList.get(imethod);
		else
			return null;

	}

	protected DarstellungsRaum darstellungsRaum = null;
	public String description = this.getClass().getSimpleName();
	protected int indexStep = -1;
	private Object[] inList = null;
	protected boolean isRestarted = false;
	protected boolean isRunFinished = false;
	protected boolean isStepping = false;
	protected int iStep = 0;

	public String name = "Provider3D";

	protected Pfad[] pfade = null;

	protected IFResultsList resultList = null;

	protected int rIndex = 0; // running Index

	protected Thread tProvider = null;
	protected boolean tStopped = false;

	private xyzGraph xyzG = null;
	
	public Provider3D() {
		super();
		providerList.addElement(this);
	}

	public void beforeStart() {
		
	}
	public void beforePath(){
		
	}
	public void cleanup() {
		
	}

	private void clear() {
		if (pfade != null) {
			for (int i = 0; i < pfade.length; i++)
				pfade[i].clearAll();
			this.darstellungsRaum.add(pfade);
		}
	}

	public int getIndexStep() {
		return indexStep;
	}

	protected int getParticles(Flame evs) {
		return evs.getStateDimension();
	}

	public int getRIndex() {
		return rIndex;
	}

	protected void initVars() {
		indexStep = -1;
		inList = null;
		isRestarted = false;
		isRunFinished = false;
		isStepping = false;
		iStep = 0;
		pfade = null;
		tStopped = false;
		rIndex = 0; // running Index
	}

	public boolean isCompatibleWith(Class<? extends Flame> flameClass) {
		return true;
	}

	public boolean isCompatibleWith(Flame flameInstance) {
		return isCompatibleWith(flameInstance.getClass());
	}

	public boolean isCompatibleWith(String flameType) {
		if (flameType.equals(Globals.PauliModel))
			return isCompatibleWith(PauliFlame.class);
		else if (flameType.equals(Globals.EVModel))
			return isCompatibleWith(EVFlame.class);
		else
			return false;
	}

	public synchronized void restart() {
		isRestarted = true;
		isRunFinished = false;
		clear();
	}

	public synchronized void resume() {
		isStepping = false;
	}

	@Override
	public void run() {
		
		beforeStart();
		
		while (!tStopped) {
			try {
				indexStep = 0;
				Flame[] history = resultList.getHistory(rIndex);
				Flame evs = history[indexStep];
				int nParticles = getParticles(evs);
				pfade = new Pfad[nParticles]; // je Rang wird ein Pfad
												// angelegt

				for (int pfadNo = 0; pfadNo < pfade.length; pfadNo++) {
					pfade[pfadNo] = new Pfad();
					Punkt punkt = transform3D(evs, pfadNo, indexStep);
					darstellungsRaum.setExcite(evs.excitement(pfadNo), evs.toString(), punkt);
					pfade[pfadNo].add(punkt);
					darstellungsRaum.setHasChanged(true, pfade[pfadNo]);
				}
				darstellungsRaum.clearAll();
				darstellungsRaum.add(pfade);

				beforePath();

				for (indexStep = 1; indexStep < history.length && history[indexStep] != null; indexStep++) {
					if (isRestarted) {
						break;
					}
					evs = history[indexStep];
					for (int pfadNo = 0; pfadNo < pfade.length; pfadNo++) {

						if (tStopped)
							return;

						Punkt punkt = transform3D(evs, pfadNo, indexStep);
						darstellungsRaum.setExcite(evs.excitement(pfadNo), evs.toString(), punkt);
						pfade[pfadNo].add(punkt);
						darstellungsRaum.setHasChanged(true, pfade[pfadNo]);
					}
					if (xyzG != null)
						xyzG.repaint();
					Thread.sleep(darstellungsRaum.getOptions().getInterval());
					while (isStepping && iStep <= indexStep)
						Thread.sleep(200);
				}
				
				// indexStep = -1;
				iStep = 1;
				isRunFinished = true;
				if (isRestarted)
					isRestarted = false;
				else {
					while (isRunFinished) {
						Thread.sleep(500);
					}
					isRestarted = false;
				}
			} catch (InterruptedException e) {
				return;
			}
		}
		cleanup();
	}

	public void setCallingFrame(xyzGraph xyzG) {
		this.xyzG = xyzG;
	}

	public void setDarstellungsRaum(DarstellungsRaum darstellungsRaum) {
		this.darstellungsRaum = darstellungsRaum;
	}

	public void setIndexStep(int indexStep) {
		this.indexStep = indexStep;
	}

	public void setResultList(IFResultsList evsHistories) {
		this.resultList = evsHistories;
	}

	public synchronized void startFirst() {
		rIndex = 0;
		isRestarted = true;
		clear();
		isRunFinished = false;
	}

	public synchronized void startLast() {
		rIndex = resultList.getHistories().length - 1;
		if (resultList.getHistory(rIndex) == null)
			rIndex = 0;
		isRestarted = true;
		clear();
		isRunFinished = false;
	}

	public synchronized void startNext() {
		rIndex++;
		if (rIndex >= resultList.getHistories().length || resultList.getHistory(rIndex) == null)
			rIndex = 0;
		isRestarted = true;
		clear();
		isRunFinished = false;
	}

	public synchronized void startPrev() {
		rIndex--;
		if (rIndex < 0)
			rIndex = resultList.getHistories().length - 1;
		if (resultList.getHistory(rIndex) == null)
			rIndex = 0;
		isRestarted = true;
		clear();
		isRunFinished = false;
	}

	/**
	 * holt Beobachtung aus erstem Eintrag in den Resultaten initiiert inList
	 * mit erstem Lauf BNList aus Results und startet den Thread Falls Thread
	 * bereits läuft, setzt die Methode die Thread-Parameter auf den ersten Lauf
	 * zurück
	 */
	public void startProvider() {
		initVars();
		if (tProvider == null || !tProvider.isAlive()) {
			if (resultList == null || darstellungsRaum == null)
				return;

			// rIndex = 0;
			this.inList = resultList.getHistory(rIndex);
			if (inList == null)
				return;

			tProvider = new Thread(this);
			tProvider.setName("Provider Thread " + this.getClass().getName());
			tProvider.start();
		} else {
			startFirst();
		}
	}

	public synchronized void step() {
		if (isStepping)
			iStep++;
		else {
			if (indexStep < 0) {
				startProvider();
				iStep = 1;
			} else
				iStep = indexStep + 1;
			isStepping = true;
		}
	}

	public void stopProvider() {
		tStopped = true;
		try {
			Thread.sleep(200);
			if (tProvider != null) {
				tProvider.interrupt();
				tProvider.join();
				tProvider = null;
			}
		} catch (InterruptedException e) {
			return;
		}
	}

	public abstract Punkt transform3D(Flame evSchwarm, int k, int indexStep);

}
