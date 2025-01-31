package com.nextbreakpoint.nextfractal.contextfree.test;

import com.nextbreakpoint.common.command.Command;
import com.nextbreakpoint.common.either.Either;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDG;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGBuilder;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGSystem;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ContextFreeLexer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ContextFreeParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Function;

public abstract class BaseTest {
	protected CFDG parseSource(String resourceName) {
		return parseCFDG3(resourceName).or(() -> parseCFDG2(resourceName)).optional().orElseThrow();
	}

	private Either<CFDG> parseCFDG3(String resourceName) {
		return Command.of(() -> parseSource(resourceName, "CFDG3", ContextFreeParser::cfdg3))
				.execute().observe().onFailure(System.out::println).get();
	}

	private Either<CFDG> parseCFDG2(String resourceName) {
		return Command.of(() -> parseSource(resourceName, "CFDG2", ContextFreeParser::cfdg2))
				.execute().observe().onFailure(System.out::println).get();
	}

	private CFDG parseSource(String resourceName, String version, Function<ContextFreeParser, ParseTree> callback) throws IOException {
		CFDG cfdg = new CFDG(new CFDGSystem(), version);
		CFDGBuilder builder = new CFDGBuilder(cfdg, 0);
		CharStream is = CharStreams.fromReader(new InputStreamReader(getResourceAsStream(resourceName)));
		ContextFreeParser parser = new ContextFreeParser(new CommonTokenStream(new ContextFreeLexer(is)));
		parser.setBuilder(builder);
		parser.getBuilder().setCurrentPath(System.getProperty("cfdg.root", "src/test/resources"));
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk(new DefaultParseTreeListener() {
			@Override
			public void visitTerminal(TerminalNode node) {
				System.out.println(node.getText() + " " + node.getSymbol());
			}
		}, callback.apply(parser));
		if (builder.getMaybeVersion() != null && !version.equals(builder.getMaybeVersion())) {
			throw new RuntimeException("Unexpected version");
		}
		return parser.getBuilder().getCfdg();
	}

	protected InputStream getResourceAsStream(String resourceName) {
		return getClass().getResourceAsStream(resourceName);
	}

	protected static class DefaultParseTreeListener implements ParseTreeListener {
		@Override
        public void visitTerminal(TerminalNode node) {
        }

		@Override
        public void visitErrorNode(ErrorNode node) {
        }

		@Override
        public void exitEveryRule(ParserRuleContext ctx) {
        }

		@Override
        public void enterEveryRule(ParserRuleContext ctx) {
        }
	}

//	public void renderImage() throws Exception {
//		ContextFreeRuntime runtime = new ContextFreeRuntime(config);
//		ContextFreeRenderer renderer = new DefaultContextFreeRenderer(Thread.MIN_PRIORITY);
//		IntegerVector2D imageSize = new IntegerVector2D(IMAGE_WIDTH, IMAGE_HEIGHT);
//		IntegerVector2D nullSize = new IntegerVector2D(0, 0);
//		Tile tile = new Tile(imageSize, imageSize, nullSize, nullSize);
//		renderer.setTile(tile);
//		IntegerVector2D bufferSize = new IntegerVector2D(tile.getTileSize().getX() + tile.getTileBorder().getX() * 2, tile.getTileSize().getY() + tile.getTileBorder().getY() * 2);
//		Surface surface = new Surface(bufferSize.getX(), bufferSize.getY());
//		renderer.setRuntime(runtime);
//		renderer.start();
//		try {
//			renderer.startRenderer();
//			renderer.joinRenderer();
//		}
//		catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		Graphics2D g2d = surface.getGraphics2D();
//		renderer.drawImage(g2d);
//		g2d.setColor(Color.WHITE);
//		g2d.drawRect(0, 0, surface.getWidth() - 1, surface.getHeight() - 1);
//		ImageIO.write(surface.getImage(), "png", new File(System.getProperty("cfdgFile").replace(".cfdg", ".png")));
//		renderer.stop();
//		renderer.dispose();
//		rootNode.dispose();
//		runtime.dispose();
//		config.dispose();
//		surface.dispose();
//	}
}
