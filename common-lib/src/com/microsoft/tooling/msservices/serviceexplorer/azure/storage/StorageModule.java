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

import com.microsoft.tooling.msservices.helpers.ExternalStorageHelper;
import com.microsoft.tooling.msservices.helpers.NotNull;
import com.microsoft.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoft.tooling.msservices.helpers.azure.AzureManagerImpl;
import com.microsoft.tooling.msservices.helpers.azure.sdk.StorageClientSDKManagerImpl;
import com.microsoft.tooling.msservices.model.Subscription;
import com.microsoft.tooling.msservices.model.storage.ClientStorageAccount;
import com.microsoft.tooling.msservices.model.storage.StorageAccount;
import com.microsoft.tooling.msservices.serviceexplorer.EventHelper.EventStateHandle;
import com.microsoft.tooling.msservices.serviceexplorer.Node;
import com.microsoft.tooling.msservices.serviceexplorer.azure.AzureRefreshableNode;
import com.microsoft.windowsazure.management.storage.models.StorageAccountTypes;

import java.util.List;

public class StorageModule extends AzureRefreshableNode {
    private static final String STORAGE_MODULE_ID = StorageModule.class.getName();
    private static final String ICON_PATH = "storage.png";
    private static final String BASE_MODULE_NAME = "Storage";

    public StorageModule(Node parent) {
        super(STORAGE_MODULE_ID, BASE_MODULE_NAME, parent, ICON_PATH);
    }

    @Override
    protected void refresh(@NotNull EventStateHandle eventState)
            throws AzureCmdException {
        removeAllChildNodes();

        // load all Storage Accounts
        List<Subscription> subscriptionList = AzureManagerImpl.getManager().getSubscriptionList();

        for (Subscription subscription : subscriptionList) {
            List<StorageAccount> storageAccounts = AzureManagerImpl.getManager().getStorageAccounts(subscription.getId());

            if (eventState.isEventTriggered()) {
                return;
            }

            for (StorageAccount sm : storageAccounts) {
                String type = sm.getType();

                if (type.equals(StorageAccountTypes.STANDARD_GRS)
                        || type.equals(StorageAccountTypes.STANDARD_LRS)
                        || type.equals(StorageAccountTypes.STANDARD_RAGRS)
                        || type.equals(StorageAccountTypes.STANDARD_ZRS)) {

                    addChildNode(new StorageNode(this, sm));
                }
            }
        }

        // load External Accounts
        for (ClientStorageAccount clientStorageAccount : ExternalStorageHelper.getList()) {
            ClientStorageAccount storageAccount = StorageClientSDKManagerImpl.getManager().getStorageAccount(clientStorageAccount.getConnectionString());

            if (eventState.isEventTriggered()) {
                return;
            }

            addChildNode(new ExternalStorageNode(this, storageAccount));
        }
    }
}