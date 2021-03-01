package turtlepp.exec.fast;

import java.util.Arrays;

import turtlepp.InterpreterException;
import turtlepp.LogoRenderer2D;
import turtlepp.datatypes.Array;
import turtlepp.datatypes.Bool;
import turtlepp.datatypes.Float64;
import turtlepp.datatypes.Integer64;
import turtlepp.datatypes.InvalidTypecastException;
import turtlepp.datatypes.LogoChar;
import turtlepp.datatypes.LogoString;
import turtlepp.datatypes.Reference;
import turtlepp.datatypes.Value;
import turtlepp.datatypes.Variable;
import turtlepp.datatypes.Value.VariableValue;
import turtlepp.exec.CodeLocationInfo;
import turtlepp.exec.Executable;
import turtlepp.exec.Invokeable;
import turtlepp.exec.OrderInterpreter;
import turtlepp.exec.OrderOptimizer;
import turtlepp.exec.Tokenizer.Token;

/**
 * Interface for simple commands that implement an "execute" function.
 * 
 * @author Nicolas Winkler
 * 
 */
public abstract class Command extends Executable {

	protected CodeLocationInfo cli;

	public Command(CodeLocationInfo cli) {
		this.cli = cli;
	}

	protected void throwException(String message) {
		throw new InterpreterException(message, cli);
	}

	public static abstract class Operation extends Command {
		Value leftArgument;
		Value argument;

		public Operation(VariableValue varName, Value argument,
				CodeLocationInfo cli) {
			super(cli);
			this.leftArgument = varName;
			this.argument = argument;
		}

		protected Variable getVariable(LogoRenderer2D renderer,
				OrderInterpreter oi) {
			Variable var = leftArgument.getVarValue(renderer, oi, cli);
			if (var == null)
				throwException("variable " + leftArgument + " not found!");
			return var;
		}

		@Override
		public Executable getOptimized(OrderOptimizer oo) {
			leftArgument = leftArgument.getOptimized(oo);
			argument = argument.getOptimized(oo);
			return this;
		}
	}

	public static class Set extends Operation {
		public Set(VariableValue varName, Value argument, CodeLocationInfo cli) {
			super(varName, argument, cli);
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			Variable v = getVariable(renderer, oi);
			v.set(argument.getVarValue(renderer, oi, cli));
			return ReturnValue.NOTHING;
		}
	}

	public static class Add extends Operation {
		public Add(VariableValue varName, Value argument, CodeLocationInfo cli) {
			super(varName, argument, cli);
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			Variable v = getVariable(renderer, oi);
			v.add(argument.getVarValue(renderer, oi, cli));
			return ReturnValue.NOTHING;
		}
	}

	public static class Sub extends Operation {
		public Sub(VariableValue varName, Value argument, CodeLocationInfo cli) {
			super(varName, argument, cli);
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			Variable v = getVariable(renderer, oi);
			v.subtract(argument.getVarValue(renderer, oi, cli));
			return ReturnValue.NOTHING;
		}
	}

	public static class Mult extends Operation {
		public Mult(VariableValue varName, Value argument, CodeLocationInfo cli) {
			super(varName, argument, cli);
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			Variable v = getVariable(renderer, oi);
			v.multiply(argument.getVarValue(renderer, oi, cli));
			return ReturnValue.NOTHING;
		}
	}

	public static class Div extends Operation {
		public Div(VariableValue varName, Value argument, CodeLocationInfo cli) {
			super(varName, argument, cli);
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			Variable v = getVariable(renderer, oi);
			v.divide(argument.getVarValue(renderer, oi, cli));
			return ReturnValue.NOTHING;
		}
	}

	public static class Pow extends Operation {
		public Pow(VariableValue varName, Value argument, CodeLocationInfo cli) {
			super(varName, argument, cli);
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			Variable v = getVariable(renderer, oi);
			v.pow(argument.getVarValue(renderer, oi, cli));
			return ReturnValue.NOTHING;
		}
	}

	public static class Invoke extends Command {
		private String invokeableName;
		private Value[] args;

		public Invoke(String name, Value[] args, CodeLocationInfo cli) {
			super(cli);
			this.invokeableName = name;
			this.args = args;
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			Invokeable invoke = oi.getSubroutine(invokeableName, args.length);
			if (invoke != null) {
				invoke.invoke(renderer, oi, args, cli);
				return ReturnValue.NOTHING;
			}
			throwException("Could not find subroutine " + invokeableName
					+ " with " + args.length + " arguments.");
			return null;
		}

