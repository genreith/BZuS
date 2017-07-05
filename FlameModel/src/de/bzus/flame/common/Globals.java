package de.bzus.flame.common;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * @author Werner Siegfried Genreith
 *
 */
public class Globals {

	public static String[] MethodMenuEntries = { "de.bzus.flame.process.Referenzmethode" };
	public static String[] ShowMenuEntries = { "de.bzus.flame.provider.ProviderHistory",
			"de.bzus.flame.provider.ProviderFlame", "de.bzus.flame.provider.ProviderFlat",
			"de.bzus.flame.provider.ProviderRiemann" };

	public static class OptionsDialog extends JDialog implements ActionListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		JButton btnCancel;
		JButton btnDefault;
		JButton btnOK;
		JButton btnSave;
		int debugLevel = Globals.debugLevel;
		int defaultDimension = Globals.defaultDimension;
		int defaultHistories = Globals.defaultHistories;
		int defaultIterations = Globals.defaultIterations;
		int defaultMeasurements = Globals.defaultMeasurements;
		int defaultRandMax = Globals.defaultRandMax;
		int defaultRandMin = Globals.defaultRandMin;
		boolean defaultUseObservable = Globals.defaultUseObservable;
		String iniFileName = Globals.iniFileName;

		JLabel lbldefaultDimension;
		JLabel lbldebugLevel;
		JLabel lbldefaultHistories;
		JLabel lbldefaultIterations;
		JLabel lbldefaultMeasurements;
		JLabel lbldefaultRandMax;
		JLabel lbldefaultRandMin;
		JLabel lbldefaultUseObservable;
		JLabel lbliniFileName;
		JLabel lbllogDir;
		JLabel lblmaxFullHistories;
		JLabel lblmaxSwarmSize;
		JLabel lblreseedCounter;
		JLabel lblminimalExcitement;
		JLabel lblnoOfBatchFileThreads;
		JLabel lblxmlDir;
		String logDir = Globals.logDir;
		int maxFullHistories = Globals.maxFullHistories;
		int maxSwarmSize = Globals.maxSwarmSize;
		long reseedCounter = Globals.reseedCounter;
		JLabel message;
		double minimalExcitement = Globals.minimalExcitement;
		int noOfBatchFileThreads = Globals.noOfBatchFileThreads;
		JTextField txtdebugLevel;
		JTextField txtdefaultDimension;

		JTextField txtdefaultHistories;
		JTextField txtdefaultIterations;

		JTextField txtdefaultMeasurements;

		JTextField txtdefaultRandMax;
		JTextField txtdefaultRandMin;
		JTextField txtdefaultUseObservable;
		JTextField txtiniFileName;
		JTextField txtlogDir;
		JTextField txtmaxFullHistories;
		JTextField txtmaxSwarmSize;
		JTextField txtreseedCounter;
		JTextField txtminimalExcitement;
		JTextField txtnoOfBatchFileThreads;
		JTextField txtxmlDir;
		String xmlDir = Globals.xmlDir;

