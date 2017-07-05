package de.bzus.flame.ui;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.bzus.flame.common.Globals;
import de.bzus.flame.common.Logging;
import de.bzus.flame.common.MsmtThreadPool;
import de.bzus.flame.common.WSGComplex;
import de.bzus.flame.common.WSGRandom;
import de.bzus.flame.exceptions.InvalidTermException;
import de.bzus.flame.interfaces.IFHandleRecord;
import de.bzus.flame.process.Referenzmethode;
import de.bzus.flame.process.RunMeasurementSample;

/**
 * @author Werner Siegfried Genreith
 *
 *         The class handles batch XML files in online and in command line mode.
 * 
 */
public class RunBatch implements Runnable, IFHandleRecord {

	private class HandleRecord implements Runnable, IFHandleRecord {

		private long[][] evswmData;
		private String flameType;
		private int iter;
		private String methodSelected = null;
		private int msmts;
		private int nHistories;
		private WSGComplex[][] observableData;
		private WSGComplex[] perspectiveData;

		private boolean useObservable = false;
		private int stateDimension;
		private int particleDimension;

		@Override
		public long[][] getEvswmData() {
			return evswmData;
		}

		@Override
		public String getFlameType() {
			return flameType;
		}

		@Override
		public WSGComplex[][] getObservableData() {
			return observableData;
		}

		@Override
		public WSGComplex[] getPerspectiveData() {
			return perspectiveData;
		}

		@Override
		public boolean isUseObservable() {
			return useObservable;
		}

		private boolean checkParsedData() {
			if (!flameType.equalsIgnoreCase(Globals.EVModel) && !flameType.equalsIgnoreCase(Globals.PauliModel)) {
				errorLog.showError("invalid flame type " + flameType, 2);
				return false;
			}

			if (Referenzmethode.getMethodByName(methodSelected) == null) {
				errorLog.showError("invalid method " + methodSelected, 2);
				return false;
			}

			if (flameType.equals(Globals.PauliModel)) {
				if (stateDimension == 0)
					stateDimension = (int) Math.round(Math.sqrt(particleDimension));
				else if (particleDimension == 0)
					particleDimension = stateDimension * stateDimension;
			} else {
				if (stateDimension == 0)
					stateDimension = particleDimension;
				else if (particleDimension == 0)
					particleDimension = stateDimension;
			}
			
			if (iter < 0 || msmts <= 0 || stateDimension < 2) {
				errorLog.showError(
						String.format("invalid parameters iter=%d; msmts=%d; dim=%d", iter, msmts, stateDimension), 2);
				return false;
			}

			if (flameType.equals(Globals.PauliModel)) {
				if (perspectiveData == null || perspectiveData.length != getStateDimension()
						|| perspectiveData[0] == null) {
					errorLog.showError("invalid perspective data", 2);
					return false;
				}

				if (useObservable && (observableData == null || observableData.length != getStateDimension()
						|| observableData[0].length != observableData.length || observableData[0][0] == null)) {
					errorLog.showError("invalid observable data", 2);
					return false;
				}
			}

			if (evswmData == null || evswmData.length != getParticleDimension() || evswmData[0].length != 4) {
				errorLog.showError("invalid flame data", 2);
				return false;
			} else {
				for (int i = 0; i < evswmData.length; i++)
					for (int j = 0; j < 4; j++)
						if (evswmData[i][j] < 0) {
							errorLog.showError("invalid flame data", 2);
							return false;
						}
			}

			return true;
		}

