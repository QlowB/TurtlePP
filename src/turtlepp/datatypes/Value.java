package turtlepp.datatypes;

import java.util.Arrays;

import turtlepp.InterpreterException;
import turtlepp.LogoRenderer2D;
import turtlepp.exec.CodeLocationInfo;
import turtlepp.exec.Function;
import turtlepp.exec.Invokeable;
import turtlepp.exec.OrderInterpreter;
import turtlepp.exec.OrderOptimizer;
import turtlepp.exec.OrderOptimizer.StackType;
import turtlepp.exec.Tokenizer.ArrayAccessToken;
import turtlepp.exec.Tokenizer.TermToken;
import turtlepp.exec.Tokenizer.TextToken;
import turtlepp.exec.Tokenizer.Token;

/**
 * Represents a value which can either be calculated or is already constant.
 * Values are used to pass arguments to functions. There are three general types
 * of what values can represent:
 * <ul>
 * <li>constant values (e.g. 5.0)</li>
 * <li>references to variables</li>
 * <li>references to functions (these have to be invoked first to evaluate the
 * return value)</li>
 * </ul>
 * 
 * @author Nicolas Winkler
 * 
 */
public abstract class Value {

	/**
	 * evaluates the value and creates a variable
	 * 
	 * @param renderer
	 *            the renderer to draw on (e.g. if a function to be evaluated
	 *            draws something)
	 * @param oi
	 *            the variables, stacks, everything
	 * @param cli
	 *            information about the code in case of exceptions
	 * @return the {@link Variable} representing this value
	 * 
	 * @throws InterpreterException
	 *             can throw exceptions if there is for example a variable
	 *             reference to an inexistent variable
	 */
	public abstract Variable getVarValue(LogoRenderer2D renderer,
			OrderInterpreter oi, CodeLocationInfo cli)
			throws InterpreterException;

	public Value getOptimized(OrderOptimizer oo) {
		return this;
	}

	/**
	 * A single integer value
	 * 
	 * @author Nicolas Winkler
	 * 
	 */
	public static class IntegerValue extends Value {
		/**
		 * the absolute value
		 */
		long value;

		public IntegerValue(long val) {
			value = val;
		}

		@Override
		public Variable getVarValue(LogoRenderer2D renderer,
				OrderInterpreter oi, CodeLocationInfo cli) {
			return new Integer64(null, value);
		}
	}

	/**
	 * A reference to a variable in the stack
	 * 
	 * @author Nicolas Winkler
	 * 
	 */
	public static class StackReferenceValue extends Value {
		/**
		 * the index from the top of the stack
		 */
		protected int fromTop;

		public StackReferenceValue(int fromTop) {
			this.fromTop = fromTop;
		}

		@Override
		public Variable getVarValue(LogoRenderer2D renderer,
				OrderInterpreter oi, CodeLocationInfo cli) {
			return oi.getVariableFromCallStack(fromTop);
		}
	}

	/**
	 * A reference to a variable in the global variable stack
	 * 
	 * @author Nicolas Winkler
	 * 
	 */
	public static class GlobalStackReferenceValue extends StackReferenceValue {
		public GlobalStackReferenceValue(int fromTop) {
			super(fromTop);
		}

		@Override
		public Variable getVarValue(LogoRenderer2D renderer,
				OrderInterpreter oi, CodeLocationInfo cli) {
			return oi.getVariableFromGlobalStack(fromTop);
		}
	}

	public static abstract class TermValue extends Value {
		protected Value[] operands;

		public Value getOperand(int i) {
			return operands[i];
		}

		abstract public Value simplify();

		/**
		 * determines if the value does not need to be wrapped in a TermValue
		 * but is a normal value.
		 */
		public abstract boolean isNotAnOperation();

		@Override
		public Value getOptimized(OrderOptimizer oo) {
			for (int i = 0; i < operands.length; i++) {
				operands[i] = operands[i].getOptimized(oo);
			}
			return this;
		}
	}

	public static class NegativeVarValue extends Value {
		Value val;

		public NegativeVarValue(String name) {
			val = new VariableValue(name);
		}

		public NegativeVarValue(Value val) {
			this.val = val;
		}

		@Override
		public Variable getVarValue(LogoRenderer2D renderer,
				OrderInterpreter oi, CodeLocationInfo cli) {
			Variable v = val.getVarValue(renderer, oi, cli).getCopy(null);
			v.negativate();
			return v;
		}

