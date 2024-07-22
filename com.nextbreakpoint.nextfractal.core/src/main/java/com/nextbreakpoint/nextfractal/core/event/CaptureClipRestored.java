package com.nextbreakpoint.nextfractal.core.event;

import com.nextbreakpoint.nextfractal.core.common.AnimationClip;
import lombok.Builder;

@Builder
public record CaptureClipRestored(AnimationClip clip) {}
