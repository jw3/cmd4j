package cmd4j.examples.events;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import cmd4j.IChain;
import cmd4j.ICommand;
import cmd4j.common.Chains;
import cmd4j.common.Chains.ChainBuilder;

/**
 * a simple event dispatcher that deals in {@link ICommand commands}
 *
 * @author wassj
 *
 */
public enum Dispatcher {
	instance;

	private final Collection<IListener<?>> listeners = new HashSet<IListener<?>>();


	/**
	 * register a listener
	 * @param listener
	 */
	public static Dispatcher addListener(final IListener<?> listener) {
		synchronized (instance.listeners) {
			instance.listeners.add(listener);
		}
		return instance;
	}


	public static Dispatcher clearListeners() {
		synchronized (instance.listeners) {
			instance.listeners.clear();
		}
		return instance;
	}


	/**
	 * collect {@link ICommand commands} from all applicable listeners and build a {@link IChain chain} to run 
	 */
	public static Dispatcher fire(final Object event)
		throws Exception {

		final Collection<IListener<? extends Object>> listeners = new ArrayList<IListener<? extends Object>>();
		synchronized (instance.listeners) {
			listeners.addAll(instance.listeners);
		}

		final ChainBuilder builder = Chains.builder();
		for (final IListener listener : listeners) {
			if (dtoIsCastableForCommand(listener, event)) {
				@SuppressWarnings("unchecked")
				// dtoIsCastableForCommand ensures the compatible type here
				final ICommand command = listener.handle(event);
				if (command != null) {
					builder.add(command);
				}
			}
		}
		builder.build().invoke(event);
		return instance;
	}


	/*
	 * 
	 * utils
	 * 
	 */

	static boolean dtoIsCastableForCommand(final IListener listener, final Object dto) {
		if (dto != null) {
			final Class<?> cmdType = typedAs(listener);
			final Class<?> dtoType = dto.getClass();
			return cmdType.isAssignableFrom(dtoType);
		}
		return true;
	}


	static Class<?> typedAs(final IListener t) {
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
