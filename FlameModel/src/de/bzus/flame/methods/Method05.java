package de.bzus.flame.methods;

import de.bzus.flame.common.WSGRandom;
import de.bzus.flame.process.Flame;
import de.bzus.flame.process.Referenzmethode;

public class Method05 extends Referenzmethode {
	public Method05() {
		super();
		setText("Experimental05");
	setTipText(super.TipText + 
			"<br>The method uses global excitations and targets instead of per amplitude." +
			"<br>However convergence and statistics are worse."
			);
	}
	
	public Flame burn(Flame evs) {
	
		WSGRandom wsgm = WSGRandom.getInstance(Thread.currentThread());
		double exc = evs.excitement();
	
		for (int pIndex = 0; pIndex < evs.getParticleDimension(); pIndex++) {
	
	
			long nCountRe = evs.getNullPairsRe(pIndex);
			for (long k = 0; k < nCountRe; k++) {
				if (wsgm.nextDouble() > exc)
					continue;
	
				double pPlusDelta = evs.testGlobalTargetRe(pIndex, -1);
				double pMinusDelta = evs.testGlobalTargetRe(pIndex, 1);
	
				double r = wsgm.nextDouble() * (pPlusDelta + pMinusDelta) - pMinusDelta;
				if (r > 0) {
					evs.eliminateRe(pIndex, 1);
				} else {
					evs.eliminateRe(pIndex, -1);
				}
	
			}
	
			long nCountIm = evs.getNullPairsIm(pIndex);
			for (long k = 0; k < nCountIm; k++) {
				if (wsgm.nextDouble() > exc)
					continue;
	
				double pPlusDelta = evs.testGlobalTargetIm(pIndex, -1);
				double pMinusDelta = evs.testGlobalTargetIm(pIndex, 1);
	
				double r = wsgm.nextDouble() * (pPlusDelta + pMinusDelta) - pMinusDelta;
				if (r > 0) {
					evs.eliminateIm(pIndex, 1);
				} else {
					evs.eliminateIm(pIndex, -1);
				}
	
			}
	
		}
		return evs;
	}
	


}
