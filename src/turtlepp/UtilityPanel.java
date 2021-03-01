package turtlepp;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

public class UtilityPanel extends JPanel {
	private static final long serialVersionUID = -5357812107511254379L;
	private JPanel top;
	private JPanel bottom;
	GridBagLayout layout;

	public UtilityPanel() {
		this(40);
	}
	
	public UtilityPanel(int bottomHeight) {
		layout = new GridBagLayout();
		GridBagConstraints layoutConstraints = new GridBagConstraints();
		setLayout(layout);

		top = new JPanel();
		bottom = new JPanel();

		layoutConstraints.gridx = 0;
		layoutConstraints.gridy = 0;
		layoutConstraints.gridwidth = 1;
		layoutConstraints.gridheight = 1;
		layoutConstraints.fill = GridBagConstraints.BOTH;
		layoutConstraints.insets = new Insets(5, 5, 5, 5);
		layoutConstraints.anchor = GridBagConstraints.CENTER;
		layoutConstraints.weightx = 1.0;
		layoutConstraints.weighty = 1.0;
		layout.setConstraints(top, layoutConstraints);
		this.add(top);

		layoutConstraints.gridx = 0;
		layoutConstraints.gridy = 1;
		layoutConstraints.gridwidth = 1;
		layoutConstraints.gridheight = bottomHeight;
		layoutConstraints.fill = GridBagConstraints.BOTH;
		layoutConstraints.insets = new Insets(0, 5, 5, 5);
		layoutConstraints.anchor = GridBagConstraints.CENTER;
		layoutConstraints.weightx = 1.0;
		layoutConstraints.weighty = 0.0;
		layout.setConstraints(bottom, layoutConstraints);
		this.add(bottom);
	}

	public JPanel getTop() {
		return top;
	}

	public JPanel getBottom() {
		return bottom;
	}
}