		@Override
		public void run() {
			Logging.getErrorLog().showError("HandleSample thread started", -1);

			Node actualNode = getNextMsmtNode();
			while (actualNode != null && !RunMeasurementSample.isStopped) {
				Logging.getErrorLog().showError("XML node " + actualNode.hashCode() + " loaded", -1);

				setHRecData();
				if (!parseMeasurements(actualNode, this))
					continue;
				;

				if (!checkParsedData()) {
					actualNode = getNextMsmtNode();
					continue;
				}

				try {
					if (randomize > 0) {
						increaseRandScheduled(randomize);
						Logging.getErrorLog().showError(
								"random node " + actualNode.hashCode() + " starting " + randomize + "instances", -1);

						WSGRandom wsgm = WSGRandom.getInstance(Thread.currentThread());

						int evsDataLen = evswmData.length * evswmData[0].length;

						for (int iRand = 0; iRand < randomize && !RunMeasurementSample.isStopped; iRand++) {
							long swarmSize = (long) randMin + (randMax > randMin ? wsgm.nextInt(randMax - randMin) : 0);

							for (int i = 0; i < evswmData.length; i++)
								for (int j = 0; j < evswmData[0].length; j++)
									evswmData[i][j] = 0;

							for (int i = 0; i < swarmSize; i++) {
								int rand = wsgm.nextInt(evsDataLen);
								int pIndex = rand / 4;
								int iIndex = rand % 4;
								evswmData[pIndex][iIndex]++;
							}

							Logging.getErrorLog().showError("measurement sample random(" + iRand + ") started for node "
									+ actualNode.hashCode(), -1);

							new RunMeasurementSample(this, evswmData, flameType, observableData, perspectiveData,
									useObservable, methodSelected, msmts, iter, nHistories, batchLog, historyLog)
											.handleSample();

							increaseRandCompleted(1);

						}

					} else {
						Logging.getErrorLog().showError("measurement sample started for node " + actualNode.hashCode(),
								-1);
						new RunMeasurementSample(this, evswmData, flameType, observableData, perspectiveData,
								useObservable, methodSelected, msmts, iter, nHistories, batchLog, historyLog)
										.handleSample();
					}
				} catch (InvalidTermException e) {
					errorLog.showError(e.getMessage(), 2);
					continue;
				} catch (InterruptedException e) {
					errorLog.showError(e.toString(), 1);
					continue;
				}

				actualNode = getNextMsmtNode();
			}
			Logging.getErrorLog().showError("HandleSample thread ended", -1);

		}

		@Override
		public void setStateDimension(int dim) {
			this.stateDimension = dim;
		}

		@Override
		public void setEvswmData(long[][] evswmData) {
			this.evswmData = evswmData;
		}

		@Override
		public void setFlameType(String type) {
			this.flameType = type;
		}

		@Override
		public void setHistoryOn(boolean historyOn) {
			RunBatch.this.historyOn = historyOn;
		}

		private void setHRecData() {
			if (RunBatch.this.evswmData != null) {
				this.evswmData = new long[RunBatch.this.evswmData.length][RunBatch.this.evswmData[0].length];
				for (int i = 0; i < evswmData.length; i++)
					for (int j = 0; j < evswmData[0].length; j++)
						this.evswmData[i][j] = RunBatch.this.evswmData[i][j];
			} else {
				evswmData = null;
			}

			this.flameType = RunBatch.this.flameType;
			this.iter = RunBatch.this.iter;
			this.methodSelected = RunBatch.this.methodSelected;
			this.msmts = RunBatch.this.msmts;
			this.nHistories = RunBatch.this.nHistories;
			this.stateDimension = RunBatch.this.stateDimension;
			this.particleDimension = RunBatch.this.particleDimension;

			if (RunBatch.this.observableData != null) {
				this.observableData = new WSGComplex[RunBatch.this.observableData.length][RunBatch.this.observableData[0].length];
				for (int i = 0; i < observableData.length; i++)
					for (int j = 0; j < observableData[0].length; j++)
						this.observableData[i][j] = RunBatch.this.observableData[i][j];
			}

			if (RunBatch.this.perspectiveData != null) {
				this.perspectiveData = new WSGComplex[RunBatch.this.perspectiveData.length];
				for (int i = 0; i < perspectiveData.length; i++)
					this.perspectiveData[i] = RunBatch.this.perspectiveData[i];
			}

			this.useObservable = RunBatch.this.useObservable;
		}

		@Override
		public void setIter(int iter) {
			this.iter = iter;
		}

		@Override
		public void setMethodSelected(String methodSelected) {
			this.methodSelected = methodSelected;
		}

		@Override
		public void setMsmts(int msmts) {
			this.msmts = msmts;
		}

		@Override
		public void setnHistories(int nHistories) {
			this.nHistories = nHistories;
		}

