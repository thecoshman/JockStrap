package com.thecoshman.util.function;

public interface Function<T, R> {
//	default<V> Function<T, V> andThen(Function<? super R, ? extends V> after);
	R apply(T t);
//	default<V> Function<V, R> compose(Function<? super V, ? extends T> before);
//	abstract static <T> Function<T, T> identity();
}
