package de.bzus.graph3d;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Presentation provides a dialog to set various parameters that determine
 * display and timing of the 2 dimensional model.
 * 
 * @author Werner Siegfried Genreith
 */
class Presentation extends javax.swing.JDialog implements ActionListener,
		ChangeListener, KeyListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5765816176323426894L;
	private JLabel jLabelMaxIteration;
	private JLabel jLabelStep;
	private JCheckBox jCheckBoxShowAxes;
	private JButton jButtonOK;
	private JButton jButtonCancel;
	private JButton jButtonDefaults;
	private JCheckBox jCheckBoxShowNodes;
	private JCheckBox jCheckBoxShowLines;
	private JTextField jTextFieldMaxIteration;
	private JTextField jTextFieldStep;
	private JButton btnXColor;
	private JButton btnYColor;
	private JButton btnZColor;

	private xyzGraph callingFrame = null;

	private Color bkgrdColor = Color.WHITE;
	private Color xColor = Color.BLUE;
	private Color yColor = Color.GREEN;
	private Color zColor = Color.RED;
	private int maxIteration = 50;
	private int step = 50;
	private long interval = 50;

	private boolean showLines = true;
	private boolean showPoints = true;
	private boolean showAxes = true;;
	private JLabel jLabel2;
	private JTextField jTextFieldInterval;
	private JLabel jLabel1;
	private JLabel jLabelSpeed;
	private JSlider jSliderInterval;
	private JButton btnBackground;
	private JTextArea lblProvider;

	/**
	 * Auto-generated main method to display this JDialog
	 */
	private void getOptions() {
		DarstellungsRaum.Options options = callingFrame.darstellungsRaum
				.getOptions();

		bkgrdColor = options.bkgrdColor;
		xColor = options.xColor;
		yColor = options.yColor;
		zColor = options.zColor;
		maxIteration = options.displayPathLen;
		interval = options.getInterval();
		step = options.step;

		showLines = options.showLines;
		showPoints = options.showPoints;
		showAxes = options.showAxes;
	}

	private void getDefaultOptions() {
		DarstellungsRaum.Options defaultOptions = new DarstellungsRaum.Options();

		bkgrdColor = defaultOptions.bkgrdColor;
		xColor = defaultOptions.xColor;
		yColor = defaultOptions.yColor;
		zColor = defaultOptions.zColor;
		maxIteration = defaultOptions.displayPathLen;
		interval = defaultOptions.getInterval();
		step = defaultOptions.step;

		showLines = defaultOptions.showLines;
		showPoints = defaultOptions.showPoints;
		showAxes = defaultOptions.showAxes;
	}

	private void setOptions() {
		DarstellungsRaum.Options options = callingFrame.darstellungsRaum
				.getOptions();

		options.bkgrdColor = bkgrdColor;
		options.xColor = xColor;
		options.yColor = yColor;
		options.zColor = zColor;
		options.displayPathLen = maxIteration;
		options.step = step;
		options.setInterval(interval);

		options.showLines = showLines;
		options.showPoints = showPoints;
		options.showAxes = showAxes;

	}

	public Presentation(JFrame frame) {
		super(frame);
		callingFrame = (xyzGraph) frame;
		setPreferredSize(new Dimension(300, 450));

		getOptions();
		initGUI();
	}

	@Override
	public void setVisible(boolean sv) {
		super.setVisible(sv);
		getOptions();
	}

	private void initGUI() {
		try {
			setTitle("Presentation");
			{
				getContentPane().setLayout(null);
				{
					jLabelStep = new JLabel();
					getContentPane().add(jLabelStep);
					jLabelStep.setText("Step");
					jLabelStep.setBounds(12, 12, 37, 16);
				}
				{
					jTextFieldStep = new JTextField();
					getContentPane().add(jTextFieldStep);
					jTextFieldStep.addKeyListener(this);
					jTextFieldStep.setEnabled(false);
					jTextFieldStep.setBounds(50, 12, 70, 16);
				}
				{
					jCheckBoxShowAxes = new JCheckBox();
					getContentPane().add(jCheckBoxShowAxes);
					jCheckBoxShowAxes.setText("Show Axes");
					jCheckBoxShowAxes.addActionListener(this);
					jCheckBoxShowAxes.setBounds(160, 12, 111, 16);
				}
				{
					jLabelMaxIteration = new JLabel();
					getContentPane().add(jLabelMaxIteration);
					jLabelMaxIteration.setText("Display Path Length");
					jLabelMaxIteration.setBounds(12, 45, 147, 16);
				}
				{
					jTextFieldMaxIteration = new JTextField();
					getContentPane().add(jTextFieldMaxIteration);
					jTextFieldMaxIteration.addKeyListener(this);
					jTextFieldMaxIteration.setBounds(160, 45, 70, 16);
				}
				{
					jCheckBoxShowLines = new JCheckBox();
					getContentPane().add(jCheckBoxShowLines);
					jCheckBoxShowLines.setText("Show Lines");
					jCheckBoxShowLines.addActionListener(this);
					jCheckBoxShowLines.setBounds(12, 77, 100, 20);
				}
				{
					jCheckBoxShowNodes = new JCheckBox();
					getContentPane().add(jCheckBoxShowNodes);
					jCheckBoxShowNodes.setText("Show Nodes");
					jCheckBoxShowNodes.addActionListener(this);
					jCheckBoxShowNodes.setBounds(12, 110, 100, 20);
				}
				{
					jButtonCancel = new JButton();
					getContentPane().add(jButtonCancel);
					jButtonCancel.setText("Cancel");
					jButtonCancel.addActionListener(this);
					jButtonCancel.setBounds(178, 227, 81, 34);
				}
				{
					jButtonOK = new JButton();
					getContentPane().add(jButtonOK);
					jButtonOK.setText("OK");
					jButtonOK.addActionListener(this);
					jButtonOK.setBounds(118, 227, 55, 34);
				}
				{
					jButtonDefaults = new JButton();
					getContentPane().add(jButtonDefaults);
					jButtonDefaults.setText("Defaults");
					jButtonDefaults.addActionListener(this);
					jButtonDefaults.setBounds(118, 180, 141, 20);
				}

				{
					jSliderInterval = new JSlider(SwingConstants.VERTICAL, 0,
							750, 50);
					getContentPane().add(jSliderInterval);
					jSliderInterval.setBounds(269, 2, 16, 236);
					jSliderInterval.addChangeListener(this);

				}
				{
					jLabelSpeed = new JLabel();
					getContentPane().add(jLabelSpeed);
					jLabelSpeed.setText("Speed");
					jLabelSpeed.setBounds(220, 91, 45, 16);
				}
				{
					jLabel1 = new JLabel();
					getContentPane().add(jLabel1);
					jLabel1.setText("millisec");
					jLabel1.setBounds(220, 133, 51, 14);
				}
				{
					jTextFieldInterval = new JTextField();
					getContentPane().add(jTextFieldInterval);
					jTextFieldInterval.setEditable(false);
					jTextFieldInterval.setBounds(217, 110, 45, 21);
				}
				{
					jLabel2 = new JLabel();
					getContentPane().add(jLabel2);
					jLabel2.setText("per step");
					jLabel2.setBounds(220, 147, 51, 14);
				}

				btnXColor = new JButton("x Color");
				btnXColor.setForeground(Color.WHITE);
				btnXColor.setBackground(Color.BLUE);
				btnXColor.setBounds(22, 175, 80, 15);
				btnXColor.addActionListener(this);
				getContentPane().add(btnXColor);

				btnYColor = new JButton("y Color");
				btnYColor.setBackground(Color.GREEN);
				btnYColor.setBounds(22, 190, 80, 15);
				btnYColor.addActionListener(this);
				getContentPane().add(btnYColor);

				btnZColor = new JButton("z Color");
				btnZColor.setBackground(Color.RED);
				btnZColor.setBounds(22, 205, 80, 15);
				btnZColor.addActionListener(this);
				getContentPane().add(btnZColor);

				btnBackground = new JButton("background");
				btnBackground.setBackground(Color.WHITE);
				btnBackground.setBounds(12, 157, 105, 15);
				btnBackground.addActionListener(this);
				getContentPane().add(btnBackground);
				{
					lblProvider = new JTextArea("Provider Description");
					lblProvider.setBounds(10, 270, 280, 150);
					lblProvider.setEditable(false);
					lblProvider.setAutoscrolls(true);
					getContentPane().add(lblProvider);
				}

				setResizable(false);
				alignUI();
				pack();
			}
		} catch (Exception e) {
			setTitle(e.toString());
		}
	}

	private void alignUI() {

		if (showAxes)
			jCheckBoxShowAxes.setSelected(true);
		else
			jCheckBoxShowAxes.setSelected(false);

		if (showLines)
			jCheckBoxShowLines.setSelected(true);
		else
			jCheckBoxShowLines.setSelected(false);

		if (showPoints)
			jCheckBoxShowNodes.setSelected(true);
		else
			jCheckBoxShowNodes.setSelected(false);

		btnXColor.setBackground(xColor);
		if (xColor.getBlue() + xColor.getGreen() + xColor.getRed() > 384)
			btnXColor.setForeground(Color.BLACK);
		else
			btnXColor.setForeground(Color.WHITE);

		btnYColor.setBackground(yColor);
		if (yColor.getBlue() + yColor.getGreen() + yColor.getRed() > 384)
			btnYColor.setForeground(Color.BLACK);
		else
			btnYColor.setForeground(Color.WHITE);

		btnZColor.setBackground(zColor);
		if (zColor.getBlue() + zColor.getGreen() + zColor.getRed() > 384)
			btnZColor.setForeground(Color.BLACK);
		else
			btnZColor.setForeground(Color.WHITE);

		btnBackground.setBackground(bkgrdColor);
		if (bkgrdColor.getBlue() + bkgrdColor.getGreen() + bkgrdColor.getRed() > 384)
			btnBackground.setForeground(Color.BLACK);
		else
			btnBackground.setForeground(Color.WHITE);

		jTextFieldStep.setText(String.valueOf(step));
		jTextFieldMaxIteration.setText(String.valueOf(maxIteration));
		jSliderInterval
				.setValue((int) (Math.round(100.0d * Math.log(interval))));
		jTextFieldInterval.setText(String.valueOf(interval));

		if (callingFrame != null && callingFrame.getProvider() != null)
			lblProvider.setText(callingFrame.getProvider().description);

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == jButtonCancel) {
			getOptions();
			alignUI();
		} else if (e.getSource() == jButtonOK) {
			try {
				maxIteration = Integer
						.valueOf(jTextFieldMaxIteration.getText());
				step = Integer.valueOf(jTextFieldStep.getText());
			} catch (Exception exc) {
			}
			setOptions();
			callingFrame.repaint();
		} else if (e.getSource() == jButtonDefaults) {
			getDefaultOptions();
			alignUI();
		} else if (e.getSource() == jCheckBoxShowAxes) {
			showAxes = jCheckBoxShowAxes.isSelected();
		} else if (e.getSource() == jCheckBoxShowLines) {
			showLines = jCheckBoxShowLines.isSelected();
		} else if (e.getSource() == jCheckBoxShowNodes) {
			showPoints = jCheckBoxShowNodes.isSelected();
		} else if (e.getSource() == btnXColor) {
			xColor = JColorChooser.showDialog(callingFrame, "X Axis Color",
					null);
			alignUI();
		} else if (e.getSource() == btnYColor) {
			yColor = JColorChooser.showDialog(callingFrame, "Y Axis Color",
					null);
			alignUI();
		} else if (e.getSource() == btnZColor) {
			zColor = JColorChooser.showDialog(callingFrame, "Z Axis Color",
					null);
			alignUI();
		} else if (e.getSource() == btnBackground) {
			bkgrdColor = JColorChooser.showDialog(callingFrame,
					"Background Color", null);
			alignUI();
		}

	}

	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider jS = (JSlider) e.getSource();
		if (jS == jSliderInterval) {
			DarstellungsRaum.Options options = callingFrame.darstellungsRaum
					.getOptions();
			options.setInterval(Math.round(Math.exp(jSliderInterval.getValue() / 100.0)));
			interval = options.getInterval();
			jTextFieldInterval.setText(String.valueOf(options.getInterval()));
			callingFrame.repaint();

		}
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		if (arg0.getKeyChar() == '\n')
			jButtonOK.doClick();
		if (arg0.getKeyChar() == '')
			jButtonCancel.doClick();
	}
}
