package de.bzus.flame.common;

import de.bzus.flame.exceptions.InvalidTermException;

public class WSGMatrix {

	public static WSGMatrix P_1_1 = new WSGMatrix(new WSGComplex[][] { { WSGComplex.ONE }, { WSGComplex.ONE } });

	public static WSGMatrix P_1_0 = new WSGMatrix(new WSGComplex[][] { { WSGComplex.ONE }, { WSGComplex.ZERO } });

	public static WSGMatrix P_0_1 = new WSGMatrix(new WSGComplex[][] { { WSGComplex.ZERO }, { WSGComplex.ONE } });

	public static WSGMatrix ONE = new WSGMatrix(
			new WSGComplex[][] { { WSGComplex.ONE, WSGComplex.ZERO }, { WSGComplex.ZERO, WSGComplex.ONE } });

	public static WSGMatrix P0 = ONE;

	public static WSGMatrix P1 = new WSGMatrix(
			new WSGComplex[][] { { WSGComplex.ZERO, WSGComplex.ONE }, { WSGComplex.ONE, WSGComplex.ZERO } });

	public static WSGMatrix P2 = new WSGMatrix(new WSGComplex[][] { { WSGComplex.ZERO, WSGComplex.ImagUNIT.negate() },
			{ WSGComplex.ImagUNIT, WSGComplex.ZERO } });

	public static WSGMatrix P3 = new WSGMatrix(
			new WSGComplex[][] { { WSGComplex.ONE, WSGComplex.ZERO }, { WSGComplex.ZERO, WSGComplex.MinusONE } });

	private static WSGComplex[][] rollOverData = {
			{ new WSGComplex(0.5), new WSGComplex(0.5), new WSGComplex(0.5), new WSGComplex(0.5) },
			{ new WSGComplex(0, -0.5), new WSGComplex(0, 0.5), new WSGComplex(0, -0.5), new WSGComplex(0, 0.5) },
			{ new WSGComplex(0, 0.5), new WSGComplex(0, 0.5), new WSGComplex(0, -0.5), new WSGComplex(0, -0.5) },
			{ new WSGComplex(0.5), new WSGComplex(-0.5), new WSGComplex(-0.5), new WSGComplex(0.5) } };

	private static WSGComplex[][] transToPauliBaseData = {
			{ new WSGComplex(0.5), new WSGComplex(0.0), new WSGComplex(0.0), new WSGComplex(0.5) },
			{ new WSGComplex(0.0), new WSGComplex(0.5), new WSGComplex(0.0, 0.5), new WSGComplex(0.0) },
			{ new WSGComplex(0.0), new WSGComplex(0.5), new WSGComplex(0.0, -0.5), new WSGComplex(0.0) },
			{ new WSGComplex(0.5), new WSGComplex(0.0), new WSGComplex(0.0), new WSGComplex(-0.5) } };

	public static WSGMatrix ZERO = new WSGMatrix(
			new WSGComplex[][] { { WSGComplex.ZERO, WSGComplex.ZERO }, { WSGComplex.ZERO, WSGComplex.ZERO } });

	public static String getCSVHeader(char separator, int rowDim, int colDim) {
		return getCSVHeader(separator, "M_", rowDim, colDim);

	}

	public static String getCSVHeader(char separator, String prefix, int rowDim, int colDim) {
		StringBuffer out = new StringBuffer();
		for (int irow = 0; irow < rowDim; irow++) {
			for (int icol = 0; icol < colDim; icol++) {
				if (colDim > 1 && rowDim > 1)
					out.append(
							WSGComplex.getCSVHeader(separator, String.format("%sR%dC%d_", prefix, irow + 1, icol + 1)));
				else if (rowDim > 1)
					out.append(WSGComplex.getCSVHeader(separator, String.format("%sRow%d_", prefix, irow + 1)));
				else if (colDim > 1)
					out.append(
							WSGComplex.getCSVHeader(separator, String.format("%sCol%d_", prefix, icol + 1)));
				else 
					out.append(WSGComplex.getCSVHeader(separator, String.format("%s_", prefix)));

			}
		}
		return out.toString();
	}

