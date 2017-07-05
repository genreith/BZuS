package de.bzus.graph3d;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.event.MouseInputListener;

/**
 * xyzGraph displays the 3 dimensional model on the screen and allows some
 * manipulations by mouse actions.
 * 
 * @author Werner Siegfried Genreith
 */
public class xyzGraph extends javax.swing.JFrame implements ActionListener, MouseInputListener, MouseWheelListener {

	private class GraphPanel extends JPanel {

		private static final long serialVersionUID = 7761347712984429660L;

		public GraphPanel() {
			super();
		}

		@Override
		public void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			darstellungsRaum.paint(g2, getSize());

			return;
		}
	}

	private static final long serialVersionUID = 5304512429364186787L;
	/*
	 * private static xyzGraph inst; public static void main(String[] args) {
	 * SwingUtilities.invokeLater(new Runnable() {
	 * 
	 * @Override public void run() { inst = new xyzGraph(null);
	 * inst.setExtendedState(MAXIMIZED_BOTH); inst.setVisible(true);
	 * inst.showDialogs(); } }); }
	 */
	private double[] _atMatrix = new double[6];
	private Point2D _pMD = null;
	private ScaleMode _scalemode = ScaleMode.FIXED;
	private JFrame caller = null;
	protected DarstellungsRaum darstellungsRaum = new DarstellungsRaum();
	private JPanel graphPanel;
	private boolean isDragging = false;
	private JMenuBar jMenuBar1;
	private JMenu jMenuNavigate;

	private JMenuItem jMenuItemFirst;
	private JMenuItem jMenuItemLast;
	private JMenuItem jMenuItemNext;
	private JMenuItem jMenuItemViewAngle;

	private JMenuItem jMenuItemPresentation;
	private JMenuItem jMenuItemPrev;
	private JMenuItem jMenuItemRestart;
	private JMenuItem jMenuItemResume;
	private JMenuItem jMenuItemScalingAUTO;
	private JMenuItem jMenuItemScalingCENTER;
	private JMenuItem jMenuItemScalingZOOM;
	private JMenuItem jMenuItemStart;
	private JMenuItem jMenuItemStatus;
	private JMenuItem jMenuItemStep;
	private JMenuItem jMenuItemDefaultView;
	private JMenuItem jMenuItemXYPlane;
	private JMenuItem jMenuItemXZPlane;
	private JMenuItem jMenuItemYZPlane;
	private JMenu jMenuOptions;
	private JMenu jMenuView;

	private JMenu jMenuRun;

	private JMenu jMenuScaling;
	private JMenu mnShow;
	private JMenuItem mntmProvider[];

	private ViewAngleUI pGUI = null;

	private Presentation presentationDlg = null;
	private Provider3D provider = null;
	private Status statusDlg = null;

	public xyzGraph(JFrame calling) {
		super();
		caller = calling;
		initGUI();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == jMenuItemViewAngle) {
			getPGUI().setVisible(true);
		} else if (e.getSource() == jMenuItemPresentation) {
			getPresentationDlg().setVisible(true);
		} else if (e.getSource() == jMenuItemStatus) {
			getStatusDlg().setVisible(true);
		} else if (e.getSource() == jMenuItemScalingAUTO) {
			darstellungsRaum.getOptions().scaling = ScaleMode.AUTOSCALE;
			repaint();
		} else if (e.getSource() == jMenuItemScalingCENTER) {
			darstellungsRaum.getOptions().scaling = ScaleMode.CENTERSCALE;
			repaint();
		} else if (e.getSource() == jMenuItemScalingZOOM) {
			darstellungsRaum.getOptions().scaling = ScaleMode.ZOOM;
			repaint();
		} else if (e.getSource() == jMenuItemDefaultView) {
			darstellungsRaum.getViewAngle().setPerspective(1.0, 2.0, 3.0);
			repaint();
		} else if (e.getSource() == jMenuItemXZPlane) {
			darstellungsRaum.getViewAngle().setPerspective(0.0, 1.0, 0.0);
			repaint();
		} else if (e.getSource() == jMenuItemYZPlane) {
			darstellungsRaum.getViewAngle().setPerspective(0.0, 0.0, 1.0);
			repaint();
		} else if (e.getSource() == jMenuItemXYPlane) {
			darstellungsRaum.getViewAngle().setPerspective(1.0, 0.0, 0.0);
			repaint();
		} else if (e.getSource() == jMenuItemFirst) {
			enableMenuItemsRun(false, true, true, true);
			provider.startFirst();
		} else if (e.getSource() == jMenuItemLast) {
			provider.startLast();
			enableMenuItemsRun(false, true, true, true);
		} else if (e.getSource() == jMenuItemNext) {
			provider.startNext();
			enableMenuItemsRun(false, true, true, true);
		} else if (e.getSource() == jMenuItemPrev) {
			provider.startPrev();
			enableMenuItemsRun(false, true, true, true);
		} else if (e.getSource() == jMenuItemStep) {
			provider.step();
			enableMenuItemsRun(false, true, true, true);
		} else if (e.getSource() == jMenuItemRestart) {
			provider.restart();
			enableMenuItemsRun(false, true, true, true);
		} else if (e.getSource() == jMenuItemResume) {
			provider.resume();
			enableMenuItemsRun(false, true, true, false);
		} else if (e.getSource() == jMenuItemStart) {
			provider.startProvider();
			enableMenuItemsRun(false, true, true, false);
			enableMenuItemsFile(true, true, true, true);

		} else {
			for (int i = 0; i < mntmProvider.length; i++) {
				if (e.getSource() == mntmProvider[i]) {
					setProvider(Provider3D.getInstances().elementAt(i));
					setTitle("Projection using " + provider.name);
				}
			}
		}
	}

	private void enableMenuItemsFile(boolean first, boolean last, boolean next, boolean prev) {
		jMenuItemFirst.setEnabled(first);
		jMenuItemLast.setEnabled(last);
		jMenuItemNext.setEnabled(next);
		jMenuItemPrev.setEnabled(prev);
	}

	private void enableMenuItemsRun(boolean start, boolean step, boolean restart, boolean resume) {
		jMenuItemStart.setEnabled(start);
		jMenuItemStep.setEnabled(step);
		jMenuItemRestart.setEnabled(restart);
		jMenuItemResume.setEnabled(resume);
	}

	private ViewAngleUI getPGUI() {
		if (pGUI == null) {
			pGUI = new ViewAngleUI(this);
			pGUI.setLocation(0, 50);
			pGUI.setVisible(false);
		}
		return pGUI;
	}

	private Presentation getPresentationDlg() {
		if (presentationDlg == null) {
			presentationDlg = new Presentation(this);
			presentationDlg.setLocation(0, getHeight() - presentationDlg.getHeight() - 25);
		}
		return presentationDlg;
	}

	public Provider3D getProvider() {
		return this.provider;
	}

	private Status getStatusDlg() {
		if (statusDlg == null) {
			statusDlg = new Status(this);
			statusDlg.setLocation(getWidth() - statusDlg.getWidth(), getHeight() - statusDlg.getHeight() - 25);
		}
		return statusDlg;
	}

	private void initGUI() {
		try {
			setTitle("3D Graphics");
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			setPreferredSize(new Dimension(600, 600));

			graphPanel = new GraphPanel();
			setContentPane(graphPanel);
			graphPanel.setOpaque(true);
			graphPanel.addMouseListener(this);
			graphPanel.addMouseMotionListener(this);
			graphPanel.addMouseWheelListener(this);

			jMenuBar1 = new JMenuBar();
			setJMenuBar(jMenuBar1);
			jMenuBar1.setEnabled(false);
			{
				jMenuRun = new JMenu("Run");

				jMenuItemStart = new JMenuItem("Start");
				jMenuItemStart.addActionListener(this);
				jMenuItemRestart = new JMenuItem("Restart");
				jMenuItemRestart.addActionListener(this);
				jMenuItemStep = new JMenuItem("Pause/Step");
				jMenuItemStep.addActionListener(this);
				jMenuItemResume = new JMenuItem("Resume");
				jMenuItemResume.addActionListener(this);

				jMenuRun.add(jMenuItemStart);
				jMenuRun.add(jMenuItemRestart);
				jMenuRun.add(jMenuItemStep);
				jMenuRun.add(jMenuItemResume);

			}
			{
				jMenuNavigate = new JMenu("Navigate");

				jMenuItemNext = new JMenuItem("Next");
				jMenuItemNext.addActionListener(this);
				jMenuItemPrev = new JMenuItem("Previous");
				jMenuItemPrev.addActionListener(this);
				jMenuItemFirst = new JMenuItem("First");
				jMenuItemFirst.addActionListener(this);
				jMenuItemLast = new JMenuItem("Last");
				jMenuItemLast.addActionListener(this);

				jMenuNavigate.add(jMenuItemNext);
				jMenuNavigate.add(jMenuItemPrev);
				jMenuNavigate.add(jMenuItemFirst);
				jMenuNavigate.add(jMenuItemLast);
			}
			{
				jMenuView = new JMenu("View");
				jMenuItemDefaultView = new JMenuItem("Default");
				jMenuItemDefaultView.addActionListener(this);
				jMenuItemXYPlane = new JMenuItem("XY plane");
				jMenuItemXYPlane.addActionListener(this);
				jMenuItemXZPlane = new JMenuItem("XZ plane");
				jMenuItemXZPlane.addActionListener(this);
				jMenuItemYZPlane = new JMenuItem("YZ plane");
				jMenuItemYZPlane.addActionListener(this);
				jMenuItemViewAngle = new JMenuItem("Continuous");
				jMenuItemViewAngle.addActionListener(this);

				jMenuView.add(jMenuItemDefaultView);
				jMenuView.add(jMenuItemXYPlane);
				jMenuView.add(jMenuItemXZPlane);
				jMenuView.add(jMenuItemYZPlane);
				jMenuView.add(jMenuItemViewAngle);
			}
			{
				jMenuOptions = new JMenu("Options");
				{
					jMenuScaling = new JMenu("Scaling");
					{
						jMenuItemScalingAUTO = new JMenuItem("automatic");
						jMenuItemScalingAUTO.addActionListener(this);
					}
					{
						jMenuItemScalingCENTER = new JMenuItem("centered");
						jMenuItemScalingCENTER.addActionListener(this);
					}
					{
						jMenuItemScalingZOOM = new JMenuItem("zoomed");
						jMenuItemScalingZOOM.addActionListener(this);
					}
					jMenuScaling.add(jMenuItemScalingAUTO);
					jMenuScaling.add(jMenuItemScalingCENTER);
					jMenuScaling.add(jMenuItemScalingZOOM);
				}

				jMenuItemPresentation = new JMenuItem("Presentation");
				jMenuItemPresentation.addActionListener(this);

				jMenuItemStatus = new JMenuItem("Status");
				jMenuItemStatus.addActionListener(this);

				jMenuOptions.add(jMenuScaling);
				jMenuOptions.add(jMenuItemPresentation);
				jMenuOptions.add(jMenuItemStatus);

			}

			jMenuBar1.add(jMenuRun);
			jMenuBar1.add(jMenuNavigate);
			jMenuBar1.add(jMenuView);
			jMenuBar1.add(jMenuOptions);

			if (caller != null) {
				mnShow = new JMenu("Show");
				jMenuBar1.add(mnShow);
				{
					mntmProvider = new JMenuItem[Provider3D.getInstances().size()];
					for (int i = 0; i < mntmProvider.length; i++) {
						mntmProvider[i] = new JMenuItem(Provider3D.getInstances().elementAt(i).name);
						mntmProvider[i].setToolTipText(Provider3D.getInstances().elementAt(i).description);
						mntmProvider[i].setEnabled(true);
						mntmProvider[i].addActionListener(this);
						mnShow.add(mntmProvider[i]);
					}
				}
			}

			enableMenuItemsFile(false, false, false, false);
			enableMenuItemsRun(false, false, false, false);
			pack();

		} catch (

		Exception e) {
			setTitle(e.toString());
		}

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 1)
			if (e.getButton() == 1) {
				if (darstellungsRaum.getOptions().scaling != ScaleMode.FIXED)
					_scalemode = darstellungsRaum.getOptions().scaling;
				darstellungsRaum.getOptions().scaling = ScaleMode.FIXED;
			} else if (e.getButton() == 3)
				if (darstellungsRaum.getOptions().scaling == ScaleMode.FIXED) {
					darstellungsRaum.getOptions().scaling = _scalemode;
				}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!isDragging)
			isDragging = true;
		if (darstellungsRaum.getOptions().scaling == ScaleMode.FIXED) {
			double diffX = e.getPoint().getX() - _pMD.getX();
			double diffY = e.getPoint().getY() - _pMD.getY();
			darstellungsRaum.getAt().setTransform(_atMatrix[0], _atMatrix[1], _atMatrix[2], _atMatrix[3],
					_atMatrix[4] + diffX, _atMatrix[5] + diffY);
			repaint();
		}

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		_pMD = arg0.getPoint();
		darstellungsRaum.getAt().getMatrix(_atMatrix);
		darstellungsRaum.getOptions().zoomCenter[0] = arg0.getPoint().getX();
		darstellungsRaum.getOptions().zoomCenter[1] = arg0.getPoint().getY();
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		if (isDragging) {
			_pMD = arg0.getPoint();
			darstellungsRaum.getAt().getMatrix(_atMatrix);
			darstellungsRaum.getOptions().zoomCenter[0] = arg0.getPoint().getX();
			darstellungsRaum.getOptions().zoomCenter[1] = arg0.getPoint().getY();
			isDragging = false;
			repaint();
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (darstellungsRaum.getOptions().scaling == ScaleMode.FIXED) {
			darstellungsRaum.getOptions().zoomFactor = e.getWheelRotation();
			double z = 1.0 / darstellungsRaum.getZoomFactor();
			double[] p = darstellungsRaum.getOptions().zoomCenter;

			_atMatrix[0] *= z;
			_atMatrix[3] *= z;
			_atMatrix[4] = p[0] * (1.0 - z) + z * _atMatrix[4];
			_atMatrix[5] = p[1] * (1.0 - z) + z * _atMatrix[5];

			darstellungsRaum.getOptions().at.setTransform(new AffineTransform(_atMatrix));
		} else {
			darstellungsRaum.getOptions().zoomFactor += e.getWheelRotation();
		}
		repaint();
	}

	public void setProvider(Provider3D prvdr) {
		jMenuBar1.setEnabled(false);

		if (prvdr != null) {
			prvdr.setDarstellungsRaum(darstellungsRaum);
			prvdr.setCallingFrame(this);
			darstellungsRaum.clearAll();

			if (this.provider != null) {
				this.provider.stopProvider();
				if (prvdr.resultList == null && this.provider.resultList != null)
					prvdr.setResultList(this.provider.resultList);
				this.provider.resultList = null;
			}

			this.provider = prvdr;
			if (this.provider.resultList != null && this.provider.resultList.getHistories().length > 0) {
				jMenuBar1.setEnabled(true);
				enableMenuItemsRun(true, true, false, false);
				enableMenuItemsFile(false, false, false, false);
			}
			repaint();
		}
	}

	public void showDialogs() {
		getPGUI().setVisible(true);
		getStatusDlg().setVisible(true);
		getPresentationDlg().setVisible(true);
	}

}
