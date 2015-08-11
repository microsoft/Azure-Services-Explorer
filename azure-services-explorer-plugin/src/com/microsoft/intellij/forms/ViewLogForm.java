/**
 * Copyright (c) Microsoft Corporation
 * <p/>
 * All rights reserved.
 * <p/>
 * MIT License
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.intellij.forms;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.table.JBTable;
import com.microsoft.intellij.helpers.ReadOnlyCellTableModel;
import com.microsoft.intellij.helpers.UIHelperImpl;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.azure.AzureManagerImpl;
import com.microsoft.tooling.msservices.model.ms.LogEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Vector;

public class ViewLogForm extends DialogWrapper {
    private JTable logTable;
    private JPanel mainPanel;

    public ViewLogForm(Project project) {
        super(project, true);

        this.setTitle("Service Log");

        try {
            logTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            ReadOnlyCellTableModel model = new ReadOnlyCellTableModel();

            model.addColumn("Level");
            model.addColumn("Message");
            model.addColumn("Source");
            model.addColumn("Time Stamp");

            Vector<Object> loadingRow = new Vector<Object>();
            loadingRow.add("loading...");

            model.addRow(loadingRow);
            logTable.setModel(model);

            logTable.getColumn("Level").setCellRenderer(new DefaultTableCellRenderer() {
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

                    if (value.toString().equals("information")) {
                        setIcon(UIHelperImpl.loadIcon("loginfo.png"));
                        value = "Information";
                    } else if (value.toString().equals("error")) {
                        setIcon(UIHelperImpl.loadIcon("logerr.png"));
                        value = "Error";
                    } else if (value.toString().equals("warning")) {
                        setIcon(UIHelperImpl.loadIcon("logwarn.png"));
                        value = "Warning";
                    }

                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                    return this;
                }
            });
        } catch (Throwable ex) {
            getWindow().setCursor(Cursor.getDefaultCursor());
            DefaultLoader.getUIHelper().showException("An error occurred while attempting to load logs.", ex,
                    "Azure Services Explorer - Error Loading Logs", false, true);
        }

        init();
    }


    public void queryLog(String subscriptionId, String serviceName, String runtime) {

        try {
            getWindow().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            ReadOnlyCellTableModel model = (ReadOnlyCellTableModel) logTable.getModel();

            while (model.getRowCount() > 0)
                model.removeRow(0);

            for (LogEntry log : AzureManagerImpl.getManager().listLog(subscriptionId, serviceName, runtime)) {

                Vector<Object> row = new Vector<Object>();
                row.add(log.getType());
                row.add(log.getMessage());
                row.add(log.getSource());
                row.add(log.getTimeCreated());

                model.addRow(row);
            }

            getWindow().setCursor(Cursor.getDefaultCursor());
        } catch (Throwable ex) {
            getWindow().setCursor(Cursor.getDefaultCursor());
            DefaultLoader.getUIHelper().showException("An error occurred while attempting to query logs.", ex,
                    "Azure Services Explorer - Error Querying Logs", false, true);
        }
    }

    private void createUIComponents() {
        logTable = new JBTable();
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        return new Action[]{getOKAction()};
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return mainPanel;
    }
}
