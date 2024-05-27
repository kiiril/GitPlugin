package com.github.kiiril.gitplugin.components;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class LanguagePercentagePanel extends JPanel {

    public LanguagePercentagePanel(Map<String, Double> languagePercentageMap) {
        Map<String, Color> languageColorMap = getDefaultLanguageColors();
        setLayout(new BorderLayout());
        add(new LanguagePercentageBar(languagePercentageMap, languageColorMap), BorderLayout.CENTER);
        add(new LanguageLegend(languagePercentageMap, languageColorMap), BorderLayout.SOUTH);
    }

    private Map<String, Color> getDefaultLanguageColors() {
        Map<String, Color> colorMap = new HashMap<>();
        colorMap.put("Java", JBColor.ORANGE);  // Use custom colors similar to GitHub's colors
        colorMap.put("Python", JBColor.BLUE);
        colorMap.put("JavaScript", JBColor.YELLOW);
        colorMap.put("HTML", JBColor.RED);
        colorMap.put("CSS", JBColor.GREEN);
        return colorMap;
    }

    private static class LanguagePercentageBar extends JPanel {
        private final Map<String, Double> languagePercentageMap;
        private final Map<String, Color> languageColorMap;

        public LanguagePercentageBar(Map<String, Double> languagePercentageMap, Map<String, Color> languageColorMap) {
            this.languagePercentageMap = languagePercentageMap;
            this.languageColorMap = languageColorMap;
            setPreferredSize(new Dimension(400, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int width = getWidth();
            int height = getHeight();
            int arcSize = height / 2;
            int startX = 0;

            int count = 0;
            for (Map.Entry<String, Double> entry : languagePercentageMap.entrySet()) {
                String language = entry.getKey();
                Double percentage = entry.getValue();
                int barWidth = (int) (width * percentage / 100);
                g.setColor(languageColorMap.getOrDefault(language, JBColor.GRAY));

                if (count == 0) {
                    // Draw the first segment with rounded left corners
                    g.fillRoundRect(startX, 0, barWidth, height, arcSize, arcSize);
                    g.fillRect(startX + arcSize / 2, 0, barWidth - arcSize / 2, height);
                } else if (count == languagePercentageMap.size() - 1) {
                    // Draw the last segment with rounded right corners
                    g.fillRoundRect(startX, 0, barWidth, height, arcSize, arcSize);
                    g.fillRect(startX, 0, barWidth - arcSize / 2, height);
                } else {
                    // Draw intermediate segments as rectangles
                    g.fillRect(startX, 0, barWidth, height);
                }

                startX += barWidth;
                count++;
            }
        }
    }

    private static class LanguageLegend extends JPanel {
        public LanguageLegend(Map<String, Double> languagePercentageMap, Map<String, Color> languageColorMap) {
            setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
            for (Map.Entry<String, Double> entry : languagePercentageMap.entrySet()) {
                String language = entry.getKey();
                Double percentage = entry.getValue();
                JLabel label = new JLabel(String.format(" %s %.1f%%", language, percentage));
                label.setIcon(createIcon(languageColorMap.getOrDefault(language, JBColor.GRAY)));
                add(label);
            }
        }

        private Icon createIcon(Color color) {
            int size = 5;
            BufferedImage image = ImageUtil.createImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();
            g2.setColor(color);
            g2.fillOval(0, 0, size, size);
            g2.dispose();
            return new ImageIcon(image);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(400, 50);
        }
    }
}
