package com.nextbreakpoint.nextfractal.core.javafx.grid.internal;

import com.nextbreakpoint.nextfractal.core.javafx.grid.GridView;
import com.nextbreakpoint.nextfractal.core.javafx.grid.GridViewItem;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;

import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static javafx.scene.control.SelectionMode.SINGLE;

public abstract class GridViewSelectionModel<T extends GridViewItem> extends MultipleSelectionModel<T> {
    private final SelectedIndicesList selectedIndices;
    private final ObservableList<T> selectedItems;
    private final GridView<T> gridView;

    public GridViewSelectionModel(GridView<T> gridView) {
        this.gridView = gridView;

        selectedIndices = new SelectedIndicesList();

        selectedItems = new SelectedItemsReadOnlyObservableList<>(selectedIndices, this::getItemCount) {
            @Override
            protected T getModelItem(int index) {
                return GridViewSelectionModel.this.getModelItem(index);
            }
        };
    }

    protected abstract T getModelItem(int index);

    protected abstract Integer getItemCount();

    void startAtomic() {
        selectedIndices.startAtomic();
    }

    void stopAtomic() {
        selectedIndices.stopAtomic();
    }

    boolean isAtomic() {
        return selectedIndices.isAtomic();
    }

    @Override
    public ObservableList<Integer> getSelectedIndices() {
        return selectedIndices;
    }

    @Override
    public ObservableList<T> getSelectedItems() {
        return selectedItems;
    }

    @Override
    public void clearAndSelect(int row) {
        if (row < 0 || row >= getItemCount()) {
            clearSelection();
            return;
        }

        final boolean wasSelected = isSelected(row);

        // RT-33558 if this method has been called with a given row, and that
        // row is the only selected row currently, then this method becomes a no-op.
        if (wasSelected && getSelectedIndices().size() == 1) {
            // before we return, we double-check that the selected item
            // is equal to the item in the given index
            if (getSelectedItem() == getModelItem(row)) {
                return;
            }
        }

        // firstly we make a copy of the selection, so that we can send out
        // the correct details in the selection change event.
        // We remove the new selection from the list seeing as it is not removed.
        BitSet selectedIndicesCopy = new BitSet();
        selectedIndicesCopy.or(selectedIndices.bitset);
        selectedIndicesCopy.clear(row);
        // No modifications should be made to 'selectedIndicesCopy' to honour the constructor.
        List<Integer> previousSelectedIndices = new SelectedIndicesList(selectedIndicesCopy);

        // RT-32411 We used to call quietClearSelection() here, but this
        // resulted in the selectedItems and selectedIndices lists never
        // reporting that they were empty.
        // makeAtomic toggle added to resolve RT-32618
        startAtomic();

        // then clear the current selection
        clearSelection();

        // and select the new row
        select(row);
        stopAtomic();

        // fire off a single add/remove/replace notification (rather than
        // individual remove and add notifications) - see RT-33324
        ListChangeListener.Change<Integer> change;

        /*
         * getFrom() documentation:
         *   If wasAdded is true, the interval contains all the values that were added.
         *   If wasPermutated is true, the interval marks the values that were permutated.
         *   If wasRemoved is true and wasAdded is false, getFrom() and getTo() should
         *   return the same number - the place where the removed elements were positioned in the list.
         */
        if (wasSelected) {
            change = buildClearAndSelectChange(
                    selectedIndices, previousSelectedIndices, row, Comparator.naturalOrder());
        } else {
            int changeIndex = Math.max(0, selectedIndices.indexOf(row));
            change = new NonIterableChange.GenericAddRemoveChange<>(
                    changeIndex, changeIndex + 1, previousSelectedIndices, selectedIndices);
        }

        selectedIndices.callObservers(change);
    }

