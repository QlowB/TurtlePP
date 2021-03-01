package turtlepp.datatypes;

import java.util.ArrayList;

import turtlepp.InterpreterException;

/**
 * Contains several mathematical constants like pi, e, the golden ratio etc.
 * 
 * @author Nicolas Winkler
 * 
 */
public class MathConstant extends Float64 {
	private static ArrayList<Variable> constants;

	static {
		constants = new ArrayList<Variable>();
		constants.add(new Pi());
		constants.add(new TwoPi());
		constants.add(new HalfPi());
		constants.add(new QuarterPi());
		constants.add(new E());
		constants.add(new Sqrt2());
		constants.add(new Sqrt3());
		constants.add(new GoldenRatio());
		constants.add(new True());
		constants.add(new False());
	}

	public static ArrayList<Variable> getConstants() {
		return constants;
	}

	public MathConstant(String name, double value) {
		super(name);
		this.value = value;
	}

	@Override
	public void set(Variable b) {
		throw new InterpreterException("cannot change a constants value. "
				+ "\n\n" + this.name + " cannot be changed to "
				+ b.getStringValue());
	}

	public void operate(Variable b) {
		if (this instanceof Pi)
			throw new InterpreterException(
					"I'm terribly sorry, but you can't change the value of pi. "
							+ "You should have paid attention in school.");
		else if (this instanceof E)
			throw new InterpreterException(
					"I'm terribly sorry, but you can't change the value of eulers number.");
		else
			throw new InterpreterException(
					"Sorry, but you can't change the value of " + this.name);
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

	public static class Pi extends MathConstant {
		public Pi() {
			super("PI", Math.PI);
		}
	}

	public static class TwoPi extends MathConstant {
		public TwoPi() {
			super("TWO_PI", Math.PI + Math.PI);
		}
	}

	public static class HalfPi extends MathConstant {
		public HalfPi() {
			super("HALF_PI", Math.PI * 0.5);
		}
	}

	public static class QuarterPi extends MathConstant {
		public QuarterPi() {
			super("QUARTER_PI", Math.PI * 0.25);
		}
	}

	public static class E extends MathConstant {
		public E() {
			super("E", Math.E);
		}
	}

	public static class Sqrt2 extends MathConstant {
		public Sqrt2() {
			super("SQRT2", Math.sqrt(2.0));
		}
	}

	public static class Sqrt3 extends MathConstant {
		public Sqrt3() {
			super("SQRT3", Math.sqrt(3.0));
		}
	}

	public static class GoldenRatio extends MathConstant {
		public GoldenRatio() {
			super("GoldenRatio", (1.0 + Math.sqrt(5)) * 0.5);
		}
	}

	public static class BoolConstant extends Bool {
		public BoolConstant(String name, boolean value) {
			super(name, value);
		}

		@Override
		public void set(Variable b) {
			throw new InterpreterException("cannot change a constants value. "
					+ "\n\n" + this.name + " cannot be changed to "
					+ b.getStringValue());
		}

		public void operate(Variable b) {
			throw new InterpreterException(
					"Sorry, but you can't change the value of " + this.name);
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
	}

	public static class True extends BoolConstant {
		public True() {
			super("true", true);
		}
	}

	public static class False extends BoolConstant {
		public False() {
			super("false", false);
		}
	}
}
