package de.bzus.graph3d;

/**
 * ViewAngle provides methods for constructing a 2 dimensional projection out of
 * a 3 dimensional model based on a view angle. This is given by a vector resp.
 * two angles that determine its angle to the z-axis and the angle to the x-axis
 * of its projection to the x-y plane
 * 
 * @author Werner Siegfried Genreith
 */
public class ViewAngle {
	private double eX = 0;
	private double eY = 0;
	private double eZ = 0;
	private boolean initiated = false;
	private boolean hasChanged = true;

	private double phi = 0;
	private double theta = 0;
	double[] x2D = null;

	private final double[] xAchse = { 0.0, 0.0, 1.0 };

	double[] y2D = null;

	private final double[] yAchse = { 0.0, 1.0, 0.0 };

	private final double[] zAchse = { 1.0, 0.0, 0.0 };

	public ViewAngle(double theta, double phi) {
		setPerspective(theta, phi);
	}

	public ViewAngle(double z, double y, double x) {
		setPerspective(z, y, x);
	}

	public double[] get2DCoordinates(double[] c) {
		double[] ret = new double[2];
		ret[0] = getXProjektion(c);
		ret[1] = getYProjektion(c);
		return ret;
	}

	public double getPhi() {
		return phi;
	}

	public double getTheta() {
		return theta;
	}

	public double getX() {
		return eX;
	}

	public double[] getXAchse2D() {
		double[] ret = { getXProjektion(xAchse), getYProjektion(xAchse) };
		return ret;
	}

	public double getXProjektion(double[] c) {
		initProjektion();
		return DarstellungsRaum.scalarProdukt(c, x2D);
	}

	public double getXProjektion(Punkt c) {
		return getXProjektion(c.getCoordinate());
	}

	public double getY() {
		return eY;
	}

	public double[] getYAchse2D() {
		double[] ret = { getXProjektion(yAchse), getYProjektion(yAchse) };
		return ret;
	}

	public double getYProjektion(double[] c) {
		initProjektion();
		return DarstellungsRaum.scalarProdukt(c, y2D);
	}

	public double getYProjektion(Punkt c) {
		return getYProjektion(c.getCoordinate());
	}

	public double getZ() {
		return eZ;
	}

	public double[] getZAchse2D() {
		double[] ret = { getXProjektion(zAchse), getYProjektion(zAchse) };
		return ret;
	}

	private void initProjektion() {
		if (initiated)
			return;

		double[] pAchse = { getZ(), getY(), getX() };

		x2D = DarstellungsRaum.vektorProdukt(pAchse,zAchse);
		y2D = DarstellungsRaum.vektorProdukt(x2D, pAchse);
		double bx = DarstellungsRaum.betrag(x2D);
		double by = DarstellungsRaum.betrag(y2D);
		if (bx == 0 || by == 0) {
			x2D[0] = 0;
			x2D[1] = Math.sin(phi);
			x2D[2] = Math.cos(phi);

			y2D[0] = 0;
			y2D[1] = Math.cos(phi);
			y2D[2] = Math.sin(phi);
		} else {
			x2D = DarstellungsRaum.scaleVector(x2D, 1.0d / bx);
			y2D = DarstellungsRaum.scaleVector(y2D, 1.0d / by);
		}
		initiated = true;
	}

	public void setTheta(double theta) {
		setPerspective(theta, this.phi);
	}

	public void setPhi(double phi) {
		setPerspective(this.theta, phi);
	}

	/**
	 * setzt Perspektive eZ, eY, eX nach polarer Blickrichtung
	 * 
	 * @param theta
	 *            Breitengrad
	 * @param phi
	 *            Laengengrad
	 */
	public void setPerspective(double theta, double phi) {
		initiated = false;
		hasChanged = true;
		double pi2 = 2 * Math.PI;
		while (theta > Math.PI)
			theta = Math.PI;
		while (theta < 0.0)
			theta = 0.0;
		while (phi >= pi2)
			phi -= pi2;
		while (phi < 0.0)
			phi += pi2;

		this.phi = phi;
		this.theta = theta;
		eZ = Math.cos(theta);
		eY = Math.sin(theta) * Math.cos(phi);
		eX = Math.sin(theta) * Math.sin(phi);
	}

	/**
	 * setzt Perspektive und normiert auf Einheitsvektor
	 * 
	 * @param z
	 * @param y
	 * @param x
	 */
	public void setPerspective(double z, double y, double x) {
		initiated = false;
		hasChanged = true;
		double b = Math.sqrt(x * x + y * y + z * z);
		if (b == 0)
			return;
		eX = x / b;
		eY = y / b;
		eZ = z / b;
		theta = Math.acos(eZ);
		phi = eY == 0 ? (eX == 0 ? 0 : Math.PI - Math.atan(eY / eX) / 2) : Math
				.atan(eX / eY);
	}

	public boolean hasChanged() {
		if (hasChanged) {
			hasChanged = false;
			return true;
		} else {
			return false;
		}
	}
}
