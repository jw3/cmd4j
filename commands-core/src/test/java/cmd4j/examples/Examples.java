package cmd4j.examples;

import java.awt.BorderLayout;

import javax.swing.JFrame;

/**
 *
 *
 * @author wassj
 *
 */
public enum Examples {
	/*enum-singleton*/;

	public static JFrame createFrame() {
		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 500);
		frame.setLayout(new BorderLayout());
		return frame;
	}
}
