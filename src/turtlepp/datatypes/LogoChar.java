package turtlepp.datatypes;

/**
 * A variable type representing a single character of a string
 * 
 * @author Nicolas Winkler
 */
public class LogoChar extends Variable {
	/**
	 * actual value
	 */
	public char value;

	/**
	 * Creates a new variable. It is set to zero.
	 * 
	 * @param name
	 *            the name of the new variable
	 */
	public LogoChar(String name) {
		super(name);
	}

	/**
	 * 
	 * @param name
	 * @param value
	 */
	public LogoChar(String name, char value) {
		this(name);
		this.value = value;
	}

	@Override
	public Variable cast(Class<? extends Variable> type) {
		if (type == Integer64.class) {
			Integer64 s = new Integer64(null);
			s.value = (long) value;
			return s;
		}
		if (type == Float64.class) {
			Float64 f = new Float64(null);
			f.value = (double) value;
			return f;
		}
		if (type == Bool.class) {
			Bool s = new Bool(null);
			s.value = value != 0;
			return s;
		}
		if (type == LogoString.class) {
			LogoString s = new LogoString(null);
			s.value = "" + value;
			return s;
		}
		if (type == getClass())
			return this;

		throw new InvalidTypecastException("cannot convert from "
				+ getTypeName() + " to " + Variable.getTypeName(type));
	}

	@Override
	public void add(Variable b) {
		if (b instanceof LogoChar) {
			value += ((LogoChar) b).value;
		} else if (b instanceof Integer64) {
			value += ((Integer64) b).value;
		} else if (b instanceof Float64) {
			value += ((Float64) b).value;
		} else if (b instanceof Reference) {
			add(((Reference) b).wrapped);
		} else {
			add(b.cast(getClass()));
		}
	}

	@Override
	public void subtract(Variable b) {
		if (b instanceof LogoChar) {
			value -= ((LogoChar) b).value;
		} else if (b instanceof Integer64) {
			value -= ((Integer64) b).value;
		} else if (b instanceof Float64) {
			value -= ((Float64) b).value;
		} else if (b instanceof Reference) {
			subtract(((Reference) b).wrapped);
		} else {
			subtract(b.cast(getClass()));
		}
	}

	@Override
	public void multiply(Variable b) {
		if (b instanceof LogoChar) {
			value *= ((LogoChar) b).value;
		} else if (b instanceof Integer64) {
			value *= ((Integer64) b).value;
		} else if (b instanceof Float64) {
			value *= ((Float64) b).value;
		} else if (b instanceof Reference) {
			multiply(((Reference) b).wrapped);
		} else {
			multiply(b.cast(getClass()));
		}
	}

	@Override
	public void divide(Variable b) {
		if (b instanceof LogoChar) {
			value /= ((LogoChar) b).value;
		} else if (b instanceof Integer64) {
			value /= ((Integer64) b).value;
		} else if (b instanceof Float64) {
			value /= ((Float64) b).value;
		} else if (b instanceof Reference) {
			divide(((Reference) b).wrapped);
		} else {
			divide(b.cast(getClass()));
		}
	}
	
	@Override
	public void pow(Variable exponent) {
		throw new InvalidOperationException("can't exponentiate variables of type "
				+ getTypeName());
	}

	@Override
	public void joinOr(Variable b) {
		if (b instanceof LogoChar) {
			value |= ((LogoChar) b).value;
		} else if (b instanceof Integer64) {
			value |= ((Integer64) b).value;
		} else if (b instanceof Float64) {
			value |= (long) ((Float64) b).value;
		} else if (b instanceof Reference) {
			joinOr(((Reference) b).wrapped);
		} else {
			joinOr(b.cast(getClass()));
		}
	}

	@Override
	public void joinAnd(Variable b) {
		if (b instanceof LogoChar) {
			value &= ((LogoChar) b).value;
		} else if (b instanceof Integer64) {
			value &= ((Integer64) b).value;
		} else if (b instanceof Float64) {
			value &= (long) ((Float64) b).value;
		} else if (b instanceof Reference) {
			joinAnd(((Reference) b).wrapped);
		} else {
			joinAnd(b.cast(getClass()));
		}
	}

	@Override
	public void joinXor(Variable b) {
		if (b instanceof LogoChar) {
			value ^= ((LogoChar) b).value;
		} else if (b instanceof Integer64) {
			value ^= ((Integer64) b).value;
		} else if (b instanceof Float64) {
			value ^= (long) ((Float64) b).value;
		} else if (b instanceof Reference) {
			joinXor(((Reference) b).wrapped);
		} else {
			joinXor(b.cast(getClass()));
		}
	}

	@Override
	public String getTypeName() {
		return "char";
	}

	@Override
	public void set(Variable b) {
		if (b instanceof LogoChar) {
			this.value = ((LogoChar) b).value;
		} else {
			LogoChar temp = (LogoChar) b.cast(LogoChar.class);
			this.value = temp.value;
		}
	}

	@Override
	public boolean equalValue(Variable b) {
		if (b instanceof LogoChar) {
			return value == ((LogoChar) b).value;
		} else if (b instanceof Integer64) {
			return value == ((Integer64) b).value;
		} else if (b instanceof Float64) {
			return value == ((Float64) b).value;
		} else {
			return value == ((LogoChar) b.cast(LogoChar.class)).value;
		}
	}

	@Override
	public boolean lessThan(Variable b) {
		if (b instanceof LogoChar) {
			return value < ((LogoChar) b).value;
		} else if (b instanceof Integer64) {
			return value < ((Integer64) b).value;
		} else if (b instanceof Float64) {
			return value < ((Float64) b).value;
		} else {
			return value < ((LogoChar) b.cast(LogoChar.class)).value;
		}
	}

	@Override
	public boolean lessEqual(Variable b) {
		if (b instanceof LogoChar) {
			return value <= ((LogoChar) b).value;
		} else if (b instanceof Integer64) {
			return value <= ((Integer64) b).value;
		} else if (b instanceof Float64) {
			return value <= ((Float64) b).value;
		} else {
			return value <= ((LogoChar) b.cast(LogoChar.class)).value;
		}
	}

	@Override
	public Variable getCopy(String newName) {
		LogoChar i = new LogoChar(newName);
		i.value = value;
		return i;
	}

	@Override
	public String toString() {
		return "LogoChar [value=" + value + ", name=" + name + "]";
	}

	@Override
	public String getStringValue() {
		return Character.toString(value);
	}

	@Override
	public double getDoubleValue() {
		return value;
	}

	@Override
	public long getLongValue() {
		return value;
	}

	@Override
	public void negativate() {
		value = (char) -value;
	}
}
