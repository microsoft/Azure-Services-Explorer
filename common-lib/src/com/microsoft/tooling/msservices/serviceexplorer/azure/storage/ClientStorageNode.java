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
package com.microsoft.tooling.msservices.serviceexplorer.azure.storage;

import com.microsoft.tooling.msservices.helpers.NotNull;
import com.microsoft.tooling.msservices.model.storage.ClientStorageAccount;
import com.microsoft.tooling.msservices.serviceexplorer.EventHelper.EventStateHandle;
import com.microsoft.tooling.msservices.serviceexplorer.Node;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.azure.AzureRefreshableNode;

public abstract class ClientStorageNode extends AzureRefreshableNode {
    protected final ClientStorageAccount clientStorageAccount;

    public ClientStorageNode(String id, String name, Node parent, String iconPath, ClientStorageAccount sm) {
        super(id, name, parent, iconPath);
        this.clientStorageAccount = sm;
    }

    public ClientStorageNode(String id, String name, Node parent, String iconPath, ClientStorageAccount sm, boolean delayActionLoading) {
        super(id, name, parent, iconPath, delayActionLoading);
        this.clientStorageAccount = sm;
    }

    @Override
    protected void onNodeClick(NodeActionEvent e) {
        this.load();
    }

    public ClientStorageAccount getClientStorageAccount() {
        return clientStorageAccount;
    }

    protected void fillChildren(@NotNull EventStateHandle eventState) {
        BlobModule blobsNode = new BlobModule(this, clientStorageAccount);
        blobsNode.load();

        if (eventState.isEventTriggered()) {
            return;
        }

        addChildNode(blobsNode);

        QueueModule queueNode = new QueueModule(this, clientStorageAccount);
        queueNode.load();

        if (eventState.isEventTriggered()) {
            return;
        }

        addChildNode(queueNode);

        TableModule tableNode = new TableModule(this, clientStorageAccount);
        tableNode.load();

        if (eventState.isEventTriggered()) {
            return;
        }

        addChildNode(tableNode);
    }
}