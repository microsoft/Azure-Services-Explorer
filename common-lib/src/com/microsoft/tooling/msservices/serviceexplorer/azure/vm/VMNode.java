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
package com.microsoft.tooling.msservices.serviceexplorer.azure.vm;

import com.google.common.collect.ImmutableMap;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.NotNull;
import com.microsoft.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoft.tooling.msservices.helpers.azure.AzureManagerImpl;
import com.microsoft.tooling.msservices.model.vm.Endpoint;
import com.microsoft.tooling.msservices.model.vm.VirtualMachine;
import com.microsoft.tooling.msservices.serviceexplorer.EventHelper.EventStateHandle;
import com.microsoft.tooling.msservices.serviceexplorer.Node;
import com.microsoft.tooling.msservices.serviceexplorer.NodeAction;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.AzureNodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.AzureNodeActionPromptListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.AzureRefreshableNode;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class VMNode extends AzureRefreshableNode {
    private static abstract class VMNodeActionPromptListener extends AzureNodeActionPromptListener {
        private VMNode vmNode;

        public VMNodeActionPromptListener(@NotNull VMNode vmNode,
                                          @NotNull String promptMessageFormat,
                                          @NotNull String progressMessage) {
            super(vmNode, String.format(promptMessageFormat, vmNode.virtualMachine.getName()), progressMessage);
            this.vmNode = vmNode;
        }

        @Override
        protected void onSubscriptionsChanged(NodeActionEvent e)
                throws AzureCmdException {
        }
    }

    public class DeleteVMAction extends VMNodeActionPromptListener {
        public DeleteVMAction() {
            super(VMNode.this,
                    "This operation will delete virtual machine %s. The associated disks will not be deleted " +
                            "from your storage account. Are you sure you want to continue?",
                    "Deleting VM");
        }

        @Override
        protected void azureNodeAction(NodeActionEvent e, @NotNull EventStateHandle stateHandle)
                throws AzureCmdException {
            AzureManagerImpl.getManager().deleteVirtualMachine(virtualMachine, false);
            DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
                @Override
                public void run() {
                    // instruct parent node to remove this node
                    getParent().removeDirectChildNode(VMNode.this);
                }
            });
        }
    }

    public class DownloadRDPAction extends AzureNodeActionListener {
        private JFileChooser saveFile;

        public DownloadRDPAction() {
            super(VMNode.this, "Downloading RDP File");
        }

        @NotNull
        @Override
        protected Callable<Boolean> beforeAsyncActionPerfomed() {
            return new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    saveFile = new JFileChooser();
                    saveFile.setDialogTitle("Save RDP file");

                    return (saveFile.showSaveDialog(null) == JFileChooser.APPROVE_OPTION);
                }
            };
        }

        @Override
        protected void azureNodeAction(NodeActionEvent e, @NotNull EventStateHandle stateHandle)
                throws AzureCmdException {
            try {
                File rdpFile = saveFile.getSelectedFile();

                if (rdpFile.exists() || rdpFile.createNewFile()) {
                    FileOutputStream fileOutputStream = new FileOutputStream(rdpFile);
                    fileOutputStream.write(AzureManagerImpl.getManager().downloadRDP(virtualMachine));
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (Exception ex) {
                DefaultLoader.getUIHelper().showException("An error occurred while attempting to download RDP file.", ex,
                        "MS Services - Error Downloading RDP File", false, true);
            }
        }

        @Override
        protected void onSubscriptionsChanged(NodeActionEvent e)
                throws AzureCmdException {
        }
    }

    public class ShutdownVMAction extends VMNodeActionPromptListener {
        public ShutdownVMAction() {
            super(VMNode.this,
                    "This operation will result in losing the VIP that was assigned to this virtual machine. " +
                            "Are you sure that you want to shut down virtual machine %s?",
                    "Shutting down VM");
        }

        @Override
        protected void azureNodeAction(NodeActionEvent e, @NotNull EventStateHandle stateHandle)
                throws AzureCmdException {
            AzureManagerImpl.getManager().shutdownVirtualMachine(virtualMachine, true);
        }
    }

    public class StartVMAction extends VMNodeActionPromptListener {
        public StartVMAction() {
            super(VMNode.this,
                    "Are you sure you want to start the virtual machine %s?",
                    "Starting VM");
        }

        @Override
        protected void azureNodeAction(NodeActionEvent e, @NotNull EventStateHandle stateHandle)
                throws AzureCmdException {
            AzureManagerImpl.getManager().startVirtualMachine(virtualMachine);
        }
    }

    public class RestartVMAction extends VMNodeActionPromptListener {
        public RestartVMAction() {
            super(VMNode.this,
                    "Are you sure you want to restart the virtual machine %s?",
                    "Restarting VM");
        }

        @Override
        protected void azureNodeAction(NodeActionEvent e, @NotNull EventStateHandle stateHandle)
                throws AzureCmdException {
            AzureManagerImpl.getManager().restartVirtualMachine(virtualMachine);
        }
    }

    private static final String WAIT_ICON_PATH = "virtualmachinewait.png";
    private static final String STOP_ICON_PATH = "virtualmachinestop.png";
    private static final String RUN_ICON_PATH = "virtualmachinerun.png";
    public static final String ACTION_DELETE = "Delete";
    public static final String ACTION_DOWNLOAD_RDP_FILE = "Connect Remote Desktop";
    public static final String ACTION_SHUTDOWN = "Shutdown";
    public static final String ACTION_START = "Start";
    public static final String ACTION_RESTART = "Restart";
    public static final int REMOTE_DESKTOP_PORT = 3389;

    protected VirtualMachine virtualMachine;

    public VMNode(Node parent, VirtualMachine virtualMachine)
            throws AzureCmdException {
        super(virtualMachine.getName(), virtualMachine.getName(), parent, WAIT_ICON_PATH, true);
        this.virtualMachine = virtualMachine;

        loadActions();

        // update vm icon based on vm status
        refreshItemsInternal();
    }

    private String getVMIconPath() {
        switch (virtualMachine.getStatus()) {
            case Ready:
                return RUN_ICON_PATH;
            case Stopped:
            case StoppedDeallocated:
                return STOP_ICON_PATH;
            default:
                return WAIT_ICON_PATH;
        }
    }

    @Override
    protected void refresh(@NotNull EventStateHandle eventState)
            throws AzureCmdException {
        virtualMachine = AzureManagerImpl.getManager().refreshVirtualMachineInformation(virtualMachine);

        if (eventState.isEventTriggered()) {
            return;
        }

        refreshItemsInternal();
    }

    private void refreshItemsInternal() {
        // update vm name and status icon
        setName(virtualMachine.getName());
        setIconPath(getVMIconPath());

        // load up the endpoint nodes
        removeAllChildNodes();

        for (Endpoint endpoint : virtualMachine.getEndpoints()) {
            VMEndpointNode vmEndPoint = new VMEndpointNode(this, endpoint);
            addChildNode(vmEndPoint);
        }
    }

    @Override
    protected Map<String, Class<? extends NodeActionListener>> initActions() {
        Map<String, Class<? extends NodeActionListener>> actionMap =
                new HashMap<String, Class<? extends NodeActionListener>>();

        actionMap.put(ACTION_DELETE, DeleteVMAction.class);
        actionMap.put(ACTION_DOWNLOAD_RDP_FILE, DownloadRDPAction.class);
        actionMap.put(ACTION_SHUTDOWN, ShutdownVMAction.class);
        actionMap.put(ACTION_START, StartVMAction.class);
        actionMap.put(ACTION_RESTART, RestartVMAction.class);

        return ImmutableMap.copyOf(actionMap);
    }

    @Override
    public List<NodeAction> getNodeActions() {
        // enable/disable menu items according to VM status
        boolean started = virtualMachine.getStatus().equals(VirtualMachine.Status.Ready);
        boolean stopped = virtualMachine.getStatus().equals(VirtualMachine.Status.Stopped) ||
                virtualMachine.getStatus().equals(VirtualMachine.Status.StoppedDeallocated);

        getNodeActionByName(ACTION_DOWNLOAD_RDP_FILE).setEnabled(!stopped && hasRDPPort(virtualMachine));
        getNodeActionByName(ACTION_SHUTDOWN).setEnabled(started);
        getNodeActionByName(ACTION_START).setEnabled(stopped);
        getNodeActionByName(ACTION_RESTART).setEnabled(started);

        return super.getNodeActions();
    }

    private boolean hasRDPPort(VirtualMachine virtualMachine) {
        for (Endpoint endpoint : virtualMachine.getEndpoints()) {
            if (endpoint.getPrivatePort() == REMOTE_DESKTOP_PORT) {
                return true;
            }
        }

        return false;
    }
}