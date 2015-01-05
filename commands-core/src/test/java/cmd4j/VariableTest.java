package cmd4j;

import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.Commands.Variable;

/**
 *
 * @author wassj
 *
 */
public class VariableTest {

	@Test
	public void isNull() {
		final Variable<Object> var = new Variable<Object>();
		Assert.assertTrue(var.isNull());
		var.set(1);
		Assert.assertFalse(var.isNull());
	}


	@Test
	public void toStringTest() {
		final Variable<Object> var = new Variable<Object>();
		Assert.assertEquals("null", var.toString());

		{
			final String expected = UUID.randomUUID().toString();
			var.set(expected);
			Assert.assertEquals(expected, var.toString());
		}

		{
			final String expected = String.valueOf(100);
			var.set(100);
			Assert.assertEquals(expected, var.toString());
		}
	}


	@Test
	public void hashTest() {
		{
			final Variable<Object> var = new Variable<Object>();
			final int hash = var.hashCode();
			var.set(100);
			Assert.assertEquals(hash, var.hashCode());
			var.set(null);
			Assert.assertEquals(hash, var.hashCode());
		}
		{
			final Variable<Object> var = new Variable<Object>(100);
			final int hash = var.hashCode();
			var.set(null);
			Assert.assertEquals(hash, var.hashCode());
			var.set(1);
			Assert.assertEquals(hash, var.hashCode());
		}
	}


	@Test
	public void equalsTest() {
		final Variable<Object> var1 = new Variable<Object>();
		Assert.assertTrue(var1.equals(var1));

		final Variable<Object> var2 = new Variable<Object>();
		Assert.assertTrue(var1.equals(var2));

		var1.set("foo");
		Assert.assertFalse(var1.equals(var2));

		var2.set("foo");
		Assert.assertTrue(var1.equals(var2));

		Assert.assertFalse(var1.equals("foo"));
	}
}
