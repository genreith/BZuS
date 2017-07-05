package de.bzus.flame.interfaces;

import de.bzus.flame.process.Flame;

public interface IFResultsList {

	Flame[][] getHistories();

	Flame[] getHistory(int msmt);

}