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
package com.microsoft.tooling.msservices.helpers.auth;

import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.tooling.msservices.helpers.NotNull;
import com.microsoft.tooling.msservices.helpers.azure.AzureCmdException;

public interface AADManager {
    @NotNull
    UserInfo authenticate(@NotNull String resource, @NotNull String title)
            throws AzureCmdException;

    void authenticate(@NotNull UserInfo userInfo,
                      @NotNull String resource,
                      @NotNull String title)
            throws AzureCmdException;

    @NotNull
    <T> T request(@NotNull UserInfo userInfo,
                  @NotNull String resource,
                  @NotNull String title,
                  @NotNull RequestCallback<T> requestCallback)
            throws AzureCmdException;

    @NotNull
    <V> ListenableFuture<V> requestFuture(@NotNull UserInfo userInfo,
                                          @NotNull String resource,
                                          @NotNull String title,
                                          @NotNull RequestCallback<ListenableFuture<V>> requestCallback);
}