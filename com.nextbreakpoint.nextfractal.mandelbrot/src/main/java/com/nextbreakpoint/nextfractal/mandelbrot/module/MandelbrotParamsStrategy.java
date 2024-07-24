package com.nextbreakpoint.nextfractal.mandelbrot.module;

import com.nextbreakpoint.nextfractal.core.common.Double2D;
import com.nextbreakpoint.nextfractal.core.common.Double4D;
import com.nextbreakpoint.nextfractal.core.common.ParamsStrategy;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.common.Time;
import com.nextbreakpoint.nextfractal.core.params.Attribute;
import com.nextbreakpoint.nextfractal.core.params.Group;
import com.nextbreakpoint.nextfractal.core.params.Parameters;

import java.util.List;

public class MandelbrotParamsStrategy implements ParamsStrategy {
    @Override
    public Parameters create(Session session) {
        return Parameters.builder().withGroups(getGroups()).build();
    }

    private static List<Group> getGroups() {
        return List.of(
                Group.builder()
                        .withName("Center")
                        .withAttributes(getCenterAttributes())
                        .build(),
                Group.builder()
                        .withName("Distance")
                        .withAttributes(getDistanceAttributes())
                        .build(),
                Group.builder()
                        .withName("Rotation")
                        .withAttributes(getRotationAttributes())
                        .build(),
                Group.builder()
                        .withName("Constant Point (variable w)")
                        .withAttributes(getInitialWAttributes())
                        .build(),
                Group.builder()
                        .withName("Initial State (variable x)")
                        .withAttributes(getInitialXAttributes())
                        .build(),
                Group.builder()
                        .withName("Algorithm")
                        .withAttributes(getAlgorithmAttributes())
                        .build(),
                Group.builder()
                        .withName("Time")
                        .withAttributes(getTimeAttributes())
                        .build()
        );
    }

    private static List<Attribute> getCenterAttributes() {
        return List.of(
                Attribute.builder()
                        .withName("X axis value")
                        .withKey("translation-x")
                        .withLogicalType("double")
                        .withMapper(session -> String.valueOf(((MandelbrotMetadata) session.metadata()).getTranslation().x()))
                        .withCombiner((session, value)  -> getSessionBuilder(session).withMetadata(getMetadataBuilder(session).withTranslation(getTranslationBuilder(session).withX(Double.parseDouble(value)).build()).build()).build())
                        .build(),
                Attribute.builder()
                        .withName("Y axis value")
                        .withKey("translation-y")
                        .withLogicalType("double")
                        .withMapper(session -> String.valueOf(((MandelbrotMetadata) session.metadata()).getTranslation().y()))
                        .withCombiner((session, value)  -> getSessionBuilder(session).withMetadata(getMetadataBuilder(session).withTranslation(getTranslationBuilder(session).withY(Double.parseDouble(value)).build()).build()).build())
                        .build()
        );
    }

    private static List<Attribute> getDistanceAttributes() {
        return List.of(
                Attribute.builder().withName("Z axis value")
                        .withKey("translation-z")
                        .withLogicalType("double")
                        .withMapper(session -> String.valueOf(((MandelbrotMetadata) session.metadata()).getTranslation().z()))
                        .withCombiner((session, value)  -> getSessionBuilder(session).withMetadata(getMetadataBuilder(session).withTranslation(getTranslationBuilder(session).withZ(Double.parseDouble(value)).build()).build()).build())
                        .build()
        );
    }

    private static List<Attribute> getRotationAttributes() {
        return List.of(
                Attribute.builder()
                        .withName("Z axis rotation in degrees")
                        .withKey("rotation-z")
                        .withLogicalType("double")
                        .withMapper(session -> String.valueOf(((MandelbrotMetadata) session.metadata()).getRotation().z()))
                        .withCombiner((session, value)  -> getSessionBuilder(session).withMetadata(getMetadataBuilder(session).withRotation(getRotationBuilder(session).withZ(Double.parseDouble(value)).build()).build()).build())
                        .build()
        );
    }