    @Override
    public void select(int row) {
        if (row == -1) {
            clearSelection();
            return;
        }
        if (row < 0 || row >= getItemCount()) {
            return;
        }

        boolean isSameRow = row == getSelectedIndex();
        T currentItem = getSelectedItem();
        T newItem = getModelItem(row);
        boolean isSameItem = newItem != null && newItem.equals(currentItem);
        boolean fireUpdatedItemEvent = isSameRow && !isSameItem;

        // focus must come first so that we have the anchors set appropriately
        gridView.getFocusModel().focus(row);

        if (!selectedIndices.isSelected(row)) {
            if (getSelectionMode() == SINGLE) {
                startAtomic();
                quietClearSelection();
                stopAtomic();
            }
            selectedIndices.set(row);
        }

        setSelectedIndex(row);

        if (fireUpdatedItemEvent) {
            setSelectedItem(newItem);
        }
    }

    @Override
    public void select(T obj) {
//        if (getItemCount() <= 0) return;

        if (obj == null && getSelectionMode() == SelectionMode.SINGLE) {
            clearSelection();
            return;
        }

        // We have no option but to iterate through the model and select the
        // first occurrence of the given object. Once we find the first one, we
        // don't proceed to select any others.
        Object rowObj = null;
        for (int i = 0, max = getItemCount(); i < max; i++) {
            rowObj = getModelItem(i);
            if (rowObj == null) continue;

            if (rowObj.equals(obj)) {
                if (isSelected(i)) {
                    return;
                }

                if (getSelectionMode() == SINGLE) {
                    quietClearSelection();
                }

                select(i);
                return;
            }
        }

        // if we are here, we did not find the item in the entire data model.
        // Even still, we allow for this item to be set to the give object.
        // We expect that in concrete subclasses of this class we observe the
        // data model such that we check to see if the given item exists in it,
        // whilst SelectedIndex == -1 && SelectedItem != null.
        setSelectedIndex(-1);
        setSelectedItem(obj);
    }

    @Override
    public void selectIndices(int row, int... rows) {
        if (rows == null || rows.length == 0) {
            select(row);
            return;
        }

        /*
         * Performance optimisation - if multiple selection is disabled, only
         * process the end-most row index.
         */

        int rowCount = getItemCount();

        if (getSelectionMode() == SINGLE) {
            quietClearSelection();

            for (int i = rows.length - 1; i >= 0; i--) {
                int index = rows[i];
                if (index >= 0 && index < rowCount) {
                    selectedIndices.set(index);
                    select(index);
                    break;
                }
            }

            if (selectedIndices.isEmpty()) {
                if (row > 0 && row < rowCount) {
                    selectedIndices.set(row);
                    select(row);
                }
            }
        } else {
            selectedIndices.set(row, rows);

            IntStream.concat(IntStream.of(row), IntStream.of(rows))
                    .filter(index -> index >= 0 && index < rowCount)
                    .reduce((first, second) -> second)
                    .ifPresent(lastIndex -> {
                        setSelectedIndex(lastIndex);
                        gridView.getFocusModel().focus(lastIndex);
                        setSelectedItem(getModelItem(lastIndex));
                    });
        }
    }

    @Override
    public void selectAll() {
        if (getSelectionMode() == SINGLE) return;

        if (getItemCount() <= 0) return;

        final int rowCount = getItemCount();
        final int focusedIndex = gridView.getFocusModel().getFocusedIndex();

        // set all selected indices to true
        clearSelection();
        selectedIndices.set(0, rowCount, true);

        if (focusedIndex == -1) {
            setSelectedIndex(rowCount - 1);
            gridView.getFocusModel().focus(rowCount - 1);
        } else {
            setSelectedIndex(focusedIndex);
            gridView.getFocusModel().focus(focusedIndex);
        }
    }

    @Override
    public void selectFirst() {
        if (getSelectionMode() == SINGLE) {
            quietClearSelection();
        }

        if (getItemCount() > 0) {
            select(0);
        }
    }

    @Override
    public void selectLast() {
        if (getSelectionMode() == SINGLE) {
            quietClearSelection();
        }

        int numItems = getItemCount();
        if (numItems > 0 && getSelectedIndex() < numItems - 1) {
            select(numItems - 1);
        }
    }

    @Override
    public void clearSelection(int index) {
        if (index < 0) return;

        // TODO shouldn't directly access like this
        // TODO might need to update focus and / or selected index/item
        boolean wasEmpty = selectedIndices.isEmpty();
        selectedIndices.clear(index);

        if (!wasEmpty && selectedIndices.isEmpty()) {
            clearSelection();
        }
    }

