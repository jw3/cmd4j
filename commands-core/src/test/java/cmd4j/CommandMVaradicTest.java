package cmd4j;

import cmd4j.ICommand.ICommandM;

/**
 * validate the ability to use a {@link ICommandM} with a varadic parameter
 * 
 * For example a declaration such as 
 * Command2M {
 * 	invoke(String, String...)
 * }
 * Would support string input of 1..N
 * 
 * Where a declaration such as 
 * Command2M {
 * 	invoke(String, String, String...)
 * }
 * Would support string input of 2..N
 * 
 * @author wassj
 *
 */
public class CommandMVaradicTest {

}
