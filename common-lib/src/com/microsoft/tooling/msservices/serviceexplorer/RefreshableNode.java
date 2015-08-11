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
package com.microsoft.tooling.msservices.serviceexplorer;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.azure.AzureCmdException;

import java.util.List;

public abstract class RefreshableNode extends Node {
    public RefreshableNode(String id, String name, Node parent, String iconPath) {
        super(id, name, parent, iconPath);
    }

    public RefreshableNode(String id, String name, Node parent, String iconPath, boolean delayActionLoading) {
        super(id, name, parent, iconPath, delayActionLoading);
    }

    @Override
    protected void loadActions() {
        addAction("Refresh", new NodeActionListener() {
            @Override
            public void actionPerformed(NodeActionEvent e) {
                load();
            }
        });

        super.loadActions();
    }

    // Sub-classes are expected to override this method if they wish to
    // refresh items synchronously. The default implementation does nothing.
    protected abstract void refreshItems() throws AzureCmdException;

    // Sub-classes are expected to override this method if they wish
    // to refresh items asynchronously. The default implementation simply
    // delegates to "refreshItems" *synchronously* and completes the Future
    // with the result of calling getChildNodes.
    protected void refreshItems(SettableFuture<List<Node>> future) {
        setLoading(true);
        try {
            refreshItems();
            future.set(getChildNodes());
        } catch (AzureCmdException e) {
            future.setException(e);
        } finally {
            setLoading(false);
        }
    }

    public ListenableFuture<List<Node>> load() {
        final RefreshableNode node = this;
        final SettableFuture<List<Node>> future = SettableFuture.create();

        DefaultLoader.getIdeHelper().runInBackground(getProject(), "Loading " + getName() + "...", false, true, null,
                new Runnable() {
                    @Override
                    public void run() {
                        final String nodeName = node.getName();
                        node.setName(nodeName + " (Refreshing...)");

                        Futures.addCallback(future, new FutureCallback<List<Node>>() {
                            @Override
                            public void onSuccess(List<Node> nodes) {
                                updateName(null);
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                updateName(throwable);
                            }

                            private void updateName(final Throwable throwable) {
                                DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        node.setName(nodeName);

                                        if (throwable != null) {
                                            DefaultLoader.getUIHelper().showException("An error occurred while attempting " +
                                                            "to load " + node.getName() + ".",
                                                    throwable,
                                                    "MS Services - Error Loading " + node.getName(),
                                                    false,
                                                    true);
                                        }
                                    }
                                });
                            }
                        });

                        node.refreshItems(future);
                    }
                }
        );

        return future;
    }
}