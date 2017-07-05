package de.bzus.flame.methods;

import de.bzus.flame.process.Flame;
import de.bzus.flame.process.PauliFlame;
import de.bzus.flame.process.Referenzmethode;

public abstract class ReferenzmethodePauli extends Referenzmethode {

	public boolean isCompatibleWith(Class<? extends Flame> testFlameclass) {
		if (testFlameclass.equals(PauliFlame.class))
			return true;
		else
			return false;
	}

	public ReferenzmethodePauli() {
		super();
		setText("Referenzmethode Pauli Modell");
	}
	
}
