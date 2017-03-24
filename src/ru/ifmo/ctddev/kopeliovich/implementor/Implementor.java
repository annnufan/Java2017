package ru.ifmo.ctddev.kopeliovich.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * Implemetor for token.
 * Realisation of <tt>info.kgeorgiy.java.advanced.implementor.Impler</tt>
 * Implementation of given interface or abstract class
 * @author Kopeliovich Anna(annnufan@gmail.com)
 */
public class Implementor implements JarImpler {

    /**
     * Type token that will be implemented
     */
    private Class<?> exClass;
    /**
     * name of new realisation
     */
    private String name, classPathName;

    /**
     * Generate String of default return value for <code>token</code>.
     * For {@link Boolean boolean} return <tt>true</tt>.
     * For {@link Void void} return empty String <tt>""</tt>.
     * For {@link javax.lang.model.type.PrimitiveType primitive} return <tt>0</tt>.
     * For all other type-token will be returned <tt>null</tt>
     *
     * @param value is token for return value
     * @return {@link String} of return value type
     */
    private String returnValue(Class<?> value) {
        if (value.isPrimitive()) {
            if (value.equals(boolean.class)) {
                return " true";
            }
            if (value.equals(void.class)) {
                return "";
            }
            return " 0";
        }
        return " null";
    }

    /**
     * For <code>root</code> generate correct String that is {@link Path path} to file in Windows.
     * For {@link Path path} of <code>root</code> add way to {@link Package package} of <code>exClass</code>.
     * @param root is {@link Path path} to our implementation class.
     * @return correct {@link String} of file {@link Path path}.
     * @throws IOException if couldn't create directories for {@link Path path}.
     */
    private Path filePath(Path root) throws IOException {
        if (exClass.getPackage() != null) {
            root = root.resolve(exClass.getPackage().getName().replace('.', '/')).normalize();
        }
        Files.createDirectories(root);
        return root.resolve(exClass.getSimpleName());
    }

    /**
     * Transform {@link Class#getModifiers() modifier} to {@link String}. And without {@link Modifier#ABSTRACT abstract} part.
     * Also filtered by <code>type</code> and his modification.
     * @param mod is value from {@link Class#getModifiers() modifier}
     * @param type is type token uses Class.
     * @return {@link String} of {@link Modifier}.
     */
    private String printModifier(int mod, int type){
        return Modifier.toString(mod & ~Modifier.ABSTRACT & type) + " ";
    }

    /**
     * Transform {@link Parameter parameter} of methods and constructors to {@link String}.
     * Write {@link Modifier modifiers}, <code>type</code> and <code>name</code> of each parameter.
     * @param parameters is Array of {@link Parameter parameter}.
     * @return {@link String} interpretation of <code>parameters</code>.
     */
    private String printParameters(Parameter[] parameters) {
        String val = "";
        for (Parameter par : parameters) {
            if (!val.equals(""))
                val += ", ";
            val += printModifier(par.getModifiers(), Modifier.parameterModifiers()) + par.getType().getCanonicalName() + " " + par.getName();
        }
        return val;
    }

    /**
     * Transform {@link Exception exception} to {@link String}.
     * Write all throws <code>Exception</code>.
     * @param exception is Array of {@link Exception exception}
     * @return {@link String} interpretation of <code>exception</code>.
     */
    private String printException(Class<?>[] exception) {
        if (exception.length == 0)
            return "";
        return Arrays.stream(exception).map(Class::getCanonicalName).collect(Collectors.joining(", ", "throws ", ""));
    }

    /**
     * Make list of {@link Parameter parameters} with delimiter.
     * @param parameters is Array of {@link Parameter parameters}
     * @return {@link String} interpretation of <code>parameters</code>.
     */

    private String printParametersNames(Parameter[] parameters) {
        return Arrays.stream(parameters).map(Parameter::getName).collect(Collectors.joining(", "));
    }

