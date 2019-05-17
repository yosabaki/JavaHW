package ru.ifmo.rain.ustinov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Generates .java and .jar files of implementations of classes and interfaces.
 *
 * @author Artem Ustinov
 * @see Implementor#implement(Class, Path)
 * @see Implementor#implementJar(Class, Path)
 */
public class Implementor implements JarImpler {
    /**
     * Creates .jar file with implementation of given class or interface. <p>
     * Resulted archive has name of given with suffix Impl. Generated class
     * derives given class or interface and contains implementation of its
     * abstract methods and non-private constructors.
     *
     * @param aClass class or interface, implementation of that will be
     *               generated.
     * @param path   path to file, where generated file will be created.
     * @throws ImplerException if given class cannot be created:
     *                         <ul>
     *                         <li>One of the arguments is null</li>
     *                         <li>Given class is primitive, final or array</li>
     *                         <li>Path to file is invalid</li>
     *                         <li>An error occurred while compiling generated class file.</li>
     *                         </ul>
     * @see Implementor#implement(Class, Path)
     */
    @Override
    public void implementJar(Class<?> aClass, Path path) throws ImplerException {
        if (aClass == null || path == null) {
            throw new ImplerException("Arguments must be non-null");
        }
        Path tempDirectory;
        try {
            tempDirectory = Files.createTempDirectory(path.toAbsolutePath().getParent(), "temp");
        } catch (IOException e) {
            throw new ImplerException("Can't create temp directory: ", e);
        }
        try {
            implement(aClass, tempDirectory);
            final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                throw new ImplerException("Can not find java compiler.");
            }
            List<String> args = new ArrayList<>();
            args.add("-cp");
            try {
                args.add(tempDirectory + File.pathSeparator + aClass.getProtectionDomain().getCodeSource().getLocation().toURI().toString());
            } catch (URISyntaxException e) {
                throw new AssertionError(e);
            }
            args.add("-encoding");
            args.add("utf-8");
            args.add(tempDirectory.resolve(aClass.getName().replace('.', File.separatorChar) + "Impl.java").toString());
            if (compiler.run(null, null, null, args.toArray(String[]::new)) != 0) {
                throw new ImplerException("Can't compile file");
            }
            Manifest manifest = new Manifest();
            Attributes attributes = manifest.getMainAttributes();
            attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
            try (JarOutputStream writer = new JarOutputStream(Files.newOutputStream(path), manifest)) {
                writer.putNextEntry(new ZipEntry(aClass.getName().replace('.', File.separatorChar) + "Impl.class"));
                Files.copy(tempDirectory.resolve(aClass.getName().replace('.', File.separatorChar) + "Impl.class"), writer);
            } catch (IOException e) {
                throw new ImplerException("Unable to write to JAR file", e);
            }
        } finally {
            tempDirectory.toFile().deleteOnExit();
        }
    }


    /**
     * Creates .java file with implementation of given class or interface <p>
     * Creates output file in directory of given path. Resulted file contains
     * valid code of class derived of given, and have suffix Impl in name.
     *
     * @param aClass class or interface, implementation of that will be
     *               generated.
     * @param path   path to file, where generated file will be created.
     * @throws ImplerException if given class cannot be created:
     *                         <ul>
     *                         <li>One of the arguments is null</li>
     *                         <li>Given class is primitive, final or array</li>
     *                         <li>Path to file is invalid</li>
     *                         <li>An error occured while compiling generated class file.</li>
     *                         </ul>
     */
    @Override
    public void implement(Class<?> aClass, Path path) throws ImplerException {
        if (aClass == null || path == null) {
            throw new ImplerException("Arguments must be non-null.");
        }
        if (aClass.isPrimitive() || aClass.isArray() || aClass == Enum.class || Modifier.isFinal(aClass.getModifiers())) {
            throw new ImplerException("Class can't be implemented.");
        }
        Path newPath = Paths.get(path.toString(), aClass.getName().replace('.', File.separatorChar) + "Impl.java");
        try {
            Files.createDirectories(newPath.getParent());
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(newPath, StandardCharsets.UTF_8)) {
                ClassWriter classWriter = new ClassWriter();
                bufferedWriter.write(classWriter.getClassCode(aClass));
            } catch (IOException e) {
                throw new ImplerException(" Can't write into file " + path.getFileName() + ": " + e.getMessage(), e);
            }
        } catch (IOException e) {
            throw new ImplerException("Can't create directories " + newPath.getParent().toString() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Prints an usage message in console.
     */
    private static void printUsage() {
        System.out.println("Usage: implementor [-jar] path/to/ouptut/file path/to/input/class");
    }

    /**
     * Creates implementation depending on arguments from command line. <p>
     * There are two legal cases:
     * <ul>
     * <li> -jar path className - runs {@link #implementJar(Class, Path)}</li>
     * <li> path className - runs {@link #implement(Class, Path)}</li>
     * </ul>
     *
     * @param args arguments for given program.
     */
    public static void main(String[] args) {
        if (args.length != 2 && args.length != 3) {
            printUsage();
            return;
        }
        for (var arg : args) {
            if (arg == null) {
                System.err.println("Arguments must be non-null.");
                return;
            }
        }
        JarImpler impler = new Implementor();
        try {
            if (args.length == 2) {
                impler.implement(Class.forName(args[0]), Paths.get(args[1]));
            } else if (args[0].equals("-jar")) {
                impler.implementJar(Class.forName(args[1]), Paths.get(args[2]));
            } else {
                printUsage();
            }
        } catch (ImplerException e) {
            System.err.println("Can't implement class " + args[0] + ": " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Class " + args[0] + " is not found." );
        } catch (InvalidPathException e) {
            System.err.println("Invalid path: " + args[1]);
        }
    }
}
