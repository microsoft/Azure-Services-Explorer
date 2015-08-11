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

import com.microsoft.tooling.msservices.model.ServiceTreeItem;

import java.util.ArrayList;

public class MobileService implements ServiceTreeItem {
    public static final String NODE_RUNTIME = "JavaScript";
    public static final String NET_RUNTIME = ".NET Framework";

    private boolean loading;

    public MobileService() {
        tables = new ArrayList<Table>();
    }

    @Override
    public String toString() {
        return name + (loading ? " (loading...)" : "");
    }

    private String name;
    private String type;
    private String state;
    private String selfLink;
    private String appUrl;
    private String appKey;
    private String masterKey;
    private ArrayList<Table> tables;
    private String webspace;
    private String region;
    private String mgmtPortalLink;
    private String subcriptionId;
    private String runtime;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getState() {
        return state;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getMasterKey() {
        return masterKey;
    }

    public ArrayList<Table> getTables() {
        return tables;
    }

    public String getWebspace() {
        return webspace;
    }

    public String getRegion() {
        return region;
    }

    public String getMgmtPortalLink() {
        return mgmtPortalLink;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public void setMasterKey(String masterKey) {
        this.masterKey = masterKey;
    }

    public void setWebspace(String webspace) {
        this.webspace = webspace;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setMgmtPortalLink(String mgmtPortalLink) {
        this.mgmtPortalLink = mgmtPortalLink;
    }

    @Override
    public boolean isLoading() {
        return loading;
    }

    @Override
    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public String getSubcriptionId() {
        return subcriptionId;
    }

    public void setSubcriptionId(String subcriptionId) {
        this.subcriptionId = subcriptionId;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }
}