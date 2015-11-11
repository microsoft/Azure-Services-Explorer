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
import com.microsoft.tooling.msservices.model.Subscription;
import com.microsoft.tooling.msservices.model.ws.WebHostingPlan;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

public class CreateWebHostingPlanForm extends DialogWrapper {
    private JPanel mainPanel;
    private JComboBox subscriptionComboBox;
    private JTextField nameTextField;
    private JComboBox geoRegionComboBox;

    private Project project;

    private Subscription subscription;
    private String geoRegion;

    public CreateWebHostingPlanForm(@org.jetbrains.annotations.Nullable Project project) {
        super(project, true, IdeModalityType.PROJECT);

        this.project = project;
        setTitle("Create App Service Plan");

        subscriptionComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getItem() instanceof Subscription) {
                    subscription = (Subscription) itemEvent.getItem();
                }
            }
        });

        geoRegionComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getItem() instanceof String) {
                    geoRegion = (String) itemEvent.getItem();
                }
            }
        });

        init();

        fillSubscriptions();
        fillGeoRegions();
    }

    @org.jetbrains.annotations.Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (subscription == null) {
            return new ValidationInfo("Select a valid subscription", subscriptionComboBox);
        }

        String name = nameTextField.getText().trim();

        if (name.length() > 60 || !name.matches("^[A-Za-z0-9][A-Za-z0-9-]*[A-Za-z0-9]$")) {
            StringBuilder builder = new StringBuilder();
            builder.append("The name must be a string that contains between 2 and 60 characters. ");
            builder.append("The field can contain only letters, numbers, and hyphens. ");
            builder.append("The first and last character in the field must be a letter or a number.");

            return new ValidationInfo(builder.toString(), nameTextField);
        }

        if (geoRegion == null) {
            return new ValidationInfo("Select a valid Geo Region", geoRegionComboBox);
        }

        return super.doValidate();
    }

    @Override
    protected void doOKAction() {
        AzureManager manager = AzureManagerImpl.getManager();
        mainPanel.getRootPane().getParent().setCursor(new Cursor(Cursor.WAIT_CURSOR));

        try {
            String webSpaceName = manager.getWebSpaceName(geoRegion);
            manager.createWebHostingPlan(subscription.getId(),
                    new WebHostingPlan(nameTextField.getText().trim(), webSpaceName, subscription.getId()));
        } catch (AzureCmdException e) {
            DefaultLoader.getUIHelper().showException("An error occurred while attempting to create app service plan.", e,
                    "Azure Services Explorer - Error Creating App Service Plan", false, true);
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

    private void fillSubscriptions() {
        try {
            List<Subscription> subscriptionList = AzureManagerImpl.getManager().getSubscriptionList();

            DefaultComboBoxModel subscriptionComboModel = new DefaultComboBoxModel(subscriptionList.toArray());
            subscriptionComboModel.setSelectedItem(null);
            subscriptionComboBox.setModel(subscriptionComboModel);

            if (!subscriptionList.isEmpty()) {
                subscriptionComboBox.setSelectedIndex(0);
            }
        } catch (AzureCmdException e) {
            DefaultLoader.getUIHelper().showException("An error occurred while trying to load the subscriptions list",
                    e, "Azure Services Explorer - Error Loading Subscriptions", false, true);
        }
    }

    private void fillGeoRegions() {
        try {
            List<String> geoRegionList = AzureManagerImpl.getManager().getWebSiteGeoRegionNames();

            DefaultComboBoxModel geoRegionComboModel = new DefaultComboBoxModel(geoRegionList.toArray());
            geoRegionComboModel.setSelectedItem(null);
            geoRegionComboBox.setModel(geoRegionComboModel);

            if (!geoRegionList.isEmpty()) {
                geoRegionComboBox.setSelectedIndex(0);
            }
        } catch (AzureCmdException e) {
            DefaultLoader.getUIHelper().showException("An error occurred while trying to load the geo region list",
                    e, "Azure Services Explorer - Error Loading Geo Regions", false, true);
        }
    }
}