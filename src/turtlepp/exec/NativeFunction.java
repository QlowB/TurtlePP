package turtlepp.exec;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import turtlepp.Language;
import turtlepp.LogoRenderer2D;
import turtlepp.datatypes.Array;
import turtlepp.datatypes.Bool;
import turtlepp.datatypes.Float64;
import turtlepp.datatypes.Integer64;
import turtlepp.datatypes.LogoChar;
import turtlepp.datatypes.LogoString;
import turtlepp.datatypes.Reference;
import turtlepp.datatypes.Value;
import turtlepp.datatypes.Variable;

/**
 * represents a function with one argument (real-number) that returns another
 * real value
 * 
 * @author Nicolas Winkler
 * 
 */
public abstract class NativeFunction extends Function {

	private static ArrayList<Function> nativeFunctions;

	static {
		nativeFunctions = new ArrayList<Function>();
		nativeFunctions.add(new Sqrt());
		nativeFunctions.add(new Sin());
		nativeFunctions.add(new Cos());
		nativeFunctions.add(new Tan());
		nativeFunctions.add(new Cot());
		nativeFunctions.add(new Exp());
		nativeFunctions.add(new ASin());
		nativeFunctions.add(new ACos());
		nativeFunctions.add(new ATan());
		nativeFunctions.add(new ATan2());
		nativeFunctions.add(new Ln());
		nativeFunctions.add(new Lg());
		nativeFunctions.add(new Log());
		nativeFunctions.add(new Sinh());
		nativeFunctions.add(new Cosh());
		nativeFunctions.add(new Tanh());
		nativeFunctions.add(new Max());
		nativeFunctions.add(new Min());
		nativeFunctions.add(new Round());
		nativeFunctions.add(new Int());
		nativeFunctions.add(new Rand());
		nativeFunctions.add(new RandWithMax());
		nativeFunctions.add(new RandWithBounds());
		nativeFunctions.add(new Nanos());
		nativeFunctions.add(new Year());
		nativeFunctions.add(new Month());
		nativeFunctions.add(new Day());
		nativeFunctions.add(new Hour());
		nativeFunctions.add(new Minute());
		nativeFunctions.add(new Second());
		nativeFunctions.add(new Millisecond());
		nativeFunctions.add(new Typename());
		nativeFunctions.add(new Input());
		nativeFunctions.add(new IsNumeric());
		nativeFunctions.add(new ToCharArray());
		nativeFunctions.add(new Length());
		nativeFunctions.add(new Width());
		nativeFunctions.add(new Height());
	}

	public NativeFunction(String name) {
		super(name);
	}

	public NativeFunction() {
		super();
	}

	public static ArrayList<Function> getNativeFunctions() {
		return nativeFunctions;
	}

	@Override
	public void optimize(SubroutineOptimizer so) {
		return; // do nothing
	}

	public static abstract class MathFunction extends NativeFunction {

		protected MathFunction(String name) {
			this.name = name;
			this.arguments = new String[] { null };
		}

		@Override
		public void invoke(LogoRenderer2D renderer, OrderInterpreter oi,
				Value[] args, CodeLocationInfo cli) {

			double arg = args[0].getVarValue(renderer, oi, cli)
					.getDoubleValue();
			temp = new Float64(null, val(arg));
		}

		protected abstract double val(double arg);
	}

	public static abstract class NoArgumentFunction extends NativeFunction {
		public NoArgumentFunction(String name) {
			this.name = name;
			this.arguments = new String[0];
		}

		@Override
		public void invoke(LogoRenderer2D renderer, OrderInterpreter oi,
				Value[] args, CodeLocationInfo cli) {
			temp = val();
		}

		protected abstract Variable val();
	}

	public static class Sqrt extends MathFunction {
		public Sqrt() {
			super("sqrt");
		}

		@Override
		public double val(double arg) {
			return Math.sqrt(arg);
		}
	}

	public static class Sin extends MathFunction {
		public Sin() {
			super("sin");
		}

		@Override
		public double val(double arg) {
			return Math.sin(arg);
		}
	}

	public static class Cos extends MathFunction {
		public Cos() {
			super("cos");
		}

