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

import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoft.tooling.msservices.model.ms.MobileService;
import com.microsoft.tooling.msservices.model.ms.MobileServiceScriptTreeItem;
import com.microsoft.tooling.msservices.model.ms.Script;
import com.microsoft.tooling.msservices.serviceexplorer.Node;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

public abstract class ScriptNodeBase extends Node {
    public ScriptNodeBase(String id, String name, Node parent, String iconPath) {
        super(id, name, parent, iconPath);
    }

    protected abstract void downloadScript(MobileService mobileService, String scriptName, String localFilePath) throws AzureCmdException;

    protected void onNodeClickInternal(final MobileServiceScriptTreeItem script) {
        // TODO: This function is far too long and confusing. Refactor this to smaller well-defined sub-routines.

        // find the parent MobileServiceNode node
        MobileServiceNode mobileServiceNode = findParentByType(MobileServiceNode.class);
        final MobileService mobileService = mobileServiceNode.getMobileService();

        boolean fileIsEditing = DefaultLoader.getIdeHelper().isFileEditing(getProject(), new File(script.getLocalFilePath(mobileService.getName())));

        if (!fileIsEditing) {
            try {
                File temppath = new File(script.getLocalDirPath(mobileService.getName()));
                temppath.mkdirs();

                if (script instanceof Script && ((Script) script).getSelfLink() == null) {
                    InputStream is = this.getClass().getResourceAsStream(
                            String.format("/com/microsoft/tooling/msservices/templates/%s.js",
                                    ((Script) script).getOperation()));
                    final ByteArrayOutputStream buff = new ByteArrayOutputStream();

                    int b;
                    while ((b = is.read()) != -1)
                        buff.write(b);

                    final File tempf = new File(temppath, ((Script) script).getOperation() + ".js");
                    tempf.createNewFile();

                    DefaultLoader.getIdeHelper().saveFile(tempf, buff, this);
                } else {
                    boolean download = false;
                    final File file = new File(script.getLocalFilePath(mobileService.getName()));
                    if (file.exists()) {
                        String[] options = new String[]{"Use remote", "Use local"};
                        int optionDialog = JOptionPane.showOptionDialog(null,
                                "There is a local copy of the script. Do you want you replace it with the remote version?",
                                "Service Explorer",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                options,
                                options[1]);

                        if (optionDialog == JOptionPane.YES_OPTION) {
                            download = true;
                        }
                    } else {
                        download = true;
                    }

                    if (download) {
                        DefaultLoader.getIdeHelper().runInBackground(getProject(), "Loading Mobile Services data...", false, true, "Downloading script", new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    downloadScript(mobileService, script.getName(), script.getLocalFilePath(mobileService.getName()));
                                    DefaultLoader.getIdeHelper().openFile(file, ScriptNodeBase.this);

                                } catch (Throwable e) {
                                    DefaultLoader.getUIHelper().showException("An error occurred while attempting to write " +
                                                    "temporal editable file.", e,
                                            "MS Services - Error Writing Temp File", false, true);
                                }
                            }
                        });
                    } else {
                        DefaultLoader.getIdeHelper().openFile(file, ScriptNodeBase.this);
                    }
                }
            } catch (Throwable e) {
                DefaultLoader.getUIHelper().showException("An error occurred while attempting to write temporal editable " +
                                "file.", e,
                        "MS Services - Error Writing Temp File", false, true);
            }
        }
    }
}