		@Override
		public Value getOptimized(OrderOptimizer oo) {
			val = val.getOptimized(oo);
			return this;
		}
	}

	/**
	 * An array of summands with positive or negative signs
	 * 
	 * @author Nicolas Winkler
	 * 
	 */
	public static class BoolTermValue extends TermValue {
		/**
		 * the combination of operands; true is or, false is and
		 */
		byte[] signs;

		private static final byte or = 1;
		private static final byte and = 2;
		private static final byte xor = 3;

		/**
		 * creates a new sum from the given tokens
		 * 
		 * @param token
		 *            the {@link TermToken} to parse
		 */
		public BoolTermValue(TermToken token) {
			TermToken[] toks = token.getNormalized().split("and|or|xor");
			operands = new Value[toks.length];
			signs = new byte[toks.length];

			for (int i = 0; i < toks.length; i++) {
				if (toks[i].getNTokens() == 1
						|| toks[i].getToken(0).matches("or"))
					signs[i] = or;
				else if (toks[i].getToken(0).matches("and"))
					signs[i] = and;
				else if (toks[i].getToken(0).matches("xor"))
					signs[i] = xor;
				if (toks[i] instanceof TermToken
						&& ((TermToken) toks[i]).getNTokens() > 1) {
					if (ComparisonValue.isComparison(toks[i]))
						operands[i] = new ComparisonValue(toks[i]);
					else
						operands[i] = new AddSubValue(toks[i]).simplify();
				} else
					operands[i] = toks[i].getToken(toks[i].getNTokens() - 1)
							.createValue();
			}
		}

		@Override
		public Variable getVarValue(LogoRenderer2D renderer,
				OrderInterpreter oi, CodeLocationInfo cli) {
			Variable source = operands[0].getVarValue(renderer, oi, cli);
			
			Variable var = source.getCopy(null);

			boolean integer = var instanceof Integer64;
			for (int i = 1; i < operands.length; i++) {
				Variable v = operands[i].getVarValue(renderer, oi, cli);

				if (integer && v instanceof Float64) {
					var = new Float64(null, var.getDoubleValue());
				}

				if (signs[i] == or)
					var.joinOr(v);
				else if (signs[i] == and)
					var.joinAnd(v);
				else if (signs[i] == xor)
					var.joinXor(v);
			}
			return var;
		}

		@Override
		public String toString() {
			return "BoolTermValue [signs=" + Arrays.toString(signs)
					+ ", operands=" + Arrays.toString(operands) + "]";
		}

		public boolean isNotAnOperation() {
			return signs.length == 1;
		}

		@Override
		public Value simplify() {
			if (isNotAnOperation()) {
				return operands[0];
			} else {
				return this;
			}
		}
	}

	/**
	 * An array of summands with positive or negative signs
	 * 
	 * @author Nicolas Winkler
	 * 
	 */
	public static class AddSubValue extends TermValue {
		/**
		 * the signs of the operands; true is +, false -
		 */
		boolean[] signs;

		/**
		 * creates a new sum from the given tokens
		 * 
		 * @param token
		 *            the {@link TermToken} to parse
		 */
		public AddSubValue(TermToken token) {
			TermToken[] toks = token.getNormalized().split("[\\+\\-]");
			operands = new Value[toks.length];
			signs = new boolean[toks.length];

			for (int i = 0; i < toks.length; i++) {
				if (toks[i].getNTokens() == 1
						|| toks[i].getToken(0).matches("\\+"))
					signs[i] = true;
				if (toks[i] instanceof TermToken
						&& ((TermToken) toks[i]).getNTokens() > 1) {
					operands[i] = new MultDivValue(toks[i]).simplify();
					// System.out.println(operands[i]);
				} else
					operands[i] = toks[i].getToken(toks[i].getNTokens() - 1)
							.createValue();
			}
		}

		@Override
		public Value simplify() {
			if (isNotAnOperation()) {
				return operands[0];
			} else {
				return this;
			}
		}

		@Override
		public Variable getVarValue(LogoRenderer2D renderer,
				OrderInterpreter oi, CodeLocationInfo cli) {
			Variable source = operands[0].getVarValue(renderer, oi, cli);
			Variable var = source.getCopy(null);

			try {

				boolean integer = var instanceof Integer64;
				for (int i = 1; i < operands.length; i++) {
					Variable v = operands[i].getVarValue(renderer, oi, cli);

					if (integer && v instanceof Float64) {
						var = new Float64(null, var.getDoubleValue());
						integer = false;
					}

					if (signs[i])
						var.add(v);
					else
						var.subtract(v);
				}
				return var;
			} catch (InterpreterException ie) {
				ie.setCodeLocationInfo(cli);
				throw ie;
			}
		}