		public OptionsDialog(JFrame jf, String title, boolean modal) {
			super(jf, title, modal);

			lblnoOfBatchFileThreads = new JLabel("noOfThreads");
			lblminimalExcitement = new JLabel("minimalExcitement");
			lblmaxFullHistories = new JLabel("maxFullHistories");
			lblmaxSwarmSize = new JLabel("maxSwarmSize");
			lblreseedCounter = new JLabel("reseedCounter");
			lbldefaultRandMin = new JLabel("defaultRandMin");
			lbldefaultRandMax = new JLabel("defaultRandMax");
			lbldefaultMeasurements = new JLabel("defaultMeasurements");
			lbldefaultIterations = new JLabel("defaultIterations");
			lbldefaultDimension = new JLabel("defaultDimension");
			lbldebugLevel = new JLabel("debug level");
			lbldefaultHistories = new JLabel("defaultHistories");
			lbldefaultUseObservable = new JLabel("defaultUseObservable");
			lbllogDir = new JLabel("Log directory");
			lblxmlDir = new JLabel("XML directory");

			lbliniFileName = new JLabel("INI File");

			txtnoOfBatchFileThreads = new JTextField(String.valueOf(Globals.noOfBatchFileThreads));
			txtminimalExcitement = new JTextField(String.valueOf(Globals.minimalExcitement));
			txtmaxFullHistories = new JTextField(String.valueOf(Globals.maxFullHistories));
			txtmaxSwarmSize = new JTextField(String.valueOf(Globals.maxSwarmSize));
			txtreseedCounter = new JTextField(String.valueOf(Globals.reseedCounter));
			txtdefaultRandMin = new JTextField(String.valueOf(Globals.defaultRandMin));
			txtdefaultRandMax = new JTextField(String.valueOf(Globals.defaultRandMax));
			txtdefaultMeasurements = new JTextField(String.valueOf(Globals.defaultMeasurements));
			txtdefaultIterations = new JTextField(String.valueOf(Globals.defaultIterations));
			txtdebugLevel = new JTextField(String.valueOf(Globals.debugLevel));
			txtdefaultDimension = new JTextField(String.valueOf(Globals.defaultDimension));
			txtdebugLevel = new JTextField(String.valueOf(Globals.debugLevel));
			txtdefaultHistories = new JTextField(String.valueOf(Globals.defaultHistories));
			txtdefaultUseObservable = new JTextField(String.valueOf(Globals.defaultUseObservable));
			txtlogDir = new JTextField(String.valueOf(Globals.logDir));
			txtxmlDir = new JTextField(String.valueOf(Globals.xmlDir));

			txtiniFileName = new JTextField(String.valueOf(Globals.iniFileName));
			txtiniFileName.setEditable(false);

			// message = new JLabel("restart to activate threads count or
			// seedTimer");
			message = new JLabel("");
			message.setForeground(Color.MAGENTA);

			btnOK = new JButton("OK");
			btnSave = new JButton("Save");
			btnCancel = new JButton("Cancel");
			btnDefault = new JButton("Defaults");
			btnSave.addActionListener(this);
			btnOK.addActionListener(this);
			btnCancel.addActionListener(this);
			btnDefault.addActionListener(this);

			lblnoOfBatchFileThreads.setBounds(10, 10, 150, 14);
			txtnoOfBatchFileThreads.setBounds(165, 10, 50, 14);
			lblminimalExcitement.setBounds(10, 30, 150, 14);
			txtminimalExcitement.setBounds(165, 30, 50, 14);
			lblmaxFullHistories.setBounds(10, 50, 150, 14);
			txtmaxFullHistories.setBounds(165, 50, 50, 14);
			lblmaxSwarmSize.setBounds(10, 70, 150, 14);
			txtmaxSwarmSize.setBounds(165, 70, 100, 14);
			lblreseedCounter.setBounds(10, 90, 150, 14);
			txtreseedCounter.setBounds(165, 90, 100, 14);
			lbldebugLevel.setBounds(10, 110, 150, 14);
			txtdebugLevel.setBounds(165, 110, 100, 14);
			lbllogDir.setBounds(10, 130, 150, 14);
			txtlogDir.setBounds(165, 130, 100, 14);
			lblxmlDir.setBounds(10, 150, 150, 14);
			txtxmlDir.setBounds(165, 150, 100, 14);

			lbldefaultIterations.setBounds(10, 180, 150, 14);
			txtdefaultIterations.setBounds(165, 180, 100, 14);
			lbldefaultMeasurements.setBounds(10, 200, 150, 14);
			txtdefaultMeasurements.setBounds(165, 200, 100, 14);
			lbldefaultRandMax.setBounds(10, 220, 150, 14);
			txtdefaultRandMax.setBounds(165, 220, 100, 14);
			lbldefaultRandMin.setBounds(10, 240, 150, 14);
			txtdefaultRandMin.setBounds(165, 240, 100, 14);
			lbldefaultUseObservable.setBounds(10, 260, 150, 14);
			txtdefaultUseObservable.setBounds(165, 260, 100, 14);
			lbldefaultHistories.setBounds(10, 280, 150, 14);
			txtdefaultHistories.setBounds(165, 280, 100, 14);
			lbldefaultDimension.setBounds(10, 300, 150, 14);
			txtdefaultDimension.setBounds(165, 300, 100, 14);

			message.setBounds(10, 320, 350, 15);

			btnCancel.setBounds(10, 340, 80, 20);
			btnOK.setBounds(100, 340, 60, 20);
			btnSave.setBounds(170, 340, 80, 20);
			btnDefault.setBounds(260, 340, 90, 20);

			setSize(390, 430);

			getContentPane().setLayout(null);
			add(lblnoOfBatchFileThreads);
			add(lblminimalExcitement);
			add(lblmaxFullHistories);
			add(lblmaxSwarmSize);
			add(lblreseedCounter);
			add(lbldefaultRandMin);
			add(lbldefaultRandMax);
			add(lbldefaultMeasurements);
			add(lbldefaultIterations);
			add(lbldefaultDimension);
			add(lbldefaultHistories);
			add(lbldebugLevel);
			add(lbldefaultUseObservable);
			add(lbllogDir);
			add(lblxmlDir);
			add(txtnoOfBatchFileThreads);
			add(txtminimalExcitement);
			add(txtmaxFullHistories);
			add(txtmaxSwarmSize);
			add(txtreseedCounter);
			add(txtdefaultRandMin);
			add(txtdefaultRandMax);
			add(txtdefaultMeasurements);
			add(txtdefaultIterations);
			add(txtdefaultDimension);
			add(txtdefaultHistories);
			add(txtdebugLevel);
			add(txtdefaultUseObservable);
			add(txtlogDir);
			add(txtxmlDir);
			add(message);
			add(btnOK);
			add(btnSave);
			add(btnCancel);
			add(btnDefault);

		}

