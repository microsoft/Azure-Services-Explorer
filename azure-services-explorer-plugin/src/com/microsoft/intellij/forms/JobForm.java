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
import com.microsoft.tooling.msservices.helpers.azure.AzureManagerImpl;
import com.microsoft.tooling.msservices.model.ms.Job;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.Integer.parseInt;

public class JobForm extends DialogWrapper {
    private final Project project;
    private JPanel mainPanel;
    private JTextField jobNameTextField;
    private JRadioButton scheduledRadioButton;
    private JComboBox intervalUnitComboBox;
    private JTextField intervalTextField;
    private JRadioButton onDemandRadioButton;
    private JCheckBox enabledCheckBox;
    private JLabel onDemandLabel;
    private JLabel everyLabel;
    private UUID id;
    private String serviceName;
    private String subscriptionId;

    public void setAfterSave(Runnable afterSave) {
        this.afterSave = afterSave;
    }

    private Runnable afterSave;

    public JobForm(Project project) {
        super(project, true);
        this.project = project;

        this.setModal(true);

        setOKActionEnabled(false);

        ButtonGroup group = new ButtonGroup();
        group.add(onDemandRadioButton);
        group.add(scheduledRadioButton);

        jobNameTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                super.keyTyped(keyEvent);

                setOKActionEnabled(!jobNameTextField.getText().isEmpty());
            }
        });

        ActionListener radioActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                setEnabledOptions();
            }
        };

        scheduledRadioButton.addActionListener(radioActionListener);
        onDemandRadioButton.addActionListener(radioActionListener);

        intervalUnitComboBox.setModel(new DefaultComboBoxModel(Job.getUnits()));

        scheduledRadioButton.setSelected(true);
        intervalTextField.setText("15");
        setEnabledOptions();

        intervalTextField.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent jComponent) {
                if (jComponent instanceof JTextField) {
                    JTextField field = (JTextField) jComponent;

                    try {

                    } catch (NumberFormatException e) {
                        return false;
                    }

                    return true;
                }

                return false;
            }
        });


        init();
    }


    public void setJob(Job job) {
        jobNameTextField.setEnabled(false);
        enabledCheckBox.setEnabled(true);
        setOKActionEnabled(true);

        id = job.getId();
        jobNameTextField.setText(job.getName());
        enabledCheckBox.setSelected(job.isEnabled());
        if (job.getIntervalUnit() == null) {
            onDemandRadioButton.setSelected(true);
        } else {
            scheduledRadioButton.setSelected(true);
            intervalTextField.setText(String.valueOf(job.getIntervalPeriod()));

            int index = 0;
            String[] units = Job.getUnits();
            for (int i = 0; i < units.length; i++)
                if (job.getIntervalUnit().equals(units[i]))
                    index = i;

            intervalUnitComboBox.setSelectedIndex(index);
        }

    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    private void setEnabledOptions() {
        intervalTextField.setEnabled(scheduledRadioButton.isSelected());
        intervalUnitComboBox.setEnabled(scheduledRadioButton.isSelected());
        everyLabel.setEnabled(scheduledRadioButton.isSelected());
        onDemandLabel.setEnabled(onDemandRadioButton.isSelected());
    }

    public Job getEditingJob() {
        Job job = new Job();

        job.setId(id);
        job.setName(jobNameTextField.getText());
        job.setEnabled(enabledCheckBox.isSelected());
        if (scheduledRadioButton.isSelected()) {
            job.setIntervalUnit(Job.getUnits()[intervalUnitComboBox.getSelectedIndex()]);
            job.setIntervalPeriod(parseInt(intervalTextField.getText()));
        }

        return job;
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {

        String jobName = jobNameTextField.getText().trim();

        if (!jobName.matches("^[A-Za-z][A-Za-z0-9_]+")) {
            return new ValidationInfo("Invalid service name. Job name must start with a letter, \n" +
                    "contain only letters, numbers, and undercores.", jobNameTextField);

        }

        if (!intervalTextField.getText().matches("[0-9]+")) {
            return new ValidationInfo("Interval must be a numberic value", intervalTextField);
        }

        return null;

    }

    @Override
    protected void doOKAction() {
        final String jobName = jobNameTextField.getText().trim();
        final int interval = onDemandRadioButton.isSelected() ? 0 : parseInt(intervalTextField.getText());
        final String unit = onDemandRadioButton.isSelected() ? "none" : Job.getUnits()[intervalUnitComboBox.getSelectedIndex()];
        final SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);

        DefaultLoader.getIdeHelper().runInBackground(project, "Saving job", false, true, "Saving job...", new Runnable() {
            @Override
            public void run() {
                try {
                    String now = ISO8601DATEFORMAT.format(new Date());

                    if (id == null) {
                        List<String> existingJobNames = new ArrayList<String>();

                        for (Job job : AzureManagerImpl.getManager().listJobs(subscriptionId, serviceName)) {
                            existingJobNames.add(job.getName().toLowerCase());
                        }


                        if (existingJobNames.contains(jobName.toLowerCase())) {
                            JOptionPane.showMessageDialog(mainPanel, "Invalid job name. A job with that name already exists in this service.",
                                    "Service Explorer", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }


                    if (id == null)
                        AzureManagerImpl.getManager().createJob(subscriptionId, serviceName, jobName, interval, unit, now);
                    else {
                        AzureManagerImpl.getManager().updateJob(subscriptionId, serviceName, jobName, interval, unit, now, enabledCheckBox.isSelected());
                    }

                    DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (afterSave != null) {
                                afterSave.run();
                            }
                        }
                    });


                } catch (Throwable ex) {
                    DefaultLoader.getUIHelper().showException("An error occurred while trying to save job", ex,
                            "Azure Services Explorer - Error Saving Job", false, true);
                }
            }
        });


        close(DialogWrapper.OK_EXIT_CODE, true);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return mainPanel;
    }

}
