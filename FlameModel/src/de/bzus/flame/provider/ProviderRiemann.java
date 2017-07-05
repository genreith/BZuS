package de.bzus.flame.provider;

import de.bzus.flame.common.Logging;
import de.bzus.flame.common.WSGComplex;
import de.bzus.flame.process.Flame;
import de.bzus.flame.ui.RunFlame;
import de.bzus.graph3d.Provider3D;
import de.bzus.graph3d.Punkt;

public class ProviderRiemann extends Provider3D {
	private Logging flameLog;
	
	public ProviderRiemann() {
		super();
		super.name = "Amplitudes on Riemann sphere";
		super.description = "<HTML>The amplitudes get mapped to a Riemann sphere<BR>"
				+ "with radius yj = abs of remaining amplitudes.</HTML>";
	}

	@Override
	public void beforeStart() {
		flameLog = Logging.getBatchLog("PRiemann",RunFlame.loggingEnabled);
		flameLog.writeLog("\n\nStart " + super.name + ": \n\n");
		flameLog.writeLog("\tX Value\tY Value\tZ Value\n");
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


				double radius2 = yj.absQ();
				double radius = Math.sqrt(radius2);
				double lambda2 = xj.absQ();

				double xValue = 2.0 * xj.getReal() * radius;
				double yValue = 2.0 * xj.getImaginary() * radius;
				double zValue = lambda2 - radius2;
				double fakt = radius / (lambda2 + radius2);
				flameLog.writeLog(String.format("%08X (%d):\t%8.4f\t%8.4f\t%8.4f%n", evSchwarm.hashCode(), particle,
						xValue, yValue, zValue));

				return new Punkt(zValue * fakt, yValue * fakt, xValue * fakt);
	}

}
