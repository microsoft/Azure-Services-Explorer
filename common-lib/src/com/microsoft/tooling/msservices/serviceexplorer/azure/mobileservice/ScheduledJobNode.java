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
package com.microsoft.tooling.msservices.serviceexplorer.azure.mobileservice;

import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.NotNull;
import com.microsoft.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoft.tooling.msservices.helpers.azure.AzureManagerImpl;
import com.microsoft.tooling.msservices.model.ms.Job;
import com.microsoft.tooling.msservices.model.ms.MobileService;
import com.microsoft.tooling.msservices.serviceexplorer.EventHelper;
import com.microsoft.tooling.msservices.serviceexplorer.Node;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.AzureNodeActionPromptListener;

import java.util.Map;

public class ScheduledJobNode extends ScriptNodeBase {
    public static final String ICON_PATH = "job.png";
    private final MobileService mobileService;
    protected Job job;

    public ScheduledJobNode(Node parent, Job job) {
        super(job.getName(), job.getName(), parent, ICON_PATH);
        this.job = job;


        MobileServiceNode mobileServiceNode = findParentByType(MobileServiceNode.class);
        mobileService = mobileServiceNode.getMobileService();
    }

    @Override
    protected void onNodeClick(NodeActionEvent event) {
        onNodeClickInternal(job);
    }

    @Override
    protected void downloadScript(MobileService mobileService, String scriptName, String localFilePath) throws AzureCmdException {
        AzureManagerImpl.getManager().downloadJobScript(
                mobileService.getSubcriptionId(),
                mobileService.getName(),
                scriptName,
                localFilePath);
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    @Override
    protected Map<String, Class<? extends NodeActionListener>> initActions() {
        addAction("Delete", new DeleteJobAction());
        return super.initActions();
    }

    public class DeleteJobAction extends AzureNodeActionPromptListener {
        public DeleteJobAction() {
            super(ScheduledJobNode.this,
                    "This operation will delete the selected job. Are you sure you want to continue?",
                    "Deleting job");
        }

        @Override
        protected void azureNodeAction(NodeActionEvent e, @NotNull EventHelper.EventStateHandle stateHandle)
                throws AzureCmdException {
            AzureManagerImpl.getManager().deleteJob(mobileService.getSubcriptionId(), mobileService.getName(), job.getName());
            DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
                @Override
                public void run() {
                    // instruct parent node to remove this node
                    getParent().removeDirectChildNode(ScheduledJobNode.this);
                }
            });
        }

        @Override
        protected void onSubscriptionsChanged(NodeActionEvent e) throws AzureCmdException {

        }
    }
}