    @Override
    public void clearSelection() {
        quietClearSelection();

        if (!isAtomic()) {
            setSelectedIndex(-1);
            gridView.getFocusModel().focus(-1);
        }
    }

    private void quietClearSelection() {
        selectedIndices.clear();
    }

    @Override
    public boolean isSelected(int index) {
        // Note the change in semantics here - we used to check to ensure that
        // the index is less than the item count, but now simply ensure that
        // it is less than the length of the selectedIndices bitset. This helps
        // to resolve issues such as RT-26721, where isSelected(int) was being
        // called for indices that exceeded the item count, as a TreeItem (e.g.
        // the root) was being collapsed.
//        if (index >= 0 && index < getItemCount()) {
        if (index >= 0 && index < selectedIndices.bitsetSize()) {
            return selectedIndices.isSelected(index);
        }

        return false;
    }

    @Override
    public boolean isEmpty() {
        return selectedIndices.isEmpty();
    }

    @Override
    public void selectPrevious() {
        int focusIndex = gridView.getFocusModel().getFocusedIndex();

        if (getSelectionMode() == SINGLE) {
            quietClearSelection();
        }

        if (focusIndex == -1) {
            select(getItemCount() - 1);
        } else if (focusIndex > 0) {
            select(focusIndex - 1);
        }
    }

    @Override
    public void selectNext() {
        int focusIndex = gridView.getFocusModel().getFocusedIndex();

        if (getSelectionMode() == SINGLE) {
            quietClearSelection();
        }

        if (focusIndex == -1) {
            select(0);
        } else if (focusIndex != getItemCount() - 1) {
            select(focusIndex + 1);
        }
    }

    private static <T> ListChangeListener.Change<T> buildClearAndSelectChange(ObservableList<T> list, List<T> removed, T retainedRow, Comparator<T> rowComparator) {
        return new ListChangeListener.Change<T>(list) {
            private final int[] EMPTY_PERM = new int[0];

            private final int removedSize = removed.size();

            private final List<T> firstRemovedRange;
            private final List<T> secondRemovedRange;

            private boolean invalid = true;
            private boolean atFirstRange = true;

            private int from = -1;

            {
                int insertionPoint = Collections.binarySearch(removed, retainedRow, rowComparator);
                if (insertionPoint >= 0) {
                    firstRemovedRange = removed;
                    secondRemovedRange = Collections.emptyList();
                } else {
                    int midIndex = -insertionPoint - 1;
                    firstRemovedRange = removed.subList(0, midIndex);
                    secondRemovedRange = removed.subList(midIndex, removedSize);
                }
            }

            @Override
            public int getFrom() {
                checkState();
                return from;
            }

            @Override
            public int getTo() {
                return getFrom();
            }

            @Override
            public List<T> getRemoved() {
                checkState();
                return atFirstRange ? firstRemovedRange : secondRemovedRange;
            }

            @Override
            public int getRemovedSize() {
                checkState();
                return atFirstRange ? firstRemovedRange.size() : secondRemovedRange.size();
            }

            @Override
            protected int[] getPermutation() {
                checkState();
                return EMPTY_PERM;
            }

            @Override
            public boolean next() {
                if (invalid) {
                    invalid = false;

                    // point 'from' to the first position, relative to
                    // the underlying selectedCells index.
                    from = atFirstRange ? 0 : 1;
                    return true;
                }

                if (atFirstRange && !secondRemovedRange.isEmpty()) {
                    atFirstRange = false;

                    // point 'from' to the second position, relative to
                    // the underlying selectedCells index.
                    from = 1;
                    return true;
                }

                return false;
            }

            @Override
            public void reset() {
                invalid = true;
                atFirstRange = !firstRemovedRange.isEmpty();
            }

            private void checkState() {
                if (invalid) {
                    throw new IllegalStateException("Invalid Change state: next() must be called before inspecting the Change.");
                }
            }
        };
    }

    private class SelectedIndicesList extends ReadOnlyUnbackedObservableList<Integer> {
        private final BitSet bitset;

        private int size = -1;
        private int lastGetIndex = -1;
        private int lastGetValue = -1;

