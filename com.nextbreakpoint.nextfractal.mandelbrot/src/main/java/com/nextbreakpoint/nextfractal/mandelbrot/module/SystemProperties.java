/*
 * NextFractal 2.4.0
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
package com.nextbreakpoint.nextfractal.mandelbrot.module;

public interface SystemProperties {
    String PROPERTY_MANDELBROT_COMPILER_DISABLED = "com.nextbreakpoint.nextfractal.mandelbrot.module.compiler.disabled";
    String PROPERTY_MANDELBROT_EXPRESSION_OPTIMISATION_ENABLED = "com.nextbreakpoint.nextfractal.mandelbrot.module.expression.optimisation.enabled";
    String PROPERTY_MANDELBROT_RENDERING_STRATEGY_OPTIMISATION_DISABLED = "com.nextbreakpoint.nextfractal.mandelbrot.module.rendering.strategy.optimisation.disabled";
    String PROPERTY_MANDELBROT_RENDERING_STRATEGY_VIRTUAL_THREADS_ENABLED = "com.nextbreakpoint.nextfractal.mandelbrot.module.rendering.strategy.virtual.threads.enabled";
    String PROPERTY_MANDELBROT_RENDERING_XAOS_OVERLAPPING_ENABLED = "com.nextbreakpoint.nextfractal.mandelbrot.module.rendering.xaos.overlapping.enabled";
    String PROPERTY_MANDELBROT_RENDERING_ROWS = "com.nextbreakpoint.nextfractal.mandelbrot.module.rendering.rows";
    String PROPERTY_MANDELBROT_RENDERING_COLS = "com.nextbreakpoint.nextfractal.mandelbrot.module.rendering.cols";
}
