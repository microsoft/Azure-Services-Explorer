/**
 * Copyright 2014 Microsoft Open Technologies Inc.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.tooling.msservices.model.ws;

import com.microsoft.tooling.msservices.helpers.NotNull;
import com.microsoft.tooling.msservices.model.ServiceTreeItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class WebSiteConfiguration implements ServiceTreeItem {
    public static class ConnectionInfo {
        private String name;
        private String type;
        private String connectionString;

        public ConnectionInfo(@NotNull String name, @NotNull String type, @NotNull String connectionString) {
            this.name = name;
            this.type = type;
            this.connectionString = connectionString;
        }

        @NotNull
        public String getName() {
            return name;
        }

        @NotNull
        public String getType() {
            return type;
        }

        @NotNull
        public String getConnectionString() {
            return connectionString;
        }
    }

    private boolean loading;

    private String webSpaceName;
    private String webSiteName;
    private String netFrameworkVersion = "";
    private String javaVersion = "";
    private String javaContainer = "";
    private String javaContainerVersion = "";
    private String phpVersion = "";
    private boolean httpLoggingEnabled = false;
    private boolean detailedErrorLoggingEnabled = false;
    private boolean requestTracingEnabled = false;
    private Calendar requestTracingExpirationTime = new GregorianCalendar();
    private boolean remoteDebuggingEnabled = false;
    private List<ConnectionInfo> connectionInfoList;
    private String subscriptionId;

    public WebSiteConfiguration(@NotNull String webSpaceName,
                                @NotNull String webSiteName,
                                @NotNull String subscriptionId) {
        this.webSpaceName = webSpaceName;
        this.webSiteName = webSiteName;
        this.subscriptionId = subscriptionId;
        this.connectionInfoList = new ArrayList<ConnectionInfo>();
    }

    @Override
    public boolean isLoading() {
        return loading;
    }

    @Override
    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    @NotNull
    public String getWebSpaceName() {
        return webSpaceName;
    }

    @NotNull
    public String getWebSiteName() {
        return webSiteName;
    }

    @NotNull
    public String getNetFrameworkVersion() {
        return netFrameworkVersion;
    }

    public void setNetFrameworkVersion(@NotNull String netFrameworkVersion) {
        this.netFrameworkVersion = netFrameworkVersion;
    }

    @NotNull
    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(@NotNull String javaVersion) {
        this.javaVersion = javaVersion;
    }

    @NotNull
    public String getJavaContainer() {
        return javaContainer;
    }

    public void setJavaContainer(@NotNull String javaContainer) {
        this.javaContainer = javaContainer;
    }

    @NotNull
    public String getJavaContainerVersion() {
        return javaContainerVersion;
    }

    public void setJavaContainerVersion(@NotNull String javaContainerVersion) {
        this.javaContainerVersion = javaContainerVersion;
    }

    @NotNull
    public String getPhpVersion() {
        return phpVersion;
    }

    public void setPhpVersion(@NotNull String phpVersion) {
        this.phpVersion = phpVersion;
    }

    public boolean isHttpLoggingEnabled() {
        return httpLoggingEnabled;
    }

    public void setHttpLoggingEnabled(boolean httpLoggingEnabled) {
        this.httpLoggingEnabled = httpLoggingEnabled;
    }

    public boolean isDetailedErrorLoggingEnabled() {
        return detailedErrorLoggingEnabled;
    }

    public void setDetailedErrorLoggingEnabled(boolean detailedErrorLoggingEnabled) {
        this.detailedErrorLoggingEnabled = detailedErrorLoggingEnabled;
    }

    public boolean isRequestTracingEnabled() {
        return requestTracingEnabled;
    }

    public void setRequestTracingEnabled(boolean requestTracingEnabled) {
        this.requestTracingEnabled = requestTracingEnabled;
    }

    @NotNull
    public Calendar getRequestTracingExpirationTime() {
        return requestTracingExpirationTime;
    }

    public void setRequestTracingExpirationTime(@NotNull Calendar requestTracingExpirationTime) {
        this.requestTracingExpirationTime = requestTracingExpirationTime;
    }

    public boolean isRemoteDebuggingEnabled() {
        return remoteDebuggingEnabled;
    }

    public void setRemoteDebuggingEnabled(boolean remoteDebuggingEnabled) {
        this.remoteDebuggingEnabled = remoteDebuggingEnabled;
    }

    @NotNull
    public List<ConnectionInfo> getConnectionInfoList() {
        return connectionInfoList;
    }

    @NotNull
    public String getSubscriptionId() {
        return subscriptionId;
    }

    @Override
    public String toString() {
        return webSiteName + " Configuration" + (loading ? " (loading...)" : "");
    }
}