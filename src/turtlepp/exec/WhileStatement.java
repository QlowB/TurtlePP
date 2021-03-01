package turtlepp.exec;

import turtlepp.InterpreterException;
import turtlepp.LogoRenderer2D;
import turtlepp.exec.OrderInterpreter.InvokeableList;
import turtlepp.exec.Tokenizer.Token;

public class WhileStatement extends IfStatement {

	public WhileStatement(String code, InvokeableList subroutines,
			int lineNumber) {
		super(lineNumber);

		String first = Tokenizer.getLines(code)[0];
		Token[] tokens = Tokenizer.tokenize(first);
		if (tokens.length == 2 && tokens[0].isTextCommand("while")) {
			createComparison(tokens[1]);
		} else {
			throw new InterpreterException(
					"Check your use of the while statement.",
					new CodeLocationInfo(lineNumber));
		}

		String rest = code.substring(first.length());
		addToBlock(rest, subroutines);
	}

	@Override
	public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
		while (checkCondition(renderer, oi)) {
			ReturnValue cb = super.execute(renderer, oi);
			if (cb == ReturnValue.EXIT_WHILE)
				break;
			if (cb != ReturnValue.NOTHING && cb != ReturnValue.NEW_VARIABLE_CREATED)
				return cb;
		}
		return ReturnValue.NOTHING;
	}
}
