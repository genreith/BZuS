package de.bzus.graph3d;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.Vector;

/**
 * DarstellungsRaum holds points and paths in 3 dimensions. It provides several
 * methods for handling 3D presentation parameters. Its central method is
 * {@link #paint(Graphics2D, Dimension)} as a delegate for
 * {@link xyzGraph#paint(java.awt.Graphics)}.
 * 
 * @author Werner Siegfried Genreith
 */
public class DarstellungsRaum {
	/**
	 * Options stellt verschiedene Anzeigeparameter zur Verfügung
	 * 
	 * @author WSG
	 * 
	 */
	public static class Options {
		
		AffineTransform at = null;
		ViewAngle viewAngle = null;
		double[] zoomCenter = new double[2];

		Color bkgrdColor = Color.WHITE;
		int displayPathLen = 200;
		private long interval = 50;

		ScaleMode scaling = ScaleMode.CENTERSCALE;
		boolean showAxes = true;
		boolean showLines = true;
		boolean showPoints = true;
		Color xColor = Color.BLUE;
		Color yColor = Color.GREEN;
		Color zColor = Color.RED;
		int step = 1;
		int zoomFactor = 0;

		public long getInterval() {
			return interval;
		}

		public void setInterval(long interval) {
			this.interval = interval;
		}
	}

	/**
	 * betrag berechnet den euklidischen Betrag eines Vektors a im 3D Raum
	 * 
	 * @param a double[]
	 * @return double
	 */
	public static double betrag(double[] a) {
		return Math.sqrt(scalarProdukt(a, a));
	}

	/**
	 * scaleVector streckt einen Vektor a im 3D Raum um den Faktor b
	 * 
	 * @param a
	 *            double[3]
	 * @param b
	 *            double
	 * @return double[3] b*a
	 */
	public static double[] scaleVector(double[] a, double b) {
		if (a.length != 3)
			return a;

		double ret[] = new double[3];
		for (int i = 0; i < 3; i++)
			ret[i] = a[i] * b;
		return ret;
	}

	/**
	 * scalarProdukt berechnet das euklidische Skalarprodukt aus a und b im 3D
	 * Raum
	 * 
	 * @param a
	 *            double[3]
	 * @param b
	 *            double[3]
	 * @return double a*b
	 */
	public static double scalarProdukt(double[] a, double[] b) {
		if (a.length != 3 || b.length != 3)
			return 0.0;
		double ret = 0.0;
		for (int i = 0; i < 3; i++)
			ret += a[i] * b[i];
		return ret;
	}

	/**
	 * vektorProdukt berechnet das vektorielle Produkt im 3D Raum
	 * 
	 * @param a vector
	 * @param b vector
	 * @return vector product
	 */
	public static double[] vektorProdukt(double[] a, double[] b) {
		if (a.length != 3 || b.length != 3)
			return null;
		double[] ret = new double[3];
		ret[2] = a[1] * b[0] - a[0] * b[1]; // vx = yz - zy
		ret[1] = a[0] * b[2] - a[2] * b[0]; // vy = zx - xz
		ret[0] = a[2] * b[1] - a[1] * b[2]; // vz = xy - yx
		return ret;
	}

	private double excite = 0;
	private Punkt exciteP = null;
	private boolean hasChanged = false;

	private Options options = new Options();
	private Vector<Pfad> Pfade = new Vector<Pfad>();

	private String state = null;
	private boolean storeAt = true;

	private double xMax = Double.NEGATIVE_INFINITY;
	private double xMax3D = Double.NEGATIVE_INFINITY;
	private double xMin = Double.POSITIVE_INFINITY;
	private double xMin3D = Double.POSITIVE_INFINITY;
	private double yMax = Double.NEGATIVE_INFINITY;

	private double yMax3D = Double.NEGATIVE_INFINITY;
	private double yMin = Double.POSITIVE_INFINITY;
	private double yMin3D = Double.POSITIVE_INFINITY;
	private double zMax3D = Double.NEGATIVE_INFINITY;
	private double zMin3D = Double.POSITIVE_INFINITY;

