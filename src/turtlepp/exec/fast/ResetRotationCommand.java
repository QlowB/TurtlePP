package turtlepp.exec.fast;

import turtlepp.LogoRenderer2D;
import turtlepp.exec.CodeLocationInfo;
import turtlepp.exec.OrderInterpreter;

public class ResetRotationCommand extends Command {
	public ResetRotationCommand(CodeLocationInfo cli) {
		super(cli);
	}
	@Override
	public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
		renderer.resetRotation();
		return ReturnValue.NOTHING;
	}
}
