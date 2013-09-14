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
import chain4j.ILink;
import chain4j.builder.ChainBuilder;
import chain4j.builder.Links;
import chain4j.fsm.State;
import chain4j.fsm.StateMachine;

import command4j.swing.event.ChainAction;

/**
 *
 *
 * @author wassj
 *
 */
public class ButtonActionsWithUndo {

	private static final Stack<IChain> undo = new Stack<IChain>();


	public static void main(String[] args) {
		final JPanel panel = new JPanel();

		final JPanel buttons = new JPanel();
		buttons.add(createColorButton(Color.red, panel));
		buttons.add(createColorButton(Color.green, panel));
		buttons.add(createColorButton(Color.blue, panel));
		buttons.add(new JButton(ChainAction.create(new UndoChain()).setValue(ChainAction.NAME, "undo")));

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
					final IChain chain = ChainBuilder.create(new ChangeColor(color, target)).build();
					chain.invoke();
					undo.push(chain);
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


	/*
	 * simple implementation of a state machine that pops and executes a chain from the undo stack
	 */
	private static class UndoChain
		extends StateMachine {

		public UndoChain() {
			setStart(start);
		}

		private final ILink start = new State() {
			public ILink run(Object dto) {
				if (!undo.isEmpty()) {
					return Links.create(undo.pop());
				}
				return null;
			};
		};
	}
}
