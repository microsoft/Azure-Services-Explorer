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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.azure.AzureManagerImpl;
import com.microsoft.tooling.msservices.model.ms.PermissionItem;
import com.microsoft.tooling.msservices.model.ms.PermissionType;
import com.microsoft.tooling.msservices.model.ms.Table;
import com.microsoft.tooling.msservices.model.ms.TablePermissions;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;


public class TableForm extends DialogWrapper {
    private JPanel mainPanel;
    private JTextField tableNameTextField;
    private JComboBox insertPermisssionComboBox;
    private JComboBox updatePermissionComboBox;
    private JComboBox deletePermissionComboBox;
    private JComboBox readPermissionComboBox;
    private String subscriptionId;
    private String serviceName;
    private Project project;
    private Table editingTable;
    private Runnable afterSave;


    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setAfterSave(Runnable afterSave) {
        this.afterSave = afterSave;
    }

    private ArrayList<String> existingTableNames;

    public void setEditingTable(Table editingTable) {

        this.editingTable = editingTable;

        this.setTitle(editingTable == null ? "Create new table" : "Edit table");
        this.tableNameTextField.setText(editingTable == null ? "" : editingTable.getName());
        this.tableNameTextField.setEnabled(editingTable == null);
        setOKActionEnabled(editingTable != null);
        setOKButtonText(editingTable == null ? "Create" : "Save");

        PermissionItem[] tablePermissions = PermissionItem.getTablePermissions();

        if (editingTable != null) {
            deletePermissionComboBox.setSelectedIndex(permissionIndex(tablePermissions, editingTable.getTablePermissions().getDelete()));
            insertPermisssionComboBox.setSelectedIndex(permissionIndex(tablePermissions, editingTable.getTablePermissions().getInsert()));
            readPermissionComboBox.setSelectedIndex(permissionIndex(tablePermissions, editingTable.getTablePermissions().getRead()));
            updatePermissionComboBox.setSelectedIndex(permissionIndex(tablePermissions, editingTable.getTablePermissions().getUpdate()));
        }
    }

    public TableForm(Project project) {
        super(project, true);
        this.project = project;

        this.setModal(true);
        this.setTitle("Create New Table");

        setOKActionEnabled(false);
        setOKButtonText("Create");

        tableNameTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                super.keyTyped(keyEvent);

                setOKActionEnabled(!tableNameTextField.getText().isEmpty());
            }
        });

        final PermissionItem[] tablePermissions = PermissionItem.getTablePermissions();

        insertPermisssionComboBox.setModel(new DefaultComboBoxModel(tablePermissions));
        deletePermissionComboBox.setModel(new DefaultComboBoxModel(tablePermissions));
        updatePermissionComboBox.setModel(new DefaultComboBoxModel(tablePermissions));
        readPermissionComboBox.setModel(new DefaultComboBoxModel(tablePermissions));

        init();
    }

    @Override
    protected void doOKAction() {
        final String tableName = tableNameTextField.getText().trim();
        final TablePermissions tablePermissions = new TablePermissions();

        tablePermissions.setDelete(((PermissionItem) deletePermissionComboBox.getSelectedItem()).getType());
        tablePermissions.setUpdate(((PermissionItem) updatePermissionComboBox.getSelectedItem()).getType());
        tablePermissions.setRead(((PermissionItem) readPermissionComboBox.getSelectedItem()).getType());
        tablePermissions.setInsert(((PermissionItem) insertPermisssionComboBox.getSelectedItem()).getType());

        DefaultLoader.getIdeHelper().runInBackground(project, "Saving table", false, true, "Saving table...", new Runnable() {
            @Override
            public void run() {
                try {

                    if (editingTable == null) {
                        AzureManagerImpl.getManager().createTable(subscriptionId, serviceName, tableName, tablePermissions);
                    } else {
                        AzureManagerImpl.getManager().updateTable(subscriptionId, serviceName, tableName, tablePermissions);
                    }

                    DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (afterSave != null) {
                                afterSave.run();
                            }


                        }
                    });
                } catch (Throwable e) {
                    DefaultLoader.getUIHelper().showException("An error occurred while attempting to save the table.", e,
                            "Azure Services Explorer - Error Saving Table", false, true);
                }
            }
        });

        close(DialogWrapper.OK_EXIT_CODE, true);
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        final String tableName = tableNameTextField.getText().trim();

        if (!tableName.matches("^[A-Za-z][A-Za-z0-9_]+")) {
            return new ValidationInfo("Invalid table name. Table name must start with a letter, \n" +
                    "contain only letters, numbers, and underscores.", tableNameTextField);
        }

        int tableNameIndex = -1;
        if (existingTableNames != null) {
            tableNameIndex = Iterables.indexOf(existingTableNames, new Predicate<String>() {
                @Override
                public boolean apply(String name) {
                    return tableName.equalsIgnoreCase(name);
                }
            });
        }

        if (tableNameIndex != -1) {
            return new ValidationInfo("Invalid table name. A table with that name already exists in this service.", tableNameTextField);
        }

        return null;
    }

    private int permissionIndex(PermissionItem[] p, PermissionType pt) {
        for (int i = 0; i < p.length; i++) {
            if (p[i].getType() == pt)
                return i;
        }
        return 0;
    }

    public void setExistingTableNames(ArrayList<String> existingTableNames) {
        this.existingTableNames = existingTableNames;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return mainPanel;
    }
}
