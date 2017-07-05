package de.bzus.flame.provider;

import de.bzus.flame.common.Logging;
import de.bzus.flame.common.WSGComplex;
import de.bzus.flame.process.Flame;
import de.bzus.flame.ui.RunFlame;
import de.bzus.graph3d.Provider3D;
import de.bzus.graph3d.Punkt;

public class ProviderFlat extends Provider3D {
	
	private Logging flameLog;

	public ProviderFlat() {
		super();
		super.name = "Amplitudes Flat";
		super.description = "<HTML>The amplitudes are mapped to x-y plane<BR>"
				+ "and z axis shows abs value of remaining amplitudes.</HTML>";

	}

	@Override
	public void beforeStart() {
		flameLog = Logging.getBatchLog("PFlat",RunFlame.loggingEnabled);
		flameLog.writeLog("\n\nStart " + super.name + ": \n\n");
		flameLog.writeLog("\tX Value\tY Value\tZ Value\n");
		
		darstellungsRaum.getViewAngle().setPerspective(0.0, 0.0, 1.0);
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


				double zValue = evSchwarm.getState().getEntry(particle, 0).getReal();
				double yValue = evSchwarm.getState().getEntry(particle, 0).getImaginary();
				double xValue = 10.0*(double) iteration;

				flameLog.writeLog(String.format("%08X (%d):\t%8.4f\t%8.4f\t%8.4f%n", evSchwarm.hashCode(), particle,
						xValue, yValue, zValue));
				return new Punkt(zValue, yValue, xValue);
	}

}
