package turtlepp.datatypes;

import turtlepp.InterpreterException;

/**
 * exception; thrown when an invalid operation wants to be done. e.g. use the
 * "less than" operator on boolean values.
 * 
 * @author Nicolas Winkler
 * 
 */
public class InvalidOperationException extends InterpreterException {
	private static final long serialVersionUID = 4185578809122440301L;

	public InvalidOperationException(String msg) {
		super(msg);
	}
}
