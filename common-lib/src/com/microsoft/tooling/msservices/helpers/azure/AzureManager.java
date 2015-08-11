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
package com.microsoft.tooling.msservices.helpers.azure;

import com.microsoft.tooling.msservices.helpers.NotNull;
import com.microsoft.tooling.msservices.helpers.Nullable;
import com.microsoft.tooling.msservices.helpers.auth.UserInfo;
import com.microsoft.tooling.msservices.model.Subscription;
import com.microsoft.tooling.msservices.model.ms.*;
import com.microsoft.tooling.msservices.model.storage.StorageAccount;
import com.microsoft.tooling.msservices.model.vm.*;
import com.microsoft.tooling.msservices.serviceexplorer.EventHelper.EventWaitHandle;

import java.util.List;

public interface AzureManager {
    void authenticate() throws AzureCmdException;

    boolean authenticated();

    boolean authenticated(@NotNull String subscriptionId);

    @Nullable
    UserInfo getUserInfo();

    void clearAuthentication();

    void importPublishSettingsFile(@NotNull String publishSettingsFilePath)
            throws AzureCmdException;

    boolean usingCertificate();

    boolean usingCertificate(@NotNull String subscriptionId);

    void clearImportedPublishSettingsFiles();

    @NotNull
    List<Subscription> getFullSubscriptionList()
            throws AzureCmdException;

    @NotNull
    List<Subscription> getSubscriptionList()
            throws AzureCmdException;

    void setSelectedSubscriptions(@NotNull List<String> selectedList)
            throws AzureCmdException;

    @NotNull
    EventWaitHandle registerSubscriptionsChanged()
            throws AzureCmdException;

    void unregisterSubscriptionsChanged(@NotNull EventWaitHandle handle)
            throws AzureCmdException;

    @NotNull
    List<SqlDb> getSqlDb(@NotNull String subscriptionId, @NotNull SqlServer server)
            throws AzureCmdException;

    @NotNull
    List<SqlServer> getSqlServers(@NotNull String subscriptionId)
            throws AzureCmdException;

    @NotNull
    List<MobileService> getMobileServiceList(@NotNull String subscriptionId)
            throws AzureCmdException;

    void createMobileService(@NotNull String subscriptionId,
                             @NotNull String region,
                             @NotNull String username,
                             @NotNull String password,
                             @NotNull String mobileServiceName,
                             @Nullable String server,
                             @Nullable String database)
            throws AzureCmdException;

    void deleteMobileService(@NotNull String subscriptionId, @NotNull String mobileServiceName);

    @NotNull
    List<Table> getTableList(@NotNull String subscriptionId, @NotNull String mobileServiceName)
            throws AzureCmdException;

    void createTable(@NotNull String subscriptionId, @NotNull String mobileServiceName,
                     @NotNull String tableName, @NotNull TablePermissions permissions)
            throws AzureCmdException;

    void updateTable(@NotNull String subscriptionId, @NotNull String mobileServiceName,
                     @NotNull String tableName, @NotNull TablePermissions permissions)
            throws AzureCmdException;

    @NotNull
    Table showTableDetails(@NotNull String subscriptionId, @NotNull String mobileServiceName,
                           @NotNull String tableName)
            throws AzureCmdException;

    void downloadTableScript(@NotNull String subscriptionId, @NotNull String mobileServiceName,
                             @NotNull String scriptName, @NotNull String downloadPath)
            throws AzureCmdException;

    void uploadTableScript(@NotNull String subscriptionId, @NotNull String mobileServiceName,
                           @NotNull String scriptName, @NotNull String filePath)
            throws AzureCmdException;

    @NotNull
    List<CustomAPI> getAPIList(@NotNull String subscriptionId, @NotNull String mobileServiceName)
            throws AzureCmdException;

    void downloadAPIScript(@NotNull String subscriptionId, @NotNull String mobileServiceName, @NotNull String scriptName,
                           @NotNull String downloadPath)
            throws AzureCmdException;

    void uploadAPIScript(@NotNull String subscriptionId, @NotNull String mobileServiceName, @NotNull String scriptName,
                         @NotNull String filePath)
            throws AzureCmdException;

    void createCustomAPI(@NotNull String subscriptionId, @NotNull String mobileServiceName, @NotNull String tableName,
                         @NotNull CustomAPIPermissions permissions)
            throws AzureCmdException;

    void updateCustomAPI(@NotNull String subscriptionId, @NotNull String mobileServiceName, @NotNull String tableName,
                         @NotNull CustomAPIPermissions permissions)
            throws AzureCmdException;

