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
package com.microsoft.tooling.msservices.model.vm;

import com.microsoft.tooling.msservices.helpers.NotNull;
import com.microsoft.tooling.msservices.model.ServiceTreeItem;

import java.util.Calendar;

public class VirtualMachineImage implements ServiceTreeItem {
    private boolean loading;
    private String name;
    private String type;
    private String category;
    private String publisherName;
    private Calendar publishedDate;
    private String label;
    private String description;
    private String operatingSystemType;
    private String location;
    private String eulaUri;
    private String privacyUri;
    private String pricingUri;
    private String recommendedVMSize;
    private boolean showInGui;

    public VirtualMachineImage(@NotNull String name, @NotNull String type, @NotNull String category,
                               @NotNull String publisherName, @NotNull Calendar publishedDate, @NotNull String label,
                               @NotNull String description, @NotNull String operatingSystemType, @NotNull String location,
                               @NotNull String eulaUri, @NotNull String privacyUri, @NotNull String pricingUri,
                               @NotNull String recommendedVMSize, boolean showInGui) {
        this.name = name;
        this.type = type;
        this.category = category;
        this.publisherName = publisherName;
        this.publishedDate = publishedDate;
        this.label = label;
        this.description = description;
        this.operatingSystemType = operatingSystemType;
        this.location = location;
        this.eulaUri = eulaUri;
        this.privacyUri = privacyUri;
        this.pricingUri = pricingUri;
        this.recommendedVMSize = recommendedVMSize;
        this.showInGui = showInGui;
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
    public String getName() {
        return name;
    }

    @NotNull
    public String getType() {
        return type;
    }

    @NotNull
    public String getCategory() {
        return category;
    }

    @NotNull
    public String getPublisherName() {
        return publisherName;
    }

    @NotNull
    public Calendar getPublishedDate() {
        return publishedDate;
    }

    @NotNull
    public String getLabel() {
        return label;
    }

    @NotNull
    public String getDescription() {
        return description;
    }

    @NotNull
    public String getOperatingSystemType() {
        return operatingSystemType;
    }

    @NotNull
    public String getLocation() {
        return location;
    }

    @NotNull
    public String getEulaUri() {
        return eulaUri;
    }

    @NotNull
    public String getPrivacyUri() {
        return privacyUri;
    }

    @NotNull
    public String getPricingUri() {
        return pricingUri;
    }

    @NotNull
    public String getRecommendedVMSize() {
        return recommendedVMSize;
    }

    public boolean isShowInGui() {
        return showInGui;
    }

    @Override
    public String toString() {
        return label + (loading ? " (loading...)" : "");
    }
}