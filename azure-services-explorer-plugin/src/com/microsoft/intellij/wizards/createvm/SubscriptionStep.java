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
package com.microsoft.intellij.wizards.createvm;

import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import com.microsoft.intellij.forms.ManageSubscriptionForm;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoft.tooling.msservices.helpers.azure.AzureManager;
import com.microsoft.tooling.msservices.helpers.azure.AzureManagerImpl;
import com.microsoft.tooling.msservices.model.Subscription;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Vector;

public class SubscriptionStep extends WizardStep<CreateVMWizardModel> {
    CreateVMWizardModel model;
    private JPanel rootPanel;
    private JList createVmStepsList;
    private JButton buttonLogin;
    private JComboBox subscriptionComboBox;
    private JLabel userInfoLabel;

    public SubscriptionStep(final CreateVMWizardModel model) {
        super("Choose a Subscription", null, null);

        this.model = model;

        model.configStepList(createVmStepsList, 0);

        buttonLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ManageSubscriptionForm form = new ManageSubscriptionForm(null);
                form.show();

                loadSubscriptions();
            }
        });

        subscriptionComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getItem() instanceof Subscription) {
                    model.setSubscription((Subscription) itemEvent.getItem());
                }
            }
        });
    }

    @Override
    public JComponent prepare(WizardNavigationState wizardNavigationState) {
        rootPanel.revalidate();

        loadSubscriptions();

        return rootPanel;
    }

    private void loadSubscriptions() {
        try {
            AzureManager manager = AzureManagerImpl.getManager();

            if (manager.authenticated()) {
                String upn = manager.getUserInfo().getUniqueName();
                userInfoLabel.setText("Signed in as: " + (upn.contains("#") ? upn.split("#")[1] : upn));
            } else {
                userInfoLabel.setText("");
            }

            List<Subscription> subscriptionList = manager.getSubscriptionList();

            final Vector<Subscription> subscriptions = new Vector<Subscription>(subscriptionList);
            subscriptionComboBox.setModel(new DefaultComboBoxModel(subscriptions));

            if (!subscriptions.isEmpty()) {
                model.setSubscription(subscriptions.get(0));
            }

            model.getCurrentNavigationState().NEXT.setEnabled(!subscriptions.isEmpty());
        } catch (AzureCmdException e) {
            DefaultLoader.getUIHelper().showException("An error occurred while attempting to load the subscriptions list.", e,
                    "Azure Services Explorer - Error Loading Subscriptions", false, true);
        }
    }
}