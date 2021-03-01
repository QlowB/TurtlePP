package turtlepp.datatypes;

import turtlepp.InterpreterException;

/**
 * exception; thrown when an invalid cast wants to be done.
 * 
 * @author Nicolas Winkler
 */
public class InvalidTypecastException extends InterpreterException {
	private static final long serialVersionUID = 3529615788309034135L;

	public InvalidTypecastException(String msg) {
		super(msg);
	}
}