		@Override
		public void setObservableData(WSGComplex[][] observableData) {
			this.observableData = observableData;
		}

		@Override
		public void setPerspectiveData(WSGComplex[] perspectiveData) {
			this.perspectiveData = perspectiveData;
		}

		@Override
		public void setUseObservable(boolean useObservable) {
			this.useObservable = useObservable;
		}

		@Override
		public void setRandomize(int randomize) {
			this.randomize = randomize;
		}

		@Override
		public void setRandMin(int randMin) {
			this.randMin = randMin;
		}

		@Override
		public void setRandMax(int randMax) {
			this.randMax = randMax;
		}

		private int randomize;
		private int randMax;
		private int randMin;

		@Override
		public int getRandomize() {
			return randomize;
		}

		@Override
		public int getStateDimension() {
			return stateDimension;
		}

		@Override
		public int getRandMax() {
			return randMax;
		}

		@Override
		public int getRandMin() {
			return randMin;
		}

		@Override
		public void setParticleDimension(int pDim) {
			particleDimension = pDim;
		}

		@Override
		public int getParticleDimension() {
			return particleDimension;
		}

	}

	private File batch = null;
	private Logging batchLog;

	private Logging errorLog;
	private long[][] evswmData;
	private String flameType;

	// private HandleRecord focusHRec = null;
	private Logging historyLog;

	private boolean historyOn = false;

	private int iter;
	private String methodSelected = null;
	private MsmtThreadPool mPool = MsmtThreadPool.getInstance();
	private int msmtNodeIndex = 0;
	private NodeList msmtNodes;

	private int msmts;

	// private Thread[] msmtThreads;
	private int nHistories;
	private WSGComplex[][] observableData;

	private WSGComplex[] perspectiveData;

	private boolean useObservable = false;

	public RunBatch(File batchFile) {
		batch = batchFile;
	}

	public Logging getBatchLog() {
		return batchLog;
	}

	public int getStateDimension() {
		if (mPool.getFocusRunnable() != null
				&& mPool.getFocusRunnable() instanceof RunMeasurementSample.HandleIterations) {
			RunMeasurementSample.HandleIterations focusHRec = (RunMeasurementSample.HandleIterations) mPool
					.getFocusRunnable();
			return focusHRec.getDim();
		} else
			return stateDimension;
	}

	public Logging getErrorLog() {
		return errorLog;
	}

	@Override
	public long[][] getEvswmData() {
		if (mPool.getFocusRunnable() != null
				&& mPool.getFocusRunnable() instanceof RunMeasurementSample.HandleIterations) {
			RunMeasurementSample.HandleIterations focusHRec = (RunMeasurementSample.HandleIterations) mPool
					.getFocusRunnable();
			return focusHRec.getEvswmData();
		} else
			return evswmData;
	}

	@Override
	public String getFlameType() {
		if (mPool.getFocusRunnable() != null
				&& mPool.getFocusRunnable() instanceof RunMeasurementSample.HandleIterations) {
			RunMeasurementSample.HandleIterations focusHRec = (RunMeasurementSample.HandleIterations) mPool
					.getFocusRunnable();
			return focusHRec.getFlameType();
		} else
			return flameType;

	}

	public Logging getHistoryLog() {
		return historyLog;
	}

	public int getIterations() {
		if (mPool.getFocusRunnable() != null
				&& mPool.getFocusRunnable() instanceof RunMeasurementSample.HandleIterations) {
			RunMeasurementSample.HandleIterations focusHRec = (RunMeasurementSample.HandleIterations) mPool
					.getFocusRunnable();
			return focusHRec.getIterations();
		} else
			return iter;
	}

	public int getIterationsCompleted() {
		if (mPool.getFocusRunnable() != null
				&& mPool.getFocusRunnable() instanceof RunMeasurementSample.HandleIterations) {
			RunMeasurementSample.HandleIterations focusHRec = (RunMeasurementSample.HandleIterations) mPool
					.getFocusRunnable();
			return focusHRec.getIterationsCompleted();
		} else
			return 0;
	}

