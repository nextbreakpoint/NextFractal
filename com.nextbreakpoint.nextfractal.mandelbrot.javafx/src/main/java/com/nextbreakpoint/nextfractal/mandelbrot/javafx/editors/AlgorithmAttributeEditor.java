package com.nextbreakpoint.nextfractal.mandelbrot.javafx.editors;

import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.javafx.AttributeEditor;
import com.nextbreakpoint.nextfractal.core.params.Attribute;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;

public class AlgorithmAttributeEditor extends AttributeEditor {
    private final Attribute attribute;
    private final ComboBox<String> comboBox;

    public AlgorithmAttributeEditor(Attribute attribute) {
        this.attribute = attribute;

		comboBox = new ComboBox<>();
		comboBox.getItems().add("Mandelbrot");
		comboBox.getItems().add("Julia/Fatou");
		comboBox.getStyleClass().add("text-small");
        comboBox.getSelectionModel().select(0);

        comboBox.setTooltip(new Tooltip(attribute.getName()));

        setCenter(comboBox);

        widthProperty().addListener((_, _, newValue) -> {
            comboBox.setPrefWidth(newValue.doubleValue());
        });

        comboBox.setOnAction(_ -> {
            if (getDelegate() != null) {
                getDelegate().onEditorChanged(this);
            }
		});
    }

    @Override
    public void loadSession(Session session) {
        comboBox.getSelectionModel().select(attribute.getMapper().apply(session));
    }

    @Override
    public Session updateSession(Session session) {
        return attribute.getCombiner().apply(session, comboBox.getSelectionModel().getSelectedItem());
    }
}