		@Override
		public double val(double arg) {
			return Math.cos(arg);
		}
	}

	public static class Tan extends MathFunction {
		public Tan() {
			super("tan");
		}

		@Override
		public double val(double arg) {
			return Math.tan(arg);
		}
	}

	public static class Cot extends MathFunction {
		public Cot() {
			super("cot");
		}

		@Override
		public double val(double arg) {
			return 1.0 / Math.tan(arg);
		}
	}

	public static class Exp extends MathFunction {
		public Exp() {
			super("exp");
		}

		@Override
		public double val(double arg) {
			return Math.exp(arg);
		}
	}

	public static class ASin extends MathFunction {
		public ASin() {
			super("asin");
		}

		@Override
		public double val(double arg) {
			return Math.asin(arg);
		}
	}

	public static class ACos extends MathFunction {
		public ACos() {
			super("acos");
		}

		@Override
		public double val(double arg) {
			return Math.acos(arg);
		}
	}

	public static class ATan extends MathFunction {
		public ATan() {
			super("atan");
		}

		@Override
		public double val(double arg) {
			return Math.atan(arg);
		}
	}

	public static class ATan2 extends MathFunction {
		public ATan2() {
			super("atan2");
			arguments = new String[] { null, null };
		}

		@Override
		public void invoke(LogoRenderer2D renderer, OrderInterpreter oi,
				Value[] args, CodeLocationInfo cli) {

			double arg1 = args[0].getVarValue(renderer, oi, cli)
					.getDoubleValue();
			double arg2 = args[1].getVarValue(renderer, oi, cli)
					.getDoubleValue();
			temp = new Float64(null, Math.atan2(arg1, arg2));
		}

		@Override
		protected double val(double arg) {
			return 0;
		}
	}

	/**
	 * the logarithmus naturalis
	 */
	public static class Ln extends MathFunction {
		public Ln() {
			super("ln");
		}

		@Override
		public double val(double arg) {
			return Math.log(arg);
		}
	}

	/**
	 * the base 10 logarithm
	 */
	public static class Lg extends MathFunction {
		public Lg() {
			super("lg");
		}

		@Override
		public double val(double arg) {
			return Math.log10(arg);
		}
	}

	public static class Log extends MathFunction {
		public Log() {
			super("log");
			arguments = new String[] { null, null };
		}

		@Override
		public void invoke(LogoRenderer2D renderer, OrderInterpreter oi,
				Value[] args, CodeLocationInfo cli) {

			double arg1 = args[0].getVarValue(renderer, oi, cli)
					.getDoubleValue();
			double arg2 = args[1].getVarValue(renderer, oi, cli)
					.getDoubleValue();
			temp = new Float64(null, Math.log(arg2) / Math.log(arg1));
		}

		@Override
		protected double val(double arg) {
			return 0;
		}
	}

	public static class Sinh extends MathFunction {
		public Sinh() {
			super("sinh");
		}

		@Override
		public double val(double arg) {
			return Math.sinh(arg);
		}
	}

	public static class Cosh extends MathFunction {
		public Cosh() {
			super("cosh");
		}

		@Override
		public double val(double arg) {
			return Math.cosh(arg);
		}
	}

	public static class Tanh extends MathFunction {
		public Tanh() {
			super("tanh");
		}

		@Override
		public double val(double arg) {
			return Math.tanh(arg);
		}
	}
	
	public static class Max extends MathFunction {
		public Max() {
			super("max");
			arguments = new String[] { null, null };
		}

		@Override
		public void invoke(LogoRenderer2D renderer, OrderInterpreter oi,
				Value[] args, CodeLocationInfo cli) {

			Variable arg1 = args[0].getVarValue(renderer, oi, cli);
			Variable arg2 = args[1].getVarValue(renderer, oi, cli);
			if (arg1.lessThan(arg2))
				temp = arg2;
			else
				temp = arg1;
		}

		@Override
		protected double val(double arg) {
			return 0;
		}
	}
	
	public static class Min extends MathFunction {
		public Min() {
			super("min");
			arguments = new String[] { null, null };
		}

