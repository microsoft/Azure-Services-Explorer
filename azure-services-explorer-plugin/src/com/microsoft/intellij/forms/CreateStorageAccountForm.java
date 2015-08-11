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

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.ListCellRendererWrapper;
import com.microsoft.intellij.helpers.LinkListener;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoft.tooling.msservices.helpers.azure.AzureManagerImpl;
import com.microsoft.tooling.msservices.model.Subscription;
import com.microsoft.tooling.msservices.model.storage.StorageAccount;
import com.microsoft.tooling.msservices.model.vm.AffinityGroup;
import com.microsoft.tooling.msservices.model.vm.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

public class CreateStorageAccountForm extends DialogWrapper {
    private JPanel contentPane;
    private JComboBox subscriptionComboBox;
    private JTextField nameTextField;
    private JComboBox regionOrAffinityGroupComboBox;
    private JComboBox replicationComboBox;
    private JLabel pricingLabel;
    private JLabel userInfoLabel;

    private Runnable onCreate;
    private Subscription subscription;
    private StorageAccount storageAccount;
    private Project project;

    private boolean isLoading = true;

    private final String PRICING_LINK = "http://go.microsoft.com/fwlink/?LinkID=400838";

    private enum ReplicationTypes {
        Standard_LRS,
        Standard_GRS,
        Standard_RAGRS;

        public String getDescription() {
            switch (this) {
                case Standard_GRS:
                    return "Geo-Redundant";
                case Standard_LRS:
                    return "Locally Redundant";
                case Standard_RAGRS:
                    return "Read Access Geo-Redundant";
            }

            return super.toString();
        }
    }

