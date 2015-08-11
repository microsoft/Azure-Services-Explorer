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
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoft.tooling.msservices.helpers.azure.AzureManagerImpl;
import com.microsoft.tooling.msservices.model.vm.VirtualMachineImage;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class SelectImageStep extends WizardStep<CreateVMWizardModel> {
    private JPanel rootPanel;
    private JList createVmStepsList;
    private JComboBox imageTypeComboBox;
    private JList imageLabelList;
    private JEditorPane imageDescriptionTextPane;
    private JPanel imageInfoPanel;

    CreateVMWizardModel model;

    private void createUIComponents() {
        imageInfoPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {

                double height = 0;
                for (Component component : this.getComponents()) {
                    height += component.getHeight();
                }

                Dimension preferredSize = super.getPreferredSize();
                preferredSize.setSize(preferredSize.getWidth(), height);
                return preferredSize;
            }
        };
    }

    private enum PublicImages {
        WindowsServer,
        SharePoint,
        BizTalkServer,
        SQLServer,
        VisualStudio,
        Linux,
        Other;

        @Override
        public String toString() {
            switch (this) {
                case WindowsServer:
                    return "Windows Server";
                case BizTalkServer:
                    return "BizTalk Server";
                case SQLServer:
                    return "SQL Server";
                case VisualStudio:
                    return "Visual Studio";
                default:
                    return super.toString();
            }
        }
    }

    private enum MSDNImages {
        BizTalkServer,
        Dynamics,
        VisualStudio,
        Other;

        @Override
        public String toString() {
            switch (this) {
                case BizTalkServer:
                    return "BizTalk Server";
                case VisualStudio:
                    return "Visual Studio";
                default:
                    return super.toString();
            }
        }
    }

    private enum PrivateImages {
        VMImages;

        @Override
        public String toString() {
            switch (this) {
                case VMImages:
                    return "VM Images";
                default:
                    return super.toString();
            }
        }
    }

    Map<Enum, List<VirtualMachineImage>> virtualMachineImages;
    private Project project;

    public SelectImageStep(final CreateVMWizardModel model, Project project) {
        super("Select a Virtual Machine Image", null, null);

        this.model = model;
        this.project = project;

        model.configStepList(createVmStepsList, 1);

        final ArrayList<Object> imageTypeList = new ArrayList<Object>();
        imageTypeList.add("Public Images");
        imageTypeList.addAll(Arrays.asList(PublicImages.values()));
        imageTypeList.add("MSDN Images");
        imageTypeList.addAll(Arrays.asList(MSDNImages.values()));
        imageTypeList.add("Private Images");
        imageTypeList.addAll(Arrays.asList(PrivateImages.values()));

        imageTypeComboBox.setModel(new DefaultComboBoxModel(imageTypeList.toArray()) {
            @Override
            public void setSelectedItem(Object o) {
                if (o instanceof Enum) {
                    super.setSelectedItem(o);
                }
            }
        });

        imageTypeComboBox.setRenderer(new ListCellRendererWrapper<Object>() {
            @Override
            public void customize(JList jList, Object o, int i, boolean b, boolean b1) {
                if (o instanceof Enum) {
                    setText("  " + o.toString());
                } else {
                    //Gets the default label font
                    Font f = UIManager.getFont("Label.font");
                    setFont(f.deriveFont(f.getStyle()
                            | Font.BOLD | Font.ITALIC));
                }
            }
        });

        imageTypeComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                fillList();
            }
        });

        imageLabelList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList jList, Object o, int i, boolean b, boolean b1) {
                String cellValue = o.toString();

                if (o instanceof VirtualMachineImage) {
                    VirtualMachineImage virtualMachineImage = (VirtualMachineImage) o;

                    cellValue = String.format("%s (%s)",
                            virtualMachineImage.getLabel(),
                            new SimpleDateFormat("yyyy-MM-dd").format(virtualMachineImage.getPublishedDate().getTime()));
                }

                this.setToolTipText(cellValue);
                return super.getListCellRendererComponent(jList, cellValue, i, b, b1);
            }
        });

        imageLabelList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                VirtualMachineImage virtualMachineImage = (VirtualMachineImage) imageLabelList.getSelectedValue();
                model.setVirtualMachineImage(virtualMachineImage);

                if (virtualMachineImage != null) {
                    imageDescriptionTextPane.setText(model.getHtmlFromVMImage(virtualMachineImage));
                    imageDescriptionTextPane.setCaretPosition(0);
                    model.getCurrentNavigationState().NEXT.setEnabled(true);

                    model.setSize(null);
                }
            }
        });

        imageDescriptionTextPane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
                if (hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    if (Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().browse(hyperlinkEvent.getURL().toURI());
                        } catch (Exception e) {
                            DefaultLoader.getUIHelper().showException("An error occurred while attempting to open the " +
                                            "specified Link.",
                                    e, "Azure Services Explorer - Error Opening Link", false, true);
                        }
                    }
                }
            }
        });
    }

    @Override
    public JComponent prepare(WizardNavigationState wizardNavigationState) {
        rootPanel.revalidate();

        if (virtualMachineImages == null) {
            imageTypeComboBox.setEnabled(false);
            model.getCurrentNavigationState().NEXT.setEnabled(false);

            imageLabelList.setListData(new String[]{"loading..."});
            imageLabelList.setEnabled(false);

            ProgressManager.getInstance().run(new Task.Backgroundable(project, "Loading virtual machine images...", false) {
                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    progressIndicator.setIndeterminate(true);

                    try {
                        for (VirtualMachineImage virtualMachineImage : AzureManagerImpl.getManager().getVirtualMachineImages(model.getSubscription().getId())) {
                            if (virtualMachineImage.isShowInGui()) {
                                Enum type = null;

                                if (virtualMachineImage.getCategory().equals("Public")) {
                                    for (PublicImages publicImage : PublicImages.values()) {
                                        if (virtualMachineImage.getPublisherName().contains(publicImage.toString())) {
                                            type = publicImage;
                                        } else if (virtualMachineImage.getOperatingSystemType().equals(publicImage.toString())) {
                                            type = publicImage;
                                        }
                                    }

                                    if (type == null) {
                                        type = PublicImages.Other;
                                    }
                                } else if (virtualMachineImage.getCategory().equals("Private")
                                        || virtualMachineImage.getCategory().equals("User")) {
                                    type = PrivateImages.VMImages;
                                } else {
                                    for (MSDNImages msdnImages : MSDNImages.values()) {
                                        if (virtualMachineImage.getPublisherName().contains(msdnImages.toString())) {
                                            type = msdnImages;
                                        } else if (virtualMachineImage.getOperatingSystemType().equals(msdnImages.toString())) {
                                            type = msdnImages;
                                        }
                                    }

                                    if (type == null) {
                                        type = MSDNImages.Other;
                                    }
                                }

                                if (virtualMachineImages == null) {
                                    virtualMachineImages = new HashMap<Enum, List<VirtualMachineImage>>();
                                }

                                if (!virtualMachineImages.containsKey(type)) {
                                    virtualMachineImages.put(type, new ArrayList<VirtualMachineImage>());
                                }

                                virtualMachineImages.get(type).add(virtualMachineImage);
                            }
                        }

                        ApplicationManager.getApplication().invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                imageTypeComboBox.setEnabled(true);
                                imageLabelList.setEnabled(true);

                                imageTypeComboBox.setSelectedIndex(1);
                            }
                        });
                    } catch (AzureCmdException e) {
                        DefaultLoader.getUIHelper().showException("An error occurred while attempting to load the virtual " +
                                        "machine images list.", e,
                                "Azure Services Explorer - Error Loading Virtual Machine Images", false, true);
                    }
                }
            });
        }

        return rootPanel;
    }

    private void fillList() {
        model.getCurrentNavigationState().NEXT.setEnabled(false);

        Enum imageType = (Enum) imageTypeComboBox.getSelectedItem();

        List<VirtualMachineImage> machineImages = virtualMachineImages.get(imageType);
        imageLabelList.setListData(machineImages == null ? new Object[]{} : machineImages.toArray());

        if (machineImages != null && machineImages.size() > 0) {
            imageLabelList.setSelectedIndex(0);
        }
    }
}