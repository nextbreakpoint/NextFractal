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
package com.nextbreakpoint.nextfractal.core.javafx.export;

import com.nextbreakpoint.common.command.Command;
import com.nextbreakpoint.common.either.Either;
import com.nextbreakpoint.nextfractal.core.common.AnimationClip;
import com.nextbreakpoint.nextfractal.core.common.AnimationEvent;
import com.nextbreakpoint.nextfractal.core.common.Bundle;
import com.nextbreakpoint.nextfractal.core.common.ExecutorUtils;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.common.ThreadUtils;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import com.nextbreakpoint.nextfractal.core.javafx.PlatformImageLoader;
import com.nextbreakpoint.nextfractal.core.javafx.grid.GridView;
import com.nextbreakpoint.nextfractal.core.javafx.misc.AdvancedTextField;
import com.nextbreakpoint.nextfractal.core.javafx.observable.BooleanObservableValue;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import lombok.extern.java.Log;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static com.nextbreakpoint.nextfractal.core.common.Plugins.tryFindFactory;

@Log
public class ExportPane extends BorderPane {
    private final ExecutorService executor;
    private final GridView<ExportGridViewItem> gridView;
    private final BooleanObservableValue captureProperty;
    private final BooleanObservableValue videoProperty;
    private final ToggleButton captureButton;

    private ExportDelegate delegate;

    public ExportPane() {
        captureProperty = new BooleanObservableValue();
        captureProperty.setValue(false);

        videoProperty = new BooleanObservableValue();
        videoProperty.setValue(false);

        final ComboBox<Integer[]> presetsCombobox = new ComboBox<>();
        presetsCombobox.getStyleClass().add("text-small");
        presetsCombobox.setTooltip(new Tooltip("Select image or video size"));
        loadImagePresets(presetsCombobox);
        final Integer[] item0 = presetsCombobox.getSelectionModel().getSelectedItem();
        final AdvancedTextField widthField = new AdvancedTextField();
        widthField.getStyleClass().add("text-small");
        widthField.setRestrict(getRestriction());
        widthField.setEditable(false);
        widthField.setText(String.valueOf(item0[0]));
        final AdvancedTextField heightField = new AdvancedTextField();
        heightField.setRestrict(getRestriction());
        heightField.setEditable(false);
        heightField.setText(String.valueOf(item0[1]));
        heightField.getStyleClass().add("text-small");

        final ComboBox<String[]> formatCombobox = new ComboBox<>();
        formatCombobox.getStyleClass().add("text-small");
        formatCombobox.setTooltip(new Tooltip("Select format to export"));
        formatCombobox.getItems().add(new String[]{"PNG image", "PNG"});
        formatCombobox.getItems().add(new String[]{"JPEG image", "JPEG"});
        formatCombobox.getSelectionModel().select(0);

        final VBox formatBox = new VBox(5);
        formatBox.setAlignment(Pos.CENTER);
        formatBox.getChildren().add(formatCombobox);

        captureButton = new ToggleButton("Capture");
        final Button exportButton = new Button("Export");
        final Button removeButton = new Button("Remove");
        final Button previewButton = new Button("Preview");
        exportButton.setTooltip(new Tooltip("Export image or video"));
        removeButton.setTooltip(new Tooltip("Remove selected clips"));
        previewButton.setTooltip(new Tooltip("Preview selected clips"));
        captureButton.setTooltip(new Tooltip("Enable/disable capture"));

        final VBox exportButtons = new VBox(4);
        exportButtons.getChildren().add(exportButton);
        exportButtons.getStyleClass().add("buttons");
        exportButtons.getStyleClass().add("text-small");

        final VBox dimensionBox = new VBox(5);
        dimensionBox.setAlignment(Pos.CENTER);
        dimensionBox.getChildren().add(presetsCombobox);

        final HBox sizeBox = new HBox(5);
        sizeBox.setAlignment(Pos.CENTER);
        sizeBox.getChildren().add(widthField);
        sizeBox.getChildren().add(heightField);

        final VBox exportControls = new VBox(8);
        exportControls.setAlignment(Pos.CENTER_LEFT);
        exportControls.getChildren().add(new Label("Exported format"));
        exportControls.getChildren().add(formatBox);
        exportControls.getChildren().add(new Label("Size in pixels"));
        exportControls.getChildren().add(dimensionBox);
        exportControls.getChildren().add(sizeBox);

        final VBox clipButtons = new VBox(4);
        clipButtons.getChildren().add(captureButton);
        clipButtons.getChildren().add(removeButton);
        clipButtons.getChildren().add(previewButton);
        clipButtons.getStyleClass().add("buttons");
        clipButtons.getStyleClass().add("text-small");

        gridView = new GridView<>(new ExportGridViewCellFactory(), false, 1);

//        gridView.setCellFactory(_ -> {
//            final ClipListCellDelegate cellDelegate = (fromIndex, toIndex) -> {
//                if (delegate != null) {
//                    delegate.captureSessionMoved(fromIndex, toIndex);
//                }
//            };
//            final ClipListCell clipListCell = new ClipListCell(tile);
//            clipListCell.setDelegate(cellDelegate);
//            return clipListCell;
//        });
        gridView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        final VBox exportPane = new VBox(8);
        exportPane.setAlignment(Pos.TOP_CENTER);
        exportPane.getChildren().add(clipButtons);
        exportPane.getChildren().add(exportControls);
        exportPane.getChildren().add(exportButtons);
        exportPane.getStyleClass().add("controls");

        setCenter(gridView);
        setBottom(exportPane);

        previewButton.setDisable(true);

        getStyleClass().add("export");

        Runnable updateButtonsAndPanels = () -> {
            final boolean selected = captureProperty.getValue();
            removeButton.setDisable(gridView.getItems().isEmpty() || selected);
            previewButton.setDisable(gridView.getItems().isEmpty() || selected);
            exportButton.setDisable(selected);
            formatCombobox.setDisable(selected);
            presetsCombobox.setDisable(selected);
            widthField.setDisable(selected);
            heightField.setDisable(selected);
        };

        presetsCombobox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer[] item) {
                if (item == null) {
                    return null;
                } else {
                    if (item[0] == 0 || item[1] == 0) {
                        return "Custom";
                    } else {
                        return item[0] + "\u00D7" + item[1];
                    }
                }
            }

