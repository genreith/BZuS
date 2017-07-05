package de.bzus.flame.ui;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
import de.bzus.flame.common.WSGMatrix;
import de.bzus.flame.common.WSGRandom;
import de.bzus.flame.exceptions.InvalidTermException;
import de.bzus.flame.interfaces.IFViewModel;
import de.bzus.flame.process.EVFlame;
import de.bzus.flame.process.Flame;
import de.bzus.flame.process.MsmtHistory;
import de.bzus.flame.process.PauliFlame;
import de.bzus.flame.process.Referenzmethode;
import de.bzus.flame.process.RunMeasurementSample;
import de.bzus.graph3d.Provider3D;
import de.bzus.graph3d.xyzGraph;

/**
 * @author Werner Siegfried Genreith
 * 
 *         The class manages the user interfaces to run any simulations one by
 *         one or automated by loading XML files in online mode Two XML formats
 *         are available: 1) representing one measurement sample (root node
 *         Measurement) 2) representing a set of measurement samples (root node
 *         Measurements with one or more sub nodes Measurement) The latter is
 *         also available in commandline batch mode by providing the file as a
 *         parameter when calling
 *
 */
public class RunFlame extends JFrame implements ActionListener, ListSelectionListener, ComponentListener {

	public abstract class DataModel {

		protected int dim;
		protected long[][] evswmData;
		protected String flameType;
		protected boolean historyOn = false;
		protected int iter;
		protected boolean logging = false;
		protected String methodSelected;
		protected int msmts;

		protected int nHistories;

		protected WSGComplex[][] observableData;

		protected WSGComplex[] perspectiveData;
		protected int randMax;

		protected int randMin;

		protected int randomize;

		protected boolean useObservable = false;

		public void cloneData(ImportXML xmlDataModel) {
			if (xmlDataModel == null)
				return;

			if (xmlDataModel.evswmData != null && xmlDataModel.evswmData[0] != null) {
				int rows = xmlDataModel.evswmData.length;
				int cols = xmlDataModel.evswmData[0].length;

				this.evswmData = new long[rows][cols];
				for (int i = 0; i < rows; i++)
					for (int j = 0; j < cols; j++)
						this.evswmData[i][j] = xmlDataModel.evswmData[i][j];
			}

			if (xmlDataModel.perspectiveData != null) {
				this.perspectiveData = new WSGComplex[xmlDataModel.perspectiveData.length];
				for (int i = 0; i < this.perspectiveData.length; i++)
					this.perspectiveData[i] = xmlDataModel.perspectiveData[i];
			}
			if (xmlDataModel.observableData != null && xmlDataModel.observableData[0] != null) {
				int rows = xmlDataModel.observableData.length;
				int cols = xmlDataModel.observableData[0].length;

				this.observableData = new WSGComplex[rows][cols];
				for (int i = 0; i < rows; i++)
					for (int j = 0; j < cols; j++)
						this.observableData[i][j] = xmlDataModel.observableData[i][j];
			}

			this.evswmData = xmlDataModel.evswmData;
			this.flameType = xmlDataModel.flameType;
			this.dim = xmlDataModel.dim;
			this.historyOn = xmlDataModel.historyOn;
			this.iter = xmlDataModel.iter;
			this.methodSelected = xmlDataModel.methodSelected;
			this.msmts = xmlDataModel.msmts;
			this.nHistories = xmlDataModel.nHistories;
		}

		public abstract DOMSource createDOMfromModel() throws ParserConfigurationException;

		public void exportModelToXML() {
			String fileName = "Measurement-"
					+ String.format("%1$ty%1$tm%td_%1$tH%1$tM%1$tS%1$tL", Calendar.getInstance()) + ".xml";
			File outFile = new File(Globals.xmlDir, fileName);
			outFile.getParentFile().mkdirs();

			try {
				JFileChooser fc = new JFileChooser(new File(Globals.xmlDir));
				fc.setSelectedFile(outFile);
				int returnVal = fc.showSaveDialog(frame);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					outFile = fc.getSelectedFile();
					StreamResult result = new StreamResult(outFile);
					TransformerFactory transformerFactory = TransformerFactory.newInstance();
					Transformer transformer = transformerFactory.newTransformer();

					DOMSource source = createDOMfromModel();
					transformer.transform(source, result);
					msgHdl.showError("Measurement saved : " + outFile.toString(), 0);
				} else {
					msgHdl.showError("save Measurement aborted by user", 1);
				}
			} catch (TransformerException tfe) {
				msgHdl.showError("Could not write Data : " + outFile.toString(), 2);
			} catch (ParserConfigurationException e) {
				msgHdl.showError("Invalid Data", 2);
			}

		}

		public long[][] getEvswmData() {
			return evswmData;
		}

		public abstract Flame getFlame();

		public int getIter() {
			return iter;
		}

		public String getMethodSelected() {
			return methodSelected;
		}

		public int getMsmts() {
			return msmts;
		}

		public int getnHistories() {
			return nHistories;
		}

		public WSGComplex[][] getObservableData() {
			return observableData;
		}

		public WSGComplex[] getPerspectiveData() {
			return perspectiveData;
		}

		protected abstract IFViewModel getViewModel();

		public boolean isHistoryOn() {
			return historyOn;
		}

		public boolean isUseObservable() {
			return useObservable;
		}

		public void setDim(int dimension) {
			dim = dimension;
		}

		public void setEvswmData(long[][] evswmData) {
			this.evswmData = evswmData;
		}

		public void setHistoryOn(boolean history) {
			historyOn = history;
		}

		public void setIter(int iter) {
			this.iter = iter;
		}

		public void setMethodSelected(String methodSelected) {
			this.methodSelected = methodSelected;
		}

		public void setMsmts(int msmts) {
			this.msmts = msmts;
		}

		public void setnHistories(int nHistories) {
			this.nHistories = nHistories;
		}

		public void setObservableData(WSGComplex[][] observableData) {
			this.observableData = observableData;
		}

		public void setPerspectiveData(WSGComplex[] perspectiveData) {
			this.perspectiveData = perspectiveData;
		}

		public void setRandMax(int randMax) {
			this.randMax = randMax;
		}

		public void setRandMin(int randMin) {
			this.randMin = randMin;
		}

		public void setRandomize(int randomize) {
			this.randomize = randomize;
		}

		public void setUseObservable(boolean useObservable) {
			this.useObservable = useObservable;
		}

		public abstract boolean syncDialog2Model();

