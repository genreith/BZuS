package de.bzus.flame.interfaces;

import de.bzus.flame.common.WSGComplex;

public interface IFHandleRecord {

	public long[][] getEvswmData();

	public String getFlameType();

	// public int getIterations();

	// public int getIterationsCompleted();

	// public String getMethodSelected();

	// public int getMsmts();

	// public int getMsmtsCompleted();

	public WSGComplex[][] getObservableData();

	public WSGComplex[] getPerspectiveData();

	public boolean isUseObservable();

	public void setParticleDimension(int pDim);
	public int getParticleDimension();

	public void setEvswmData(long[][] evswmData);

	public void setFlameType(String type);

	public void setHistoryOn(boolean historyOn);

	public void setIter(int iter);

	public void setMethodSelected(String methodSelected);

	public void setMsmts(int msmts);

	public void setnHistories(int nHistories);

	public void setObservableData(WSGComplex[][] observableData);

	public void setPerspectiveData(WSGComplex[] perspectiveData);

	public void setUseObservable(boolean useObservable);

	public int getRandomize();
	public int getRandMax();
	public int getRandMin();

	public void setRandomize(int randomize);

	public void setRandMin(int randMin);

	public void setRandMax(int randMax);

	public int getStateDimension();

	public void setStateDimension(int sDim);

}