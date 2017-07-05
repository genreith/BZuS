package de.bzus.flame.methods;

import de.bzus.flame.process.EVFlame;
import de.bzus.flame.process.Flame;
import de.bzus.flame.process.Referenzmethode;

public abstract class ReferenzmethodeEV extends Referenzmethode {

	public boolean isCompatibleWith(Class<? extends Flame> testFlameclass) {
		if (testFlameclass.equals(EVFlame.class))
			return true;
		else
			return false;
	}

	public ReferenzmethodeEV() {
		super();
		setText("Referenzmethode EV Modell");
	}

}
