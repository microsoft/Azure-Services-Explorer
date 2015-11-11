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
import java.util.List;

public class WebSitePublishSettings implements ServiceTreeItem {
    public static abstract class PublishProfile {
        private String name;
        private String publishUrl;
        private String userName;
        private String password;
        private String destinationAppUrl;
        private String sqlServerDBConnectionString;
        private String mySQLDBConnectionString;
        private String hostingProviderForumLink;
        private String controlPanelLink;

        public PublishProfile(@NotNull String name) {
            this.name = name;
        }

        @NotNull
        public String getName() {
            return name;
        }

        @NotNull
        public abstract String getPublishMethod();

        @NotNull
        public String getPublishUrl() {
            return publishUrl;
        }

        public void setPublishUrl(@NotNull String publishUrl) {
            this.publishUrl = publishUrl;
        }

        @NotNull
        public String getUserName() {
            return userName;
        }

        public void setUserName(@NotNull String userName) {
            this.userName = userName;
        }

        @NotNull
        public String getPassword() {
            return password;
        }

        public void setPassword(@NotNull String password) {
            this.password = password;
        }

        @NotNull
        public String getDestinationAppUrl() {
            return destinationAppUrl;
        }

        public void setDestinationAppUrl(@NotNull String destinationAppUrl) {
            this.destinationAppUrl = destinationAppUrl;
        }

        @NotNull
        public String getSqlServerDBConnectionString() {
            return sqlServerDBConnectionString;
        }

        public void setSqlServerDBConnectionString(@NotNull String sqlServerDBConnectionString) {
            this.sqlServerDBConnectionString = sqlServerDBConnectionString;
        }

        @NotNull
        public String getMySQLDBConnectionString() {
            return mySQLDBConnectionString;
        }

        public void setMySQLDBConnectionString(@NotNull String mySQLDBConnectionString) {
            this.mySQLDBConnectionString = mySQLDBConnectionString;
        }

        @NotNull
        public String getHostingProviderForumLink() {
            return hostingProviderForumLink;
        }

        public void setHostingProviderForumLink(@NotNull String hostingProviderForumLink) {
            this.hostingProviderForumLink = hostingProviderForumLink;
        }

        @NotNull
        public String getControlPanelLink() {
            return controlPanelLink;
        }

        public void setControlPanelLink(@NotNull String controlPanelLink) {
            this.controlPanelLink = controlPanelLink;
        }
    }

    public static class MSDeployPublishProfile extends PublishProfile {
        private String msdeploySite;

        public MSDeployPublishProfile(@NotNull String name) {
            super(name);
        }

        @NotNull
        @Override
        public String getPublishMethod() {
            return "MSDeploy";
        }

        @NotNull
        public String getMsdeploySite() {
            return msdeploySite;
        }

        public void setMsdeploySite(@NotNull String msdeploySite) {
            this.msdeploySite = msdeploySite;
        }
    }

    public static class FTPPublishProfile extends PublishProfile {
        private boolean ftpPassiveMode = false;

        public FTPPublishProfile(@NotNull String name) {
            super(name);
        }

        @NotNull
        @Override
        public String getPublishMethod() {
            return "FTP";
        }

        public boolean isFtpPassiveMode() {
            return ftpPassiveMode;
        }

        public void setFtpPassiveMode(boolean ftpPassiveMode) {
            this.ftpPassiveMode = ftpPassiveMode;
        }
    }

    private boolean loading;

    private String webSpaceName;
    private String webSiteName;
    private List<PublishProfile> publishProfileList;
    private String subscriptionId;

    public WebSitePublishSettings(@NotNull String webSpaceName,
                                  @NotNull String webSiteName,
                                  @NotNull String subscriptionId) {
        this.webSpaceName = webSpaceName;
        this.webSiteName = webSiteName;
        this.subscriptionId = subscriptionId;
        this.publishProfileList = new ArrayList<PublishProfile>();
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
    public List<PublishProfile> getPublishProfileList() {
        return publishProfileList;
    }

    @NotNull
    public String getSubscriptionId() {
        return subscriptionId;
    }

    @Override
    public String toString() {
        return webSiteName + " Publish Settings" + (loading ? " (loading...)" : "");
    }
}