package turtlepp.exec.fast;

import turtlepp.LogoRenderer2D;
import turtlepp.exec.CodeLocationInfo;
import turtlepp.exec.OrderInterpreter;

public class SetTurtleCommand extends Command {
	boolean set;

	public SetTurtleCommand(boolean b, CodeLocationInfo cli) {
		super(cli);
		set = b;
	}

	@Override
	public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
		renderer.setShowTurtle(set);
		return ReturnValue.NOTHING;
	}
}
