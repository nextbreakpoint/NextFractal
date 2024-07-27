package com.nextbreakpoint.nextfractal.core.event;

import com.nextbreakpoint.nextfractal.core.graphics.Size;
import lombok.Builder;

@Builder
public record SessionExportRequested(Size size, String format) {}
