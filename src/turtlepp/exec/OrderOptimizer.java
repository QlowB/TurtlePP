package turtlepp.exec;

import java.util.ArrayList;
import java.util.Stack;

public class OrderOptimizer {
	public enum StackType {
		GLOBAL, LOCAL
	}

	private class State {
		public int globalVariablesSize;
		public int localVariablesSize;
		public int callStackSize;

		public State(int globalVariablesSize, int localVariablesSize,
				int callStackSize) {
			this.globalVariablesSize = globalVariablesSize;
			this.localVariablesSize = localVariablesSize;
			this.callStackSize = callStackSize;
		}
	}

	protected Stack<String> globalVariables;
	protected Stack<String> localVariables;
	protected ArrayList<Invokeable> invokeables;
	protected Stack<Invokeable> callStack;
	protected Stack<State> stateStack;

	public OrderOptimizer(OrderInterpreter oi) {
		globalVariables = new Stack<String>();
		localVariables = new Stack<String>();
		invokeables = oi.getInvokeables();
		callStack = new Stack<Invokeable>();
		stateStack = new Stack<State>();
	}

	protected void pushGlobalVariable(String name) {
		globalVariables.push(name);
	}

	protected int getGlobalIndex(String name) {
		for (int i = globalVariables.size() - 1; i >= 0; i--) {
			String var = globalVariables.get(i);
			if (var.equals(name)) {
				return globalVariables.size() - i;
			}
		}
		return -1;
	}

	protected void pushLocalVariable(String name) {
		localVariables.push(name);
	}

	protected int getLocalIndex(String name) {
		for (int i = localVariables.size() - 1; i >= 0; i--) {
			String var = localVariables.get(i);
			if (var.equals(name)) {
				return localVariables.size() - i;
			}
		}
		return -1;
	}

	public void pushVariable(String string) {
		if (!callStack.isEmpty()) {
			pushLocalVariable(string);
		} else {
			pushGlobalVariable(string);
		}
	}

	/**
	 * searches for a specific variable and determines in which stack it is
	 * found
	 * 
	 * @param name
	 * @return
	 */
	public StackType getStackType(String name) {
		name = name.toLowerCase();
		int index = getLocalIndex(name);
		if (index != -1)
			return StackType.LOCAL;

		index = getGlobalIndex(name);
		if (index != -1)
			return StackType.GLOBAL;

		return null;
	}

	public int getStackIndex(String name) {
		name = name.toLowerCase();
		int index = getLocalIndex(name);
		if (index != -1)
			return index;

		index = getGlobalIndex(name);
		if (index != -1)
			return index;

		return -1;
	}

	public void pushState() {
		stateStack.push(new State(globalVariables.size(),
				localVariables.size(), callStack.size()));
	}

	public void popState() {
		State lastState = stateStack.pop();
		globalVariables.setSize(lastState.globalVariablesSize);
		localVariables.setSize(lastState.localVariablesSize);
		callStack.setSize(lastState.callStackSize);
	}

	public Invokeable getSubroutine(String invokeableName, int length) {
		for (int i = 0; i < invokeables.size(); i++) {
			Invokeable inv = invokeables.get(i);
			if (inv.getName().equals(invokeableName) && length == inv.getNArguments())
				return inv;
		}
		return null;
	}
}
