package com.nextbreakpoint.nextfractal.flux;

import javafx.scene.layout.Pane;

public interface FractalFactory {
	/**
	 * @return
	 */
	public String getId();

	/**
	 * @return
	 */
	public FractalSession createSession();
	
	/**
	 * @param session
	 * @return
	 */
	public Pane createEditorPane(FractalSession session);

	/**
	 * @param session
	 * @param width
	 * @param height
	 * @return
	 */
	public Pane createRenderPane(FractalSession session, int width, int height);
}
