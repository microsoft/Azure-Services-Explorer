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

package com.microsoft.tooling.msservices.model.ms;

public class CustomAPIPermissions {
    private PermissionType getPermission;
    private PermissionType postPermission;
    private PermissionType putPermission;
    private PermissionType patchPermission;
    private PermissionType deletePermission;

    public PermissionType getGetPermission() {
        return getPermission;
    }

    public PermissionType getPostPermission() {
        return postPermission;
    }

    public PermissionType getPutPermission() {
        return putPermission;
    }

    public PermissionType getPatchPermission() {
        return patchPermission;
    }

    public PermissionType getDeletePermission() {
        return deletePermission;
    }


    public void setGetPermission(PermissionType getPermission) {
        this.getPermission = getPermission;
    }

    public void setPostPermission(PermissionType postPermission) {
        this.postPermission = postPermission;
    }

    public void setPutPermission(PermissionType putPermission) {
        this.putPermission = putPermission;
    }

    public void setPatchPermission(PermissionType patchPermission) {
        this.patchPermission = patchPermission;
    }

    public void setDeletePermission(PermissionType deletePermission) {
        this.deletePermission = deletePermission;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("get=");
        sb.append(PermissionItem.getPermitionString(getPermission));
        sb.append(",");
        sb.append("put=");
        sb.append(PermissionItem.getPermitionString(putPermission));
        sb.append(",");
        sb.append("post=");
        sb.append(PermissionItem.getPermitionString(postPermission));
        sb.append(",");
        sb.append("patch=");
        sb.append(PermissionItem.getPermitionString(patchPermission));
        sb.append(",");
        sb.append("delete=");
        sb.append(PermissionItem.getPermitionString(deletePermission));

        return sb.toString();
    }


}
