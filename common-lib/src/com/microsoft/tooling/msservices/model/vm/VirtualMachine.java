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

import java.util.ArrayList;
import java.util.List;

public class VirtualMachine implements ServiceTreeItem {
    public static enum Status {
        Unknown,
        Ready,
        Stopped,
        StoppedDeallocated,
        Busy,
        Creating,
        Starting,
        Stopping,
        Deleting,
        Restarting,
        Cycling,
        FailedStarting,
        Unresponsive,
        Preparing
    }

    private boolean loading;
    private String name;
    private String serviceName;
    private String deploymentName;
    private String availabilitySet;
    private String subnet;
    private String size;
    private Status status;
    private String subscriptionId;
    private List<Endpoint> endpoints;

    public VirtualMachine(@NotNull String name, @NotNull String serviceName, @NotNull String deploymentName,
                          @NotNull String availabilitySet, @NotNull String subnet, @NotNull String size,
                          @NotNull Status status, @NotNull String subscriptionId) {
        this.name = name;
        this.serviceName = serviceName;
        this.deploymentName = deploymentName;
        this.availabilitySet = availabilitySet;
        this.subnet = subnet;
        this.size = size;
        this.status = status;
        this.subscriptionId = subscriptionId;
        this.endpoints = new ArrayList<Endpoint>();
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
    public String getServiceName() {
        return serviceName;
    }

    @NotNull
    public String getDeploymentName() {
        return deploymentName;
    }

    public void setDeploymentName(@NotNull String deploymentName) {
        this.deploymentName = deploymentName;
    }

    @NotNull
    public String getAvailabilitySet() {
        return availabilitySet;
    }

    public void setAvailabilitySet(@NotNull String availabilitySet) {
        this.availabilitySet = availabilitySet;
    }

    @NotNull
    public String getSubnet() {
        return subnet;
    }

    public void setSubnet(@NotNull String subnet) {
        this.subnet = subnet;
    }

    @NotNull
    public String getSize() {
        return size;
    }

    public void setSize(@NotNull String size) {
        this.size = size;
    }

    @NotNull
    public Status getStatus() {
        return status;
    }

    public void setStatus(@NotNull Status status) {
        this.status = status;
    }

    @NotNull
    public String getSubscriptionId() {
        return subscriptionId;
    }

    @NotNull
    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

    @Override
    public String toString() {
        return name + (loading ? " (loading...)" : "");
    }
}