    /**
     * Produces code implementing class or interface specified by provided <tt>token</tt>.
     * <p>
     * Generated class full name should be same as full name of the type token with <tt>Impl</tt> suffix
     * added. Generated source code should be placed in the correct subdirectory of the specified
     * <tt>root</tt> directory and have correct file name. For example, the implementation of the
     * interface {@link List} should go to <tt>$root/java/util/ListImpl.java</tt>
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     * @throws ImplerException when implementation cannot be
     *                         generated.
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token == null || root == null) {
            throw new ImplerException("Empty token or root.");
        }
        if (token.isPrimitive() || token.equals(Enum.class) || token.isArray()) {
            throw new ImplerException("Token is not class or interface");
        }
        if (Modifier.isFinal(token.getModifiers())) {
            throw new ImplerException("Token is final");
        }
        name = token.getSimpleName() + "Impl";
        exClass = token;
        try {
            classPathName = filePath(root).toString() + "Impl.java";
            Path path = Paths.get(classPathName);
            try (Writer classFile = new UnicodeFilter(Files.newBufferedWriter(path))) {
                printClass(classFile);
            } catch (IOException e) {
                System.err.println("Can't print class " + classPathName);
            }
        } catch (InvalidPathException e) {
            System.err.println("Can't get path " + classPathName);
        } catch (IOException e) {
            System.err.println("Can't create directories");
        }
    }

    /**
     * Filter for Unicode extends {@link FilterWriter}
     *
     */
    private class UnicodeFilter extends FilterWriter{


        /**
         * Create a new filtered writer for Unicode.
         *
         * @param out a Writer object to provide the underlying stream.
         * @throws NullPointerException if <code>out</code> is <code>null</code>
         */
        protected UnicodeFilter(Writer out) {
            super(out);
        }

        /**
         * Replaces unicode characters in <code>string</code> to "\\uXXXX" sequences
         * <p>
         * {@inheritDoc}
         */
        @Override
        public void write(String string, int off, int len) throws IOException {
            StringBuilder b = new StringBuilder();
            for (char c : string.substring(off, off + len).toCharArray()) {
                if (c >= 128) {
                    b.append(String.format("\\u%04X", (int) c));
                } else {
                    b.append(c);
                }
            }
            super.write(b.toString(), 0, b.length());
        }
    }

    /**
     * Write {@link Method method} to {@link BufferedWriter writer}.
     * Write Method with it <code>name</code>, <code>parameters</code> and with default return value.
     * @param method is {@link Method} for writing.
     * @param writer is {@link BufferedWriter} for writing.
     * @throws IOException is Exception of {@link BufferedWriter}.
     */
    private void printMethod(Method method, Writer writer) throws IOException {
        writer.write("\t" + printModifier(method.getModifiers(), Modifier.methodModifiers()) + method.getReturnType().getCanonicalName() + " " + method.getName() + "(");
        writer.write(printParameters(method.getParameters()) + "){");
        writer.write("return" + returnValue(method.getReturnType()) + ";}" + "\n");
    }

    /**
     * Function for finding and writing all abstract {@link Method methods}.
     * @param classEx is type token that will be implemented.
     * @param writer is {@link BufferedWriter} for writing.
     * @param container is {@link HashSet} with {@link Container}.
     * @throws IOException is Exception of {@link BufferedWriter}.
     */
    private void findMethod(Class<?> classEx, Writer writer, HashSet<Container> container) throws IOException {
        if (!Modifier.isAbstract(classEx.getModifiers()))
            return;
        for (Method method : classEx.getMethods()) {
            if (Modifier.isAbstract(method.getModifiers()) && !container.contains(method)) {
                printMethod(method, writer);
                container.add(new Container(method));
            }
        }
        while (classEx != null && !classEx.equals(Object.class)) {
            if (!Modifier.isAbstract(classEx.getModifiers()))
                return;
            for (Method method : classEx.getDeclaredMethods()) {
                if (Modifier.isAbstract(method.getModifiers()) && !Modifier.isPrivate(method.getModifiers()) && !Modifier.isPublic(method.getModifiers()) && !container.contains(method)) {
                    printMethod(method, writer);
                    container.add(new Container(method));
                }
            }
            classEx = classEx.getSuperclass();
        }
    }

    /**
     * Write our class <code>exClass</code> to {@link BufferedWriter writer}.
     *
     *
     * @param writer is {@link BufferedWriter} for writing.
     * @throws IOException is Exception of {@link BufferedWriter}.
     * @throws ImplerException is will be throwing if type token haven't public constructors, but have private or protected.
     */
    private void printClass(Writer writer) throws IOException, ImplerException {
        if (exClass.getPackage() != null) {
            writer.write("package " + exClass.getPackage().getName() + ";" + "\n");
        }
        writer.write(printModifier(exClass.getModifiers(), Modifier.classModifiers()) + "class " + name + " ");
        writer.write((exClass.isInterface()?"implements ":"extends ") + exClass.getSimpleName() + "{"+ "\n");
        int counterConstr = 0;
        for (Constructor<?> constructor : exClass.getDeclaredConstructors()) {
            if (!Modifier.isFinal(constructor.getModifiers()) && !Modifier.isPrivate(constructor.getModifiers())) {
                writer.write("\t" + printModifier(constructor.getModifiers(), Modifier.constructorModifiers()) + name + "(" + printParameters(constructor.getParameters()) + ")");
                writer.write(printException(constructor.getExceptionTypes()) + " {");
                writer.write("super(" + printParametersNames(constructor.getParameters()) + ");}"+ "\n");
                counterConstr++;
            }
        }
        if ((counterConstr == 0) && (exClass.getDeclaredConstructors().length > 0)) {
            throw new ImplerException("Haven't constructors for override.");
        }
        HashSet<Container> container = new HashSet<>();
        findMethod(exClass, writer, container);
        writer.write("}"+ "\n");
    }

