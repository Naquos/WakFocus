package com.wakfocus.models;

public class ThemeComboBox {
    private String label;
    private ThemeEnum theme;

    public ThemeComboBox(String label, ThemeEnum theme) {
        this.label = label;
        this.theme = theme;
    }

    public String getLabel() {
        return label;
    }

    public ThemeEnum getTheme() {
        return theme;
    }
}