    private static List<Attribute> getInitialWAttributes() {
        return List.of(
                Attribute.builder()
                        .withName("Real part of constant point w")
                        .withKey("constant-r")
                        .withLogicalType("double")
                        .withMapper(session -> String.valueOf(((MandelbrotMetadata) session.metadata()).getPoint().x()))
                        .withCombiner((session, value)  -> getSessionBuilder(session).withMetadata(getMetadataBuilder(session).withPoint(getPointBuilder(session).withX(Double.parseDouble(value)).build()).build()).build())
                        .build(),
                Attribute.builder()
                        .withName("Imaginary part of constant point w")
                        .withKey("constant-i")
                        .withLogicalType("double")
                        .withMapper(session -> String.valueOf(((MandelbrotMetadata) session.metadata()).getPoint().y()))
                        .withCombiner((session, value)  -> getSessionBuilder(session).withMetadata(getMetadataBuilder(session).withPoint(getPointBuilder(session).withY(Double.parseDouble(value)).build()).build()).build())
                        .build()
        );
    }

    private static List<Attribute> getInitialXAttributes() {
        return List.of(
                Attribute.builder()
                        .withName("Real part of initial state x")
                        .withKey("state-r")
                        .withLogicalType("double")
                        .withMapper(session -> String.valueOf(0.0d))
                        .withCombiner((session, value)  -> session)
                        .withReadOnly(true)
                        .build(),
                Attribute.builder()
                        .withName("Imaginary part of initial state x")
                        .withKey("state-i")
                        .withLogicalType("double")
                        .withMapper(session -> String.valueOf(0.0d))
                        .withCombiner((session, value)  -> session)
                        .withReadOnly(true)
                        .build()
        );
    }

    private static List<Attribute> getAlgorithmAttributes() {
        return List.of(
                Attribute.builder()
                        .withName("Algorithm variant")
                        .withKey("mandelbrot-algorithm")
                        .withLogicalType("string")
                        .withMapper(session -> ((MandelbrotMetadata) session.metadata()).isJulia() ? "Julia/Fatou" : "Mandelbrot")
                        .withCombiner((session, value)  -> getSessionBuilder(session).withMetadata(getMetadataBuilder(session).withJulia(!value.equalsIgnoreCase("mandelbrot")).build()).build())
                        .build()
        );
    }

    private static List<Attribute> getTimeAttributes() {
        return List.of(
                Attribute.builder()
                        .withName("Time in seconds")
                        .withKey("time-value")
                        .withLogicalType("double")
                        .withMapper(session -> String.valueOf(session.metadata().time().value()))
                        .withCombiner((session, value)  -> getSessionBuilder(session).withMetadata(getMetadataBuilder(session).withTime(getTimeBuilder(session).withValue(Double.parseDouble(value)).build()).build()).build())
                        .build(),
                Attribute.builder()
                        .withName("Time animation speed")
                        .withKey("time-animation-speed")
                        .withLogicalType("double")
                        .withMapper(session -> String.valueOf(session.metadata().time().scale()))
                        .withCombiner((session, value)  -> getSessionBuilder(session).withMetadata(getMetadataBuilder(session).withTime(getTimeBuilder(session).withScale(Double.parseDouble(value)).build()).build()).build())
                        .build()
        );
    }

    private static Time.TimeBuilder getTimeBuilder(Session session) {
        return session.metadata().time().toBuilder();
    }

    private static Double2D.Double2DBuilder getPointBuilder(Session session) {
        return ((MandelbrotMetadata) session.metadata()).getPoint().toBuilder();
    }

    private static Double4D.Double4DBuilder getTranslationBuilder(Session session) {
        return ((MandelbrotMetadata) session.metadata()).getTranslation().toBuilder();
    }

    private static Double4D.Double4DBuilder getRotationBuilder(Session session) {
        return ((MandelbrotMetadata) session.metadata()).getRotation().toBuilder();
    }

    private static MandelbrotMetadata.MandelbrotMetadataBuilder getMetadataBuilder(Session session) {
        return ((MandelbrotMetadata) session.metadata()).toBuilder();
    }

    private static MandelbrotSession.MandelbrotSessionBuilder getSessionBuilder(Session session) {
        return ((MandelbrotSession) session).toBuilder();
    }
}
