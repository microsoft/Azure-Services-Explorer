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

import com.microsoft.tooling.msservices.helpers.NotNull;

public class UserInfo {
    private final String tenantId;
    private final String uniqueName;

    public UserInfo(@NotNull String tenantId, @NotNull String uniqueName) {
        this.tenantId = tenantId;
        this.uniqueName = uniqueName;
    }

    @NotNull
    public String getTenantId() {
        return this.tenantId;
    }

    @NotNull
    public String getUniqueName() {
        return this.uniqueName;
    }

    @Override
    public boolean equals(Object otherObj) {
        if (otherObj != null && otherObj instanceof UserInfo) {
            UserInfo other = (UserInfo) otherObj;
            return tenantId.equals(other.tenantId) && uniqueName.equals(other.uniqueName);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return tenantId.hashCode() * 13 + uniqueName.hashCode();
    }
}