        // Fix for RT-20945 (and numerous other issues!)
        private int atomicityCount = 0;

        /**
         * Constructs a new instance of SelectedIndicesList
         */
        public SelectedIndicesList() {
            this(new BitSet());
        }

        /**
         * Constructs a new instance of SelectedIndicesList from the provided BitSet.
         * The underlying source BitSet shouldn't be modified once it has been passed to the constructor.
         * @param bitset Bitset to be used.
         */
        public SelectedIndicesList(BitSet bitset) {
            this.bitset = bitset;
        }

        boolean isAtomic() {
            return atomicityCount > 0;
        }

        void startAtomic() {
            atomicityCount++;
        }

        void stopAtomic() {
            atomicityCount = Math.max(0, atomicityCount - 1);
        }

        // Returns the selected index at the given index.
        // e.g. if our selectedIndices are [1,3,5], then an index of 2 will return 5 here.
        @Override
        public Integer get(int index) {
            final int itemCount = size();
            if (index < 0 || index >= itemCount) {
                throw new IndexOutOfBoundsException(index + " >= " + itemCount);
            }
            if (lastGetIndex == index) {
                return lastGetValue;
            } else if (index == (lastGetIndex + 1) && lastGetValue < itemCount) {
                // we're iterating forward in order, short circuit for
                // performance reasons (RT-39776)
                lastGetIndex++;
                lastGetValue = bitset.nextSetBit(lastGetValue + 1);
                return lastGetValue;
            } else if (index == (lastGetIndex - 1) && lastGetValue > 0) {
                // we're iterating backward in order, short circuit for
                // performance reasons (RT-39776)
                lastGetIndex--;
                lastGetValue = bitset.previousSetBit(lastGetValue - 1);
                return lastGetValue;
            } else {
                for (lastGetIndex = 0, lastGetValue = bitset.nextSetBit(0);
                     lastGetValue >= 0 || lastGetIndex == index;
                     lastGetIndex++, lastGetValue = bitset.nextSetBit(lastGetValue + 1)) {
                    if (lastGetIndex == index) {
                        return lastGetValue;
                    }
                }
            }

            return -1;
        }

        public void set(int index) {
            if (!isValidIndex(index) || isSelected(index)) {
                return;
            }

            _beginChange();
            size = -1;
            bitset.set(index);
            if (index <= lastGetValue) reset();
            int indicesIndex = indexOf(index);
            _nextAdd(indicesIndex, indicesIndex + 1);
            _endChange();
        }

        private boolean isValidIndex(int index) {
            return index >= 0 && index < getItemCount();
        }

        public void set(int index, boolean isSet) {
            if (isSet) {
                set(index);
            } else {
                clear(index);
            }
        }

        public void set(int index, int end, boolean isSet) {
            _beginChange();
            size = -1;
            if (isSet) {
                bitset.set(index, end, isSet);
                if (index <= lastGetValue) reset();
                int indicesIndex = indexOf(index);
                int span = end - index;
                _nextAdd(indicesIndex, indicesIndex + span);
            } else {
                // TODO handle remove
                bitset.set(index, end, isSet);
                if (index <= lastGetValue) reset();
            }
            _endChange();
        }

        public void set(int index, int... indices) {
            if (indices == null || indices.length == 0) {
                set(index);
            } else {
                // we reduce down to the minimal number of changes possible
                // by finding all contiguous indices, of all indices that are
                // not already selected, and which are in the valid range
                startAtomic();
                List<Integer> sortedNewIndices =
                        IntStream.concat(IntStream.of(index), IntStream.of(indices))
                                .distinct()
                                .filter(this::isValidIndex)
                                .filter(this::isNotSelected)
                                .sorted()
                                .boxed()
                                .peek(this::set) // we also set here, but it's atomic!
                                .toList();
                stopAtomic();

                final int size = sortedNewIndices.size();
                if (size == 0) {
                    // no-op
                } else if (size == 1) {
                    _beginChange();
                    int _index = sortedNewIndices.get(0);
                    int indicesIndex = indexOf(_index);
                    _nextAdd(indicesIndex, indicesIndex + 1);
                    _endChange();
                } else {
                    _beginChange();

                    int startIndex = indexOf(sortedNewIndices.get(0));
                    int endIndex = startIndex + 1;

                    for (int i = 1; i < sortedNewIndices.size(); ++i) {
                        int currentValue = get(endIndex);
                        int currentNewValue = sortedNewIndices.get(i);
                        if (currentValue != currentNewValue) {
                            _nextAdd(startIndex, endIndex);
                            while (get(endIndex) != currentNewValue) ++endIndex;
                            startIndex = endIndex++;
                        } else {
                            ++endIndex;
                        }
                        if (i == sortedNewIndices.size() - 1) {
                            _nextAdd(startIndex, endIndex);
                        }
                    }

                    _endChange();
                }
            }
        }

