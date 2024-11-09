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
package com.nextbreakpoint.nextfractal.core.javafx.grid.internal;

import com.nextbreakpoint.nextfractal.core.javafx.grid.GridView;
import com.nextbreakpoint.nextfractal.core.javafx.grid.GridViewItem;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.FocusModel;

import java.lang.ref.WeakReference;
import java.util.List;

public class GridViewFocusModel<T extends GridViewItem> extends FocusModel<T> {

    private final GridView<T> gridView;
    private int itemCount = 0;

    public GridViewFocusModel(final GridView<T> gridView) {
        if (gridView == null) {
            throw new IllegalArgumentException("ListView can not be null");
        }

        this.gridView = gridView;

        itemsObserver = new InvalidationListener() {
            private WeakReference<ObservableList<T>> weakItemsRef = new WeakReference<>(gridView.getItems());

            @Override
            public void invalidated(Observable observable) {
                ObservableList<T> oldItems = weakItemsRef.get();
                weakItemsRef = new WeakReference<>(gridView.getItems());
                updateItemsObserver(oldItems, gridView.getItems());
            }
        };
        this.gridView.getItems().addListener(new WeakInvalidationListener(itemsObserver));
        if (gridView.getItems() != null) {
            this.gridView.getItems().addListener(weakItemsContentListener);
        }

        updateItemCount();
        updateDefaultFocus();

        focusedIndexProperty().addListener(_ -> gridView.notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_ITEM));
    }


    private void updateItemsObserver(ObservableList<T> oldList, ObservableList<T> newList) {
        // the listview items list has changed, we need to observe
        // the new list, and remove any observer we had from the old list
        if (oldList != null) oldList.removeListener(weakItemsContentListener);
        if (newList != null) newList.addListener(weakItemsContentListener);

        updateItemCount();
        updateDefaultFocus();
    }

    private final InvalidationListener itemsObserver;

    // Listen to changes in the listview items list, such that when it
    // changes we can update the focused index to refer to the new indices.
    private final ListChangeListener<T> itemsContentListener = c -> {
        updateItemCount();

        while (c.next()) {
            // looking at the first change
            int from = c.getFrom();

            if (c.wasReplaced() || c.getAddedSize() == getItemCount()) {
                updateDefaultFocus();
                return;
            }

            if (getFocusedIndex() == -1 || from > getFocusedIndex()) {
                return;
            }

            c.reset();
            boolean added = false;
            boolean removed = false;
            int addedSize = 0;
            int removedSize = 0;
            while (c.next()) {
                added |= c.wasAdded();
                removed |= c.wasRemoved();
                addedSize += c.getAddedSize();
                removedSize += c.getRemovedSize();
            }

            if (added && !removed) {
                focus(Math.min(getItemCount() - 1, getFocusedIndex() + addedSize));
            } else if (!added && removed) {
                focus(Math.max(0, getFocusedIndex() - removedSize));
            }
        }
    };

    private WeakListChangeListener<T> weakItemsContentListener
            = new WeakListChangeListener<>(itemsContentListener);

    @Override
    protected int getItemCount() {
        return itemCount;
    }

    @Override
    protected T getModelItem(int index) {
        if (isEmpty()) return null;
        if (index < 0 || index >= itemCount) return null;

        return gridView.getItems().get(index);
    }

    private boolean isEmpty() {
        return itemCount == -1;
    }

    private void updateItemCount() {
        if (gridView == null) {
            itemCount = -1;
        } else {
            List<T> items = gridView.getItems();
            itemCount = items == null ? -1 : items.size();
        }
    }

    private void updateDefaultFocus() {
        // when the items list totally changes, we should clear out
        // the focus
        int newValueIndex = -1;
        if (gridView.getItems() != null) {
            T focusedItem = getFocusedItem();
            if (focusedItem != null) {
                newValueIndex = gridView.getItems().indexOf(focusedItem);
            }

            // we put focus onto the first item, if there is at least
            // one item in the list
            if (newValueIndex == -1) {
                newValueIndex = !gridView.getItems().isEmpty() ? 0 : -1;
            }
        }

        focus(newValueIndex);
    }
}
