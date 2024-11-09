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
package com.nextbreakpoint.nextfractal.contextfree.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextbreakpoint.nextfractal.core.common.Metadata;
import com.nextbreakpoint.nextfractal.core.common.MetadataCodec;

public class ContextFreeMetadataCodec implements MetadataCodec {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Metadata decodeMetadata(String metadata) throws Exception {
        return MAPPER.readValue(metadata, ContextFreeMetadata.class);
    }

    @Override
    public String encodeMetadata(Object metadata) throws Exception {
        if (metadata instanceof ContextFreeMetadata) {
            return MAPPER.writeValueAsString(metadata);
        } else {
            throw new IllegalStateException("Unexpected class: " + metadata.getClass());
        }
    }
}
