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

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class NodeObject {
	private final Map<String, Object> map = new HashMap<>();
	private List<NodeObject> childList;
	@Getter
    private NodeObject parentNode;
	@Getter
    private final String nodeId;
	@Setter(lombok.AccessLevel.PROTECTED)
    @Getter
    private String nodeLabel;
	@Setter(lombok.AccessLevel.PROTECTED)
    @Getter
    private String nodeClass;
	@Setter
    @Getter
    private Object value;
	@Getter
    private boolean changed;

	public void dispose() {
		if (childList != null) {
			for (NodeObject child : childList) {
				child.dispose();
			}
			childList.clear();
			childList = null;
		}
		parentNode = null;
	}

	public NodeObject(final String nodeId) {
		if (nodeId == null) {
			throw new IllegalArgumentException("nodeId is null");
		}
		this.nodeId = nodeId;
	}

	private void setParentNode(final NodeObject parentNode) {
		this.parentNode = parentNode;
	}

    public boolean isChildNode(final NodeObject node) {
		return childList.contains(node);
	}

	public NodeObject getChildNode(final int index) {
		if ((index < 0) || (index >= getChildList().size())) {
			return null;
		}
		return getChildList().get(index);
	}

	public NodeObject getChildNodeByClass(final String nodeClass) {
		for (NodeObject node : getChildList()) {
			if (node.getNodeClass().equals(nodeClass)) {
				return node;
			}
		}
		return null;
	}

	public NodeObject getChildNodeById(final String nodeId) {
		for (NodeObject node : getChildList()) {
			if (node.getNodeId().equals(nodeId)) {
				return node;
			}
		}
		return null;
	}
	
	public int getChildNodeCount() {
		return getChildList().size();
	}

	protected void updateNode() {
		updateChildNodes();
	}

	protected void updateChildNodes() {
	}

    public boolean isMutable() {
		return false;
	}

	public boolean isEditable() {
		return false;
	}

	public boolean isAttribute() {
		return false;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		if (isChanged()) {
			builder.append("*");
		}
		builder.append(nodeId);
		builder.append(" (");
		builder.append(nodeClass != null ? nodeClass : "<no class>");
		builder.append(")");
		return builder.toString();
	}

	protected void appendChildNodeToParent(final NodeObject node) {
		if (parentNode != null) {
			parentNode.appendChildNode(node);
		}
	}

	public void appendChildNode(final NodeObject node) {
		node.setParentNode(this);
		if (getChildList().contains(node)) {
			return;
		}
		getChildList().add(node);
		node.nodeAdded();
	}

	public void removeChildNode(final int index) {
		final NodeObject node = getChildList().get(index);
		getChildList().remove(index);
		node.nodeRemoved();
		node.setParentNode(null);
	}

	public void removeAllChildNodes() {
		for (int i = getChildNodeCount() - 1; i >= 0; i--) {
			removeChildNode(i);
		}
	}

	public void insertNodeBefore(final int index, final NodeObject node) {
		node.setParentNode(this);
		if (getChildList().contains(node)) {
			return;
		}
		if ((index < 0) || (index > getChildList().size())) {
			throw new IllegalArgumentException("index out of bounds");
		}
		getChildList().add(index, node);
		node.nodeAdded();
	}

	public void insertNodeAfter(final int index, final NodeObject node) {
		node.setParentNode(this);
		if (getChildList().contains(node)) {
			return;
		}
		if ((index < 0) || (index > getChildList().size() - 1)) {
			throw new IllegalArgumentException("index out of bounds");
		}
		if (index < getChildList().size() - 1) {
			getChildList().add(index + 1, node);
		}
		else {
			getChildList().add(node);
		}
		node.nodeAdded();
	}

	public void insertChildNodeAt(final int index, final NodeObject node) {
		if (index < getChildList().size()) {
			insertNodeBefore(index, node);
		}
		else if (index > 0) {
			insertNodeAfter(index - 1, node);
		}
		else {
			appendChildNode(node);
		}
	}

	public void moveUpChildNode(final int index) {
		final NodeObject node = getChildList().get(index);
		if (index > 0) {
			removeChildNode(index);
			insertNodeBefore(index - 1, node);
		}
	}

	public void moveDownChildNode(final int index) {
		final NodeObject node = getChildList().get(index);
		if (index < getChildList().size() - 1) {
			removeChildNode(index);
			insertNodeAfter(index, node);
		}
	}

	public void moveChildNode(final int index, final int newIndex) {
		final NodeObject node = getChildList().get(index);
		if (index < getChildList().size() - 1) {
			removeChildNode(index);
			insertNodeAfter(newIndex, node);
		}
	}

	public void setChildNode(final int index, final NodeObject node) {
		if ((index < 0) || (index > getChildList().size() - 1)) {
			throw new IllegalArgumentException("index out of bounds");
		}
		removeChildNode(index);
		insertNodeAfter(index, node);
	}

	public String getValueAsString() {
		return toString();
	}

	public final String getLabel() {
		final StringBuilder builder = new StringBuilder();
		if (isChanged()) {
			builder.append("*");
		}
		addLabel(builder);
		return builder.toString();
	}

	protected void addLabel(final StringBuilder builder) {
		if (nodeLabel != null) {
			builder.append(nodeLabel);
		}
	}

	public final String getDescription() {
		final StringBuilder builder = new StringBuilder();
		addDescription(builder);
		if (parentNode != null) {
			builder.append(" [");
			builder.append(parentNode.getChildList().indexOf(this));
			builder.append("]");
		}
		return builder.toString();
	}

	protected void addDescription(final StringBuilder builder) {
		addLabel(builder);
	}

	public int indexOf(final NodeObject node) {
		return getChildList().indexOf(node);
	}

	private List<NodeObject> getChildList() {
		if (childList == null) {
			childList = new ArrayList<NodeObject>();
		}
		return childList;
	}

	protected void nodeAdded() {
	}

	protected void nodeRemoved() {
	}

	public String dump() {
		final StringBuilder builder = new StringBuilder();
		dumpNode(builder, this, 0);
		return builder.toString();
	}

	private void dumpNode(final StringBuilder builder, final NodeObject node, final int level) {
        builder.append(" ".repeat(Math.max(0, level)));
		builder.append(node);
		if (node.getChildNodeCount() > 0) {
			if (node.getParentNode() != null) {
				builder.append(" path = [");
				builder.append("]");
			}
			builder.append(" {\n");
			for (int i = 0; i < node.getChildNodeCount(); i++) {
				dumpNode(builder, node.getChildNode(i), level + 1);
			}
            builder.append(" ".repeat(Math.max(0, level)));
			builder.append("}\n");
		}
		else {
			if (node.getParentNode() != null) {
				builder.append(" path = [");
				builder.append("]");
			}
			builder.append("\n");
		}
	}

	public void putObject(final String key, final Object value) {
		map.put(key, value);
	}

	public Object getObject(final String key) {
		return map.get(key);
	}

	public void removeObject(final String key) {
		map.remove(key);
	}
}
