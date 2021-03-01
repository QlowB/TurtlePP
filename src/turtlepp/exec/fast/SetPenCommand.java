package turtlepp.exec.fast;

import turtlepp.LogoRenderer2D;
import turtlepp.exec.CodeLocationInfo;
import turtlepp.exec.OrderInterpreter;

public class SetPenCommand extends Command {
	boolean set;
	public SetPenCommand(boolean b, CodeLocationInfo cli) {
		super(cli);
		set = b;
	}

	@Override
	public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
		renderer.setPen(set);
		return ReturnValue.NOTHING;
	}

}
