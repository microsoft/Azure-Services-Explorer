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
package com.microsoft.tooling.msservices.helpers.azure.sdk;

import com.microsoft.tooling.msservices.helpers.CallableSingleArg;
import com.microsoft.tooling.msservices.helpers.NotNull;
import com.microsoft.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoft.tooling.msservices.model.storage.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface StorageClientSDKManager {
    @NotNull
    ClientStorageAccount getStorageAccount(@NotNull String connectionString);

    @NotNull
    List<BlobContainer> getBlobContainers(@NotNull ClientStorageAccount storageAccount) throws AzureCmdException;

    @NotNull
    BlobContainer createBlobContainer(@NotNull ClientStorageAccount storageAccount, @NotNull BlobContainer blobContainer)
            throws AzureCmdException;

    void deleteBlobContainer(@NotNull ClientStorageAccount storageAccount, @NotNull BlobContainer blobContainer)
            throws AzureCmdException;

    @NotNull
    BlobDirectory getRootDirectory(@NotNull ClientStorageAccount storageAccount, @NotNull BlobContainer blobContainer)
            throws AzureCmdException;

    @NotNull
    List<BlobItem> getBlobItems(@NotNull ClientStorageAccount storageAccount, @NotNull BlobDirectory blobDirectory)
            throws AzureCmdException;

    @NotNull
    BlobDirectory createBlobDirectory(@NotNull ClientStorageAccount storageAccount,
                                      @NotNull BlobDirectory parentBlobDirectory,
                                      @NotNull BlobDirectory blobDirectory)
            throws AzureCmdException;

    @NotNull
    BlobFile createBlobFile(@NotNull ClientStorageAccount storageAccount,
                            @NotNull BlobDirectory parentBlobDirectory,
                            @NotNull BlobFile blobFile)
            throws AzureCmdException;

    void deleteBlobFile(@NotNull ClientStorageAccount storageAccount,
                        @NotNull BlobFile blobFile)
            throws AzureCmdException;

    void uploadBlobFileContent(@NotNull ClientStorageAccount storageAccount,
                               @NotNull BlobContainer blobContainer,
                               @NotNull String filePath,
                               @NotNull InputStream content,
                               CallableSingleArg<Void, Long> processBlockEvent,
                               long maxBlockSize,
                               long length)
            throws AzureCmdException;

    void downloadBlobFileContent(@NotNull ClientStorageAccount storageAccount,
                                 @NotNull BlobFile blobFile,
                                 @NotNull OutputStream content)
            throws AzureCmdException;

    @NotNull
    List<Queue> getQueues(@NotNull ClientStorageAccount storageAccount)
            throws AzureCmdException;

    @NotNull
    Queue createQueue(@NotNull ClientStorageAccount storageAccount,
                      @NotNull Queue queue)
            throws AzureCmdException;

    void deleteQueue(@NotNull ClientStorageAccount storageAccount, @NotNull Queue queue)
            throws AzureCmdException;

    @NotNull
    List<QueueMessage> getQueueMessages(@NotNull ClientStorageAccount storageAccount, @NotNull Queue queue)
            throws AzureCmdException;

    void clearQueue(@NotNull ClientStorageAccount storageAccount, @NotNull Queue queue)
            throws AzureCmdException;

    void createQueueMessage(@NotNull ClientStorageAccount storageAccount,
                            @NotNull QueueMessage queueMessage,
                            int timeToLiveInSeconds)
            throws AzureCmdException;

    @NotNull
    QueueMessage dequeueFirstQueueMessage(@NotNull ClientStorageAccount storageAccount, @NotNull Queue queue)
            throws AzureCmdException;

    @NotNull
    List<Table> getTables(@NotNull ClientStorageAccount storageAccount)
            throws AzureCmdException;

    @NotNull
    Table createTable(@NotNull ClientStorageAccount storageAccount,
                      @NotNull Table table)
            throws AzureCmdException;

    void deleteTable(@NotNull ClientStorageAccount storageAccount, @NotNull Table table)
            throws AzureCmdException;

    @NotNull
    List<TableEntity> getTableEntities(@NotNull ClientStorageAccount storageAccount, @NotNull Table table,
                                       @NotNull String filter)
            throws AzureCmdException;

    @NotNull
    TableEntity createTableEntity(@NotNull ClientStorageAccount storageAccount, @NotNull String tableName,
                                  @NotNull String partitionKey, @NotNull String rowKey,
                                  @NotNull Map<String, TableEntity.Property> properties)
            throws AzureCmdException;

    @NotNull
    TableEntity updateTableEntity(@NotNull ClientStorageAccount storageAccount, @NotNull TableEntity tableEntity)
            throws AzureCmdException;

    void deleteTableEntity(@NotNull ClientStorageAccount storageAccount, @NotNull TableEntity tableEntity)
            throws AzureCmdException;
}