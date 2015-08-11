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
package com.microsoft.intellij.serviceexplorer.azure.mobileservice;

import com.intellij.openapi.project.Project;
import com.microsoft.intellij.forms.CustomAPIForm;
import com.microsoft.tooling.msservices.helpers.Name;
import com.microsoft.tooling.msservices.model.ms.MobileService;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.mobileservice.CustomAPINode;
import com.microsoft.tooling.msservices.serviceexplorer.azure.mobileservice.MobileServiceNode;

@Name("Edit Custom API")
public class EditCustomAPIAction extends NodeActionListener {
    private CustomAPINode customAPINode;

    public EditCustomAPIAction(CustomAPINode customAPINode) {
        this.customAPINode = customAPINode;
    }

    @Override
    public void actionPerformed(NodeActionEvent e) {
        // get the parent MobileServiceNode node
        MobileServiceNode mobileServiceNode = customAPINode.findParentByType(MobileServiceNode.class);
        MobileService mobileService = mobileServiceNode.getMobileService();

        final CustomAPIForm form = new CustomAPIForm((Project) customAPINode.getProject());
        form.setEditingCustomAPI(customAPINode.getCustomAPI());
        form.setServiceName(mobileService.getName());

        form.setSubscriptionId(mobileService.getSubcriptionId());
        form.setAfterSave(new Runnable() {
            @Override
            public void run() {
                customAPINode.setCustomAPI(form.getEditingCustomAPI());
            }
        });
        form.show();
    }
}