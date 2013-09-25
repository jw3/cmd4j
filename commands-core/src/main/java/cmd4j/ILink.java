package cmd4j;

import cmd4j.common.Links;

/**
 * The context in which an {@link ICommand} executes.
 * 
 * Represents a 'link in a chain', in other words a part of an {@link IChain} that
 * is potentially connected to a link that executed prior and one that executes after.
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
	 * the {@link ILink} to be executed after this 
	 * @return {@link ILink} or null
	 */
	ILink next();


	/**
	 * Data Transfer Object that is passed to {@link ICommand}s within this link.
	 * This acts as an override to the {@link IChain} level dto.
	 * @return Object the Data Transfer Object
	 */
	Object dto();


	/**
	 * the {@link ICommand} in this link.  The actual Command type will be determined 
	 * at execution time.  It is possible that through the use of {@link ICommand3}
	 * that this link could execute more than one Command.  So this property could
	 * be thought of to represent the 'head' command for this link.
	 * @return {@link ICommand}
	 */
	ICommand cmd();
}