    public CreateStorageAccountForm(Project project) {
        super(project, true);

        this.project = project;

        setModal(true);
        setTitle("Create Storage Account");

        pricingLabel.addMouseListener(new LinkListener(PRICING_LINK));

        regionOrAffinityGroupComboBox.setRenderer(new ListCellRendererWrapper<Object>() {

            @Override
            public void customize(JList jList, Object o, int i, boolean b, boolean b1) {
                if (!(o instanceof String) && o != null) {
                    setText("  " + o.toString());
                }
            }
        });

        nameTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                validateEmptyFields();
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                validateEmptyFields();
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                validateEmptyFields();
            }
        });

        regionOrAffinityGroupComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                validateEmptyFields();
            }
        });

        if (AzureManagerImpl.getManager().authenticated()) {
            String upn = AzureManagerImpl.getManager().getUserInfo().getUniqueName();
            userInfoLabel.setText("Signed in as: " + (upn.contains("#") ? upn.split("#")[1] : upn));
        } else {
            userInfoLabel.setText("");
        }

        replicationComboBox.setModel(new DefaultComboBoxModel(ReplicationTypes.values()));
        replicationComboBox.setRenderer(new ListCellRendererWrapper<ReplicationTypes>() {
            @Override
            public void customize(JList jList, ReplicationTypes replicationTypes, int i, boolean b, boolean b1) {
                setText(replicationTypes.getDescription());
            }
        });

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    private void validateEmptyFields() {
        boolean allFieldsCompleted = !(
                nameTextField.getText().isEmpty() || regionOrAffinityGroupComboBox.getSelectedObjects().length == 0);

        setOKActionEnabled(!isLoading && allFieldsCompleted);
    }


    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (nameTextField.getText().length() < 3
                || nameTextField.getText().length() > 24
                || !nameTextField.getText().matches("[a-z0-9]+")) {
            return new ValidationInfo("Invalid storage account name. The name should be between 3 and 24 characters long and \n" +
                    "can contain only lowercase letters and numbers.", nameTextField);
        }

        return null;
    }

    @Override
    protected void doOKAction() {

        final String name = nameTextField.getText();
        final String region = (regionOrAffinityGroupComboBox.getSelectedItem() instanceof Location) ? regionOrAffinityGroupComboBox.getSelectedItem().toString() : "";
        final String affinityGroup = (regionOrAffinityGroupComboBox.getSelectedItem() instanceof AffinityGroup) ? regionOrAffinityGroupComboBox.getSelectedItem().toString() : "";
        final String replication = replicationComboBox.getSelectedItem().toString();

        DefaultLoader.getIdeHelper().runInBackground(project, "Creating storage account", false, true, "Creating storage account...", new Runnable() {
            @Override
            public void run() {
                try {
                    storageAccount = new StorageAccount(name, subscription.getId().toString());
                    storageAccount.setType(replication);
                    storageAccount.setLocation(region);
                    storageAccount.setAffinityGroup(affinityGroup);

                    AzureManagerImpl.getManager().createStorageAccount(storageAccount);
                    AzureManagerImpl.getManager().refreshStorageAccountInformation(storageAccount);

                    DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (onCreate != null) {
                                onCreate.run();
                            }
                        }
                    });
                } catch (AzureCmdException e) {
                    storageAccount = null;
                    DefaultLoader.getUIHelper().showException("An error occurred while attempting to create the specified " +
                                    "storage account.", e,
                            "Azure Services Explorer - Error Creating Storage Account", false, true);
                }
            }
        });

        close(DialogWrapper.OK_EXIT_CODE, true);
    }


    public void fillFields(final Subscription subscription) {
        final CreateStorageAccountForm createStorageAccountForm = this;
        if (subscription == null) {
            try {
                subscriptionComboBox.setEnabled(true);

                java.util.List<Subscription> fullSubscriptionList = AzureManagerImpl.getManager().getFullSubscriptionList();
                subscriptionComboBox.setModel(new DefaultComboBoxModel(new Vector<Subscription>(fullSubscriptionList)));
                subscriptionComboBox.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent itemEvent) {
                        createStorageAccountForm.subscription = (Subscription) itemEvent.getItem();
                        loadRegions();
                    }
                });

                if (fullSubscriptionList.size() > 0) {
                    createStorageAccountForm.subscription = fullSubscriptionList.get(0);
                    loadRegions();
                }
            } catch (AzureCmdException e) {
                DefaultLoader.getUIHelper().showException("An error occurred while attempting to get subscriptions.", e,
                        "Azure Services Explorer - Error Getting Subscriptions", false, true);
            }
        } else {
            this.subscription = subscription;
            subscriptionComboBox.addItem(subscription.getName());

            loadRegions();
        }
    }

    public void setOnCreate(Runnable onCreate) {
        this.onCreate = onCreate;
    }

    public StorageAccount getStorageAccount() {
        return storageAccount;
    }

    public void loadRegions() {
        isLoading = true;

        regionOrAffinityGroupComboBox.addItem("<Loading...>");

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Loading regions...", false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                progressIndicator.setIndeterminate(true);

                try {
                    java.util.List<AffinityGroup> affinityGroups = AzureManagerImpl.getManager().getAffinityGroups(subscription.getId().toString());
                    java.util.List<Location> locations = AzureManagerImpl.getManager().getLocations(subscription.getId().toString());

                    final Vector<Object> vector = new Vector<Object>();
                    vector.add("Regions");
                    vector.addAll(locations);
                    if (affinityGroups.size() > 0) {
                        vector.add("Affinity Groups");
                        vector.addAll(affinityGroups);
                    }

                    DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            isLoading = false;

                            validateEmptyFields();

                            regionOrAffinityGroupComboBox.removeAllItems();
                            regionOrAffinityGroupComboBox.setModel(new DefaultComboBoxModel(vector) {
                                public void setSelectedItem(Object o) {
                                    if (!(o instanceof String)) {
                                        super.setSelectedItem(o);
                                    }
                                }
                            });

                            regionOrAffinityGroupComboBox.setSelectedIndex(1);
                        }
                    });
                } catch (AzureCmdException e) {
                    DefaultLoader.getUIHelper().showException("An error occurred while attempting to load the regions list",
                            e, "Azure Services Explorer - Error Loading Regions", false, true);
                }
            }
        });
    }
}
