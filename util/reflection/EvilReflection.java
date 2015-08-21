package com.thecoshman.util.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

class EvilReflection {
    public static class ReflectionException extends RuntimeException {
        public ReflectionException(final String msg) {
            super(msg);
        }

        public ReflectionException(final String msg, final Exception e) {
            this(msg + ": " + e.getMessage());
        }

        private static final long serialVersionUID = -1098766058205613891L;
    }

    public static void set(final Class<?> type, final String fieldName, final Object fieldValue) {
        final Field field = getField(type, fieldName);
        removeFinal(field);
        setAccessible(field);
        setValue(field, fieldValue);
    }

    public static void set(final Object object, final String fieldName, final Object fieldValue) {
        final Field field = getField(object.getClass(), fieldName);
        removeFinal(field);
        setAccessible(field);
        setValue(object, field, fieldValue);
    }

    public static <T> T invoke(final Class<?> type, final String methodName) {
        return invoke(type, methodName, new Class<?>[0], new Object[0]);
    }

    public static <T> T invoke(final Class<?> type, final String methodName, final Class<?>[] argTypes, final Object[] argValues) {
        final Method method = getMethod(type, methodName, argTypes);
        setAccessible(method);
        return invoke(method, argValues);
    }

    public static <T> T invoke(final Object instance, final String methodName) {
        return invoke(instance, methodName, new Class<?>[0], new Object[0]);
    }

    public static <T> T invoke(final Object instance, final String methodName, final Class<?>[] argTypes, final Object[] argValues) {
        final Method method = getMethod(instance.getClass(), methodName, argTypes);
        setAccessible(method);
        return invoke(instance, method, argValues);
    }

    public static Field getField(final Class<?> type, final String name) {
        final Field[] fields = type.getDeclaredFields();
        for (final Field field : fields) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        throw new ReflectionException("unable to find the field '" + name + "' in type '" + type.getName() + "'");
    }

    public static Method getMethod(final Class<?> type, final String methodName, final Class<?>[] argTypes) {
        try {
            return type.getDeclaredMethod(methodName, argTypes);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new ReflectionException("Couldn't find the method", e);
        }
    }

    public static void removeFinal(final Field field) {
        if ((field.getModifiers() & Modifier.FINAL) == 0) {
            return; // field is alaready final
        }
        try {
            final Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (final NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new ReflectionException("Exception whilst attempting to remove 'final' resitriction", e);
        }
    }

    public static void setAccessible(final Field field) {
        field.setAccessible(true);
    }

    public static void setAccessible(final Method method) {
        method.setAccessible(true);
    }

    public static void setValue(final Field field, final Object value) {
        setValue(null, field, value);
    }

    public static void setValue(final Object instance, final Field field, final Object value) {
        try {
            field.set(instance, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new ReflectionException("Was unable to set value of field", e);
        }
    }

    public static <T> T invoke(final Method method, final Object[] args) {
        return invoke(null, method, args);
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(final Object instance, final Method method, final Object[] args) {
        try {
            return (T) method.invoke(instance, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new ReflectionException("unable to invoke static method", e);
        }
    }

    public static Class<?>[] array(final Class<?>... classes) {
        return classes;
    }

    public static Object[] array(final Object... objects) {
        return objects;
    }
}
