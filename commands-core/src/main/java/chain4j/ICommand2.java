package chain4j;

/**
 * @note consider that which invoke version the {@link ILink} calls would be based on the state of two things
 *   1) the dto nullity: if dto is null the noarg {@link ICommand#invoke()} will be called, if non null {@link ICommand2#invoke(Object)} will be called
 *   2) << REVISIT i think this has to do with the dto fitting into the param type of the invoke method >>
 * 
 * @note also considering typing this and using an abstract class which extends {@link ICommand}
 * this would allow for reflective type inspection, but would be a burden for implementors by not having an interface for dao use
 * 
 * @author wassj
 *
 */
public interface ICommand2
	extends ICommand {

	void invoke(Object dto)
		throws Exception;
}
