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

import java.util.ArrayList;

public class PermissionItem {
    public PermissionItem(PermissionType type, String description) {
        this.description = description;
        this.type = type;
    }

    private String description;
    private PermissionType type;

    public PermissionType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }

    public static PermissionItem[] getTablePermissions() {
        ArrayList<PermissionItem> pilist = new ArrayList<PermissionItem>();
        pilist.add(new PermissionItem(PermissionType.Application, "Anybody with an application key"));
        pilist.add(new PermissionItem(PermissionType.User, "Only authenticated users"));
        pilist.add(new PermissionItem(PermissionType.Admin, "Only scripts and admins"));
        pilist.add(new PermissionItem(PermissionType.Public, "Everyone"));

        PermissionItem[] res = new PermissionItem[pilist.size()];
        return pilist.toArray(res);
    }

    public static String getPermitionString(PermissionType type) {
        switch (type) {
            case Admin:
                return "admin";
            case Application:
                return "application";
            case Public:
                return "public";
            case User:
                return "user";
        }
        return null;
    }

    public static PermissionType getPermitionType(String type) {
        if (type.equals("admin"))
            return PermissionType.Admin;
        else if (type.equals("application"))
            return PermissionType.Application;
        else if (type.equals("public"))
            return PermissionType.Public;
        else if (type.equals("user"))
            return PermissionType.User;

        return null;
    }
}
