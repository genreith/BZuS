package de.bzus.flame.provider;

import de.bzus.flame.common.Logging;
import de.bzus.flame.process.Flame;
import de.bzus.flame.ui.RunFlame;
import de.bzus.graph3d.Provider3D;
import de.bzus.graph3d.Punkt;

public class ProviderHistory extends Provider3D {
	
	private Logging flameLog;

	public ProviderHistory() {
		super();
		super.name = "Amplitudes History";
		super.description = "<HTML>The absolute amplitudes evolution is mapped to xz plane<BR>"
				+ "and sqrt of entropy evolution to xy plane, where x axis shows the iteration count.</HTML>";

	}

	@Override
	public void beforeStart() {
		flameLog = Logging.getBatchLog("PFlat",RunFlame.loggingEnabled);
		flameLog.writeLog("\n\nStart " + super.name + ": \n\n");
		flameLog.writeLog("\tX Value\tY Value\tZ Value\n");
		
		darstellungsRaum.getViewAngle().setPerspective(0.0, 1.0, 0.0);
	}

	@Override
	public void cleanup() {
		flameLog.closeLog();
	}
	@Override
	public Punkt transform3D(Flame evSchwarm, int particle, int iteration) {
		
				double xValue = 10*(double) iteration;
				double yValue = Math.sqrt(evSchwarm.getNullPairs(particle));
				double zValue = Math.sqrt(evSchwarm.getStateAmplitude(particle).absQ());

				flameLog.writeLog(String.format("%08X (%d):\t%8.4f\t%8.4f\t%8.4f%n", evSchwarm.hashCode(), particle,
						xValue, yValue, zValue));
				return new Punkt(zValue, yValue, xValue);
	}

}