    public static void main(String[] args) {
        Implementor imp = new Implementor();
        try {
            imp.implement(Class.forName(args[0]), Paths.get(args[1]));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("Class from arguments not found");
        } catch (ImplerException e) {
            e.printStackTrace();
        }
    }


    /**
     * Produces <tt>.jar</tt> file implementing class or interface specified by provided <tt>token</tt>.
     * <p>
     * Generated class full name should be same as full name of the type token with <tt>Impl</tt> suffix
     * added.
     *
     * @param token type token to create implementation for.
     * @param jarFile target <tt>.jar</tt> file.
     * @throws ImplerException when implementation cannot be generated.
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        try {
            Path path = Paths.get(".");
            implement(token, path);
            Path file = Paths.get(classPathName).normalize();
            compileFile(path, file);
            Path classFile = fileClassPath(file);
            printJar(jarFile, classFile);
            classFile.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new ImplerException("Find IOException in process of get file name: " + e.getMessage());
        } catch (ImplerException e) {
            throw new ImplerException(e);
        }
    }

    /**
     *
     /**
     * Print JARFile from {@link Path pathJar}.
     * @param path -- home directory for JAR
     * @param filePath -- {@link Path} for printing
     * @throws IOException if problem in {@link JarOutputStream}
     */
    private void printJar(Path path, Path filePath) throws IOException {
        try(JarOutputStream jarStream = new JarOutputStream(Files.newOutputStream(path))) {
            jarStream.putNextEntry(new ZipEntry(filePath.toString()));
            Files.copy(filePath, jarStream);
        }
    }


    /**
     * Transform {@link Path path} of ".java" to ".class"
     * @param path is {@link Path} of Java file.
     * @return {@link Path path} of class file.
     * @throws ImplerException if file not ".java"
     */
    private Path fileClassPath(Path path) throws ImplerException {
        String pathStr = path.toString();
        if (!pathStr.endsWith(".java")) {
            throw new ImplerException("Not java file path");
        }
        return Paths.get(pathStr.substring(0, pathStr.length() - 5) + ".class");
    }

    /**
     * Compile <code>fileToCompile</code> from <code>root</code>.
     * @param root is {@link Path} of <code>fileToCompile</code>
     * @param fileToCompile is {@link Path} of file to compile
     * @throws ImplerException if compiler not found or code of returns not zero
     */
    private void compileFile(Path root, Path fileToCompile) throws ImplerException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Compiler not found");
        }

        int returnCode = compiler.run(null, null, null, fileToCompile.toString(), "-encoding", "utf8", "-cp",
                root + "/" + System.getProperty("java.class.path"));
        if (returnCode != 0) {
            throw new ImplerException("Compiler returns non-zero code");
        }
    }

    /**
     * Container for {@link Method} with @override <code>equals</code> and <code>hashCode</code>.
     */
    private class Container{

        /**
         * Our {@link Method}
         */
        final Method method;

        /**
         * Constructor for our Container.
         * @param method is will be our <code>method</code>.
         */
        Container(Method method) {
            this.method = method;
        }

        /**Two {@link Method methods}  will be <code>equals</code> if they have same name and parameter's type.
         * @param obj is {@link Object} for comparing. If <code>obj</code> isn't <code>Container</code> -- return <code>false</code>.
         * @return it's will be <code>true</code> if <code>method</code> and <code>obj</code> are equals.
         */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Container)) {
                return false;
            }
            Method method2 = ((Container) obj).method;

            return method.getName().equals(method2.getName()) && Arrays.equals(method.getParameterTypes(), method2.getParameterTypes());
        }

        /**
         * Use name and type of parameters for hashing.
         * @return hash code of our {@link Method methods}
         */
        @Override
        public int hashCode() {
            int hash = method.getName().hashCode();
            for (Parameter p : method.getParameters()) {
                hash ^= p.getType().hashCode();
            }
            return hash;
        }
    }
}