package com.infonova.eircom.utils;
//package com.thecoshman.util.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Makes it easier to do what you really shouldn't have to do.
 * <p>
 * Ideally, you would only ever use this in unit testing of older legacy code
 * If you are writing new test code that requires this, something is probably wrong
 * If you are writing new production code that requires this, something is probably horribly wrong
 * <p>
 * Whilst all methods are public, some expose the raw reflection api a bit too much, and should be avoided
 */
class EvilReflection {
    /**
     * An exception that can be thrown by some of the methods of <code>EvilReflection</code>
     */
    public static class ReflectionException extends RuntimeException {
        public ReflectionException(final String msg) {
            super(msg);
        }

        public ReflectionException(final String msg, final Exception e) {
            this(msg + ": " + e.getMessage());
        }

        private static final long serialVersionUID = -1098766058205613891L;
    }

    /**
     * set a static field to a given value
     *
     * @param type       the type you wish to set a field on
     * @param fieldName  name of the field, as appears in the source code
     * @param fieldValue the value you wish to set for the field
     */
    public static void set(final Class<?> type, final String fieldName, final Object fieldValue) {
        final Field field = getField(type, fieldName);
        removeFinal(field);
        setAccessible(field);
        setValue(field, fieldValue);
    }

    /**
     * set an instance field to a give value
     *
     * @param object     the instance you wish to set a field on
     * @param fieldName  name of the field, as appears in the source code
     * @param fieldValue the value you wish to set for the field
     */
    public static void set(final Object object, final String fieldName, final Object fieldValue) {
        final Field field = getField(object.getClass(), fieldName);
        removeFinal(field);
        setAccessible(field);
        setValue(object, field, fieldValue);
    }

    /**
     * get the value of a static field
     *
     * @param type      the type you wish to get a value from
     * @param fieldName name of the field, as appears in the source code
     * @return the value that field was set to
     */
    public static <T> T get(final Class<?> type, final String fieldName){
        final Field field = getField(type, fieldName);
        setAccessible(field);
        try {
            return (T) field.get(null);
        } catch (IllegalAccessException e) {
            throw new ReflectionException("Couldn't read the value of the static field '" + fieldName + "' from type '" + type.getName() + "'", e);
        }
    }

    /**
     * get the value of an instance field
     *
     * @param object    the instance you wish to get a value from
     * @param fieldName name of the field, as appears in the source code
     * @return the value that field was set to
     */
    public static <T> T get(final Object object, final String fieldName){
        final Field field = getField(object.getClass(), fieldName);
        setAccessible(field);
        try {
            return (T) field.get(object);
        } catch (IllegalAccessException e) {
            throw new ReflectionException("Couldn't read the value of the field '" + fieldName + "' from an object of type '" + object.getClass().getName() + "'", e);
        }
    }

    /**
     * invoke a static method that has no arguments
     *
     * @param type       the type you wish to invoke a method from
     * @param methodName name of the method, as appears in the source code
     * @return the value returned by the method invoked
     */
    public static <T> T invoke(final Class<?> type, final String methodName) {
        return invoke(type, methodName, new Class<?>[0], new Object[0]);
    }

    /**
     * invoke a static method that has arguments of the given types, passing the given values
     *
     * @param type       the type you wish to invoke a method from
     * @param methodName name of the method, as appears in the source code
     * @param argTypes   the list of argument types for the method you wish to invoke. These form part of the signature of the method.
     * @param argValues  the list of values you wish to pass to the invoked <code>Method</code>. These should be in the correct order, as in the source code.
     * @return the value returned by the method invoked
     */
    public static <T> T invoke(final Class<?> type, final String methodName, final Class<?>[] argTypes, final Object[] argValues) {
        final Method method = getMethod(type, methodName, argTypes);
        setAccessible(method);
        return invoke(method, argValues);
    }

    /**
     * invoke an instance method that has no arguments
     *
     * @param instance   the instance you wish to invoke a method from
     * @param methodName name of the method, as appears in the source code
     * @return the value returned by the method invoked
     */
    public static <T> T invoke(final Object instance, final String methodName) {
        return invoke(instance, methodName, new Class<?>[0], new Object[0]);
    }

    /**
     * invoke an instance method that has arguments of the given types, passing the given values
     *
     * @param instance   the instance you wish to invoke a method from
     * @param methodName name of the method, as appears in the source code
     * @param argTypes   the list of argument types for the method you wish to invoke. These form part of the signature of the method.
     * @param argValues  the list of values you wish to pass to the invoked method. These should be in the same order as the arguments.
     * @return the value returned by the method invoked
     */
    public static <T> T invoke(final Object instance, final String methodName, final Class<?>[] argTypes, final Object[] argValues) {
        final Method method = getMethod(instance.getClass(), methodName, argTypes);
        setAccessible(method);
        return invoke(instance, method, argValues);
    }

    /**
     * get a field from a type. It does not matter if you want it for static or an instance
     * this will recurse up to the hierarchy to find the field in 'Object' if required
     * <P>
     * not recommended for direct use, this would be used internally by other methods
     * for setting the value of a field, consider using <code>set</code>
     * for getting the value of a field, consider using <code>get</code>
     *
     * @param type the type you wish to get a <code>Field</code> from
     * @param name the name of <code>Field</code>, as appears in the source code
     * @return the requested <code>Field</code> if it was found
     */
    public static Field getField(final Class<?> type, final String name) {
        final Field[] fields = type.getDeclaredFields();
        for (final Field field : fields) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        if(Object.class.equals(type)) {
            throw new ReflectionException("unable to find the field '" + name + "' in type '" + type.getName() + "'");
        }
        return getField(type.getSuperclass(), name);
    }

