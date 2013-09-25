package cmd4j;

import cmd4j.common.Links;

/**
 * The context in which a {@link ICommand command} executes.
 * 
 * Represents 'a {@link ILink link} of a {@link IChain chain}'.  In other words; a part of a chain that
 * is potentially connected to another link that is executed prior and likewise for one that executes after.
 * 
 * @author wassj
 * 
 * @dto A Link can provide an overriding dto that will be passed to commands executing withing the context of this Link.
 * @concurrency A Link does not exhibit any concurrency behavior by default, but can be decorated to do so. 
 * 
 * @see Links
 */
public interface ILink {

	/**
	 * get the {@link ILink link} to be executed after this 
	 * @return next {@link ILink link} or null
	 */
	ILink next();


	/**
	 * Data Transfer Object that is passed to {@link ICommand commands} within this link.
	 * This acts as an override to the {@link IChain chain} level dto.
	 * @return Object the Data Transfer Object
	 */
	Object dto();


	/**
	 * the {@link ICommand command} in this link.  The actual command type will be inspected 
	 * at execution time.  It is possible that through the use of {@link ICommand3 command3}
	 * that this link could execute more than one command.  So this property should
	 * be thought of to represent the 'head' command for this link.
	 * @return {@link ICommand}
	 */
	ICommand cmd();
}
