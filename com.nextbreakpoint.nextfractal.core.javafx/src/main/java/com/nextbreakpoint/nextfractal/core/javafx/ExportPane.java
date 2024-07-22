/*
 * NextFractal 2.3.2
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
package com.nextbreakpoint.nextfractal.core.javafx;

import com.nextbreakpoint.common.command.Command;
import com.nextbreakpoint.nextfractal.core.common.Clip;
import com.nextbreakpoint.nextfractal.core.common.CoreFactory;
import com.nextbreakpoint.nextfractal.core.common.DefaultThreadFactory;
import com.nextbreakpoint.nextfractal.core.common.ImageComposer;
import com.nextbreakpoint.nextfractal.core.render.RendererSize;
import com.nextbreakpoint.nextfractal.core.render.RendererTile;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
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

import java.nio.IntBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.nextbreakpoint.nextfractal.core.common.Plugins.tryFindFactory;

public class ExportPane extends BorderPane {
	private static Logger logger = Logger.getLogger(ExportPane.class.getName());
	private static final int PADDING = 8;

	private final RendererTile tile;
	private final ExecutorService executor;
	private final ListView<Bitmap> listView;
	private final BooleanObservableValue captureProperty;
	private final BooleanObservableValue videoProperty;
	private final ToggleButton captureButton;

	private ExportDelegate delegate;

	public ExportPane(RendererTile tile) {
		this.tile = tile;

		captureProperty = new BooleanObservableValue();
		captureProperty.setValue(false);

		videoProperty = new BooleanObservableValue();
		videoProperty.setValue(false);

		ComboBox<Integer[]> presetsCombobox = new ComboBox<>();
		presetsCombobox.getStyleClass().add("text-small");
		presetsCombobox.setTooltip(new Tooltip("Select image or video size"));
		loadImagePresets(presetsCombobox);
		Integer[] item0 = presetsCombobox.getSelectionModel().getSelectedItem();
		AdvancedTextField widthField = new AdvancedTextField();
		widthField.getStyleClass().add("text-small");
		widthField.setRestrict(getRestriction());
		widthField.setEditable(false);
		widthField.setText(String.valueOf(item0[0]));
		AdvancedTextField heightField = new AdvancedTextField();
		heightField.setRestrict(getRestriction());
		heightField.setEditable(false);
		heightField.setText(String.valueOf(item0[1]));
		heightField.getStyleClass().add("text-small");

		ComboBox<String[]> formatCombobox = new ComboBox<>();
		formatCombobox.getStyleClass().add("text-small");
		formatCombobox.setTooltip(new Tooltip("Select format to export"));
		formatCombobox.getItems().add(new String[] { "PNG image", "PNG" });
		formatCombobox.getItems().add(new String[] { "JPEG image", "JPEG" });
		formatCombobox.getSelectionModel().select(0);

		VBox formatBox = new VBox(5);
		formatBox.setAlignment(Pos.CENTER);
		formatBox.getChildren().add(formatCombobox);

		captureButton = new ToggleButton("Capture");
		Button exportButton = new Button("Export");
		Button removeButton = new Button("Remove");
		Button previewButton = new Button("Preview");
		exportButton.setTooltip(new Tooltip("Export image or video"));
		removeButton.setTooltip(new Tooltip("Remove selected clips"));
		previewButton.setTooltip(new Tooltip("Preview selected clips"));
		captureButton.setTooltip(new Tooltip("Enable/disable capture"));

		VBox exportButtons = new VBox(4);
		exportButtons.getChildren().add(exportButton);
		exportButtons.getStyleClass().add("buttons");
		exportButtons.getStyleClass().add("text-small");

		VBox dimensionBox = new VBox(5);
		dimensionBox.setAlignment(Pos.CENTER);
		dimensionBox.getChildren().add(presetsCombobox);

		HBox sizeBox = new HBox(5);
		sizeBox.setAlignment(Pos.CENTER);
		sizeBox.getChildren().add(widthField);
		sizeBox.getChildren().add(heightField);

		VBox exportControls = new VBox(8);
		exportControls.setAlignment(Pos.CENTER_LEFT);
		exportControls.getChildren().add(new Label("Exported format"));
		exportControls.getChildren().add(formatBox);
		exportControls.getChildren().add(new Label("Size in pixels"));
		exportControls.getChildren().add(dimensionBox);
		exportControls.getChildren().add(sizeBox);

		VBox clipButtons = new VBox(4);
		clipButtons.getChildren().add(captureButton);
		clipButtons.getChildren().add(removeButton);
		clipButtons.getChildren().add(previewButton);
		clipButtons.getStyleClass().add("buttons");
		clipButtons.getStyleClass().add("text-small");

		listView = new ListView<>();
		listView.setFixedCellSize(tile.getTileSize().getHeight() + PADDING);
		listView.setCellFactory(view -> new ClipListCell(tile, (fromIndex, toIndex) -> {
            if (delegate != null) {
                delegate.captureSessionMoved(fromIndex, toIndex);
            }
        }));
		listView.setTooltip(new Tooltip("List of captured clips"));
		listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		listView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends Bitmap> c) -> itemSelected(listView));

		VBox exportPane = new VBox(8);
		exportPane.setAlignment(Pos.TOP_CENTER);
		exportPane.getChildren().add(clipButtons);
		exportPane.getChildren().add(exportControls);
		exportPane.getChildren().add(exportButtons);
		exportPane.getStyleClass().add("controls");

		setCenter(listView);
		setBottom(exportPane);

		previewButton.setDisable(true);

		getStyleClass().add("export");

		Runnable updateButtonsAndPanels = () -> {
			boolean selected = captureProperty.getValue();
			removeButton.setDisable(listView.getItems().isEmpty() || selected);
			previewButton.setDisable(listView.getItems().isEmpty() || selected);
			exportButton.setDisable(selected);
			formatCombobox.setDisable(selected);
			presetsCombobox.setDisable(selected);
			widthField.setDisable(selected);
			heightField.setDisable(selected);
		};

		presetsCombobox.setConverter(new StringConverter<Integer[]>() {
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

		formatCombobox.setConverter(new StringConverter<String[]>() {
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

		presetsCombobox.setCellFactory(new Callback<ListView<Integer[]>, ListCell<Integer[]>>() {
			@Override
			public ListCell<Integer[]> call(ListView<Integer[]> p) {
				return new ListCell<Integer[]>() {
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

		formatCombobox.setCellFactory(new Callback<ListView<String[]>, ListCell<String[]>>() {
			@Override
			public ListCell<String[]> call(ListView<String[]> p) {
				return new ListCell<String[]>() {
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

		presetsCombobox.valueProperty().addListener((value, oldItem, newItem) -> {
            if (newItem != null && (newItem[0] == 0 || newItem[1] == 0)) {
                widthField.setEditable(true);
                heightField.setEditable(true);
                if (listView.getItems().isEmpty()) {
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

		formatCombobox.valueProperty().addListener((value, oldItem, newItem) -> {
			if (newItem != null && newItem[1].equals("PNG")) {
				loadImagePresets(presetsCombobox);
			} else {
				loadVideoPresets(presetsCombobox);
			}
		});

		exportButton.setOnMouseClicked(e -> {
			if (delegate != null) {
				int renderWidth = Integer.parseInt(widthField.getText());
				int renderHeight = Integer.parseInt(heightField.getText());
				String format = formatCombobox.getSelectionModel().getSelectedItem()[1];
				delegate.createSession(new RendererSize(renderWidth, renderHeight), format);
			}
		});

		captureButton.setOnAction(e -> {
			captureProperty.setValue(captureButton.isSelected());
			if (delegate != null) {
				if (captureProperty.getValue()) {
					delegate.startCaptureSession();
				} else {
					delegate.stopCaptureSession();
				}
			}
		});

		captureProperty.addListener((observable, oldValue, newValue) -> {
			Platform.runLater(() -> {
				captureButton.setSelected(newValue);
				updateButtonsAndPanels.run();
			});
		});

		removeButton.setOnMouseClicked(e -> {
			List<Integer> selectedIndices = listView.getSelectionModel().getSelectedIndices().stream().collect(Collectors.toList());
			if (!selectedIndices.isEmpty()) {
				for (int i = selectedIndices.size() - 1; i >= 0; i--) {
					removeItem(listView, selectedIndices.get(i));
				}
				if (listView.getItems().isEmpty()) {
					videoProperty.setValue(false);
				}
			}
//			} else {
//				for (int i = listView.getItems().size() - 1; i >= 0; i--) {
//					removeItem(listView, i);
//				}
//			}
		});

		previewButton.setOnMouseClicked(e -> {
			if (!listView.getItems().isEmpty() && delegate != null) {
				if (!listView.getSelectionModel().getSelectedItems().isEmpty()) {
					delegate.playbackStart(listView.getSelectionModel().getSelectedItems().stream()
						.map(bitmap -> (Clip) bitmap.getProperty("clip")).collect(Collectors.toList()));
				} else {
					delegate.playbackStart(listView.getItems().stream()
						.map(bitmap -> (Clip) bitmap.getProperty("clip")).collect(Collectors.toList()));
				}
			}
		});

		videoProperty.addListener((observable, oldValue, newValue) -> {
			if (newValue) {
				formatCombobox.getItems().clear();
				formatCombobox.getItems().add(new String[] { "PNG image", "PNG" });
				formatCombobox.getItems().add(new String[] { "JPEG image", "JPEG" });
				formatCombobox.getItems().add(new String[] { "Quicktime video", "MOV" });
				formatCombobox.getItems().add(new String[] { "MP4 video", "MP4" });
				formatCombobox.getItems().add(new String[] { "AVI video", "AVI" });
				formatCombobox.getSelectionModel().select(2);
			} else {
				formatCombobox.getItems().clear();
				formatCombobox.getItems().add(new String[] { "PNG image", "PNG" });
				formatCombobox.getItems().add(new String[] { "JPEG image", "JPEG" });
				formatCombobox.getSelectionModel().select(0);
			}
			updateButtonsAndPanels.run();
		});

		widthProperty().addListener((observable, oldValue, newValue) -> {
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

		executor = Executors.newSingleThreadExecutor(createThreadFactory("Export"));
	}

	private void loadImagePresets(ComboBox<Integer[]> presetsCombobox) {
		presetsCombobox.getItems().clear();
		presetsCombobox.getItems().add(new Integer[] { 0, 0 });
		presetsCombobox.getItems().add(new Integer[] { 8192, 8192 });
		presetsCombobox.getItems().add(new Integer[] { 4096, 4096 });
		presetsCombobox.getItems().add(new Integer[] { 2048, 2048 });
		presetsCombobox.getItems().add(new Integer[] { 1900, 1900 });
		presetsCombobox.getItems().add(new Integer[] { 1900, 1080 });
		presetsCombobox.getItems().add(new Integer[] { 1650, 1650 });
		presetsCombobox.getItems().add(new Integer[] { 1650, 1050 });
		presetsCombobox.getItems().add(new Integer[] { 1024, 1024 });
		presetsCombobox.getItems().add(new Integer[] { 1024, 768 });
		presetsCombobox.getItems().add(new Integer[] { 640, 640 });
		presetsCombobox.getItems().add(new Integer[] { 640, 480 });
		presetsCombobox.getItems().add(new Integer[] { 512, 512 });
		presetsCombobox.getItems().add(new Integer[] { 256, 256 });
		presetsCombobox.getSelectionModel().select(7);
	}

	private void loadVideoPresets(ComboBox<Integer[]> presetsCombobox) {
		presetsCombobox.getItems().clear();
		presetsCombobox.getItems().add(new Integer[] { 0, 0 });
		presetsCombobox.getItems().add(new Integer[] { 1920, 1080 });
		presetsCombobox.getItems().add(new Integer[] { 1440, 1080 });
		presetsCombobox.getItems().add(new Integer[] { 1280, 720 });
		presetsCombobox.getItems().add(new Integer[] { 720, 480 });
		presetsCombobox.getItems().add(new Integer[] { 720, 576 });
		presetsCombobox.getItems().add(new Integer[] { 352, 288 });
		presetsCombobox.getItems().add(new Integer[] { 352, 240 });
		presetsCombobox.getSelectionModel().select(1);
	}

	private void itemSelected(ListView<Bitmap> listView) {
	}

	private DefaultThreadFactory createThreadFactory(String name) {
		return new DefaultThreadFactory(name, true, Thread.MIN_PRIORITY);
	}

	private void addItem(ListView<Bitmap> listView, Clip clip, IntBuffer pixels, RendererSize size, boolean notifyAddClip) {
		BrowseBitmap bitmap = new BrowseBitmap(size.getWidth(), size.getHeight(), pixels);
		bitmap.setProperty("clip", clip);
		listView.getItems().add(bitmap);
		if (listView.getItems().size() == 1) {
			videoProperty.setValue(true);
		}
		if (delegate != null) {
			if (notifyAddClip) {
				delegate.captureSessionAdded(clip);
			} else {
				delegate.captureSessionRestored(clip);
			}
		}
	}

	private void removeItem(ListView<Bitmap> listView, int index) {
		Bitmap bitmap = listView.getItems().remove(index);
		if (bitmap == null) {
			return;
		}
		if (listView.getItems().isEmpty()) {
			videoProperty.setValue(false);
		}
		if (delegate != null) {
			Clip clip = (Clip) bitmap.getProperty("clip");
			delegate.captureSessionRemoved(clip);
		}
	}

	protected String getRestriction() {
		return "-?\\d*\\.?\\d*";
	}
	
	public void setExportDelegate(ExportDelegate delegate) {
		this.delegate = delegate;
	}

	public void dispose() {
		List<ExecutorService> executors = List.of(executor);
		executors.forEach(ExecutorService::shutdownNow);
		executors.forEach(this::await);
	}

	private void await(ExecutorService executor) {
		Command.of(() -> executor.awaitTermination(5000, TimeUnit.MILLISECONDS))
				.execute()
				.observe()
				.onFailure(e -> logger.warning("Await termination timeout"))
				.get();
	}

	public void appendClip(Clip clip) {
		addClip(clip, true);
	}

	private void addClip(Clip clip, boolean notifyAddClip) {
		Command.of(tryFindFactory(clip.getFirstEvent().getPluginId()))
				.map(this::createImageComposer)
				.execute()
				.optional()
				.ifPresent(composer -> submitItem(clip, composer, notifyAddClip));
	}

	private void submitItem(Clip clip, ImageComposer composer, boolean notifyAddClip) {
		executor.submit(() -> Command.of(() -> renderImage(clip, composer))
				.execute().optional().ifPresent(pixels -> Platform.runLater(() -> addItem(listView, clip, pixels, composer.getSize(), notifyAddClip))));
	}

	private IntBuffer renderImage(Clip clip, ImageComposer composer) {
		return composer.renderImage(clip.getFirstEvent().getScript(), clip.getFirstEvent().getMetadata());
	}

	private ImageComposer createImageComposer(CoreFactory factory) {
		return factory.createImageComposer(createThreadFactory("Export Composer"), tile, true);
	}

	public void loadClips(List<Clip> clips) {
		removeAllItems();
		clips.forEach(clip -> addClip(clip, false));
	}

	private void removeAllItems() {
		if (delegate != null) {
			listView.getItems().stream().map(bitmap -> (Clip)bitmap.getProperty("clip")).forEach(clip -> delegate.captureSessionRemoved(clip));
		}
		listView.getItems().clear();
	}

	public void mergeClips(List<Clip> clips) {
		clips.forEach(clip -> addClip(clip, true));
	}

    public void setCaptureSelected(boolean selected) {
		captureProperty.setValue(selected);
    }
}
