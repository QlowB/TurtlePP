package turtlepp.exec;

import turtlepp.InterpreterException;
import turtlepp.LogoRenderer2D;
import turtlepp.datatypes.Bool;
import turtlepp.datatypes.Value;
import turtlepp.datatypes.Variable;
import turtlepp.datatypes.Value.*;
import turtlepp.exec.OrderInterpreter.InvokeableList;
import turtlepp.exec.Tokenizer.Token;

public class IfStatement extends Block {
	Value condition;
	Block elseBlock;

	protected IfStatement(int lineIndex) {
		super(lineIndex);
	}

	public IfStatement(String code, InvokeableList subroutines, int lineIndex) {
		this(lineIndex);
		String first = Tokenizer.getLines(code)[0];
		Token[] tokens = Tokenizer.tokenize(first);
		if (tokens.length == 2 && tokens[0].isTextCommand("if")) {
			createComparison(tokens[1]);
		} else {
			throw new InterpreterException(
					"Check your use of the if statement.",
					new CodeLocationInfo(lineIndex));
		}

		elseBlock = null;

		String rest = code.substring(first.length());
		addToBlock(rest, subroutines);
	}

	protected void createComparison(Token compToken) {
		Value v = compToken.createValue();
		condition = v;
	}

	protected boolean checkCondition(LogoRenderer2D renderer,
			OrderInterpreter oi) {

		if (condition instanceof ComparisonValue) {
			return ((ComparisonValue) condition).getComparison().compare(
					renderer, oi, new CodeLocationInfo(lineOffset));
		}

		Variable a = condition.getVarValue(renderer, oi, new CodeLocationInfo(
				lineOffset));
		if (a instanceof Bool)
			return ((Bool) a).value;
		else
			return ((Bool) a.cast(Bool.class)).value;
	}

	@Override
	protected void foundElse(int line, String[] remainingCode) {
		String code = "";
		for (int i = 1; i < remainingCode.length; i++) {
			code += remainingCode[i] + "\n";
		}
		String[] elseStatement = remainingCode[0].split("\\s+");
		if (elseStatement.length == 1) {
			elseBlock = new Block(code, null, lineOffset + line);
		} else if (elseStatement.length > 2) {
			boolean cond = elseStatement[0].equals("else")
					&& elseStatement[1].equals("if");

			String ifLine = "";
			for (int i = 1; i < elseStatement.length; i++) {
				ifLine += elseStatement[i] + " ";
			}

			if (cond) {
				elseBlock = new IfStatement(ifLine + "\n" + code, null,
						lineOffset + line);
			}
		}
	}

	@Override
	public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
		boolean cond = checkCondition(renderer, oi);
		if (cond) {
			ReturnValue cb = super.execute(renderer, oi);
			if (cb != ReturnValue.NOTHING
					&& cb != ReturnValue.NEW_VARIABLE_CREATED)
				return cb;
		} else if (elseBlock != null) {
			elseBlock.execute(renderer, oi);
		}
		return ReturnValue.NOTHING;
	}
}
