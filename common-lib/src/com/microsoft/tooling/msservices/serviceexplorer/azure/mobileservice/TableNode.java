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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.NotNull;
import com.microsoft.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoft.tooling.msservices.helpers.azure.AzureManagerImpl;
import com.microsoft.tooling.msservices.model.ms.Column;
import com.microsoft.tooling.msservices.model.ms.MobileService;
import com.microsoft.tooling.msservices.model.ms.Script;
import com.microsoft.tooling.msservices.model.ms.Table;
import com.microsoft.tooling.msservices.serviceexplorer.EventHelper.EventStateHandle;
import com.microsoft.tooling.msservices.serviceexplorer.Node;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.AzureNodeActionPromptListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.AzureRefreshableNode;


import java.util.List;
import java.util.Map;

public class TableNode extends AzureRefreshableNode {
    public static final String ICON_PATH = "table.png";
    public static final String SCRIPTS = "Scripts";
    public static final String COLUMNS = "Columns";
    private final MobileService mobileService;
    protected Table table;
    protected boolean childNodesLoaded = false;

    protected Node scriptsNode; // parent node for all script nodes
    protected Node columnsNode; // parent node for all column nodes

    public TableNode(Node parent, Table table) {
        super(table.getName(), table.getName(), parent, ICON_PATH);
        this.table = table;

        MobileServiceNode mobileServiceNode = findParentByType(MobileServiceNode.class);
        mobileService = mobileServiceNode.getMobileService();

    }

    @Override
    protected void refresh(@NotNull EventStateHandle eventState)
            throws AzureCmdException {
        // get the parent MobileServiceNode node

        // fetch table details
        Table tableInfo = AzureManagerImpl.getManager().showTableDetails(
                mobileService.getSubcriptionId(),
                mobileService.getName(),
                table.getName());

        if (eventState.isEventTriggered()) {
            return;
        }

        // load scripts and columns nodes
        scriptsNode = loadScriptNode(tableInfo);
        columnsNode = loadColumnNode(tableInfo);
    }

    protected Node loadScriptNode(Table tableInfo) {
        // create and add a new parent node for this item; we add the "node"
        // variable as a child *before* adding the element nodes so that the
        // service explorer tool window is automatically notified when they are
        // added; if we called "addChildNode" after the children of "node"
        // have been added then the service explorer tool window will not be
        // notified of those new nodes
        if (scriptsNode == null) {
            scriptsNode = new Node(table.getName() + "_script", SCRIPTS, this, null);
            addChildNode(scriptsNode);
        } else {
            scriptsNode.removeAllChildNodes();
        }

        for (String operation : Script.getOperationList()) {
            Script s = new Script();
            s.setOperation(operation);
            s.setBytes(0);
            s.setName(String.format("%s.%s", tableInfo.getName(), operation));

            for (Script script : tableInfo.getScripts()) {
                if (script.getOperation().equals(operation)) {
                    s = script;
                }
            }

            scriptsNode.addChildNode(new TableScriptNode(scriptsNode, s));
        }

        return scriptsNode;
    }

    protected Node loadColumnNode(Table tableInfo) {
        // create and add a new parent node for this item; we add the "node"
        // variable as a child *before* adding the element nodes so that the
        // service explorer tool window is automatically notified when they are
        // added; if we called "addChildNode" after the children of "node"
        // have been added then the service explorer tool window will not be
        // notified of those new nodes
        if (columnsNode == null) {
            columnsNode = new Node(table.getName() + "_column", COLUMNS, this, null);
            addChildNode(columnsNode);
        } else {
            columnsNode.removeAllChildNodes();
        }

        for (Column col : tableInfo.getColumns()) {
            if (!col.getName().startsWith("__")) {
                columnsNode.addChildNode(new TableColumnNode(columnsNode, col));
            }
        }

        return columnsNode;
    }

    @Override
    protected void onNodeClick(NodeActionEvent event) {
        // we attempt loading the services only if we haven't already
        // loaded them
        if (!childNodesLoaded) {
            Futures.addCallback(load(), new FutureCallback<List<Node>>() {
                @Override
                public void onSuccess(List<Node> nodes) {
                    childNodesLoaded = true;
                }

                @Override
                public void onFailure(Throwable throwable) {
                }
            });
        }
    }

    public Table getTable() {
        return table;
    }

    @Override
    protected Map<String, Class<? extends NodeActionListener>> initActions() {
        addAction("Delete", new DeleteTableAction());
        return super.initActions();
    }

    public class DeleteTableAction extends AzureNodeActionPromptListener {
        public DeleteTableAction() {
            super(TableNode.this,
                    "This operation will delete the selected table. Are you sure you want to continue?",
                    "Deleting Table");
        }

        @Override
        protected void azureNodeAction(NodeActionEvent e, @NotNull EventStateHandle stateHandle)
                throws AzureCmdException {
            AzureManagerImpl.getManager().deleteTable(mobileService.getSubcriptionId(), mobileService.getName(), table.getName());
            DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
                @Override
                public void run() {
                    // instruct parent node to remove this node
                    getParent().removeDirectChildNode(TableNode.this);
                }
            });
        }

        @Override
        protected void onSubscriptionsChanged(NodeActionEvent e) throws AzureCmdException {

        }
    }
}