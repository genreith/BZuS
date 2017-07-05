package de.bzus.flame.common;

import de.bzus.flame.exceptions.InvalidTermException;
/*
 * Copyright 2008, 2009 Siegfried Genreith
 */

/**
 * Term enables some calculations on objects implementing the SwarmIF or IFTerm
 * interface. Terms get constructed using +, -, *, / or #
 * 
 * @author Werner Siegfried Genreith
 */
public class Term {
	private static String Brackets = "()[]{}";
	private static final int MAXRECURSION = 1000;

	private String myTerm = null;

	public Term(String term) {
		myTerm = term;
	}

	public Term(WSGComplex term) {
		myTerm = term.toString();
	}

	private int closeBracket(String term, int vonPos) {
		return closeBracket(term, vonPos, Brackets);
	}

	private int closeBracket(String term, int vonPos, String brackets) {
		if (brackets.length() < 2) // zB brackets = "()[]{}"
			return term.length();
		int bcount = 0;
		int i;
		int bIndex = brackets.indexOf(term.charAt(vonPos));
		if (bIndex + 1 >= brackets.length())
			return term.length();
		for (i = vonPos; i < term.length(); i++) {
			if (term.charAt(i) == brackets.charAt(bIndex))
				bcount++;
			else if (term.charAt(i) == brackets.charAt(bIndex + 1))
				bcount--;

			if (bcount == 0)
				break;
		}
		return i;
	}

	private WSGComplex evaluate(String term, int evalCtr) throws InvalidTermException {
		if (evalCtr > MAXRECURSION)
			return WSGComplex.ZERO;

		if (term == null)
			throw new InvalidTermException("no term given");

		term = term.trim();
		if (term.length() == 0)
			throw new InvalidTermException("no term given");

		evalCtr++;
		WSGComplex ret = null;

		try {
			ret = WSGComplex.valueOf(term);
		} catch (NumberFormatException e1) {
			ret = parse(term, evalCtr);
		}

		return ret;
	}

	private int nextOp(String term, int vonPos) {
		int i;
		for (i = vonPos; i < term.length(); i++) {
			if (Brackets.indexOf(term.charAt(i)) >= 0)
				i = closeBracket(term, i);
			if (term.charAt(i) == '*')
				return i;
		}
		return 0;
	}

	private int nextPM(String term, int vonPos) {
		int i;
		for (i = vonPos; i < term.length(); i++) {
			if (Brackets.indexOf(term.charAt(i)) >= 0)
				i = closeBracket(term, i);
			if (term.charAt(i) == '+' || term.charAt(i) == '#' || term.charAt(i) == '-')
				return i;

		}
		return 0;
	}

	private WSGComplex parse(String term, int evalCtr) throws InvalidTermException {

		if (evalCtr > MAXRECURSION)
			return WSGComplex.ZERO;

		if (term == null)
			throw new InvalidTermException("no term given");

		term = term.trim();
		if (term.length() == 0)
			throw new InvalidTermException("no term given");

		evalCtr++;
		int pos = 0;
		for (pos = 0; pos < term.length(); pos++) {
			try {
				switch (term.charAt(pos)) {
				case '-':
					return evaluate(term.substring(0, pos), evalCtr)
							.subtract(evaluate(term.substring(pos + 1, term.length()), evalCtr));
				case '+':
					return evaluate(term.substring(0, pos), evalCtr)
							.add(evaluate(term.substring(pos + 1, term.length()), evalCtr));

				case '*':
					if (nextPM(term, pos) > 0) {
						pos = nextPM(term, pos) - 1;
						break;
					} else {
						return evaluate(term.substring(0, pos), evalCtr)
								.multiply(evaluate(term.substring(pos + 1, term.length()), evalCtr));
					}

				case '^':
					if (nextPM(term, pos) > 0) {
						pos = nextPM(term, pos) - 1;
						break;
					} else if (nextOp(term, pos) > 0) {
						pos = nextOp(term, pos) - 1;
						break;
					} else {
						try {
							int pot = Integer.valueOf(term.substring(pos + 1, term.length()));
							return evaluate(term.substring(0, pos), evalCtr).pow(pot);
						} catch (NumberFormatException e) {
							throw new InvalidTermException(term);
						}
					}
				default:
					if (Brackets.indexOf(term.charAt(pos)) >= 0) {
						pos = closeBracket(term, pos);
						if (pos >= term.length())
							throw new InvalidTermException(term);
					}
				}
			} catch (ClassCastException e2) {
				throw new InvalidTermException(term);
			}

		}

		throw new InvalidTermException(term);

	}

	public WSGComplex valueOf() throws InvalidTermException {
		return evaluate(myTerm, 0);
	}

}