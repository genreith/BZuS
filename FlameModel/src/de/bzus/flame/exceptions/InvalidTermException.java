package de.bzus.flame.exceptions;


public class InvalidTermException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidTermException(String txt) {
		super("Invalid Term "+txt);
	} 

	public InvalidTermException() {
		super("Invalid Term");
	}

	public InvalidTermException(String arg0, Throwable arg1) {
		super("Invalid Term "+arg0, arg1);
	}

	public InvalidTermException(Throwable arg0) {
		super(arg0);
	}

}
