package turtlepp.datatypes;

import turtlepp.InterpreterException;

/**
 * An array of a specific data type.
 * 
 * @author Nicolas Winkler
 * 
 */
public class Array extends Variable {
	/**
	 * the data type of the array elements
	 */
	Class<? extends Variable[]> contentType;

	/**
	 * the data
	 */
	public Variable[] array;

	private Array(String name) {
		super(name);
		array = null;
	}

	public Array(String name, Class<? extends Variable[]> contentType) {
		this(name);
		this.contentType = contentType;
	}

	public Array(String name, Variable[] content) {
		this(name);
		this.array = content;
		this.contentType = content.getClass();
	}

	@Override
	public Variable cast(Class<? extends Variable> type) {
		throw new InterpreterException("cannot cast array.");
	}

	@Override
	public String getTypeName() {
		return "array";
	}

	@Override
	public void set(Variable b) {
		if (b instanceof Array) {
			array = new Variable[((Array) b).array.length];
			copy(((Array) b).array);
		} else {
			throw new InterpreterException("Cannot set array to single value.");
		}
	}

	private void copy(Variable[] array2) {
		for (int i = 0; i < array2.length; i++) {
			array[i] = array2[i].getCopy(null);
		}
	}

	@Override
	public Variable getCopy(String newName) {
		Variable[] arr = new Variable[array.length];
		for (int i = 0; i < arr.length; i++) {
			if (array[i] != null) {
				arr[i] = array[i].getCopy(null);
			} else
				arr[i] = null;
		}
		Array a = new Array(newName, arr);
		return a;
	}

	@Override
	public boolean equalValue(Variable b) {
		throw new InterpreterException(
				"cannot compare variables of array type.");
	}

	@Override
	public boolean lessThan(Variable b) {
		throw new InterpreterException(
				"cannot compare variables of array type.");
	}

	@Override
	public boolean lessEqual(Variable b) {
		throw new InterpreterException(
				"cannot compare variables of array type.");
	}

	@Override
	public void add(Variable summand) {
		throw new InterpreterException(
				"cannot operate with variables of array type.");
	}

	@Override
	public void subtract(Variable subtrahend) {
		throw new InterpreterException(
				"cannot operate with variables of array type.");
	}

	@Override
	public void multiply(Variable factor) {
		throw new InterpreterException(
				"cannot operate with variables of array type.");
	}

	@Override
	public void divide(Variable dividend) {
		throw new InterpreterException(
				"cannot operate with variables of array type.");
	}

	@Override
	public void pow(Variable exponent) {
		throw new InterpreterException(
				"cannot operate with variables of array type.");
	}

	@Override
	public void joinOr(Variable v) {
		throw new InterpreterException(
				"cannot operate with variables of array type.");
	}

	@Override
	public void joinAnd(Variable v) {
		throw new InterpreterException(
				"cannot operate with variables of array type.");
	}

	@Override
	public void joinXor(Variable v) {
		throw new InterpreterException(
				"cannot operate with variables of array type.");
	}

	@Override
	public void negativate() {
		throw new InterpreterException(
				"cannot operate with variables of array type.");
	}

	@Override
	public String getStringValue() {
		String output = "[";
		for (int i = 0; i < array.length; i++) {
			output += array[i] == null ? "" : array[i].getStringValue();
			if (i < array.length - 1)
				output += ", ";
		}
		return output + "]";
	}

	@Override
	public double getDoubleValue() {
		return 0;
	}

	@Override
	public long getLongValue() {
		return 0;
	}
}
