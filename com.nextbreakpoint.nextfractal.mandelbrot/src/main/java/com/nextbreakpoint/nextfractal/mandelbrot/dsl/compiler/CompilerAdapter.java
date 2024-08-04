/*
 * NextFractal 2.3.2
 * https://github.com/nextbreakpoint/nextfractal
 *
 * Copyright 2015-2024 Andrea Medeghini
 *
 * This file is part of NextFractal.
 *
 * NextFractal is an application for creating fractals and other graphics artifacts.
 *
 * NextFractal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NextFractal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NextFractal.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiler;

import com.nextbreakpoint.nextfractal.core.common.IOUtils;
import com.nextbreakpoint.nextfractal.core.common.ParserError;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ClassFactory;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLCompilerException;
import lombok.extern.java.Log;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import static com.nextbreakpoint.nextfractal.core.common.ParserErrorType.JAVA_COMPILE;

@Log
public class CompilerAdapter {
	private final JavaCompiler javaCompiler;

	public CompilerAdapter(JavaCompiler javaCompiler) {
		this.javaCompiler = Objects.requireNonNull(javaCompiler);
	}

	public <T> ClassFactory<T> compile(Class<T> clazz, String javaSource, String packageName, String className) throws DSLCompilerException {
		try {
			return new JavaClassFactory<>(compile(javaSource, packageName, className, clazz));
		} catch (DSLCompilerException e) {
			throw e;
		} catch (Throwable e) {
            final List<ParserError> errors = new ArrayList<>();
			errors.add(new ParserError(JAVA_COMPILE, 0, 0, 0, 0, e.getMessage()));
			throw new DSLCompilerException("Can't compile class", javaSource, errors);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> Class<T> compile(String source, String packageName, String className, Class<T> clazz) throws Exception {
		log.log(Level.FINE, "Compile Java source:\n" + source);
		final List<ParserError> errors = new ArrayList<>();
		List<SimpleJavaFileObject> compilationUnits = new ArrayList<>();
		compilationUnits.add(new JavaSourceFileObject(className, source));
		final List<String> options = getCompilerOptions();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
		String fullClassName = packageName + "." + className;
		JavaFileManager fileManager = new JavaCompilerFileManager(javaCompiler.getStandardFileManager(diagnostics, null, null), fullClassName);
		try {
			final JavaCompiler.CompilationTask task = javaCompiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
			if (task.call()) {
				JavaCompilerClassLoader loader = new JavaCompilerClassLoader();
				defineClasses(fileManager, loader, packageName, className);
				Class<?> compiledClazz = loader.loadClass(packageName + "." + className);
				log.log(Level.FINE, compiledClazz.getCanonicalName());
				if (clazz.isAssignableFrom(compiledClazz)) {
					return (Class<T>) compiledClazz;
				}
			} else {
				for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
//					if (diagnostic.getCode().equals("compiler.err.cant.access")) {
//						// Not sure why it doesn't happen with Java 8, but only with Java 9.
//						ParserErrorType type = ParserErrorType.JAVA_COMPILER;
//						long line = diagnostic.getLineNumber();
//						long charPositionInLine = diagnostic.getColumnNumber();
//						long index = diagnostic.getStartPosition();
//						long length = diagnostic.getEndPosition() - diagnostic.getStartPosition();
//						String message = diagnostic.getMessage(null);
//						ParserError error = new ParserError(type, line, charPositionInLine, index, length, message);
//						log.log(Level.WARNING, error.toString());
//						errors.add(error);
//					} else {
                    long line = diagnostic.getLineNumber();
					long charPositionInLine = diagnostic.getColumnNumber();
					long index = diagnostic.getStartPosition();
					long length = diagnostic.getEndPosition() - diagnostic.getStartPosition();
					String message = diagnostic.getMessage(null);
					ParserError error = new ParserError(JAVA_COMPILE, line, charPositionInLine, index, length, message);
					log.log(Level.WARNING, error.toString());
					errors.add(error);
					throw new DSLCompilerException("Can't compile class", source, errors);
				}
			}
		} finally {
			try {
				fileManager.close();
			} catch (IOException _) {
			}
		}
		return null;
	}

	private static List<String> getCompilerOptions() {
		//TODO is this still required?
		final String modulePath = System.getProperty("nextfractal.module.path", System.getProperty("jdk.module.path"));
		if (modulePath != null) {
			return Arrays.asList("-source", "11", "-target", "11", "-proc:none", "-Xdiags:verbose", "--module-path", modulePath, "--add-modules", "com.nextbreakpoint.nextfractal.mandelbrot");
		} else {
			return Arrays.asList("-source", "21", "-target", "21", "-proc:none", "-Xdiags:verbose");
		}
	}

	private void defineClasses(JavaFileManager fileManager, JavaCompilerClassLoader loader, String packageName, String className) throws IOException {
		String name = packageName + "." + className;
		JavaFileObject file = fileManager.getJavaFileForOutput(StandardLocation.locationFor(name), name, Kind.CLASS, null);
		byte[] fileData = loadBytes(file);
		log.log(Level.FINE, file.toUri().toString() + " (" + fileData.length + ")");
		loader.defineClassFromData(name, fileData);
	}

	private byte[] loadBytes(JavaFileObject file) throws IOException {
        try (InputStream is = file.openInputStream()) {
			try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
				IOUtils.copyBytes(is, os);
				return os.toByteArray();
			}
        }
	}
}	
