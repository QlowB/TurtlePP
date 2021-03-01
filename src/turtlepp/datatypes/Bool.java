package turtlepp.datatypes;

/**
 * A simple boolean value
 * 
 * @author Nicolas Winkler
 * 
 */
public class Bool extends Variable {
	/**
	 * the value represented by this object
	 */
	public boolean value;

	public Bool(String name) {
		super(name);
	}

	public Bool(String name, boolean value) {
		super(name);
		this.value = value;
	}

	@Override
	public Variable cast(Class<? extends Variable> type) {
		if (type == LogoString.class) {
			LogoString s = new LogoString(null);
			s.value = getStringValue();
			return s;
		}
		if (type == getClass())
			return this;

		throw new InvalidTypecastException("cannot convert from "
				+ getTypeName() + " to " + Variable.getTypeName(type));
	}

	@Override
	public String getTypeName() {
		return "boolean";
	}

	@Override
	public void set(Variable b) {
		if (b instanceof Bool) {
			this.value = ((Bool) b).value;
		} else {
			Bool temp = (Bool) b.cast(getClass());
			this.value = temp.value;
		}
	}

	@Override
	public Variable getCopy(String newName) {
		Bool b = new Bool(newName);
		b.value = value;
		return b;
	}

	@Override
	public boolean equalValue(Variable b) {
		if (b instanceof Bool) {
			return value == ((Bool) b).value;
		} else {
			return value == ((Bool) b.cast(Bool.class)).value;
		}
	}

	@Override
	public boolean lessThan(Variable b) {
		throw new InvalidOperationException("can't compare variables of type "
				+ getTypeName());
	}

	@Override
	public boolean lessEqual(Variable b) {
		throw new InvalidOperationException("can't compare variables of type "
				+ getTypeName());
	}

	@Override
	public void add(Variable summand) {
		throw new InvalidOperationException("can't add variables of type "
				+ getTypeName());
	}

	@Override
	public void subtract(Variable subtrahend) {
		throw new InvalidOperationException("can't subtract variables of type "
				+ getTypeName());
	}

	@Override
	public void multiply(Variable factor) {
		throw new InvalidOperationException("can't multiply variables of type "
				+ getTypeName());
	}
	
	@Override
	public void pow(Variable exponent) {
		throw new InvalidOperationException("can't exponentiate variables of type "
				+ getTypeName());
	}

	@Override
	public void divide(Variable dividend) {
		throw new InvalidOperationException("can't divide variables of type "
				+ getTypeName());
	}

	@Override
	public void joinOr(Variable b) {
		if (b instanceof Bool) {
			value = value || ((Bool) b).value;
		} else if (b instanceof Integer64) {
			value = value || ((Integer64) b).value != 0;
		} else {
			value = value || ((Bool) b.cast(getClass())).value;
		}
	}

	@Override
	public void joinAnd(Variable b) {
		if (b instanceof Bool) {
			value = value && ((Bool) b).value;
		} else if (b instanceof Integer64) {
			value = value && ((Integer64) b).value != 0;
		} else {
			value = value && ((Bool) b.cast(getClass())).value;
		}
	}

	@Override
	public void joinXor(Variable b) {
		if (b instanceof Bool) {
			value = value ^ ((Bool) b).value;
		} else if (b instanceof Integer64) {
			value = value ^ ((Integer64) b).value != 0;
		} else {
			value = value ^ ((Bool) b.cast(getClass())).value;
		}
	}

	@Override
	public void negativate() {
		value = !value;
	}

	@Override
	public String getStringValue() {
		return value ? "true" : "false";
	}

	@Override
	public double getDoubleValue() {
		return value ? 1.0 : 0.0;
	}

	@Override
	public long getLongValue() {
		return value ? 1 : 0;
	}

	@Override
	public String toString() {
		return "Bool [value=" + value + "]";
	}
}
