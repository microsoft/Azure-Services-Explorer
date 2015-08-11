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
import com.microsoft.tooling.msservices.model.ms.CustomAPI;
import com.microsoft.tooling.msservices.model.ms.MobileService;
import com.microsoft.tooling.msservices.serviceexplorer.EventHelper;
import com.microsoft.tooling.msservices.serviceexplorer.Node;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.AzureNodeActionPromptListener;

import java.util.Map;

public class CustomAPINode extends ScriptNodeBase {
    public static final String ICON_PATH = "api.png";
    private final MobileService mobileService;
    protected CustomAPI customAPI;

    public CustomAPINode(Node parent, CustomAPI customAPI) {
        super(customAPI.getName(), customAPI.getName(), parent, ICON_PATH);
        this.customAPI = customAPI;

        MobileServiceNode mobileServiceNode = findParentByType(MobileServiceNode.class);
        mobileService = mobileServiceNode.getMobileService();

    }

    @Override
    protected void onNodeClick(NodeActionEvent event) {
        onNodeClickInternal(customAPI);
    }

    @Override
    protected void downloadScript(MobileService mobileService, String scriptName, String localFilePath)
            throws AzureCmdException {
        AzureManagerImpl.getManager().downloadAPIScript(
                mobileService.getSubcriptionId(),
                mobileService.getName(),
                scriptName,
                localFilePath);
    }

    @Override
    protected Map<String, Class<? extends NodeActionListener>> initActions() {
        addAction("Delete", new DeleteApiAction());
        return super.initActions();
    }

    public CustomAPI getCustomAPI() {
        return customAPI;
    }

    public void setCustomAPI(CustomAPI customAPI) {
        this.customAPI = customAPI;
    }

    public class DeleteApiAction extends AzureNodeActionPromptListener {
        public DeleteApiAction() {
            super(CustomAPINode.this,
                    "This operation will delete the selected custom API. Are you sure you want to continue?",
                    "Deleting API");
        }

        @Override
        protected void azureNodeAction(NodeActionEvent e, @NotNull EventHelper.EventStateHandle stateHandle)
                throws AzureCmdException {
            AzureManagerImpl.getManager().deleteCustomApi(mobileService.getSubcriptionId(), mobileService.getName(), customAPI.getName());
            DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
                @Override
                public void run() {
                    // instruct parent node to remove this node
                    getParent().removeDirectChildNode(CustomAPINode.this);
                }
            });
        }

        @Override
        protected void onSubscriptionsChanged(NodeActionEvent e) throws AzureCmdException {

        }
    }
}