		@Override
		public String toString() {
			return "AddSubValue [signs=" + Arrays.toString(signs)
					+ ", operands=" + Arrays.toString(operands) + "]";
		}

		public boolean isNotAnOperation() {
			return signs.length == 1 && signs[0] == true;
		}
	}

	/**
	 * An array of summands with positive or negative signs
	 * 
	 * @author Nicolas Winkler
	 * 
	 */
	public static class ComparisonValue extends Value {
		Comparison comparison;

		/**
		 * creates a new comparison from the given tokens
		 * 
		 * @param token
		 *            the {@link TermToken} to parse
		 */
		public ComparisonValue(TermToken token) {
			TermToken[] toks = token.getNormalized().split("[<>=]|==|<=|>=|!=");
			if (toks.length != 2 || toks[1].getNTokens() <= 1)
				throw new InterpreterException("Invalid comparison");
			Value a = toks[0].createValue();
			Value b = toks[1].getToken(1).createValue();
			Token comp = toks[1].getToken(0);
			if (comp.isTextCommand("<")) {
				comparison = new LessThan(a, b);
			} else if (comp.isTextCommand("<=")) {
				comparison = new LessEqual(a, b);
			} else if (comp.isTextCommand("==")) {
				comparison = new Equal(a, b);
			} else if (comp.isTextCommand(">")) {
				comparison = new LessThan(b, a);
			} else if (comp.isTextCommand(">=")) {
				comparison = new LessEqual(b, a);
			} else if (comp.isTextCommand("!=")) {
				comparison = new NotEqual(b, a);
			} else {
				throw new InterpreterException("Unknown comparative operand");
			}
		}

		@Override
		public Variable getVarValue(LogoRenderer2D renderer,
				OrderInterpreter oi, CodeLocationInfo cli) {
			return new Bool(null, comparison.compare(renderer, oi, cli));
		}

		public boolean getBooleanValue(LogoRenderer2D renderer,
				OrderInterpreter oi, CodeLocationInfo cli) {
			return comparison.compare(renderer, oi, cli);
		}

		public Comparison getComparison() {
			return comparison;
		}

		public static boolean isComparison(TermToken token) {
			TermToken[] toks = token.getNormalized().split("[<>=]|==|<=|>=|!=");
			if (toks.length != 2 || toks[1].getNTokens() <= 1)
				return false;
			return true;
		}
	}

	/**
	 * An array of factors
	 * 
	 * @author Nicolas Winkler
	 * 
	 */
	public static class MultDivValue extends TermValue {
		/**
		 * the exponents of the factors; true is 1, false -1
		 */
		boolean[] signs;

		/**
		 * 
		 * @param token
		 *            the {@link TermToken} to parse
		 */
		public MultDivValue(TermToken token) {
			TermToken[] toks = token.getNormalized().split("[\\*/]");
			operands = new Value[toks.length];
			signs = new boolean[toks.length];

			for (int i = 0; i < toks.length; i++) {
				signs[i] = true;
				if (toks[i].getNTokens() != 1
						&& toks[i].getToken(0).matches("\\/")) {
					signs[i] = false;
				}
				if (toks[i] instanceof TermToken
						&& ((TermToken) toks[i]).getNTokens() > 1) {
					operands[i] = new PowerValue(toks[i]).simplify();
				} else {
					operands[i] = toks[i].getToken(toks[i].getNTokens() - 1)
							.createValue();
				}
			}
		}

		@Override
		public Variable getVarValue(LogoRenderer2D renderer,
				OrderInterpreter oi, CodeLocationInfo cli) {
			Variable var = operands[0].getVarValue(renderer, oi, cli).getCopy(
					null);

			try {
				boolean integer = var instanceof Integer64;
				for (int i = 1; i < operands.length; i++) {
					Variable v = operands[i].getVarValue(renderer, oi, cli);

					if (integer && v instanceof Float64) {
						var = new Float64(null, var.getDoubleValue());
						integer = false;
					}

					if (signs[i])
						var.multiply(v);
					else
						var.divide(v);
				}
			} catch (InterpreterException ie) {
				ie.setCodeLocationInfo(cli);
				throw ie;
			}
			return var;
		}

