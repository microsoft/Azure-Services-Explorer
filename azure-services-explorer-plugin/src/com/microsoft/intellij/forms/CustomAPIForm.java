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
import com.intellij.openapi.ui.ValidationInfo;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.azure.AzureManagerImpl;
import com.microsoft.tooling.msservices.model.ms.CustomAPI;
import com.microsoft.tooling.msservices.model.ms.CustomAPIPermissions;
import com.microsoft.tooling.msservices.model.ms.PermissionItem;
import com.microsoft.tooling.msservices.model.ms.PermissionType;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class CustomAPIForm extends DialogWrapper {
    private JPanel mainPanel;
    private JTextField tableNameTextField;
    private JComboBox getPermissionComboBox;
    private JComboBox postPermissionComboBox;
    private JComboBox putPermissionComboBox;
    private JComboBox patchPermissionComboBox;
    private JComboBox deletePermissionComboBox;
    private String subscriptionId;
    private String serviceName;
    private Project project;
    private CustomAPI editingCustomAPI;
    private Runnable afterSave;
    private List<String> existingApiNames;


    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public CustomAPIForm(Project project) {

        super(project, true);
        this.project = project;

        this.setModal(true);
        this.setTitle("Create New Custom API");
        setOKActionEnabled(false);


        final PermissionItem[] tablePermissions = PermissionItem.getTablePermissions();

        getPermissionComboBox.setModel(new DefaultComboBoxModel(tablePermissions));
        postPermissionComboBox.setModel(new DefaultComboBoxModel(tablePermissions));
        putPermissionComboBox.setModel(new DefaultComboBoxModel(tablePermissions));
        patchPermissionComboBox.setModel(new DefaultComboBoxModel(tablePermissions));
        deletePermissionComboBox.setModel(new DefaultComboBoxModel(tablePermissions));

        tableNameTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                super.keyTyped(keyEvent);

                setOKActionEnabled(!tableNameTextField.getText().isEmpty());
            }
        });

        setOKButtonText("Create");

        init();
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        String apiName = tableNameTextField.getText().trim();

        if (!apiName.matches("^[A-Za-z][A-Za-z0-9_]+")) {
            new ValidationInfo("Invalid api name. Api name must start with a letter, \n" +
                    "contain only letters, numbers, and undercores.", tableNameTextField);

        }

        return null;
    }

    @Override
    protected void doOKAction() {
        final String apiName = tableNameTextField.getText().trim();
        final CustomAPIPermissions permissions = new CustomAPIPermissions();

        permissions.setPatchPermission(((PermissionItem) patchPermissionComboBox.getSelectedItem()).getType());
        permissions.setDeletePermission(((PermissionItem) deletePermissionComboBox.getSelectedItem()).getType());
        permissions.setGetPermission(((PermissionItem) getPermissionComboBox.getSelectedItem()).getType());
        permissions.setPostPermission(((PermissionItem) postPermissionComboBox.getSelectedItem()).getType());
        permissions.setPutPermission(((PermissionItem) putPermissionComboBox.getSelectedItem()).getType());

        DefaultLoader.getIdeHelper().runInBackground(project, "Saving Custom API", false, true, "Saving Custom API", new Runnable() {
            @Override
            public void run() {
                try {

                    if (existingApiNames == null) {
                        existingApiNames = new ArrayList<String>();

                        for (CustomAPI api : AzureManagerImpl.getManager().getAPIList(subscriptionId, serviceName)) {
                            existingApiNames.add(api.getName().toLowerCase());
                        }
                    }

                    if (editingCustomAPI == null && existingApiNames.contains(apiName.toLowerCase())) {

                        JOptionPane.showMessageDialog(mainPanel, "Invalid API name. An API with that name already exists in this service.",
                                "Service Explorer", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (editingCustomAPI == null) {
                        AzureManagerImpl.getManager().createCustomAPI(subscriptionId, serviceName, apiName, permissions);
                    } else {
                        AzureManagerImpl.getManager().updateCustomAPI(subscriptionId, serviceName, apiName, permissions);
                        editingCustomAPI.setCustomAPIPermissions(permissions);
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

    private int permissionIndex(PermissionItem[] p, PermissionType pt) {
        for (int i = 0; i < p.length; i++) {
            if (p[i].getType() == pt)
                return i;
        }
        return 0;
    }

    public void setEditingCustomAPI(CustomAPI editingCustomAPI) {
        this.editingCustomAPI = editingCustomAPI;


        this.setTitle(editingCustomAPI == null ? "Create new custom API" : "Edit custom API");

        setOKButtonText(editingCustomAPI == null ? "Create" : "Save");
        this.tableNameTextField.setText(editingCustomAPI == null ? "" : editingCustomAPI.getName());
        this.tableNameTextField.setEnabled(editingCustomAPI == null);
        setOKActionEnabled(editingCustomAPI != null);

        PermissionItem[] tablePermissions = PermissionItem.getTablePermissions();

        if (editingCustomAPI != null) {
            getPermissionComboBox.setSelectedIndex(permissionIndex(tablePermissions, editingCustomAPI.getCustomAPIPermissions().getGetPermission()));
            deletePermissionComboBox.setSelectedIndex(permissionIndex(tablePermissions, editingCustomAPI.getCustomAPIPermissions().getDeletePermission()));
            patchPermissionComboBox.setSelectedIndex(permissionIndex(tablePermissions, editingCustomAPI.getCustomAPIPermissions().getPatchPermission()));
            postPermissionComboBox.setSelectedIndex(permissionIndex(tablePermissions, editingCustomAPI.getCustomAPIPermissions().getPostPermission()));
            putPermissionComboBox.setSelectedIndex(permissionIndex(tablePermissions, editingCustomAPI.getCustomAPIPermissions().getPutPermission()));
        }

    }

    public CustomAPI getEditingCustomAPI() {
        return editingCustomAPI;
    }

    public void setAfterSave(Runnable editSaved) {
        this.afterSave = editSaved;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return mainPanel;
    }
}
