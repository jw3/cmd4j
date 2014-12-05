package cmd4j;

import mockit.Mocked;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.ICommand2;
import cmd4j.ICommand.ICommand3;
import cmd4j.ICommand.ICommand4;
import cmd4j.ICommand.IInputCommand;
import cmd4j.ICommand.IReturningCommand;

import com.google.common.base.Optional;

/**
 * validate the behavior of the Internals.Command.acceptedInput function
 * @author wassj
 *
 */
public class CommandsAcceptedInputTest {

	@Test
	public void inputCommand(@Mocked final IInputCommand<Integer> input) {
		final Optional<Class<?>> result = Internals.Command.acceptedInput(input);
		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(result.get(), Integer.class);
	}


	@Test
	public void returningCommand(@Mocked final IReturningCommand<Integer> input) {
		final Optional<Class<?>> result = Internals.Command.acceptedInput(input);
		Assert.assertFalse(result.isPresent());
	}


	@Test
	public void command1(@Mocked final ICommand1 input) {
		final Optional<Class<?>> result = Internals.Command.acceptedInput(input);
		Assert.assertFalse(result.isPresent());
	}


	@Test
	public void command2(@Mocked final ICommand2<Integer> input) {
		final Optional<Class<?>> result = Internals.Command.acceptedInput(input);
		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(result.get(), Integer.class);
	}


	@Test
	public void command3(@Mocked final ICommand3<String> input) {
		final Optional<Class<?>> result = Internals.Command.acceptedInput(input);
		Assert.assertFalse(result.isPresent());
	}


	@Test
	public void command4(@Mocked final ICommand4<Integer, String> input) {
		final Optional<Class<?>> result = Internals.Command.acceptedInput(input);
		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(result.get(), Integer.class);
	}


	@Test
	public void directImplementor(@Mocked final DirectImplementor<Number, String, Integer, Double, Byte> input) {
		final Optional<Class<?>> result = Internals.Command.acceptedInput(input);
		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(result.get(), Integer.class);
	}


	@Test
	public void indirectImplementor(@Mocked final IndirectImplementor<Number, String, Integer, Double, Byte> input) {
		final Optional<Class<?>> result = Internals.Command.acceptedInput(input);
		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(result.get(), Integer.class);
	}


	@Test
	public void directImplementorChild(@Mocked final ChildOfDirectImplementor<Number, String, Integer> input) {
		final Optional<Class<?>> result = Internals.Command.acceptedInput(input);
		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(result.get(), Integer.class);
	}


	@Test
	public void indirectImplementorChild(@Mocked final ChildOfIndirectImplementor<Number, String, Integer> input) {
		final Optional<Class<?>> result = Internals.Command.acceptedInput(input);
		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(result.get(), Integer.class);
	}


	@Test
	public void directImplementorGrandchild(@Mocked final GrandchildOfDirectImplementor<String, Integer> input) {
		final Optional<Class<?>> result = Internals.Command.acceptedInput(input);
		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(result.get(), Integer.class);
	}


	@Test
	public void indirectImplementorGrandchild(@Mocked final GrandchildOfIndirectImplementor<String, Integer> input) {
		final Optional<Class<?>> result = Internals.Command.acceptedInput(input);
		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(result.get(), Integer.class);
	}


	@Test
	public void directImplementorConcrete(@Mocked final ConcreteDirectImplementor<Number, String, Integer, Double, Byte> input) {
		final Optional<Class<?>> result = Internals.Command.acceptedInput(input);
		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(result.get(), Object.class);
	}


	@Test
	public void indirectImplementorConcrete(@Mocked final ConcreteIndirectImplementor<Number, String, Integer, Double, Byte> input) {
		final Optional<Class<?>> result = Internals.Command.acceptedInput(input);
		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(result.get(), Object.class);
	}


	@Test
	public void directImplementorChildConcrete(@Mocked final ConcreteChildOfDirectImplementor<Number, String, Integer> input) {
		final Optional<Class<?>> result = Internals.Command.acceptedInput(input);
		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(result.get(), Object.class);
	}


	@Test
	public void indirectImplementorChildConcrete(@Mocked final ConcreteChildOfIndirectImplementor<Number, String, Integer> input) {
		final Optional<Class<?>> result = Internals.Command.acceptedInput(input);
		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(result.get(), Object.class);
	}


	@Test
	public void directImplementorGrandchildConcrete(@Mocked final ConcreteGrandchildOfDirectImplementor<String, Integer> input) {
		final Optional<Class<?>> result = Internals.Command.acceptedInput(input);
		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(result.get(), Object.class);
	}


	@Test
	public void indirectImplementorGrandchildConcrete(@Mocked final ConcreteGrandchildOfIndirectImplementor<String, Integer> input) {
		final Optional<Class<?>> result = Internals.Command.acceptedInput(input);
		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(result.get(), Object.class);
	}


	/*
	 * 
	 * 
	 * additional test classes hierarchy
	 * 
	 * 
	 */

	abstract public static class DirectImplementor<A, E, I, O, U>
		implements IInputCommand<I> {
	}


	abstract public static class ChildOfDirectImplementor<A, E, I>
		extends DirectImplementor<A, E, I, String, Integer> {
	}


	abstract public static class GrandchildOfDirectImplementor<E, I>
		extends ChildOfDirectImplementor<String, E, I> {
	}


	abstract public static class IndirectImplementor<A, E, I, O, U>
		implements ICommand2<I> {
	}


	abstract public static class ChildOfIndirectImplementor<A, E, I>
		extends IndirectImplementor<A, E, I, String, Integer> {
	}


	abstract public static class GrandchildOfIndirectImplementor<E, I>
		extends ChildOfIndirectImplementor<String, E, I> {
	}


	public static class ConcreteDirectImplementor<A, E, I, O, U>
		implements IInputCommand<I> {
	}


	public static class ConcreteChildOfDirectImplementor<A, E, I>
		extends DirectImplementor<A, E, I, String, Integer> {
	}


	public static class ConcreteGrandchildOfDirectImplementor<E, I>
		extends ChildOfDirectImplementor<String, E, I> {
	}


	public static class ConcreteIndirectImplementor<A, E, I, O, U>
		implements ICommand2<I> {
		public void invoke(final I input) {
		}
	}


	public static class ConcreteChildOfIndirectImplementor<A, E, I>
		extends IndirectImplementor<A, E, I, String, Integer> {
		public void invoke(final I input) {
		}
	}


	public static class ConcreteGrandchildOfIndirectImplementor<E, I>
		extends ChildOfIndirectImplementor<String, E, I> {
		public void invoke(final I input) {
		}
	}
}
