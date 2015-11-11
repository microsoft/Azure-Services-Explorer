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
package com.microsoft.windowsazure.management.websites;

import com.microsoft.windowsazure.core.LazyCollection;
import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.core.utils.BOMInputStream;
import com.microsoft.windowsazure.core.utils.XmlUtility;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.websites.models.RampUpRule;
import com.microsoft.windowsazure.management.websites.models.RoutingRule;
import com.microsoft.windowsazure.management.websites.models.WebSiteGetPublishProfileResponse;
import com.microsoft.windowsazure.management.websites.models.WebSiteUpdateConfigurationParameters;
import com.microsoft.windowsazure.tracing.CloudTracing;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class WebSiteOperationsImpl2 extends WebSiteOperationsImpl {
    public WebSiteOperationsImpl2(WebSiteManagementClientImpl client) {
        super(client);
    }

    @Override
    public WebSiteGetPublishProfileResponse getPublishProfile(String webSpaceName, String webSiteName) throws IOException, ServiceException, ParserConfigurationException, SAXException, URISyntaxException {
        // Validate
        if (webSpaceName == null) {
            throw new NullPointerException("webSpaceName");
        }
        if (webSiteName == null) {
            throw new NullPointerException("webSiteName");
        }

        // Tracing
        boolean shouldTrace = CloudTracing.getIsEnabled();
        String invocationId = null;
        if (shouldTrace) {
            invocationId = Long.toString(CloudTracing.getNextInvocationId());
            HashMap<String, Object> tracingParameters = new HashMap<String, Object>();
            tracingParameters.put("webSpaceName", webSpaceName);
            tracingParameters.put("webSiteName", webSiteName);
            CloudTracing.enter(invocationId, this, "getPublishProfileAsync", tracingParameters);
        }

        // Construct URL
        String url = "/" + (this.getClient().getCredentials().getSubscriptionId() != null ? this.getClient().getCredentials().getSubscriptionId().trim() : "") + "/services/WebSpaces/" + webSpaceName.trim() + "/sites/" + webSiteName.trim() + "/publishxml";
        String baseUrl = this.getClient().getBaseUri().toString();
        // Trim '/' character from the end of baseUrl and beginning of url.
        if (baseUrl.charAt(baseUrl.length() - 1) == '/') {
            baseUrl = baseUrl.substring(0, (baseUrl.length() - 1) + 0);
        }
        if (url.charAt(0) == '/') {
            url = url.substring(1);
        }
        url = baseUrl + "/" + url;
        url = url.replace(" ", "%20");

        // Create HTTP transport objects
        HttpGet httpRequest = new HttpGet(url);

        // Set Headers
        httpRequest.setHeader("x-ms-version", "2014-04-01");

        // Send Request
        HttpResponse httpResponse = null;
        try {
            if (shouldTrace) {
                CloudTracing.sendRequest(invocationId, httpRequest);
            }
            httpResponse = this.getClient().getHttpClient().execute(httpRequest);
            if (shouldTrace) {
                CloudTracing.receiveResponse(invocationId, httpResponse);
            }
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                ServiceException ex = ServiceException.createFromXml(httpRequest, null, httpResponse, httpResponse.getEntity());
                if (shouldTrace) {
                    CloudTracing.error(invocationId, ex);
                }
                throw ex;
            }

            // Create Result
            WebSiteGetPublishProfileResponse result = null;
            // Deserialize Response
            InputStream responseContent = httpResponse.getEntity().getContent();
            result = new WebSiteGetPublishProfileResponse();
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document responseDoc = documentBuilder.parse(new BOMInputStream(responseContent));

            Element publishDataElement = XmlUtility.getElementByTagNameNS(responseDoc, "", "publishData");
            if (publishDataElement != null) {
                if (publishDataElement != null) {
                    for (int i1 = 0; i1 < com.microsoft.windowsazure.core.utils.XmlUtility.getElementsByTagNameNS(publishDataElement, "", "publishProfile").size(); i1 = i1 + 1) {
                        org.w3c.dom.Element publishProfilesElement = ((org.w3c.dom.Element) com.microsoft.windowsazure.core.utils.XmlUtility.getElementsByTagNameNS(publishDataElement, "", "publishProfile").get(i1));
                        WebSiteGetPublishProfileResponse.PublishProfile publishProfileInstance = new WebSiteGetPublishProfileResponse.PublishProfile();
                        result.getPublishProfiles().add(publishProfileInstance);

                        Attr profileNameAttribute = publishProfilesElement.getAttributeNodeNS(null, "profileName");
                        if (profileNameAttribute != null) {
                            publishProfileInstance.setProfileName(profileNameAttribute.getValue());
                        }

                        Attr publishMethodAttribute = publishProfilesElement.getAttributeNodeNS(null, "publishMethod");
                        if (publishMethodAttribute != null) {
                            publishProfileInstance.setPublishMethod(publishMethodAttribute.getValue());
                        }

                        Attr publishUrlAttribute = publishProfilesElement.getAttributeNodeNS(null, "publishUrl");
                        if (publishUrlAttribute != null) {
                            publishProfileInstance.setPublishUrl(publishUrlAttribute.getValue());
                        }

                        Attr msdeploySiteAttribute = publishProfilesElement.getAttributeNodeNS(null, "msdeploySite");
                        if (msdeploySiteAttribute != null) {
                            publishProfileInstance.setMSDeploySite(msdeploySiteAttribute.getValue());
                        }

                        Attr ftpPassiveModeAttribute = publishProfilesElement.getAttributeNodeNS(null, "ftpPassiveMode");
                        if (ftpPassiveModeAttribute != null) {
                            publishProfileInstance.setFtpPassiveMode(DatatypeConverter.parseBoolean(ftpPassiveModeAttribute.getValue().toLowerCase()));
                        }

                        Attr userNameAttribute = publishProfilesElement.getAttributeNodeNS(null, "userName");
                        if (userNameAttribute != null) {
                            publishProfileInstance.setUserName(userNameAttribute.getValue());
                        }

                        Attr userPWDAttribute = publishProfilesElement.getAttributeNodeNS(null, "userPWD");
                        if (userPWDAttribute != null) {
                            publishProfileInstance.setUserPassword(userPWDAttribute.getValue());
                        }

                        Attr destinationAppUrlAttribute = publishProfilesElement.getAttributeNodeNS(null, "destinationAppUrl");
                        if (destinationAppUrlAttribute != null) {
                            publishProfileInstance.setDestinationAppUri(new URI(destinationAppUrlAttribute.getValue()));
                        }

                        Attr sQLServerDBConnectionStringAttribute = publishProfilesElement.getAttributeNodeNS(null, "SQLServerDBConnectionString");
                        if (sQLServerDBConnectionStringAttribute != null) {
                            publishProfileInstance.setSqlServerConnectionString(sQLServerDBConnectionStringAttribute.getValue());
                        }

                        Attr mySQLDBConnectionStringAttribute = publishProfilesElement.getAttributeNodeNS(null, "mySQLDBConnectionString");
                        if (mySQLDBConnectionStringAttribute != null) {
                            publishProfileInstance.setMySqlConnectionString(mySQLDBConnectionStringAttribute.getValue());
                        }

                        Attr hostingProviderForumLinkAttribute = publishProfilesElement.getAttributeNodeNS(null, "hostingProviderForumLink");
                        if (hostingProviderForumLinkAttribute != null) {
                            publishProfileInstance.setHostingProviderForumUri(new URI(hostingProviderForumLinkAttribute.getValue()));
                        }

                        Attr controlPanelLinkAttribute = publishProfilesElement.getAttributeNodeNS(null, "controlPanelLink");
                        if (controlPanelLinkAttribute != null) {
                            publishProfileInstance.setControlPanelUri(new URI(controlPanelLinkAttribute.getValue()));
                        }

                        Element databasesSequenceElement = XmlUtility.getElementByTagNameNS(publishProfilesElement, "", "databases");
                        if (databasesSequenceElement != null) {
                            for (int i2 = 0; i2 < com.microsoft.windowsazure.core.utils.XmlUtility.getElementsByTagNameNS(databasesSequenceElement, "", "add").size(); i2 = i2 + 1) {
                                org.w3c.dom.Element databasesElement = ((org.w3c.dom.Element) com.microsoft.windowsazure.core.utils.XmlUtility.getElementsByTagNameNS(databasesSequenceElement, "", "add").get(i2));
                                WebSiteGetPublishProfileResponse.Database addInstance = new WebSiteGetPublishProfileResponse.Database();
                                publishProfileInstance.getDatabases().add(addInstance);

                                Attr nameAttribute = databasesElement.getAttributeNodeNS(null, "name");
                                if (nameAttribute != null) {
                                    addInstance.setName(nameAttribute.getValue());
                                }

                                Attr connectionStringAttribute = databasesElement.getAttributeNodeNS(null, "connectionString");
                                if (connectionStringAttribute != null) {
                                    addInstance.setConnectionString(connectionStringAttribute.getValue());
                                }

                                Attr providerNameAttribute = databasesElement.getAttributeNodeNS(null, "providerName");
                                if (providerNameAttribute != null) {
                                    addInstance.setProviderName(providerNameAttribute.getValue());
                                }

                                Attr typeAttribute = databasesElement.getAttributeNodeNS(null, "type");
                                if (typeAttribute != null) {
                                    addInstance.setType(typeAttribute.getValue());
                                }
                            }
                        }
                    }
                }
            }

            result.setStatusCode(statusCode);
            if (httpResponse.getHeaders("x-ms-request-id").length > 0) {
                result.setRequestId(httpResponse.getFirstHeader("x-ms-request-id").getValue());
            }

            if (shouldTrace) {
                CloudTracing.exit(invocationId, result);
            }
            return result;
        } finally {
            if (httpResponse != null && httpResponse.getEntity() != null) {
                httpResponse.getEntity().getContent().close();
            }
        }
    }

    @Override
    public OperationResponse updateConfiguration(String webSpaceName, String webSiteName, WebSiteUpdateConfigurationParameters parameters) throws IOException, ServiceException {
        // Validate
        if (webSpaceName == null) {
            throw new NullPointerException("webSpaceName");
        }
        if (webSiteName == null) {
            throw new NullPointerException("webSiteName");
        }
        if (parameters == null) {
            throw new NullPointerException("parameters");
        }

        // Tracing
        boolean shouldTrace = CloudTracing.getIsEnabled();
        String invocationId = null;
        if (shouldTrace) {
            invocationId = Long.toString(CloudTracing.getNextInvocationId());
            HashMap<String, Object> tracingParameters = new HashMap<String, Object>();
            tracingParameters.put("webSpaceName", webSpaceName);
            tracingParameters.put("webSiteName", webSiteName);
            tracingParameters.put("parameters", parameters);
            CloudTracing.enter(invocationId, this, "updateConfigurationAsync", tracingParameters);
        }

        // Construct URL
        String url = "/" + (this.getClient().getCredentials().getSubscriptionId() != null ? this.getClient().getCredentials().getSubscriptionId().trim() : "") + "/services/WebSpaces/" + webSpaceName.trim() + "/sites/" + webSiteName.trim() + "/config";
        String baseUrl = this.getClient().getBaseUri().toString();
        // Trim '/' character from the end of baseUrl and beginning of url.
        if (baseUrl.charAt(baseUrl.length() - 1) == '/') {
            baseUrl = baseUrl.substring(0, (baseUrl.length() - 1) + 0);
        }
        if (url.charAt(0) == '/') {
            url = url.substring(1);
        }
        url = baseUrl + "/" + url;
        url = url.replace(" ", "%20");

        // Create HTTP transport objects
        HttpPut httpRequest = new HttpPut(url);

        // Set Headers
        httpRequest.setHeader("Accept", "application/json");
        httpRequest.setHeader("Content-Type", "application/json; charset=utf-8");
        httpRequest.setHeader("x-ms-version", "2014-04-01");

        // Serialize Request
        String requestContent = null;
        JsonNode requestDoc = null;

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode webSiteUpdateConfigurationParametersValue = objectMapper.createObjectNode();
        requestDoc = webSiteUpdateConfigurationParametersValue;

        if (parameters.getAppSettings() != null) {
            if (parameters.getAppSettings() instanceof LazyCollection == false || ((LazyCollection) parameters.getAppSettings()).isInitialized()) {
                ArrayNode appSettingsDictionary = objectMapper.createArrayNode();
                for (Map.Entry<String, String> entry : parameters.getAppSettings().entrySet()) {
                    String appSettingsKey = entry.getKey();
                    String appSettingsValue = entry.getValue();
                    ObjectNode appSettingsItemObject = objectMapper.createObjectNode();
                    ((ObjectNode) appSettingsItemObject).put("Name", appSettingsKey);
                    ((ObjectNode) appSettingsItemObject).put("Value", appSettingsValue);
                    appSettingsDictionary.add(appSettingsItemObject);
                }
                ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("AppSettings", appSettingsDictionary);
            }
        }

        if (parameters.getConnectionStrings() != null) {
            if (parameters.getConnectionStrings() instanceof LazyCollection == false || ((LazyCollection) parameters.getConnectionStrings()).isInitialized()) {
                ArrayNode connectionStringsArray = objectMapper.createArrayNode();
                for (WebSiteUpdateConfigurationParameters.ConnectionStringInfo connectionStringsItem : parameters.getConnectionStrings()) {
                    ObjectNode connectionStringInfoValue = objectMapper.createObjectNode();
                    connectionStringsArray.add(connectionStringInfoValue);

                    if (connectionStringsItem.getConnectionString() != null) {
                        ((ObjectNode) connectionStringInfoValue).put("ConnectionString", connectionStringsItem.getConnectionString());
                    }

                    if (connectionStringsItem.getName() != null) {
                        ((ObjectNode) connectionStringInfoValue).put("Name", connectionStringsItem.getName());
                    }

                    ((ObjectNode) connectionStringInfoValue).put("Type", connectionStringsItem.getType().ordinal());
                }
                ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("ConnectionStrings", connectionStringsArray);
            }
        }

        if (parameters.getDefaultDocuments() != null) {
            if (parameters.getDefaultDocuments() instanceof LazyCollection == false || ((LazyCollection) parameters.getDefaultDocuments()).isInitialized()) {
                ArrayNode defaultDocumentsArray = objectMapper.createArrayNode();
                for (String defaultDocumentsItem : parameters.getDefaultDocuments()) {
                    defaultDocumentsArray.add(defaultDocumentsItem);
                }
                ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("DefaultDocuments", defaultDocumentsArray);
            }
        }

        if (parameters.isDetailedErrorLoggingEnabled() != null) {
            ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("DetailedErrorLoggingEnabled", parameters.isDetailedErrorLoggingEnabled());
        }

        if (parameters.getDocumentRoot() != null) {
            ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("DocumentRoot", parameters.getDocumentRoot());
        }

        if (parameters.getHandlerMappings() != null) {
            if (parameters.getHandlerMappings() instanceof LazyCollection == false || ((LazyCollection) parameters.getHandlerMappings()).isInitialized()) {
                ArrayNode handlerMappingsArray = objectMapper.createArrayNode();
                for (WebSiteUpdateConfigurationParameters.HandlerMapping handlerMappingsItem : parameters.getHandlerMappings()) {
                    ObjectNode handlerMappingValue = objectMapper.createObjectNode();
                    handlerMappingsArray.add(handlerMappingValue);

                    if (handlerMappingsItem.getArguments() != null) {
                        ((ObjectNode) handlerMappingValue).put("Arguments", handlerMappingsItem.getArguments());
                    }

                    if (handlerMappingsItem.getExtension() != null) {
                        ((ObjectNode) handlerMappingValue).put("Extension", handlerMappingsItem.getExtension());
                    }

                    if (handlerMappingsItem.getScriptProcessor() != null) {
                        ((ObjectNode) handlerMappingValue).put("ScriptProcessor", handlerMappingsItem.getScriptProcessor());
                    }
                }
                ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("HandlerMappings", handlerMappingsArray);
            }
        }

        if (parameters.isHttpLoggingEnabled() != null) {
            ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("HttpLoggingEnabled", parameters.isHttpLoggingEnabled());
        }

        if (parameters.getLogsDirectorySizeLimit() != null) {
            ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("LogsDirectorySizeLimit", parameters.getLogsDirectorySizeLimit());
        }

        if (parameters.getManagedPipelineMode() != null) {
            ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("ManagedPipelineMode", parameters.getManagedPipelineMode().ordinal());
        }

        if (parameters.getMetadata() != null) {
            if (parameters.getMetadata() instanceof LazyCollection == false || ((LazyCollection) parameters.getMetadata()).isInitialized()) {
                ArrayNode metadataDictionary = objectMapper.createArrayNode();
                for (Map.Entry<String, String> entry2 : parameters.getMetadata().entrySet()) {
                    String metadataKey = entry2.getKey();
                    String metadataValue = entry2.getValue();
                    ObjectNode metadataItemObject = objectMapper.createObjectNode();
                    ((ObjectNode) metadataItemObject).put("Name", metadataKey);
                    ((ObjectNode) metadataItemObject).put("Value", metadataValue);
                    metadataDictionary.add(metadataItemObject);
                }
                ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("Metadata", metadataDictionary);
            }
        }

        if (parameters.getNetFrameworkVersion() != null) {
            ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("NetFrameworkVersion", parameters.getNetFrameworkVersion());
        }

        if (parameters.getNumberOfWorkers() != null) {
            ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("NumberOfWorkers", parameters.getNumberOfWorkers());
        }

        if (parameters.getPhpVersion() != null) {
            ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("PhpVersion", parameters.getPhpVersion());
        }

        if (parameters.isRemoteDebuggingEnabled() != null) {
            ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("RemoteDebuggingEnabled", parameters.isRemoteDebuggingEnabled());
        }

        if (parameters.getRemoteDebuggingVersion() != null) {
            ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("RemoteDebuggingVersion", parameters.getRemoteDebuggingVersion().toString());
        }

        if (parameters.isRequestTracingEnabled() != null) {
            ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("RequestTracingEnabled", parameters.isRequestTracingEnabled());
        }

        if (parameters.getRequestTracingExpirationTime() != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("RequestTracingExpirationTime", simpleDateFormat.format(parameters.getRequestTracingExpirationTime().getTime()));
        }

        if (parameters.getScmType() != null) {
            ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("ScmType", parameters.getScmType());
        }

        if (parameters.isUse32BitWorkerProcess() != null) {
            ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("Use32BitWorkerProcess", parameters.isUse32BitWorkerProcess());
        }

        if (parameters.isWebSocketsEnabled() != null) {
            ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("WebSocketsEnabled", parameters.isWebSocketsEnabled());
        }

        if (parameters.isAlwaysOn() != null) {
            ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("AlwaysOn", parameters.isAlwaysOn());
        }

        if (parameters.getRoutingRules() != null) {
            if (parameters.getRoutingRules() instanceof LazyCollection == false || ((LazyCollection) parameters.getRoutingRules()).isInitialized()) {
                ArrayNode routingRulesArray = objectMapper.createArrayNode();
                for (RoutingRule routingRulesItem : parameters.getRoutingRules()) {
                    ObjectNode routingRuleValue = objectMapper.createObjectNode();
                    routingRulesArray.add(routingRuleValue);
                    if (routingRulesItem instanceof RampUpRule) {
                        ((ObjectNode) routingRuleValue).put("__type", "RampUpRule:http://schemas.microsoft.com/windowsazure");
                        RampUpRule derived = ((RampUpRule) routingRulesItem);

                        if (derived.getActionHostName() != null) {
                            ((ObjectNode) routingRuleValue).put("ActionHostName", derived.getActionHostName());
                        }

                        ((ObjectNode) routingRuleValue).put("ReroutePercentage", derived.getReroutePercentage());

                        if (derived.getChangeStep() != null) {
                            ((ObjectNode) routingRuleValue).put("ChangeStep", derived.getChangeStep());
                        }

                        if (derived.getChangeIntervalInMinutes() != null) {
                            ((ObjectNode) routingRuleValue).put("ChangeIntervalInMinutes", derived.getChangeIntervalInMinutes());
                        }

                        if (derived.getMinReroutePercentage() != null) {
                            ((ObjectNode) routingRuleValue).put("MinReroutePercentage", derived.getMinReroutePercentage());
                        }

                        if (derived.getMaxReroutePercentage() != null) {
                            ((ObjectNode) routingRuleValue).put("MaxReroutePercentage", derived.getMaxReroutePercentage());
                        }

                        if (derived.getChangeDecisionCallbackUrl() != null) {
                            ((ObjectNode) routingRuleValue).put("ChangeDecisionCallbackUrl", derived.getChangeDecisionCallbackUrl());
                        }

                        if (derived.getName() != null) {
                            ((ObjectNode) routingRuleValue).put("Name", derived.getName());
                        }
                    }
                }
                ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("RoutingRules", routingRulesArray);
            }
        }

        if (parameters.getJavaVersion() != null) {
            ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("JavaVersion", parameters.getJavaVersion());
        }

        if (parameters.getJavaContainer() != null) {
            ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("JavaContainer", parameters.getJavaContainer());
        }

        if (parameters.getJavaContainerVersion() != null) {
            ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("JavaContainerVersion", parameters.getJavaContainerVersion());
        }

        if (parameters.getAutoSwapSlotName() != null) {
            ((ObjectNode) webSiteUpdateConfigurationParametersValue).put("AutoSwapSlotName", parameters.getAutoSwapSlotName());
        }

        StringWriter stringWriter = new StringWriter();
        objectMapper.writeValue(stringWriter, requestDoc);
        requestContent = stringWriter.toString();
        StringEntity entity = new StringEntity(requestContent);
        httpRequest.setEntity(entity);
        httpRequest.setHeader("Content-Type", "application/json; charset=utf-8");

        // Send Request
        HttpResponse httpResponse = null;
        try {
            if (shouldTrace) {
                CloudTracing.sendRequest(invocationId, httpRequest);
            }
            httpResponse = this.getClient().getHttpClient().execute(httpRequest);
            if (shouldTrace) {
                CloudTracing.receiveResponse(invocationId, httpResponse);
            }
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                ServiceException ex = ServiceException.createFromXml(httpRequest, requestContent, httpResponse, httpResponse.getEntity());
                if (shouldTrace) {
                    CloudTracing.error(invocationId, ex);
                }
                throw ex;
            }

            // Create Result
            OperationResponse result = null;
            result = new OperationResponse();
            result.setStatusCode(statusCode);
            if (httpResponse.getHeaders("x-ms-request-id").length > 0) {
                result.setRequestId(httpResponse.getFirstHeader("x-ms-request-id").getValue());
            }

            if (shouldTrace) {
                CloudTracing.exit(invocationId, result);
            }
            return result;
        } finally {
            if (httpResponse != null && httpResponse.getEntity() != null) {
                httpResponse.getEntity().getContent().close();
            }
        }
    }
}