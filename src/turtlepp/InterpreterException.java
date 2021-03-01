package turtlepp;

import turtlepp.exec.CodeLocationInfo;

/**
 * Exception thrown while running the code
 * 
 * @author Nicolas Winkler
 */
public class InterpreterException extends RuntimeException {
	private static final long serialVersionUID = -1359445494271602273L;

	/**
	 * information about the original code
	 */
	private CodeLocationInfo cli;

	/**
	 * initializes the Exception with a message
	 * 
	 * @param message
	 *            the error message
	 */
	public InterpreterException(String message) {
		super(message);
		cli = null;
	}

	/**
	 * initializes the Exception with a message and information about the
	 * location of the exception
	 * 
	 * @param message
	 *            the error message
	 * @param cli
	 *            where the exception happened
	 */
	public InterpreterException(String message, CodeLocationInfo cli) {
		super(message);
		this.cli = cli;
	}

	/**
	 * provides information about the location of the errors
	 */
	public CodeLocationInfo getCodeLocationInfo() {
		return cli;
	}

	
	public void setCodeLocationInfo(CodeLocationInfo cli) {
		this.cli = cli;
	}
}