        @Override
        public void clear() {
            _beginChange();
            List<Integer> removed = bitset.stream().boxed().collect(Collectors.toList());
            size = 0;
            bitset.clear();
            reset();
            _nextRemove(0, removed);
            _endChange();
        }

        public void clear(int index) {
            if (!bitset.get(index)) return;

            int indicesIndex = indexOf(index);
            _beginChange();
            size = -1;
            bitset.clear(index);
            if (index <= lastGetValue) reset();
            _nextRemove(indicesIndex, index);
            _endChange();
        }

        public boolean isSelected(int index) {
            return bitset.get(index);
        }

        public boolean isNotSelected(int index) {
            return !isSelected(index);
        }

        /** Returns number of true bits in BitSet */
        @Override
        public int size() {
            if (size >= 0) {
                return size;
            }
            size = bitset.cardinality();
            return size;
        }

        /** Returns the number of bits reserved in the BitSet */
        public int bitsetSize() {
            return bitset.size();
        }

        @Override
        public int indexOf(Object obj) {
            if (!(obj instanceof Number)) {
                return -1;
            }
            Number n = (Number) obj;
            int index = n.intValue();
            if (!bitset.get(index)) {
                return -1;
            }

            // is left most bit
            if (index == 0) {
                return 0;
            }

            // is right most bit
            if (index == bitset.length() - 1) {
                return size() - 1;
            }

            // count right bit
            if (index > bitset.length() / 2) {
                int count = 1;
                for (int i = bitset.nextSetBit(index + 1); i >= 0; i = bitset.nextSetBit(i + 1)) {
                    count++;
                }
                return size() - count;
            }

            // count left bit
            int count = 0;
            for (int i = bitset.previousSetBit(index - 1); i >= 0; i = bitset.previousSetBit(i - 1)) {
                count++;
            }
            return count;
        }

        @Override
        public boolean contains(Object o) {
            if (o instanceof Number) {
                Number n = (Number) o;
                int index = n.intValue();

                return index >= 0 && index < bitset.length() &&
                        bitset.get(index);
            }

            return false;
        }

        public void reset() {
            this.lastGetIndex = -1;
            this.lastGetValue = -1;
        }

        @Override
        public void _beginChange() {
            if (!isAtomic()) {
                super._beginChange();
            }
        }

        @Override
        public void _endChange() {
            if (!isAtomic()) {
                super._endChange();
            }
        }

        @Override
        public final void _nextUpdate(int pos) {
            if (!isAtomic()) {
                nextUpdate(pos);
            }
        }

        @Override
        public final void _nextSet(int idx, Integer old) {
            if (!isAtomic()) {
                nextSet(idx, old);
            }
        }

        @Override
        public final void _nextReplace(int from, int to, List<? extends Integer> removed) {
            if (!isAtomic()) {
                nextReplace(from, to, removed);
            }
        }

        @Override
        public final void _nextRemove(int idx, List<? extends Integer> removed) {
            if (!isAtomic()) {
                nextRemove(idx, removed);
            }
        }

        @Override
        public final void _nextRemove(int idx, Integer removed) {
            if (!isAtomic()) {
                nextRemove(idx, removed);
            }
        }

        @Override
        public final void _nextPermutation(int from, int to, int[] perm) {
            if (!isAtomic()) {
                nextPermutation(from, to, perm);
            }
        }

        @Override
        public final void _nextAdd(int from, int to) {
            if (!isAtomic()) {
                nextAdd(from, to);
            }
        }
    }
}