		@Override
		public void invoke(LogoRenderer2D renderer, OrderInterpreter oi,
				Value[] args, CodeLocationInfo cli) {

			Variable arg1 = args[0].getVarValue(renderer, oi, cli);
			Variable arg2 = args[1].getVarValue(renderer, oi, cli);
			if (arg1.lessThan(arg2))
				temp = arg1;
			else
				temp = arg2;
		}

		@Override
		protected double val(double arg) {
			return 0;
		}
	}
	
	public static class Round extends MathFunction {
		public Round() {
			super("round");
		}
		
		@Override
		public void invoke(LogoRenderer2D renderer, OrderInterpreter oi,
				Value[] args, CodeLocationInfo cli) {
			Variable arg1 = args[0].getVarValue(renderer, oi, cli);
			
			if (arg1 instanceof Integer64)
				temp = arg1;
			else
				temp = new Integer64(null, Math.round(arg1.getDoubleValue()));
		}

		@Override
		protected double val(double arg) {
			return 0;
		}
	}
	
	public static class Int extends MathFunction {
		public Int() {
			super("int");
		}
		
		@Override
		public void invoke(LogoRenderer2D renderer, OrderInterpreter oi,
				Value[] args, CodeLocationInfo cli) {
			Variable arg1 = args[0].getVarValue(renderer, oi, cli);
			
			if (arg1 instanceof Integer64)
				temp = arg1;
			else
				temp = new Integer64(null, arg1.getLongValue());
		}

		@Override
		protected double val(double arg) {
			return 0;
		}
	}

	public static class Rand extends NoArgumentFunction {
		public Rand() {
			super("rand");
		}

		@Override
		protected Variable val() {
			return new Float64(null, Math.random());
		}
	}

	public static class RandWithMax extends MathFunction {
		public RandWithMax() {
			super("rand");
		}

		@Override
		protected double val(double arg) {
			return Math.random() * arg;
		}
	}

	public static class RandWithBounds extends MathFunction {
		public RandWithBounds() {
			super("rand");
			arguments = new String[] { null, null };
		}

		@Override
		public void invoke(LogoRenderer2D renderer, OrderInterpreter oi,
				Value[] args, CodeLocationInfo cli) {

			double arg1 = args[0].getVarValue(renderer, oi, cli)
					.getDoubleValue();
			double arg2 = args[1].getVarValue(renderer, oi, cli)
					.getDoubleValue();
			temp = new Float64(null, Math.random() * (arg2 - arg1) + arg1);
		}

		/**
		 * never invoked
		 */
		@Override
		protected double val(double arg) {
			return 0;
		}
	}

	public static class Nanos extends NoArgumentFunction {
		public Nanos() {
			super("nanos");
		}

		@Override
		protected Variable val() {
			return new Integer64(null, System.nanoTime());
		}
	}

	public static class Year extends NoArgumentFunction {
		public Year() {
			super("year");
		}

		@Override
		protected synchronized Variable val() {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			int year = cal.get(Calendar.YEAR);
			return new Integer64(null, year);
		}
	}

	public static class Month extends NoArgumentFunction {
		public Month() {
			super("month");
		}

		@Override
		protected synchronized Variable val() {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			int month = cal.get(Calendar.MONTH) + 1;
			return new Integer64(null, month);
		}
	}

	public static class Day extends NoArgumentFunction {
		public Day() {
			super("day");
		}

		@Override
		protected synchronized Variable val() {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			int day = cal.get(Calendar.DAY_OF_MONTH);
			return new Integer64(null, day);
		}
	}

	public static class Hour extends NoArgumentFunction {
		public Hour() {
			super("hour");
		}

		@Override
		protected synchronized Variable val() {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			return new Integer64(null, hour);
		}
	}

	public static class Minute extends NoArgumentFunction {
		public Minute() {
			super("minute");
		}

		@Override
		protected synchronized Variable val() {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			int minute = cal.get(Calendar.MINUTE);
			return new Integer64(null, minute);
		}
	}

	public static class Second extends NoArgumentFunction {
		public Second() {
			super("second");
		}

