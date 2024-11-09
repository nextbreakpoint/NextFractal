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

import com.nextbreakpoint.nextfractal.core.common.ParamsStrategy;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.params.Attribute;
import com.nextbreakpoint.nextfractal.core.params.Group;
import com.nextbreakpoint.nextfractal.core.params.Parameters;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

public class ContextFreeParamsStrategy implements ParamsStrategy {
    @Override
    public Parameters create(Session session) {
        return Parameters.builder().withGroups(getGroups()).build();
    }

    private static List<Group> getGroups() {
        return List.of(
                Group.builder()
                        .withName("Random numbers generator")
                        .withAttributes(getRandomSeedAttribute())
                        .build()
                );
    }

    private static List<Attribute> getRandomSeedAttribute() {
        return List.of(
                Attribute.builder()
                        .withName("Random seed")
                        .withKey("contextfree-seed")
                        .withLogicalType("string")
                        .withMapper(session -> String.valueOf(((ContextFreeMetadata) session.metadata()).getSeed()))
                        .withCombiner((session, value)  -> getSessionBuilder(session).withTimestamp(Instant.now(Clock.systemUTC())).withMetadata(getMetadataBuilder(session).withSeed(value).build()).build())
                        .build()
        );
    }

    private static ContextFreeMetadata.ContextFreeMetadataBuilder getMetadataBuilder(Session session) {
        return ((ContextFreeMetadata) session.metadata()).toBuilder();
    }

    private static ContextFreeSession.ContextFreeSessionBuilder getSessionBuilder(Session session) {
        return ((ContextFreeSession) session).toBuilder();
    }
}
