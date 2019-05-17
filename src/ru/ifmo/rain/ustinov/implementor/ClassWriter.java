package ru.ifmo.rain.ustinov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;


/**
 * Generates code with implementation of given class or interface.
 */
class ClassWriter {
    /**
     * Tab symbol, constructed with 4 spaces.
     */
    private static final String INDENT = "    ";

    /**
     * Generates code with type name of given class.
     *
     * @return full type name with packages or simple
     * type if it is primitive
     */
    private String getTypeCode(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return clazz.getSimpleName();
        }
        return clazz.getCanonicalName();
    }

    /**
     * Generates paddings for strings.
     *
     * @return padding of {@link #INDENT} symbols
     */
    private String getIndentation(int padding) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < padding; i++) {
            sb.append(INDENT);
        }
        return sb.toString();
    }

    /**
     * generates code with modifiers of given class.
     *
     * @param modifiers enum code of {@link java.lang.reflect.Modifier}
     * @return code with given modifiers excluded native,
     * transient and abstract
     */
    private String getModifiersCode(int modifiers) {
        return Modifier.toString(modifiers & ~Modifier.NATIVE & ~Modifier.TRANSIENT & ~Modifier.ABSTRACT);
    }

    /**
     * generates a standard value of given class.
     *
     * @return <ul>
     * <li>false for {@link Boolean}</li>
     * <li>empty string for void</li>
     * <li>"0" for primitives</li>
     * <li>"null" for classes</li>
     * </ul>
     */
    private String getReturnValue(Class<?> clazz) {
        if (clazz.equals(boolean.class)) {
            return " false";
        }
        if (clazz.equals(void.class)) {
            return "";
        }
        if (clazz.isPrimitive()) {
            return " 0";
        }
        return " null";
    }

    /**
     * Generates name of given class used in arguments of methods and constructors.
     *
     * @param number a suffix to generated name.
     * @return name of given class concatenated with given number.
     */
    private String generateArgumentName(Class<?> clazz, int number) {
        if (clazz.isPrimitive()) {
            return clazz.getSimpleName().substring(0, 1) + number;
        }
        return Character.toLowerCase(clazz.getSimpleName().charAt(0)) + clazz.getSimpleName().substring(1).replace("[]", "s") + number;
    }

    /**
     * Generates implementation body of given method or constructor.
     *
     * @param executable method or constructor that needs to be generated
     * @return valid code of given method or constructor.
     */
    private String getExecitableImplementationCode(Executable executable) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndentation(2));
        if (executable instanceof Method) {
            sb.append("return");
            sb.append(getReturnValue(((Method) executable).getReturnType()));
        } else {
            sb.append("super");
            sb.append(getArgumentsCode(executable, false));
        }
        sb.append(";").append(System.lineSeparator());
        return sb.toString();
    }

    /**
     * Generates code for parameters of given method or constructor.
     *
     * @param executable method or constructor that needs to be generated
     * @param showType   flag that determines that return type will be generated
     * @return a string with listed arguments of given method or constructor,
     * covered in brackets, separated by commas.
     * if showType is true then arguments are leaded with their types
     * else only generated names are generated.
     */
    private String getArgumentsCode(Executable executable, boolean showType) {
        StringJoiner sj = new StringJoiner(", ", "(", ")");
        int i = 0;
        for (Class<?> c : executable.getParameterTypes()) {
            sj.add((showType ? getTypeCode(c) + " " : "") + generateArgumentName(c, i++));
        }
        return sj.toString();
    }

    /**
     * Generates a code with import string of given package.
     *
     * @param aPackage package that need to be imported
     * @return import string of given package
     * or empty string if package is empty.
     */
    private String getPackageCode(Package aPackage) {
        if (aPackage.getName().isEmpty()) {
            return "";
        }
        return "package " +
                aPackage.getName() +
                ";" +
                System.lineSeparator();
    }

    /**
     * Generates a declaration code of given class. <p>
     * Resulted class has name of given class with "Impl" suffix.
     *
     * @param clazz a class that needs to be generated
     * @return generates declaration of public class that derives given.
     */
    private String getClassDeclarationCode(Class<?> clazz) {
        return "public class " + clazz.getSimpleName() + "Impl " + (clazz.isInterface() ? "implements " : "extends ") + clazz.getSimpleName() + " ";
    }

    /**
     * Adds implementation codes of abstract methods of given class to given hashSet.
     *
     * @param clazz       Class that needs to be generated
     * @param methodCodes {@link HashSet} of distinct abstract methods
     *                    that contains codes of these methods
     */
    private void addAbstractMethods(Class<?> clazz, Set<String> methodCodes) {
        if (clazz == null) {
            return;
        }
        for (var method : clazz.getMethods()) {
            if (Modifier.isAbstract(method.getModifiers())) {
                methodCodes.add(getExecutableCode(method));
            }
        }
        for (var method : clazz.getDeclaredMethods()) {
            if (Modifier.isAbstract(method.getModifiers())) {
                methodCodes.add(getExecutableCode(method));
            }
        }
        addAbstractMethods(clazz.getSuperclass(), methodCodes);
    }

    /**
     * Generates suffix to declaration of method with throw exception string.
     *
     * @param executable executable that needs to be generated
     * @return an empty string if given method doesn't throw checked exceptions.
     * A code with throw exception string otherwise.
     */
    private String getExceptionsCode(Executable executable) {
        if (executable.getExceptionTypes().length == 0)
            return "";
        StringJoiner stringJoiner = new StringJoiner(", ", "throws ", "");
        for (var exception : executable.getExceptionTypes()) {
            stringJoiner.add(exception.getCanonicalName());
        }
        return stringJoiner.toString();
    }

    /**
     * Generates type and name of executable.
     *
     * @param executable Executable that needs to be generated
     * @return type + name of executable + Impl
     */
    private String getTypeAndNameCode(Executable executable) {
        if (executable instanceof Method) {
            var clazz = ((Method) executable).getReturnType();
            if (clazz.isPrimitive()) {
                return clazz.getSimpleName() + " " + executable.getName();
            }
            return clazz.getCanonicalName() + " " + executable.getName();
        } else {
            return executable.getDeclaringClass().getSimpleName() + "Impl";
        }
    }

    /**
     * Generates code given method or constructor.
     *
     * @param executable Executable that needs to be generated
     * @return Full implementation code of given method or constructor.
     */
    private String getExecutableCode(Executable executable) {
        return getModifiersCode(executable.getModifiers()) +
                " " +
                getTypeAndNameCode(executable) +
                getArgumentsCode(executable, true) +
                getExceptionsCode(executable) +
                " { " +
                System.lineSeparator() +
                getExecitableImplementationCode(executable) +
                getIndentation(1) + "}";
    }


    /**
     * Generates code for all abstract methods and public constructors in class.
     *
     * @param clazz class that needs to be implemented
     * @return A string of all generated code for all abstract methods and
     * non-private constructors of given class.
     * @throws ImplerException if there are all constructors are private
     */
    private String getExecutablesCode(Class<?> clazz) throws ImplerException {
        StringJoiner stringJoiner = new StringJoiner(System.lineSeparator() + System.lineSeparator() + getIndentation(1), "{ " + System.lineSeparator() + getIndentation(1), System.lineSeparator() + "}");
        Set<String> methodCodes = new HashSet<>();
        addAbstractMethods(clazz, methodCodes);
        for (var methodCode : methodCodes) {
            stringJoiner.add(methodCode);
        }
        Constructor[] constructors = clazz.getDeclaredConstructors();
        int counter = 0;
        for (var constructor : constructors) {
            if (!Modifier.isPrivate(constructor.getModifiers())) {
                counter++;
                stringJoiner.add(getExecutableCode(constructor));
            }
        }
        if (counter == 0 && !clazz.isInterface()) {
            throw new ImplerException("All constructors are private");
        }
        return stringJoiner.toString();
    }

    /**
     * Encodes given string to utf-8.
     *
     * @param input string that needs to be translated
     * @return unicode string of input
     */
    private String toUnicode(String input) {
        StringBuilder b = new StringBuilder();
        for (char c : input.toCharArray()) {
            if ((int) c >= 128) {
                b.append(String.format("\\u%04X", (int) c));
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }

    /**
     * Generates implementation code of given class.
     * Resulted code is implementation of public class that derives given class
     * and it has name of given class name with "Impl" suffix.
     *
     * @param clazz class that needs to be generated
     * @return An implementation code of given class encoded in utf-8.
     * @throws ImplerException if there are all constructors are private in given class.
     */
    String getClassCode(Class<?> clazz) throws ImplerException {
        @SuppressWarnings("StringBufferReplaceableByString")
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(getPackageCode(clazz.getPackage()));
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(getClassDeclarationCode(clazz));
        stringBuilder.append(getExecutablesCode(clazz));

        return toUnicode(stringBuilder.toString());
    }


}
