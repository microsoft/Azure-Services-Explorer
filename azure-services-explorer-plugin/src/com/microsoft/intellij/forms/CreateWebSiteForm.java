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

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.IDEHelper;
import com.microsoft.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoft.tooling.msservices.helpers.azure.AzureManager;
import com.microsoft.tooling.msservices.helpers.azure.AzureManagerImpl;
import com.microsoft.tooling.msservices.helpers.tasks.CancellableTask;
import com.microsoft.tooling.msservices.helpers.tasks.CancellableTask.CancellableTaskHandle;
import com.microsoft.tooling.msservices.model.Subscription;
import com.microsoft.tooling.msservices.model.ws.WebHostingPlan;
import com.microsoft.tooling.msservices.model.ws.WebSite;
import com.microsoft.tooling.msservices.model.ws.WebSiteConfiguration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CreateWebSiteForm extends DialogWrapper {
    private static final String createWebHostingPlanLabel = "<< Create new App Service Plan >>";
    private JPanel mainPanel;
    private JComboBox subscriptionComboBox;
    private JTextField nameTextField;
    private JComboBox webHostingPlanComboBox;
    private JCheckBox enableJRECheckBox;
    private JComboBox webContainerComboBox;
    private JLabel webContainerLabel;

    private Project project;

    private Subscription subscription;
    private WebHostingPlan webHostingPlan;
    private CancellableTaskHandle fillWebHostinPlansTaskHandle;

    public CreateWebSiteForm(@org.jetbrains.annotations.Nullable Project project) {
        super(project, true, IdeModalityType.PROJECT);

        this.project = project;
        setTitle("Create Web App");

        subscriptionComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getItem() instanceof Subscription) {
                    subscription = (Subscription) itemEvent.getItem();
                    fillWebHostingPlans();
                }
            }
        });

        webHostingPlanComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getItem() instanceof WebHostingPlan) {
                    webHostingPlan = (WebHostingPlan) itemEvent.getItem();
                } else if (createWebHostingPlanLabel.equals(itemEvent.getItem())) {
                    webHostingPlan = null;
                    showCreateWebHostingPlanForm();
                }
            }
        });

        enableJRECheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                webContainerLabel.setEnabled(enableJRECheckBox.isSelected());
                webContainerComboBox.setEnabled(enableJRECheckBox.isSelected());
            }
        });

        webContainerComboBox.setModel(new DefaultComboBoxModel(new String[]{"TOMCAT", "JETTY"}));

        init();

        fillSubscriptions();
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

        if (webHostingPlan == null) {
            return new ValidationInfo("Select a valid App Service Plan", webHostingPlanComboBox);
        }

        return super.doValidate();
    }

    @Override
    protected void doOKAction() {
        AzureManager manager = AzureManagerImpl.getManager();
        mainPanel.getRootPane().getParent().setCursor(new Cursor(Cursor.WAIT_CURSOR));

        try {
            WebSite webSite = manager.createWebSite(subscription.getId(), webHostingPlan.getWebSpaceName(),
                    webHostingPlan.getName(), nameTextField.getText().trim());
            if (enableJRECheckBox.isSelected()) {
                WebSiteConfiguration webSiteConfiguration = manager.getWebSiteConfiguration(subscription.getId(),
                        webSite.getWebSpaceName(), webSite.getName());
                webSiteConfiguration.setJavaVersion("1.7.0_51");
                webSiteConfiguration.setJavaContainer((String) webContainerComboBox.getSelectedItem());

                manager.updateWebSiteConfiguration(subscription.getId(), webSite.getWebSpaceName(), webSite.getName(), webSiteConfiguration);
            }
        } catch (AzureCmdException e) {
            DefaultLoader.getUIHelper().showException("An error occurred while attempting to create web app.", e,
                    "Azure Services Explorer - Error Creating Web App", false, true);
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
        if (fillWebHostinPlansTaskHandle != null && !fillWebHostinPlansTaskHandle.isFinished()) {
            fillWebHostinPlansTaskHandle.cancel();
        }

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

    private void fillWebHostingPlans() {
        if (fillWebHostinPlansTaskHandle != null && !fillWebHostinPlansTaskHandle.isFinished()) {
            fillWebHostinPlansTaskHandle.cancel();
        }

        webHostingPlan = null;
        webHostingPlanComboBox.setModel(new DefaultComboBoxModel(new String[]{"<Loading...>"}));

        IDEHelper.ProjectDescriptor projectDescriptor = new IDEHelper.ProjectDescriptor(project.getName(),
                project.getBasePath() == null ? "" : project.getBasePath());

        try {
            fillWebHostinPlansTaskHandle = DefaultLoader.getIdeHelper().runInBackground(projectDescriptor, "Loading app service plans...", null, new CancellableTask() {
                final AzureManager manager = AzureManagerImpl.getManager();
                final Object lock = new Object();

                CancellationHandle cancellationHandle;
                List<WebHostingPlan> webHostingPlans;

                @Override
                public synchronized void run(final CancellationHandle cancellationHandle) throws Throwable {
                    this.cancellationHandle = cancellationHandle;
                    webHostingPlans = new ArrayList<WebHostingPlan>();

                    List<ListenableFuture<Void>> webSpaceFutures = new ArrayList<ListenableFuture<Void>>();

                    for (final String webSpaceName : manager.getWebSpaces(subscription.getId())) {
                        if (cancellationHandle.isCancelled()) {
                            return;
                        }

                        final SettableFuture<Void> webSpaceFuture = SettableFuture.create();

                        DefaultLoader.getIdeHelper().executeOnPooledThread(new Runnable() {
                            @Override
                            public void run() {
                                loadWebHostingPlans(subscription, webSpaceName, webSpaceFuture);
                            }
                        });

                        webSpaceFutures.add(webSpaceFuture);
                    }

                    try {
                        Futures.allAsList(webSpaceFutures).get();
                    } catch (InterruptedException e) {
                        throw new AzureCmdException(e.getMessage(), e);
                    } catch (ExecutionException e) {
                        throw new AzureCmdException(e.getCause().getMessage(), e.getCause());
                    }
                }

                @Override
                public void onCancel() {
                }

                @Override
                public void onSuccess() {
                    DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            DefaultComboBoxModel appServicePlanComboModel =
                                    new DefaultComboBoxModel(webHostingPlans.toArray());
                            appServicePlanComboModel.insertElementAt(createWebHostingPlanLabel, 0);
                            appServicePlanComboModel.setSelectedItem(null);
                            webHostingPlanComboBox.setModel(appServicePlanComboModel);

                            if (!webHostingPlans.isEmpty()) {
                                webHostingPlanComboBox.setSelectedIndex(1);
                            }
                        }
                    });
                }

                @Override
                public void onError(Throwable throwable) {
                    DefaultLoader.getUIHelper().showException("An error occurred while trying to load the app service plans",
                            throwable, "Azure Services Explorer - Error Loading App Service Plans", false, true);
                }

                private void loadWebHostingPlans(final Subscription subscription, final String webSpace,
                                                 final SettableFuture<Void> webSpaceFuture) {
                    try {
                        List<WebHostingPlan> webHostingPlans = manager.getWebHostingPlans(subscription.getId(), webSpace);

                        synchronized (lock) {
                            this.webHostingPlans.addAll(webHostingPlans);
                        }

                        webSpaceFuture.set(null);
                    } catch (AzureCmdException e) {
                        webSpaceFuture.setException(e);
                    }
                }
            });
        } catch (AzureCmdException e) {
            DefaultLoader.getUIHelper().showException("An error occurred while trying to load the app service plans",
                    e, "Azure Services Explorer - Error Loading App Service Plans", false, true);
        }
    }

    private void showCreateWebHostingPlanForm() {
        final CreateWebHostingPlanForm form = new CreateWebHostingPlanForm(project);
        form.show();

        DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
            @Override
            public void run() {
                fillWebHostingPlans();
            }
        });
    }
}