	public String getMethodSelected() {
		if (mPool.getFocusRunnable() != null
				&& mPool.getFocusRunnable() instanceof RunMeasurementSample.HandleIterations) {
			RunMeasurementSample.HandleIterations focusHRec = (RunMeasurementSample.HandleIterations) mPool
					.getFocusRunnable();
			return focusHRec.getMethodSelected();
		} else
			return methodSelected;
	}

	public int getMsmts() {
		int m;
		if (mPool.getFocusRunnable() != null
				&& mPool.getFocusRunnable() instanceof RunMeasurementSample.HandleIterations) {
			RunMeasurementSample.HandleIterations focusHRec = (RunMeasurementSample.HandleIterations) mPool
					.getFocusRunnable();
			m = focusHRec.getMsmts();
		} else {
			m = msmts;
		}
		return m;
	}

	public int getMsmtsCompleted() {
		int m;
		if (mPool.getFocusRunnable() != null
				&& mPool.getFocusRunnable() instanceof RunMeasurementSample.HandleIterations) {
			RunMeasurementSample.HandleIterations focusHRec = (RunMeasurementSample.HandleIterations) mPool
					.getFocusRunnable();
			m = focusHRec.getMsmtsCompleted();
		} else {
			m = 0;
		}
		return m;
	}

	public synchronized Node getNextMsmtNode() {
		if (msmtNodeIndex < msmtNodes.getLength()) {
			return msmtNodes.item(msmtNodeIndex++);
		} else {
			return null;
		}
	}

	public int getnHistories() {
		return nHistories;
	}

	private int iRandCompleted;
	private int iRandScheduled;
	private int stateDimension;
	private int particleDimension;

	private synchronized void increaseRandScheduled(int delta) {
		iRandScheduled += delta;
	}

	private synchronized void increaseRandCompleted(int delta) {
		iRandCompleted += delta;
	}

	public int getRandomize() {
		if (mPool.getFocusRunnable() != null
				&& mPool.getFocusRunnable() instanceof RunMeasurementSample.HandleIterations) {
			RunMeasurementSample.HandleIterations focusHRec = (RunMeasurementSample.HandleIterations) mPool
					.getFocusRunnable();
			IFHandleRecord hr = focusHRec.getHandleRecord();
			return hr != null ? hr.getRandomize() : 0;
		} else
			return 0;
	}

	public int getRandMax() {
		if (mPool.getFocusRunnable() != null
				&& mPool.getFocusRunnable() instanceof RunMeasurementSample.HandleIterations) {
			RunMeasurementSample.HandleIterations focusHRec = (RunMeasurementSample.HandleIterations) mPool
					.getFocusRunnable();
			IFHandleRecord hr = focusHRec.getHandleRecord();
			return hr != null ? hr.getRandMax() : 0;
		} else
			return 0;
	}

	public int getRandMin() {
		if (mPool.getFocusRunnable() != null
				&& mPool.getFocusRunnable() instanceof RunMeasurementSample.HandleIterations) {
			RunMeasurementSample.HandleIterations focusHRec = (RunMeasurementSample.HandleIterations) mPool
					.getFocusRunnable();
			IFHandleRecord hr = focusHRec.getHandleRecord();
			return hr != null ? hr.getRandMin() : 0;
		} else
			return 0;
	}

	public int getNodesCompleted() {
		return msmtNodeIndex + iRandCompleted;
	}

	public int getNodesCount() {
		if (msmtNodes == null)
			return 0;
		else
			return msmtNodes.getLength() + iRandScheduled;

	}

	@Override
	public WSGComplex[][] getObservableData() {
		if (mPool.getFocusRunnable() != null && mPool.getFocusRunnable().getClass().equals(HandleRecord.class)) {
			HandleRecord focusHRec = (HandleRecord) mPool.getFocusRunnable();
			return focusHRec.getObservableData();
		} else
			return observableData;
	}

	@Override
	public WSGComplex[] getPerspectiveData() {
		if (mPool.getFocusRunnable() != null && mPool.getFocusRunnable().getClass().equals(HandleRecord.class)) {
			HandleRecord focusHRec = (HandleRecord) mPool.getFocusRunnable();
			return focusHRec.getPerspectiveData();
		} else
			return perspectiveData;
	}

	public boolean isHistoryOn() {
		return historyOn;
	}