    /**
     * get a method from a type. This does not matter if you want it for static or an instance
     * this will recurse up to the hierarchy to find the method in 'Object' if required
     * <P>
     * not recommended for direct use, this would be used internally by other methods
     * for invoking methods, consider instead <code>invoke</code>
     *
     * @param type     the type you wish to get a <code>Method</code> from
     * @param name     the name of <code>Method</code>, as appears in the source code
     * @param argTypes the types of the arguments, in the correct order, as they appear in the source code. Must be set, even if just an empty array
     * @return the request <code>Method</code> if it was found
     */
    public static Method getMethod(final Class<?> type, final String name, final Class<?>[] argTypes) {
        try {
            return type.getDeclaredMethod(name, argTypes);
        } catch (NoSuchMethodException | SecurityException e) {
            if (Object.class.equals(type)) {
                throw new ReflectionException("Couldn't find the method", e);
            }
        }
        return getMethod(type.getSuperclass(), name, argTypes);
    }

    /**
     * removes the <code>final</code> modifier from a <code>Field</code>
     * <p>
     * handles if the <code>Field</code> already does not have the <code>final</code> modifier
     * this is unable to remove <code>final</code> from certain <code>String</code> objects (<code>static final</code> I believe)
     * <p>
     * not recommended for direct use, this would be used internally by other methods
     *
     * @param field the <code>Field</code> to attempt to remove the <code>final</code> modifier from
     */
    public static void removeFinal(final Field field) {
        if ((field.getModifiers() & Modifier.FINAL) == 0) {
            return;  // field is already final
        }
        try {
            final Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (final NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new ReflectionException("Exception whilst attempting to remove 'final' restriction", e);
        }
    }

    /**
     * effectively makes the field 'public'
     * <p>
     * not recommended for direct use, this would be used internally by other methods
     *
     * @param field the <code>Field</code> to attempt to make accessible
     */
    public static void setAccessible(final Field field) {
        field.setAccessible(true);
    }

    /**
     * effectively makes the method 'public'
     * <p>
     * not recommended for direct use, this would be used internally by other methods
     *
     * @param method the <code>Method</code> to attempt to make accessible
     */
    public static void setAccessible(final Method method) {
        method.setAccessible(true);
    }

    /**
     * set a static field to a given value
     * <p>
     * not recommended for direct use, this would be used internally by other methods
     * consider instead <code>set(Class<?>, String, Object)</code>
     *
     * @param field the (static) <code>Field</code> you wish set the value for
     * @param value the value you wish to set for the <code>Field</code>
     */
    public static void setValue(final Field field, final Object value) {
        setValue(null, field, value);
    }

    /**
     * set an instance field to a given value
     * <p>
     * not recommended for direct use, this would be used internally by other methods
     * consider instead <code>set(Object, String, Object)</code>
     *
     * @param instance the <code>Object</code> for which you wish to set a <code>Field</code> for
     * @param field    the <code>Field</code> you wish set the value for
     * @param value    the value you wish to set for the <code>Field</code>
     */
    public static void setValue(final Object instance, final Field field, final Object value) {
        try {
            field.set(instance, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new ReflectionException("Was unable to set value of field", e);
        }
    }

    /**
     * invoke a Method for a static type, passing in the given argument values
     * <p>
     * not recommended for direct use, this would be used internally by other methods
     * consider instead <code>invoke(Class<?>, String)</code>
     *
     * @param method the (static) <code>Method</code> to invoke
     * @param args   the arguments to pass to the <code>Method</code>. Must be set, even if just an empty array
     * @return the value return from the invoked <code>Method</code>
     */
    public static <T> T invoke(final Method method, final Object[] args) {
        return invoke(null, method, args);
    }

    /**
     * invoke a Method for an instance, passing in the given argument values
     * <p>
     * not recommended for direct use, this would be used internally by other methods
     * consider instead either `invoke(Object, String)` or `invoke(Object, String, Class<?>[], Object[])`
     *
     * @param instance the instance on which to invoke this <code>Method</code>
     * @param method   the (static) <code>Method</code> to invoke
     * @param args     the arguments to pass to the <code>Method</code>. Must be set, even if just an empty array
     * @return the value return from the invoked <code>Method</code>
     */
    @SuppressWarnings("unchecked")
    public static <T> T invoke(final Object instance, final Method method, final Object[] args) {
        try {
            return (T) method.invoke(instance, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new ReflectionException("Unable to invoke static method", e);
        }
    }

    /**
     * helper function for cleanly building an array of Classes
     * works the same as <code>array(Object...)</code> but specifically for <code>Class</code>
     *
     * @param classes the objects you wish to add to the array. These need not be of the same type
     * @return a primitive array version of the parameters passed in
     */
    public static Class<?>[] array(final Class<?>... classes) {
        return classes;
    }

    /**
     * helper function for cleanly building an array of Objects
     *
     * @param objects the objects you wish to add to the array. These need not be of the same type
     * @return a primitive array version of the parameters passed in
     */
    public static Object[] array(final Object... objects) {
        return objects;
    }
}
