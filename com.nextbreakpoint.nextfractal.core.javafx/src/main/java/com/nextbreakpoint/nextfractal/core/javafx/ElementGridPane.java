/*
 * NextFractal 2.3.0
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

import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ElementGridPane<T extends Serializable> extends BorderPane {
//	private SelectionDelegate<NodeObject> delegate;
	private Pane container = new Pane();
	private GridGroup sentinelGroup;
	private NodeObject listNode;
	private int maxElements;
	
	public ElementGridPane(NodeObject listNode, Dimension2D size) {
		this(listNode, size, 7);
	}

	public ElementGridPane(NodeObject listNode, Dimension2D size, int maxElements) {
		setPrefWidth(size.getWidth());
		setMinHeight(size.getHeight());
		ScrollPane scrollPane = new ScrollPane(container);
		scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		scrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
		setCenter(scrollPane);
		container.setPrefWidth(size.getWidth());
		container.setMinHeight(size.getHeight());
		int cells = getCellCount(size.getWidth(), size.getHeight());
		setMinWidth(cells * size.getHeight());
		this.maxElements = maxElements;
		this.listNode = listNode;
	}

	protected void init() {
		for (int i = 0; i < getNodeCount(); i++) {
			container.getChildren().add(createItem(getNode(i)));
		}
		sentinelGroup = createSentinel();
		container.getChildren().add(sentinelGroup);
		doLayout();
	}

//	public SelectionDelegate<NodeObject> getDelegate() {
//		return delegate;
//	}
//
//	public void setDelegate(SelectionDelegate<NodeObject> delegate) {
//		this.delegate = delegate;
//	}

	protected void appendNode(NodeObject nodeRef) {
		listNode.appendChildNode(nodeRef);
	}

	protected void insertNodeAfter(int index, NodeObject nodeRef) {
		listNode.insertNodeAfter(index, nodeRef);
	}

	protected void insertNodeBefore(int index, NodeObject nodeRef) {
		listNode.insertNodeBefore(index, nodeRef);
	}

	protected void removeNode(int index) {
		listNode.removeChildNode(index);
	}

	protected NodeObject getNode(int index) {
		return listNode.getChildNode(index);
	}
	
	protected int getNodeCount() {
		return listNode.getChildNodeCount();
	}
	
	protected abstract NodeObject createNode(T element);

	protected void fireNodeSelected(NodeObject nodeObject) {
//		if (delegate != null) {
//			delegate.didSelectValue(nodeObject);
//		}
	}
	
	protected abstract T createElement();

	protected abstract String getElementName(T element);

	@SuppressWarnings("unchecked")
	protected T getElement(NodeObject nodeObject) {
		return (T)nodeObject.getValue();
	}

	private int getCellCount(double width, double size) {
		return (int)Math.floor(width / (size + 10));
	}
	
	private void doLayout() {
		int cells = getCellCount(getPrefWidth(), getMinHeight());
		for (int i = 0; i < container.getChildren().size(); i++) {
			Node child = container.getChildren().get(i);
			child.setLayoutX((i % cells) * (getMinHeight() + 10));
			child.setLayoutY((i / cells) * (getMinHeight() + 10));
			((Group) child).getChildren().get(0).setTranslateX(0);
			((Group) child).getChildren().get(0).setTranslateY(0);
		}
	}

	private Node unwrapNode(GridGroup group) {
		return group.getChildren().get(0);
	}
	
	private void setCellSize(Pane node) {
		node.setPrefWidth(getMinHeight());
		node.setPrefHeight(getMinHeight());
		node.setMinWidth(getMinHeight());
		node.setMinHeight(getMinHeight());
		node.setMaxWidth(getMinHeight());
		node.setMaxHeight(getMinHeight());
	}

	private GridGroup createSentinel() {
		GridSentinel sentinel = new GridSentinel();
		setCellSize(sentinel);
		GridGroup node = createGroup(sentinel);
		return node;
	}

	private GridGroup createItem(NodeObject nodeObject) {
		GridItem item = new GridItem(nodeObject);
		setCellSize(item);
		GridGroup group = createGroup(item);
		return group;
	}

	private GridGroup createGroup(final GridItem node) {
		final GroupItem group = new GroupItem(node);
		makeDraggable(group);
		return group;
	}

	private GridGroup createGroup(final GridSentinel node) {
		final GroupSentinel group = new GroupSentinel(node);
		makeDraggable(group);
		return group;
	}

	private void makeDraggable(final GridGroup sourceGroup) {
		final DragContext dragContext = new DragContext();
		
		sourceGroup.addEventFilter(MouseEvent.ANY,
			new EventHandler<MouseEvent>() {
				public void handle(final MouseEvent mouseEvent) {
					mouseEvent.consume();
				}
			});

		sourceGroup.addEventFilter(MouseEvent.MOUSE_PRESSED,
			new EventHandler<MouseEvent>() {
				public void handle(final MouseEvent mouseEvent) {
					Node node = unwrapNode(sourceGroup);
					dragContext.mouseAnchorX = mouseEvent.getX();
					dragContext.mouseAnchorY = mouseEvent.getY();
					dragContext.initialTranslateX = node.getTranslateX();
					dragContext.initialTranslateY = node.getTranslateY();
					beginDrag(dragContext, sourceGroup);
				}
			});

		sourceGroup.addEventFilter(MouseEvent.MOUSE_DRAGGED,
			new EventHandler<MouseEvent>() {
				public void handle(final MouseEvent mouseEvent) {
					double x = computeX(dragContext, sourceGroup, mouseEvent);
					double y = computeY(dragContext, sourceGroup, mouseEvent);
					updateDrag(dragContext, sourceGroup, x, y);
				}
			});

		sourceGroup.addEventFilter(MouseEvent.MOUSE_RELEASED,
			new EventHandler<MouseEvent>() {
				public void handle(final MouseEvent mouseEvent) {
					double x = computeX(dragContext, sourceGroup, mouseEvent);
					double y = computeY(dragContext, sourceGroup, mouseEvent);
					endDrag(dragContext, sourceGroup, x, y);
				}
			});
	}

	@SuppressWarnings("unchecked")
	private ElementGridPane<T>.GridGroup getGroup(int i) {
		return (GridGroup)container.getChildren().get(i);
	}

	@SuppressWarnings("unchecked")
	private NodeObject getNode(final GridGroup sourceGroup) {
		return ((GridItem)unwrapNode(sourceGroup)).getNode();
	}

	private double computeX(final DragContext dragContext, final GridGroup sourceGroup, final MouseEvent mouseEvent) {
		Node node = unwrapNode(sourceGroup);
		double x = dragContext.initialTranslateX + mouseEvent.getX() - dragContext.mouseAnchorX;
		if (x < -sourceGroup.getLayoutX()) {
			x = -sourceGroup.getLayoutX();
		}
		if (x >= getWidth() - sourceGroup.getLayoutX() - node.getBoundsInLocal().getWidth()) {
			x = getWidth() - sourceGroup.getLayoutX() - node.getBoundsInLocal().getWidth();
		}
		return x;
	}

	private double computeY(final DragContext dragContext, final GridGroup sourceGroup, final MouseEvent mouseEvent) {
		Node node = unwrapNode(sourceGroup);
		double y = dragContext.initialTranslateY + mouseEvent.getY() - dragContext.mouseAnchorY;
		if (y < -sourceGroup.getLayoutY()) {
			y = -sourceGroup.getLayoutY();
		}
		if (y >= getHeight() - sourceGroup.getLayoutY() - node.getBoundsInLocal().getHeight()) {
			y = getHeight() - sourceGroup.getLayoutY() - node.getBoundsInLocal().getHeight();
		}
		return y;
	}

	private int findGroup(final GridGroup sourceGroup, double x, double y) {
		int selectedIndex = -1;
		double nx = sourceGroup.getLayoutX() + x;
		double ny = sourceGroup.getLayoutY() + y;
		Node node1 = unwrapNode(sourceGroup);
		for (int i = 0; i < container.getChildren().size(); i++) {
			GridGroup group = getGroup(i);
			double tx = nx - group.getLayoutX();
			double ty = ny - group.getLayoutY();
			if (group != sourceGroup && group.contains(tx + node1.getBoundsInLocal().getWidth() / 2, ty + node1.getBoundsInLocal().getHeight() / 2)) {
				selectedIndex = i;
				break;
			}
		}
		return selectedIndex;
	}

	private void beginDrag(final DragContext dragContext, final GridGroup sourceGroup) {
		if (sourceGroup != sentinelGroup || container.getChildren().size() <= maxElements) {
			sourceGroup.setSource(true);
			sourceGroup.beginDrag();
			if (sourceGroup != sentinelGroup) {
				sentinelGroup.beginDrag();
			}
			dragContext.index = container.getChildren().indexOf(sourceGroup);
			container.getChildren().remove(dragContext.index);
			container.getChildren().add(sourceGroup);
			dragContext.selectedIndex = -1;
		} else {
			sourceGroup.setSource(false);
		}
	}

	private void updateDrag(final DragContext dragContext, final GridGroup sourceGroup, double x, double y) {
		if (sourceGroup.isSource()) {
			Node node = unwrapNode(sourceGroup);
			node.setTranslateX(x);
			node.setTranslateY(y);
			int selectedIndex = findGroup(sourceGroup, x, y);
			if (dragContext.selectedIndex != selectedIndex) {
				if (dragContext.selectedIndex != -1) {
					GridGroup group = getGroup(dragContext.selectedIndex);
					group.setSelected(false);
					group.updateDrag();
				}
				dragContext.selectedIndex = selectedIndex;
				if (selectedIndex != -1) {
					GridGroup group = getGroup(dragContext.selectedIndex);
					group.setSelected(true);
					group.updateDrag();
				}
			}
			sourceGroup.updateDrag();
			if (sourceGroup != sentinelGroup) {
				sentinelGroup.updateDrag();
			}
		}
	}

	private void endDrag(final DragContext dragContext,	final GridGroup sourceGroup, double x, double y) {
		if (sourceGroup.isSource()) {
			int sourceIndex = dragContext.index;
			Node node = unwrapNode(sourceGroup);
			container.getChildren().remove(sourceGroup);
			int targetIndex = findGroup(sourceGroup, x, y);
			GridGroup targetGroup = null;
			if (targetIndex != -1) {
				targetGroup = getGroup(targetIndex);
			}
			if (targetGroup != null) {
				double tx = sourceGroup.getLayoutX() + x - targetGroup.getLayoutX();
				if (sourceGroup instanceof ElementGridPane<?>.GroupItem) {
					if (targetGroup instanceof ElementGridPane<?>.GroupItem) {
						NodeObject nodeObject = getNode(sourceIndex);
						removeNode(sourceIndex);
						if (tx + node.getBoundsInLocal().getWidth() / 2 <= targetGroup.getBoundsInParent().getWidth() / 2) {
							insertNodeBefore(targetIndex, nodeObject);
							container.getChildren().add(targetIndex, sourceGroup);
						} else {
							insertNodeAfter(targetIndex, nodeObject);
							container.getChildren().add(targetIndex + 1, sourceGroup);
						}
					} else if (targetGroup instanceof ElementGridPane<?>.GroupSentinel) {
						removeNode(sourceIndex);
					}
					doLayout();
				} else if (sourceGroup instanceof ElementGridPane<?>.GroupSentinel) {
					if (targetGroup instanceof ElementGridPane<?>.GroupItem) {
						NodeObject nodeObject = createNode(createElement());
						Node newNode = createItem(nodeObject);
						if (tx + node.getBoundsInLocal().getWidth() / 2 <= targetGroup.getBoundsInParent().getWidth() / 2) {
							insertNodeBefore(targetIndex, nodeObject);
							container.getChildren().add(targetIndex, newNode);
						} else {
							insertNodeAfter(targetIndex, nodeObject);
							container.getChildren().add(targetIndex + 1, newNode);
						}
					}
					container.getChildren().add(sourceGroup);
					doLayout();
				}
			} else if (sourceGroup instanceof ElementGridPane<?>.GroupSentinel) {
				NodeObject nodeObject = createNode(createElement());
				Node newNode = createItem(nodeObject);
				appendNode(nodeObject);
				container.getChildren().add(newNode);
				container.getChildren().add(sourceGroup);
				doLayout();
			} else {
				container.getChildren().add(dragContext.index, sourceGroup);
				if (Math.hypot(node.getTranslateX(), node.getTranslateY()) < 5) {
					fireNodeSelected(getNode(sourceGroup));
				}
			}
			node.setTranslateX(0);
			node.setTranslateY(0);
			if (targetGroup != null && targetGroup != sentinelGroup) {
				targetGroup.setSelected(false);
				targetGroup.endDrag();
			}
			sourceGroup.setSource(false);
			sourceGroup.endDrag();
			if (sourceGroup != sentinelGroup) {
				sentinelGroup.setSelected(false);
				sentinelGroup.endDrag();
			}
		}
	}

	private class DragContext {
		private double mouseAnchorX;
		private double mouseAnchorY;
		private double initialTranslateX;
		private double initialTranslateY;
		private int selectedIndex;
		private int index;
	}
	
	private class GridItem extends BorderPane {
		private NodeObject nodeObject;
		
		public GridItem(NodeObject nodeObject) {
			this.nodeObject = nodeObject;
			getStyleClass().add("grid-item");
			Label label = new Label(getName());
			label.setAlignment(Pos.CENTER);
			setCenter(label);
		}

		public String getName() {
			String name = getElementName(getElement(nodeObject));
			if (name != null) {
				Pattern pattern = Pattern.compile("([A-Z])[a-z ]*", 0);
				Matcher matcher = pattern.matcher(name);
				StringBuilder builder = new StringBuilder();
				while (matcher.find()) {
					builder.append(matcher.group(1));
				}
				return builder.toString();
			}
			return "?";
		}

		public NodeObject getNode() {
			return nodeObject;
		}
	}
	
	private class GridSentinel extends BorderPane {
		public GridSentinel() {
			getStyleClass().add("grid-sentinel");
			Label label = new Label("+");
			label.setAlignment(Pos.CENTER);
			setCenter(label);
		}
		
		public void setLabel(String text) {
			((Label)getCenter()).setText(text);
		}
	}
	
	private class GridGroup extends Group {
		private boolean selected = false;
		private boolean source = false;
		private boolean changed = false;
		
		public GridGroup(Node node) {
			super(node);
		}

		public boolean isSelected() {
			return selected;
		}

		public boolean isSource() {
			return source;
		}

		public boolean isChanged() {
			return changed;
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
			setChanged(true);
		}

		public void setSource(boolean source) {
			this.source = source;
			setChanged(true);
		}

		protected void setChanged(boolean source) {
			this.changed = true;
		}

		public void beginDrag() {
		}
		
		public void updateDrag() {
		}

		public void endDrag() {
		}
	}
	
	private class GroupItem extends GridGroup {
		public GroupItem(GridItem node) {
			super(node);
		}

		public void beginDrag() {
			setChanged(true);
		}

		public void updateDrag() {
			if (isChanged()) {
				if (!isSource() || Math.hypot(unwrapNode(this).getTranslateX(), unwrapNode(this).getTranslateY()) > 5) {
					if (isSelected()) {
						unwrapNode(this).setStyle("-fx-border-color:#440000;-fx-background-color:#FF0000;-fx-opacity:1.0");
					} else {
						if (isSource()) {
							unwrapNode(this).setStyle("-fx-border-color:#004400;-fx-background-color:#00FF00;-fx-opacity:0.5");
						} else {
							unwrapNode(this).setStyle("-fx-border-color:#444444;-fx-background-color:#666666;-fx-opacity:1.0");
						}
					}
				}
				setChanged(false);
			}
		}

		public void endDrag() {
			unwrapNode(this).setStyle("-fx-border-color:#444444;-fx-background-color:#666666;-fx-opacity:1.0");
		}
	}
	
	private class GroupSentinel extends GridGroup {
		public GroupSentinel(GridSentinel node) {
			super(node);
		}

		@SuppressWarnings("unchecked")
		private void setLabel(String text) {
			((GridSentinel)unwrapNode(this)).setLabel(text);
			setChanged(true);
		}

		public void beginDrag() {
			setChanged(true);
		}

		public void updateDrag() {
			if (isChanged()) {
				if (!isSource() || Math.hypot(unwrapNode(this).getTranslateX(), unwrapNode(this).getTranslateY()) > 5) {
					if (isSelected()) {
						unwrapNode(this).setStyle("-fx-border-color:#222222;-fx-background-color:#444444;-fx-opacity:1.0");
					} else {
						if (isSource()) {
							setLabel("?");
							unwrapNode(this).setStyle("-fx-border-color:#004400;-fx-background-color:#00FF00;-fx-opacity:0.5");
						} else {
							setLabel("-");
							unwrapNode(this).setStyle("-fx-border-color:#333333;-fx-background-color:#555555;-fx-opacity:1.0");
						}
					}
				}
				setChanged(false);
			}
		}

		public void endDrag() {
			setLabel("+");
			unwrapNode(this).setStyle("-fx-border-color:#444400;-fx-background-color:#ffff00;-fx-opacity:1.0");
		}
	}
}
