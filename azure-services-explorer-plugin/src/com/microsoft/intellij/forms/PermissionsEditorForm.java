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
import com.intellij.ui.CheckBoxList;
import com.intellij.ui.CheckBoxListListener;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.uiDesigner.core.GridConstraints;
import com.microsoft.tooling.msservices.model.Office365Permission;
import com.microsoft.tooling.msservices.model.Office365PermissionList;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PermissionsEditorForm extends DialogWrapper {
    private JPanel contentPane;
    private JPanel panelPermissions;
    private CheckBoxList<Office365Permission> listPermissions;
    private Office365PermissionList permissions;
    private Runnable onOK;

    public PermissionsEditorForm(String title, Office365PermissionList office365Permissions, Project project) {
        super(project, true);

        this.permissions = new Office365PermissionList(office365Permissions.size());
        for (Office365Permission p : office365Permissions) {
            this.permissions.add(p.clone());
        }

        setTitle("Edit Permissions : " + title);
        setModal(true);

        // populate list box
        listPermissions = new CheckBoxList<Office365Permission>();
        GridConstraints constraints = new GridConstraints();
        constraints.setRow(0);
        constraints.setColumn(0);
        constraints.setFill(GridConstraints.FILL_BOTH);

        JBScrollPane jbScrollPane = new JBScrollPane(listPermissions,
                JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        panelPermissions.add(jbScrollPane, constraints);
        for (Office365Permission permission : this.permissions) {
            listPermissions.addItem(permission, permission.getDescription(), permission.isEnabled());
        }

        // this updates the datamodel when the checkbox is clicked in the listbox
        listPermissions.setCheckBoxListListener(new CheckBoxListListener() {
            @Override
            public void checkBoxSelectionChanged(int i, boolean b) {
                permissions.get(i).setEnabled(b);
            }
        });

        init();
    }

    public Office365PermissionList getPermissions() {
        return permissions;
    }

    public void setOnOK(Runnable onOK) {
        this.onOK = onOK;

    }

    @Override
    protected void doOKAction() {
        if (onOK != null) {
            onOK.run();
        }
        close(DialogWrapper.OK_EXIT_CODE, true);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }
}
