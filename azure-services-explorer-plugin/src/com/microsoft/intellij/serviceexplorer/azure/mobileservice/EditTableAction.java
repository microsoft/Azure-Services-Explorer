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
package com.microsoft.intellij.serviceexplorer.azure.mobileservice;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.microsoft.intellij.forms.TableForm;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.Name;
import com.microsoft.tooling.msservices.helpers.NotNull;
import com.microsoft.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoft.tooling.msservices.helpers.azure.AzureManagerImpl;
import com.microsoft.tooling.msservices.model.ms.MobileService;
import com.microsoft.tooling.msservices.model.ms.Table;
import com.microsoft.tooling.msservices.serviceexplorer.EventHelper.EventStateHandle;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.azure.AzureNodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.mobileservice.MobileServiceNode;
import com.microsoft.tooling.msservices.serviceexplorer.azure.mobileservice.TableNode;

@Name("Edit table")
public class EditTableAction extends AzureNodeActionListener {
    private TableNode tableNode;

    public EditTableAction(TableNode tableNode) {
        super(tableNode, "Retrieving Table Information");
        this.tableNode = tableNode;
    }

    @Override
    protected void azureNodeAction(NodeActionEvent e, @NotNull EventStateHandle stateHandle)
            throws AzureCmdException {
        try {
            // get the parent MobileServiceNode node
            final MobileService mobileService = tableNode.findParentByType(MobileServiceNode.class).getMobileService();

            final Table selectedTable = AzureManagerImpl.getManager().showTableDetails(
                    mobileService.getSubcriptionId(),
                    mobileService.getName(),
                    tableNode.getTable().getName());

            if (stateHandle.isEventTriggered()) {
                return;
            }

            ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                    TableForm form = new TableForm((Project) tableNode.getProject());
                    form.setServiceName(mobileService.getName());
                    form.setSubscriptionId(mobileService.getSubcriptionId());
                    form.setEditingTable(selectedTable);
                    form.show();
                }
            }, ModalityState.any());
        } catch (AzureCmdException e1) {
            DefaultLoader.getUIHelper().showException("An error occurred while attempting to edit table.", e1,
                    "Azure Services Explorer - Error Editing Table", false, true);
        }
    }

    @Override
    protected void onSubscriptionsChanged(NodeActionEvent e)
            throws AzureCmdException {
    }
}