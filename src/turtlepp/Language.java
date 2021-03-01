package turtlepp;

import java.util.ArrayList;

import turtlepp.datatypes.MathConstant;
import turtlepp.datatypes.Variable;
import turtlepp.exec.Function;
import turtlepp.exec.NativeFunction;

/**
 * contains several constants and function used for parsing and compiling code
 * 
 * @author Nicolas Winkler
 * 
 */
public final class Language {

	/**
	 * constructor not visible, there should never be an instance of this class
	 */
	private Language() {
	}

	/**
	 * keywords (the ones that appear blue in the editor)
	 */
	private static String[] keywords;

	/**
	 * names of the native functions
	 */
	private static String[] nativeFunctions;

	/**
	 * names of math constants (like pi, e, sqrt2)
	 */
	private static String[] mathConstants;

	/**
	 * names of functions that can be invoked by the program like 'mousePressed'
	 */
	private static String[] invokedFunctions;

	static {
		keywords = new String[] { "clear", "penUp", "antialiasingOn",
				"antialiasingOff", "penDown", "pushPosition", "popPoisition",
				"pushMatrix", "popMatrix", "resetMatrix", "loadIdentity",
				"exit", "forward", "fd", "backward", "bw", "right", "rt",
				"left", "lt", "rotate", "skew", "int", "float", "setPosition",
				"setPos", "translate", "scale", "point", "line", "triangle",
				"if", "else", "sub", "repeat", "end", "showTurtle", "st",
				"hideTurtle", "ht", "print", "clearOutput", "while",
				"resetRotation", "penColor", "ref", "function", "polygon",
				"pu", "pd", "string", "sleep", "boolean", "and", "or", "xor",
				"reset", "char", "setLength", "ellipse" };

		ArrayList<Function> nf = NativeFunction.getNativeFunctions();
		nativeFunctions = new String[nf.size()];
		for (int i = 0; i < nf.size(); i++) {
			nativeFunctions[i] = nf.get(i).getName();
		}

		ArrayList<Variable> mc = MathConstant.getConstants();
		mathConstants = new String[mc.size()];
		for (int i = 0; i < mc.size(); i++) {
			mathConstants[i] = mc.get(i).getName();
		}

		invokedFunctions = new String[] { "draw", "mouseClicked",
				"mousePressed", "mouseReleased", "mouseMoved", "mouseDragged" };
	}

	/**
	 * @return an array containing all keywords used in MyLogo
	 */
	public static String[] getKeywords() {
		return keywords;
	}

	/**
	 * @return an array containing all native functions provided by MyLogo
	 */
	public static String[] getNativeFunctions() {
		return nativeFunctions;
	}

	/**
	 * @return an array containing all mathematical constants
	 */
	public static String[] getMathConstants() {
		return mathConstants;
	}

	/**
	 * 
	 * @return an array containing the names of all callback functions that can
	 *         be invoked by the program like 'mousePressed'
	 */
	public static String[] getInvokedFunctions() {
		return invokedFunctions;
	}

	/**
	 * determines if a token can be parsed to a number
	 * 
	 * @param token
	 *            the toke to test
	 * @return <code>true</code> if the token is numeric, <code>false</code>
	 *         otherwise.
	 */
	public static boolean isNumeric(String token) {
		return token.matches("\\d+(\\.\\d*((E|e)(-|\\+)?\\d*)?)?")
				|| token.matches("\\.\\d+((E|e)(-|\\+)?\\d*)?");
	}
}