	public DarstellungsRaum() {
		options.viewAngle = new ViewAngle(1.0, 2.0, 3.0);
	}

	/**
	 * add fügt einen Pfad dem Darstellungsraum zu und setzt Min-/Maxwerte
	 * 
	 * @param p Pfad
	 * @return Erfolg=TRUE
	 */
	public boolean add(Pfad p) {
		hasChanged = true;
		set3DMinMaxValues(p.getMinValues(), p.getMaxValues());
		return Pfade.add(p);
	}

	/**
	 * add fügt mehrere Pfade hinzu
	 * 
	 * @param p Pfade[]
	 */
	public void add(Pfad[] p) {

		for (int i = 0; i < p.length; i++)
			add(p[i]);
	}

	private Color calcAutoColor(Color inColor, double it) {
		double fakt = (options.displayPathLen - it) / options.displayPathLen;
		int rot = (int) (fakt * inColor.getRed() + (1 - fakt)
				* options.bkgrdColor.getRed());
		int gruen = (int) (fakt * inColor.getGreen() + (1 - fakt)
				* options.bkgrdColor.getGreen());
		int blau = (int) (fakt * inColor.getBlue() + (1 - fakt)
				* options.bkgrdColor.getBlue());

		return new Color(rot, gruen, blau);
	}

	public void clearAll() {
		Pfade.clear();

		xMax = Double.NEGATIVE_INFINITY;
		xMax3D = Double.NEGATIVE_INFINITY;
		xMin = Double.POSITIVE_INFINITY;

		xMin3D = Double.POSITIVE_INFINITY;
		yMax = Double.NEGATIVE_INFINITY;
		yMax3D = Double.NEGATIVE_INFINITY;

		yMin = Double.POSITIVE_INFINITY;
		yMin3D = Double.POSITIVE_INFINITY;
		zMax3D = Double.NEGATIVE_INFINITY;
		zMin3D = Double.POSITIVE_INFINITY;
	}

	public boolean delete(Pfad p) {
		hasChanged = true;
		return Pfade.remove(p);
	}

	/**
	 * zeichnet Achsen entsprechend der gewählten Perspektive = Blickrichtung
	 * 
	 * @param g
	 * @param xMin3D
	 * @param xMax3D
	 * @param yMin3D
	 * @param yMax3D
	 * @param zMin3D
	 * @param zMax3D
	 */
	private void drawAxes(Graphics2D g, double xMin3D, double xMax3D,
			double yMin3D, double yMax3D, double zMin3D, double zMax3D) {
		double[] x = options.viewAngle.getXAchse2D();
		double mx = Math.max(Math.abs(xMin3D), Math.abs(xMax3D));
		Line2D.Double xAchse = new Line2D.Double(-x[0] * mx, -x[1] * mx, x[0]
				* mx, x[1] * mx);
		double[] y = options.viewAngle.getYAchse2D();
		double my = Math.max(Math.abs(yMin3D), Math.abs(yMax3D));
		Line2D.Double yAchse = new Line2D.Double(-y[0] * my, -y[1] * my, y[0]
				* my, y[1] * my);
		double[] z = options.viewAngle.getZAchse2D();
		double mz = Math.max(Math.abs(zMin3D), Math.abs(zMax3D));
		Line2D.Double zAchse = new Line2D.Double(-z[0] * mz, -z[1] * mz, z[0]
				* mz, z[1] * mz);
		float strokeSize = 0.5f / (float) getAt().getScaleX();
		if (strokeSize < 0.0f)
			strokeSize = 0.0f;
		g.setStroke(new BasicStroke(strokeSize));
		g.setColor(options.xColor);
		g.draw(xAchse);
		// Font myFont=new Font("Arial", Font.ITALIC|Font.PLAIN, 1);
		// g.setFont( myFont ); //Schriftart setzen
		// g.drawString("x", (float) (x[0]* mx), (float)(x[1] * mx));
		// g.drawString("x", 10.0f, 10.0f);
		g.setColor(options.yColor);
		g.draw(yAchse);
		// g.drawString("y", (float) (y[0]* my), (float)(y[1] * my));
		// g.drawString("y", 10.0f, 20.0f);
		g.setColor(options.zColor);
		g.draw(zAchse);
		// g.drawString("z", (float) (z[0]* mz), (float)(z[1] * mz));
		// g.drawString("z", 10.0f, 30.0f);
	}