		@Override
		public void actionPerformed(ActionEvent e) {

			boolean inputOK = true;
			if (e.getSource().equals(btnSave) || e.getSource().equals(btnOK)) {
				try {
					noOfBatchFileThreads = Integer.valueOf(txtnoOfBatchFileThreads.getText());
				} catch (NumberFormatException e1) {
					message.setText("Invalid noOfBatchFileThreads :" + txtnoOfBatchFileThreads.getText());
					inputOK = false;
				}
				try {
					minimalExcitement = Double.valueOf(txtminimalExcitement.getText());
				} catch (NumberFormatException e1) {
					message.setText("Invalid minimalExcitement :" + txtminimalExcitement.getText());
					inputOK = false;
				}
				try {
					maxFullHistories = Integer.valueOf(txtmaxFullHistories.getText());
				} catch (NumberFormatException e1) {
					message.setText("Invalid maxFullHistories :" + txtmaxFullHistories.getText());
					inputOK = false;
				}
				try {
					maxSwarmSize = Integer.valueOf(txtmaxSwarmSize.getText());
				} catch (NumberFormatException e1) {
					message.setText("Invalid maxSwarmSize :" + txtmaxSwarmSize.getText());
					inputOK = false;
				}
				try {
					reseedCounter = Integer.valueOf(txtreseedCounter.getText());
				} catch (NumberFormatException e1) {
					message.setText("Invalid reseedCounter :" + txtreseedCounter.getText());
					inputOK = false;
				}
				try {
					defaultRandMin = Integer.valueOf(txtdefaultRandMin.getText());
				} catch (NumberFormatException e1) {
					message.setText("Invalid defaultRandMin :" + txtdefaultRandMin.getText());
					inputOK = false;
				}
				try {
					defaultRandMax = Integer.valueOf(txtdefaultRandMax.getText());
				} catch (NumberFormatException e1) {
					message.setText("Invalid defaultRandMax :" + txtdefaultRandMax.getText());
					inputOK = false;
				}
				try {
					defaultMeasurements = Integer.valueOf(txtdefaultMeasurements.getText());
				} catch (NumberFormatException e1) {
					message.setText("Invalid defaultMeasurements :" + txtdefaultMeasurements.getText());
					inputOK = false;
				}
				try {
					defaultIterations = Integer.valueOf(txtdefaultIterations.getText());
				} catch (NumberFormatException e1) {
					message.setText("Invalid defaultIterations :" + txtdefaultIterations.getText());
					inputOK = false;
				}
				try {
					debugLevel = Integer.valueOf(txtdebugLevel.getText());
				} catch (NumberFormatException e1) {
					message.setText("Invalid debugLevel :" + txtdebugLevel.getText());
					inputOK = false;
				}
				try {
					defaultDimension = Integer.valueOf(txtdefaultDimension.getText());
				} catch (NumberFormatException e1) {
					message.setText("Invalid defaultDimension :" + txtdefaultDimension.getText());
					inputOK = false;
				}
				try {
					debugLevel = Integer.valueOf(txtdebugLevel.getText());
				} catch (NumberFormatException e1) {
					message.setText("Invalid debugLevel :" + txtdebugLevel.getText());
					inputOK = false;
				}
				try {
					defaultHistories = Integer.valueOf(txtdefaultHistories.getText());
				} catch (NumberFormatException e1) {
					message.setText("Invalid defaultHistories :" + txtdefaultHistories.getText());
					inputOK = false;
				}
				try {
					defaultUseObservable = Boolean.valueOf(txtdefaultUseObservable.getText());
				} catch (NumberFormatException e1) {
					message.setText("Invalid defaultUseObservable :" + txtdefaultUseObservable.getText());
					inputOK = false;
				}

				iniFileName = txtiniFileName.getText();

				if (inputOK) {
					if (noOfBatchFileThreads > 0)
						Globals.noOfBatchFileThreads = noOfBatchFileThreads;
					if (minimalExcitement < 1.00 && minimalExcitement > 0.0)
						Globals.minimalExcitement = minimalExcitement;
					if (maxFullHistories > 0)
						Globals.maxFullHistories = maxFullHistories;
					if (maxSwarmSize > 100)
						Globals.maxSwarmSize = maxSwarmSize;
					if (reseedCounter > 0)
						Globals.reseedCounter = reseedCounter;
					if (defaultRandMin > 0)
						Globals.defaultRandMin = defaultRandMin;
					else
						Globals.defaultRandMin = 1;
					if (defaultRandMax >= defaultRandMin)
						Globals.defaultRandMax = defaultRandMax;
					else
						Globals.defaultRandMax = defaultRandMin;
					if (defaultMeasurements > 0)
						Globals.defaultMeasurements = defaultMeasurements;
					if (defaultIterations >= 0)
						Globals.defaultIterations = defaultIterations;
					if (defaultDimension > 1)
						Globals.defaultDimension = defaultDimension;
					if (defaultHistories >= 0)
						Globals.defaultHistories = defaultHistories;
					Globals.debugLevel = debugLevel;
					Globals.defaultUseObservable = defaultUseObservable;
					Globals.logDir = logDir;
					Globals.xmlDir = xmlDir;

					if (e.getSource().equals(btnSave)) {

						if (saveINIFile(iniFileName))
							dispose();
						else
							message.setText("options not written to " + Globals.iniFileName);

					} else {
						dispose();
					}
				}
			} else if (e.getSource().equals(btnDefault)) {
				loadDefaults();
				txtnoOfBatchFileThreads.setText(String.valueOf(noOfBatchFileThreads));
				txtminimalExcitement.setText(String.valueOf(minimalExcitement));
				txtmaxFullHistories.setText(String.valueOf(maxFullHistories));
				txtmaxSwarmSize.setText(String.valueOf(maxSwarmSize));
				txtdefaultRandMin.setText(String.valueOf(defaultRandMin));
				txtdefaultRandMax.setText(String.valueOf(defaultRandMax));
				txtdefaultMeasurements.setText(String.valueOf(defaultMeasurements));
				txtdefaultIterations.setText(String.valueOf(defaultIterations));
				txtdefaultDimension.setText(String.valueOf(defaultDimension));
				txtdefaultHistories.setText(String.valueOf(defaultHistories));
				txtdefaultUseObservable.setText(String.valueOf(defaultUseObservable));
				txtiniFileName.setText(String.valueOf(iniFileName));
				txtreseedCounter.setText(String.valueOf(reseedCounter));
				txtlogDir.setText(String.valueOf(logDir));
				txtxmlDir.setText(String.valueOf(xmlDir));
				txtdebugLevel.setText(String.valueOf(debugLevel));

			} else if (e.getSource().equals(btnCancel)) {
				dispose();
			}
		}

