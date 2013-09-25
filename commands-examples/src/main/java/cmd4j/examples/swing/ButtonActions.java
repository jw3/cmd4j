package cmd4j.examples.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import cmd4j.ICommand;
import cmd4j.common.Chains;

/**
 * A basic example to show how to tie commands into a JButton action listener
 *
 * @author wassj
 *
 */
public class ButtonActions {

	public static void main(String[] args) {
		final JPanel panel = new JPanel();

		final JPanel buttons = new JPanel();
		buttons.add(createColorButton(Color.red, panel));
		buttons.add(createColorButton(Color.green, panel));
		buttons.add(createColorButton(Color.blue, panel));

		final JFrame frame = Examples.createFrame();
		frame.add(panel, BorderLayout.CENTER);
		frame.add(buttons, BorderLayout.SOUTH);

		frame.setVisible(true);
	}


	public static JButton createColorButton(final Color color, final JComponent target) {
		final JButton button = new JButton(color.getRed() + "," + color.getGreen() + "," + color.getBlue());
		button.setBackground(color);
		button.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				try {
					Chains.builder().add(new ChangeColor(color, target)).build().invoke();
				}
				catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		return button;
	}


	public static JFrame createFrame() {
		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 500);
		frame.setLayout(new BorderLayout());
		return frame;
	}


	public static class ChangeColor
		implements ICommand {

		private final Color color;
		private final JComponent target;


		public ChangeColor(final Color color, final JComponent target) {
			this.color = color;
			this.target = target;
		}


		public void invoke() {
			target.setBackground(color);
		}
	}
}
