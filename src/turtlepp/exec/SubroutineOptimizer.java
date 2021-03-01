package turtlepp.exec;

public class SubroutineOptimizer extends OrderOptimizer {

	public SubroutineOptimizer(OrderInterpreter oi) {
		super(oi);
		super.callStack.push(new Subroutine());
	}

}
