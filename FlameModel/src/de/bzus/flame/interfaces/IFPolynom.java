package de.bzus.flame.interfaces;

import de.bzus.flame.common.Polynom;
import de.bzus.flame.common.WSGComplex;

public interface IFPolynom {

	int getDegree();

	WSGComplex valueAt(double x);

	WSGComplex valueAt(WSGComplex x);

	Polynom add(Polynom p2);

	Polynom subtract(Polynom p2);

	Polynom multiply(double fakt);

	Polynom multiply(WSGComplex fakt);

	Polynom multiply(Polynom p2);

	String toString();

}