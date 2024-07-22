package com.nextbreakpoint.nextfractal.core.event;

import com.nextbreakpoint.nextfractal.core.common.AnimationClip;
import lombok.Builder;

import java.util.List;

@Builder
public record CaptureClipsLoaded(List<AnimationClip> clips) {}
