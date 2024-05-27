package com.github.kiiril.gitplugin.components;


import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CommitsTable extends JPanel {
    private final BatchedTableModel tableModel;

    public CommitsTable(String[][] completeDataSet, String author) {
        setLayout(new BorderLayout());

        String[] columnNames = {"Time", "Message", "Author"};
        tableModel = new BatchedTableModel(completeDataSet, columnNames);
        JTable table = new JBTable(tableModel);
        table.setDefaultRenderer(Object.class, new HighlightedRowRenderer(tableModel, author, 2));

        JButton loadMoreButton = new JButton("Load More");
        loadMoreButton.addActionListener(e -> tableModel.loadNextBatch());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loadMoreButton);

        JBScrollPane scrollPane = new JBScrollPane(table);
        scrollPane.setBorder(new LineBorder(JBColor.BLACK, 1, true));
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private static class BatchedTableModel extends DefaultTableModel {
        private final String[][] completeDataSet;
        private final int batchSize = 10;
        private int currentBatchStart = 0;
        private final List<String[]> currentBatch;

        public BatchedTableModel(String[][] completeDataSet, String[] columnNames) {
            super(columnNames, 0);
            this.completeDataSet = completeDataSet;
            this.currentBatch = new ArrayList<>();
            loadNextBatch();
        }
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public int getRowCount() {
            return currentBatch == null ? 0 : currentBatch.size();
        }

        @Override
        public int getColumnCount() {
            return super.getColumnCount();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            String[] rowData = currentBatch.get(rowIndex);
            return rowData[columnIndex];
        }

        public void loadNextBatch() {
            int nextBatchStart = currentBatchStart + batchSize;
            for (int i = currentBatchStart; i < Math.min(nextBatchStart, completeDataSet.length); i++) {
                String[] rowData = completeDataSet[i];
                currentBatch.add(rowData);
            }
            currentBatchStart = nextBatchStart;
            fireTableDataChanged();
        }

        public String[][] getCurrentBatch() {
            return currentBatch.toArray(new String[0][]);
        }
    }

    private static class HighlightedRowRenderer extends DefaultTableCellRenderer {
        private final BatchedTableModel tableModel;
        private final String author;
        private final int authorColumnIndex;

        public HighlightedRowRenderer(BatchedTableModel tableModel, String author, int authorColumnIndex) {
            this.tableModel = tableModel;
            this.author = author;
            this.authorColumnIndex = authorColumnIndex;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String[][] currentBatch = tableModel.getCurrentBatch();
            if (currentBatch[row][authorColumnIndex].equals(author)) {
                renderer.setBackground(JBColor.GREEN);
            } else {
                renderer.setBackground(table.getBackground());
            }

            return renderer;
        }
    }
}