	public static WSGMatrix getDiagonalMatrix(WSGComplex[] diags) {
		int dim = diags.length;
		WSGComplex[][] mData = new WSGComplex[dim][dim];
		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < dim; j++) {
				if (i == j)
					mData[i][j] = diags[i];
				else
					mData[i][j] = WSGComplex.ZERO;
			}
		}
		return new WSGMatrix(mData);
	}

	public static WSGMatrix getIdentity(int dim) {
		WSGComplex[][] mData = new WSGComplex[dim][dim];
		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < dim; j++) {
				if (i == j)
					mData[i][j] = WSGComplex.ONE;
				else
					mData[i][j] = WSGComplex.ZERO;
			}
		}
		return new WSGMatrix(mData);

	}

	public static WSGMatrix getMatrixFromEigenvektoren(WSGComplex[][] ev, WSGComplex[] ew) throws InvalidTermException {
		WSGComplex[][] mEWD = new WSGComplex[ev.length][ev[0].length];
		for (int i = 0; i < ev.length; i++) {
			for (int j = 0; j < ev.length; j++) {
				mEWD[i][j] = i == j ? ew[i] : WSGComplex.ZERO;
			}
		}
		WSGMatrix mEW = new WSGMatrix(mEWD);
		WSGMatrix mEV = new WSGMatrix(ev);
		WSGMatrix mEVInv = mEV.inverse();

		WSGMatrix m = mEV.operate(mEW).operate(mEVInv);

		// m.eigenVektorenData = ev;
		m.eigenVektorenMatrix = mEV;
		m.eigenwerte = ew;

		m.isCalculatedEigenVektoren = true;
		m.isCalculatedEigenwerte = true;

		return m;
	}

	// bzgl.
	// kanonischer
	// Basis
	private Polynom cPolynom = null;

	private WSGMatrix eigenVektorenMatrix = null;

	private WSGMatrix inverseMatrix = null;

	private WSGComplex[] eigenwerte = new WSGComplex[2];

	private boolean isCalculatedCPolynom = false;

	private boolean isCalculatedEigenVektoren = false;

	private boolean isCalculatedEigenwerte = false;

	private boolean isCalculatedInverse = false;

	private WSGComplex[][] matrixData = null;

	public WSGMatrix(WSGComplex[] field) {
		matrixData = new WSGComplex[field.length][1];
		for (int i = 0; i < field.length; i++)
			matrixData[i][0] = field[i];
	}

	public WSGMatrix(WSGComplex[][] field) {
		matrixData = new WSGComplex[field.length][field[0].length];
		for (int i = 0; i < field.length; i++)
			for (int j = 0; j < field[0].length; j++)
				matrixData[i][j] = field[i][j];
	}

	public WSGMatrix add(WSGMatrix mat) throws InvalidTermException {
		int orow = this.getRowDimension();
		int icol = this.getColumnDimension();
		int irow = mat.getRowDimension();
		int ocol = mat.getColumnDimension();

		if (icol != ocol || irow != orow)
			throw new InvalidTermException("Matrix dimensions do not match");

		WSGComplex[][] dMat = new WSGComplex[orow][ocol];
		for (int orind = 0; orind < this.getRowDimension(); orind++) {
			for (int ocind = 0; ocind < ocol; ocind++) {
				dMat[orind][ocind] = new WSGComplex(this.matrixData[orind][ocind].add(mat.getEntry(orind, ocind)));
			}
		}
		return new WSGMatrix(dMat);

	}

	public WSGMatrix adjungiert() {
		WSGComplex[][] adj = new WSGComplex[getColumnDimension()][getRowDimension()];

		for (int i = 0; i < getRowDimension(); i++)
			for (int j = 0; j < getColumnDimension(); j++)
				adj[j][i] = getEntry(i, j).conjugate();

		return new WSGMatrix(adj);
	}

	public WSGMatrix transponiert() {
		WSGComplex[][] trp = new WSGComplex[getColumnDimension()][getRowDimension()];

		for (int i = 0; i < getRowDimension(); i++)
			for (int j = 0; j < getColumnDimension(); j++)
				trp[j][i] = getEntry(i, j);

		return new WSGMatrix(trp);
	}

	public WSGMatrix clone() {
		return new WSGMatrix(this.matrixData);
	}

	public Polynom cPolynom() {
		if (!isCalculatedCPolynom) {
			cPolynom = cPolynom(this, -1);
			isCalculatedCPolynom = true;
		}
		return cPolynom;
	}

	private static Polynom cPolynom(WSGMatrix m, int xfree) {

		Polynom p0 = null;
		if (xfree < 0) {
			WSGComplex[] p0Data = { m.matrixData[0][0], new WSGComplex(-1) };
			if (m.getColumnDimension() > 1) {
				p0 = new Polynom(p0Data).multiply(cPolynom(m.subMatrix(0, 0), -1));
				int sign = -1;
				for (int i = 1; i < m.getColumnDimension(); i++) {
					p0 = p0.add(cPolynom(m.subMatrix(0, i), i - 1).multiply(m.matrixData[0][i]).multiply(sign));
					sign = -sign;
				}
			} else {
				p0 = new Polynom(p0Data);
			}

		} else {
			WSGComplex[] p0Data = { m.matrixData[xfree][0] };
			int sign = 1;
			for (int i = 0; i < xfree; i++)
				sign = -sign;

			if (m.getColumnDimension() > 1) {

				p0 = new Polynom(p0Data).multiply(cPolynom(m.subMatrix(xfree, 0), -1)).multiply(sign);
				for (int i = 1; i < m.getColumnDimension(); i++) {
					sign = -sign;
					p0 = p0.add(cPolynom(m.subMatrix(xfree, i), 0).multiply(m.matrixData[xfree][i]).multiply(sign));
				}
			} else {

				p0 = new Polynom(p0Data).multiply(sign);
			}
		}
		return p0;
	}

	public String debug(boolean header) {
		StringBuffer out = new StringBuffer();

		if (header) {
			for (int irow = 0; irow < getRowDimension(); irow++) {
				for (int icol = 0; icol < getColumnDimension(); icol++) {
					out.append(String.format("Row%02d/Col%02d\t", irow, icol));
				}
			}
		} else {
			for (int irow = 0; irow < getRowDimension(); irow++) {
				for (int icol = 0; icol < getColumnDimension(); icol++) {
					out.append(String.format("%s\t", matrixData[irow][icol]));
				}
			}
		}
		return out.toString();
	}

	public WSGComplex determinante() throws InvalidTermException {
		return determinante(this);
	}

	private static WSGComplex determinante(WSGMatrix mat) throws InvalidTermException {
		if (mat.getColumnDimension() != mat.getRowDimension())
			throw new InvalidTermException("non quadratic matrix");

		if (mat.getColumnDimension() == 1)
			return mat.getEntry(0, 0);

		WSGComplex result = new WSGComplex(0, 0);
		int sign = 1;
		for (int i = 0; i < mat.getColumnDimension(); i++) {
			if (sign > 0)
				result = result.add(mat.getEntry(0, i).multiply(determinante(mat.subMatrix(0, i))));
			else
				result = result.subtract(mat.getEntry(0, i).multiply(determinante(mat.subMatrix(0, i))));
			sign = -sign;
		}
		return result;

	}

	public boolean equals(WSGMatrix comp) {
		boolean isEqual = true;
		for (int irow = 0; isEqual && irow < getRowDimension(); irow++) {
			for (int icol = 0; isEqual && icol < getColumnDimension(); icol++) {
				isEqual = getEntry(irow, icol).equals(comp.getEntry(irow, icol));
			}
		}
		return isEqual;
	}

	public WSGComplex[] getColumn(int iCol) {
		WSGComplex[] col = new WSGComplex[getRowDimension()];
		for (int j = 0; j < getRowDimension(); j++)
			col[j] = getEntry(j, iCol);
		return col;
	}

	public int getColumnDimension() {
		if (matrixData == null || matrixData.length == 0 || matrixData[0] == null || matrixData[0].length == 0)
			return 0;
		else
			return matrixData[0].length;
	}

	public WSGMatrix getEigenVektoren() throws InvalidTermException {
		if (isCalculatedEigenVektoren)
			return eigenVektorenMatrix;

		if (getColumnDimension() != 2) {
			eigenVektorenMatrix = WSGMatrix.getIdentity(getColumnDimension());
		} else {
			getEigenwerte();

			WSGComplex[] ev0 = new WSGComplex[2];
			WSGComplex[] ev1 = new WSGComplex[2];

			if (matrixData[0][1].absQ() > 0) {
				ev0[0] = matrixData[0][1];
				ev0[1] = eigenwerte[0].subtract(matrixData[0][0]);

				ev1[0] = matrixData[0][1];
				ev1[1] = eigenwerte[1].subtract(matrixData[0][0]);
				ev0 = normiere(ev0);
				ev1 = normiere(ev1);
			} else if (matrixData[1][0].absQ() > 0) {
				ev0[0] = eigenwerte[0].subtract(matrixData[1][1]);
				ev0[1] = matrixData[1][0];

				ev1[0] = eigenwerte[1].subtract(matrixData[1][1]);
				ev1[1] = matrixData[1][0];
				ev0 = normiere(ev0);
				ev1 = normiere(ev1);
			} else {
				ev0[0] = new WSGComplex(1.0);
				ev0[1] = new WSGComplex();

				ev1[0] = new WSGComplex();
				ev1[1] = new WSGComplex(1.0);

			}

			if (eigenwerte[0].equals(eigenwerte[1])) {
				ev1[0] = ev0[1].conjugate();
				ev1[1] = ev0[0].conjugate().negate();
				ev0 = normiere(ev0);
				ev1 = normiere(ev1);
			}
			// Eigenvektoren in Spalten
			WSGComplex[][] eigenVektorenData = new WSGComplex[2][2];
			eigenVektorenData[0][0] = ev0[0];
			eigenVektorenData[1][0] = ev0[1];
			eigenVektorenData[0][1] = ev1[0];
			eigenVektorenData[1][1] = ev1[1];

			eigenVektorenMatrix = new WSGMatrix(eigenVektorenData);
		}
		isCalculatedEigenVektoren = true;

		return eigenVektorenMatrix;
	}

	public WSGComplex[] getEigenwerte() throws InvalidTermException {

		if (!isCalculatedEigenwerte) {
			if (getColumnDimension() != getRowDimension()) {
				throw new InvalidTermException("non quadratic matrix");
			}
			if (getRowDimension() == 2) {
				try {
					WSGComplex sp2 = spur().divide(2.0);
					WSGComplex rad = (sp2.multiply(sp2).subtract(determinante())).sqrt();

					eigenwerte[0] = sp2.subtract(rad);
					eigenwerte[1] = sp2.add(rad);
				} catch (InvalidTermException e) {
					Logging.getErrorLog().showError(e.toString(), 2);
				}
			} else {
				eigenwerte = new WSGComplex[getColumnDimension()];
				double d = 0.5 * getColumnDimension();
				for (int i = 0; i < getColumnDimension(); i++)
					eigenwerte[i] = new WSGComplex(d--);
			}
			isCalculatedEigenwerte = true;

		}
		return eigenwerte;
	}

	public WSGComplex getEntry(int row, int column) {
		return matrixData[row][column];
	}

	public WSGMatrix getHermitian() {
		try {
			return this.adjungiert().add(this).multiply(0.5);
		} catch (InvalidTermException e) {
			return this;
		}
	}

	public WSGComplex getPAmplitude(int pIndex) {
		WSGComplex ampl = new WSGComplex(0.0);
		for (int iCol = 0; iCol < 4; iCol++) {
			ampl = ampl.add(transToPauliBaseData[pIndex][iCol].multiply(matrixData[iCol / 2][iCol % 2]));
		}
		return ampl;
	}

	public WSGMatrix getRollOver() {
		WSGComplex[][] newData = new WSGComplex[2][2];
		for (int iRow = 0; iRow < 4; iRow++) {
			int oRow0 = iRow / 2;
			int oCol0 = iRow % 2;
			for (int iCol = 0; iCol < 4; iCol++) {
				int oRow1 = iCol / 2;
				int oCol1 = iCol % 2;
				if (newData[oRow0][oCol0] == null)
					newData[oRow0][oCol0] = rollOverData[iRow][iCol].multiply(matrixData[oRow1][oCol1]);
				else
					newData[oRow0][oCol0] = newData[oRow0][oCol0]
							.add(rollOverData[iRow][iCol].multiply(matrixData[oRow1][oCol1]));

			}
		}
		return new WSGMatrix(newData);
	}

	public WSGComplex[] getRow(int iRow) {
		WSGComplex[] row = new WSGComplex[getColumnDimension()];
		for (int j = 0; j < getColumnDimension(); j++)
			row[j] = getEntry(iRow, j);
		return row;
	}

	public int getRowDimension() {
		if (matrixData == null || matrixData.length == 0 || matrixData[0] == null || matrixData[0].length == 0)
			return 0;
		else
			return matrixData.length;
	}

	public WSGMatrix inverse() throws InvalidTermException {
		if (!isCalculatedInverse) {
			WSGComplex[][] inverseMatrixDat = null;

			inverseMatrixDat = new WSGComplex[getRowDimension()][getColumnDimension()];
			WSGComplex det = determinante();
			if (det.equals(new WSGComplex(0)))
				throw new InvalidTermException("Singuläre Matrix");
			int rSign = 1;
			for (int irow = 0; irow < getRowDimension(); irow++) {
				int cSign = rSign;
				for (int icol = 0; icol < getColumnDimension(); icol++) {
					WSGMatrix subMat = subMatrix(irow, icol);
					inverseMatrixDat[icol][irow] = subMat.determinante().divide(det).multiply(cSign);
					cSign = -cSign;
				}
				rSign = -rSign;
			}
			inverseMatrix = new WSGMatrix(inverseMatrixDat);
			isCalculatedInverse = true;
		}
		return inverseMatrix;
	}

	public WSGMatrix multiply(double fakt) {
		WSGComplex[][] result = new WSGComplex[this.getRowDimension()][this.getColumnDimension()];
		for (int i = 0; i < getRowDimension(); i++)
			for (int j = 0; j < getColumnDimension(); j++) {
				result[i][j] = new WSGComplex(getEntry(i, j).multiply(fakt));
			}
		return new WSGMatrix(result);
	}

	public WSGMatrix multiply(WSGComplex fakt) {
		WSGComplex[][] result = new WSGComplex[this.getRowDimension()][this.getColumnDimension()];
		for (int i = 0; i < getRowDimension(); i++)
			for (int j = 0; j < getColumnDimension(); j++) {
				result[i][j] = new WSGComplex(getEntry(i, j).multiply(fakt));
			}
		return new WSGMatrix(result);
	}

	private static WSGComplex[] normiere(WSGComplex[] v) {
		WSGComplex[] retV = new WSGComplex[v.length];
		int start = 0;
		while (v[start].equals(WSGComplex.ZERO)) {
			retV[start] = WSGComplex.ZERO;
			start++;
		}

		if (start >= retV.length)
			return v;

		double vAbs2 = v[start].absQ();
		double vStartAbs = v[start].abs();
		retV[start] = WSGComplex.ONE;

		for (int i = start + 1; i < v.length; i++) {
			vAbs2 += v[i].absQ();
			try {
				retV[i] = v[i].divide(v[0]);
			} catch (InvalidTermException e) {
				e.printStackTrace();
			}
		}

		double fakt = vStartAbs / Math.sqrt(vAbs2);
		for (int i = start; i < v.length; i++) {
			retV[i] = retV[i].multiply(fakt);
		}
		return retV;
	}

	public WSGMatrix operate(WSGMatrix mat) throws InvalidTermException {
		int orow = this.getRowDimension();
		int icol = this.getColumnDimension();
		int irow = mat.getRowDimension();
		int ocol = mat.getColumnDimension();

		if (icol != irow)
			throw new InvalidTermException("Matrix dimensions do not match");

		WSGComplex[][] dMat = new WSGComplex[orow][ocol];
		for (int orind = 0; orind < this.getRowDimension(); orind++) {
			for (int ocind = 0; ocind < ocol; ocind++) {
				dMat[orind][ocind] = new WSGComplex(0.0);
				for (int ind = 0; ind < irow; ind++)
					try {
						dMat[orind][ocind] = dMat[orind][ocind]
								.add(this.getEntry(orind, ind).multiply(mat.getEntry(ind, ocind)));
					} catch (NullPointerException e) {
						Logging.getErrorLog()
								.showError(e.toString() + String.format(
										"dMat[%1$d][%2$d] = dMat[%1$d][%2$d].add(this.getEntry(%1$d, %3$d).multiply(mat.getEntry(%3$d, %2$d)));",
										orind, ocind, ind), 2);
					}
			}
		}
		return new WSGMatrix(dMat);
	}

	public WSGComplex spur() throws InvalidTermException {
		if (getColumnDimension() != getRowDimension())
			throw new InvalidTermException("non quadratic matrix");
		double resultRe = 0.0, resultIm = 0.0;
		for (int i = 0; i < getRowDimension(); i++) {
			resultRe += getEntry(i, i).getReal();
			resultIm += getEntry(i, i).getImaginary();
		}
		return new WSGComplex(resultRe, resultIm);
	}

	public WSGMatrix subMatrix(int row, int column) {
		WSGComplex odat[][] = new WSGComplex[getRowDimension() - 1][getColumnDimension() - 1];
		int oRow = 0;
		for (int i = 0; i < getRowDimension(); i++) {
			int oCol = 0;
			if (i == row)
				continue;
			for (int j = 0; j < getColumnDimension(); j++) {
				if (j == column)
					continue;
				odat[oRow][oCol] = new WSGComplex(getEntry(i, j));
				oCol++;
			}
			oRow++;
		}
		return new WSGMatrix(odat);
	}

	public WSGMatrix subtract(WSGMatrix mat) throws InvalidTermException {
		int orow = this.getRowDimension();
		int icol = this.getColumnDimension();
		int irow = mat.getRowDimension();
		int ocol = mat.getColumnDimension();

		if (icol != ocol || irow != orow)
			throw new InvalidTermException("Matrix dimensions do not match");

		WSGComplex[][] dMat = new WSGComplex[orow][ocol];
		for (int orind = 0; orind < this.getRowDimension(); orind++) {
			for (int ocind = 0; ocind < ocol; ocind++) {
				dMat[orind][ocind] = new WSGComplex(this.matrixData[orind][ocind].subtract(mat.getEntry(orind, ocind)));
			}
		}
		return new WSGMatrix(dMat);
	}

	public String toCSV(char separator) {
		StringBuffer out = new StringBuffer();
		for (int irow = 0; irow < getRowDimension(); irow++) {
			for (int icol = 0; icol < getColumnDimension(); icol++) {
				out.append(String.format("%s", getEntry(irow, icol).toCSV(separator)));
			}
		}
		return out.toString();
	}

	public String toString() {

		StringBuffer out = new StringBuffer();
		for (int irow = 0; irow < getRowDimension(); irow++) {
			out.append("Row " + (irow + 1) + " : (");
			for (int icol = 0; icol < getColumnDimension(); icol++) {
				out.append(new WSGComplex(getEntry(irow, icol)) + " ");
			}
			out.append(")\n");
		}

		return out.toString();
	}

}
