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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import com.intellij.util.Consumer;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoft.tooling.msservices.helpers.azure.AzureManagerImpl;
import com.microsoft.tooling.msservices.model.vm.VirtualMachineImage;
import com.microsoft.tooling.msservices.model.vm.VirtualMachineSize;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MachineSettingsStep extends WizardStep<CreateVMWizardModel> {
    private JPanel rootPanel;
    private JList createVmStepsList;
    private JEditorPane imageDescriptionTextPane;
    private JTextField vmNameTextField;
    private JComboBox vmSizeComboBox;
    private JTextField vmUserTextField;
    private JPasswordField vmPasswordField;
    private JPasswordField confirmPasswordField;
    private JCheckBox passwordCheckBox;
    private JButton certificateButton;
    private JTextField certificateField;
    private JCheckBox certificateCheckBox;
    private JPanel certificatePanel;
    private JPanel passwordPanel;

    Project project;
    CreateVMWizardModel model;

    public MachineSettingsStep(CreateVMWizardModel mModel, Project project) {
        super("Virtual Machine Basic Settings", null, null);

        this.project = project;
        this.model = mModel;

        model.configStepList(createVmStepsList, 2);

        DocumentListener documentListener = new DocumentListener() {
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
        };

        vmNameTextField.getDocument().addDocumentListener(documentListener);
        vmUserTextField.getDocument().addDocumentListener(documentListener);
        certificateField.getDocument().addDocumentListener(documentListener);
        vmPasswordField.getDocument().addDocumentListener(documentListener);
        confirmPasswordField.getDocument().addDocumentListener(documentListener);

        imageDescriptionTextPane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
                if (hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    if (Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().browse(hyperlinkEvent.getURL().toURI());
                        } catch (Exception e) {
                            DefaultLoader.getUIHelper().showException("An error occurred while attempting to open the " +
                                            "specified Link.", e,
                                    "Azure Services Explorer - Error Opening Link", false, true);
                        }
                    }
                }
            }
        });

        certificateCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                for (Component component : certificatePanel.getComponents()) {
                    component.setEnabled(certificateCheckBox.isSelected());
                }

                certificatePanel.setEnabled(certificateCheckBox.isSelected());

                validateEmptyFields();
            }
        });

        passwordCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                for (Component component : passwordPanel.getComponents()) {
                    component.setEnabled(passwordCheckBox.isSelected());
                }

                passwordPanel.setEnabled(passwordCheckBox.isSelected());

                validateEmptyFields();
            }
        });

        certificateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false) {
                    @Override
                    public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
                        try {
                            return file.isDirectory() || (file.getExtension() != null && file.getExtension().equals("cer"));
                        } catch (Throwable t) {
                            return super.isFileVisible(file, showHiddenFiles);
                        }
                    }

                    @Override
                    public boolean isFileSelectable(VirtualFile file) {
                        return (file.getExtension() != null && file.getExtension().equals("cer"));
                    }
                };

                fileChooserDescriptor.setTitle("Choose Certificate File");

                FileChooser.chooseFile(fileChooserDescriptor, null, null, new Consumer<VirtualFile>() {
                    @Override
                    public void consume(VirtualFile virtualFile) {
                        if (virtualFile != null) {
                            certificateField.setText(virtualFile.getPath());
                        }
                    }
                });
            }
        });
    }

    @Override
    public JComponent prepare(WizardNavigationState wizardNavigationState) {
        rootPanel.revalidate();

        final VirtualMachineImage virtualMachineImage = model.getVirtualMachineImage();

        if (virtualMachineImage.getOperatingSystemType().equals("Linux")) {
            certificateCheckBox.setEnabled(true);
            passwordCheckBox.setEnabled(true);
            certificateCheckBox.setSelected(true);
            passwordCheckBox.setSelected(false);
        } else {
            certificateCheckBox.setSelected(false);
            passwordCheckBox.setSelected(true);
            certificateCheckBox.setEnabled(false);
            passwordCheckBox.setEnabled(false);
        }

        validateEmptyFields();

        imageDescriptionTextPane.setText(model.getHtmlFromVMImage(virtualMachineImage));
        imageDescriptionTextPane.setCaretPosition(0);

        if (vmSizeComboBox.getItemCount() == 0) {
            vmSizeComboBox.setModel(new DefaultComboBoxModel(new String[]{"<Loading...>"}));

            ProgressManager.getInstance().run(new Task.Backgroundable(project, "Loading VM sizes...", false) {
                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    progressIndicator.setIndeterminate(true);

                    try {
                        final List<VirtualMachineSize> virtualMachineSizes = AzureManagerImpl.getManager().getVirtualMachineSizes(model.getSubscription().getId().toString());

                        Collections.sort(virtualMachineSizes, new Comparator<VirtualMachineSize>() {
                            @Override
                            public int compare(VirtualMachineSize t0, VirtualMachineSize t1) {
                                if (t0.getName().contains("Basic") && t1.getName().contains("Basic")) {
                                    return t0.getName().compareTo(t1.getName());
                                } else if (t0.getName().contains("Basic")) {
                                    return -1;
                                } else if (t1.getName().contains("Basic")) {
                                    return 1;
                                }

                                int coreCompare = Integer.valueOf(t0.getCores()).compareTo(t1.getCores());

                                if (coreCompare == 0) {
                                    return Integer.valueOf(t0.getMemoryInMB()).compareTo(t1.getMemoryInMB());
                                } else {
                                    return coreCompare;
                                }
                            }
                        });

                        ApplicationManager.getApplication().invokeAndWait(new Runnable() {
                            @Override
                            public void run() {
                                vmSizeComboBox.setModel(new DefaultComboBoxModel(virtualMachineSizes.toArray()));

                                selectDefaultSize();
                            }
                        }, ModalityState.any());
                    } catch (AzureCmdException e) {
                        DefaultLoader.getUIHelper().showException("An error occurred while attempting to load the VM " +
                                        "sizes list.", e,
                                "Azure Services Explorer - Error Loading VM Sizes", false, true);
                    }
                }
            });
        } else {
            selectDefaultSize();
        }

        return rootPanel;
    }

    @Override
    public WizardStep onNext(CreateVMWizardModel model) {
        WizardStep wizardStep = super.onNext(model);

        String name = vmNameTextField.getText();

        if (name.length() > 15 || name.length() < 3) {
            JOptionPane.showMessageDialog(null, "Invalid virtual machine name. The name must be between 3 and 15 character long.", "Error creating the virtual machine", JOptionPane.ERROR_MESSAGE);
            return this;
        }

        if (!name.matches("^[A-Za-z][A-Za-z0-9-]+[A-Za-z0-9]$")) {
            JOptionPane.showMessageDialog(null, "Invalid virtual machine name. The name must start with a letter, \n" +
                    "contain only letters, numbers, and hyphens, " +
                    "and end with a letter or number.", "Error creating the virtual machine", JOptionPane.ERROR_MESSAGE);
            return this;
        }

        String password = passwordCheckBox.isSelected() ? new String(vmPasswordField.getPassword()) : "";

        if (passwordCheckBox.isSelected()) {
            String conf = new String(confirmPasswordField.getPassword());

            if (!password.equals(conf)) {
                JOptionPane.showMessageDialog(null, "Password confirmation should match password", "Error creating the service", JOptionPane.ERROR_MESSAGE);
                return this;
            }

            if (!password.matches("(?=^.{8,255}$)((?=.*\\d)(?=.*[A-Z])(?=.*[a-z])|(?=.*\\d)(?=.*[^A-Za-z0-9])(?=.*[a-z])|(?=.*[^A-Za-z0-9])(?=.*[A-Z])(?=.*[a-z])|(?=.*\\d)(?=.*[A-Z])(?=.*[^A-Za-z0-9]))^.*")) {
                JOptionPane.showMessageDialog(null, "The password does not conform to complexity requirements.\n" +
                        "It should be at least eight characters long and contain a mixture of upper case, lower case, digits and symbols.", "Error creating the virtual machine", JOptionPane.ERROR_MESSAGE);
                return this;
            }
        }

        String certificate = certificateCheckBox.isSelected() ? certificateField.getText() : "";

        model.setName(name);
        model.setSize((VirtualMachineSize) vmSizeComboBox.getSelectedItem());
        model.setUserName(vmUserTextField.getText());
        model.setPassword(password);
        model.setCertificate(certificate);

        return wizardStep;
    }

    private void selectDefaultSize() {
        ApplicationManager.getApplication().invokeAndWait(new Runnable() {
            @Override
            public void run() {
                String recommendedVMSize = model.getVirtualMachineImage().getRecommendedVMSize().isEmpty()
                        ? "Small"
                        : model.getVirtualMachineImage().getRecommendedVMSize();

                for (int i = 0; i < vmSizeComboBox.getItemCount(); i++) {
                    VirtualMachineSize virtualMachineSize = (VirtualMachineSize) vmSizeComboBox.getItemAt(i);
                    if (virtualMachineSize.getName().equals(recommendedVMSize)) {
                        vmSizeComboBox.setSelectedItem(virtualMachineSize);
                        break;
                    }
                }
            }
        }, ModalityState.any());
    }

    private void validateEmptyFields() {
        boolean allFieldsCompleted = !(
                vmNameTextField.getText().isEmpty()
                        || vmUserTextField.getText().isEmpty()
                        || !(passwordCheckBox.isSelected() || certificateCheckBox.isSelected())
                        || (passwordCheckBox.isSelected() &&
                        (vmPasswordField.getPassword().length == 0
                                || confirmPasswordField.getPassword().length == 0))
                        || (certificateCheckBox.isSelected() && certificateField.getText().isEmpty()));

        model.getCurrentNavigationState().NEXT.setEnabled(allFieldsCompleted);
    }
}