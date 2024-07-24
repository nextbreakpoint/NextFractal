package com.nextbreakpoint.nextfractal.contextfree.module;

import com.nextbreakpoint.nextfractal.core.common.ParamsStrategy;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.params.Attribute;
import com.nextbreakpoint.nextfractal.core.params.Group;
import com.nextbreakpoint.nextfractal.core.params.Parameters;

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
                        .withCombiner((session, value)  -> getSessionBuilder(session).withMetadata(getMetadataBuilder(session).withSeed(value).build()).build())
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
