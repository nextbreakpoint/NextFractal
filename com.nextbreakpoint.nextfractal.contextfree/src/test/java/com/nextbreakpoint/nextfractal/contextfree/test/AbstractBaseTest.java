/*
 * NextFractal 2.1.3
 * https://github.com/nextbreakpoint/nextfractal
 *
 * Copyright 2015-2022 Andrea Medeghini
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
package com.nextbreakpoint.nextfractal.contextfree.test;

import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDG;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDGDriver;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDGLexer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDGParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractBaseTest {
	protected CFDG parseSource(String resourceName) throws IOException {
		ANTLRInputStream is = new ANTLRInputStream(getResourceAsStream(resourceName));
		CFDGParser parser = new CFDGParser(new CommonTokenStream(new CFDGLexer(is)));
		parser.setDriver(new CFDGDriver());
		parser.getDriver().setCurrentPath(System.getProperty("cfdg.root", "src/test/resources"));
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk(new DefaultParseTreeListener() {
			@Override
			public void visitTerminal(TerminalNode node) {
				System.out.println(node.getText() + " " + node.getSymbol());
			}
		}, parser.choose());
		return parser.getDriver().getCFDG();
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
