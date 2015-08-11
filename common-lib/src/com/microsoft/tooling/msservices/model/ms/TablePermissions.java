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

public class TablePermissions {
    private PermissionType insert;
    private PermissionType read;
    private PermissionType update;
    private PermissionType delete;

    public PermissionType getInsert() {
        return insert;
    }

    public void setInsert(PermissionType insert) {
        this.insert = insert;
    }

    public PermissionType getRead() {
        return read;
    }

    public void setRead(PermissionType read) {
        this.read = read;
    }

    public PermissionType getUpdate() {
        return update;
    }

    public void setUpdate(PermissionType update) {
        this.update = update;
    }

    public PermissionType getDelete() {
        return delete;
    }

    public void setDelete(PermissionType delete) {
        this.delete = delete;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("insert=");
        sb.append(PermissionItem.getPermitionString(insert));
        sb.append(",");
        sb.append("read=");
        sb.append(PermissionItem.getPermitionString(read));
        sb.append(",");
        sb.append("update=");
        sb.append(PermissionItem.getPermitionString(update));
        sb.append(",");
        sb.append("delete=");
        sb.append(PermissionItem.getPermitionString(delete));

        return sb.toString();
    }
}
