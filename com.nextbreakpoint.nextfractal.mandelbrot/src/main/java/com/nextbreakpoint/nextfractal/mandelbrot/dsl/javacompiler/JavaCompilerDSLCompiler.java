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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.javacompiler;

import com.nextbreakpoint.nextfractal.core.common.IOUtils;
import com.nextbreakpoint.nextfractal.core.common.ParserError;
import com.nextbreakpoint.nextfractal.mandelbrot.core.CompilerException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.ClassFactory;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResult;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Color;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaCompilerDSLCompiler {
	private static final Logger logger = Logger.getLogger(JavaCompilerDSLCompiler.class.getName());

	public ClassFactory<Orbit> compileOrbit(DSLParserResult report) throws CompilerException {
		List<ParserError> errors = new ArrayList<>();
		try {
			Class<Orbit> clazz = compileToClass(report.getOrbitSource(), report.getPackageName(), report.getClassName() + "Orbit", Orbit.class, errors);
			return new JavaClassFactory<>(clazz);
		} catch (Throwable e) {
			ParserError.ErrorType type = ParserError.ErrorType.JAVA_COMPILER;
			String message = e.getMessage();
			errors.add(new ParserError(type, 0, 0, 0, 0, message));
			throw new CompilerException("Can't compile orbit", report.getOrbitSource(), errors);
		}
	}

	public ClassFactory<Color> compileColor(DSLParserResult report) throws CompilerException {
		List<ParserError> errors = new ArrayList<>();
		try {
			Class<Color> clazz = compileToClass(report.getColorSource(), report.getPackageName(), report.getClassName() + "Color", Color.class, errors);
			return new JavaClassFactory<>(clazz);
		} catch (Throwable e) {
			ParserError.ErrorType type = ParserError.ErrorType.JAVA_COMPILER;
			String message = e.getMessage();
			errors.add(new ParserError(type, 0, 0, 0, 0, message));
			throw new CompilerException("Can't compile color", report.getColorSource(), errors);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T> Class<T> compileToClass(String source, String packageName, String className, Class<T> clazz, List<ParserError> errors) throws CompilerException {
		logger.log(Level.FINE, "Compile Java source:\n" + source);
		List<SimpleJavaFileObject> compilationUnits = new ArrayList<>();
		compilationUnits.add(new JavaSourceFileObject(className, source));
		final List<String> options = getCompilerOptions();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		String fullClassName = packageName + "." + className;
		JavaFileManager fileManager = new JavaCompilerFileManager(compiler.getStandardFileManager(diagnostics, null, null), fullClassName);
		try {
			final JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
			if (task.call()) {
				JavaCompilerClassLoader loader = new JavaCompilerClassLoader();
				defineClasses(fileManager, loader, packageName, className);
				Class<?> compiledClazz = loader.loadClass(packageName + "." + className);
				logger.log(Level.FINE, compiledClazz.getCanonicalName());
				if (clazz.isAssignableFrom(compiledClazz)) {
					return (Class<T>) compiledClazz;
				}
			} else {
				for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
					if (diagnostic.getCode().equals("compiler.err.cant.access")) {
						// TODO Not sure why it doesn't happen with Java 8, but only with Java 9.
						ParserError.ErrorType type = ParserError.ErrorType.JAVA_COMPILER;
						long line = diagnostic.getLineNumber();
						long charPositionInLine = diagnostic.getColumnNumber();
						long index = diagnostic.getStartPosition();
						long length = diagnostic.getEndPosition() - diagnostic.getStartPosition();
						String message = diagnostic.getMessage(null);
						ParserError error = new ParserError(type, line, charPositionInLine, index, length, message);
						logger.log(Level.WARNING, error.toString());
						errors.add(error);
					} else {
						ParserError.ErrorType type = ParserError.ErrorType.JAVA_COMPILER;
						long line = diagnostic.getLineNumber();
						long charPositionInLine = diagnostic.getColumnNumber();
						long index = diagnostic.getStartPosition();
						long length = diagnostic.getEndPosition() - diagnostic.getStartPosition();
						String message = diagnostic.getMessage(null);
						ParserError error = new ParserError(type, line, charPositionInLine, index, length, message);
						logger.log(Level.FINE, error.toString());
						errors.add(error);
					}
				}
			}
		} catch (Exception e) {
			ParserError.ErrorType type = ParserError.ErrorType.JAVA_COMPILER;
			String message = e.getMessage();
			ParserError error = new ParserError(type, 0, 0, 0, 0, message);
			logger.log(Level.SEVERE, "Can't compile class", e);
			errors.add(error);
			throw new CompilerException("Can't compile class", source, errors);
		} finally {
			try {
				fileManager.close();
			} catch (IOException e) {
			}
		}
		return null;
	}

	private static List<String> getCompilerOptions() {
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
		logger.log(Level.FINE, file.toUri().toString() + " (" + fileData.length + ")");
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
