package de.bzus.graph3d;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.util.Vector;

import de.bzus.flame.common.Logging;

/**
 * Pfad provides methods for constructing, managing and displaying a 3
 * dimensional path in 2 dimensions.
 * 
 * @author Werner Siegfried Genreith
 */
public class Pfad {
	private int next = 0;
	private Color pc = Color.MAGENTA;
	private float ps = 0.5f;
	private Vector<Punkt> PunktListe = new Vector<Punkt>();
	private double xMax2D = Double.NEGATIVE_INFINITY;
	private double xMax3D = Double.NEGATIVE_INFINITY;
	private double xMin2D = Double.POSITIVE_INFINITY;
	private double xMin3D = Double.POSITIVE_INFINITY;
	private double yMax2D = Double.NEGATIVE_INFINITY;
	private double yMax3D = Double.NEGATIVE_INFINITY;
	private double yMin2D = Double.POSITIVE_INFINITY;
	private double yMin3D = Double.POSITIVE_INFINITY;
	private double zMax3D = Double.NEGATIVE_INFINITY;
	private double zMin3D = Double.POSITIVE_INFINITY;

	public Pfad() {
	}

	public Pfad(Punkt[] pl) {
		for (int i = 0; i < pl.length; i++) {
			PunktListe.add(pl[i]);
			set3DMinMaxValues(pl[i]);
		}
	}

	public synchronized void add(Punkt punkt) {
		set3DMinMaxValues(punkt);
		PunktListe.add(punkt);
	}

	public Color autoColor(Punkt p1, Punkt p0) {
		int rot = 255, gruen = 255, blau = 255;

		double[] direction = new double[3];
		direction[0] = p1.getZ() - p0.getZ();
		direction[1] = p1.getY() - p0.getY();
		direction[2] = p1.getX() - p0.getX();

		double betrag = 0.0;
		for (int i = 0; i < direction.length; i++)
			betrag += direction[i] * direction[i];
		if (betrag == 0)
			return new Color(rot, gruen, blau);

		betrag = Math.sqrt(betrag);
		blau = (int) (255.0 * Math.abs(direction[2]) / betrag);
		gruen = (int) (255.0 * Math.abs(direction[1]) / betrag);
		rot = (int) (255.0 * Math.abs(direction[0]) / betrag);

		return new Color(rot, gruen, blau);

	}

	public synchronized void clearAll() {
		xMax3D = Double.NEGATIVE_INFINITY;
		xMin3D = Double.POSITIVE_INFINITY;
		yMax3D = Double.NEGATIVE_INFINITY;
		yMin3D = Double.POSITIVE_INFINITY;
		zMax3D = Double.NEGATIVE_INFINITY;
		zMin3D = Double.POSITIVE_INFINITY;

		PunktListe.clear();
	}

	public synchronized void deleteFirst() {
		if (PunktListe.size() > 0)
			PunktListe.remove(0);
	}

	private Line2D.Double[] lines2D = null;
	private Color[] lines2DAutoColor = null;
	private Punkt[] points2Draw = null;

	public Punkt[] getPointsLast() {
		return points2Draw;
	}

	public Color[] getColorsLast() {
		return lines2DAutoColor;
	}

	public Line2D.Double[] getLinesLast() {
		return lines2D;
	}

	public synchronized void generateSampleLast(int maxJ, int step, ViewAngle perspektive) {
		int sizeOfPunktListe = PunktListe.size();
		if (maxJ < 1)
			maxJ = sizeOfPunktListe - 1;

		lines2D = new Line2D.Double[maxJ];
		int noPoints2Draw = sizeOfPunktListe / step;
		points2Draw = new Punkt[noPoints2Draw];
		lines2DAutoColor = new Color[maxJ];

		int j = 0;
		points2Draw[j] = PunktListe.elementAt(sizeOfPunktListe - 1);
		double[] x = points2Draw[j].get2DCoordinates(perspektive);
		set2DMinMaxValues(x);
		if (step < 1)
			return;
		for (j = 1; j < maxJ + 1 && step * j < sizeOfPunktListe; j++) {
			points2Draw[j] = PunktListe.elementAt(sizeOfPunktListe - 1 - step * j);
			double[] y = points2Draw[j].get2DCoordinates(perspektive);
			lines2D[j - 1] = new Line2D.Double(x[0], x[1], y[0], y[1]);
			lines2DAutoColor[j - 1] = autoColor(points2Draw[j], points2Draw[j - 1]);

			x = y;
			set2DMinMaxValues(x);
		}
		for (; step * j < sizeOfPunktListe; j++) {
			try {
				points2Draw[j] = PunktListe.elementAt(sizeOfPunktListe - 1 - step * j);
				double[] y = points2Draw[j].get2DCoordinates(perspektive);
				set2DMinMaxValues(y);
			} catch (IndexOutOfBoundsException exc) {
				Logging.getErrorLog().showError(exc + "\n---> step=" + step + "; j=" + j + "; maxJ=" + maxJ
						+ "; sizeOfPunktListe=" + sizeOfPunktListe, 2);
			}
		}
	}

