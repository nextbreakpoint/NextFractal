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
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import static com.nextbreakpoint.nextfractal.core.common.ErrorType.COMPILE;

@Log
public class CompilerAdapter {
	private static final String PROPERTY_NEXTFRACTAL_MODULE_PATH = "com.nextbreakpoint.nextfractal.module.path";
	private static final String PROPERTY_NEXTFRACTAL_CLASS_PATH = "com.nextbreakpoint.nextfractal.class.path";

	private final JavaCompiler javaCompiler;

	public CompilerAdapter(JavaCompiler javaCompiler) {
		this.javaCompiler = Objects.requireNonNull(javaCompiler);
	}

	@SuppressWarnings("unchecked")
    public <T> ClassFactory<T> compile(Class<T> clazz, String javaSource, String packageName, String className) throws CompilerException {
		log.log(Level.FINE, "Compile Java source:\n" + javaSource);

		final String fullClassName = packageName + "." + className;

		final List<String> options = getCompilerOptions();

		final List<SimpleJavaFileObject> compilationUnits = List.of(new JavaSourceFileObject(className, javaSource));

		final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

		try (JavaFileManager fileManager = new JavaCompilerFileManager(javaCompiler.getStandardFileManager(diagnostics, null, null), fullClassName)) {
			final JavaCompiler.CompilationTask task = javaCompiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);

			if (task.call()) {
				final JavaCompilerClassLoader loader = new JavaCompilerClassLoader(clazz.getClassLoader());

				defineClasses(fileManager, loader, fullClassName);

				final Class<?> compiledClazz = loader.loadClass(fullClassName);

				log.log(Level.FINE, "Generated class name: " + compiledClazz.getCanonicalName());

				if (!clazz.isAssignableFrom(compiledClazz)) {
                    throw new CompilerException("Can't compile class", javaSource, makeError("Incompatible class"));
				}

				return new JavaClassFactory<>((Class<T>) compiledClazz);
			} else {
				throw new CompilerException("Can't compile class", javaSource, makeErrors(diagnostics));
			}
		} catch (CompilerException e) {
			throw e;
		} catch (Throwable e) {
			log.log(Level.WARNING, "Can't compile class", e);
            throw new CompilerException("Can't compile class", javaSource, makeError(e.getMessage()));
		}
	}

	private static List<String> getCompilerOptions() {
		final String modulePath = System.getProperty(PROPERTY_NEXTFRACTAL_MODULE_PATH, System.getProperty("jdk.module.path"));
		if (modulePath != null) {
			log.info("Module path = " + modulePath);
			return List.of("-source", "22", "-target", "22", "-proc:none", "--module-path", modulePath, "--add-modules", "com.nextbreakpoint.nextfractal.mandelbrot");
		} else {
			final String classPath = System.getProperty(PROPERTY_NEXTFRACTAL_CLASS_PATH);
			if (classPath != null) {
				log.info("Class path = " + classPath);
				return List.of("-source", "22", "-target", "22", "-proc:none", "--class-path", classPath);
			} else {
				return List.of("-source", "22", "-target", "22", "-proc:none");
			}
		}
	}

	private static ScriptError makeError(String message) {
		return new ScriptError(COMPILE, 0, 0, 0, 0, message);
	}

	private static ScriptError makeError(Diagnostic<? extends JavaFileObject> diagnostic) {
		final long line = diagnostic.getLineNumber();
		final long charPositionInLine = diagnostic.getColumnNumber();
		final long index = diagnostic.getStartPosition();
		final long length = diagnostic.getEndPosition() - diagnostic.getStartPosition();
		final String message = diagnostic.getMessage(null);
		return new ScriptError(COMPILE, line, charPositionInLine, index, length, message);
	}

	private static List<ScriptError> makeErrors(DiagnosticCollector<JavaFileObject> diagnostics) {
		return diagnostics.getDiagnostics().stream().map(CompilerAdapter::makeError).toList();
	}

	private static void defineClasses(JavaFileManager fileManager, JavaCompilerClassLoader loader, String fullClassName) throws IOException {
        final JavaFileObject file = fileManager.getJavaFileForOutput(StandardLocation.locationFor(fullClassName), fullClassName, Kind.CLASS, null);
		final byte[] fileData = loadBytes(file);
		log.log(Level.FINE, file.toUri().toString() + " (" + fileData.length + ")");
		loader.defineClassFromData(fullClassName, fileData);
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
