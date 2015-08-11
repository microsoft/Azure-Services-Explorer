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

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.microsoft.intellij.helpers.LinkListener;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.azure.AzureManager;
import com.microsoft.tooling.msservices.helpers.azure.AzureManagerImpl;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class ImportSubscriptionForm extends DialogWrapper {
    private JPanel mainPanel;
    private JButton browseButton;
    private JTextField txtFile;
    private JLabel lblDownload;
    private JLabel lblPolicy;

    public ImportSubscriptionForm(Project project) {
        super(project, true);

        lblPolicy.addMouseListener(new LinkListener("http://msdn.microsoft.com/en-us/vstudio/dn425032.aspx"));
        lblDownload.addMouseListener(new LinkListener("http://go.microsoft.com/fwlink/?LinkID=301775"));

        this.setModal(true);
        this.setTitle("Import Microsoft Azure Subscriptions");

        final ImportSubscriptionForm form = this;
        this.setOKActionEnabled(false);
        this.setOKButtonText("Import");

        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false) {
                    @Override
                    public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
                        try {
                            return file.isDirectory() || (file.getExtension() != null && file.getExtension().equals("publishsettings"));
                        } catch (Throwable t) {
                            return super.isFileVisible(file, showHiddenFiles);
                        }
                    }

                    @Override
                    public boolean isFileSelectable(VirtualFile file) {
                        return (file.getExtension() != null && file.getExtension().equals("publishsettings"));
                    }
                };
                fileChooserDescriptor.setTitle("Choose Subscriptions File");

                FileChooser.chooseFile(fileChooserDescriptor, null, null, new Consumer<VirtualFile>() {
                    @Override
                    public void consume(VirtualFile virtualFile) {
                        if (virtualFile != null) {
                            form.setOKActionEnabled(true);
                            txtFile.setText(virtualFile.getPath());
                        }
                    }
                });
            }
        });


        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setOKActionEnabled(txtFile.getText() != null && !txtFile.getText().trim().isEmpty());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setOKActionEnabled(txtFile.getText() != null && !txtFile.getText().trim().isEmpty());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setOKActionEnabled(txtFile.getText() != null && !txtFile.getText().trim().isEmpty());
            }
        };

        txtFile.getDocument().addDocumentListener(documentListener);

        init();
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (new File(txtFile.getText()).exists()) {
            return null;
        } else {
            return new ValidationInfo("The specified subscription file does not exist.", txtFile);
        }

    }

    @Override
    protected void doOKAction() {

        try {
            AzureManager apiManager = AzureManagerImpl.getManager();
            apiManager.clearAuthentication();
            apiManager.importPublishSettingsFile(txtFile.getText());

            if (onSubscriptionLoaded != null)
                onSubscriptionLoaded.run();

            close(DialogWrapper.OK_EXIT_CODE, true);
        } catch (Throwable e) {
            DefaultLoader.getUIHelper().showException("An error occurred while attempting to import the subscription.", e,
                    "Azure Services Explorer - Error Importing Subscription", false, true);
        }

    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return mainPanel;
    }

    private Runnable onSubscriptionLoaded;

    public void setOnSubscriptionLoaded(Runnable onSubscriptionLoaded) {
        this.onSubscriptionLoaded = onSubscriptionLoaded;
    }
}