	public AffineTransform getAt() {
		if (options.at == null)
			options.at = new AffineTransform();
		return options.at;
	}

	public double getExcite() {
		return excite;
	}

	public Punkt getExciteP() {
		return exciteP;
	}

	public Options getOptions() {
		if (options == null)
			options = new Options();
		return options;
	}

	public ViewAngle getViewAngle() {
		return options.viewAngle;
	}

	public String getState() {
		return state;
	}

	public double getXMax3D() {
		if (Double.isInfinite(xMax3D))
			return 1.0d;
		else
			return xMax3D;
	}

	public double getXMin3D() {
		if (Double.isInfinite(xMin3D))
			return -1.0d;
		else
			return xMin3D;
	}

	public double getYMax3D() {
		if (Double.isInfinite(yMax3D))
			return 1.0d;
		else
			return yMax3D;
	}

	public double getYMin3D() {
		if (Double.isInfinite(yMin3D))
			return -1.0d;
		else
			return yMin3D;
	}

	public double getZMax3D() {
		if (Double.isInfinite(zMax3D))
			return 1.0d;
		else
			return zMax3D;
	}

	public double getZMin3D() {
		if (Double.isInfinite(zMin3D))
			return -1.0d;
		else
			return zMin3D;
	}

	public double getZoomFactor() {
		return Math.exp(options.zoomFactor / 10.0);
	}

	public boolean isStoreAt() {
		return storeAt;
	}

	public void paint(Graphics2D g, Dimension d) {
		Line2D.Double[][] lines2D = null;
		Color[][] lines2DAutoColor = null;
		Punkt[][] points2D = null;

		g.setColor(options.bkgrdColor);
		g.fill(new Rectangle(d));
		if (this.hasChanged || options.viewAngle.hasChanged()) {
			xMax = Double.NEGATIVE_INFINITY;
			xMin = Double.POSITIVE_INFINITY;
			yMax = Double.NEGATIVE_INFINITY;
			yMin = Double.POSITIVE_INFINITY;
			lines2D = new Line2D.Double[Pfade.size()][];
			points2D = new Punkt[Pfade.size()][];
			lines2DAutoColor = new Color[Pfade.size()][];

			for (int i = 0; i < Pfade.size(); i++) {
				Pfad pfad = Pfade.elementAt(i);
				int maxJ = Math.min(options.displayPathLen, pfad.getLen() - 1);
				if (maxJ > 0)
					pfad.generateSampleLast(maxJ, options.step,
							options.viewAngle);
				set2DMinMaxValues(pfad.get2DMinMaxValues());
				lines2D[i] = pfad.getLinesLast();
				lines2DAutoColor[i] = pfad.getColorsLast();
				points2D[i] = pfad.getPointsLast();
			}
		}

		AffineTransform tempAt = null;
		if (options.scaling == ScaleMode.CENTERSCALE)
			tempAt = scaleCENTER(d);
		else if (options.scaling == ScaleMode.AUTOSCALE)
			tempAt = scaleAUTO(d);
		else if (options.scaling == ScaleMode.ZOOM)
			tempAt = scaleZOOM(d);
		else if (options.scaling == ScaleMode.FIXED)
			tempAt = scaleFIXED(d);

		if (storeAt) {
			options.at = tempAt;
			storeAt = false;
		}

		g.transform(tempAt);

		// Zeichne Achsen
		if (options.showAxes) {
			if (Double.isInfinite(xMin3D))
				drawAxes(g, -1.0, 1.0, -1.0, 1.0, -1.0, 1.0);
			else
				drawAxes(g, xMin3D, xMax3D, yMin3D, yMax3D, zMin3D, zMax3D);

		}

		if (options.showPoints) {
			for (int i = 0; points2D != null && i < points2D.length; i++) {
				for (int j = 0; points2D[i] != null && j < points2D[i].length; j++) {
					// g.setColor(calcAutoColor(points2D[i][j].autoColor(), j));
					g.setColor(points2D[i][j].autoColor());
					double w = 0.5f / tempAt.getScaleX();
					if (w < 0.0f)
						w = 0.0f;
					double wa = 4 * w;
					double wb = 2 * wa;

					g.setStroke(new BasicStroke((float) w));
					double[] x = points2D[i][j]
							.get2DCoordinates(options.viewAngle);
					g.fill(new Ellipse2D.Double(x[0] - wa, x[1] - wa, wb, wb));
				}
			}
		}
		if (options.showLines) {
			for (int i = 0; lines2D != null && i < lines2D.length; i++) {
				for (int j = 0; lines2D[i] != null && j < lines2D[i].length; j++) {
					g.setColor(calcAutoColor(lines2DAutoColor[i][j], j));
					float strokeSize = Pfade.get(i).getStroke()
							/ (float) tempAt.getScaleX();
					if (strokeSize < 0.0f)
						strokeSize = 0.0f;
					g.setStroke(new BasicStroke(strokeSize));
					g.draw(lines2D[i][j]);
				}
			}
		}
	}

