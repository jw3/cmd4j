package cmd4j;

/**
 * Because decorators are used unknowingly by the client one or more may be applied to a single object.
 * This stacking of decorators poses different issues depending on the implementations being stacked.
 * 
 * This test should verify that all known decorators stack upon each other and still work.  If that is not
 * possible then the stacking should be prevented for those decorators that do not play well together.
 *
 * @author wassj
 *
 */
public class DecoratorsStackOnEachOtherProperlyTest {

}
