package chain4j.common;

import chain4j.ICommand;

/**
 *
 *
 * @author wassj
 *
 */
public interface ICommandUndo
	extends ICommand {

	void undo()
		throws Exception;
}
