package turtlepp.exec.fast;

import turtlepp.LogoRenderer2D;
import turtlepp.datatypes.Value;
import turtlepp.datatypes.Variable;
import turtlepp.exec.CodeLocationInfo;
import turtlepp.exec.OrderInterpreter;

public class ClearCommand extends Command {
	private Value r;
	private Value g;
	private Value b;

	/**
	 * if any of the parameters is <code>null</code>, the color value is set to
	 * black.
	 */
	public ClearCommand(Value r, Value g, Value b, CodeLocationInfo cli) {
		super(cli);
		this.r = r;
		this.g = g;
		this.b = b;
	}

	@Override
	public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
		java.awt.Color backgroundColor = java.awt.Color.black;
		if (r != null && g != null && b != null) {
			Variable rVal = r.getVarValue(renderer, oi, cli);
			Variable gVal = g.getVarValue(renderer, oi, cli);
			Variable bVal = b.getVarValue(renderer, oi, cli);
			int rc = (int) rVal.getDoubleValue();
			int gc = (int) gVal.getDoubleValue();
			int bc = (int) bVal.getDoubleValue();
			backgroundColor = new java.awt.Color(rc, gc, bc);
		}
		renderer.clear(backgroundColor);
		return ReturnValue.NOTHING;
	}
}