	private AffineTransform scaleAUTO(Dimension d) {
		storeAt = true;
		if (Double.isInfinite(xMax))
			xMax = 1.0;
		if (Double.isInfinite(xMin))
			xMin = -1.0;
		if (Double.isInfinite(yMax))
			yMax = 1.0;
		if (Double.isInfinite(yMin))
			yMin = -1.0;

		if (xMin == xMax) {
			double b = xMin;
			xMin = b - 1.0;
			xMax = b + 1.0;
		}
		if (yMin == yMax) {
			double b = yMin;
			yMin = b - 1.0;
			yMax = b + 1.0;
		}
		double zF = getZoomFactor();
		double sx = d.getWidth() / (xMax - xMin) / zF;
		double sy = d.getHeight() / (yMin - yMax) / zF;
		double tx = -sx * xMin;
		double ty = -sy * yMax;

		return new AffineTransform(sx, 0.0, 0.0, sy, tx, ty);

	}

	private AffineTransform scaleCENTER(Dimension d) {
		storeAt = true;
		double[] atMat = new double[6];
		getAt().getMatrix(atMat);

		double zF = getZoomFactor();
		double xm = Math.max(Math.abs(getXMax3D()), Math.abs(getXMin3D()));
		double ym = Math.max(Math.abs(getYMax3D()), Math.abs(getYMin3D()));
		double zm = Math.max(Math.abs(getZMax3D()), Math.abs(getZMin3D()));
		double m = Math.sqrt(xm * xm + ym * ym + zm * zm);
		double div = 2.0 * m * zF;
		atMat[0] = d.width / div;
		atMat[3] = -d.height / div;
		if (atMat[0] < -atMat[3])
			atMat[3] = -atMat[0];
		else
			atMat[0] = -atMat[3];

		atMat[4] = d.width / 2.0;
		atMat[5] = d.height / 2.0;
		return new AffineTransform(atMat);

	}

	private AffineTransform scaleFIXED(Dimension d) {
		storeAt = false;
		return getAt();
	}

