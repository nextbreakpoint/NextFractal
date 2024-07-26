package com.nextbreakpoint.nextfractal.core.event;

import com.nextbreakpoint.nextfractal.core.export.ExportSession;
import com.nextbreakpoint.nextfractal.core.export.ExportSessionState;
import lombok.Builder;

@Builder
public record ExportSessionStateChanged(ExportSession session, ExportSessionState state, float progress) {}
