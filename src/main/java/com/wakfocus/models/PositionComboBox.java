package com.wakfocus.models;

import dorkbox.notify.Position;

public class PositionComboBox {
    private String label;
    private Position position;
    
    public PositionComboBox(String label, Position position) {
        this.label = label;
        this.position = position;
    }

    public String getLabel() {
        return label;
    }

    public Position getPosition() {
        return position;
    }
}
