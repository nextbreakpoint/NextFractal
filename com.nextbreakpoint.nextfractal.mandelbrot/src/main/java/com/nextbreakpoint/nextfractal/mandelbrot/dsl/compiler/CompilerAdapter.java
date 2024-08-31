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

import com.nextbreakpoint.nextfractal.core.common.ClassFactory;
import com.nextbreakpoint.nextfractal.core.common.IOUtils;
import com.nextbreakpoint.nextfractal.core.common.ScriptError;
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

import static com.nextbreakpoint.nextfractal.core.common.ErrorType.COMPILE;

@Log
public class CompilerAdapter {
	private final JavaCompiler javaCompiler;

	public CompilerAdapter(JavaCompiler javaCompiler) {
		this.javaCompiler = Objects.requireNonNull(javaCompiler);
	}

	@SuppressWarnings("unchecked")
    public <T> ClassFactory<T> compile(Class<T> clazz, String javaSource, String packageName, String className) throws CompilerException {
		log.log(Level.FINE, "Compile Java source:\n" + javaSource);
		final List<SimpleJavaFileObject> compilationUnits = new ArrayList<>();
		compilationUnits.add(new JavaSourceFileObject(className, javaSource));
		final List<String> options = getCompilerOptions();
		final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
		final String fullClassName = packageName + "." + className;
		try (JavaFileManager fileManager = new JavaCompilerFileManager(javaCompiler.getStandardFileManager(diagnostics, null, null), fullClassName)) {
			final JavaCompiler.CompilationTask task = javaCompiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
			if (task.call()) {
				final JavaCompilerClassLoader loader = new JavaCompilerClassLoader();
				defineClasses(fileManager, loader, packageName, className);
				final Class<?> compiledClazz = loader.loadClass(packageName + "." + className);
				log.log(Level.FINE, compiledClazz.getCanonicalName());
				if (!clazz.isAssignableFrom(compiledClazz)) {
					final ScriptError error = new ScriptError(COMPILE, 0, 0, 0, 0, "Incompatible class");
					throw new CompilerException("Can't compile class", javaSource, List.of(error));
				}
				return new JavaClassFactory<>((Class<T>) compiledClazz);
			} else {
				for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
					final long line = diagnostic.getLineNumber();
					final long charPositionInLine = diagnostic.getColumnNumber();
					final long index = diagnostic.getStartPosition();
					final long length = diagnostic.getEndPosition() - diagnostic.getStartPosition();
					final String message = diagnostic.getMessage(null);
					final ScriptError error = new ScriptError(COMPILE, line, charPositionInLine, index, length, message);
					log.log(Level.WARNING, error.toString());
					throw new CompilerException("Can't compile class", javaSource, List.of(error));
				}
			}
		} catch (CompilerException e) {
			throw e;
		} catch (Throwable e) {
			final ScriptError error = new ScriptError(COMPILE, 0, 0, 0, 0, e.getMessage());
			throw new CompilerException("Can't compile class", javaSource, List.of(error));
		}
		final ScriptError error = new ScriptError(COMPILE, 0, 0, 0, 0, "Generic error");
		throw new CompilerException("Can't compile class", javaSource, List.of(error));
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

	private static void defineClasses(JavaFileManager fileManager, JavaCompilerClassLoader loader, String packageName, String className) throws IOException {
		final String name = packageName + "." + className;
		final JavaFileObject file = fileManager.getJavaFileForOutput(StandardLocation.locationFor(name), name, Kind.CLASS, null);
		final byte[] fileData = loadBytes(file);
		log.log(Level.FINE, file.toUri().toString() + " (" + fileData.length + ")");
		loader.defineClassFromData(name, fileData);
	}

	private static byte[] loadBytes(JavaFileObject file) throws IOException {
        try (InputStream is = file.openInputStream()) {
			try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
				IOUtils.copyBytes(is, os);
				return os.toByteArray();
			}
        }
	}
}	
