package turtlepp.datatypes;

import turtlepp.InterpreterException;

/**
 * Variable type similar to a pointer
 * 
 * @author Nicolas Winkler
 * 
 */
public class Reference extends Variable {
	/**
	 * the object this reference is pointing to
	 */
	public Variable wrapped;

	public Reference(Variable wrap, String name) {
		super(name);
		this.wrapped = wrap;
	}

	public Reference(String name) {
		super(name);
		this.wrapped = null;
	}

	@Override
	public void add(Variable b) {
		wrapped.add(b);
	}

	@Override
	public void subtract(Variable b) {
		wrapped.subtract(b);
	}

	@Override
	public void multiply(Variable b) {
		wrapped.multiply(b);
	}

	@Override
	public void divide(Variable b) {
		wrapped.divide(b);
	}

	@Override
	public void pow(Variable b) {
		wrapped.pow(b);
	}

	@Override
	public void joinOr(Variable v) {
		wrapped.joinOr(v);
	}

	@Override
	public void joinAnd(Variable v) {
		wrapped.joinAnd(v);
	}

	@Override
	public void joinXor(Variable v) {
		wrapped.joinXor(v);
	}

	@Override
	public Variable cast(Class<? extends Variable> type) {
		if (wrapped != null)
			return wrapped.cast(type);
		throw new InterpreterException(
				"reference must always be initialized before dereferencing!");
	}

	@Override
	public String getTypeName() {
		if (wrapped == null)
			return "ref";
		else
			return "ref to " + wrapped.getTypeName();
	}

	@Override
	public void set(Variable b) {
		if (wrapped == null) {
			wrapped = b;
		} else
			wrapped.set(b);
	}

	@Override
	public Variable getCopy(String newName) {
		return new Reference(wrapped, newName);
	}

	@Override
	public String getStringValue() {
		if (wrapped == null)
			return "empty reference";
		else if (wrapped.name != null)
			return "reference to -> " + wrapped.name + " = "
					+ wrapped.getStringValue();
		else
			return "reference to -> " + wrapped.getStringValue();
	}

	@Override
	public boolean equalValue(Variable b) {
		return wrapped.equalValue(b);
	}

	@Override
	public boolean lessThan(Variable b) {
		return wrapped.lessThan(b);
	}

	@Override
	public boolean lessEqual(Variable b) {
		return wrapped.lessEqual(b);
	}

	@Override
	public double getDoubleValue() {
		if (wrapped != null)
			return wrapped.getDoubleValue();
		return 0;
	}

	@Override
	public long getLongValue() {
		if (wrapped != null)
			return wrapped.getLongValue();
		return 0;
	}

	@Override
	public void negativate() {
		if (wrapped != null)
			wrapped.negativate();
	}
}