    void deleteTable(@NotNull String subscriptionId, @NotNull String mobileServiceName,
                     @NotNull String tableName) throws AzureCmdException;

    void deleteCustomApi(@NotNull String subscriptionId, @NotNull String mobileServiceName,
                         @NotNull String apiName) throws AzureCmdException;

    void deleteJob(@NotNull String subscriptionId, @NotNull String mobileServiceName,
                   @NotNull String jobName) throws AzureCmdException;

    @NotNull
    List<Job> listJobs(@NotNull String subscriptionId, @NotNull String mobileServiceName)
            throws AzureCmdException;

    void createJob(@NotNull String subscriptionId, @NotNull String mobileServiceName, @NotNull String jobName,
                   int interval, @NotNull String intervalUnit, @NotNull String startDate)
            throws AzureCmdException;

    void updateJob(@NotNull String subscriptionId, @NotNull String mobileServiceName, @NotNull String jobName,
                   int interval, @NotNull String intervalUnit, @NotNull String startDate, boolean enabled)
            throws AzureCmdException;

    void downloadJobScript(@NotNull String subscriptionId, @NotNull String mobileServiceName, @NotNull String scriptName,
                           @NotNull String downloadPath)
            throws AzureCmdException;

    void uploadJobScript(@NotNull String subscriptionId, @NotNull String mobileServiceName, @NotNull String scriptName,
                         @NotNull String filePath)
            throws AzureCmdException;

    @NotNull
    List<LogEntry> listLog(@NotNull String subscriptionId, @NotNull String mobileServiceName, @NotNull String runtime)
            throws AzureCmdException;

    @NotNull
    List<CloudService> getCloudServices(@NotNull String subscriptionId)
            throws AzureCmdException;

    @NotNull
    List<VirtualMachine> getVirtualMachines(@NotNull String subscriptionId)
            throws AzureCmdException;

    @NotNull
    VirtualMachine refreshVirtualMachineInformation(@NotNull VirtualMachine vm)
            throws AzureCmdException;

    void startVirtualMachine(@NotNull VirtualMachine vm)
            throws AzureCmdException;

    void shutdownVirtualMachine(@NotNull VirtualMachine vm, boolean deallocate) throws AzureCmdException;

    void restartVirtualMachine(@NotNull VirtualMachine vm) throws AzureCmdException;

    void deleteVirtualMachine(@NotNull VirtualMachine vm, boolean deleteFromStorage) throws AzureCmdException;

    @NotNull
    byte[] downloadRDP(@NotNull VirtualMachine vm) throws AzureCmdException;

    @NotNull
    List<StorageAccount> getStorageAccounts(@NotNull String subscriptionId) throws AzureCmdException;

    @NotNull
    List<VirtualMachineImage> getVirtualMachineImages(@NotNull String subscriptionId) throws AzureCmdException;

    @NotNull
    List<VirtualMachineSize> getVirtualMachineSizes(@NotNull String subscriptionId) throws AzureCmdException;

    @NotNull
    List<Location> getLocations(@NotNull String subscriptionId)
            throws AzureCmdException;

    @NotNull
    List<AffinityGroup> getAffinityGroups(@NotNull String subscriptionId) throws AzureCmdException;

    @NotNull
    List<VirtualNetwork> getVirtualNetworks(@NotNull String subscriptionId) throws AzureCmdException;

    void createStorageAccount(@NotNull StorageAccount storageAccount)
            throws AzureCmdException;

    void createCloudService(@NotNull CloudService cloudService)
            throws AzureCmdException;

    void createVirtualMachine(@NotNull VirtualMachine virtualMachine, @NotNull VirtualMachineImage vmImage,
                              @NotNull StorageAccount storageAccount, @NotNull String virtualNetwork,
                              @NotNull String username, @NotNull String password, @NotNull byte[] certificate)
            throws AzureCmdException;

    void createVirtualMachine(@NotNull VirtualMachine virtualMachine, @NotNull VirtualMachineImage vmImage,
                              @NotNull String mediaLocation, @NotNull String virtualNetwork,
                              @NotNull String username, @NotNull String password, @NotNull byte[] certificate)
            throws AzureCmdException;

    @NotNull
    StorageAccount refreshStorageAccountInformation(@NotNull StorageAccount storageAccount)
            throws AzureCmdException;

    String createServiceCertificate(@NotNull String subscriptionId, @NotNull String serviceName,
                                    @NotNull byte[] data, @NotNull String password)
            throws AzureCmdException;

    void deleteStorageAccount(@NotNull StorageAccount storageAccount)
            throws AzureCmdException;
}