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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.IDEHelper.ArtifactDescriptor;
import com.microsoft.tooling.msservices.helpers.IDEHelper.ProjectDescriptor;
import com.microsoft.tooling.msservices.helpers.NotNull;
import com.microsoft.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoft.tooling.msservices.helpers.azure.AzureManager;
import com.microsoft.tooling.msservices.helpers.azure.AzureManagerImpl;
import com.microsoft.tooling.msservices.helpers.tasks.CancellableTask;
import com.microsoft.tooling.msservices.helpers.tasks.CancellableTask.CancellableTaskHandle;
import com.microsoft.tooling.msservices.model.Subscription;
import com.microsoft.tooling.msservices.model.ws.WebSite;
import com.microsoft.tooling.msservices.model.ws.WebSiteConfiguration;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class WebSiteDeployForm extends DialogWrapper {
    private JPanel mainPanel;
    private JList webSiteJList;
    private JButton buttonEditSubscriptions;
    private JButton buttonAddApp;
    private JButton buttonEditApp;
    private Project project;
    private WebSite selectedWebSite;
    private WebSite editableWebSite;
    private WebSiteConfiguration editableWebSiteConfiguration;
    private CancellableTaskHandle fillListTaskHandle;

    public WebSiteDeployForm(@org.jetbrains.annotations.Nullable Project project) {
        super(project, true, IdeModalityType.PROJECT);

        this.project = project;
        setTitle("Deploy Artifact in Azure Web Apps");

        buttonAddApp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                createWebApp();
            }
        });

        buttonEditApp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editWebApp();
            }
        });

        buttonEditSubscriptions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editSubscriptions();
            }
        });

        init();
        fillList();
    }

    @Override
    protected boolean postponeValidation() {
        return true;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    protected ValidationInfo doValidate() {
        return (selectedWebSite != null) ? null : new ValidationInfo("Select a valid web app as target for deployment", webSiteJList);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    protected JComponent createCenterPanel() {
        return mainPanel;
    }

    @Override
    protected void dispose() {
        if (fillListTaskHandle != null && !fillListTaskHandle.isFinished()) {
            fillListTaskHandle.cancel();
        }

        super.dispose();
    }

    public void deploy() throws AzureCmdException {
        ProjectDescriptor projectDescriptor = new ProjectDescriptor(project.getName(),
                project.getBasePath() == null ? "" : project.getBasePath());
        WebSite webSite = this.selectedWebSite;

        AzureManager manager = AzureManagerImpl.getManager();
        ArtifactDescriptor artifactDescriptor = manager.getWebArchiveArtifact(projectDescriptor);

        if (artifactDescriptor != null) {
            manager.deployWebArchiveArtifact(projectDescriptor, artifactDescriptor, webSite);
        }
    }

    private void fillList() {
        if (fillListTaskHandle != null && !fillListTaskHandle.isFinished()) {
            fillListTaskHandle.cancel();
        }

        webSiteJList.setModel(getMessageListModel("(loading... )"));

        for (ListSelectionListener selectionListener : webSiteJList.getListSelectionListeners()) {
            webSiteJList.removeListSelectionListener(selectionListener);
        }

        buttonEditApp.setEnabled(false);

        ProjectDescriptor projectDescriptor = new ProjectDescriptor(project.getName(),
                project.getBasePath() == null ? "" : project.getBasePath());

        try {
            fillListTaskHandle = DefaultLoader.getIdeHelper().runInBackground(projectDescriptor, "Retrieving web apps info...", null, new CancellableTask() {
                final AzureManager manager = AzureManagerImpl.getManager();
                final Object lock = new Object();

                CancellationHandle cancellationHandle;
                List<Subscription> subscriptionList;
                Map<WebSite, WebSiteConfiguration> webSiteConfigMap;

                @Override
                public synchronized void run(final CancellationHandle cancellationHandle) throws Throwable {
                    this.cancellationHandle = cancellationHandle;
                    subscriptionList = manager.getSubscriptionList();
                    webSiteConfigMap = new HashMap<WebSite, WebSiteConfiguration>();

                    List<ListenableFuture<Void>> subscriptionFutures = new ArrayList<ListenableFuture<Void>>();

                    for (final Subscription subscription : subscriptionList) {
                        if (cancellationHandle.isCancelled()) {
                            return;
                        }

                        final SettableFuture<Void> subscriptionFuture = SettableFuture.create();

                        DefaultLoader.getIdeHelper().executeOnPooledThread(new Runnable() {
                            @Override
                            public void run() {
                                loadWebSiteConfigurations(subscription, subscriptionFuture);
                            }
                        });

                        subscriptionFutures.add(subscriptionFuture);
                    }

                    try {
                        Futures.allAsList(subscriptionFutures).get();
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
                            if (subscriptionList.isEmpty()) {
                                setMessages("Please sign in/import your Azure subscriptions.");
                                editSubscriptions(subscriptionList);
                            } else if (webSiteConfigMap.isEmpty()) {
                                setMessages("There are no Azure Web Apps on the imported subscriptions");
                                editSubscriptions(subscriptionList);
                            } else {
                                setWebApps(webSiteConfigMap);
                            }
                        }
                    });
                }

                @Override
                public void onError(@NotNull Throwable throwable) {
                    DefaultLoader.getUIHelper().showException("An error occurred while trying to load the web apps info",
                            throwable, "Azure Services Explorer - Error Loading Web Apps Info", false, true);
                }

                private void loadWebSiteConfigurations(final Subscription subscription,
                                                       final SettableFuture<Void> subscriptionFuture) {
                    try {
                        List<ListenableFuture<Void>> webSpaceFutures = new ArrayList<ListenableFuture<Void>>();

                        for (final String webSpace : manager.getWebSpaces(subscription.getId())) {
                            if (cancellationHandle.isCancelled()) {
                                subscriptionFuture.set(null);
                                return;
                            }

                            final SettableFuture<Void> webSpaceFuture = SettableFuture.create();

                            DefaultLoader.getIdeHelper().executeOnPooledThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadWebSiteConfigurations(subscription, webSpace, webSpaceFuture);
                                }
                            });

                            webSpaceFutures.add(webSpaceFuture);
                        }

                        Futures.addCallback(Futures.allAsList(webSpaceFutures), new FutureCallback<List<Void>>() {
                            @Override
                            public void onSuccess(List<Void> voids) {
                                subscriptionFuture.set(null);
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                subscriptionFuture.setException(throwable);
                            }
                        });
                    } catch (AzureCmdException ex) {
                        subscriptionFuture.setException(ex);
                    }
                }

                private void loadWebSiteConfigurations(final Subscription subscription, final String webSpace,
                                                       final SettableFuture<Void> webSpaceFuture) {
                    try {
                        List<ListenableFuture<Void>> webSiteFutures = new ArrayList<ListenableFuture<Void>>();

                        for (final WebSite webSite : manager.getWebSites(subscription.getId(), webSpace)) {
                            if (cancellationHandle.isCancelled()) {
                                webSpaceFuture.set(null);
                                return;
                            }

                            final SettableFuture<Void> webSiteFuture = SettableFuture.create();

                            DefaultLoader.getIdeHelper().executeOnPooledThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadWebSiteConfigurations(subscription, webSpace, webSite, webSiteFuture);
                                }
                            });

                            webSiteFutures.add(webSiteFuture);
                        }

                        Futures.addCallback(Futures.allAsList(webSiteFutures), new FutureCallback<List<Void>>() {
                            @Override
                            public void onSuccess(List<Void> voids) {
                                webSpaceFuture.set(null);
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                webSpaceFuture.setException(throwable);
                            }
                        });
                    } catch (AzureCmdException ex) {
                        webSpaceFuture.setException(ex);
                    }
                }

                private void loadWebSiteConfigurations(final Subscription subscription, final String webSpace,
                                                       final WebSite webSite,
                                                       final SettableFuture<Void> webSiteFuture) {
                    WebSiteConfiguration webSiteConfiguration;

                    try {
                        webSiteConfiguration = AzureManagerImpl.getManager().
                                getWebSiteConfiguration(webSite.getSubscriptionId(),
                                        webSite.getWebSpaceName(), webSite.getName());
                    } catch (Throwable ignore) {
                        webSiteConfiguration = new WebSiteConfiguration(webSpace, webSite.getName(),
                                subscription.getId());
                    }

                    synchronized (lock) {
                        webSiteConfigMap.put(webSite, webSiteConfiguration);
                    }

                    webSiteFuture.set(null);
                }
            });
        } catch (AzureCmdException e) {
            DefaultLoader.getUIHelper().showException("An error occurred while trying to load the web apps info",
                    e, "Azure Services Explorer - Error Loading Web Apps Info", false, true);
        }
    }

    private void setWebApps(@NotNull final Map<WebSite, WebSiteConfiguration> webSiteConfigMap) {
        final List<WebSite> webSiteList = new ArrayList<WebSite>(webSiteConfigMap.keySet());
        Collections.sort(webSiteList, new Comparator<WebSite>() {
            @Override
            public int compare(WebSite ws1, WebSite ws2) {
                return ws1.getName().compareTo(ws2.getName());
            }
        });

        selectedWebSite = null;
        editableWebSite = null;
        editableWebSiteConfiguration = null;

        webSiteJList.setModel(new AbstractListModel() {
            @Override
            public int getSize() {
                return webSiteList.size();
            }

            @Override
            public Object getElementAt(int i) {
                WebSite webSite = webSiteList.get(i);
                WebSiteConfiguration webSiteConfiguration = webSiteConfigMap.get(webSite);

                StringBuilder builder = new StringBuilder(webSite.getName());

                if (!webSiteConfiguration.getJavaVersion().isEmpty()) {
                    builder.append(" (JRE ");
                    builder.append(webSiteConfiguration.getJavaVersion());

                    if (!webSiteConfiguration.getJavaContainer().isEmpty()) {
                        builder.append("; ");
                        builder.append(webSiteConfiguration.getJavaContainer());
                        builder.append(" ");
                        builder.append(webSiteConfiguration.getJavaContainerVersion());
                    }

                    builder.append(")");
                } else {
                    builder.append(" (.NET ");
                    builder.append(webSiteConfiguration.getNetFrameworkVersion());

                    if (!webSiteConfiguration.getPhpVersion().isEmpty()) {
                        builder.append("; PHP ");
                        builder.append(webSiteConfiguration.getPhpVersion());
                    }

                    builder.append(")");
                }

                return builder.toString();
            }
        });

        webSiteJList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                int index = webSiteJList.getSelectedIndex();


                if (isDeployable(webSiteConfigMap, webSiteList, index)) {
                    selectedWebSite = webSiteList.get(index);
                } else {
                    selectedWebSite = null;
                }

                if (index >= 0 && webSiteList.size() > index) {
                    buttonEditApp.setEnabled(true);
                    editableWebSite = webSiteList.get(index);
                    editableWebSiteConfiguration = webSiteConfigMap.get(webSiteList.get(index));
                } else {
                    buttonEditApp.setEnabled(false);
                    editableWebSite = null;
                    editableWebSiteConfiguration = null;
                }
            }
        });

        webSiteJList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel jLabel = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                Border border = jLabel.getBorder();
                Border margin = new EmptyBorder(0, 2, 0, 2);
                jLabel.setBorder(new CompoundBorder(border, margin));

                if (!isDeployable(webSiteConfigMap, webSiteList, index)) {
                    jLabel.setBackground(list.getBackground());
                    jLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
                }

                return jLabel;
            }
        });
    }

    private static boolean isDeployable(@NotNull Map<WebSite, WebSiteConfiguration> webSiteConfigMap,
                                        @NotNull List<WebSite> webSiteList,
                                        int index) {
        return index >= 0 && webSiteList.size() > index && !webSiteConfigMap.get(webSiteList.get(index)).getJavaContainer().isEmpty();
    }

    private void setMessages(String... messages) {
        webSiteJList.setModel(getMessageListModel(messages));
    }

    @NotNull
    private static AbstractListModel getMessageListModel(final String... messages) {
        return new AbstractListModel() {
            @Override
            public int getSize() {
                return messages.length;
            }

            @Override
            public Object getElementAt(int index) {
                return messages[index];
            }
        };
    }

    private void createWebApp() {
        CreateWebSiteForm form = new CreateWebSiteForm(project);
        form.show();

        if (form.isOK()) {
            fillList();
        }
    }

    private void editWebApp() {
        EditWebSiteForm form = new EditWebSiteForm(project, editableWebSite, editableWebSiteConfiguration);
        form.show();

        if (form.isOK()) {
            fillList();
        }
    }

    private void editSubscriptions() {
        try {
            editSubscriptions(AzureManagerImpl.getManager().getSubscriptionList());
        } catch (AzureCmdException e) {
            setMessages("There has been an error while retrieving the configured Azure subscriptions.",
                    "Please retry signing in/importing your Azure subscriptions.");
        }
    }

    private void editSubscriptions(List<Subscription> previousSubscriptionList) {
        try {
            ManageSubscriptionForm form = new ManageSubscriptionForm(project);
            form.show();

            List<Subscription> subscriptionList = AzureManagerImpl.getManager().getSubscriptionList();

            if (!(previousSubscriptionList.containsAll(subscriptionList) &&
                    subscriptionList.containsAll(previousSubscriptionList))) {
                if (subscriptionList.size() == 0) {
                    setMessages("Please sign in/import your Azure subscriptions.");
                } else {
                    fillList();
                }
            }
        } catch (AzureCmdException e) {
            setMessages("There has been an error while retrieving the configured Azure subscriptions.",
                    "Please retry signing in/importing your Azure subscriptions.");
        }
    }
}