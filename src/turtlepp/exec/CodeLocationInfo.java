package turtlepp.exec;

/**
 * Provides information about the origin of one specific command. If there is an
 * exception, the interpreter can use this class to get to the line of code
 * where the exception was caused.
 * 
 * @author Nicolas Winkler
 * 
 */
public class CodeLocationInfo {
	/**
	 * Information about the origin of a command
	 * 
	 * @author Nicolas Winkler
	 * 
	 */
	public static enum Domain {
		CODE, CONSOLE
	}

	/**
	 * the index of the line
	 */
	protected int lineNumber;

	/**
	 * where the error occurred
	 */
	private Domain domain;

	/**
	 * creates a new information about a specific command
	 * 
	 * @param lineNumber
	 *            the original line in the code
	 */
	public CodeLocationInfo(int lineNumber) {
		this.lineNumber = lineNumber;
		setDomain(Domain.CODE);
	}

	/**
	 * creates a new information about an error in a specific domain
	 * 
	 * @param domain
	 *            the domain where the error occurred
	 */
	public CodeLocationInfo(Domain domain) {
		this.lineNumber = 0;
		this.setDomain(domain);
	}

	/**
	 * gets the index of the line this {@link CodeLocationInfo} comes from
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	/**
	 * sets the index of the line this {@link CodeLocationInfo} comes from
	 */
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	/**
	 * determines weather the command was typed into the console or run from the
	 * code window
	 */
	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
}
