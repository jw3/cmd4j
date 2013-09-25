package cmd4j.examples.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Stack;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import cmd4j.IChain;
import cmd4j.ICommand;
import cmd4j.ICommand.IUndo;
import cmd4j.ICommand1;
import cmd4j.ICommand3;
import cmd4j.common.Chains;
import cmd4j.swing.event.ChainAction;

/**
 * example of a small ui with some actions tied into undoable commands
 *
 * @author wassj
 *
 */
public class ButtonActionsWithUndo {
	private static final Stack<IChain> undoStack = new Stack<IChain>();


	/*
	 * 
	 */
	public static void main(String[] args) {
		final JPanel panel = new JPanel();

		final JPanel buttons = new JPanel();
		buttons.add(createColorButton(Color.red, panel));
		buttons.add(createColorButton(Color.green, panel));
		buttons.add(createColorButton(Color.blue, panel));
		buttons.add(new JButton(new UndoAction().setValue(ChainAction.NAME, "undo")));

		final JFrame frame = Examples.createFrame();
		frame.add(panel, BorderLayout.CENTER);
		frame.add(buttons, BorderLayout.SOUTH);

		frame.setVisible(true);
	}


	public static JButton createColorButton(final Color color, final JComponent target) {
		final JButton button = new JButton(new SetColorAction(color, target).setValue(Action.NAME, colorString(color)));
		button.setBackground(color);

		return button;
	}


	/**
	 * An example of an undoable command
	 *
	 * @author wassj
	 *
	 */
	private static class UndoableChangeColor
		implements ICommand1, IUndo {

		private final Color color;
		private final Color original;
		private final JComponent target;


		public UndoableChangeColor(final Color color, final JComponent target) {
			this.color = color;
			this.original = target.getBackground();
			this.target = target;
		}


		public void invoke() {
			this.setColor(color);
		}


		public void undo() {
			this.setColor(original);
		}


		private void setColor(final Color color) {
			target.setBackground(color);
		}
	}


	/**
	 * example implementation of a {@link ChainAction} which sets the component color using an {@link IUndo} {@link ICommand}
	 *
	 * @author wassj
	 *
	 */
	private static final class SetColorAction
		extends ChainAction {

		private final Color color;
		private final JComponent target;


		public SetColorAction(final Color color, final JComponent target) {
			this.color = color;
			this.target = target;
		}


		public IChain getChain() {
			return Chains.create(new ICommand1() {
				public void invoke()
					throws Exception {

					final ICommand change = new UndoableChangeColor(color, target);

					final IChain chain = Chains.builder().add(change).build();
					chain.invoke(); // we can block here!

					undoStack.push(chain);
				}
			});
		}
	}


	/**
	 * {@link ChainAction} that pops and invokes {@link IChain}s from the undo stack
	 *
	 * @author wassj
	 *
	 */
	private static final class UndoAction
		extends ChainAction {

		public IChain getChain() {
			return Chains.create(new ICommand3() {
				public ICommand invoke(Object dto) {
					if (!undoStack.isEmpty()) {
						return Chains.makeUndoable(undoStack.pop());
					}
					return null;
				}
			});
		}
	}


	private static String colorString(final Color color) {
		return color.getRed() + "," + color.getGreen() + "," + color.getBlue();
	}
}