		/**
		 * tries to create an instance of {@link InvokeFast}
		 */
		@Override
		public Executable getOptimized(OrderOptimizer oo) {
			Invokeable invoke = oo.getSubroutine(invokeableName, args.length);

			if (invoke != null)
				return new InvokeFast(invoke, args, cli).getOptimized(oo);
			else
				return this;
		}
	}

	public static class InvokeFast extends Command {
		private Invokeable invokeable;
		private Value[] args;

		public InvokeFast(Invokeable invokeable, Value[] args,
				CodeLocationInfo cli) {
			super(cli);
			this.invokeable = invokeable;
			this.args = args;
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			invokeable.invoke(renderer, oi, args, cli);
			return ReturnValue.NOTHING;
		}

		@Override
		public Executable getOptimized(OrderOptimizer oo) {
			for (int i = 0; i < args.length; i++) {
				args[i] = args[i].getOptimized(oo);
			}
			return this;
		}
	}

	public static class Line extends Command {
		Value x1, y1, x2, y2;

		public Line(Value x1, Value y1, Value x2, Value y2, CodeLocationInfo cli) {
			super(cli);
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			double vx1 = x1.getVarValue(renderer, oi, cli).getDoubleValue();
			double vy1 = y1.getVarValue(renderer, oi, cli).getDoubleValue();
			double vx2 = x2.getVarValue(renderer, oi, cli).getDoubleValue();
			double vy2 = y2.getVarValue(renderer, oi, cli).getDoubleValue();
			renderer.line(vx1, vy1, vx2, vy2);
			return ReturnValue.NOTHING;
		}

		@Override
		public Executable getOptimized(OrderOptimizer oo) {
			x1 = x1.getOptimized(oo);
			y1 = y1.getOptimized(oo);
			x2 = x2.getOptimized(oo);
			y2 = y2.getOptimized(oo);
			return this;
		}
	}

	public static class Triangle extends Command {
		Value x1, y1, x2, y2, x3, y3;

		public Triangle(Value x1, Value y1, Value x2, Value y2, Value x3,
				Value y3, CodeLocationInfo cli) {
			super(cli);
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
			this.x3 = x3;
			this.y3 = y3;
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			double vx1 = x1.getVarValue(renderer, oi, cli).getDoubleValue();
			double vy1 = y1.getVarValue(renderer, oi, cli).getDoubleValue();
			double vx2 = x2.getVarValue(renderer, oi, cli).getDoubleValue();
			double vy2 = y2.getVarValue(renderer, oi, cli).getDoubleValue();
			double vx3 = x3.getVarValue(renderer, oi, cli).getDoubleValue();
			double vy3 = y3.getVarValue(renderer, oi, cli).getDoubleValue();
			renderer.triangle(vx1, vy1, vx2, vy2, vx3, vy3);
			// System.out.println(vx1 + " --- " + vy1 + " --- " + vx2 + " --- "
			// + vy2 + " --- " + vx3 + " --- " + vy3 + " --- ");
			return ReturnValue.NOTHING;
		}

		@Override
		public Executable getOptimized(OrderOptimizer oo) {
			x1 = x1.getOptimized(oo);
			y1 = y1.getOptimized(oo);
			x2 = x2.getOptimized(oo);
			y2 = y2.getOptimized(oo);
			x3 = x3.getOptimized(oo);
			y3 = y3.getOptimized(oo);
			return this;
		}
	}

	public static class PenColor extends Command {
		Value r, g, b;

		public PenColor(Value r, Value g, Value b, CodeLocationInfo cli) {
			super(cli);
			this.r = r;
			this.g = g;
			this.b = b;
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			double red = r.getVarValue(renderer, oi, cli).getDoubleValue();
			double green = g.getVarValue(renderer, oi, cli).getDoubleValue();
			double blue = b.getVarValue(renderer, oi, cli).getDoubleValue();
			renderer.penColor((float) red, (float) green, (float) blue);
			return ReturnValue.NOTHING;
		}

		@Override
		public Executable getOptimized(OrderOptimizer oo) {
			r = r.getOptimized(oo);
			g = g.getOptimized(oo);
			b = b.getOptimized(oo);
			return this;
		}
	}

	public static abstract class TwoArgumentCommand extends Command {
		protected Value a;
		protected Value b;

