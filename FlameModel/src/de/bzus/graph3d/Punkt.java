package de.bzus.graph3d;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Punkt provides methods for constructing, managing and displaying 
 * 3 dimensional points in 2 dimensions. 
 * 
 * @author Werner Siegfried Genreith
 */
public class Punkt {
	/**
	 * 
	 */
	private Color autoColor = null;
	private double betrag = -1;
	private BufferedImage img = null;
	private Color pc = Color.CYAN;
	private float ps = 0.5f;
	private final double[] r = new double[3];

	public Punkt(BufferedImage img, double z, double y, double x) {
		this.r[2] = x;
		this.r[1] = y;
		this.r[0] = z;
		this.img = img;
	}

	public Punkt(double z, double y, double x) {
		this.r[2] = x;
		this.r[1] = y;
		this.r[0] = z;
		this.img = null;
	}

	public Color autoColor() {
		if (autoColor != null)
			return autoColor;

		int rot = 255, gruen = 255, blau = 255;
		if (betrag() == 0)
			return new Color(rot, gruen, blau);
		blau = (int) (255.0 * Math.abs(r[2]) / betrag());
		gruen = (int) (255.0 * Math.abs(r[1]) / betrag());
		rot = (int) (255.0 * Math.abs(r[0]) / betrag());
		autoColor = new Color(rot, gruen, blau);
		return autoColor;
	}

	public double betrag() {
		if (betrag < 0) {
			betrag = 0.0;
			for (int i = 0; i < r.length; i++)
				betrag += r[i] * r[i];
			betrag = Math.sqrt(betrag);
		}
		return betrag;
	}

	public BufferedImage getBufferedImage() {
		return img;
	}

	private final double[] r2D = new double[2];

	private double phi = 0.0;
	private double theta = 0.0;
	private boolean hasPerspektiveChanged = true;

	private boolean hasPerspektiveChanged(ViewAngle persp) {
		if (persp.getPhi() != phi || persp.getTheta() != theta) {
			phi = persp.getPhi();
			theta = persp.getTheta();
			hasPerspektiveChanged = true;
		}
		return hasPerspektiveChanged;
	}

	public double[] get2DCoordinates(ViewAngle perspective) {
		if (hasPerspektiveChanged(perspective)) {
			r2D[0] = perspective.getXProjektion(r);
			r2D[1] = perspective.getYProjektion(r);
			hasPerspektiveChanged = false;
		}
		return r2D;
	}

	public Color getColor() {
		return pc;
	}

	public double[] getCoordinate() {
		return r;
	}

	public float getStroke() {
		return ps;
	}

	public double getX() {
		return r[2];
	}

	public double getY() {
		return r[1];
	}

	public double getZ() {
		return r[0];
	}

	public void setBufferedImage(BufferedImage img) {
		this.img = img;
		return;
	}

	public void setColor(Color pc) {
		this.pc = pc;
	}

	public void setStroke(float ps) {
		this.ps = ps;
	}

	@Override
	public String toString() {
		String ret = String.format(
				"X= %1$+8.4E / Y= %2$+8.4E / Z= %3$+8.4E / abs= %4$+8.4E / ",
				r[2], r[1], r[0], betrag);
		return ret;
	}
}
