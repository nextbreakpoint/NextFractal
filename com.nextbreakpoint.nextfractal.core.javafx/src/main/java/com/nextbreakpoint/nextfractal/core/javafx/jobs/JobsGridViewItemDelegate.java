package com.nextbreakpoint.nextfractal.core.javafx.jobs;

import com.nextbreakpoint.nextfractal.core.javafx.grid.GridViewItemDelegate;

public interface JobsGridViewItemDelegate extends GridViewItemDelegate {
    void onJobUpdated(float progress);
}
