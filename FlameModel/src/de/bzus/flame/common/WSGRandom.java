package de.bzus.flame.common;

import java.util.Hashtable;
import java.util.Random;

public class WSGRandom extends Random {

	private static Hashtable<Thread, WSGRandom> randomInstances = new Hashtable<Thread, WSGRandom>();

	private static Random seedRand = new Random();
	/**
	 * This class makes sure that only one instance of random number generator
	 * per thread will get created and seeded. The seed itself is a random
	 * number generated as nextLong from a static default seeded Random
	 * instance.
	 */
	private static final long serialVersionUID = -1627544832829302501L;

	public static WSGRandom getInstance(Thread threadId) {
		if (randomInstances.containsKey(threadId))
			return randomInstances.get(threadId);

		WSGRandom wr = new WSGRandom();
		long seedValue = seedRand.nextLong() ^ System.nanoTime();
		wr.setSeed(seedValue);
		Logging.getErrorLog().showError(String.format("RNG created with seeding %X", seedValue), -1);

		randomInstances.put(threadId, wr);
		return wr;
	}
	private long randCounter = 0;

	private WSGRandom() {
		super();
	}

	private long increaseRandCounter() {
		if (++randCounter % Globals.reseedCounter == 0) {
			long seedValue = seedRand.nextLong() ^ System.nanoTime();
			this.setSeed(seedValue);
			Logging.getErrorLog().showError(String.format("RNG reseeded with %X at random counter %d",
					seedValue, randCounter), -1);
		}
		return randCounter;
	}

	public double nextDouble() {
		increaseRandCounter();
		return super.nextDouble();
	}

	public int nextInt(int max) {
		increaseRandCounter();
		return max > 0 ? super.nextInt(max) : 0;
	}

}