	private AffineTransform scaleZOOM(Dimension d) {
		storeAt = true;
		double div = 0.0;
		int minW = Math.min(d.width, d.height);
		if (exciteP == null)
			return scaleCENTER(d);
		double[] r = { exciteP.getZ(), exciteP.getY(), exciteP.getX() };
		double[] x = options.viewAngle.get2DCoordinates(r);
		if (excite != 0.0)
			div = 2.0 * excite * (1.0 + excite) * betrag(r);
		double sx = minW / div / getZoomFactor();
		double sy = -sx;
		double tx = d.width / 2 - x[0] * sx;
		double ty = d.height / 2 - x[1] * sy;

		return new AffineTransform(sx, 0.0, 0.0, sy, tx, ty);
	}

	public void set(Pfad[] p) {
		Pfade = new Vector<Pfad>();
		for (int i = 0; i < p.length; i++)
			add(p[i]);
	}

	/**
	 * aktualisiert Minimal- und Maximalwerte der 2D Zeichenfläche
	 * 
	 * @param xyMinMax
	 *            double[4] (xMin, yMin, xMax, yMax)
	 */
	private void set2DMinMaxValues(double[] xyMinMax) {
		if (xyMinMax.length < 4)
			return;
		if (Double.isInfinite(xMax) || xMax < xyMinMax[2])
			xMax = xyMinMax[2];
		if (Double.isInfinite(xMin) || xMin > xyMinMax[0])
			xMin = xyMinMax[0];
		if (Double.isInfinite(yMax) || yMax < xyMinMax[3])
			yMax = xyMinMax[3];
		if (Double.isInfinite(yMin) || yMin > xyMinMax[1])
			yMin = xyMinMax[1];
	}

	/**
	 * aktualisiert Minimal- und Maximalwerte im 3D Pfadraum
	 * 
	 * @param min
	 *            double[3] (zMin, yMin, xMin)
	 * @param max
	 *            double[3] (zMax, yMax, xMax)
	 */
	private void set3DMinMaxValues(double[] min, double[] max) {
		if (min == null || max == null || min.length != 3 || max.length != 3)
			return;
		if (Double.isInfinite(xMin3D) || min[2] < xMin3D)
			xMin3D = min[2];
		if (Double.isInfinite(xMax3D) || max[2] > xMax3D)
			xMax3D = max[2];
		if (Double.isInfinite(yMin3D) || min[1] < yMin3D)
			yMin3D = min[1];
		if (Double.isInfinite(yMax3D) || max[1] > yMax3D)
			yMax3D = max[1];
		if (Double.isInfinite(zMin3D) || min[0] < zMin3D)
			zMin3D = min[0];
		if (Double.isInfinite(zMax3D) || max[0] > zMax3D)
			zMax3D = max[0];
	}

	public void setAt(AffineTransform at) {
		options.scaling = ScaleMode.FIXED;
		options.at = at;
	}

	public ScaleMode setAutoScale(ScaleMode as) {
		ScaleMode b = options.scaling;
		options.scaling = as;
		return b;
	}

	public void setExcite(double excite, String state, Punkt absP) {
		this.excite = excite;
		this.exciteP = absP;
		this.state = state;
	}

	public void setHasChanged(boolean hasChanged, Pfad pfad) {
		set3DMinMaxValues(pfad.getMinValues(), pfad.getMaxValues());
		this.hasChanged = hasChanged;
	}

	public void setOptions(Options options) {
		this.options = options;
	}

	public void setViewAngle(ViewAngle pt) {
		hasChanged = true;
		options.viewAngle = pt;
	}

	public void setStoreAt(boolean storeAt) {
		this.storeAt = storeAt;
	}

	public void setXMax3D(double xMax3D) {
		this.xMax3D = xMax3D;
	}

	public void setXMin3D(double xMin3D) {
		this.xMin3D = xMin3D;
	}

	public void setYMax3D(double yMax3D) {
		this.yMax3D = yMax3D;
	}

	public void setYMin3D(double yMin3D) {
		this.yMin3D = yMin3D;
	}

	public void setZMax3D(double zMax3D) {
		this.zMax3D = zMax3D;
	}

	public void setZMin3D(double zMin3D) {
		this.zMin3D = zMin3D;
	}
};
