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
import com.intellij.ui.ListCellRendererWrapper;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoft.tooling.msservices.helpers.azure.AzureManagerImpl;
import com.microsoft.tooling.msservices.model.Subscription;
import com.microsoft.tooling.msservices.model.vm.AffinityGroup;
import com.microsoft.tooling.msservices.model.vm.CloudService;
import com.microsoft.tooling.msservices.model.vm.Location;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.Vector;


public class CreateCloudServiceForm extends DialogWrapper {
    private JPanel contentPane;
    private JComboBox subscriptionComboBox;
    private JTextField nameTextField;
    private JComboBox regionOrAffinityGroupComboBox;

    private Subscription subscription;
    private CloudService cloudService;
    private Runnable onCreate;
    private Project project;

    private static String LOADING_MSG = "<Loading...>";

    public CreateCloudServiceForm(Project project) {
        super(project, true);
        setModal(true);

        this.project = project;

        setTitle("Create Cloud Service");

        regionOrAffinityGroupComboBox.setRenderer(new ListCellRendererWrapper() {
            @Override
            public void customize(JList jList, Object o, int i, boolean b, boolean b1) {
                if (!(o instanceof String)) {
                    if (o instanceof AffinityGroup) {
                        AffinityGroup ag = (AffinityGroup) o;
                        setText(String.format("  %s (%s)", ag.getName(), ag.getLocation()));
                    } else {
                        setText("  " + o.toString());
                    }
                }
            }
        });

        init();
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (nameTextField.getText().isEmpty()) {
            return new ValidationInfo("Name cannot be empty", nameTextField);
        } else if (regionOrAffinityGroupComboBox.getSelectedObjects().length == 0 || regionOrAffinityGroupComboBox.getSelectedItem().equals(LOADING_MSG)) {
            return new ValidationInfo("A region or affinity group must be selected", regionOrAffinityGroupComboBox);
        } else if (!nameTextField.getText().matches("^[A-Za-z0-9][A-Za-z0-9-]+[A-Za-z0-9]$")) {
            return new ValidationInfo("Invalid cloud service name. Cloud service name must start with a letter or number, \n" +
                    "contain only letters, numbers, and hyphens, " +
                    "and end with a letter or number.", nameTextField);
        }

        return null;
    }


    public void fillFields(final Subscription subscription) {
        this.subscription = subscription;

        subscriptionComboBox.addItem(subscription.getName());

        regionOrAffinityGroupComboBox.addItem(LOADING_MSG);

        DefaultLoader.getIdeHelper().runInBackground(project, "Loading regions...", false, true, "Loading regions...", new Runnable() {
            @Override
            public void run() {
                try {
                    final List<AffinityGroup> affinityGroups = AzureManagerImpl.getManager().getAffinityGroups(subscription.getId());
                    final List<Location> locations = AzureManagerImpl.getManager().getLocations(subscription.getId());

                    DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            final Vector<Object> vector = new Vector<Object>();
                            vector.add("Regions");
                            vector.addAll(locations);
                            if (affinityGroups.size() > 0) {
                                vector.add("Affinity Groups");
                                vector.addAll(affinityGroups);
                            }

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


    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    @Override
    protected void doOKAction() {
        DefaultLoader.getIdeHelper().runInBackground(project, "Creating cloud service", false, true, "Creating cloud service...", new Runnable() {
            @Override
            public void run() {
                try {
                    String name = nameTextField.getText();
                    Object regionOrAffinity = regionOrAffinityGroupComboBox.getSelectedItem();
                    String location = (regionOrAffinity instanceof Location) ?
                            ((Location) regionOrAffinity).getName() :
                            "";
                    String affinityGroup = (regionOrAffinity instanceof AffinityGroup) ?
                            ((AffinityGroup) regionOrAffinity).getName() :
                            "";

                    cloudService = new CloudService(name, location, affinityGroup, subscription.getId());
                    AzureManagerImpl.getManager().createCloudService(cloudService);
                } catch (Exception e) {
                    cloudService = null;
                    DefaultLoader.getUIHelper().showException("An error occurred while attempting to create " +
                                    "the specified cloud service", e,
                            "Azure Services Explorer - Error Creating Storage Account", false, true);
                }

                DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        onCreate.run();
                    }
                });

            }
        });

        this.close(DialogWrapper.OK_EXIT_CODE, true);
    }

    public CloudService getCloudService() {
        return cloudService;
    }

    public void setOnCreate(Runnable onCreate) {
        this.onCreate = onCreate;
    }
}