	@Override
	public boolean isUseObservable() {
		if (mPool.getFocusRunnable() != null && mPool.getFocusRunnable().getClass().equals(HandleRecord.class)) {
			HandleRecord focusHRec = (HandleRecord) mPool.getFocusRunnable();
			return focusHRec.useObservable;
		} else
			return useObservable;
	}

	private void parse(Document doc) throws InvalidTermException {
		// optional, but recommended
		// read this -
		// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work

		doc.getDocumentElement().normalize();
		Element rootElement = doc.getDocumentElement();

		if (!rootElement.getNodeName().equals("Measurements")) {
			throw new InvalidTermException("No Measurements Batch File");
		}
		Node sourceNode = doc.getFirstChild();

		parseMeasurements(sourceNode, this);
	}

	private boolean parseMeasurements(Node sourceNode, IFHandleRecord recordHandler) {

		if (sourceNode.getNodeType() != Node.ELEMENT_NODE)
			return false;

		NamedNodeMap nm = sourceNode.getAttributes();
		for (int j = 0; j < nm.getLength(); j++) {
			Node nn = nm.item(j);
			if (nn.getNodeName().equals("Msmts")) {
				recordHandler.setMsmts(Integer.valueOf(nn.getTextContent()));
			} else if (nn.getNodeName().equals("Iterations")) {
				recordHandler.setIter(Integer.valueOf(nn.getTextContent()));
			} else if (nn.getNodeName().equals("Method")) {
				recordHandler.setMethodSelected(nn.getTextContent());
			} else if (nn.getNodeName().equals("History")) {
				recordHandler.setHistoryOn(Boolean.valueOf(nn.getTextContent()));
			} else if (nn.getNodeName().equals("nHistories")) {
				recordHandler.setnHistories(Integer.valueOf(nn.getTextContent()));
			} else {
				errorLog.showError("invalid attribute : " + nn.getNodeName() + " in " + sourceNode.getNodeName(), 1);
			}
		}

		NodeList cns = sourceNode.getChildNodes();
		for (int i = 0; i < cns.getLength(); i++) {
			Node tNode = cns.item(i);
			if (tNode.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if (tNode.getNodeName().equals("Observable")) {
				nm = tNode.getAttributes();
				for (int j = 0; j < nm.getLength(); j++) {
					Node nn = nm.item(j);
					if (nn.getNodeName().equals("Dimension")) {
						recordHandler.setStateDimension(Integer.valueOf(nn.getTextContent()));
					} else if (nn.getNodeName().equals("Use")) {
						recordHandler.setUseObservable(Boolean.valueOf(nn.getTextContent()));
					} else {
						errorLog.showError("invalid attribute : " + nn.getNodeName() + " in " + tNode.getNodeName(), 1);
					}
				}
				if (recordHandler.isUseObservable()) {
					int dim = recordHandler.getStateDimension();
					NodeList nRows = tNode.getChildNodes();
					if (recordHandler.getObservableData() == null) {
						recordHandler.setObservableData(new WSGComplex[dim][dim]);
					}
					int iJ = 0;
					int iK = 0;
					for (int j = 0; j < nRows.getLength() && iJ < recordHandler.getObservableData().length; j++) {
						if (nRows.item(j).getNodeType() != Node.ELEMENT_NODE)
							continue;
						if (!nRows.item(j).getNodeName().equals("Row"))
							continue;
						NodeList nCols = nRows.item(j).getChildNodes();
						iK = 0;
						for (int k = 0; k < nCols.getLength()
								&& iK < recordHandler.getObservableData()[0].length; k++) {
							if (nCols.item(k).getNodeType() != Node.ELEMENT_NODE)
								continue;
							if (!nCols.item(k).getNodeName().equals("Value"))
								continue;
							recordHandler.getObservableData()[iJ][iK++] = WSGComplex
									.valueOf(nCols.item(k).getTextContent());
						}
						iJ++;
					}
					if (iJ != recordHandler.getObservableData().length)
						errorLog.showError(String.format("data dimension %d does not match XML dimension %d in %s",
								recordHandler.getObservableData().length, iJ, tNode.getNodeName()), 2);
					if (iK != recordHandler.getObservableData()[0].length)
						errorLog.showError(String.format("col dimension %d does not match XML columns %d in %s",
								recordHandler.getObservableData()[0].length, iK, tNode.getNodeName()), 2);
				}
			} else if (tNode.getNodeName().equals("Flame")) {

				nm = tNode.getAttributes();
				for (int j = 0; j < nm.getLength(); j++) {
					Node nn = nm.item(j);
					if (nn.getNodeName().equals("Dimension")) {
						recordHandler.setStateDimension(Integer.valueOf(nn.getTextContent()));
					} else if (nn.getNodeName().equals("Type")) {
						recordHandler.setFlameType(String.valueOf(nn.getTextContent()));
					} else if (nn.getNodeName().equals("Randomize")) {
						recordHandler.setRandomize(Integer.valueOf(nn.getTextContent()));
					} else if (nn.getNodeName().equals("RandMin")) {
						recordHandler.setRandMin(Integer.valueOf(nn.getTextContent()));
					} else if (nn.getNodeName().equals("RandMax")) {
						recordHandler.setRandMax(Integer.valueOf(nn.getTextContent()));
					} else {
						errorLog.showError("invalid attribute : " + nn.getNodeName() + " in " + tNode.getNodeName(), 1);
					}
				}
				
				if (recordHandler.getFlameType().equals(Globals.PauliModel)) {
						recordHandler.setParticleDimension(recordHandler.getStateDimension()* recordHandler.getStateDimension());
				} else {
					recordHandler.setParticleDimension(recordHandler.getStateDimension());
				}

				NodeList nRows = tNode.getChildNodes();
				if (recordHandler.getEvswmData() == null) {
					recordHandler.setEvswmData(new long[recordHandler.getParticleDimension()][4]);
				}

				int iJ = 0;
				int iK = 0;
				for (int j = 0; j < nRows.getLength(); j++) {
					if (nRows.item(j).getNodeType() != Node.ELEMENT_NODE || !nRows.item(j).getNodeName().equals("Row"))
						continue;
					NodeList nCols = nRows.item(j).getChildNodes();
					iK = 0;
					for (int k = 0; k < nCols.getLength(); k++) {
						if (nCols.item(k).getNodeType() != Node.ELEMENT_NODE
								|| !nCols.item(k).getNodeName().equals("Value"))
							continue;
						recordHandler.getEvswmData()[iJ][iK++] = Long.valueOf(nCols.item(k).getTextContent());
					}
					iJ++;
				}
				if (recordHandler.getRandomize() == 0 && iJ != recordHandler.getEvswmData().length)
					errorLog.showError(String.format("data dimension %d does not match XML dimension %d in %s",
							recordHandler.getEvswmData().length, iJ, tNode.getNodeName()), 2);
				if (recordHandler.getRandomize() == 0 && iK != recordHandler.getEvswmData()[0].length)
					errorLog.showError(String.format("col dimension %d does not match XML columns %d in %s",
							recordHandler.getEvswmData()[0].length, iK, tNode.getNodeName()), 2);

			} else if (tNode.getNodeName().equals("Perspective")) {
				nm = tNode.getAttributes();
				for (int j = 0; j < nm.getLength(); j++) {
					Node nn = nm.item(j);
					if (nn.getNodeType() != Node.ELEMENT_NODE || !nn.getNodeName().equals("Dimension"))
						recordHandler.setStateDimension(Integer.valueOf(nn.getTextContent()));
				}
				NodeList nRows = tNode.getChildNodes();
				if (recordHandler.getPerspectiveData() == null) {
					recordHandler.setPerspectiveData(new WSGComplex[recordHandler.getStateDimension()]);
				}
				int iJ = 0;
				for (int j = 0; j < nRows.getLength(); j++) {
					if (nRows.item(j).getNodeType() != Node.ELEMENT_NODE
							|| !nRows.item(j).getNodeName().equals("Value"))
						continue;
					try {
						recordHandler.getPerspectiveData()[iJ++] = WSGComplex.valueOf(nRows.item(j).getTextContent());
					} catch (ArrayIndexOutOfBoundsException e) {
						Logging.getErrorLog()
								.showError(e.toString() + String.format(
										"hr.getPerspectiveData()[%d] = WSGComplex.valueOf(nRows.item(%d).getTextContent())",
										iJ, j), 2);
					}
				}
				if (iJ != recordHandler.getPerspectiveData().length)
					errorLog.showError(String.format("data dimension %d does not match XML dimension %d in %s",
							recordHandler.getPerspectiveData().length, iJ, tNode.getNodeName()), 2);

			}
		}
		return true;

	}

	@Override
	public void run() {

		mPool.setFocusByRunnable(null);

		errorLog = Logging.getErrorLog("Err_" + batch.getName(), true);
		errorLog.showError("RunBatch thread started", 0);

		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(batch);
			parse(doc);

			msmtNodes = doc.getElementsByTagName("Measurement");

			if (msmtNodes.getLength() < 1)
				return;

			batchLog = Logging.getBatchLog("Log_" + batch.getName(), true);

			if (historyOn)
				historyLog = Logging.getHistoryLog("His_" + batch.getName(), true);
			else if (historyLog != null) {
				historyLog.closeLog();
				historyLog = null;
			}

			batchLog.writeLog("\n" + Globals.toCSV('\t') + "\n");

			Thread t1 = new Thread(new HandleRecord());
			t1.setName("HandleNode_1");
			Thread t2 = new Thread(new HandleRecord());
			t2.setName("HandleNode_2");
			Thread t3 = new Thread(new HandleRecord());
			t3.setName("HandleNode_3");
			Thread t4 = new Thread(new HandleRecord());
			t4.setName("HandleNode_4");

			iRandCompleted = 0;
			iRandScheduled = 0;
			t1.start();
			t2.start();
			t3.start();
			t4.start();

			t1.join();
			t2.join();
			t3.join();
			t4.join();
			
			if (batchLog != null)
				batchLog.closeLog();
			if (historyLog != null)
				historyLog.closeLog();

		} catch (SAXException e) {
			MsgHandler.getMsgHdl().showError("Invalid XML : " + batch.toString(), 2);
		} catch (IOException e) {
			MsgHandler.getMsgHdl().showError("File not readable : " + batch.toString(), 2);
		} catch (ParserConfigurationException e) {
			MsgHandler.getMsgHdl().showError("Invalid XML : " + batch.toString(), 2);
		} catch (InterruptedException e) {
			MsgHandler.getMsgHdl().showError("Thread interrupted: " + e, 2);
		} catch (InvalidTermException e) {
			MsgHandler.getMsgHdl().showError(e.getMessage(), 2);
		}

		errorLog.showError("RunBatch thread ended", 0);
		errorLog.closeLog();

	}

