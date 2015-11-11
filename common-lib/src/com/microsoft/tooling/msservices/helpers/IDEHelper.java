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
package com.microsoft.tooling.msservices.helpers;

import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoft.tooling.msservices.helpers.tasks.CancellableTask;
import com.microsoft.tooling.msservices.helpers.tasks.CancellableTask.CancellableTaskHandle;
import com.microsoft.tooling.msservices.model.storage.*;
import com.microsoft.tooling.msservices.serviceexplorer.Node;
import org.apache.commons.lang3.tuple.Pair;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface IDEHelper {
    class ProjectDescriptor {
        @NotNull
        private final String name;
        @NotNull
        private final String path;

        public ProjectDescriptor(@NotNull String name, @NotNull String path) {
            this.name = name;
            this.path = path;
        }

        @NotNull
        public String getName() {
            return name;
        }

        @NotNull
        public String getPath() {
            return path;
        }
    }

    class ArtifactDescriptor {
        @NotNull
        private String name;
        @NotNull
        private String artifactType;

        public ArtifactDescriptor(@NotNull String name, @NotNull String artifactType) {
            this.name = name;
            this.artifactType = artifactType;
        }

        @NotNull
        public String getName() {
            return name;
        }

        @NotNull
        public String getArtifactType() {
            return artifactType;
        }
    }

    void openFile(@NotNull File file, @NotNull Node node);

    void saveFile(@NotNull File file, @NotNull ByteArrayOutputStream buff, @NotNull Node node);

    void replaceInFile(@NotNull Object module, @NotNull Pair<String, String>... replace);

    void copyJarFiles2Module(@NotNull Object moduleObject, @NotNull File zipFile, @NotNull String zipPath)
            throws IOException;

    boolean isFileEditing(@NotNull Object projectObject, @NotNull File file);

    <T extends StorageServiceTreeItem> void openItem(@NotNull Object projectObject,
                                                     @Nullable ClientStorageAccount storageAccount,
                                                     @NotNull T item,
                                                     @Nullable String itemType,
                                                     @NotNull String itemName,
                                                     @Nullable String iconName);

    void openItem(@NotNull Object projectObject, @NotNull Object itemVirtualFile);

    @Nullable
    <T extends StorageServiceTreeItem> Object getOpenedFile(@NotNull Object projectObject,
                                                            @NotNull ClientStorageAccount storageAccount,
                                                            @NotNull T item);

    void closeFile(@NotNull Object projectObject, @NotNull Object openedFile);

    void refreshQueue(@NotNull Object projectObject, @NotNull ClientStorageAccount storageAccount, @NotNull Queue queue);

    void refreshBlobs(@NotNull Object projectObject, @NotNull ClientStorageAccount storageAccount, @NotNull BlobContainer container);

    void refreshTable(@NotNull Object projectObject, @NotNull ClientStorageAccount storageAccount, @NotNull Table table);

    void invokeLater(@NotNull Runnable runnable);

    void invokeAndWait(@NotNull Runnable runnable);

    void executeOnPooledThread(@NotNull Runnable runnable);

    void runInBackground(@Nullable Object project, @NotNull String name, boolean canBeCancelled,
                         boolean isIndeterminate, @Nullable String indicatorText,
                         Runnable runnable);

    @NotNull
    CancellableTaskHandle runInBackground(@NotNull ProjectDescriptor projectDescriptor,
                                          @NotNull String name,
                                          @Nullable String indicatorText,
                                          @NotNull CancellableTask cancellableTask) throws AzureCmdException;

    @Nullable
    String getProperty(@NotNull Object projectObject, @NotNull String name);

    @NotNull
    String getProperty(@NotNull Object projectObject, @NotNull String name, @NotNull String defaultValue);

    void setProperty(@NotNull Object projectObject, @NotNull String name, @NotNull String value);

    void unsetProperty(@NotNull Object projectObject, @NotNull String name);

    boolean isPropertySet(@NotNull Object projectObject, @NotNull String name);

    @Nullable
    String getProperty(@NotNull String name);

    @NotNull
    String getProperty(@NotNull String name, @NotNull String defaultValue);

    void setProperty(@NotNull String name, @NotNull String value);

    void unsetProperty(@NotNull String name);

    boolean isPropertySet(@NotNull String name);

    @NotNull
    String promptForOpenSSLPath();

    @Nullable
    String[] getProperties(@NotNull String name);

    void setProperties(@NotNull String name, @NotNull String[] value);

    @NotNull
    List<ArtifactDescriptor> getArtifacts(@NotNull ProjectDescriptor projectDescriptor)
            throws AzureCmdException;

    @NotNull
    ListenableFuture<String> buildArtifact(@NotNull ProjectDescriptor projectDescriptor,
                                           @NotNull ArtifactDescriptor artifactDescriptor);
}