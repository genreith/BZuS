package de.bzus.graph3d;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;

/**
 * Status display various parameters that determine the actual system state.
 * This includes presentation parameters, the actual measurement and state within
 * the process.
 *  
 * @author Werner Siegfried Genreith
 */
class Status extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7416089975910427120L;

	private xyzGraph callingFrame = null;
	private Timer timer = null;

	public Status(xyzGraph frame) {
		super(frame);
		callingFrame = frame;
		setPreferredSize(new Dimension(620, 260));
		setResizable(true);
		initGUI();
		alignUI();
		timer = new Timer(200, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				alignUI();
			}
		});
		timer.start();
	}

	private JTextField jTexcite = null;
	private JTextField jTexciteP = null;
	private JTextField jTbkgrdColor = null;
	private JTextField jTinterval = null;
	private JTextField jTmaxIteration = null;
	private JTextField jTscaling = null;
	private JTextField jTshowAxes = null;
	private JTextField jTshowLines = null;
	private JTextField jTshowPoints = null;
	private JTextField jTzoomCenter = null;
	private JTextField jTzoomFactor = null;
	private JTextArea jTstate = null;
	private JScrollPane scrollpane = null;
	private JTextField jTpathNo = null;
	private JTextField jTpointNo = null;
	private JTextField jTstep = null;

	private JLabel jLexcite = null;
	private JLabel jLexciteP = null;
	private JLabel jLbkgrdColor = null;
	private JLabel jLinterval = null;
	private JLabel jLmaxIteration = null;
	private JLabel jLscaling = null;
	private JLabel jLshowAxes = null;
	private JLabel jLshowLines = null;
	private JLabel jLshowPoints = null;
	private JLabel jLzoomCenter = null;
	private JLabel jLzoomFactor = null;
	private JLabel jLstate = null;
	private JLabel jLpathNo = null;
	private JLabel jLpointNo = null;
	private JLabel jLstep = null;

	private void initGUI() {
		setTitle("Status");
		getContentPane().setLayout(null);

		jLexcite = new JLabel("Excitation");
		getContentPane().add(jLexcite);
		jTexcite = new JTextField();
		jTexcite.setEditable(false);
		getContentPane().add(jTexcite);
		jLexcite.setBounds(0, 0, 95, 15);
		jTexcite.setBounds(96, 0, 200, 15);

		jLpathNo = new JLabel("Run");
		getContentPane().add(jLpathNo);
		jTpathNo = new JTextField();
		jTpathNo.setEditable(false);
		getContentPane().add(jTpathNo);
		jLpathNo.setBounds(300, 0, 35, 15);
		jTpathNo.setBounds(336, 0, 35, 15);

		jLpointNo = new JLabel("Iteration");
		getContentPane().add(jLpointNo);
		jTpointNo = new JTextField();
		jTpointNo.setEditable(false);
		getContentPane().add(jTpointNo);
		jLpointNo.setBounds(396, 0, 70, 15);
		jTpointNo.setBounds(470, 0, 126, 15);

		jLstep = new JLabel("Step");
		getContentPane().add(jLstep);
		jTstep = new JTextField();
		jTstep.setEditable(false);
		getContentPane().add(jTstep);
		jLstep.setBounds(0, 20, 95, 15);
		jTstep.setBounds(96, 20, 200, 15);

		jLbkgrdColor = new JLabel("Background");
		getContentPane().add(jLbkgrdColor);
		jTbkgrdColor = new JTextField();
		jTbkgrdColor.setEditable(false);
		getContentPane().add(jTbkgrdColor);
		jLbkgrdColor.setBounds(300, 20, 95, 15);
		jTbkgrdColor.setBounds(396, 20, 200, 15);

		jLinterval = new JLabel("Speed");
		getContentPane().add(jLinterval);
		jTinterval = new JTextField();
		jTinterval.setEditable(false);
		getContentPane().add(jTinterval);
		jLinterval.setBounds(0, 40, 95, 15);
		jTinterval.setBounds(96, 40, 200, 15);

		jLmaxIteration = new JLabel("Display PathLen");
		getContentPane().add(jLmaxIteration);
		jTmaxIteration = new JTextField();
		jTmaxIteration.setEditable(false);
		getContentPane().add(jTmaxIteration);
		jLmaxIteration.setBounds(300, 40, 95, 15);
		jTmaxIteration.setBounds(396, 40, 200, 15);

		jLscaling = new JLabel("Scaling");
		getContentPane().add(jLscaling);
		jTscaling = new JTextField();
		jTscaling.setEditable(false);
		getContentPane().add(jTscaling);
		jLscaling.setBounds(0, 60, 95, 15);
		jTscaling.setBounds(96, 60, 200, 15);

		jLshowAxes = new JLabel("Show Axes");
		getContentPane().add(jLshowAxes);
		jTshowAxes = new JTextField();
		jTshowAxes.setEditable(false);
		getContentPane().add(jTshowAxes);
		jLshowAxes.setBounds(300, 60, 95, 15);
		jTshowAxes.setBounds(396, 60, 200, 15);

		jLshowLines = new JLabel("Show Lines");
		getContentPane().add(jLshowLines);
		jTshowLines = new JTextField();
		jTshowLines.setEditable(false);
		getContentPane().add(jTshowLines);
		jLshowLines.setBounds(0, 80, 95, 15);
		jTshowLines.setBounds(96, 80, 200, 15);

		jLshowPoints = new JLabel("Show Nodes");
		getContentPane().add(jLshowPoints);
		jTshowPoints = new JTextField();
		jTshowPoints.setEditable(false);
		getContentPane().add(jTshowPoints);
		jLshowPoints.setBounds(300, 80, 95, 15);
		jTshowPoints.setBounds(396, 80, 200, 15);

		jLzoomCenter = new JLabel("ZoomCenter");
		getContentPane().add(jLzoomCenter);
		jTzoomCenter = new JTextField();
		jTzoomCenter.setEditable(false);
		getContentPane().add(jTzoomCenter);
		jLzoomCenter.setBounds(0, 100, 95, 15);
		jTzoomCenter.setBounds(96, 100, 200, 15);

		jLzoomFactor = new JLabel("ZoomFactor");
		getContentPane().add(jLzoomFactor);
		jTzoomFactor = new JTextField();
		jTzoomFactor.setEditable(false);
		getContentPane().add(jTzoomFactor);
		jLzoomFactor.setBounds(300, 100, 95, 15);
		jTzoomFactor.setBounds(396, 100, 200, 15);

		jLexciteP = new JLabel("Actual Node");
		getContentPane().add(jLexciteP);
		jTexciteP = new JTextField();
		jTexciteP.setEditable(false);
		getContentPane().add(jTexciteP);
		jLexciteP.setBounds(0, 120, 95, 15);
		jTexciteP.setBounds(96, 120, 500, 15);

		jLstate = new JLabel("Actual State");
		getContentPane().add(jLstate);
		jTstate = new JTextArea("a\nb\nc");
		jTstate.setEditable(false);
		jTstate.setLineWrap(true);
		scrollpane = new JScrollPane(jTstate);
		getContentPane().add(scrollpane);
		scrollpane.setBounds(100, 140, 496, 80);

		pack();
	}

	private void alignUI() {
		DarstellungsRaum.Options opt = callingFrame.darstellungsRaum
				.getOptions();
		double excite = callingFrame.darstellungsRaum.getExcite();
		jTexcite.setText(String.format("%8.4E", excite));
		jTexciteP.setText(String.valueOf(callingFrame.darstellungsRaum
				.getExciteP()));
		jTbkgrdColor.setText("R<" + opt.bkgrdColor.getRed() + ">/B<"
				+ opt.bkgrdColor.getBlue() + ">/G<"
				+ opt.bkgrdColor.getGreen()+ ">");
		jTstep.setText(String.valueOf(opt.step));
		jTinterval.setText(String.valueOf(opt.getInterval()));
		jTmaxIteration.setText(String.valueOf(opt.displayPathLen));
		jTscaling.setText(String.valueOf(opt.scaling));
		jTshowAxes.setText(String.valueOf(opt.showAxes));
		jTshowLines.setText(String.valueOf(opt.showLines));
		jTshowPoints.setText(String.valueOf(opt.showPoints));
		jTzoomCenter.setText("x=" + opt.zoomCenter[0] + " / y="
				+ opt.zoomCenter[1]);
		jTzoomFactor.setText(String.valueOf(opt.zoomFactor));
		jTstate.setText(callingFrame.darstellungsRaum.getState());
		if (callingFrame.getProvider() != null) {
			jTpathNo.setText(String.valueOf(callingFrame.getProvider()
					.getRIndex()));
			jTpointNo.setText(String.valueOf(callingFrame.getProvider()
					.getIndexStep()));
		}
	}
	
	@Override
	public void paint (Graphics g) {
		super.paint(g);
		scrollpane.setBounds(100, 140, getWidth()-124, getHeight()-180);
	}
}
