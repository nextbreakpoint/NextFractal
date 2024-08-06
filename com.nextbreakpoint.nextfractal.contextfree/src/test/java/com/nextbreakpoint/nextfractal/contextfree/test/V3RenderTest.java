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
package com.nextbreakpoint.nextfractal.contextfree.test;

import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDG;
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFDGImage;
import com.nextbreakpoint.nextfractal.contextfree.graphics.Renderer;
import com.nextbreakpoint.nextfractal.core.common.DefaultThreadFactory;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsUtils;
import com.nextbreakpoint.nextfractal.core.graphics.Point;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import com.nextbreakpoint.nextfractal.core.graphics.Tile;
import com.nextbreakpoint.nextfractal.core.graphics.internal.Java2DGraphicsFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class V3RenderTest extends AbstractBaseTest {
	public static Stream<Arguments> parameters() {
		return Stream.of(
			Arguments.of("/v3-shape-square.cfdg", "/v3-shape-square.png"),
			Arguments.of("/v3-shape-triangle.cfdg", "/v3-shape-triangle.png"),
			Arguments.of("/v3-shape-circle.cfdg", "/v3-shape-circle.png"),
			Arguments.of("/v3-shape-transform.cfdg", "/v3-shape-transform.png"),
			Arguments.of("/v3-shape-multiple-primitives.cfdg", "/v3-shape-multiple-primitives.png"),
			Arguments.of("/v3-shape-initial-adjustment.cfdg", "/v3-shape-initial-adjustment.png"),
			Arguments.of("/v3-shape-options.cfdg", "/v3-shape-options.png"),
			Arguments.of("/v3-shapes-blah.cfdg", "/v3-shapes-blah.png"),
			Arguments.of("/v3-shapes-blah-random.cfdg", "/v3-shapes-blah-random.png"),
			Arguments.of("/v3-shape-variable.cfdg", "/v3-shape-variable.png"),
			Arguments.of("/v3-shape-function.cfdg", "/v3-shape-function.png"),
			Arguments.of("/v3-shape-path.cfdg", "/v3-shape-path.png"),
			Arguments.of("/v3-shape-loop.cfdg", "/v3-shape-loop.png"),
			Arguments.of("/v3-shape-if.cfdg", "/v3-shape-if.png"),
			Arguments.of("/v3-shape-path2.cfdg", "/v3-shape-path2.png"),
			Arguments.of("/v3-shape-switch.cfdg", "/v3-shape-switch.png"),
			Arguments.of("/v3-shape-trans.cfdg", "/v3-shape-trans.png"),
			Arguments.of("/v3-shape-parameters.cfdg", "/v3-shape-parameters.png"),
			Arguments.of("/v3-shape-include.cfdg", "/v3-shape-include.png"),
			Arguments.of("/v3-shape-tile.cfdg", "/v3-shape-tile.png"),
			Arguments.of("/v3-shape-size.cfdg", "/v3-shape-size.png"),
			Arguments.of("/v3-shape-clone.cfdg", "/v3-shape-clone.png"),
			Arguments.of("/v3-shape-symmetry-dihedral.cfdg", "/v3-shape-symmetry-dihedral.png"),
			Arguments.of("/v3-shape-symmetry-cyclic.cfdg", "/v3-shape-symmetry-cyclic.png"),
			Arguments.of("/v3-shape-symmetry-p11g.cfdg", "/v3-shape-symmetry-p11g.png"),
			Arguments.of("/v3-shape-symmetry-p11m.cfdg", "/v3-shape-symmetry-p11m.png"),
			Arguments.of("/v3-shape-symmetry-p1m1.cfdg", "/v3-shape-symmetry-p1m1.png"),
			Arguments.of("/v3-shape-symmetry-p2.cfdg", "/v3-shape-symmetry-p2.png"),
			Arguments.of("/v3-shape-symmetry-p2mg.cfdg", "/v3-shape-symmetry-p2mg.png"),
			Arguments.of("/v3-shape-symmetry-pm.cfdg", "/v3-shape-symmetry-pm.png"),
			Arguments.of("/v3-shape-symmetry-pg.cfdg", "/v3-shape-symmetry-pg.png"),
			Arguments.of("/v3-shape-symmetry-cm.cfdg", "/v3-shape-symmetry-cm.png")
////		Arguments.of("/v3-shape-symmetry-pmm.cfdg", "/v3-shape-symmetry-pmm.png"),
////		Arguments.of("/v3-shape-symmetry-pmg.cfdg", "/v3-shape-symmetry-pmg.png"),
////		Arguments.of("/v3-shape-symmetry-pgg.cfdg", "/v3-shape-symmetry-pgg.png"),
////		Arguments.of("/v3-shape-symmetry-cmm.cfdg", "/v3-shape-symmetry-cmm.png"),
////		Arguments.of("/v3-shape-symmetry-p4m.cfdg", "/v3-shape-symmetry-p4.png"),
////		Arguments.of("/v3-shape-symmetry-p4m.cfdg", "/v3-shape-symmetry-p4m.png"),
////		Arguments.of("/v3-shape-symmetry-p4g.cfdg", "/v3-shape-symmetry-p4g.png"),
////		Arguments.of("/v3-shape-symmetry-p3.cfdg", "/v3-shape-symmetry-p3.png")
		);
	}

	@ParameterizedTest
	@MethodSource("parameters")
	public void shouldRenderImage(String sourceName, String imageName) throws IOException {
		System.out.println(sourceName);

		BufferedImage actualImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);

		DefaultThreadFactory threadFactory = new DefaultThreadFactory("Generator", true, Thread.MIN_PRIORITY);
		GraphicsFactory rendererFactory = GraphicsUtils.findGraphicsFactory("Java2D");

		Tile tile = new Tile(new Size(200, 200), new Size(200, 200), new Point(0, 0), new Size(0, 0));
		Renderer renderer = new Renderer(threadFactory, rendererFactory, tile);

		CFDG cfdg = parseSource(sourceName);

		renderer.setOpaque(true);
		renderer.setInterpreter(new CFDGImage(cfdg));
		renderer.setSeed("ABCD");
		renderer.init();
		renderer.runTask();
		renderer.waitForTasks();

		renderer.drawImage(rendererFactory.createGraphicsContext(actualImage.createGraphics()), 0, 0);

		saveImage("tmp" + imageName, actualImage);

		BufferedImage expectedImage = loadImage(imageName);
		assertThat(compareImages(expectedImage, actualImage)).isEqualTo(0.0);
	}

	private double compareImages(BufferedImage expectedImage, BufferedImage actualImage) {
		int[] expexctedPixels = new int[expectedImage.getWidth() * expectedImage.getHeight()];
		int[] actualPixels = new int[actualImage.getWidth() * actualImage.getHeight()];
		expectedImage.getRGB(0, 0, expectedImage.getWidth(), expectedImage.getHeight(), expexctedPixels, 0, expectedImage.getWidth());
		actualImage.getRGB(0, 0, actualImage.getWidth(), actualImage.getHeight(), actualPixels, 0, actualImage.getWidth());
		return error(convertFormat(expexctedPixels), convertFormat(actualPixels));
	}

	private byte[] convertFormat(int[] data) {
		byte[] buffer = new byte[data.length * 4];
		for (int j = 0; j < data.length; j += 1) {
			buffer[j * 4 + 0] = (byte)(data[j] >> 24);
			buffer[j * 4 + 1] = (byte)(data[j] >> 16);
			buffer[j * 4 + 2] = (byte)(data[j] >> 8);
			buffer[j * 4 + 3] = (byte)(data[j] >> 0);
		}
		return buffer;
	}

	private double error(byte[] data1, byte[] data2) {
		double error = 0;
		for (int j = 0; j < data1.length; j += 4) {
			error += distance(data1, data2, j);
		}
		return error / (data1.length / 4);
	}

	private double distance(byte[] data1, byte[] data2, int i) {
		return Math.sqrt(Math.pow(data1[i + 0] - data2[i + 0], 2) + Math.pow(data1[i + 1] - data2[i + 1], 2) + Math.pow(data1[i + 2] - data2[i + 2], 2) + Math.pow(data1[i + 3] - data2[i + 3], 2));
	}

	private void saveImage(String imageName, BufferedImage image) throws IOException {
		File file = new File(imageName);
		file.mkdirs();
		System.out.println(file.getAbsoluteFile());
		ImageIO.write(image, "png", file);
	}

	private BufferedImage loadImage(String imageName) throws IOException {
		return ImageIO.read(getResourceAsStream(imageName));
	}
}
