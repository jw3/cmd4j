package chain4j.common;

import chain4j.ICommand1;

/**
 *
 *
 * @author wassj
 *
 */
public interface IUndo
	extends ICommand1 {

	void undo()
		throws Exception;
}
