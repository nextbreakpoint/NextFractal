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
package com.nextbreakpoint.nextfractal.core.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Not thread safe
 */
public abstract class EventBus {
    private static final Logger logger = Logger.getLogger(EventBus.class.getName());

    private final Map<String, List<EventListener>> registeredListeners = new HashMap<>();
    private final String name;

    public EventBus(String name) {
        this.name = name;
    }

    public final void subscribe(String channel, EventListener listener) {
        final List<EventListener> listeners = registeredListeners.getOrDefault(channel, new ArrayList<>());
        listeners.add(listener);
        registeredListeners.put(channel, listeners);
    }

    public final void unsubscribe(String channel, EventListener listener) {
        final List<EventListener> listeners = registeredListeners.get(channel);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                registeredListeners.remove(channel);
            }
        }
    }

    public abstract void postEvent(Object event);

    protected final void postEvent(String channel, Object event) {
        try {
            logger.log(Level.FINE, "Dispatching event: bus: " + name + ", channel: " + channel + ": " + event.toString());
            dispatchEvent(channel, event);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Can't process event", e);
        }
    }

    private void dispatchEvent(String channel, Object event) {
        final List<EventListener> listeners = registeredListeners.get(channel);
        if (listeners != null) {
            listeners.forEach(listener -> listener.onEvent(event));
        }
    }
}