	public synchronized void deleteLast() {
		if (PunktListe.size() > 0)
			PunktListe.remove(PunktListe.size() - 1);
	}

	public Color getColor() {
		return pc;
	}

	public Punkt getFirst() {
		next = 0;
		return getNext();
	}

	public Punkt getLast() {
		next = getLen() - 1;
		return getPrev();
	}

	public synchronized int getLen() {
		return PunktListe.size();
	}

	public double[] getMaxValues() {
		double[] ret = { zMax3D, yMax3D, xMax3D };
		return ret;
	}

	public double[] getMinValues() {
		double[] ret = { zMin3D, yMin3D, xMin3D };
		return ret;
	}

	public synchronized Punkt getNext() {
		int ret = 0;
		if (next >= getLen()) {
			next = 0;
			return null;
		} else
			ret = next;
		next++;
		return PunktListe.get(ret);
	}

	public synchronized Punkt getPrev() {
		int ret = 0;
		if (next < 0) {
			ret = -1;
			next = getLen() - 1;
		} else
			ret = next;
		next--;
		if (ret < 0)
			return null;
		else
			return PunktListe.get(ret);
	}

	public float getStroke() {
		return ps;
	}

	public synchronized double[] getXPoints() {
		double[] ret = new double[PunktListe.size()];
		for (int i = 0; i < PunktListe.size(); i++)
			ret[i] = PunktListe.get(i).getX();
		return ret;
	}

	public synchronized double[] getYPoints() {
		double[] ret = new double[PunktListe.size()];
		for (int i = 0; i < PunktListe.size(); i++)
			ret[i] = PunktListe.get(i).getY();
		return ret;
	}

	public synchronized double[] getZPoints() {
		double[] ret = new double[PunktListe.size()];
		for (int i = 0; i < PunktListe.size(); i++)
			ret[i] = PunktListe.get(i).getZ();
		return ret;
	}

	public double[] get2DMinMaxValues() {
		double[] ret = { xMin2D, yMin2D, xMax2D, yMax2D };
		return ret;
	}

	private void set2DMinMaxValues(double[] x) {
		if (x.length < 2)
			return;
		if (Double.isInfinite(xMax2D) || xMax2D < x[0])
			xMax2D = x[0];
		if (Double.isInfinite(xMin2D) || xMin2D > x[0])
			xMin2D = x[0];
		if (Double.isInfinite(yMax2D) || yMax2D < x[1])
			yMax2D = x[1];
		if (Double.isInfinite(yMin2D) || yMin2D > x[1])
			yMin2D = x[1];
	}

	private void set3DMinMaxValues(Punkt punkt) {
		if (punkt == null)
			return;
		double t = punkt.getX();
		if (Double.isInfinite(xMax3D) || t > xMax3D)
			xMax3D = t;
		if (Double.isInfinite(xMin3D) || t < xMin3D)
			xMin3D = t;
		t = punkt.getY();
		if (Double.isInfinite(yMax3D) || t > yMax3D)
			yMax3D = t;
		if (Double.isInfinite(yMin3D) || t < yMin3D)
			yMin3D = t;
		t = punkt.getZ();
		if (Double.isInfinite(zMax3D) || t > zMax3D)
			zMax3D = t;
		if (Double.isInfinite(zMin3D) || t < zMin3D)
			zMin3D = t;
	}

	public void setColor(Color c) {
		pc = c;
	}

	public void setStroke(float ps) {
		this.ps = ps;
	}

}
