package de.bzus.flame.provider;

import de.bzus.flame.common.Logging;
import de.bzus.flame.common.WSGComplex;
import de.bzus.flame.process.Flame;
import de.bzus.flame.ui.RunFlame;
import de.bzus.graph3d.Provider3D;
import de.bzus.graph3d.Punkt;

public class ProviderFlame extends Provider3D {
	private Logging flameLog;

	public ProviderFlame() {
		super();
		super.name = "Multi Dimensional Map";
		super.description = "<HTML>" 
		+ "The visualization transforms the amplitudes into space<br>"
		+ "by applying the Pauli matrices" 
		+ "</HTML>";
	}

	@Override
	public void beforeStart() {
		flameLog = Logging.getBatchLog("PFlame", RunFlame.loggingEnabled);
		flameLog.writeLog("\n\nStart " + super.name + ": \n\n");
		flameLog.writeLog("\tT Value\tX Valuef\tY Value\tZValue\t\tMinkNorm\n");
	}

	@Override
	public void cleanup() {
		flameLog.closeLog();
	}

	@Override
	public Punkt transform3D(Flame evSchwarm, int particle, int iteration) {

		WSGComplex xj = evSchwarm.getState().getEntry(particle, 0);

		WSGComplex yj;
		if (evSchwarm.getStateDimension() > 2) {
			yj = new WSGComplex(Math.sqrt(evSchwarm.absQ() - xj.absQ()));
		} else {
			int ad = particle == 0 ? 1 : 0;
			yj = evSchwarm.getState().getEntry(ad, 0);
		}

		double tValue = evSchwarm.absQ();
		double xValue = yj.conjugate().multiply(xj).add(xj.conjugate().multiply(yj)).getReal();
		double yValue = yj.conjugate().multiply(xj).subtract(xj.conjugate().multiply(yj)).multiply(WSGComplex.ImagUNIT)
				.getReal();
		double zValue = xj.absQ() - yj.absQ();

		flameLog.writeLog(String.format("%08X (%d) |\t%8.4f\t%8.4f\t%8.4f\t%8.4f\t| =  \t%8.4f%n", evSchwarm.hashCode(),
				particle, tValue, xValue, yValue, zValue,
				tValue * tValue - xValue * xValue - yValue * yValue - zValue * zValue));

		return new Punkt(zValue, yValue, xValue);
	}

}