		public void loadDefaults() {
			iniFileName = "FlameModel.ini";
			maxFullHistories = 100;
			maxSwarmSize = 1000000;
			reseedCounter = 10000000;
			defaultRandMin = 1000;
			defaultRandMax = 10000;
			defaultMeasurements = 100;
			defaultIterations = 500;
			defaultDimension = 3;
			debugLevel = 1;
			defaultHistories = 10;
			defaultUseObservable = true;
			minimalExcitement = 0.0001;
			noOfBatchFileThreads = Integer.valueOf(System.getenv("NUMBER_OF_PROCESSORS"));
			logDir = "./log";
			xmlDir = "./xml";
		}

	}

	public static int debugLevel = 1;
	public static int defaultDimension = 3;

	public static int defaultHistories = 10;

	public static int defaultIterations = 500;
	public static int defaultMeasurements = 100;

	public static int defaultRandMax = 10000;

	public static int defaultRandMin = 1000;

	public static boolean defaultUseObservable = true;

	public static final String EVModel = "EVModel";

	public static String iniFileName = "FlameModel.ini";

	public static String logDir = "./log";
	public static int maxFullHistories = 100;
	public static int maxSwarmSize = 1000000;
	public static long reseedCounter = 10000000;
	public static double minimalExcitement = 0.0001;
	public static int noOfBatchFileThreads = Integer.valueOf(System.getenv("NUMBER_OF_PROCESSORS"));
	public static final String PauliModel = "PauliModel";
	public static String xmlDir = "./xml";

	public static String getString() {
		return "\nGlobals: debugLevel=" + debugLevel + "; maxSwarmSize=" + maxSwarmSize + "; reseedCounter="
				+ reseedCounter + "; minimalExcitement=" + minimalExcitement + "; defaultRandMin=" + defaultRandMin
				+ "; defaultRandMax=" + defaultRandMax + "; defaultMeasurements=" + defaultMeasurements
				+ "; defaultIterations=" + defaultIterations + "; defaultDimension=" + defaultDimension
				+ "; defaultHistories=" + defaultHistories + "; defaultUseObservable=" + defaultUseObservable
				+ "; log directory=" + logDir + "; script directory=" + xmlDir;
	}

	public static void loadINIFile(String iniFileName) {
		Properties prop = new Properties();
		try {

			prop.load(new FileInputStream(iniFileName));

			if (prop.getProperty("debugLevel") != null)
				debugLevel = Integer.valueOf(prop.getProperty("debugLevel"));
			if (prop.getProperty("iniFileName") != null)
				iniFileName = prop.getProperty("iniFileName");
			if (prop.getProperty("noOfBatchFileThreads") != null)
				noOfBatchFileThreads = Integer.valueOf(prop.getProperty("noOfBatchFileThreads"));
			if (prop.getProperty("minimalExcitement") != null)
				minimalExcitement = Double.valueOf(prop.getProperty("minimalExcitement"));
			if (prop.getProperty("maxFullHistories") != null)
				maxFullHistories = Integer.valueOf(prop.getProperty("maxFullHistories"));
			if (prop.getProperty("maxSwarmSize") != null)
				maxSwarmSize = Integer.valueOf(prop.getProperty("maxSwarmSize"));
			if (prop.getProperty("reseedCounter") != null)
				reseedCounter = Integer.valueOf(prop.getProperty("reseedCounter"));
			if (prop.getProperty("defaultRandMin") != null)
				defaultRandMin = Integer.valueOf(prop.getProperty("defaultRandMin"));
			if (prop.getProperty("defaultRandMax") != null)
				defaultRandMax = Integer.valueOf(prop.getProperty("defaultRandMax"));
			if (prop.getProperty("defaultMeasurements") != null)
				defaultMeasurements = Integer.valueOf(prop.getProperty("defaultMeasurements"));
			if (prop.getProperty("defaultIterations") != null)
				defaultIterations = Integer.valueOf(prop.getProperty("defaultIterations"));
			if (prop.getProperty("defaultDimension") != null)
				defaultDimension = Integer.valueOf(prop.getProperty("defaultDimension"));
			if (prop.getProperty("defaultHistories") != null)
				defaultHistories = Integer.valueOf(prop.getProperty("defaultHistories"));
			if (prop.getProperty("defaultUseObservable") != null)
				defaultUseObservable = Boolean.valueOf(prop.getProperty("defaultUseObservable"));

			if (prop.getProperty("logDir") != null)
				logDir = String.valueOf(prop.getProperty("logDir"));
			if (prop.getProperty("xmlDir") != null)
				xmlDir = String.valueOf(prop.getProperty("xmlDir"));

			if (prop.getProperty("burningMethods") != null)
				MethodMenuEntries = prop.getProperty("burningMethods").split(",");
			if (prop.getProperty("provider3D") != null)
				ShowMenuEntries = prop.getProperty("provider3D").split(",");

		} catch (NumberFormatException e) {
			System.out.println("invalid number format in " + iniFileName + "; Defaults taken");
		} catch (IOException e) {
			System.out.println("INI " + iniFileName + " not found; Defaults taken");
		}
	}

	public static boolean saveINIFile(String iniFileName) {
		Properties prop = new Properties();
		if (Globals.noOfBatchFileThreads > 0)
			prop.setProperty("noOfBatchFileThreads", String.valueOf(Globals.noOfBatchFileThreads));
		if (Globals.minimalExcitement < 1.00)
			prop.setProperty("minimalExcitement", String.valueOf(Globals.minimalExcitement));
		if (Globals.maxFullHistories > 0)
			prop.setProperty("maxFullHistories", String.valueOf(Globals.maxFullHistories));
		if (Globals.maxSwarmSize > 100)
			prop.setProperty("maxSwarmSize", String.valueOf(Globals.maxSwarmSize));
		if (Globals.reseedCounter > 0)
			prop.setProperty("reseedCounter", String.valueOf(Globals.reseedCounter));
		prop.setProperty("logDir", String.valueOf(Globals.logDir));
		prop.setProperty("xmlDir", String.valueOf(Globals.xmlDir));
		prop.setProperty("debugLevel", String.valueOf(Globals.debugLevel));

		if (Globals.defaultRandMin >= 0)
			prop.setProperty("defaultRandMin", String.valueOf(Globals.defaultRandMin));
		if (Globals.defaultRandMax >= Globals.defaultRandMin)
			prop.setProperty("defaultRandMax", String.valueOf(Globals.defaultRandMax));
		else
			prop.setProperty("defaultRandMax", String.valueOf(Globals.defaultRandMin));
		if (Globals.defaultMeasurements > 0)
			prop.setProperty("defaultMeasurements", String.valueOf(Globals.defaultMeasurements));
		if (Globals.defaultIterations >= 0)
			prop.setProperty("defaultIterations", String.valueOf(Globals.defaultIterations));
		if (Globals.defaultDimension > 1)
			prop.setProperty("defaultDimension", String.valueOf(Globals.defaultDimension));
		if (Globals.defaultHistories > 0)
			prop.setProperty("defaultHistories", String.valueOf(Globals.defaultHistories));

		prop.setProperty("defaultUseObservable", String.valueOf(Globals.defaultUseObservable));

		if (Globals.MethodMenuEntries != null && Globals.MethodMenuEntries.length > 0) {
			StringBuffer mme = new StringBuffer(Globals.MethodMenuEntries[0]);
			for (int i = 1; i < Globals.MethodMenuEntries.length; i++)
				mme.append("," + Globals.MethodMenuEntries[i]);
			prop.setProperty("burningMethods", mme.toString());
		}
		if (Globals.ShowMenuEntries != null && Globals.ShowMenuEntries.length > 0) {
			StringBuffer mme = new StringBuffer(Globals.ShowMenuEntries[0]);
			for (int i = 1; i < Globals.ShowMenuEntries.length; i++)
				mme.append("," + Globals.ShowMenuEntries[i]);
			prop.setProperty("provider3D", mme.toString());
		}
		try {
			prop.store(new FileWriter(Globals.iniFileName), "Options written by Flame Options Dialog");
			return true;
		} catch (IOException e1) {
			return false;
		}

	}

	public static String toCSV(char separator) {
		return "Globals:" + separator + "debugLevel=" + debugLevel + separator + "maxSwarmSize=" + maxSwarmSize
				+ separator + "reseedCounter=" + reseedCounter + separator + "minimalExcitement=" + minimalExcitement
				+ separator + "logDir=" + logDir + separator + "xmlDir=" + xmlDir + separator + "maxFullHistories="
				+ maxFullHistories + separator + "noOfBatchFileThreads=" + noOfBatchFileThreads + separator
				+ "iniFilename=" + iniFileName + separator + "defaultRandMin=" + defaultRandMin + separator
				+ "defaultRandMax=" + defaultRandMax + separator + "defaultMeasurements=" + defaultMeasurements
				+ separator + "defaultIterations=" + defaultIterations + separator + "defaultDimension="
				+ defaultDimension + separator + "defaultHistories=" + defaultHistories + separator
				+ "defaultUseObservable=" + defaultUseObservable;
	}

}
