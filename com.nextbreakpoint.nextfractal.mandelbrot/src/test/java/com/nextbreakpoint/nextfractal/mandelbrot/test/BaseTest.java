package com.nextbreakpoint.nextfractal.mandelbrot.test;

import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.MandelbrotLexer;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.MandelbrotParser;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTBuilder;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTFractal;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DiagnosticErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.Objects;

public abstract class BaseTest {
	protected ASTFractal parse(String source) throws Exception {
		CharStream is = CharStreams.fromReader(new StringReader(source));
		MandelbrotLexer lexer = new MandelbrotLexer(is);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		lexer.addErrorListener(new CompilerErrorListener());
		MandelbrotParser parser = new MandelbrotParser(tokens);
		parser.addErrorListener(new CompilerErrorListener());
		parser.fractal();
		ASTBuilder builder = parser.getBuilder();
        return builder.getFractal();
	}
	
	protected void printErrors(List<ScriptError> errors) {
		for (ScriptError error : errors) {
			System.out.println(error.toString());
		}
	}

	private static class CompilerErrorListener extends DiagnosticErrorListener {
		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
			System.out.println("[" + line + ":" + charPositionInLine + "] " + msg);
			super.syntaxError(recognizer, offendingSymbol, line, charPositionInLine, msg, e);
		}
	}

	protected String getSource(String name) throws IOException {
        try (InputStream is = Objects.requireNonNull(getClass().getResourceAsStream(name))) {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
        		return os.toString();
            }
        }
	}
}
