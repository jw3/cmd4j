package command4j.examples.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import chain4j.IChain;
import chain4j.ICommand;
import chain4j.ICommand1;
import chain4j.ICommand3;
import chain4j.builder.ChainBuilder;
import chain4j.common.Chains;
import chain4j.common.IUndo;

import command4j.swing.event.ChainAction;

/**
 *
 *
 * @author wassj
 *
 */
public class ButtonActionsWithUndo {

	private static final Stack<IChain> undoStack = new Stack<IChain>();

	private static final ICommand3 undo = new ICommand3() {
		public ICommand invoke(Object dto) {
			if (!undoStack.isEmpty()) {
				return Chains.makeUndoable(undoStack.pop());
			}
			return null;
		}
	};


	public static void main(String[] args) {
		final JPanel panel = new JPanel();

		final JPanel buttons = new JPanel();
		buttons.add(createColorButton(Color.red, panel));
		buttons.add(createColorButton(Color.green, panel));
		buttons.add(createColorButton(Color.blue, panel));
		buttons.add(new JButton(ChainAction.create(Chains.create(undo)).setValue(ChainAction.NAME, "undo")));

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
					final ICommand change = new CommandUndoDecorator(new ChangeColor(color, target), new ChangeColor(target.getBackground(), target));
					final IChain chain = ChainBuilder.create(change).build();
					chain.invoke();
					undoStack.push(chain);
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
		implements ICommand1 {

		private final Color color;
		private final JComponent target;


		public ChangeColor(final Color color, final JComponent target) {
			this.color = color;
			this.target = target;
			System.out.println("command to " + color);
		}


		public void invoke() {
			System.out.println("set color to " + color);
			target.setBackground(color);
		}
	}


	/**
	 * A decorator that allows a Chain to be undone/redone
	 *
	 * @author wassj
	 *
	 */
	private static class CommandUndoDecorator
		implements IUndo {

		private final ICommand command;
		private final ICommand undo;


		public CommandUndoDecorator(final ICommand command, final ICommand undo) {
			this.command = command;
			this.undo = undo;
		}


		public void invoke()
			throws Exception {

			// REVISIT assuming command1
			((ICommand1)command).invoke();
		}


		public void undo()
			throws Exception {

			if (undo != null) {
				// REVISIT assuming command1
				((ICommand1)undo).invoke();
			}
		}
	}
}
