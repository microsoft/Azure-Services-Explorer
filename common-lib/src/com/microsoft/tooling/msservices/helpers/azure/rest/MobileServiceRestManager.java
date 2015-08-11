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
package com.microsoft.tooling.msservices.helpers.azure.rest;

import com.microsoft.tooling.msservices.helpers.NotNull;
import com.microsoft.tooling.msservices.helpers.Nullable;
import com.microsoft.tooling.msservices.helpers.XmlHelper;
import com.microsoft.tooling.msservices.helpers.azure.AzureCmdException;
import org.w3c.dom.NodeList;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.xpath.XPathConstants;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MobileServiceRestManager extends RestServiceManagerBaseImpl {
    private static RestServiceManager instance;

    @NotNull
    public static synchronized RestServiceManager getManager() {
        if (instance == null) {
            instance = new MobileServiceRestManager();
        }

        return instance;
    }

    @NotNull
    public String executePollRequest(@NotNull String managementUrl,
                                     @NotNull String path,
                                     @NotNull ContentType contentType,
                                     @NotNull String method,
                                     @Nullable String postData,
                                     @NotNull String pollPath,
                                     @NotNull HttpsURLConnectionProvider sslConnectionProvider)
            throws AzureCmdException {
        try {
            HttpsURLConnection sslConnection = sslConnectionProvider.getSSLConnection(managementUrl, path, contentType);
            HttpResponse response = getResponse(method, postData, sslConnection);
            int code = response.getCode();

            if (code < 200 || code >= 300) {
                throw new AzureCmdException(String.format("Error status code %s: %s", code, response.getMessage()),
                        response.getContent());
            } else {
                if (code == 202) {
                    Map<String, List<String>> headers = response.getHeaders();

                    if (!headers.containsKey("x-ms-request-id") ||
                            headers.get("x-ms-request-id").size() <= 0 ||
                            headers.get("x-ms-request-id").get(0) == null) {
                        throw new AzureCmdException(String.format("Status code %s: %s. Contains no valid request id",
                                code,
                                response.getMessage()),
                                response.getContent());
                    }

                    String requestId = headers.get("x-ms-request-id").get(0);
                    String operationPath = pollPath + requestId;
                    executePollRequest(managementUrl, operationPath, sslConnectionProvider);
                }

                return response.getContent();
            }
        } catch (IOException e) {
            throw new AzureCmdException(e.getMessage(), e);
        }
    }

    private static void executePollRequest(@NotNull String managementUrl,
                                           @NotNull String operationPath,
                                           @NotNull HttpsURLConnectionProvider sslConnectionProvider)
            throws IOException, AzureCmdException {
        while (true) {
            HttpsURLConnection sslConnection = sslConnectionProvider.getSSLConnection(managementUrl,
                    operationPath, ContentType.Xml);
            HttpResponse response = getResponse("GET", null, sslConnection);

            int code = response.getCode();

            if (code < 200 || code >= 300) {
                throw new AzureCmdException(String.format("Error status code %s: %s", code, response.getMessage()),
                        response.getContent());
            } else {
                String pollres = response.getContent();

                try {
                    NodeList nl = ((NodeList) XmlHelper.getXMLValue(pollres, "//Status", XPathConstants.NODESET));

                    if (nl.getLength() > 0) {
                        String status = nl.item(0).getTextContent();
                        if (status.equals("Succeeded")) {
                            break;
                        } else if (!status.equals("InProgress")) {
                            throw new AzureCmdException(
                                    String.format("Invalid status reported while polling async request: %s", status),
                                    pollres);
                        }
                    } else {
                        throw new AzureCmdException("No status reported while polling async request.", pollres);
                    }
                } catch (Exception e) {
                    throw new AzureCmdException(e.getMessage(), e);
                }
            }

            // wait for a while otherwise Azure complains with a
            // "too many requests received" error
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new AzureCmdException(e.getMessage(), e);
            }
        }
    }

}