		public TwoArgumentCommand(Token a, Token b, CodeLocationInfo cli) {
			super(cli);
			this.a = a.createValue();
			this.b = b.createValue();
		}

		@Override
		public Executable getOptimized(OrderOptimizer oo) {
			a = a.getOptimized(oo);
			b = b.getOptimized(oo);
			return this;
		}
	}

	public static class SetPos extends TwoArgumentCommand {
		public SetPos(Token a, Token b, CodeLocationInfo cli) {
			super(a, b, cli);
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			Variable x = a.getVarValue(renderer, oi, cli);
			Variable y = b.getVarValue(renderer, oi, cli);
			renderer.setPosition(x.getDoubleValue(), y.getDoubleValue());
			return ReturnValue.NOTHING;
		}
	}

	public static class Skew extends TwoArgumentCommand {
		public Skew(Token a, Token b, CodeLocationInfo cli) {
			super(a, b, cli);
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			Variable x = a.getVarValue(renderer, oi, cli);
			Variable y = b.getVarValue(renderer, oi, cli);
			renderer.skew(x.getDoubleValue(), y.getDoubleValue());
			return ReturnValue.NOTHING;
		}
	}

	public static class Translate extends TwoArgumentCommand {
		public Translate(Token a, Token b, CodeLocationInfo cli) {
			super(a, b, cli);
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			Variable x = a.getVarValue(renderer, oi, cli);
			Variable y = b.getVarValue(renderer, oi, cli);
			renderer.translate(x.getDoubleValue(), y.getDoubleValue());
			return ReturnValue.NOTHING;
		}
	}

	public static class Scale extends TwoArgumentCommand {
		public Scale(Token a, Token b, CodeLocationInfo cli) {
			super(a, b, cli);
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			Variable x = a.getVarValue(renderer, oi, cli);
			Variable y = b.getVarValue(renderer, oi, cli);
			renderer.scale(x.getDoubleValue(), y.getDoubleValue());
			return ReturnValue.NOTHING;
		}
	}

	public static class Point extends TwoArgumentCommand {
		public Point(Token a, Token b, CodeLocationInfo cli) {
			super(a, b, cli);
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			Variable x = a.getVarValue(renderer, oi, cli);
			Variable y = b.getVarValue(renderer, oi, cli);
			renderer.point(x.getDoubleValue(), y.getDoubleValue());
			return ReturnValue.NOTHING;
		}
	}
	
	public static class Ellipse extends Command {
		Value x, y, w, h;
		public Ellipse(Value x, Value y, Value w, Value h, CodeLocationInfo cli) {
			super(cli);
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			Variable x = this.x.getVarValue(renderer, oi, cli);
			Variable y = this.y.getVarValue(renderer, oi, cli);
			Variable w = this.w.getVarValue(renderer, oi, cli);
			Variable h = this.h.getVarValue(renderer, oi, cli);
			renderer.ellipse(x.getDoubleValue(), y.getDoubleValue(),
					w.getDoubleValue(), h.getDoubleValue());
			return ReturnValue.NOTHING;
		}
	}

	/**
	 * Instantiates new variables. This class is however not used to create new
	 * arrays.
	 * 
	 * @author Nicolas Winkler
	 * 
	 */
	public static class NewVariable extends Command {
		Variable newVar;
		Value initialValue;

		public NewVariable(Variable newVar, CodeLocationInfo cli) {
			super(cli);
			this.newVar = newVar;
			initialValue = null;
		}

		public NewVariable(Variable newVar, Value initialValue,
				CodeLocationInfo cli) {
			super(cli);
			this.newVar = newVar;
			this.initialValue = initialValue;
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			Variable var = newVar.getCopy(newVar.getName());
			oi.pushVariable(var);
			if (initialValue != null) {
				try {
					var.set(initialValue.getVarValue(renderer, oi, cli));
				} catch (InvalidTypecastException ite) {
					ite.setCodeLocationInfo(cli);
					throw ite;
				}
			}
			return ReturnValue.NEW_VARIABLE_CREATED;
		}

		public void setInitialValue(Value val) {
			this.initialValue = val;
		}

		@Override
		public Executable getOptimized(OrderOptimizer oo) {
			oo.pushVariable(newVar.getName());
			if (initialValue != null)
				initialValue = initialValue.getOptimized(oo);
			return this;
		}
	}

	public static class Rotate extends Command {
		Value v;