		public abstract void syncModel2Dialog();

	}

	public class EVDataModel extends DataModel {

		private EVViewModel viewModel;

		public EVDataModel(IFViewModel view) {
			flameType = Globals.EVModel;

			if (view.getClass().equals(EVViewModel.class))
				viewModel = (EVViewModel) view;
			else
				msgHdl.showError("invalid view model: must be of type EVViewModel", 3);

			for (int i = 0; Referenzmethode.getMethodByIndex(i) != null; i++) {
				if (Referenzmethode.getMethodByIndex(i).isCompatibleWith(EVFlame.class)) {
					methodSelected = Referenzmethode.getMethodByIndex(i).getText();
					break;
				}
			}

		}

		@Override
		public DOMSource createDOMfromModel() throws ParserConfigurationException {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("Measurement");
			rootElement.setAttribute("Msmts", String.format("%d", msmts));
			rootElement.setAttribute("Iterations", String.format("%d", iter));
			rootElement.setAttribute("Method", methodSelected);
			rootElement.setAttribute("History", String.format("%b", historyOn));
			rootElement.setAttribute("nHistories", String.format("%d", nHistories));
			doc.appendChild(rootElement);
			{
				Element eFlame = doc.createElement("Flame");
				eFlame.setAttribute("Dimension", String.format("%d", getFlame().getStateDimension()));
				eFlame.setAttribute("Type", Globals.EVModel);
				rootElement.appendChild(eFlame);

				for (int i = 0; i < evswmData.length; i++) {
					Element row = doc.createElement("Row");
					eFlame.appendChild(row);

					for (int j = 0; j < evswmData[0].length; j++) {
						Element rowValue = doc.createElement("Value");
						rowValue.appendChild(doc.createTextNode(String.format("%d", evswmData[i][j])));
						row.appendChild(rowValue);
					}
				}
			}

			DOMSource source = new DOMSource(doc);
			return source;
		}

		@Override
		public Flame getFlame() {
			if (getEvswmData() != null) {
				return new EVFlame(evswmData);
			} else
				return null;
		}

		@Override
		protected EVViewModel getViewModel() {
			return viewModel;
		}

		@Override
		public boolean syncDialog2Model() {
			historyOn = chckbxHistory.isSelected();

			loggingEnabled = chckbxWriteLog.isSelected();

			dim = Integer.valueOf(viewModel.textFieldDimension.getText());
			msmts = Integer.valueOf(textFieldMeasurments.getText());
			iter = Integer.valueOf(textFieldIterations.getText());
			nHistories = Integer.valueOf(textFieldHistCount.getText());
			randMax = Integer.valueOf(viewModel.textFieldRandomMax.getText());
			randMin = Integer.valueOf(viewModel.textFieldRandomMin.getText());
			evswmData = new long[dim][4];

			if (evswmData != null && evswmData[0] != null && viewModel.tableFlame.getRowCount() == evswmData[0].length
					&& viewModel.tableFlame.getColumnCount() == evswmData.length) {
				for (int i = 0; i < evswmData.length; i++) {
					for (int k = 0; k < evswmData[0].length; k++) {
						evswmData[i][k] = Long
								.parseLong(((String) viewModel.tableFlame.getModel().getValueAt(k, i)).trim());
					}
				}
			}

			return true;
		}

		@Override
		public void syncModel2Dialog() {
			chckbxHistory.setSelected(historyOn);
			int dimOld = Integer.parseInt(viewModel.textFieldDimension.getText());
			viewModel.textFieldDimension.setText(String.valueOf(dim));
			textFieldMeasurments.setText(String.valueOf(msmts));
			textFieldIterations.setText(String.valueOf(iter));
			textFieldHistCount.setText(String.valueOf(nHistories));
			chckbxWriteLog.setSelected(loggingEnabled);
			viewModel.textFieldRandomMax.setText(String.valueOf(randMax));
			viewModel.textFieldRandomMin.setText(String.valueOf(randMin));

			// dim = evswmData[0].length;
			if (dim != dimOld) {
				String[][] tData = new String[4][dim];
				String[] tTitles = new String[dim];
				for (int i = 0; i < dim; i++) {
					tTitles[i] = "e" + (i + 1);
					for (int j = 0; j < 4; j++)
						tData[j][i] = "0";
				}
				viewModel.tableFlame.setModel(new DefaultTableModel(tData, tTitles));
				viewModel.tableHeader = viewModel.tableFlame.getTableHeader();
				viewModel.sizePModel();
			}
			if (evswmData != null)
				for (int i = 0; i < dim; i++) {
					for (int k = 0; k < 4; k++) {
						viewModel.tableFlame.getModel().setValueAt(String.valueOf(evswmData[i][k]), k, i);
					}
				}

		}

	}

	private class EVViewModel implements IFViewModel, ActionListener {

		private JButton btnClearInput;
		private JButton btnRandom;
		private JPanel evPanel = new JPanel();
		private JLabel lblDimension;
		private JLabel lblInput;
		private JTable tableFlame;
		private JTableHeader tableHeader;
		private JLabel lblRow0 = new JLabel("betaj");
		private JLabel lblRow1 = new JLabel("=1*");
		private JLabel lblRow2 = new JLabel("+i*");
		private JLabel lblRow3 = new JLabel("-1*");
		private JLabel lblRow4 = new JLabel("-i*");

		private JLabel lblFlamme;

		private JTextField textFieldDimension;
		private JTextField textFieldRandomMax;
		private JTextField textFieldRandomMin;

		private void sizeEVTable() {
			int dim = Integer.valueOf(textFieldDimension.getText());
			if (dim != tableFlame.getModel().getColumnCount()) {
				String[][] tData = new String[4][dim];
				String[] tTitles = new String[dim];
				for (int i = 0; i < dim; i++) {
					tTitles[i] = "e" + (i + 1);
					for (int j = 0; j < 4; j++)
						tData[j][i] = "0";
				}
				tableFlame.setModel(new DefaultTableModel(tData, tTitles));
				tableHeader = tableFlame.getTableHeader();
			}
		}

		public EVViewModel(JFrame parent) {

			evPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
			evPanel.setLayout(null);
			evPanel.setBounds(10, 10, parent.getWidth() - 35, 160);

			lblDimension = new JLabel("Dimension");
			lblFlamme = new JLabel("E swarm");

			textFieldDimension = new JTextField();
			textFieldDimension.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent arg0) {
					sizeEVTable();
					sizePModel();
				}
			});

			textFieldDimension.setText(String.valueOf(Globals.defaultDimension));
			textFieldDimension.setColumns(10);

			tableFlame = new JTable();
			tableFlame.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
			sizeEVTable();
			lblInput = new JLabel("EV");

			btnRandom = new JButton("Random");
			btnRandom.addActionListener(this);

			btnClearInput = new JButton("Clear");
			btnClearInput.addActionListener(this);

			textFieldRandomMin = new JTextField();
			textFieldRandomMin.setText(String.valueOf(Globals.defaultRandMin));
			textFieldRandomMin.setColumns(10);

			textFieldRandomMax = new JTextField();
			textFieldRandomMax.setText(String.valueOf(Globals.defaultRandMax));
			textFieldRandomMax.setColumns(10);

			evPanel.add(textFieldDimension);
			evPanel.add(tableHeader);
			evPanel.add(lblRow0);
			evPanel.add(lblRow1);
			evPanel.add(lblRow2);
			evPanel.add(lblRow3);
			evPanel.add(lblRow4);
			evPanel.add(tableFlame);
			evPanel.add(lblFlamme);
			evPanel.add(lblDimension);
			evPanel.add(lblInput);
			evPanel.add(btnClearInput);
			evPanel.add(btnRandom);
			evPanel.add(textFieldRandomMin);
			evPanel.add(textFieldRandomMax);

			sizePModel();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == btnClearInput) {
				int cCol = tableFlame.getModel().getColumnCount();
				int cRow = tableFlame.getModel().getRowCount();
				for (int i = 0; i < cRow; i++)
					for (int j = 0; j < cCol; j++)
						tableFlame.getModel().setValueAt("0", i, j);
			} else if (e.getSource() == btnRandom) {
				int cCol = tableFlame.getModel().getColumnCount();
				int cRow = tableFlame.getModel().getRowCount();
				for (int i = 0; i < cRow; i++)
					for (int j = 0; j < cCol; j++)
						tableFlame.getModel().setValueAt("0", i, j);

				int randMin = Integer.valueOf(textFieldRandomMin.getText());
				int randMax = Integer.valueOf(textFieldRandomMax.getText());

				// WSGMath wsgm = new WSGMath();
				WSGRandom wsgm = WSGRandom.getInstance(Thread.currentThread());

				long swarmSize = (long) randMin;
				if (randMax > randMin)
					swarmSize = (long) randMin + wsgm.nextInt(randMax - randMin);
				int evsDataLen = cCol * cRow;
				for (int i = 0; i < swarmSize; i++) {
					int rand = wsgm.nextInt(evsDataLen);
					int pIndex = rand / 4;
					int iIndex = rand % 4;
					long val = Long.parseLong(((String) tableFlame.getModel().getValueAt(iIndex, pIndex)).trim());
					tableFlame.getModel().setValueAt(String.valueOf(val + 1), iIndex, pIndex);
				}
			}

		}

		@Override
		public JPanel getPanelModel() {
			return evPanel;
		}

		@Override
		public void setEnabled(boolean isEnabled) {
			lblDimension.setEnabled(isEnabled);
			textFieldDimension.setEnabled(isEnabled);
			tableFlame.setEnabled(isEnabled);
			lblInput.setEnabled(isEnabled);
			btnClearInput.setEnabled(isEnabled);
			btnRandom.setEnabled(isEnabled);
			textFieldRandomMin.setEnabled(isEnabled);
			textFieldRandomMax.setEnabled(isEnabled);
		}

		@Override
		public void sizePModel() {
			lblDimension.setBounds(2, 2, 60, 15);
			textFieldDimension.setBounds(65, 2, 20, 15);

			int tWidth = evPanel.getWidth() - 20;
			int dWidth = tableFlame.getColumnCount() * 75;
			tWidth = tWidth > dWidth ? dWidth : tWidth;

			int pMCenter = evPanel.getWidth() / 2;

			lblFlamme.setBounds(pMCenter - 40, 2, 80, 14);
			lblRow0.setHorizontalAlignment(SwingConstants.RIGHT);
			lblRow1.setHorizontalAlignment(SwingConstants.RIGHT);
			lblRow2.setHorizontalAlignment(SwingConstants.RIGHT);
			lblRow3.setHorizontalAlignment(SwingConstants.RIGHT);
			lblRow4.setHorizontalAlignment(SwingConstants.RIGHT);
			lblRow0.setBounds(2, 50, 31, 20);
			lblRow1.setBounds(2, 70, 31, 20);
			lblRow2.setBounds(2, 90, 31, 20);
			lblRow3.setBounds(2, 110, 31, 20);
			lblRow4.setBounds(2, 130, 31, 20);
			tableHeader.setBounds(35, 50, tWidth, 20);
			tableFlame.setRowHeight(20);
			tableFlame.setBounds(35, 70, tWidth, 4 * 20);

			lblInput.setBounds(2, 35, 50, 15);
			btnClearInput.setBounds(60, 30, 70, 20);
			btnRandom.setBounds(evPanel.getWidth() - 180, 30, 90, 20);
			textFieldRandomMin.setBounds(evPanel.getWidth() - 85, 30, 40, 20);
			textFieldRandomMax.setBounds(evPanel.getWidth() - 40, 30, 40, 20);

		}
	}

	public class ImportXML {

		protected int dim = 2;
		protected long[][] evswmData;
		protected String flameType;
		protected boolean historyOn = false;
		protected int iter;
		protected String methodSelected;
		protected int msmts;
		protected int nHistories;
		protected WSGComplex[][] observableData;
		protected WSGComplex[] perspectiveData;
		protected int randMax;
		protected int randMin;
		protected int randomize;
		protected boolean useObservable = false;

		public DataModel importModelFromXML() {
			DataModel selectedDataModel = null;
			File inFile = new File(Globals.xmlDir);
			if (!inFile.exists())
				inFile = new File("./");

			try {

				JFileChooser fc = new JFileChooser(inFile);

				int returnVal = fc.showOpenDialog(frame);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					inFile = fc.getSelectedFile();

					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder.parse(inFile);

					selectedDataModel = parse2Model(doc);

					if (selectedDataModel != null) {
						selectedDataModel.cloneData(this);
						msgHdl.showError("Measurement restored from : " + inFile.toString(), 0);
					} else {
						msgHdl.showError("Invalid file : " + inFile.toString(), 1);
					}
				} else {

					msgHdl.showError("No file selected", 1);
				}

			} catch (SAXException e) {
				msgHdl.showError("Invalid XML : " + inFile.toString(), 2);
			} catch (IOException e) {
				msgHdl.showError("File not readable : " + inFile.toString(), 2);
			} catch (ParserConfigurationException e) {
				msgHdl.showError("Invalid XML : " + inFile.toString(), 2);
			}

			return selectedDataModel;
		}

		private DataModel parse2Model(Document doc) {
			DataModel selectedDataModel = null;

			doc.getDocumentElement().normalize();
			Element rootElement = doc.getDocumentElement();

			if (!rootElement.getNodeName().equals("Measurement")) {
				msgHdl.showError("No measurement file - try load batch", 2);
				return null;
			}

			methodSelected = rootElement.getAttribute("Method");
			try {
				msmts = Integer.valueOf(rootElement.getAttribute("Msmts"));
				iter = Integer.valueOf(rootElement.getAttribute("Iterations"));
			} catch (NumberFormatException e) {
				msmts = Globals.defaultMeasurements;
				iter = Globals.defaultIterations;
			}

			try {
				historyOn = Boolean.valueOf(rootElement.getAttribute("History"));
				nHistories = Integer.valueOf(rootElement.getAttribute("nHistories"));
			} catch (NumberFormatException e) {
				historyOn = false;
				nHistories = Globals.defaultHistories;
			}

			try {
				randomize = Integer.valueOf(rootElement.getAttribute("Randomize"));
			} catch (NumberFormatException e) {
				randomize = 0;
			}

			try {
				randMin = Integer.valueOf(rootElement.getAttribute("RandMin"));
				randMax = Integer.valueOf(rootElement.getAttribute("RandMax"));
			} catch (NumberFormatException e) {
				randMin = Globals.defaultRandMin;
				randMax = Globals.defaultRandMax;
			}

			NodeList cns = rootElement.getChildNodes();
			for (int i = 0; i < cns.getLength(); i++) {
				Node tNode = cns.item(i);
				// tNode.normalize();
				if (tNode.getNodeType() != Node.ELEMENT_NODE)
					continue;

				if (tNode.getNodeName().equals("Observable")) {
					NamedNodeMap nm = tNode.getAttributes();
					for (int j = 0; j < nm.getLength(); j++) {
						Node nn = nm.item(j);
						if (nn.getNodeName().equals("Use")) {
							useObservable = Boolean.valueOf(nn.getTextContent());
						} else if (nn.getNodeName().equals("Dimension")) {
							dim = Integer.valueOf(nn.getTextContent());
						} else {
							msgHdl.showError("invalid attribute : " + nn.getNodeName() + " in " + tNode.getNodeName(),
									1);
						}
					}
					if (useObservable) {
						NodeList nRows = tNode.getChildNodes();
						if (observableData == null) {
							observableData = new WSGComplex[dim][dim];
						}
						int iJ = 0;
						int iK = 0;
						for (int j = 0; j < nRows.getLength() && iJ < observableData.length; j++) {
							// nRows.item(j).normalize();
							if (nRows.item(j).getNodeType() != Node.ELEMENT_NODE)
								continue;
							if (!nRows.item(j).getNodeName().equals("Row"))
								continue;
							NodeList nCols = nRows.item(j).getChildNodes();
							iK = 0;
							for (int k = 0; k < nCols.getLength() && iK < observableData[0].length; k++) {
								if (nCols.item(k).getNodeType() != Node.ELEMENT_NODE)
									continue;
								if (!nCols.item(k).getNodeName().equals("Value"))
									continue;
								observableData[iJ][iK++] = WSGComplex.valueOf(nCols.item(k).getTextContent());
							}
							iJ++;
						}
						if (iJ != observableData.length)
							msgHdl.showError(String.format(
									"data dimension %d does not match XML dimension %d in observable data",
									observableData.length, iJ), 2);
						if (iK != observableData[0].length)
							msgHdl.showError(
									String.format("col dimension %d does not match XML columns %d in observable data",
											observableData[0].length, iK),
									2);
					}
				} else if (tNode.getNodeName().equals("Flame")) {
					NamedNodeMap nm = tNode.getAttributes();
					for (int j = 0; j < nm.getLength(); j++) {
						Node nn = nm.item(j);
						if (nn.getNodeName().equals("Dimension")) {
							dim = Integer.valueOf(nn.getTextContent());
						} else if (nn.getNodeName().equals("Type")) {
							flameType = nn.getTextContent();
						} else {
							msgHdl.showError("invalid attribute : " + nn.getNodeName() + " in " + tNode.getNodeName(),
									1);
						}
					}
					NodeList nRows = tNode.getChildNodes();
					if (evswmData == null) {
						if (flameType.equals(Globals.PauliModel)) {
							evswmData = new long[4][4];
							selectedDataModel = pauliDataModel;
						} else {
							evswmData = new long[dim][4];
							selectedDataModel = evDataModel;
						}
					}

					int iJ = 0;
					int iK = 0;
					for (int j = 0; iJ < evswmData.length && j < nRows.getLength(); j++) {
						if (nRows.item(j).getNodeType() != Node.ELEMENT_NODE
								|| !nRows.item(j).getNodeName().equals("Row"))
							continue;
						// nRows.item(j).normalize();
						NodeList nCols = nRows.item(j).getChildNodes();
						iK = 0;
						for (int k = 0; iK < evswmData[0].length && k < nCols.getLength(); k++) {
							if (nCols.item(k).getNodeType() != Node.ELEMENT_NODE
									|| !nCols.item(k).getNodeName().equals("Value"))
								continue;
							evswmData[iJ][iK++] = Long.parseLong(nCols.item(k).getTextContent().trim());
						}
						iJ++;
					}
					if (iJ != evswmData.length)
						msgHdl.showError(
								String.format("data dimension %d does not match XML dimension %d in flame data",
										evswmData.length, iJ),
								2);
					if (iK != evswmData[0].length)
						msgHdl.showError(String.format("col dimension %d does not match XML columns %d in flame data",
								evswmData[0].length, iK), 2);
				} else if (tNode.getNodeName().equals("Perspective")) {
					NamedNodeMap nm = tNode.getAttributes();
					for (int j = 0; j < nm.getLength(); j++) {
						Node nn = nm.item(j);
						if (nn.getNodeName().equals("Use")) {
							useObservable = Boolean.valueOf(nn.getTextContent());
						} else if (nn.getNodeName().equals("Dimension")) {
							dim = Integer.valueOf(nn.getTextContent());
						} else {
							msgHdl.showError("invalid attribute : " + nn.getNodeName() + " in " + tNode.getNodeName(),
									1);
						}
					}
					NodeList nRows = tNode.getChildNodes();
					if (perspectiveData == null) {
						perspectiveData = new WSGComplex[dim];
					}
					int iJ = 0;
					for (int j = 0; j < nRows.getLength() && iJ < perspectiveData.length; j++) {
						if (nRows.item(j).getNodeType() != Node.ELEMENT_NODE
								|| !nRows.item(j).getNodeName().equals("Value"))
							continue;
						perspectiveData[iJ++] = WSGComplex.valueOf(nRows.item(j).getTextContent());
					}
					if (iJ != perspectiveData.length)
						msgHdl.showError(
								String.format("data dimension %d does not match XML dimension %d in perspective data",
										perspectiveData.length, iJ),
								2);
				}

			}
			return selectedDataModel;
		}
	}

	private class InitThread implements Runnable {
		public int getIterations() {
			return datamodel.getIter();
		}

		public int getIterationsCompleted() {
			if (mPool.getFocusRunnable() != null
					&& mPool.getFocusRunnable() instanceof RunMeasurementSample.HandleIterations) {
				return ((RunMeasurementSample.HandleIterations) mPool.getFocusRunnable()).getIterationsCompleted();
			} else
				return 0;
		}

		public int getMsmts() {
			return datamodel.getMsmts();
		}

		public int getMsmtsCompleted() {
			if (rMsmtSmp != null)
				return rMsmtSmp.getMsmtsCompleted();
			else
				return 0;
		}

		@Override
		public void run() {
			Logging flameLog = Logging.getBatchLog("EVSLog-" + datamodel.getMethodSelected(), loggingEnabled);
			Logging histLog = Logging.getHistoryLog("EVSHis-" + datamodel.getMethodSelected(), datamodel.isHistoryOn());

			flameLog.showError("InitThread started", 0);

			RunMeasurementSample.isStopped = false;
			RunMeasurementSample.isPaused = false;

			int nHist = datamodel.getnHistories();
			nHist = nHist < Globals.maxFullHistories ? nHist : Globals.maxFullHistories;
			datamodel.setnHistories(nHist);

			textAreaResults.setText("sample started at "
					+ String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS%n", Calendar.getInstance()));
			textAreaResults.append(Globals.getString());

			textAreaResults.append("\nsource swarm=" + datamodel.getFlame());

			try {
				rMsmtSmp = new RunMeasurementSample(null, datamodel.evswmData, datamodel.flameType,
						datamodel.observableData, datamodel.perspectiveData, datamodel.useObservable,
						datamodel.methodSelected, datamodel.msmts, datamodel.iter, datamodel.nHistories, flameLog,
						histLog);
				msmtHist = rMsmtSmp.handleSample();
			} catch (InvalidTermException | InterruptedException e1) {
				msgHdl.showError(e1.getMessage(), 2);
			}

			for (int i = 0; i < jLThread.length; i++) {
				jLThread[i].setBackground(Color.GRAY);
			}

			if (msmtHist != null && datamodel.getMsmts() > 0) {
				textAreaResults.append(msmtHist.toString());
			} else {
				textAreaResults.append("\nempty sample");
			}

			textAreaResults.append("\n\nsample finished at "
					+ String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS\n", Calendar.getInstance()));
			textAreaResults.append("\nPlease view results in log files:");
			textAreaResults.append("\nErrorLog=" + Logging.getErrorLog().toString());
			if (loggingEnabled)
				textAreaResults.append("\nBatchLog=" + Logging.getBatchLog().toString());
			if (historyLoggingEnabled)
				textAreaResults.append("\nHistoryLog=" + Logging.getHistoryLog().toString());

			flameLog.writeLog(textAreaResults.getText());
			flameLog.closeLog();
			histLog.closeLog();

			for (int i = 0; i < mntmProvider.length; i++)
				mntmProvider[i].setEnabled(true);

			lblMessage.setForeground(Color.BLACK);
			flameLog.showError("InitThread ended", 0);
		}

		public void updateThreadBar() {
			for (int i = 0; i < jLThread.length; i++) {
				if (mPool.isRunning(i)) {
					if (RunMeasurementSample.isPaused)
						jLThread[i].setBackground(Color.YELLOW);
					else
						jLThread[i].setBackground(Color.GREEN);
					if (mPool.getFocusThread() == null)
						mPool.setFocusByIndex(i);
				} else {
					jLThread[i].setBackground(Color.RED);
					if (mPool.getFocusThreadIndex() == i)
						mPool.setFocusByIndex(-1);
				}
			}
		}

	}

	public class PauliDataModel extends DataModel {

		private PauliViewModel viewModel;

		public PauliDataModel(IFViewModel view) {

			flameType = Globals.PauliModel;

			if (view.getClass().equals(PauliViewModel.class))
				viewModel = (PauliViewModel) view;
			else
				msgHdl.showError("invalid view model: must be of type PauliViewModel", 3);

			for (int i = 0; Referenzmethode.getMethodByIndex(i) != null; i++) {
				if (Referenzmethode.getMethodByIndex(i).isCompatibleWith(PauliFlame.class)) {
					methodSelected = Referenzmethode.getMethodByIndex(i).getText();
					break;
				}
			}
		}

		@Override
		public DOMSource createDOMfromModel() throws ParserConfigurationException {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("Measurement");
			rootElement.setAttribute("Msmts", String.format("%d", msmts));
			rootElement.setAttribute("Iterations", String.format("%d", iter));
			rootElement.setAttribute("Method", methodSelected);
			rootElement.setAttribute("History", String.format("%b", historyOn));
			rootElement.setAttribute("nHistories", String.format("%d", nHistories));
			doc.appendChild(rootElement);
			{
				Element eObservable = doc.createElement("Observable");
				eObservable.setAttribute("Use", String.format("%b", useObservable));
				eObservable.setAttribute("Dimension", String.format("%d", dim));
				rootElement.appendChild(eObservable);

				if (useObservable && observableData != null)
					for (int i = 0; i < dim; i++) {
						Element row = doc.createElement("Row");
						eObservable.appendChild(row);
						for (int j = 0; j < dim; j++) {
							Element rowValue = doc.createElement("Value");
							rowValue.appendChild(doc.createTextNode(observableData[i][j].toString()));
							row.appendChild(rowValue);
						}
					}
			}
			{
				Element eFlame = doc.createElement("Flame");
				eFlame.setAttribute("Dimension", String.format("%d", getFlame().getStateDimension()));
				eFlame.setAttribute("Type", Globals.PauliModel);
				rootElement.appendChild(eFlame);

				for (int i = 0; i < evswmData.length; i++) {
					Element row = doc.createElement("Row");
					eFlame.appendChild(row);

					for (int j = 0; j < evswmData[0].length; j++) {
						Element rowValue = doc.createElement("Value");
						rowValue.appendChild(doc.createTextNode(String.format("%d", evswmData[i][j])));
						row.appendChild(rowValue);
					}
				}
			}
			{
				Element ePerspective = doc.createElement("Perspective");
				ePerspective.setAttribute("Dimension", String.format("%d", dim));
				rootElement.appendChild(ePerspective);

				for (int j = 0; j < perspectiveData.length; j++) {
					Element rowValue = doc.createElement("Value");
					rowValue.appendChild(doc.createTextNode(perspectiveData[j].toString()));
					ePerspective.appendChild(rowValue);
				}
			}
			DOMSource source = new DOMSource(doc);
			return source;
		}

		@Override
		public Flame getFlame() {
			WSGComplex[] psD = getPerspectiveData();
			long[][] evD = getEvswmData();

			WSGComplex[][] obD = getObservableData();
			if (isUseObservable() && evD != null && psD != null && obD != null) {
				return new PauliFlame(evD, new WSGMatrix(psD), new WSGMatrix(obD));
			} else if (evD != null && psD != null) {
				return new PauliFlame(evD, new WSGMatrix(psD));
			} else if (evD != null) {
				return new PauliFlame(evD);
			} else {
				return null;
			}
		}

		@Override
		protected PauliViewModel getViewModel() {
			return viewModel;
		}

		@Override
		public boolean syncDialog2Model() {
			dim = 2;
			boolean success = true;
			historyOn = chckbxHistory.isSelected();
			nHistories = Integer.valueOf(textFieldHistCount.getText());
			randMax = Integer.valueOf(viewModel.textFieldRandomMax.getText());
			randMin = Integer.valueOf(viewModel.textFieldRandomMin.getText());

			loggingEnabled = chckbxWriteLog.isSelected();

			useObservable = viewModel.chckbxUseObersable.isSelected();

			msmts = Integer.valueOf(textFieldMeasurments.getText());
			iter = Integer.valueOf(textFieldIterations.getText());

			int pSize = viewModel.tableFlame.getModel().getRowCount();
			int cSize = viewModel.tableFlame.getModel().getColumnCount();

			if (evswmData == null) {
				evswmData = new long[4][4];
			}
			if (evswmData.length != pSize || evswmData[0].length != cSize) {
				evswmData = new long[pSize][cSize];
				msgHdl.showError("flame data: Wrong dimensions", 2);
				success = false;
			}

			for (int i = 0; i < pSize; i++) {
				for (int k = 0; k < cSize; k++) {
					evswmData[i][k] = Long
							.parseLong(((String) viewModel.tableFlame.getModel().getValueAt(k, i)).trim());
				}
			}

			if (useObservable) {

				int pDim = viewModel.tableObservable.getModel().getRowCount();

				if (observableData == null) {
					observableData = new WSGComplex[dim][dim];
				}
				if (observableData.length != pDim || observableData[0].length != pDim) {
					observableData = new WSGComplex[pDim][pDim];
					msgHdl.showError("observable data: Wrong dimension", 2);
					success = false;
				}

				observableData = new WSGComplex[pDim][pDim];
				for (int i = 0; i < pDim; i++) {
					for (int k = 0; k < pDim; k++) {
						try {
							String val = (String) viewModel.tableObservable.getModel().getValueAt(i, k);
							observableData[i][k] = WSGComplex.valueOf(val);
						} catch (NumberFormatException e1) {
							observableData[i][k] = WSGComplex.ZERO;
							msgHdl.showError(e1.toString(), 1);
							success = false;
						}
					}
				}
			}

			if (perspectiveData == null)
				perspectiveData = new WSGComplex[dim];

			if (perspectiveData.length != dim) {
				msgHdl.showError("perspective data: Wrong dimension", 2);
				success = false;
			}

			for (int i = 0; i < dim; i++) {
				try {
					perspectiveData[i] = WSGComplex
							.valueOf((String) viewModel.tablePerspective.getModel().getValueAt(i, 0));
				} catch (NumberFormatException e1) {
					perspectiveData[i] = WSGComplex.ZERO;
					msgHdl.showError(e1.toString(), 1);
					success = false;
				}
			}
			return success;
		}

		@Override
		public void syncModel2Dialog() {

			chckbxHistory.setSelected(historyOn);
			textFieldHistCount.setText(String.format("%d", nHistories));
			viewModel.textFieldRandomMax.setText(String.valueOf(randMax));
			viewModel.textFieldRandomMin.setText(String.valueOf(randMin));

			chckbxWriteLog.setSelected(loggingEnabled);

			viewModel.chckbxUseObersable.setSelected(useObservable);
			if (useObservable) {
				viewModel.tableObservable.setVisible(true);
			} else {
				viewModel.tableObservable.setVisible(false);
			}

			frame.setTitle("Simulation using " + methodSelected);

			textFieldMeasurments.setText(String.format("%d", msmts));
			textFieldIterations.setText(String.format("%d", iter));

			if (evswmData == null) {
				evswmData = new long[4][4];
			}
			for (int i = 0; i < evswmData.length; i++) {
				for (int k = 0; k < evswmData[0].length; k++) {
					viewModel.tableFlame.getModel().setValueAt(String.valueOf(evswmData[i][k]), k, i);
				}
			}

			if (useObservable) {
				if (observableData == null)
					observableData = new WSGComplex[][] { { new WSGComplex(1), new WSGComplex(0) },
							{ new WSGComplex(0), new WSGComplex(-1) } };
				for (int i = 0; i < observableData.length; i++) {
					for (int k = 0; k < observableData[0].length; k++) {
						viewModel.tableObservable.getModel().setValueAt(observableData[i][k].toString(), i, k);
					}
				}
			}

			if (perspectiveData == null)
				perspectiveData = new WSGComplex[] { new WSGComplex(1.0), new WSGComplex(1.0) };
			for (int i = 0; i < perspectiveData.length; i++) {
				viewModel.tablePerspective.getModel().setValueAt(perspectiveData[i].toString(), i, 0);
			}
		}

	}

	private class PauliViewModel implements IFViewModel, ActionListener {

		private JButton btnClearInput;
		private JButton btnRandom;

		private JCheckBox chckbxUseObersable;
		private JLabel lblFlamme;
		private JLabel lblPerspektive;
		private JPanel pauliPanel = new JPanel();
		private JTableHeader tableHeader;
		private JTable tableFlame;

		private JLabel lblRow1 = new JLabel("=1*");
		private JLabel lblRow2 = new JLabel("+i*");
		private JLabel lblRow3 = new JLabel("-1*");
		private JLabel lblRow4 = new JLabel("-i*");

		private JTable tableObservable;
		private JTable tablePerspective;
		private JTextField textFieldRandomMax;
		private JTextField textFieldRandomMin;

		public PauliViewModel(JFrame parent) {

			pauliPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
			pauliPanel.setBounds(10, 10, parent.getWidth() - 35, 160);
			pauliPanel.setLayout(null);

			{
				chckbxUseObersable = new JCheckBox("use Observable");
				chckbxUseObersable.setSelected(Boolean.valueOf(Globals.defaultUseObservable));
				chckbxUseObersable.addActionListener(this);

				tableObservable = new JTable();
				tableObservable.setModel(new DefaultTableModel(new String[][] { { "1", "0" }, { "0", "-1" }, },
						new String[] { "c0", "c1" }));
				tableObservable.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

				lblFlamme = new JLabel("P swarm");

				btnRandom = new JButton("Random");
				btnRandom.addActionListener(this);

				btnClearInput = new JButton("Clear");
				btnClearInput.addActionListener(this);

				textFieldRandomMin = new JTextField();
				textFieldRandomMin.setText(String.valueOf(Globals.defaultRandMin));
				textFieldRandomMin.setColumns(10);

				textFieldRandomMax = new JTextField();
				textFieldRandomMax.setText(String.valueOf(Globals.defaultRandMax));
				textFieldRandomMax.setColumns(10);

				tableFlame = new JTable();
				tableFlame.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
				tableFlame
						.setModel(
								new DefaultTableModel(
										new String[][] { { "0", "0", "0", "0" }, { "0", "0", "0", "0" },
												{ "0", "0", "0", "0" }, { "0", "0", "0", "0" } },
										new String[] { "p0", "p1", "p2", "p3" }));

				tableHeader = tableFlame.getTableHeader();
				lblPerspektive = new JLabel("Perspective");

				tablePerspective = new JTable();
				tablePerspective
						.setModel(new DefaultTableModel(new String[][] { { "1" }, { "1" }, }, new String[] { "0" }));
				tablePerspective.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

				pauliPanel.add(chckbxUseObersable);
				pauliPanel.add(tableObservable);
				pauliPanel.add(lblFlamme);
				pauliPanel.add(btnRandom);
				pauliPanel.add(btnClearInput);
				pauliPanel.add(textFieldRandomMin);
				pauliPanel.add(textFieldRandomMax);
				pauliPanel.add(tableHeader);
				pauliPanel.add(lblRow1);
				pauliPanel.add(lblRow2);
				pauliPanel.add(lblRow3);
				pauliPanel.add(lblRow4);
				pauliPanel.add(tableFlame);
				pauliPanel.add(lblPerspektive);
				pauliPanel.add(tablePerspective);
				sizePModel();
			}

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == btnClearInput) {
				int cCol = tableFlame.getModel().getColumnCount();
				int cRow = tableFlame.getModel().getRowCount();
				for (int i = 0; i < cRow; i++)
					for (int j = 0; j < cCol; j++)
						tableFlame.getModel().setValueAt("0", i, j);
			} else if (e.getSource() == btnRandom) {
				int cCol = tableFlame.getModel().getColumnCount();
				int cRow = tableFlame.getModel().getRowCount();
				for (int i = 0; i < cRow; i++)
					for (int j = 0; j < cCol; j++)
						tableFlame.getModel().setValueAt("0", i, j);

				int randMin = Integer.valueOf(textFieldRandomMin.getText());
				int randMax = Integer.valueOf(textFieldRandomMax.getText());
				WSGRandom wsgm = WSGRandom.getInstance(Thread.currentThread());
				long swarmSize = (long) randMin;
				if (randMax > randMin)
					swarmSize = (long) randMin + wsgm.nextInt(randMax - randMin);
				int evsDataLen = cCol * cRow;

				for (int i = 0; i < swarmSize; i++) {
					int rand = wsgm.nextInt(evsDataLen);
					int pIndex = rand / 4;
					int iIndex = rand % 4;
					long val = Long.parseLong(((String) tableFlame.getModel().getValueAt(pIndex, iIndex)).trim());
					tableFlame.getModel().setValueAt(String.valueOf(val + 1), pIndex, iIndex);
				}
			} else if (e.getSource() == chckbxUseObersable) {
				if (chckbxUseObersable.isSelected()) {
					tableObservable.setVisible(true);
					tableObservable.setEnabled(true);
				} else {
					tableObservable.setEnabled(false);
					tableObservable.setVisible(false);
				}
			}
		}

		@Override
		public JPanel getPanelModel() {
			return pauliPanel;
		}

		@Override
		public void setEnabled(boolean e) {
			textFieldRandomMax.setEnabled(e);
			textFieldRandomMin.setEnabled(e);

			btnClearInput.setEnabled(e);
			btnRandom.setEnabled(e);

			tableFlame.setEnabled(e);
			tableObservable.setEnabled(e);
			tablePerspective.setEnabled(e);

			chckbxUseObersable.setEnabled(e);
		}

		@Override
		public void sizePModel() {
			int pMCenter = pauliPanel.getWidth() / 2;
			int tFStart = pMCenter - 150;
			int tFEnd = pMCenter + 150;

			chckbxUseObersable.setBounds(5, 20, 117, 23);
			tableObservable.setBounds(5, 48, 150, 32);
			lblFlamme.setBounds(pMCenter - 40, 2, 80, 14);
			btnClearInput.setBounds(tFStart + 45, 20, 70, 20);
			btnRandom.setBounds(tFEnd - 180, 20, 90, 20);
			textFieldRandomMin.setBounds(tFEnd - 85, 20, 40, 20);
			textFieldRandomMax.setBounds(tFEnd - 40, 20, 40, 20);

			lblRow1.setHorizontalAlignment(SwingConstants.RIGHT);
			lblRow2.setHorizontalAlignment(SwingConstants.RIGHT);
			lblRow3.setHorizontalAlignment(SwingConstants.RIGHT);
			lblRow4.setHorizontalAlignment(SwingConstants.RIGHT);
			lblRow1.setBounds(tFStart - 33, 70, 31, 20);
			lblRow2.setBounds(tFStart - 33, 90, 31, 20);
			lblRow3.setBounds(tFStart - 33, 110, 31, 20);
			lblRow4.setBounds(tFStart - 33, 130, 31, 20);

			tableHeader.setBounds(tFStart, 50, 300, 20);
			tableFlame.setRowHeight(20);
			tableFlame.setBounds(tFStart, 70, 300, 80);
			lblPerspektive.setBounds(pauliPanel.getWidth() - 105, 22, 100, 14);
			tablePerspective.setBounds(pauliPanel.getWidth() - 105, 48, 100, 32);
		}
	}

	public static RunFlame frame;

	public static boolean loggingEnabled = false;
	public static boolean historyLoggingEnabled = false;

	private static MsmtHistory msmtHist = null;

	/**
	 * 
	 */
	private static final long serialVersionUID = 6117604270584809350L;

	public static void main(String[] args) {

		Globals.loadINIFile(Globals.iniFileName);

		if (args == null || args.length < 1 || args[0].length() < 1) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						frame = new RunFlame();
						frame.setVisible(true);
						frame.addComponentListener(frame);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} else {
			File rbFile = new File(args[0]);
			if (!rbFile.exists()) {
				Logging.getErrorLog().showError(args[0] + " does not exist!", 2);
				return;
			}

			RunBatch rb = new RunBatch(rbFile);
			Thread rbThread = new Thread(rb, "RunBatch");
			// rbThread.setPriority(1);
			rbThread.start();
			int tCtr = 0;
			try {
				Thread.sleep(100);
				while (rbThread.isAlive()) {
					if (tCtr % 60 == 0) {
						int tage = tCtr / 86400;
						int stunden = (tCtr % 86400) / 3600;
						int minuten = (tCtr % 3600) / 60;
						System.out.print(String.format("%n%03dd%02dh%02dm", tage, stunden, minuten));
					}
					System.out.print(".");
					Thread.sleep(1000);
					tCtr++;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			System.out.println("\nRunFlame " + args[0] + " completed");
		}
	}

	private RunBatch batchInstance;
	private JButton btnRun;

	private JButton btnStop;
	private JCheckBox chckbxHistory;
	private JCheckBox chckbxWriteLog;

	private JPanel contentPane;
	private DataModel datamodel;

	private EVDataModel evDataModel;

	private JTextField[] jLThread = new JTextField[Globals.noOfBatchFileThreads];
	private JLabel lblIterations;
	private JLabel lblIterationsPerMeasurement;
	private JLabel lblMeasurements;
	private JLabel lblMessage;
	private JLabel lblMsmts;
	private JLabel lblNodes;
	private JLabel lblResults;

	private JMenuItem mntmEV;
	private JMenuItem mntmLoad;
	private JMenuItem mntmLoadBatch;
	private JMenuItem mntmMethoden[];
	private JMenuItem mntmPauli;
	private JMenuItem mntmProvider[];
	private JMenuItem mntmSave;
	private JMenuItem mntmViewOptions;
	private JMenuItem mntmAbout;
	private JMenuItem mntmBackground;

	private JPanel panelModel;
	private JPanel panelResults;

	private JProgressBar progressBarIterations;
	private JProgressBar progressBarMsmts;
	private JProgressBar progressBarNodes;

	private JScrollPane scrollPane;
	private JTextArea textAreaResults;
	private JTextField textFieldHistCount;

	private JTextField textFieldIterations;
	private JTextField textFieldMeasurments;
	private MsmtThreadPool mPool;
	private MsgHandler msgHdl;

	private PauliDataModel pauliDataModel;

	private Provider3D provider = null;
	private RunMeasurementSample rMsmtSmp = null;

	private Timer timer;

	private xyzGraph xyzG;

	private void instantiateProviderAndMethods() {

		for (int i = 0; i < Globals.MethodMenuEntries.length; i++) {
			try {
				ClassLoader.getSystemClassLoader().loadClass(Globals.MethodMenuEntries[i]).newInstance();
			} catch (ClassNotFoundException e) {
				Logging.getErrorLog().showError(e.toString(), 1);
			} catch (InstantiationException e) {
				Logging.getErrorLog().showError(e.toString(), 1);
			} catch (IllegalAccessException e) {
				Logging.getErrorLog().showError(e.toString(), 1);
			}
		}
		for (int i = 0; i < Globals.ShowMenuEntries.length; i++) {
			try {
				ClassLoader.getSystemClassLoader().loadClass(Globals.ShowMenuEntries[i]).newInstance();
			} catch (ClassNotFoundException e) {
				Logging.getErrorLog().showError(e.toString(), 1);
			} catch (InstantiationException e) {
				Logging.getErrorLog().showError(e.toString(), 1);
			} catch (IllegalAccessException e) {
				Logging.getErrorLog().showError(e.toString(), 1);
			}
		}
	}

	public RunFlame() {
		instantiateProviderAndMethods();
		mPool = MsmtThreadPool.getInstance();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 750, 750);
		setMinimumSize(new Dimension(740, 450));

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnShow = new JMenu("Show");
		JMenu mnFile = new JMenu("File");
		JMenu mnMethod = new JMenu("Method");
		JMenu mnView = new JMenu("View");
		JMenu mnOptions = new JMenu("Options");
		JMenu mnHelp = new JMenu("Help");

		mntmPauli = new JMenuItem("P swarm");
		mntmPauli.addActionListener(this);
		mntmEV = new JMenuItem("E swarm");
		mntmEV.addActionListener(this);

		mntmSave = new JMenuItem("Save");
		mntmSave.setToolTipText("Save measurement to disk");
		mntmSave.addActionListener(this);
		mntmLoad = new JMenuItem("Load");
		mntmLoad.setToolTipText("Load measurement from disk");
		mntmLoad.addActionListener(this);
		mntmLoadBatch = new JMenuItem("Load batch");
		mntmLoadBatch.setToolTipText("Load batch file");
		mntmLoadBatch.addActionListener(this);
		mntmViewOptions = new JMenuItem("Globals");
		mntmViewOptions.addActionListener(this);
		mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(this);
		mntmBackground = new JMenuItem("Background");
		mntmBackground.addActionListener(this);

		mnFile.add(mntmSave);
		mnFile.add(mntmLoad);
		mnFile.add(mntmLoadBatch);

		mnView.add(mntmPauli);
		mnView.add(mntmEV);

		mnOptions.add(mntmViewOptions);

		mnHelp.add(mntmAbout);
		mnHelp.add(mntmBackground);
		menuBar.add(mnFile);
		menuBar.add(mnView);
		menuBar.add(mnMethod);
		menuBar.add(mnShow);
		menuBar.add(mnOptions);
		menuBar.add(mnHelp);

		mntmMethoden = new JMenuItem[Referenzmethode.getMethodenListe().length];
		for (int i = 0; i < Referenzmethode.getMethodenListe().length; i++) {
			mntmMethoden[i] = new JMenuItem(Referenzmethode.getMethodByIndex(i).getText());
			mntmMethoden[i].setToolTipText("<html><h1>" + Referenzmethode.getMethodByIndex(i).getText() + "</h1><p>"
					+ Referenzmethode.getMethodByIndex(i).getTipText() + "</p></html>");
			mntmMethoden[i].setEnabled(true);
			mntmMethoden[i].addActionListener(this);
			mnMethod.add(mntmMethoden[i]);
		}

		mntmProvider = new JMenuItem[Provider3D.getInstances().size()];
		for (int i = 0; i < mntmProvider.length; i++) {
			mntmProvider[i] = new JMenuItem(Provider3D.getInstances().elementAt(i).name);
			mntmProvider[i].setToolTipText(Provider3D.getInstances().elementAt(i).description);
			mntmProvider[i].setEnabled(false);
			mntmProvider[i].addActionListener(this);
			mnShow.add(mntmProvider[i]);
		}

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		pauliDataModel = new PauliDataModel(new PauliViewModel(this));
		evDataModel = new EVDataModel(new EVViewModel(this));

		datamodel = pauliDataModel;
		// datamodel = evDataModel;
		panelModel = datamodel.getViewModel().getPanelModel();
		contentPane.add(panelModel);

		panelResults = new JPanel();
		panelResults.setBorder(null);
		panelResults.setLayout(null);
		contentPane.add(panelResults);
		{
			lblResults = new JLabel("Results");
			panelResults.add(lblResults);

			lblMeasurements = new JLabel("Measurements");
			panelResults.add(lblMeasurements);

			lblIterationsPerMeasurement = new JLabel("Iterations per Measurement");
			panelResults.add(lblIterationsPerMeasurement);

			textFieldMeasurments = new JTextField();
			textFieldMeasurments.setText(String.valueOf(Globals.defaultMeasurements));
			panelResults.add(textFieldMeasurments);
			textFieldMeasurments.setColumns(10);

			textFieldIterations = new JTextField();
			textFieldIterations.setText(String.valueOf(Globals.defaultIterations));
			panelResults.add(textFieldIterations);
			textFieldIterations.setColumns(10);

			for (int i = 0; i < jLThread.length; i++) {
				jLThread[i] = new JTextField();
				jLThread[i].setBackground(Color.GRAY);
				panelResults.add(jLThread[i]);
			}

			btnRun = new JButton("Run");
			btnRun.addActionListener(this);
			panelResults.add(btnRun);

			textAreaResults = new JTextArea();
			textAreaResults.setEditable(false);
			scrollPane = new JScrollPane(textAreaResults);
			panelResults.add(scrollPane);

			lblMessage = new JLabel("Message");
			panelResults.add(lblMessage);
			msgHdl = MsgHandler.getMsgHdl(this.lblMessage);

			btnStop = new JButton("Stop");
			btnStop.setBackground(Color.GRAY);
			btnStop.setForeground(Color.RED);
			btnStop.addActionListener(this);
			btnStop.setVisible(false);
			panelResults.add(btnStop);

			chckbxWriteLog = new JCheckBox("Write Log");
			panelResults.add(chckbxWriteLog);

			chckbxHistory = new JCheckBox("History");
			panelResults.add(chckbxHistory);

			textFieldHistCount = new JTextField();
			textFieldHistCount.setText(String.valueOf(Globals.defaultHistories));
			panelResults.add(textFieldHistCount);
			textFieldHistCount.setColumns(2);

			textFieldHistCount.addActionListener(this);

			progressBarNodes = new JProgressBar();
			progressBarNodes.setVisible(false);
			panelResults.add(progressBarNodes);

			progressBarMsmts = new JProgressBar();
			progressBarMsmts.setVisible(false);
			panelResults.add(progressBarMsmts);

			progressBarIterations = new JProgressBar();
			progressBarIterations.setVisible(false);
			panelResults.add(progressBarIterations);

			lblIterations = new JLabel("Iterations");
			lblIterations.setVisible(false);
			panelResults.add(lblIterations);

			lblMsmts = new JLabel("Msmts");
			lblMsmts.setVisible(false);
			panelResults.add(lblMsmts);

			lblNodes = new JLabel("Nodes");
			lblNodes.setVisible(false);
			panelResults.add(lblNodes);

			sizePResults();
		}
		enableInputFields(true);
		for (int i = 0; i < mntmProvider.length; i++) {
			mntmProvider[i].setEnabled(false);
		}

		setTitle("Simulation using " + datamodel.getMethodSelected());

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnRun) {
			if (btnRun.getText().equals("Run")) {
				runAndControlOnline();
			} else if (btnRun.getText().equals("Run Batch")) {
				runAndControlBatch();
			} else if (btnRun.getText().equals("Pause")) {
				btnRun.setText("Resume");
				RunMeasurementSample.isPaused = true;
				btnStop.setVisible(true);
				msmtHist = rMsmtSmp.getMsmtHistory();
				if (msmtHist != null)
					textAreaResults.append(msmtHist.toString());

			} else if (btnRun.getText().equals("Resume")) {
				btnRun.setText("Pause");
				RunMeasurementSample.isPaused = false;

				btnStop.setVisible(true);

			} else if (btnRun.getText().equals("Pause Batch")) {
				btnRun.setText("Resume Batch");
				RunMeasurementSample.isPaused = true;
				btnStop.setVisible(true);

			} else if (btnRun.getText().equals("Resume Batch")) {
				btnRun.setText("Pause Batch");
				RunMeasurementSample.isPaused = false;

				btnStop.setVisible(true);
			}
		} else if (e.getSource() == btnStop) {
			if (btnStop.getText().equals("Stop")) {
				btnStop.setVisible(false);
				btnRun.setText("Run");
				RunMeasurementSample.isPaused = false;
				RunMeasurementSample.isStopped = true;
				msmtHist = rMsmtSmp.getMsmtHistory();
				if (msmtHist != null)
					textAreaResults.append(msmtHist.toString());
			} else if (btnStop.getText().equals("Stop Batch")) {
				RunMeasurementSample.isPaused = false;
				RunMeasurementSample.isStopped = true;
				btnRun.setText("Run");
				btnStop.setText("Stop");
				btnStop.setVisible(false);
				// enableInputFields(true);
			}
		} else if (e.getSource() == textFieldHistCount) {
			chckbxHistory.setSelected(true);
		} else if (e.getSource() == mntmSave) {
			datamodel.syncDialog2Model();
			datamodel.exportModelToXML();
		} else if (e.getSource() == mntmLoad) {
			ImportXML ixml = new ImportXML();
			DataModel dm = ixml.importModelFromXML();
			if (dm != null && dm != datamodel)
				setDataModel(dm);
			datamodel.syncModel2Dialog();
			setTitle("Simulation using " + datamodel.getMethodSelected());
			datamodel.syncDialog2Model();

		} else if (e.getSource() == mntmLoadBatch) {
			File inFile = new File(Globals.xmlDir);
			if (!inFile.exists())
				inFile = new File("./");

			JFileChooser fc = new JFileChooser(inFile);
			int returnVal = fc.showOpenDialog(RunFlame.frame);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				batchInstance = new RunBatch(fc.getSelectedFile());
				String bmStarted = String.format("batch file selected : %s", fc.getSelectedFile().getPath());
				msgHdl.showError(bmStarted, 0);
				textAreaResults.setText(bmStarted);
				btnRun.setText("Run Batch");
			} else {
				msgHdl.showError("No file selected", 1);
			}

		} else if (e.getSource() == mntmPauli) {
			setDataModel(pauliDataModel);
		} else if (e.getSource() == mntmEV) {
			setDataModel(evDataModel);
		} else if (e.getSource() == mntmViewOptions) {
			Globals.OptionsDialog options = new Globals.OptionsDialog(this, "Process Options", true);
			options.setVisible(true);

		} else if (e.getSource() == mntmAbout) {

			JOptionPane.showMessageDialog(this,
					"<html>" + "<h1>Swarm Dynamics</h1>"
							+ "<p>The software simulates swarm behaviour for P swarms and E swarms<br>"
							+ "as documented in the background documentation.</p>"
							+ "<p>The simulation statistics are displayed. Results and step by step history can get saved for futher analysis.</p>"
							+ "<p>Test cases may be saved and reloaded from XML files.</p>"
							+ "<p>Mass tests are enabled via xml batch files.</p>"
							+ "<p>Various visualizations present the detailed history in 3 dimensional space.</p>"
							+ "<p>&copy; Werner Siegfried Genreith</p>"
							+ "<p>E-Mail <a href=\"mailto:info@bzus.de\">info@bzus.de</a></p>" + "</html>");

		} else if (e.getSource() == mntmBackground) {
			if (Desktop.isDesktopSupported()) {
				try {
					File myFile = new File("./SwarmDynamics.pdf");
					Desktop.getDesktop().open(myFile);
				} catch (IOException ex) {
					// no application registered for PDFs
				}
			}
		} else {
			for (int i = 0; i < mntmMethoden.length; i++) {
				if (e.getSource() == mntmMethoden[i]) {
					datamodel.setMethodSelected(Referenzmethode.getMethodByIndex(i).getText());
					setTitle("Simulation using " + datamodel.getMethodSelected());
					break;
				}
			}
			for (int i = 0; i < mntmProvider.length; i++) {
				if (e.getSource() == mntmProvider[i]) {
					xyzG = new xyzGraph(this);
					xyzG.setExtendedState(MAXIMIZED_BOTH);
					xyzG.addWindowListener(new WindowListener() {

						@Override
						public void windowOpened(WindowEvent e) {
							// TODO Auto-generated method stub

						}

						@Override
						public void windowIconified(WindowEvent e) {
							// TODO Auto-generated method stub

						}

						@Override
						public void windowDeiconified(WindowEvent e) {
							// TODO Auto-generated method stub

						}

						@Override
						public void windowDeactivated(WindowEvent e) {
							// TODO Auto-generated method stub

						}

						@Override
						public void windowClosing(WindowEvent e) {
							if (xyzG.getProvider() != null)
								xyzG.getProvider().stopProvider();
						}

						@Override
						public void windowClosed(WindowEvent e) {
							// TODO Auto-generated method stub

						}

						@Override
						public void windowActivated(WindowEvent e) {
							// TODO Auto-generated method stub

						}
					});

					xyzG.setVisible(true);
					xyzG.showDialogs();
					provider = Provider3D.getInstances().elementAt(i);
					provider.setResultList(msmtHist);
					xyzG.setProvider(provider);
					xyzG.setTitle("Projection using " + provider.name);
					break;
				}
			}

		}
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
		if (e.getSource() == this) {
			panelModel.setBounds(panelModel.getX(), panelModel.getY(), this.getWidth() - 35, panelModel.getHeight());
			datamodel.getViewModel().sizePModel();

			panelResults.setBounds(panelResults.getX(), panelResults.getY(), this.getWidth() - 35,
					this.getHeight() - panelModel.getHeight() - 80);
			sizePResults();
		}

	}

	@Override
	public void componentShown(ComponentEvent e) {
		/*
		 * if (e.getSource() == this) { scrollPane.setBounds(10, 275,
		 * this.getWidth() - 30, this.getHeight() - 350);
		 * tableFlame.setBounds(10, 50, this.getWidth() - 30, 64);
		 * 
		 * // scrollPane.setBounds(10, 275, 420, 360); }
		 */
	}

	private void enableInputFields(boolean e) {
		datamodel.getViewModel().setEnabled(e);
		// pauliDataModel.getViewModel().setEnabled(e);
		// evDataModel.getViewModel().setEnabled(e);

		textFieldMeasurments.setEnabled(e);
		textFieldIterations.setEnabled(e);

		chckbxHistory.setEnabled(e);
		chckbxWriteLog.setEnabled(e);
		textFieldHistCount.setEnabled(e);

		mntmSave.setEnabled(e);
		mntmLoad.setEnabled(e);
		mntmLoadBatch.setEnabled(e);

		mntmEV.setEnabled(e);
		mntmPauli.setEnabled(e);

		for (int i = 0; i < mntmMethoden.length; i++) {
			if (Referenzmethode.getMethodByIndex(i).isCompatibleWith(datamodel.flameType))
				mntmMethoden[i].setEnabled(e);
			else
				mntmMethoden[i].setEnabled(false);
		}

		for (int i = 0; i < mntmProvider.length; i++) {
			if (Provider3D.getMethodByIndex(i).isCompatibleWith(datamodel.flameType))
				mntmProvider[i].setEnabled(e);
			else
				mntmProvider[i].setEnabled(false);
		}

	}

	private void runAndControlBatch() {

		if (batchInstance == null) {
			msgHdl.showError("No batch handler available", 2);
			return;
		}

		btnRun.setText("Pause Batch");
		btnStop.setVisible(true);
		btnStop.setText("Stop Batch");

		RunMeasurementSample.isPaused = false;
		RunMeasurementSample.isStopped = false;

		enableInputFields(false);

		Calendar bmStartTime = Calendar.getInstance();
		String bmMessage = String.format("Batch mode started at %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS ", bmStartTime);

		msgHdl.showError(bmMessage, 0);
		textAreaResults.append("\n" + bmMessage);

		Thread batchInstanceThread = new Thread(batchInstance);
		batchInstanceThread.setName("RunBatch-thread");

		progressBarNodes.setMinimum(0);
		progressBarNodes.setVisible(true);
		lblNodes.setVisible(true);
		progressBarMsmts.setMinimum(0);
		progressBarMsmts.setVisible(true);
		lblMsmts.setVisible(true);
		progressBarIterations.setMinimum(0);
		progressBarIterations.setVisible(true);
		lblIterations.setVisible(true);

		progressBarNodes.setString("Starting");
		progressBarNodes.setStringPainted(true);
		progressBarMsmts.setString("Starting");
		progressBarMsmts.setStringPainted(true);
		progressBarIterations.setString("Starting");
		progressBarIterations.setStringPainted(true);

		timer = new Timer(250, new ActionListener() {

			@Override
			public synchronized void actionPerformed(ActionEvent e) {
				if (batchInstanceThread.isAlive()) {
					progressBarNodes.setMaximum(batchInstance.getNodesCount());
					progressBarNodes.setValue(batchInstance.getNodesCompleted());
					progressBarMsmts.setMaximum(batchInstance.getMsmts());
					progressBarMsmts.setValue(batchInstance.getMsmtsCompleted());
					progressBarIterations.setMaximum(batchInstance.getIterations());
					progressBarIterations.setValue(batchInstance.getIterationsCompleted());

					progressBarNodes.setString(String.format("%d", batchInstance.getNodesCount()));
					progressBarMsmts.setString(String.format("%d", batchInstance.getMsmts()));
					progressBarIterations.setString(String.format("%d", batchInstance.getIterations()));

					String flameType = batchInstance.getFlameType();

					if (flameType != null) {
						if (flameType.equals(Globals.EVModel)) {
							setDataModel(evDataModel);
							enableInputFields(false);
						} else {
							setDataModel(pauliDataModel);
							enableInputFields(false);
						}
					}
					setTitle("Simulation using " + datamodel.getMethodSelected());
					datamodel.setDim(batchInstance.getStateDimension());
					datamodel.setEvswmData(batchInstance.getEvswmData());
					datamodel.setUseObservable(batchInstance.isUseObservable());
					datamodel.setObservableData(batchInstance.getObservableData());
					datamodel.setPerspectiveData(batchInstance.getPerspectiveData());
					datamodel.setHistoryOn(batchInstance.isHistoryOn());
					datamodel.setnHistories(batchInstance.getnHistories());
					datamodel.setMsmts(batchInstance.getMsmts());
					datamodel.setIter(batchInstance.getIterations());
					datamodel.setMethodSelected(batchInstance.getMethodSelected());
					if (batchInstance.getRandomize() > 0) {
						datamodel.setRandMax(batchInstance.getRandMax());
						datamodel.setRandMin(batchInstance.getRandMin());
					}
					datamodel.syncModel2Dialog();

					for (int i = 0; i < jLThread.length; i++)
						if (RunMeasurementSample.isPaused)
							jLThread[i].setBackground(Color.YELLOW);
						else if (MsmtThreadPool.getInstance().isRunning(i))
							jLThread[i].setBackground(Color.GREEN);
						else
							jLThread[i].setBackground(Color.RED);
				} else {
					progressBarNodes.setVisible(false);
					lblNodes.setVisible(false);
					progressBarMsmts.setVisible(false);
					lblMsmts.setVisible(false);
					progressBarIterations.setVisible(false);
					lblIterations.setVisible(false);
					textAreaResults.append("\n" + msgHdl.getActualMessage());
					timer.stop();
					for (int i = 0; i < jLThread.length; i++)
						jLThread[i].setBackground(Color.GRAY);

					btnRun.setText("Run");
					btnStop.setVisible(false);
					btnStop.setText("Stop");
					enableInputFields(true);

					textAreaResults.append("\nPlease view results in log files:");
					textAreaResults.append("\nErrorLog=" + Logging.getErrorLog().toString());
					if (loggingEnabled)
						textAreaResults.append("\nBatchLog=" + Logging.getBatchLog().toString());
					if (historyLoggingEnabled)
						textAreaResults.append("\nHistoryLog=" + Logging.getHistoryLog().toString());

				}
				;
			}
		});

		try {
			batchInstanceThread.start();
			Thread.sleep(100);
			timer.start();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

	}

	private void runAndControlOnline() {

		btnRun.setText("Pause");
		btnStop.setVisible(true);
		RunMeasurementSample.isPaused = false;
		enableInputFields(false);
		loggingEnabled = chckbxWriteLog.isSelected();
		historyLoggingEnabled = chckbxHistory.isSelected();

		datamodel.syncDialog2Model();
		datamodel.syncModel2Dialog();

		progressBarMsmts.setMinimum(0);
		progressBarMsmts.setVisible(true);
		lblMsmts.setVisible(true);
		progressBarIterations.setMinimum(0);
		progressBarIterations.setVisible(true);
		lblIterations.setVisible(true);

		progressBarMsmts.setString("Starting");
		progressBarMsmts.setStringPainted(true);
		progressBarIterations.setString("Starting");
		progressBarIterations.setStringPainted(true);

		Calendar bmStartTime = Calendar.getInstance();
		String bmStarted = String.format("measurements started at %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS ", bmStartTime);
		msgHdl.showError(bmStarted, 0);

		InitThread iT = new InitThread();
		Thread initThread = new Thread(iT);
		initThread.setName("Run Iterations");

		timer = new Timer(250, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (initThread.isAlive()) {
					iT.updateThreadBar();
					progressBarMsmts.setMaximum(iT.getMsmts());
					progressBarMsmts.setValue(iT.getMsmtsCompleted());
					progressBarIterations.setMaximum(iT.getIterations());
					progressBarIterations.setValue(iT.getIterationsCompleted());

					progressBarMsmts.setString(String.format("%d", iT.getMsmts()));
					progressBarIterations.setString(String.format("%d", iT.getIterations()));

				} else {
					iT.updateThreadBar();
					progressBarMsmts.setVisible(false);
					lblMsmts.setVisible(false);
					progressBarIterations.setVisible(false);
					lblIterations.setVisible(false);
					msgHdl.showError(String.format("measurements completed at %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS ",
							Calendar.getInstance()), 0);
					timer.stop();
					for (int i = 0; i < jLThread.length; i++)
						jLThread[i].setBackground(Color.GRAY);
					enableInputFields(true);
					btnRun.setText("Run");
					btnStop.setVisible(false);
				}
				;
			}
		});

		initThread.start();
		try {
			Thread.sleep(100);
			timer.start();
		} catch (InterruptedException e1) {
			msgHdl.showError("Thread interrupted: " + e1, 3);
		}
	}

	private void setDataModel(DataModel dm) {
		if (datamodel.getViewModel() == null
				|| !datamodel.getViewModel().getClass().equals(dm.getViewModel().getClass())) {
			contentPane.removeAll();

			datamodel = dm;
			panelModel = datamodel.getViewModel().getPanelModel();
			datamodel.getViewModel().sizePModel();
			contentPane.add(panelModel);
			contentPane.add(panelResults);
			sizePResults();
			enableInputFields(true);
			for (int i = 0; i < mntmProvider.length; i++) {
				mntmProvider[i].setEnabled(false);
			}
			setTitle("Simulation using " + datamodel.getMethodSelected());
			contentPane.repaint();
		}
	}

	private void sizePResults() {

		panelResults.setBounds(panelModel.getX(), panelModel.getY() + panelModel.getHeight(), this.getWidth() - 40,
				this.getHeight() - panelModel.getHeight() - 100);

		lblMeasurements.setBounds(0, 7, 100, 15);
		textFieldMeasurments.setBounds(0, 27, 50, 15);

		lblIterationsPerMeasurement.setBounds(200, 7, 220, 15);
		textFieldIterations.setBounds(200, 27, 50, 15);
		for (int i = 0; i < jLThread.length; i++) {
			jLThread[i].setBounds(10 * i, 47, 10, 10);
		}

		btnRun.setBounds(panelResults.getWidth() - 120, 26, 120, 23);

		chckbxWriteLog.setBounds(panelResults.getWidth() - 120, 82, 97, 23);
		chckbxHistory.setBounds(panelResults.getWidth() - 120, 107, 69, 23);
		textFieldHistCount.setBounds(panelResults.getWidth() - 40, 109, 38, 17);
		btnStop.setBounds(panelResults.getWidth() - 120, 137, 120, 23);

		lblIterations.setBounds(0, 107, 60, 14);
		progressBarIterations.setBounds(65, 107, 128, 14);
		lblMsmts.setBounds(0, 127, 60, 14);
		progressBarMsmts.setBounds(65, 127, 256, 14);
		lblNodes.setBounds(0, 147, 60, 14);
		progressBarNodes.setBounds(65, 147, 512, 14);

		lblMessage.setBounds(0, 162, panelResults.getWidth(), 26);
		lblResults.setBounds(0, 188, 46, 14);
		scrollPane.setBounds(0, 202, panelResults.getWidth(), panelResults.getHeight() - 202);

	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
	}
}