		@Override
		public String toString() {
			return "MultDivValue [signs=" + Arrays.toString(signs)
					+ ", operands=" + Arrays.toString(operands) + "]";
		}

		public boolean isNotAnOperation() {
			return signs.length == 1 && signs[0] == true;
		}

		@Override
		public Value simplify() {
			if (isNotAnOperation()) {
				return operands[0];
			} else {
				return this;
			}
		}
	}

	public static class PowerValue extends TermValue {

		public PowerValue(TermToken token) {
			TermToken[] toks = token.getNormalized().split("\\^");
			operands = new Value[toks.length];

			for (int i = 0; i < toks.length; i++) {
				operands[i] = toks[i].getToken(toks[i].getNTokens() - 1)
						.createValue();
			}
		}

		@Override
		public Value simplify() {
			if (isNotAnOperation())
				return operands[0];
			return this;
		}

		@Override
		public boolean isNotAnOperation() {
			return operands.length == 1;
		}

		@Override
		public Variable getVarValue(LogoRenderer2D renderer,
				OrderInterpreter oi, CodeLocationInfo cli)
				throws InterpreterException {
			Variable var = operands[0].getVarValue(renderer, oi, cli).getCopy(
					null);

			try {
				boolean integer = var instanceof Integer64;
				for (int i = 1; i < operands.length; i++) {
					Variable v = operands[i].getVarValue(renderer, oi, cli);
					if (integer && v instanceof Float64) {
						var = new Float64(null, var.getDoubleValue());
						integer = false;
					}
					var.pow(v);
				}
			} catch (InterpreterException ie) {
				ie.setCodeLocationInfo(cli);
				throw ie;
			}
			return var;
		}
	}

	/**
	 * a function that has first to be invoked to evaluate the return value
	 * 
	 * @author Nicolas Winkler
	 * 
	 */
	public static class FunctionValue extends Value {
		private String name;
		private Value[] args;

		public FunctionValue(String name) {
			this.name = name;
		}

		@Override
		public Variable getVarValue(LogoRenderer2D renderer,
				OrderInterpreter oi, CodeLocationInfo cli) {
			Invokeable inv = oi.getSubroutine(name, args.length);
			if (inv instanceof Function) {
				Variable var = ((Function) inv).getVarValue(renderer, oi,
						this.args, cli);
				return var;
			}
			throw new InterpreterException("The function " + name + " with "
					+ args.length + " arguments " + "doesn't exist.", cli);
		}

		public Value[] getArgs() {
			return args;
		}

		public void setArgs(Value[] args) {
			this.args = args;
		}

		@Override
		public Value getOptimized(OrderOptimizer oo) {
			for (int i = 0; i < args.length; i++) {
				args[i] = args[i].getOptimized(oo);
			}
			return this;
		}
	}

	/**
	 * 
	 * @author Nicolas Winkler
	 * 
	 */
	public static class ConstantValue extends Value {
		// private String token;
		protected Variable val;

		protected ConstantValue() {
		}

		public ConstantValue(String token) {
			val = Variable.createConstant(token);
		}

		@Override
		public Variable getVarValue(LogoRenderer2D renderer,
				OrderInterpreter oi, CodeLocationInfo cli) {
			return val.getCopy(null);
		}

		@Override
		public String toString() {
			return "ConstantValue [val=" + val + "]";
		}
	}

	/**
	 * a reference by name to a variable
	 * 
	 * @author Nicolas Winkler
	 * 
	 */
	public static class VariableValue extends Value {
		/**
		 * the name of the variable
		 */
		protected String name;

		/**
		 * initialization
		 * 
		 * @param name
		 *            the name of the variable
		 */
		public VariableValue(String name) {
			this.name = name;
		}

		@Override
		public Variable getVarValue(LogoRenderer2D renderer,
				OrderInterpreter oi, CodeLocationInfo cli) {
			Variable v = oi.getVariable(name);
			if (v == null)
				throw new InterpreterException("variable " + name
						+ " not found.", cli);
			return v;
		}

		@Override
		public String toString() {
			return "VariableValue [name=" + name + "]";
		}

		public static VariableValue create(Token variableName) {
			if (!variableName.canBeVariable()) {
				throw new InterpreterException("Invalid token: "
						+ variableName.getStringToken());
			}
			if (variableName instanceof TextToken) {
				return new VariableValue(variableName.getStringToken());
			}
			if (variableName instanceof ArrayAccessToken) {
				return new ArrayAccessValue((ArrayAccessToken) variableName);
			}
			return null;
		}