		public Rotate(Value v, CodeLocationInfo cli) {
			super(cli);
			this.v = v;
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			renderer.rotate(v.getVarValue(renderer, oi, cli).getDoubleValue());
			return ReturnValue.NOTHING;
		}

		@Override
		public Executable getOptimized(OrderOptimizer oo) {
			v = v.getOptimized(oo);
			return this;
		}
	}

	public static class Turn extends Command {
		Value v;
		boolean right;

		public Turn(Value v, boolean right, CodeLocationInfo cli) {
			super(cli);
			this.v = v;
			this.right = right;
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			double val = v.getVarValue(renderer, oi, cli).getDoubleValue();
			if (right)
				renderer.right(val);
			else
				renderer.left(val);
			return ReturnValue.NOTHING;
		}

		@Override
		public Executable getOptimized(OrderOptimizer oo) {
			v = v.getOptimized(oo);
			return this;
		}
	}

	public static class Print extends Command {
		private Value[] args;

		public Print(Value[] args, CodeLocationInfo cli) {
			super(cli);
			this.args = args;
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			String text = "";
			for (int i = 0; i < args.length; i++) {
				Variable v = args[i].getVarValue(renderer, oi, cli);
				if (v != null) {
					text += v.getStringValue();
				}
				if (i < args.length - 1)
					text += ", ";
			}
			renderer.print(text);
			return ReturnValue.NOTHING;
		}

		@Override
		public String toString() {
			return "Print [args=" + Arrays.toString(args) + "]";
		}

		@Override
		public Executable getOptimized(OrderOptimizer oo) {
			for (int i = 0; i < args.length; i++) {
				args[i] = args[i].getOptimized(oo);
			}
			return this;
		}
	}
	
	public static class PrintLn extends Command {
		private Value[] args;

		public PrintLn(Value[] args, CodeLocationInfo cli) {
			super(cli);
			this.args = args;
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			String text = "";
			for (int i = 0; i < args.length; i++) {
				Variable v = args[i].getVarValue(renderer, oi, cli);
				text += v.getStringValue();
				if (i < args.length - 1)
					text += ", ";
			}
			renderer.println(text);
			return ReturnValue.NOTHING;
		}

		@Override
		public String toString() {
			return "PrintLn [args=" + Arrays.toString(args) + "]";
		}

		@Override
		public Executable getOptimized(OrderOptimizer oo) {
			for (int i = 0; i < args.length; i++) {
				args[i] = args[i].getOptimized(oo);
			}
			return this;
		}
	}

	public static class Exit extends Command {
		ReturnValue returnValue;

		public Exit(ReturnValue retVal, CodeLocationInfo cli) {
			super(cli);
			returnValue = retVal;
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			return returnValue;
		}

	}

	public static class ResetMatrix extends Command {
		public ResetMatrix(CodeLocationInfo cli) {
			super(cli);
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			renderer.resetMatrix();
			return ReturnValue.NOTHING;
		}

	}

	public static class PopMatrix extends Command {
		public PopMatrix(CodeLocationInfo cli) {
			super(cli);
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			renderer.popMatrix();
			return ReturnValue.NOTHING;
		}
	}

	public static class PushMatrix extends Command {
		public PushMatrix(CodeLocationInfo cli) {
			super(cli);
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			renderer.pushMatrix();
			return ReturnValue.NOTHING;
		}
	}

	public static class Forward extends Command {
		Value val;
		boolean forward;

		public Forward(Value val, CodeLocationInfo cli) {
			super(cli);
			this.val = val;
			forward = true;
		}

		public Forward(Value v, boolean b, CodeLocationInfo cli) {
			this(v, cli);
			forward = b;
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			if (forward)
				renderer.forward(val.getVarValue(renderer, oi, cli)
						.getDoubleValue());
			else
				renderer.backward(val.getVarValue(renderer, oi, cli)
						.getDoubleValue());
			return ReturnValue.NOTHING;
		}

		@Override
		public Executable getOptimized(OrderOptimizer oo) {
			val = val.getOptimized(oo);
			return this;
		}
	}

	public static class ClearOutputCommand extends Command {
		public ClearOutputCommand(CodeLocationInfo cli) {
			super(cli);
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			renderer.clearOutput();
			return ReturnValue.NOTHING;
		}
	}

	public static class Polygon extends Command {
		private Value[] args;
		private double[] xPos;
		private double[] yPos;

