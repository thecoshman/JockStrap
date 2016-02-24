package com.thecoshman.util;

import com.thecoshman.util.function.Consumer;
import com.thecoshman.util.function.Function;

public class Optional<T> {
	private T value;

	private Optional() {
		this.value = null;
	}

	private Optional(T v) {
		this.value = v;
	}

	public static <T> Optional<T> of(T value) {
		if (value == null) {
			throw new NullPointerException();
		}
		return new Optional<T>(value);
	}

	public static <T> Optional<T> ofNullable(T value) {
		if (value == null) {
			return Optional.empty();
		}
		return Optional.of(value);
	}

	public static <T> Optional<T> empty() {
		return new Optional<T>();
	}

	@Override
	public int hashCode() {
		if (value == null) {
			return 0;
		}
		return value.hashCode();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Optional)) {
			return false;
		}
		Optional other = (Optional) obj;
		if (isPresent() != other.isPresent()){
			return false;
		}
		return !isPresent() || value.equals(other.get());
	}

	@Override
	public String toString() {
		if (isPresent()) {
			return "Optional<" + value.getClass().getSimpleName()
					+ "> with value";
		}
		return "empty Optional<" + value.getClass().getSimpleName() + ">";
	}

	public boolean isPresent() {
		return value != null;
	}

	public T get() {
		if (isPresent()) {
			return value;
		}
		throw new NoSuchElementException();
	}

	public T orElse(T other) {
		if (isPresent()) {
			return get();
		}
		return other;
	}

	// Optional<T> filter(Predicate<? super T> predicate){
	// If a value is present, and the value matches the given predicate, return
	// an Optional describing the value, otherwise return an empty Optional.
	// }

	// <U> Optional<U> flatMap(Function<? super T,Optional<U>> mapper){
	// If a value is present, apply the provided Optional-bearing mapping
	// function to it, return that result, otherwise return an empty Optional.
	// }

	public void ifPresent(Consumer<? super T> consumer){
		 if(isPresent()){
			 consumer.accept(get());
		 }
	 }

	 public <U> Optional<U> map(Function<? super T,? extends U> mapper){
		 if(!isPresent()){
			 return Optional.empty();
		 }
		 return  Optional.ofNullable((U) mapper.apply(value));
	 }

	// public T orElseGet(Supplier<? extends T> other){
	// // Return the value if present, otherwise invoke other and return the
	// result of that invocation.
	// }

	// <X extends Throwable>
	// T orElseThrow(Supplier<? extends X> exceptionSupplier){
	// Return the contained value, if present, otherwise throw an exception to
	// be created by the provided supplier.
	// }
}
