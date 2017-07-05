package de.bzus.flame.common;

import de.bzus.flame.interfaces.IFPolynom;

public class Polynom implements IFPolynom {
	private WSGComplex[] koeffizienten = null;

	public int getDegree() {
		return koeffizienten.length - 1;
	}

	public Polynom(WSGComplex[] a) {
		koeffizienten = a;
	}

	public WSGComplex valueAt(double x) {
		return valueAt(new WSGComplex(x));
	}

	public WSGComplex valueAt(WSGComplex x) {
		WSGComplex ret = koeffizienten[getDegree()];
		for (int i = 1; i < koeffizienten.length; i++) {
			ret = ret.multiply(x).add(koeffizienten[getDegree() - i]);
		}
		return ret;
	}

	public Polynom add(Polynom p2) {
		int minD = this.getDegree() < p2.getDegree() ? this.getDegree() : p2.getDegree();
		int maxD = this.getDegree() > p2.getDegree() ? this.getDegree() : p2.getDegree();

		WSGComplex[] k = new WSGComplex[maxD + 1];
		for (int i = 0; i < k.length; i++) {
			if (i <= minD)
				k[i] = this.koeffizienten[i].add(p2.koeffizienten[i]);
			else
				k[i] = maxD == this.getDegree() ? this.koeffizienten[i] : p2.koeffizienten[i];
		}
		return new Polynom(k);
	}

	public Polynom subtract(Polynom p2) {
		int minD = this.getDegree() < p2.getDegree() ? this.getDegree() : p2.getDegree();
		int maxD = this.getDegree() > p2.getDegree() ? this.getDegree() : p2.getDegree();

		WSGComplex[] k = new WSGComplex[maxD + 1];
		for (int i = 0; i < k.length; i++) {
			if (i <= minD)
				k[i] = this.koeffizienten[i].subtract(p2.koeffizienten[i]);
			else
				k[i] = maxD == this.getDegree() ? this.koeffizienten[i] : p2.koeffizienten[i].negate();
		}
		return new Polynom(k);
	}

	public Polynom multiply(double fakt) {
		WSGComplex[] k = new WSGComplex[this.getDegree() + 1];

		for (int i = 0; i < getDegree() + 1; i++) {
			k[i] = koeffizienten[i].multiply(fakt);
		}
		return new Polynom(k);
	}

	public Polynom multiply(WSGComplex fakt) {
		WSGComplex[] k = new WSGComplex[this.getDegree() + 1];

		for (int i = 0; i < getDegree() + 1; i++) {
			k[i] = koeffizienten[i].multiply(fakt);
		}
		return new Polynom(k);
	}

	public Polynom multiply(Polynom p2) {
		WSGComplex[] k = new WSGComplex[this.getDegree() + p2.getDegree() + 1];

		for (int i = 0; i < getDegree() + 1; i++) {
			for (int j = 0; j < p2.getDegree() + 1; j++) {
				if (k[i + j] == null)
					k[i + j] = this.koeffizienten[i].multiply(p2.koeffizienten[j]);
				else
					k[i + j] = k[i + j].add(this.koeffizienten[i].multiply(p2.koeffizienten[j]));
			}
		}
		return new Polynom(k);
	}

	public String toString() {
		StringBuffer outS = new StringBuffer("Polynom:");
		String prefix = "\t";
		for (int i = 0; i < getDegree() + 1; i++) {
			if (!koeffizienten[i].equals(WSGComplex.ZERO)) {
				if (i > 0) {
					if (koeffizienten[i].equals(WSGComplex.ONE))
						outS.append(prefix + "x^" + i);
					else
						outS.append(prefix + koeffizienten[i] + "x^" + i);
				} else {
					outS.append(prefix + koeffizienten[i]);
				}
				prefix = " + ";
			}
		}
		outS.append("\n");
		return outS.toString();
	}
}
