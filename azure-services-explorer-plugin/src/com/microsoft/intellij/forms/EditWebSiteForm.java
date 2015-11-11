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
import com.microsoft.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoft.tooling.msservices.helpers.azure.AzureManager;
import com.microsoft.tooling.msservices.helpers.azure.AzureManagerImpl;
import com.microsoft.tooling.msservices.model.ws.WebSite;
import com.microsoft.tooling.msservices.model.ws.WebSiteConfiguration;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class EditWebSiteForm extends DialogWrapper {
    private JPanel mainPanel;
    private JTextField nameTextField;
    private JCheckBox enableJRECheckBox;
    private JComboBox webContainerComboBox;
    private JLabel webContainerLabel;

    private Project project;
    private WebSite webSite;
    private WebSiteConfiguration webSiteConfiguration;

    public EditWebSiteForm(@Nullable Project project, WebSite webSite, WebSiteConfiguration webSiteConfiguration) {
        super(project, true, IdeModalityType.PROJECT);

        this.project = project;
        this.webSite = webSite;
        this.webSiteConfiguration = webSiteConfiguration;
        setTitle("Edit Web App");

        enableJRECheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                webContainerLabel.setEnabled(enableJRECheckBox.isSelected());
                webContainerComboBox.setEnabled(enableJRECheckBox.isSelected());
            }
        });

        init();

        nameTextField.setText(webSite.getName());
        webContainerComboBox.setModel(new DefaultComboBoxModel(new String[]{"TOMCAT", "JETTY"}));

        if (!webSiteConfiguration.getJavaContainer().equals("")) {
            enableJRECheckBox.setSelected(true);
            webContainerComboBox.setEnabled(true);
            webContainerComboBox.setSelectedItem(webSiteConfiguration.getJavaContainer());
        } else {
            enableJRECheckBox.setSelected(false);
            webContainerComboBox.setEnabled(false);
        }
    }

    @org.jetbrains.annotations.Nullable
    @Override
    protected ValidationInfo doValidate() {
        return super.doValidate();
    }

    @Override
    protected void doOKAction() {
        AzureManager manager = AzureManagerImpl.getManager();
        mainPanel.getRootPane().getParent().setCursor(new Cursor(Cursor.WAIT_CURSOR));

        try {
            if (enableJRECheckBox.isSelected()) {
                webSiteConfiguration.setJavaVersion("1.7.0_51");
                webSiteConfiguration.setJavaContainer((String) webContainerComboBox.getSelectedItem());
            } else {
                webSiteConfiguration.setJavaVersion("");
                webSiteConfiguration.setJavaContainer("");
            }

            manager.updateWebSiteConfiguration(webSite.getSubscriptionId(), webSite.getWebSpaceName(), webSite.getName(), webSiteConfiguration);
        } catch (AzureCmdException e) {
            DefaultLoader.getUIHelper().showException("An error occurred while attempting to edit web app configuration.", e,
                    "Azure Services Explorer - Error Editing Web App Configuration", false, true);
        } finally {
            mainPanel.getRootPane().getParent().setCursor(Cursor.getDefaultCursor());
        }

        super.doOKAction();
    }

    @org.jetbrains.annotations.Nullable
    @Override
    protected JComponent createCenterPanel() {
        return mainPanel;
    }

    @Override
    protected void dispose() {
        super.dispose();
    }
}