		@Override
		public Value getOptimized(OrderOptimizer oo) {
			StackType st = oo.getStackType(name);
			int index = oo.getStackIndex(name);

			if (st == StackType.LOCAL)
				return new StackReferenceValue(index);
			else if (st == StackType.GLOBAL)
				return new GlobalStackReferenceValue(index);
			else
				return super.getOptimized(oo);
		}
	}

	public static class ArrayAccessValue extends VariableValue {
		private Value array;
		private Value[] indices;

		public ArrayAccessValue(ArrayAccessToken aat) {
			super("");
			array = aat.getArrayVariable().createValue();
			indices = new Value[aat.getNIndices()];
			for (int i = 0; i < indices.length; i++) {
				indices[i] = aat.getIndex(i).createValue();
			}
		}

		@Override
		public Variable getVarValue(LogoRenderer2D renderer,
				OrderInterpreter oi, CodeLocationInfo cli)
				throws InterpreterException {
			Variable v = array.getVarValue(renderer, oi, cli);

			if (v == null)
				throw new InterpreterException("Invalid array access.", cli);

			for (int i = indices.length - 1; i > 0; i--) {
				int ind = (int) indices[i].getVarValue(renderer, oi, cli)
						.getLongValue();
				if (v instanceof Array) {
					Variable[] arr = ((Array) v).array;
					if (ind >= arr.length || ind < 0)
						throwOutOfBounds(cli);
					v = arr[ind];
				} else {
					throw new InterpreterException(
							"Invalid array access. Variable is not an array.",
							cli);
				}
			}

			int ind = (int) indices[0].getVarValue(renderer, oi, cli)
					.getLongValue();

			while (v instanceof Reference) {
				v = ((Reference) v).wrapped;
			}

			if (v instanceof Array) {
				Variable[] arr = ((Array) v).array;
				if (ind >= arr.length || ind < 0)
					throwOutOfBounds(cli);
				return ((Array) v).array[ind];
			}

			if (v instanceof LogoString) {
				return new LogoChar(null, ((LogoString) v).value.charAt(ind));
			}
			throw new InterpreterException("Invalid array access.", cli);
		}

		private void throwOutOfBounds(CodeLocationInfo cli) {
			throw new InterpreterException("Array index out of bounds.", cli);
		}

		@Override
		public Value getOptimized(OrderOptimizer oo) {
			for (int i = 0; i < indices.length; i++)
				indices[i] = indices[i].getOptimized(oo);

			if (indices.length == 0)
				return array.getOptimized(oo);

			array = array.getOptimized(oo);
			return this;
		}
	}

	/**
	 * 
	 * @author Nicolas Winkler
	 * 
	 */
	public static class StringValue extends ConstantValue {
		public StringValue(String token) {
			val = new LogoString(null, token);
		}

		@Override
		public Variable getVarValue(LogoRenderer2D renderer,
				OrderInterpreter oi, CodeLocationInfo cli) {
			return val;
		}

		@Override
		public String toString() {
			return "StringValue [val=" + val + "]";
		}
	}

	public abstract static class Comparison {
		protected Value left;
		protected Value right;

		public Comparison(Value left, Value right) {
			this.left = left;
			this.right = right;
		}

		public boolean compare(LogoRenderer2D renderer, OrderInterpreter oi,
				CodeLocationInfo cli) {
			Variable a = left.getVarValue(renderer, oi, cli);
			Variable b = right.getVarValue(renderer, oi, cli);
			return compareVars(a, b);
		}

		protected abstract boolean compareVars(Variable left, Variable right);
	}

	public static class LessThan extends Comparison {
		public LessThan(Value left, Value right) {
			super(left, right);
		}

		protected boolean compareVars(Variable left, Variable right) {
			return left.lessThan(right);
		}
	}

	public static class LessEqual extends Comparison {
		public LessEqual(Value left, Value right) {
			super(left, right);
		}

		protected boolean compareVars(Variable left, Variable right) {
			return left.lessEqual(right);
		}
	}

	public static class Equal extends Comparison {
		public Equal(Value left, Value right) {
			super(left, right);
		}

		protected boolean compareVars(Variable left, Variable right) {
			return left.equalValue(right);
		}
	}

	public static class NotEqual extends Comparison {
		public NotEqual(Value left, Value right) {
			super(left, right);
		}

		protected boolean compareVars(Variable left, Variable right) {
			return !left.equalValue(right);
		}
	}
}