            @Override
            public Integer[] fromString(String preset) {
                return null;
            }
        });

        formatCombobox.setConverter(new StringConverter<>() {
            @Override
            public String toString(String[] item) {
                if (item == null) {
                    return null;
                } else {
                    return item[0];
                }
            }

            @Override
            public String[] fromString(String preset) {
                return null;
            }
        });

        presetsCombobox.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Integer[]> call(ListView<Integer[]> p) {
                return new ListCell<>() {
                    private final Label label;

                    {
                        label = new Label();
                    }

                    @Override
                    protected void updateItem(Integer[] item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            label.setText(presetsCombobox.getConverter().toString(item));
                            setGraphic(label);
                        }
                    }
                };
            }
        });

        formatCombobox.setCellFactory(new Callback<>() {
            @Override
            public ListCell<String[]> call(ListView<String[]> p) {
                return new ListCell<>() {
                    private final Label label;

                    {
                        label = new Label();
                    }

                    @Override
                    protected void updateItem(String[] item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            label.setText(formatCombobox.getConverter().toString(item));
                            setGraphic(label);
                        }
                    }
                };
            }
        });

        presetsCombobox.valueProperty().addListener((_, _, newItem) -> {
            if (newItem != null && (newItem[0] == 0 || newItem[1] == 0)) {
                widthField.setEditable(true);
                heightField.setEditable(true);
                if (gridView.getItems().isEmpty()) {
                    widthField.setText("1024");
                    heightField.setText("768");
                } else {
                    widthField.setText("720");
                    heightField.setText("570");
                }
            } else {
                widthField.setEditable(false);
                heightField.setEditable(false);
                if (newItem != null) {
                    widthField.setText(String.valueOf(newItem[0]));
                    heightField.setText(String.valueOf(newItem[1]));
                } else {
                    widthField.setText("0");
                    heightField.setText("0");
                }
            }
        });

        formatCombobox.valueProperty().addListener((_, _, newItem) -> {
            if (newItem != null && newItem[1].equals("PNG")) {
                loadImagePresets(presetsCombobox);
            } else {
                loadVideoPresets(presetsCombobox);
            }
        });

        exportButton.setOnMouseClicked(_ -> {
            if (delegate != null) {
                final int renderWidth = Integer.parseInt(widthField.getText());
                final int renderHeight = Integer.parseInt(heightField.getText());
                final String format = formatCombobox.getSelectionModel().getSelectedItem()[1];
                delegate.createSession(new Size(renderWidth, renderHeight), format);
            }
        });

        captureButton.setOnAction(_ -> {
            captureProperty.setValue(captureButton.isSelected());
            if (delegate != null) {
                if (captureProperty.getValue()) {
                    delegate.startCaptureSession();
                } else {
                    delegate.stopCaptureSession();
                }
            }
        });

        captureProperty.addListener((_, _, newValue) -> {
            Platform.runLater(() -> {
                captureButton.setSelected(newValue);
                updateButtonsAndPanels.run();
            });
        });

        removeButton.setOnMouseClicked(_ -> {
            if (gridView.getItems() != null) {
                final List<Integer> selectedIndices = gridView.getSelectionModel().getSelectedIndices().stream().toList();
                if (!selectedIndices.isEmpty()) {
                    for (int i = selectedIndices.size() - 1; i >= 0; i--) {
                        removeItem(gridView.getItems().get(selectedIndices.get(i)));
                    }
                }
            }
        });

        previewButton.setOnMouseClicked(_ -> {
            if (gridView.getItems() != null) {
                if (!gridView.getItems().isEmpty() && delegate != null) {
                    if (!gridView.getSelectionModel().getSelectedItems().isEmpty()) {
                        delegate.playbackStart(gridView.getSelectionModel().getSelectedItems().stream()
                                .map(bitmap -> (AnimationClip) bitmap.getProperty("clip")).collect(Collectors.toList()));
                    } else {
                        delegate.playbackStart(gridView.getItems().stream()
                                .map(bitmap -> (AnimationClip) bitmap.getProperty("clip")).collect(Collectors.toList()));
                    }
                }
            }
        });

        videoProperty.addListener((_, _, newValue) -> {
            if (newValue) {
                formatCombobox.getItems().clear();
                formatCombobox.getItems().add(new String[]{"PNG image", "PNG"});
                formatCombobox.getItems().add(new String[]{"JPEG image", "JPEG"});
                formatCombobox.getItems().add(new String[]{"Quicktime video", "MOV"});
                formatCombobox.getItems().add(new String[]{"MP4 video", "MP4"});
                formatCombobox.getItems().add(new String[]{"AVI video", "AVI"});
                formatCombobox.getSelectionModel().select(2);
            } else {
                formatCombobox.getItems().clear();
                formatCombobox.getItems().add(new String[]{"PNG image", "PNG"});
                formatCombobox.getItems().add(new String[]{"JPEG image", "JPEG"});
                formatCombobox.getSelectionModel().select(0);
            }
            updateButtonsAndPanels.run();
        });

        widthProperty().addListener((_, _, newValue) -> {
            double width = newValue.doubleValue() - getInsets().getLeft() - getInsets().getRight();
            formatCombobox.setPrefWidth(width);
            presetsCombobox.setPrefWidth(width);
            exportPane.setPrefWidth(width);
            exportPane.setMaxWidth(width);
            exportButton.setPrefWidth(width);
            captureButton.setPrefWidth(width);
            removeButton.setPrefWidth(width);
            previewButton.setPrefWidth(width);
        });

        updateButtonsAndPanels.run();

        executor = ExecutorUtils.newSingleThreadExecutor(ThreadUtils.createVirtualThreadFactory("Export"));
    }

    private void loadImagePresets(ComboBox<Integer[]> presetsCombobox) {
        presetsCombobox.getItems().clear();
        presetsCombobox.getItems().add(new Integer[]{0, 0});
        presetsCombobox.getItems().add(new Integer[]{8192, 8192});
        presetsCombobox.getItems().add(new Integer[]{4096, 4096});
        presetsCombobox.getItems().add(new Integer[]{2048, 2048});
        presetsCombobox.getItems().add(new Integer[]{1900, 1900});
        presetsCombobox.getItems().add(new Integer[]{1900, 1080});
        presetsCombobox.getItems().add(new Integer[]{1650, 1650});
        presetsCombobox.getItems().add(new Integer[]{1650, 1050});
        presetsCombobox.getItems().add(new Integer[]{1024, 1024});
        presetsCombobox.getItems().add(new Integer[]{1024, 768});
        presetsCombobox.getItems().add(new Integer[]{640, 640});
        presetsCombobox.getItems().add(new Integer[]{640, 480});
        presetsCombobox.getItems().add(new Integer[]{512, 512});
        presetsCombobox.getItems().add(new Integer[]{256, 256});
        presetsCombobox.getSelectionModel().select(7);
    }

    private void loadVideoPresets(ComboBox<Integer[]> presetsCombobox) {
        presetsCombobox.getItems().clear();
        presetsCombobox.getItems().add(new Integer[]{0, 0});
        presetsCombobox.getItems().add(new Integer[]{1920, 1080});
        presetsCombobox.getItems().add(new Integer[]{1440, 1080});
        presetsCombobox.getItems().add(new Integer[]{1280, 720});
        presetsCombobox.getItems().add(new Integer[]{720, 480});
        presetsCombobox.getItems().add(new Integer[]{720, 576});
        presetsCombobox.getItems().add(new Integer[]{352, 288});
        presetsCombobox.getItems().add(new Integer[]{352, 240});
        presetsCombobox.getSelectionModel().select(1);
    }

    protected String getRestriction() {
        return "-?\\d*\\.?\\d*";
    }

    public void setExportDelegate(ExportDelegate delegate) {
        this.delegate = delegate;
    }

    public void dispose() {
        ExecutorUtils.shutdown(executor);
    }

    public void setCaptureSelected(boolean selected) {
        captureProperty.setValue(selected);
    }

    public void appendClip(AnimationClip clip) {
        addClip(clip, true);
    }

    public void loadClips(List<AnimationClip> clips) {
        removeItems();
        clips.forEach(clip -> addClip(clip, false));
    }

    public void mergeClips(List<AnimationClip> clips) {
        clips.forEach(clip -> addClip(clip, true));
    }

    private void addClip(AnimationClip clip, boolean notifyAddClip) {
        //TODO verify: clip can e empty
        if (!clip.isEmpty()) {
            final PlatformImageLoader imageLoader = createImageLoader(clip);
            final ExportGridViewItem item = new ExportGridViewItem(imageLoader);
            item.putProperty("clip", clip);
            addItem(item, notifyAddClip);
        } else {
            log.warning("Clip is empty");
        }
    }

    private void addItem(ExportGridViewItem item, boolean notifyAddClip) {
        if (item == null) {
            return;
        }
        if (gridView.getItems() == null) {
            return;
        }
        gridView.getItems().addLast(item);
        if (gridView.getItems().size() == 1) {
            videoProperty.setValue(true);
        }
        if (delegate != null) {
            final AnimationClip clip = (AnimationClip) item.getProperty("clip");
            if (notifyAddClip) {
                delegate.captureSessionAdded(clip);
            } else {
                delegate.captureSessionRestored(clip);
            }
        }
    }

    private void removeItem(ExportGridViewItem item) {
        if (item == null) {
            return;
        }
        if (gridView.getItems() == null) {
            return;
        }
        if (delegate != null) {
            final AnimationClip clip = (AnimationClip) item.getProperty("clip");
            delegate.captureSessionRemoved(clip);
        }
        gridView.getItems().remove(item);
        if (gridView.getItems().isEmpty()) {
            videoProperty.setValue(false);
        }
    }

    private void removeItems() {
        if (gridView.getItems() != null) {
            if (delegate != null) {
                gridView.getItems()
                        .stream()
                        .map(item -> (AnimationClip) item.getProperty("clip"))
                        .forEach(clip -> delegate.captureSessionRemoved(clip));
            }
            gridView.getItems().clear();
            videoProperty.setValue(false);
        }
    }

    private PlatformImageLoader createImageLoader(AnimationClip clip) {
        return new PlatformImageLoader(executor, () -> createBundle(clip), () -> new Size((int) getWidth(), (int) getWidth()));
    }

    private Either<Bundle> createBundle(AnimationClip clip) {
        return createSession(clip.getFirstEvent())
                .map(session -> new Bundle(session, List.of()));
    }

    private Either<Session> createSession(AnimationEvent event) {
        return Command.of(tryFindFactory(event.pluginId()))
                .map(factory -> factory.createSession(event.script(), event.metadata()))
                .execute();
    }
}
