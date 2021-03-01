package turtlepp.exec.fast;

import turtlepp.LogoRenderer2D;
import turtlepp.exec.CodeLocationInfo;
import turtlepp.exec.OrderInterpreter;

public class PushPositionCommand extends Command {
	public PushPositionCommand(CodeLocationInfo cli) {
		super(cli);
	}
	@Override
	public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
		renderer.pushPosition();
		return ReturnValue.NOTHING;
	}

}