		public Polygon(Value[] args, CodeLocationInfo cli) {
			super(cli);
			this.args = args;
			if ((args.length & 0x01) != 0)
				throw new InterpreterException(
						"Polygon must have an even amount of arguments.", cli);
			xPos = new double[args.length / 2];
			yPos = new double[args.length / 2];
		}

		@Override
		public synchronized ReturnValue execute(LogoRenderer2D renderer,
				OrderInterpreter oi) {
			for (int i = 0; i < xPos.length; i++) {
				xPos[i] = (int) args[i + i].getVarValue(renderer, oi, cli)
						.getLongValue();
				yPos[i] = (int) args[i + i + 1].getVarValue(renderer, oi, cli)
						.getLongValue();
			}
			renderer.polygon(xPos, yPos);
			return ReturnValue.NOTHING;
		}

		@Override
		public Executable getOptimized(OrderOptimizer oo) {
			for (int i = 0; i < args.length; i++) {
				args[i] = args[i].getOptimized(oo);
			}
			return this;
		}
	}

	public static class Sleep extends Command {
		Value millis;

		public Sleep(Value millis, CodeLocationInfo cli) {
			super(cli);
			this.millis = millis;
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			long l = millis.getVarValue(renderer, oi, cli).getLongValue();
			try {
				Thread.sleep(l);
			} catch (InterruptedException e) {
				// too bad... just continue
			}
			return ReturnValue.NOTHING;
		}

		@Override
		public Executable getOptimized(OrderOptimizer oo) {
			millis = millis.getOptimized(oo);
			return this;
		}
	}

	public static class Reset extends Command {
		public Reset(CodeLocationInfo cli) {
			super(cli);
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			renderer.reset();
			return ReturnValue.NOTHING;
		}
	}

	public static class CreateArray extends Command {
		private Value[] length;
		private String name;
		private Variable template;

		public CreateArray(String name, Value[] length, CodeLocationInfo cli,
				Variable template) {
			super(cli);
			this.name = name;
			this.length = length;
			this.template = template;
		}

		private Variable[] initialize(LogoRenderer2D renderer,
				OrderInterpreter oi, int level) {
			Variable[] content = null;

			int len = (int) length[level].getVarValue(renderer, oi, cli)
					.getLongValue();

			if (level <= 0) {
				if (template instanceof Integer64)
					content = new Integer64[len];
				else if (template instanceof Float64)
					content = new Float64[len];
				else if (template instanceof LogoString)
					content = new LogoString[len];
				else if (template instanceof Reference)
					content = new Reference[len];
				else if (template instanceof Bool)
					content = new Bool[len];
				else if (template instanceof LogoChar)
					content = new LogoChar[len];
				else if (template instanceof Array)
					content = new Array[len];
				else
					throw new InterpreterException("Unknown array type.", cli);

				for (int i = 0; i < content.length; i++)
					content[i] = template.getCopy(null);
			} else {
				content = new Array[len];
				Array arr = new Array(null, initialize(renderer, oi, level - 1));
				if (len > 0)
					content[0] = arr;
				for (int i = 1; i < len; i++) {
					content[i] = arr.getCopy(null);
				}
			}

			return content;
		}

		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			Variable[] content = initialize(renderer, oi, length.length - 1);
			Array arr = new Array(name, content);
			oi.pushVariable(arr);
			return ReturnValue.NOTHING;
		}

		@Override
		public Executable getOptimized(OrderOptimizer oo) {
			for (int i = 0; i < length.length; i++)
				length[i] = length[i].getOptimized(oo);
			oo.pushVariable(name);
			return this;
		}
	}

	public static class SetLength extends Command {
		private Value var;
		private Value length;

		public SetLength(VariableValue var, Value length, CodeLocationInfo cli) {
			super(cli);
			this.var = var;
			this.length = length;
		}

		@SuppressWarnings({ "unused" })
		@Override
		public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
			if (true)
				throw new InterpreterException(
						"setLength command not yet implemented.", cli);

			Variable v = var.getVarValue(renderer, oi, cli);
			if (v instanceof Array) {
				Variable len = length.getVarValue(renderer, oi, cli);
				Variable[] temp = ((Array) v).array;
			}
			return null;
		}

		@Override
		public Executable getOptimized(OrderOptimizer oo) {
			var = var.getOptimized(oo);
			length = length.getOptimized(oo);
			return this;
		}
	}
}
