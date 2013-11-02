package cmd4j;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand4;
import cmd4j.ICommand.IDtoCommand;
import cmd4j.ICommand.IReturningCommand;

/**
 * validate the logic from {@link Internals.Link#invokeCommand(ICommand, Object, boolean)} related type inspection
 * 
 * @see Internals.Link#dtoIsCastableForCommand(ICommand, Object)
 * @see Internals.Link#typedAs(Object, Class)
 *
 * @author wassj
 *
 */
public class TypeInspectionTest {

	private interface ISuper<O>
		extends IReturningCommand<O> {
	}


	public interface ISub<O>
		extends ISuper<O>, ICommand4<Integer, O> {
	}


	@Test
	public void extensions() {
		final ICommand command = new ISub<String>() {
			public String invoke(Integer dto) {
				return null;
			}
		};

		System.out.println("cmd instnaceof type: " + (command instanceof Type));
		System.out.println("cmd class instnaceof type: " + (command.getClass() instanceof Type));
		if (command.getClass() instanceof Type) {
			GenericDeclaration d = null;

		}

		final Class<?>[] ifaces = command.getClass().getInterfaces();
		for (Class<?> iface : ifaces) {
			System.out.println(iface.getName());
			for (Class<?> iface2 : iface.getInterfaces()) {
				System.out.println(iface2.getName());
				for (Type iface3 : iface2.getGenericInterfaces()) {
					if (iface3 instanceof ParameterizedType) {
						ParameterizedType gd = (ParameterizedType)iface3;
						System.out.println(iface3);
						System.out.println(Arrays.asList(gd.getActualTypeArguments()));

					}
				}
			}
		}

		final Type[] types = getTypeParametersOf(command.getClass(), IDtoCommand.class);
		if (types != null) {
			System.out.println(Arrays.asList(types));
		}
	}


	public static Type[] getTypeParametersOf(final Class<?> from, final Class<?> target) {
		if (!from.isInterface()) {
			final Type superclass = from.getGenericSuperclass();
			if (superclass instanceof ParameterizedType) {
				final ParameterizedType parameterized = (ParameterizedType)superclass;
				if (parameterized.getRawType().equals(target)) {
					return parameterized.getActualTypeArguments();
				}
			}
		}
		for (final Type iface : from.getGenericInterfaces()) {
			if (iface instanceof ParameterizedType) {
				final ParameterizedType parameterized = (ParameterizedType)iface;
				if (parameterized.getRawType().equals(target)) {
					return parameterized.getActualTypeArguments();
				}
			}
		}

		for (final Class<?> iface : from.getInterfaces()) {
			final Type[] params = getTypeParametersOf(target, iface);
			if (params != null) {
				return params;
			}
		}
		final Class<?> superclass = from.getSuperclass();
		if (superclass != null && !Object.class.equals(superclass)) {
			return getTypeParametersOf(target, superclass);
		}
		return null;
	}
}
