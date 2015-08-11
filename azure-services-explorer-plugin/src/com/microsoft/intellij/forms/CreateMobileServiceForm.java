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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.JBColor;
import com.microsoft.intellij.helpers.LinkListener;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoft.tooling.msservices.helpers.azure.AzureManagerImpl;
import com.microsoft.tooling.msservices.helpers.azure.rest.AzureRestAPIHelper;
import com.microsoft.tooling.msservices.model.Subscription;
import com.microsoft.tooling.msservices.model.ms.SqlDb;
import com.microsoft.tooling.msservices.model.ms.SqlServer;
import com.microsoft.tooling.msservices.model.vm.Location;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class CreateMobileServiceForm extends DialogWrapper {

    private final Project project;
    private JPanel mainPanel;
    private JTextField nameTextField;
    private JComboBox regionComboBox;
    private JComboBox serverComboBox;
    private JTextField serverUserNameTextField;
    private JPasswordField serverPasswordPasswordField;
    private JPasswordField serverPasswordConfirmationPasswordField;
    private JLabel lblPricing;
    private JLabel lblPasswordConfirmation;
    private JComboBox subscriptionComboBox;
    private JLabel lblPrivacy;
    private Runnable serviceCreated;


    public CreateMobileServiceForm(Project project) {
        super(project, true);

        this.project = project;

        this.setModal(true);
        this.setTitle("Create Mobile Service");

        lblPrivacy.setText("Online privacy statement");
        lblPrivacy.setForeground(JBColor.BLUE);
        lblPrivacy.addMouseListener(new LinkListener("http://msdn.microsoft.com/en-us/vstudio/dn425032.aspx"));

        lblPricing.addMouseListener(new LinkListener("http://www.azure.com/en-us/pricing/details/mobile-services/"));

        subscriptionComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getStateChange() == ItemEvent.SELECTED) {

                    subcriptionSelected((Subscription) subscriptionComboBox.getSelectedItem());
                }
            }
        });

        regionComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                for (int i = 0; i < serverComboBox.getItemCount(); i++) {
                    if (serverComboBox.getItemAt(i) instanceof SqlDb) {
                        SqlDb sqlDb = (SqlDb) serverComboBox.getItemAt(i);
                        if (sqlDb.getServer().getRegion().equals(regionComboBox.getSelectedItem().toString())) {
                            serverComboBox.setSelectedIndex(i);
                            return;
                        }
                    }
                }

                serverComboBox.setSelectedIndex(serverComboBox.getItemCount() - 1);

            }
        });

        serverComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                    updateVisibleFields(itemEvent.getItem());
                }
            }
        });

        DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    java.util.List<Subscription> subsList = AzureManagerImpl.getManager().getSubscriptionList();
                    DefaultComboBoxModel subscriptionDefaultComboBoxModel = new DefaultComboBoxModel(subsList.toArray(new Subscription[subsList.size()]));
                    subscriptionComboBox.setModel(subscriptionDefaultComboBoxModel);

                    if (subsList.size() > 0) {
                        subcriptionSelected(subsList.get(0));
                    }

                } catch (Throwable e) {

                    mainPanel.setCursor(Cursor.getDefaultCursor());
                    DefaultLoader.getUIHelper().showException("An error occurred while attempting to retrieve the " +
                                    "subscription list.", e,
                            "Azure Services Explorer - Error Retrieving Subscriptions", false, true);
                }
            }
        });

        init();

    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        String name = nameTextField.getText();
        String region = regionComboBox.getSelectedItem().toString();
        String server = (serverComboBox.getSelectedItem() instanceof SqlDb ? ((SqlDb) serverComboBox.getSelectedItem()).getServer().getName() : null);
        String admin = serverUserNameTextField.getText();
        String pass = new String(serverPasswordPasswordField.getPassword());
        String db = (serverComboBox.getSelectedItem() instanceof SqlDb ? ((SqlDb) serverComboBox.getSelectedItem()).getName() : null);
        String conf = new String(serverPasswordConfirmationPasswordField.getPassword());

        if (name.isEmpty()) {
            return new ValidationInfo("The service name must not be empty", nameTextField);
        }

        if (region.isEmpty()) {
            return new ValidationInfo("A region must be selected", regionComboBox);
        }

        if (admin.isEmpty()) {
            return new ValidationInfo("User name must not be empty", serverComboBox);
        }

        if (pass.isEmpty()) {
            return new ValidationInfo("Password must not be empty", serverPasswordPasswordField);
        }

        if (server != null && db == null) {
            return new ValidationInfo("Database must not be empty", serverComboBox);
        }

        if (!nameTextField.getText().matches("^[A-Za-z][A-Za-z0-9-]+[A-Za-z0-9]$")) {
            String error = "Invalid service name. Service name must start with a letter, \n" +
                    "contain only letters, numbers, and hyphens, " +
                    "and end with a letter or number.";

            return new ValidationInfo(error, nameTextField);
        }

        if (server == null) {
            if (!pass.matches("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$") || pass.contains(admin)) {
                String error = "Invalid password. The password must: \n" +
                        " - Not contain all login name\n" +
                        " - Have at least one upper case english letter\n" +
                        " - Have at least one lower case english letter\n" +
                        " - Have at least one digit\n" +
                        " - Have at least one special character\n" +
                        " - Be minimum 8 in length";

                return new ValidationInfo(error, serverPasswordPasswordField);
            }

            if (!pass.equals(conf)) {
                return new ValidationInfo("Password confirmation should match password", serverPasswordPasswordField);
            }
        }

        return null;
    }

    @Override
    protected void doOKAction() {

        final String id = ((Subscription) subscriptionComboBox.getSelectedItem()).getId();
        final String name = nameTextField.getText();
        final String region = regionComboBox.getSelectedItem().toString();
        final String server = (serverComboBox.getSelectedItem() instanceof SqlDb ? ((SqlDb) serverComboBox.getSelectedItem()).getServer().getName() : null);
        final String admin = serverUserNameTextField.getText();
        final String pass = new String(serverPasswordPasswordField.getPassword());
        final String db = (serverComboBox.getSelectedItem() instanceof SqlDb ? ((SqlDb) serverComboBox.getSelectedItem()).getName() : null);
        final CreateMobileServiceForm createMobileServiceForm = this;

        setEnabled(false);

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Creating service...", false) {

            @Override
            public void run(ProgressIndicator progressIndicator) {

                try {
                    progressIndicator.setText2("Checking name...");

                    if (AzureRestAPIHelper.existsMobileService(name)) {
                        JOptionPane.showMessageDialog(mainPanel, "The service name is used by another mobile service", "Error creating the service", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    progressIndicator.setText2("Creating service");
                    progressIndicator.setFraction(0.1);
                    AzureManagerImpl.getManager().createMobileService(id, region, admin, pass, name, server, db);

                    DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            serviceCreated.run();
                        }
                    });

                } catch (Throwable e) {

                    DefaultLoader.getUIHelper().showException("An error occurred while attempting to create the mobile service.",
                            e,
                            "Azure Services Explorer - Error Creating Mobile Service",
                            false,
                            true);
                }
            }
        });

        createMobileServiceForm.close(DialogWrapper.OK_EXIT_CODE, true);

    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return mainPanel;
    }

    private void setEnabled(boolean enabled) {
        for (Component component : getComponents(mainPanel)) {
            component.setEnabled(enabled);
        }

        this.setOKActionEnabled(enabled);
    }

    private Component[] getComponents(Component container) {
        ArrayList<Component> list = null;

        try {
            list = new ArrayList<Component>(Arrays.asList(
                    ((Container) container).getComponents()));
            for (int index = 0; index < list.size(); index++) {
                for (Component currentComponent : getComponents(list.get(index))) {
                    list.add(currentComponent);
                }
            }
        } catch (ClassCastException e) {
            list = new ArrayList<Component>();
        }

        return list.toArray(new Component[list.size()]);
    }


    private void updateVisibleFields(Object selectedServer) {
        boolean isExistingDb = selectedServer instanceof SqlDb;

        lblPasswordConfirmation.setVisible(!isExistingDb);
        serverPasswordConfirmationPasswordField.setVisible(!isExistingDb);

        if (isExistingDb) {
            SqlDb db = (SqlDb) selectedServer;
            serverUserNameTextField.setText(db.getServer().getAdmin());
        } else {
            serverUserNameTextField.setText("");
        }
    }

    private void subcriptionSelected(final Subscription subscription) {
        regionComboBox.setModel(new DefaultComboBoxModel(new String[]{"(loading...)"}));

        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<Location> locations = AzureManagerImpl.getManager().getLocations(subscription.getId());

                    ApplicationManager.getApplication().invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            regionComboBox.setModel(new DefaultComboBoxModel(locations.toArray()));
                        }
                    }, ModalityState.any());
                } catch (AzureCmdException e) {
                    DefaultLoader.getUIHelper().showException("An error occurred while attempting to retrieve " +
                                    "the location list.", e,
                            "Azure Services Explorer - Error Retrieving Locations", false, true);
                }
            }
        });

        serverComboBox.setModel(new DefaultComboBoxModel(new String[]{"(loading...)"}));

        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<SqlDb> databaseList = new ArrayList<SqlDb>();
                    List<SqlServer> sqlServerList = AzureManagerImpl.getManager().getSqlServers(subscription.getId());

                    ArrayList<Future<List<SqlDb>>> futures = new ArrayList<Future<List<SqlDb>>>();

                    for (final SqlServer server : sqlServerList) {
                        futures.add(ApplicationManager.getApplication().executeOnPooledThread(new Callable<List<SqlDb>>() {
                            @Override
                            public List<SqlDb> call() throws Exception {
                                return AzureManagerImpl.getManager().getSqlDb(subscription.getId(), server);
                            }
                        }));
                    }

                    for (Future<List<SqlDb>> future : futures) {
                        for (SqlDb sqlDb : future.get()) {
                            if (!sqlDb.getEdition().equals("System"))
                                databaseList.add(sqlDb);
                        }
                    }

                    ApplicationManager.getApplication().invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            DefaultComboBoxModel defaultComboBoxModel = new DefaultComboBoxModel(databaseList.toArray());

                            defaultComboBoxModel.addElement("<< Create a Free SQL Database >>");
                            serverComboBox.setModel(defaultComboBoxModel);

                            updateVisibleFields(defaultComboBoxModel.getSelectedItem());
                        }
                    }, ModalityState.any());
                } catch (Exception e) {
                    DefaultLoader.getUIHelper().showException("An error occurred while attempting to retrieve " +
                                    "the server and database list.", e,
                            "Azure Services Explorer - Error Retrieving Servers And Databases", false, true);
                }
            }
        });
    }

    public void setServiceCreated(Runnable serviceCreated) {
        this.serviceCreated = serviceCreated;
    }
}