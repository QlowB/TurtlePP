package turtlepp.exec.fast;

import turtlepp.LogoRenderer2D;
import turtlepp.exec.CodeLocationInfo;
import turtlepp.exec.OrderInterpreter;

public class PopPositionCommand extends Command {
	
	public PopPositionCommand(CodeLocationInfo cli) {
		super(cli);
	}
	
	@Override
	public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
		renderer.popPosition();
		return ReturnValue.NOTHING;
	}
}
