package cmd4j.builder;
/*package chain4j;

import org.junit.Assert;
import org.testng.annotations.Test;

import chain4j.builder.ChainBuilder;
import chain4j.test.Say;
import chain4j.test.Service;

*//**
*
*
* @author wassj
*
*/
/*
public class TestChainBuilder {

*//**
* ensure that empty chains are always identified as unthreaded
*/
/*
@Test
public void testEmptyChainsAreUnthreaded() {
final IChain c1 = ChainBuilder.empty();
Assert.assertTrue(c1.isUnthreaded());

final IChain c2 = ChainBuilder.create().unthreaded(true).build();
Assert.assertTrue(c2.isUnthreaded());

// setting is ignored if chain is empty
final IChain c3 = ChainBuilder.create().unthreaded(false).build();
Assert.assertTrue(c3.isUnthreaded());
}


*//**
* test that an unthreaded flag is properly set across building scenarios
*/
/*
@Test
public void testNonEmptyChainsRespectFlag() {
for (boolean flag : new boolean[] {true, false}) {
	final IChain c1 = ChainBuilder.create(Say.nothing()).unthreaded(flag).build();
	Assert.assertTrue(c1.isUnthreaded() == flag);

	final IChain c2 = ChainBuilder.create(Say.nothing()).executor(Service.a.get()).unthreaded(flag).build();
	Assert.assertTrue(c2.isUnthreaded() == flag);
}
}


*//**
* ensure that the call to set the executor on the unthreaded chain fails
*/
/*
@Test(expectedExceptions = IllegalStateException.class)
public void testFailToSetExecutorOnUnthreadedChain() {
final IChain c3 = ChainBuilder.create(Say.nothing()).unthreaded(true).build().executor(Service.a.get());
Assert.assertTrue(c3.isUnthreaded());
}
}
*/
