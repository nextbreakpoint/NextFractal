/*
 * NextFractal 2.1.5
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
package com.nextbreakpoint.nextfractal.core.javafx;

import com.nextbreakpoint.Try;
import com.nextbreakpoint.nextfractal.core.encode.Encoder;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class AttributeEditorPlugins {
    private AttributeEditorPlugins() {}

    private static ServiceLoader<AttributeEditorFactory> factoryLoader() {
        return ServiceLoader.load(AttributeEditorFactory.class);
    }

    private static Stream<AttributeEditorFactory> factoryStream() {
        return StreamSupport.stream(factoryLoader().spliterator(), false);
    }

    public static Optional<AttributeEditorFactory> findFactory(String pluginId) {
        return factoryStream().filter(plugin -> pluginId.equals(plugin.getId())).findFirst();
    }

    public static Try<AttributeEditorFactory, Exception> tryFindFactory(String pluginId) {
        return findFactory(pluginId).map(plugin -> Try.of(() -> plugin)).orElse(Try.failure(new Exception("Factory not found " + pluginId)));
    }

    public static Stream<AttributeEditorFactory> factories() {
        return factoryStream();
    }
}
