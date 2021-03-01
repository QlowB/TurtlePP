package turtlepp.exec.fast;

import turtlepp.LogoRenderer2D;
import turtlepp.exec.CodeLocationInfo;
import turtlepp.exec.OrderInterpreter;

public class SetAntialiasingCommand extends Command {
	boolean set;
	
	public SetAntialiasingCommand(boolean b, CodeLocationInfo cli) {
		super(cli);
		set = b;
	}

	@Override
	public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
		renderer.setAntialiasing(set);
		return ReturnValue.NOTHING;
	}
}
