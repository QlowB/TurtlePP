package turtlepp.datatypes;

/**
 * Variable type representing a string of characters
 * 
 * @author Nicolas Winkler
 * 
 */
public class LogoString extends Variable {
	public String value;

	public LogoString(String name) {
		super(name);
	}

	public LogoString(String name, String value) {
		super(name);
		this.value = value;
	}

	@Override
	public Variable cast(Class<? extends Variable> type) {
		try {
			if (type == Float64.class) {
				Float64 f = new Float64(null);
				f.value = Double.parseDouble(value.trim());
				return f;
			}
			if (type == Integer64.class) {
				Integer64 f = new Integer64(null);
				f.value = Long.parseLong(value.trim());
				return f;
			}
			if (type == LogoChar.class) {
				LogoChar f = new LogoChar(null);
				if (value.length() == 1)
					f.value = value.charAt(0);
				else
					f.value = (char) Integer.parseInt(value.trim());
				return f;
			}
		} catch (NumberFormatException nfe) {
			throw new InvalidTypecastException("string value " + value
					+ " cannot be parsed.");
		}
		if (type == getClass())
			return this;

		throw new InvalidTypecastException("cannot convert from "
				+ getTypeName() + " to " + Variable.getTypeName(type));
	}

	@Override
	public String getTypeName() {
		return "string";
	}

	@Override
	public void set(Variable b) {
		if (b instanceof Integer64) {
			this.value = "" + ((Integer64) b).value;
		} else if (b instanceof Float64) {
			this.value = "" + ((Float64) b).value;
		} else {
			LogoString temp = (LogoString) b.cast(LogoString.class);
			this.value = temp.value;
		}
	}

	@Override
	public Variable getCopy(String newName) {
		return new LogoString(newName, value); // value can be passed directly,
												// Strings are immutable
	}

	@Override
	public boolean equalValue(Variable b) {
		if (b instanceof LogoString) {
			return value.equals(((LogoString) b).value);
		} else {
			return value == ((LogoString) b.cast(LogoString.class)).value;
		}
	}

	@Override
	public boolean lessThan(Variable b) {
		if (b instanceof LogoString) {
			return value.compareTo(((LogoString) b).value) < 0;
		} else {
			return value
					.compareTo(((LogoString) b.cast(LogoString.class)).value) < 0;
		}
	}

	@Override
	public boolean lessEqual(Variable b) {
		if (b instanceof LogoString) {
			return value.compareTo(((LogoString) b).value) <= 0;
		} else {
			return value
					.compareTo(((LogoString) b.cast(LogoString.class)).value) <= 0;
		}
	}

	@Override
	public void add(Variable summand) {
		if (summand instanceof LogoString) {
			value += ((LogoString) summand).value;
		} else {
			value += ((LogoString) summand.cast(LogoString.class)).value;
		}
	}

	@Override
	public void subtract(Variable subtrahend) {
		throw new InvalidTypecastException("cannot subtract from a string");
	}

	@Override
	public void multiply(Variable factor) {
		throw new InvalidTypecastException("cannot multiply a string");
	}

	@Override
	public void divide(Variable dividend) {
		throw new InvalidTypecastException(
				"cannot use division operator on a string");
	}

	@Override
	public void pow(Variable exponent) {
		throw new InvalidTypecastException("cannot exponentiate a string");
	}

	@Override
	public void joinOr(Variable v) {
		throw new InvalidTypecastException(
				"logical operator 'or' cannot operate on a string.");
	}

	@Override
	public void joinAnd(Variable v) {
		throw new InvalidTypecastException(
				"logical operator 'and' cannot operate on a string.");
	}

	@Override
	public void joinXor(Variable v) {
		throw new InvalidTypecastException(
				"logical operator 'xor' cannot operate on a string.");
	}

	@Override
	public void negativate() {
		throw new InvalidTypecastException("cannot negativate a string");
	}

	@Override
	public String getStringValue() {
		return value;
	}

	@Override
	public double getDoubleValue() {
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException nfe) {
			throw new InvalidTypecastException("string value " + value
					+ " cannot be parsed.");
		}
	}

	@Override
	public long getLongValue() {
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException nfe) {
			throw new InvalidTypecastException("string value " + value
					+ " cannot be parsed.");
		}
	}

	@Override
	public String toString() {
		return "LogoString [value=" + value + "]";
	}
}
