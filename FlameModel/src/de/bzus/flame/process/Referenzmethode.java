package de.bzus.flame.process;

import java.util.Vector;

import de.bzus.flame.common.Globals;
import de.bzus.flame.common.WSGRandom;

/**
 * @author WSG
 * 
 *         This is the superclass for all methods managing the iterations on a
 *         flame instance to determine one of several possible final states
 *
 */
public class Referenzmethode {
	
	private static Vector<Referenzmethode> methodenListe = new Vector<Referenzmethode>();
	
	public static Referenzmethode getMethodByIndex(int imethod) {
		if (imethod < methodenListe.size())
			return methodenListe.get(imethod);
		else
			return null;

	}


	public static Referenzmethode getMethodByName(String sMethod) {
		if (sMethod != null)
			for (int i = 0; i < methodenListe.size(); i++)
				if (sMethod.equalsIgnoreCase(methodenListe.get(i).Text)) {
					return methodenListe.get(i);
				}
		return null;
	}

	public static String[] getMethodenListe() {
		String[] ret = new String[methodenListe.size()];
		for (int i = 0; i < ret.length; i++)
			ret[i] = methodenListe.get(i).getText();
		return ret;
	}

	protected double parm1 = Double.NaN;

	protected double parm1Default = Double.NaN;

	protected String parm1Text = "n/a";

	protected double parm2 = Double.NaN;

	protected double parm2Default = Double.NaN;

	protected String parm2Text = "n/a";

	protected String Text = "Reference Method";

	protected String TipText = "The reference method adds a null ring for each unpaired element per amplitude.<br>"
			+ "Burning starts immediately after this with probability excitation^2 for each null pair.";

	public Referenzmethode() {
		methodenListe.addElement(this);
	}

	/**
	 * Method manages the burn process on a flame instance. Each null pair gets
	 * burned with probability exc. The flame changes get tested one by one by
	 * calculating target and selecting the best choice with a probability
	 * depending on the difference between target values
	 * 
	 * @param evs
	 *            : Flame instance to operate on
	 * 
	 * @return Flame : burned Flame instance
	 */

	public Flame burn(Flame evs) {

		WSGRandom wsgm = WSGRandom.getInstance(Thread.currentThread());

		for (int pIndex = 0; pIndex < evs.getParticleDimension(); pIndex++) {

			double exc = evs.excitement(pIndex);

			long nCountRe = evs.getNullPairsRe(pIndex);
			for (long k = 0; k < nCountRe; k++) {
				if (wsgm.nextDouble() > exc)
					continue;

				double pPlusDelta = evs.testTargetRe(pIndex, -1);
				double pMinusDelta = evs.testTargetRe(pIndex, 1);

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

				double pPlusDelta = evs.testTargetIm(pIndex, -1);
				double pMinusDelta = evs.testTargetIm(pIndex, 1);

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

	public double getParm1() {
		return parm1;
	}

	public double getParm1Default() {
		return parm1Default;
	}

	public String getParm1Text() {
		return parm1Text;
	}

	public double getParm2() {
		return parm2;
	}

	public double getParm2Default() {
		return parm2Default;
	}

	public String getParm2Text() {
		return parm2Text;
	}

	public String getText() {
		return Text;
	}

	public String getTipText() {
		return TipText;
	}

	public boolean isCompatibleWith(Class<? extends Flame> flameClass) {
		return true;
	}

	public boolean isCompatibleWith(Flame flameInstance) {
		return isCompatibleWith(flameInstance.getClass());
	}

	public boolean isCompatibleWith(String flameType) {
		if (flameType.equals(Globals.PauliModel))
			return isCompatibleWith(PauliFlame.class);
		else if (flameType.equals(Globals.EVModel))
			return isCompatibleWith(EVFlame.class);
		else
			return false;
	}

	/**
	 * This method generates the next flame state by adding nullrings and
	 * burning
	 * 
	 * @param evs
	 *            Flame instance to operate on
	 * @return Flame
	 */
	public Flame selectNext(Flame evs) {

		for (int pIndex = 0; pIndex < evs.getParticleDimension(); pIndex++) {
			evs.addNullRings(pIndex, evs.getUnpaired(pIndex));
		}

		return burn(evs);
	}

	public void setParm1(double parm1) {
		this.parm1 = parm1;
	}

	public void setParm1Default(double parm1Default) {
		this.parm1Default = parm1Default;
	}

	public void setParm1Text(String parm1Text) {
		this.parm1Text = parm1Text;
	}

	public void setParm2(double parm2) {
		this.parm2 = parm2;
	}

	public void setParm2Default(double parm2Default) {
		this.parm2Default = parm2Default;
	}

	public void setParm2Text(String parm2Text) {
		this.parm2Text = parm2Text;
	}

	public void setText(String text) {
		Text = text;
	}

	public void setTipText(String tipText) {
		TipText = tipText;
	}

}
