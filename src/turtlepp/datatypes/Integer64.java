package turtlepp.datatypes;

/**
 * A variable type representing a 64 bit long integer
 * 
 * @author Nicolas Winkler
 */
public class Integer64 extends Variable {
	/**
	 * actual value
	 */
	public long value;

	/**
	 * Creates a new variable. It is set to zero.
	 * 
	 * @param name
	 *            the name of the new variable
	 */
	public Integer64(String name) {
		super(name);
	}

	/**
	 * 
	 * @param name
	 * @param value
	 */
	public Integer64(String name, long value) {
		this(name);
		this.value = value;
	}

	@Override
	public Variable cast(Class<? extends Variable> type) {
		if (type == getClass())
			return this;
		else if (type == Float64.class) {
			Float64 f = new Float64(null);
			f.value = (double) value;
			return f;
		} else if (type == LogoChar.class) {
			LogoChar s = new LogoChar(null);
			s.value = (char) value;
			return s;
		} else if (type == Bool.class) {
			Bool s = new Bool(null);
			s.value = value != 0;
			return s;
		} else if (type == LogoString.class) {
			LogoString s = new LogoString(null);
			s.value = "" + value;
			return s;
		}
		throw new InvalidTypecastException("cannot convert from "
				+ getTypeName() + " to " + Variable.getTypeName(type));
	}

	@Override
	public void add(Variable b) {
		if (b instanceof Integer64) {
			value += ((Integer64) b).value;
		} else if (b instanceof Float64) {
			value += ((Float64) b).value;
		} else if (b instanceof LogoChar) {
			value += ((LogoChar) b).value;
		} else if (b instanceof Reference) {
			add(((Reference) b).wrapped);
		} else {
			add(b.cast(getClass()));
		}
	}

	@Override
	public void subtract(Variable b) {
		if (b instanceof Integer64) {
			value -= ((Integer64) b).value;
		} else if (b instanceof Float64) {
			value -= ((Float64) b).value;
		} else if (b instanceof LogoChar) {
			value -= ((LogoChar) b).value;
		} else if (b instanceof Reference) {
			subtract(((Reference) b).wrapped);
		} else {
			subtract(b.cast(getClass()));
		}
	}

	@Override
	public void multiply(Variable b) {
		if (b instanceof Integer64) {
			value *= ((Integer64) b).value;
		} else if (b instanceof Float64) {
			value *= ((Float64) b).value;
		} else if (b instanceof LogoChar) {
			value *= ((LogoChar) b).value;
		} else if (b instanceof Reference) {
			multiply(((Reference) b).wrapped);
		} else {
			multiply(b.cast(getClass()));
		}
	}

	@Override
	public void divide(Variable b) {
		if (b instanceof Integer64) {
			value /= ((Integer64) b).value;
		} else if (b instanceof Float64) {
			value /= ((Float64) b).value;
		} else if (b instanceof LogoChar) {
			value /= ((LogoChar) b).value;
		} else if (b instanceof Reference) {
			divide(((Reference) b).wrapped);
		} else {
			divide(b.cast(getClass()));
		}
	}
	
	@Override
	public void pow(Variable exponent) {
		if (exponent instanceof Integer64) {
			value = (long) Math.pow(value, ((Integer64) exponent).value);
		} else if (exponent instanceof Float64) {
			value = (long) Math.pow(value, ((Float64) exponent).value);
		} else {
			pow(exponent.cast(getClass()));
		}
	}

	@Override
	public void joinOr(Variable b) {
		if (b instanceof Integer64) {
			value |= ((Integer64) b).value;
		} else if (b instanceof Float64) {
			value |= (long) ((Float64) b).value;
		} else if (b instanceof LogoChar) {
			value |= ((LogoChar) b).value;
		} else if (b instanceof Reference) {
			joinOr(((Reference) b).wrapped);
		} else {
			joinOr(b.cast(getClass()));
		}
	}

	@Override
	public void joinAnd(Variable b) {
		if (b instanceof Integer64) {
			value &= ((Integer64) b).value;
		} else if (b instanceof Float64) {
			value &= (long) ((Float64) b).value;
		} else if (b instanceof LogoChar) {
			value &= ((LogoChar) b).value;
		} else if (b instanceof Reference) {
			joinAnd(((Reference) b).wrapped);
		} else {
			joinAnd(b.cast(getClass()));
		}
	}

	@Override
	public void joinXor(Variable b) {
		if (b instanceof Integer64) {
			value ^= ((Integer64) b).value;
		} else if (b instanceof Float64) {
			value ^= (long) ((Float64) b).value;
		} else if (b instanceof LogoChar) {
			value ^= ((LogoChar) b).value;
		} else if (b instanceof Reference) {
			joinXor(((Reference) b).wrapped);
		} else {
			joinXor(b.cast(getClass()));
		}
	}

	@Override
	public String getTypeName() {
		return "int";
	}

	@Override
	public void set(Variable b) {
		if (b instanceof Integer64) {
			this.value = ((Integer64) b).value;
		} else {
			Integer64 temp = (Integer64) b.cast(Integer64.class);
			this.value = temp.value;
		}
	}

	@Override
	public boolean equalValue(Variable b) {
		if (b instanceof Integer64) {
			return value == ((Integer64) b).value;
		} else if (b instanceof Float64) {
			return value == ((Float64) b).value;
		} else {
			return value == ((Integer64) b.cast(Integer64.class)).value;
		}
	}

	@Override
	public boolean lessThan(Variable b) {
		if (b instanceof Integer64) {
			return value < ((Integer64) b).value;
		} else if (b instanceof Float64) {
			return value < ((Float64) b).value;
		} else {
			return value < ((Integer64) b.cast(Integer64.class)).value;
		}
	}

	@Override
	public boolean lessEqual(Variable b) {
		if (b instanceof Integer64) {
			return value <= ((Integer64) b).value;
		} else if (b instanceof Float64) {
			return value <= ((Float64) b).value;
		} else {
			return value <= ((Integer64) b.cast(Integer64.class)).value;
		}
	}

	@Override
	public Variable getCopy(String newName) {
		Integer64 i = new Integer64(newName);
		i.value = value;
		return i;
	}

	@Override
	public String toString() {
		return "Integer64 [value=" + value + ", name=" + name + "]";
	}

	@Override
	public String getStringValue() {
		return Long.toString(value);
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
		value = -value;
	}
}
