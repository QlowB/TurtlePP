package turtlepp.datatypes;

import turtlepp.Language;

/**
 * represents a variable created while running the program
 * 
 * @author Nicolas Winkler
 */
public abstract class Variable {

	/**
	 * the variable's name
	 */
	protected String name;

	/**
	 * initializes the variable
	 */
	protected Variable(String name) {
		if (name == null)
			this.name = null;
		else
			this.name = name.toLowerCase();
	}

	/**
	 * tries to cast the variable to another data type
	 * 
	 * @param type
	 *            the other data type
	 * @return the casted variable
	 */
	abstract public Variable cast(Class<? extends Variable> type);

	/**
	 * gets the type of the variable e.g. "int"
	 * 
	 * @return the type of the variable
	 */
	abstract public String getTypeName();

	/**
	 * sets the variable to a new value
	 * 
	 * @param b
	 *            the new value
	 */
	abstract public void set(Variable b);

	/**
	 * copies the variable
	 * 
	 * @param newName
	 *            the name of the copy
	 * @return a copy of the original variable with the specified name
	 */
	abstract public Variable getCopy(String newName);

	/**
	 * tests if two variable have the same value
	 * 
	 * @param b
	 *            the other variable
	 * @return <code>true</code> if they actually do have the same value,
	 *         <code>false</code> otherwise
	 */
	abstract public boolean equalValue(Variable b);

	/**
	 * tests if this variable is less than another one
	 * 
	 * @param b
	 *            the other variable
	 * @return <code>true</code> if this is actually less than b,
	 *         <code>false</code> otherwise
	 */
	abstract public boolean lessThan(Variable b);

	/**
	 * tests if this variable is less or equal than another one
	 * 
	 * @param b
	 *            the other variable
	 * @return <code>true</code> if this is actually less or equal than b,
	 *         <code>false</code> otherwise
	 */
	abstract public boolean lessEqual(Variable b);

	/**
	 * adds another variable to this variable
	 * 
	 * @param summand
	 *            the other summand
	 */
	abstract public void add(Variable summand);

	/**
	 * subtracts another variable from this variable
	 * 
	 * @param subtrahend
	 *            the subtrahend
	 */
	abstract public void subtract(Variable subtrahend);

	/**
	 * multiplies this variable with another one
	 * 
	 * @param factor
	 *            the other factor
	 */
	abstract public void multiply(Variable factor);

	/**
	 * divides this variable by another one
	 * 
	 * @param dividend
	 *            the dividend
	 */
	abstract public void divide(Variable dividend);

	/**
	 * exponentiates the variable
	 * 
	 * @param exponent
	 *            the exponent
	 */
	abstract public void pow(Variable exponent);

	/**
	 * sets the variable to this or v
	 * 
	 * @param v
	 *            the other or part
	 */
	public abstract void joinOr(Variable v);

	/**
	 * sets the variable to this and v
	 * 
	 * @param v
	 *            the other and part
	 */
	public abstract void joinAnd(Variable v);

	/**
	 * sets the variable to this xor v
	 * 
	 * @param v
	 *            the other xor part
	 */
	public abstract void joinXor(Variable v);

	/**
	 * sets the variable to its negative value
	 */
	abstract public void negativate();

	/**
	 * converts the content of the variable to a string
	 * 
	 * @return the value of the variable as a string
	 */
	abstract public String getStringValue();

	/**
	 * converts the content of the variable to a double
	 * 
	 * @return the value of the variable as a double
	 */
	abstract public double getDoubleValue();

	/**
	 * converts the content of the variable to a long
	 * 
	 * @return the value of the variable as a long
	 */
	abstract public long getLongValue();

	/**
	 * creates a variable from the constant expression
	 * 
	 * @param str
	 *            the constant expression
	 * @return the new variable, <code>null</code> if str is not a valid number
	 */
	public static Variable createConstant(String str) {
		if (str.matches("'.'")) {
			LogoChar c = new LogoChar(null, str.charAt(1));
			return c;
		} else if (str.contains(".")) {
			Float64 f = new Float64(null);
			f.value = Double.parseDouble(str);
			return f;
		} else if (Language.isNumeric(str)) {
			Integer64 i = new Integer64(null);
			i.value = Long.parseLong(str);
			return i;
		} else {
			return null;
		}
	}

	/**
	 * gets the name of the variable
	 * 
	 * @return the variable's name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * sets the variable's name
	 * 
	 * @param newName
	 *            the new name
	 */
	public final void setName(String newName) {
		name = newName.toLowerCase();
	}

	public static String getTypeName(Class<? extends Variable> type) {
		if (type == Bool.class) {
			return new Bool(null).getTypeName();
		}
		if (type == LogoString.class) {
			return new LogoString(null).getTypeName();
		}
		if (type == Integer64.class) {
			return new Integer64(null).getTypeName();
		}
		if (type == Float64.class) {
			return new Float64(null).getTypeName();
		}
		if (type == LogoChar.class) {
			return new LogoChar(null).getTypeName();
		}
		if (type == Reference.class) {
			return new Reference(null).getTypeName();
		}
		if (type == Array.class) {
			return new Array(null, new Variable[0]).getTypeName();
		}
		return "unknown type";
	}
}
