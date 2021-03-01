package turtlepp.exec;

import turtlepp.InterpreterException;
import turtlepp.datatypes.Bool;
import turtlepp.datatypes.Float64;
import turtlepp.datatypes.Integer64;
import turtlepp.datatypes.LogoChar;
import turtlepp.datatypes.LogoString;
import turtlepp.datatypes.Reference;
import turtlepp.datatypes.Value;
import turtlepp.datatypes.Variable;
import turtlepp.datatypes.Value.VariableValue;
import turtlepp.exec.Executable.ReturnValue;
import turtlepp.exec.Tokenizer.ArrayAccessToken;
import turtlepp.exec.Tokenizer.TermToken;
import turtlepp.exec.Tokenizer.Token;
import turtlepp.exec.fast.*;

/**
 * Translates the source code into faster commands. The commands are stored in
 * subclasses of {@link Executable}.
 * 
 * @author Nicolas Winkler
 * 
 */
public class Compiler {

	/**
	 * tries to create a type of {@link Command}. If that fails, it returns
	 * itself
	 * 
	 * @return the new {@link Command} or this
	 */
	public static Executable compile(String command, CodeLocationInfo cli) {
		Token[] tokens = null;
		try {
			tokens = Tokenizer.tokenize(command);
		} catch (ParserException pe) {
			pe.printStackTrace();
			throw new ParserException(pe.getMessage(), cli);
		}

		// System.out.println(Arrays.toString(tokens));
		// System.out.println(tokens[1].createValue());

		if (tokens == null
				|| (tokens.length == 1 && tokens[0] instanceof TermToken)) {
			throw new ParserException(
					"Invalid Command.", cli);
		}
		// System.out.println(Arrays.toString(tokens));

		if (tokens[0].isTextCommand("clear")) {
			Value r = null, g = null, b = null;
			if (tokens.length >= 4) {
				r = tokens[1].createValue();
				g = tokens[2].createValue();
				b = tokens[3].createValue();
			}
			return new ClearCommand(r, g, b, cli);
		}

		if (tokens[0].isTextCommand("clearOutput")) {
			return new Command.ClearOutputCommand(cli);
		}
		if (tokens[0].isTextCommand("penUp") || tokens[0].isTextCommand("pu")) {
			return new SetPenCommand(false, cli);
		}
		if (tokens[0].isTextCommand("penDown") || tokens[0].isTextCommand("pd")) {
			return new SetPenCommand(true, cli);
		}
		if (tokens[0].isTextCommand("antialiasingOn")) {
			return new SetAntialiasingCommand(true, cli);
		}
		if (tokens[0].isTextCommand("antialiasingOff")) {
			return new SetAntialiasingCommand(false, cli);
		}
		if (tokens[0].isTextCommand("hideTurtle")
				|| tokens[0].isTextCommand("ht")) {
			return new SetTurtleCommand(false, cli);
		}
		if (tokens[0].isTextCommand("showTurtle")
				|| tokens[0].isTextCommand("st")) {
			return new SetTurtleCommand(true, cli);
		}
		if (tokens[0].isTextCommand("pushPosition")) {
			return new PushPositionCommand(cli);
		}
		if (tokens[0].isTextCommand("popPosition")) {
			return new PopPositionCommand(cli);
		}
		if (tokens[0].isTextCommand("resetRotation")) {
			return new ResetRotationCommand(cli);
		}

		if (tokens[0].isTextCommand("pushMatrix")) {
			return new Command.PushMatrix(cli);
		}

		if (tokens[0].isTextCommand("popMatrix")) {
			return new Command.PopMatrix(cli);
		}

		if (tokens[0].isTextCommand("resetMatrix")
				|| tokens[0].isTextCommand("loadIdentity")) {
			return new Command.ResetMatrix(cli);
		}

		if (tokens[0].isTextCommand("reset")) {
			return new Command.Reset(cli);
		}

		if (tokens.length < 2 && tokens[0].isTextCommand("exit")) {
			return new Command.Exit(ReturnValue.EXIT, cli);
		}
		if (tokens.length >= 2 && tokens[0].isTextCommand("exit")) {
			if (tokens[1].isTextCommand("sub")) {
				return new Command.Exit(ReturnValue.EXIT_SUB, cli);
			}
			if (tokens[1].isTextCommand("function")) {
				return new Command.Exit(ReturnValue.EXIT_FUNCTION, cli);
			}
			if (tokens[1].isTextCommand("repeat")) {
				return new Command.Exit(ReturnValue.EXIT_REPEAT, cli);
			}
			if (tokens[1].isTextCommand("while")) {
				return new Command.Exit(ReturnValue.EXIT_WHILE, cli);
			}
		}

		if (tokens.length >= 1 && tokens[0].isTextCommand("print")) {
			Value[] args = new Value[tokens.length - 1];
			for (int i = 1; i < tokens.length; i++) {
				args[i - 1] = tokens[i].createValue();
			}
			// System.out.println(tokens[1] + " --- " + args[0]);
			return new Command.Print(args, cli);
		}

		if (tokens.length == 2 && tokens[0].isTextCommand("sleep")) {
			Value v = tokens[1].createValue();
			return new Command.Sleep(v, cli);
		}

		if (tokens.length == 2
				&& (tokens[0].isTextCommand("forward") || tokens[0]
						.isTextCommand("fd"))) {
			Value v = tokens[1].createValue();
			return new Command.Forward(v, true, cli);
		}

		if (tokens.length == 2
				&& (tokens[0].isTextCommand("backward") || tokens[0]
						.isTextCommand("bw"))) {
			Value v = tokens[1].createValue();
			return new Command.Forward(v, false, cli);
		}

		if (tokens.length == 2
				&& (tokens[0].isTextCommand("right") || tokens[0]
						.isTextCommand("rt"))) {
			Value v = tokens[1].createValue();
			return new Command.Turn(v, true, cli);
		}
		if (tokens.length == 2
				&& (tokens[0].isTextCommand("left") || tokens[0]
						.isTextCommand("lt"))) {
			Value v = tokens[1].createValue();
			return new Command.Turn(v, false, cli);
		}

		if (tokens.length == 2 && tokens[0].isTextCommand("rotate")) {
			Value v = tokens[1].createValue();
			return new Command.Rotate(v, cli);
		}

		if (tokens.length == 3 && tokens[0].isTextCommand("setLength")) {
			return new Command.SetLength(null, null, cli);
		}

		arrayInit: if (tokens.length >= 2 && tokens[0] instanceof TermToken) {
			Token t = ((TermToken) tokens[0]).simplify();
			if (t instanceof ArrayAccessToken) {
				ArrayAccessToken aat = (ArrayAccessToken) t;
				//String varType = aat.getArrayVariable().getStringToken();
				Value[] length = new Value[aat.getNIndices()];
				for (int i = 0; i < length.length; i++) {
					length[i] = aat.getIndex(i).createValue();
				}
				String varName = tokens[1].getStringToken();
				Variable template = aat.createVariableTemplate();
				//System.out.println(template);
				
				/*if (varType.equalsIgnoreCase(Variable
						.getTypeName(Integer64.class)))
					template = new Integer64(null);
				else if (varType.equalsIgnoreCase(Variable
						.getTypeName(Float64.class)))
					template = new Float64(null);
				else if (varType.equalsIgnoreCase(Variable
						.getTypeName(LogoString.class)))
					template = new LogoString(null);
				else if (varType.equalsIgnoreCase(Variable
						.getTypeName(Bool.class)))
					template = new Bool(null);
				else if (varType.equalsIgnoreCase(Variable
						.getTypeName(Reference.class)))
					template = new Reference(null);
				else if (varType.equalsIgnoreCase(Variable
						.getTypeName(LogoChar.class)))
					template = new LogoChar(null);
				else
					break arrayInit;*/
				
				if (template == null)
					break arrayInit;

				Command.CreateArray ca = new Command.CreateArray(varName,
						length, cli, template);
				return ca;
			}
		}
		if (tokens.length >= 2 && tokens[0].isTextCommand("int")) {
			Integer64 var = new Integer64(tokens[1].getStringToken());
			Command.NewVariable fcn = new Command.NewVariable(var, cli);
			if (tokens.length == 4) {
				if (tokens[2].isTextCommand("=")) {
					Value val = tokens[3].createValue();
					fcn.setInitialValue(val);
				} else {
					throw new InterpreterException(
							"invalid initialization of int "
									+ tokens[1].getStringToken() + ".", cli);
				}
			}
			return fcn;
		}

		if (tokens.length >= 2 && tokens[0].isTextCommand("float")) {
			Float64 var = new Float64(tokens[1].getStringToken());
			Command.NewVariable fcn = new Command.NewVariable(var, cli);
			if (tokens.length == 4) {
				if (tokens[2].isTextCommand("=")) {
					Value val = tokens[3].createValue();
					fcn.setInitialValue(val);
				} else {
					throw new InterpreterException(
							"invalid initialization of float "
									+ tokens[1].getStringToken() + ".", cli);
				}
			}
			return fcn;
		}

		if (tokens.length >= 2 && tokens[0].isTextCommand("boolean")) {
			Bool var = new Bool(tokens[1].getStringToken());
			Command.NewVariable fcn = new Command.NewVariable(var, cli);
			if (tokens.length == 4) {
				if (tokens[2].isTextCommand("=")) {
					Value val = tokens[3].createValue();
					fcn.setInitialValue(val);
				} else {
					throw new InterpreterException(
							"invalid initialization of boolean "
									+ tokens[1].getStringToken() + ".", cli);
				}
			}
			return fcn;
		}

		if (tokens.length >= 2 && tokens[0].isTextCommand("char")) {
			LogoChar var = new LogoChar(tokens[1].getStringToken());
			Command.NewVariable fcn = new Command.NewVariable(var, cli);
			if (tokens.length == 4) {
				if (tokens[2].isTextCommand("=")) {
					Value val = tokens[3].createValue();
					fcn.setInitialValue(val);
				} else {
					throw new InterpreterException(
							"invalid initialization of char "
									+ tokens[1].getStringToken() + ".", cli);
				}
			}
			return fcn;
		}

		if (tokens.length >= 2 && tokens[0].isTextCommand("string")) {
			LogoString var = new LogoString(tokens[1].getStringToken());
			Command.NewVariable fcn = new Command.NewVariable(var, cli);
			if (tokens.length == 4) {
				if (tokens[2].isTextCommand("=")) {
					Value val = tokens[3].createValue();
					fcn.setInitialValue(val);
				} else {
					throw new InterpreterException(
							"invalid initialization of string "
									+ tokens[1].getStringToken() + ".", cli);
				}
			}
			return fcn;
		}

		if (tokens.length >= 2 && tokens[0].isTextCommand("ref")) {
			Reference var = new Reference(tokens[1].getStringToken());
			Command.NewVariable fcn = new Command.NewVariable(var, cli);
			if (tokens.length == 4) {
				if (tokens[2].isTextCommand("=")) {
					Value val = tokens[3].createValue();
					fcn.setInitialValue(val);
				} else {
					throw new InterpreterException(
							"invalid initialization of ref "
									+ tokens[1].getStringToken() + ".", cli);
				}
			}
			return fcn;
		}

		if (tokens.length >= 3 && (tokens[0].isTextCommand("setPosition")
				|| tokens[0].isTextCommand("setPos"))) {
			return new Command.SetPos(tokens[1], tokens[2], cli);
		}
		if (tokens.length >= 3 && tokens[0].isTextCommand("skew")) {
			return new Command.Skew(tokens[1], tokens[2], cli);
		}
		if (tokens.length >= 3 && tokens[0].isTextCommand("translate")) {
			return new Command.Translate(tokens[1], tokens[2], cli);
		}
		if (tokens.length >= 3 && tokens[0].isTextCommand("scale")) {
			return new Command.Scale(tokens[1], tokens[2], cli);
		}
		if (tokens.length >= 3 && tokens[0].isTextCommand("point")) {
			return new Command.Point(tokens[1], tokens[2], cli);
		}

		if (tokens.length >= 3
				&& (tokens[1].isTextCommand("=")
						|| tokens[1].isTextCommand("+=")
						|| tokens[1].isTextCommand("-=")
						|| tokens[1].isTextCommand("*=") || tokens[1]
							.isTextCommand("/=")
				|| tokens[1].isTextCommand("^="))) {

			if (tokens[0] instanceof TermToken)
				tokens[0] = ((TermToken) tokens[0]).simplify();

			if (!tokens[0].canBeVariable()) {
				throw new InterpreterException(tokens[0].getStringToken()
						+ " is not a variable name.", cli);
			}

			Token variableName = tokens[0];

			Value toSet = tokens[2].createValue();

			if (tokens[1].isTextCommand("=")) {
				if (variableName instanceof TermToken) {
					return new Command.Set(VariableValue.create(variableName),
							toSet, cli);
				}
				return new Command.Set(VariableValue.create(variableName),
						toSet, cli);
			}
			// oi.calculate(order[2].token);
			if (tokens[1].isTextCommand("+=")) {
				return new Command.Add(VariableValue.create(variableName),
						toSet, cli);
			}
			if (tokens[1].isTextCommand("-=")) {
				return new Command.Sub(VariableValue.create(variableName),
						toSet, cli);
			}
			if (tokens[1].isTextCommand("*=")) {
				return new Command.Mult(VariableValue.create(variableName),
						toSet, cli);
			}
			if (tokens[1].isTextCommand("/=")) {
				return new Command.Div(VariableValue.create(variableName),
						toSet, cli);
			}
			if (tokens[1].isTextCommand("^=")) {
				return new Command.Pow(VariableValue.create(variableName),
						toSet, cli);
			}
		}

		if (tokens.length >= 4 && tokens[0].isTextCommand("penColor")) {
			Value r = tokens[1].createValue();
			Value g = tokens[2].createValue();
			Value b = tokens[3].createValue();
			return new Command.PenColor(r, g, b, cli);
		}

		if (tokens.length >= 5 && tokens[0].isTextCommand("line")) {
			Value x1 = tokens[1].createValue();
			Value y1 = tokens[2].createValue();
			Value x2 = tokens[3].createValue();
			Value y2 = tokens[4].createValue();
			return new Command.Line(x1, y1, x2, y2, cli);
		}
		
		if (tokens.length >= 5 && tokens[0].isTextCommand("ellipse")) {
			Value x1 = tokens[1].createValue();
			Value y1 = tokens[2].createValue();
			Value x2 = tokens[3].createValue();
			Value y2 = tokens[4].createValue();
			return new Command.Ellipse(x1, y1, x2, y2, cli);
		}

		if (tokens.length >= 7 && tokens[0].isTextCommand("triangle")) {
			Value x1 = tokens[1].createValue();
			Value y1 = tokens[2].createValue();
			Value x2 = tokens[3].createValue();
			Value y2 = tokens[4].createValue();
			Value x3 = tokens[5].createValue();
			Value y3 = tokens[6].createValue();
			return new Command.Triangle(x1, y1, x2, y2, x3, y3, cli);
		}

		if (tokens.length >= 1 && tokens[0].isTextCommand("polygon")) {
			if ((tokens.length & 0x01) == 0)
				throw new InterpreterException(
						"polygon must have an even amount of arguments.", cli);

			Value[] args = new Value[tokens.length - 1];
			for (int i = 0; i < args.length; i++) {
				args[i] = tokens[i + 1].createValue();
			}
			return new Command.Polygon(args, cli);
		}

		String invokeableName = tokens[0].getStringToken();
		Value[] args = new Value[tokens.length - 1];
		for (int i = 0; i < args.length; i++) {
			args[i] = tokens[i + 1].createValue();
		}
		return new Command.Invoke(invokeableName, args, cli);

		// return this;
	}
}
