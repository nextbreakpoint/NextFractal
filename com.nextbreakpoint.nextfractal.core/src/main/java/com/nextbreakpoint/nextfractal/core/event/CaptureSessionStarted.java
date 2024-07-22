package com.nextbreakpoint.nextfractal.core.event;

import com.nextbreakpoint.nextfractal.core.common.AnimationClip;
import lombok.Builder;

@Builder
public record CaptureSessionStarted(AnimationClip clip) {}
