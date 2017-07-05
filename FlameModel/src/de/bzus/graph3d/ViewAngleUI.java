package de.bzus.graph3d;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.swing.JFrame;
import javax.swing.event.MouseInputListener;

/**
 * ViewAngleUI allows to turn the actual view the x,y,z axes. This is done by
 * mouse actions drag and drop.
 * 
 * @author Werner Siegfried Genreith
 */
class ViewAngleUI extends javax.swing.JDialog implements MouseInputListener {
	private static final long serialVersionUID = -6602984821386275618L;

	private xyzGraph callingFrame = null;

	public DarstellungsRaum dR = new DarstellungsRaum();
	private DarstellungsRaum dRxyz = null;

	public ViewAngleUI(JFrame frame) {
		super(frame);
		initGUI();
		callingFrame = (xyzGraph) frame;
		this.dRxyz = callingFrame.darstellungsRaum;
	}

	private void initGUI() {
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		setSize(200, 220);
		setResizable(false);

		setTitle("ViewAngle");
		setVisible(true);
		Punkt[] p1 = { new Punkt(0d, 0d, 0d), new Punkt(0d, 0d, 1d), new Punkt(0d, 1d, 1d), new Punkt(0d, 1d, 0d),
				new Punkt(0d, 0d, 0d), };
		Punkt[] p2 = { new Punkt(1d, 0d, 0d), new Punkt(1d, 0d, 1d), new Punkt(1d, 1d, 1d), new Punkt(1d, 1d, 0d),
				new Punkt(1d, 0d, 0d), };
		Punkt[] p3 = { new Punkt(0d, 0d, 0d), new Punkt(1d, 0d, 0d), };
		Punkt[] p4 = { new Punkt(0d, 0d, 1d), new Punkt(1d, 0d, 1d), };
		Punkt[] p5 = { new Punkt(0d, 1d, 1d), new Punkt(1d, 1d, 1d), };
		Punkt[] p6 = { new Punkt(0d, 1d, 0d), new Punkt(1d, 1d, 0d), };
		dR.add(new Pfad(p1));
		dR.add(new Pfad(p2));
		dR.add(new Pfad(p3));
		dR.add(new Pfad(p4));
		dR.add(new Pfad(p5));
		dR.add(new Pfad(p6));
		dR.setAt(new AffineTransform(60.0, 0.0, 0.0, -60.0, 100.0, 130.0));
		dR.getOptions().scaling = ScaleMode.FIXED;
		dR.getOptions().showAxes = true;

		{
			getContentPane().setLayout(null);

		}
	}

	private Point2D pMD = null;
	private double phiMD = 0.0;
	private double thetaMD = 0.0;

	@Override
	public void mouseDragged(MouseEvent e) {
		double diffX = e.getPoint().getX() - pMD.getX();
		double diffY = e.getPoint().getY() - pMD.getY();

		dR.getViewAngle().setPhi(phiMD - diffX / getBounds().getWidth() * Math.PI);
		dR.getViewAngle().setTheta(thetaMD - diffY / 2.0 / getBounds().getHeight() * Math.PI);
		repaint();
		if (dRxyz != null) {
			dRxyz.getViewAngle().setPhi(dR.getViewAngle().getPhi());
			dRxyz.getViewAngle().setTheta(dR.getViewAngle().getTheta());
			callingFrame.repaint();

		}

	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		dR.paint(g2, getSize());
	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		dR.setViewAngle(dRxyz.getViewAngle());
		repaint();
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		pMD = arg0.getPoint();
		phiMD = dR.getViewAngle().getPhi();
		thetaMD = dR.getViewAngle().getTheta();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {

	}
}
