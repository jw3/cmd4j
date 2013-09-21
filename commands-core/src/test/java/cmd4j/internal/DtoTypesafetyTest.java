package cmd4j.internal;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.junit.Assert;
import org.testng.annotations.Test;

import cmd4j.IChain;
import cmd4j.ICommand;
import cmd4j.ICommand2;
import cmd4j.common.ChainBuilder;
import cmd4j.common.Chains;

/**
 * Ensure that a DTO does not end up being passed to a {@link ICommand} that will not accept it.
 *
 * @author wassj
 *
 */
public class DtoTypesafetyTest {

	/**
	 * these tests all should pass as they match up to the command they are passed to
	 * @throws Exception
	 */
	@Test
	public void testCorrectTypes()
		throws Exception {

		{
			final BaseInvoked invoked = new Untyped();
			ChainBuilder.create(invoked).build().invoke(new String());
			Assert.assertTrue(invoked.wasInvoked());
		}
		{
			final BaseInvoked invoked = new TypedNumber();
			ChainBuilder.create(invoked).build().invoke(1.1);
			Assert.assertTrue(invoked.wasInvoked());
		}
		{
			final BaseInvoked invoked = new TypedInteger();
			ChainBuilder.create(invoked).build().invoke(1);
			Assert.assertTrue(invoked.wasInvoked());
		}
	}


	@Test
	public void testCorrectTypesInStrictMode()
		throws Exception {

		{
			final BaseInvoked invoked = new Untyped();
			ChainBuilder.create(invoked).build().invoke(new String());
			Assert.assertTrue(invoked.wasInvoked());
		}
		{
			final BaseInvoked invoked = new TypedNumber();
			ChainBuilder.create(invoked).build().invoke(1.1);
			Assert.assertTrue(invoked.wasInvoked());
		}
		{
			final BaseInvoked invoked = new TypedInteger();
			ChainBuilder.create(invoked).build().invoke(1);
			Assert.assertTrue(invoked.wasInvoked());
		}
	}


	// REVISIT can narrow this exception after some work on the exception machinery
	///@Test(expectedExceptions = IllegalArgumentException.class)
	@Test(expectedExceptions = Exception.class)
	public void testIncorrectTypes1()
		throws Exception {

		final BaseInvoked invoked = new Untyped();
		final BaseInvoked invoked2 = new TypedString();
		final BaseInvoked uninvoked = new TypedNumber();

		ChainBuilder.create().add(uninvoked).add(invoked).add(invoked2).build().invoke("not a number");

		Assert.assertFalse(uninvoked.wasInvoked());
		Assert.assertTrue(invoked.wasInvoked());
		Assert.assertTrue(invoked2.wasInvoked());
	}


	// REVISIT can narrow this exception after some work on the exception machinery
	///@Test(expectedExceptions = IllegalArgumentException.class)
	@Test(expectedExceptions = Exception.class)
	public void testIncorrectTypes2()
		throws Exception {

		final BaseInvoked invoked = new Untyped();
		final BaseInvoked invoked2 = new TypedNumber();
		final BaseInvoked uninvoked = new TypedInteger();

		ChainBuilder.create().add(uninvoked).add(invoked).add(invoked2).build().invoke(1.1);

		Assert.assertFalse(uninvoked.wasInvoked());
		Assert.assertTrue(invoked.wasInvoked());
		Assert.assertTrue(invoked2.wasInvoked());
	}


	/**
	 * visitable decorator allows the incompatible commands to be skipped
	 */
	@Test
	public void testIncorrectTypes1_visitable()
		throws Exception {

		final BaseInvoked invoked = new Untyped();
		final BaseInvoked invoked2 = new TypedString();
		final BaseInvoked uninvoked = new TypedNumber();

		final IChain chain = ChainBuilder.create().add(uninvoked).add(invoked).add(invoked2).build();
		Chains.makeVisitable(chain).invoke("not a number");

		Assert.assertFalse(uninvoked.wasInvoked());
		Assert.assertTrue(invoked.wasInvoked());
		Assert.assertTrue(invoked2.wasInvoked());
	}


	/**
	 * visitable decorator allows the incompatible commands to be skipped
	 */
	@Test
	public void testIncorrectTypes2_visitable()
		throws Exception {

		final BaseInvoked invoked = new Untyped();
		final BaseInvoked invoked2 = new TypedNumber();
		final BaseInvoked uninvoked = new TypedInteger();

		final IChain chain = ChainBuilder.create().add(uninvoked).add(invoked).add(invoked2).build();
		Chains.makeVisitable(chain).invoke(1.1);

		Assert.assertFalse(uninvoked.wasInvoked());
		Assert.assertTrue(invoked.wasInvoked());
		Assert.assertTrue(invoked2.wasInvoked());
	}


	private static class Untyped
		extends BaseInvoked
		implements ICommand2 {

		public void invoke(Object dto) {
			invoked = true;
		}
	}


	private static class TypedString
		extends BaseInvoked
		implements ICommand2<String> {

		public void invoke(String dto) {
			invoked = true;
		}
	}


	private static class TypedNumber
		extends BaseInvoked
		implements ICommand2<Number> {

		public void invoke(Number dto) {
			invoked = true;
		}
	}


	private static class TypedInteger
		extends BaseInvoked
		implements ICommand2<Integer> {

		public void invoke(Integer dto) {
			invoked = true;
		}
	}


	private abstract static class BaseInvoked
		implements ICommand {

		protected boolean invoked;


		public boolean wasInvoked() {
			return invoked;
		}
	}


	public static Class<?> typedAs(ICommand t) {
		for (Type type : t.getClass().getGenericInterfaces()) {
			if (type instanceof ParameterizedType) {
				final Type paramType = ((ParameterizedType)type).getActualTypeArguments()[0];
				if (paramType instanceof Class<?>) {
					return (Class<?>)paramType;
				}
			}
		}
		return Object.class;
	}
}
