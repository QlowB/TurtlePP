package turtlepp.datatypes;

import turtlepp.InterpreterException;

/**
 * 64 bit floating point number
 * 
 * @author Nicolas Winkler
 */
public class Float64 extends Variable {

	/**
	 * the value of the variable
	 */
	public double value;

	/**
	 * initializes a new variable to zero
	 * 
	 * @param name
	 *            the name of the variable
	 */
	public Float64(String name) {
		super(name);
	}

	/**
	 * initializes a new variable to a specific value
	 * 
	 * @param name
	 *            the name of the variable
	 * @param value
	 *            the initial value
	 */
	public Float64(String name, double value) {
		super(name);
		this.value = value;
	}

	@Override
	public Variable cast(Class<? extends Variable> type) {
		if (type == Integer64.class) {
			Integer64 i = new Integer64(null);
			i.value = (long) value;
			return i;
		}
		if (type == LogoString.class) {
			LogoString s = new LogoString(null);
			s.value = "" + value;
			return s;
		}
		else if (type == LogoChar.class) {
			LogoChar s = new LogoChar(null);
			s.value = (char) value;
			return s;
		}
		if (type == Float64.class)
			return this;

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
			value = Math.pow(value, ((Integer64) exponent).value);
		} else if (exponent instanceof Float64) {
			value = Math.pow(value, ((Float64) exponent).value);
		} else {
			pow(exponent.cast(getClass()));
		}
	}

	@Override
	public void joinOr(Variable b) {
		throw new InterpreterException(
				"cannot use logical operator 'or' on type " + getTypeName());
	}

	@Override
	public void joinAnd(Variable b) {
		throw new InterpreterException(
				"cannot use logical operator 'and' on type " + getTypeName());
	}

	@Override
	public void joinXor(Variable b) {
		throw new InterpreterException(
				"cannot use logical operator 'xor' on type " + getTypeName());
	}

	@Override
	public String getTypeName() {
		return "float";
	}

	@Override
	public void set(Variable b) {
		if (b instanceof Float64) {
			this.value = ((Float64) b).value;
		} else {
			Float64 temp = (Float64) b.cast(Float64.class);
			this.value = temp.value;
		}
	}

	@Override
	public boolean equalValue(Variable b) {
		if (b instanceof Float64) {
			return value == ((Float64) b).value;
		} else {
			return value == ((Float64) b.cast(Float64.class)).value;
		}
	}

	@Override
	public boolean lessThan(Variable b) {
		if (b instanceof Float64) {
			return value < ((Float64) b).value;
		} else {
			return value < ((Float64) b.cast(Float64.class)).value;
		}
	}

	@Override
	public boolean lessEqual(Variable b) {
		if (b instanceof Float64) {
			return value <= ((Float64) b).value;
		} else {
			return value <= ((Float64) b.cast(Float64.class)).value;
		}
	}

	@Override
	public Variable getCopy(String newName) {
		Float64 f = new Float64(newName);
		f.value = value;
		return f;
	}

	@Override
	public String toString() {
		return "Float64 [value=" + value + ", name=" + name + "]";
	}

	@Override
	public String getStringValue() {
		return Double.toString(value);
	}

	@Override
	public double getDoubleValue() {
		return value;
	}

	@Override
	public long getLongValue() {
		return (long) value;
	}

	@Override
	public void negativate() {
		value = -value;
	}
}