	@Override
	public void setStateDimension(int dim) {
		this.stateDimension = dim;
	}

	@Override
	public void setEvswmData(long[][] evswmData) {
		this.evswmData = evswmData;
	}

	@Override
	public void setFlameType(String type) {
		flameType = type;
	}

	@Override
	public void setHistoryOn(boolean historyOn) {
		this.historyOn = historyOn;
	}

	@Override
	public void setIter(int iter) {
		this.iter = iter;
	}

	@Override
	public void setMethodSelected(String methodSelected) {
		this.methodSelected = methodSelected;
	}

	@Override
	public void setMsmts(int msmts) {
		this.msmts = msmts;
	}

	@Override
	public void setnHistories(int nHistories) {
		this.nHistories = nHistories;
	}

	@Override
	public void setObservableData(WSGComplex[][] observableData) {
		this.observableData = observableData;
	}

	@Override
	public void setPerspectiveData(WSGComplex[] perspectiveData) {
		this.perspectiveData = perspectiveData;
	}

	@Override
	public void setUseObservable(boolean useObservable) {
		this.useObservable = useObservable;
	}

	@Override
	public void setRandomize(int randomize) {
	}

	@Override
	public void setRandMin(int randMin) {
	}

	@Override
	public void setRandMax(int randMax) {
	}

	@Override
	public void setParticleDimension(int pDim) {
		particleDimension = pDim;
	}

	@Override
	public int getParticleDimension() {
		return particleDimension;
	}
}