		@Override
		protected synchronized Variable val() {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			int second = cal.get(Calendar.SECOND);
			return new Integer64(null, second);
		}
	}

	public static class Millisecond extends NoArgumentFunction {
		public Millisecond() {
			super("millisecond");
		}

		@Override
		protected synchronized Variable val() {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			int millis = cal.get(Calendar.MILLISECOND);
			return new Integer64(null, millis);
		}
	}

	public static class Typename extends NativeFunction {
		public Typename() {
			super("typename");
			arguments = new String[] { null };
		}

		@Override
		public void invoke(LogoRenderer2D renderer, OrderInterpreter oi,
				Value[] args, CodeLocationInfo cli) {

			Variable arg = args[0].getVarValue(renderer, oi, cli);
			temp = new LogoString(null, arg.getTypeName());
		}
	}

	public static class Input extends NativeFunction {
		public Input() {
			super("input");
			arguments = new String[] {};
		}

		@Override
		public void invoke(LogoRenderer2D renderer, OrderInterpreter oi,
				Value[] args, CodeLocationInfo cli) {
			temp = new LogoString(null, renderer.popInputString());
		}
	}

	public static class IsNumeric extends NativeFunction {
		public IsNumeric() {
			super("isNumeric");
			arguments = new String[] { null };
		}

		@Override
		public void invoke(LogoRenderer2D renderer, OrderInterpreter oi,
				Value[] args, CodeLocationInfo cli) {
			boolean numeric = false;
			Variable v = args[0].getVarValue(renderer, oi, cli);

			while (v instanceof Reference) {
				v = ((Reference) v).wrapped;
			}

			if (v instanceof Integer64 || v instanceof Float64)
				numeric = true;
			else if (v instanceof LogoString) {
				String val = ((LogoString) v).value;
				numeric = Language.isNumeric(val);
			}
			temp = new Bool(null, numeric);
		}
	}

	public static class ToCharArray extends NativeFunction {
		public ToCharArray() {
			super("toCharArray");
			arguments = new String[] { null };
		}

		@Override
		public void invoke(LogoRenderer2D renderer, OrderInterpreter oi,
				Value[] args, CodeLocationInfo cli) {
			Variable v = args[0].getVarValue(renderer, oi, cli).cast(
					LogoString.class);
			while (v instanceof Reference) {
				v = ((Reference) v).wrapped;
			}
			if (v instanceof LogoString) {
				String s = v.getStringValue();
				LogoChar[] arr = new LogoChar[s.length()];
				for (int i = 0; i < arr.length; i++) {
					arr[i] = new LogoChar(null, s.charAt(i));
				}
				temp = new Array(null, arr);
			}
		}
	}

	public static class Length extends NativeFunction {
		public Length() {
			super("length");
			arguments = new String[] { null };
		}

		@Override
		public void invoke(LogoRenderer2D renderer, OrderInterpreter oi,
				Value[] args, CodeLocationInfo cli) {
			Variable v = args[0].getVarValue(renderer, oi, cli);
			while (v instanceof Reference) {
				v = ((Reference) v).wrapped;
			}
			if (v instanceof Array) {
				temp = new Integer64(null, ((Array) v).array.length);
			} else if (v instanceof LogoString) {
				temp = new Integer64(null, ((LogoString) v).value.length());
			} else {
				temp = new Integer64(null,
						((LogoString) v.cast(LogoString.class)).value.length());
			}
		}
	}

	public static class Width extends NativeFunction {
		public Width() {
			super("width");
			arguments = new String[] {};
		}

		@Override
		public void invoke(LogoRenderer2D renderer, OrderInterpreter oi,
				Value[] args, CodeLocationInfo cli) {
			temp = new Integer64(null, renderer.getImage().getWidth());
		}
	}

	public static class Height extends NativeFunction {
		public Height() {
			super("height");
			arguments = new String[] {};
		}

		@Override
		public void invoke(LogoRenderer2D renderer, OrderInterpreter oi,
				Value[] args, CodeLocationInfo cli) {
			temp = new Integer64(null, renderer.getImage().getHeight());
		}
	}
}
