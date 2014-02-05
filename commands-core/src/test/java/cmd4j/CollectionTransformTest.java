package cmd4j;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.collections.Sets;

import cmd4j.ICommand.IFunction;

import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * validate the Collection transform functions
 * @author wassj
 *
 */
public class CollectionTransformTest {
	private final Collection<String> input = Splitter.on("-").splitToList(UUID.randomUUID().toString());


	@Test
	public void collection()
		throws Exception {

		final Collection<String> expected = Collections2.transform(input, Functions2.function(toUpper));
		final Collection<String> actual = Functions2.collection(toUpper).invoke(input);
		Assert.assertEquals(actual, expected);

		for (final String in : input) {
			Assert.assertTrue(actual.contains(in.toUpperCase()));
		}
	}


	@Test
	public void map()
		throws Exception {

		final Map<String, String> expected = Maps.asMap(Sets.newHashSet(input), Functions2.function(toUpper));
		final Map<String, String> actual = Functions2.map(toUpper).invoke(input);
		Assert.assertEquals(actual, expected);

		for (final String in : input) {
			Assert.assertTrue(actual.containsKey(in));
			Assert.assertEquals(actual.get(in), in.toUpperCase());
		}
	}


	@Test
	public void multi()
		throws Exception {

		final Map<String, Collection<String>> expected = Maps.asMap(Sets.newHashSet(input), Functions2.function(csvTokens));
		final Multimap<String, String> actual = Functions2.multimap(csvTokens).invoke(input);
		for (final String key : actual.keys()) {
			Assert.assertEquals(Sets.newHashSet(actual.get(key)), Sets.newHashSet(expected.get(key)));
		}
	}

	private static final IFunction<String, String> toUpper = new IFunction<String, String>() {
		public String invoke(final String input) {
			return input.toUpperCase();
		}
	};

	private static final IFunction<String, Collection<String>> csvTokens = new IFunction<String, Collection<String>>() {
		public Collection<String> invoke(final String input) {
			return Splitter.fixedLength(3).splitToList(input);
		}
	};
}
