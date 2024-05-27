package com.github.kiiril.gitplugin.components;

import com.intellij.openapi.ui.ComboBox;

import javax.swing.*;
import java.awt.*;

public class ExternalLibrariesList extends JPanel {
    public ExternalLibrariesList(String[] externalLibraries) {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        ComboBox<String> comboBox = new ComboBox<>(externalLibraries);
        add(comboBox);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, 40);
    }
}
