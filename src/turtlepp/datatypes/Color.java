package turtlepp.datatypes;

/**
 * not yet implemented
 * 
 * @author Nicolas Winkler
 * 
 */
public class Color extends Variable {
	public java.awt.Color value;

	public Color(String name) {
		super(name);
		this.value = java.awt.Color.black;
	}

	public Color(String name, java.awt.Color value) {
		super(name);
		this.value = value;
	}

	public void operate(Variable b) {
		throw new InvalidOperationException(
				"Can't use operators on variable of type " + getTypeName());
	}

	@Override
	public void add(Variable b) {
		operate(b);
	}

	@Override
	public void subtract(Variable b) {
		operate(b);
	}

	@Override
	public void multiply(Variable b) {
		operate(b);
	}

	@Override
	public void divide(Variable b) {
		operate(b);
	}

	@Override
	public void pow(Variable exponent) {
		operate(exponent);
	}

	@Override
	public void joinOr(Variable b) {
		operate(b);
	}

	@Override
	public void joinAnd(Variable b) {
		operate(b);
	}

	@Override
	public void joinXor(Variable b) {
		operate(b);
	}

	@Override
	public Variable cast(Class<? extends Variable> type) {
		if (type == Integer64.class)
			return new Integer64(null, value.getRGB());
		else
			try {
				throw new InvalidTypecastException(
						"Can't convert from color to "
								+ type.newInstance().getTypeName() + ".");
			} catch (IllegalAccessException e) {
				throw new InvalidTypecastException("Can't convert color.");
			} catch (InstantiationException e) {
				throw new InvalidTypecastException("Can't convert color.");
			}
	}

	@Override
	public String getTypeName() {
		return "color";
	}

	@Override
	public void set(Variable b) {
		if (b instanceof Color) {
			this.value = new java.awt.Color(((Color) b).value.getRGB());
		}
	}

	@Override
	public Variable getCopy(String newName) {
		return new Color(newName, new java.awt.Color(value.getRGB()));
	}

	@Override
	public String getStringValue() {
		return "RGB(" + value.getRed() + ", " + value.getGreen() + ", "
				+ value.getBlue() + ")";
	}

	@Override
	public boolean equalValue(Variable b) {
		if (b instanceof Color)
			return value.equals(((Color) b).value);

		throw new InvalidOperationException(
				"can't compare variables of type color with other types of variables");
	}

	@Override
	public boolean lessThan(Variable b) {
		throw new InvalidOperationException(
				"can't compare variables of type color");
	}

	@Override
	public boolean lessEqual(Variable b) {
		throw new InvalidOperationException(
				"can't compare variables of type color");
	}

	@Override
	public double getDoubleValue() {
		throw new InvalidTypecastException(
				"Can't convert color to numerical value.");
	}

	@Override
	public long getLongValue() {
		throw new InvalidTypecastException(
				"Can't convert color to numerical value.");
	}

	@Override
	public void negativate() {
		value = new java.awt.Color(255 - value.getRed(),
				255 - value.getGreen(), 255 - value.getBlue());
	}
}
