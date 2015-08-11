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
package com.microsoft.tooling.msservices.helpers;

import com.microsoft.tooling.msservices.components.DefaultLoader;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public class ServiceCodeReferenceHelper {
    private static final String AZURESDK_URL = "http://zumo.blob.core.windows.net/sdk/azuresdk-android-1.1.5.zip";
    private static final String TEMPLATES_URL = "/com/microsoft/tooling/msservices/templates/";
    private static final String NOTIFICATIONHUBS_PATH = "notificationhubs/";
    public static final String STRINGS_XML = "src/main/res/values/strings.xml";

    public ServiceCodeReferenceHelper() {
    }

    public static InputStream getTemplateResource(String libTemplate) {
        return ServiceCodeReferenceHelper.class.getResourceAsStream(TEMPLATES_URL + libTemplate);
    }

    public void addNotificationHubsLibs(Object module)
            throws ParserConfigurationException, TransformerException, SAXException, XPathExpressionException, IOException {
        addReferences(NOTIFICATIONHUBS_PATH, module);
    }

    public void fillMobileServiceResource(String activityName, String appUrl, String appKey, Object module) throws IOException {
        DefaultLoader.getIdeHelper().replaceInFile(module, new ImmutablePair<String, String>(">$APPURL_" + activityName + "<", ">" + appUrl + "<"),
                new ImmutablePair<String, String>(">$APPKEY_" + activityName + "<", ">" + appKey + "<"));
    }

    public void fillNotificationHubResource(String activityName, String senderId, String connStr, String hubName, Object module) {
        DefaultLoader.getIdeHelper().replaceInFile(module, new ImmutablePair<String, String>(">$SENDERID_" + activityName + "<", ">" + senderId + "<"),
                new ImmutablePair<String, String>(">$CONNSTR_" + activityName + "<", ">" + connStr + "<"),
                new ImmutablePair<String, String>(">$HUBNAME_" + activityName + "<", ">" + hubName + "<"));
    }

    public void fillOffice365Resource(String activityName, String appId, String name, String clientId, Object module) {
        DefaultLoader.getIdeHelper().replaceInFile(module, new ImmutablePair<String, String>(">$O365_APP_ID_" + activityName + "<", ">" + appId + "<"),
                new ImmutablePair<String, String>(">$O365_NAME_" + activityName + "<", ">" + name + "<"),
                new ImmutablePair<String, String>(">$O365_CLIENTID_" + activityName + "<", ">" + clientId + "<"));
    }

    private void addReferences(String zipPath, Object module)
            throws IOException, ParserConfigurationException, SAXException, XPathExpressionException, TransformerException {
        //Downloads libraries
        String path = System.getProperty("java.io.tmpdir");

        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }

        path = path + "TempAzure";

        File pathFile = new File(path);

        if (pathFile.exists() || pathFile.mkdirs()) {
            path = path + File.separator + "androidAzureSDK.zip";

            File zipFile = new File(path);

            if (!zipFile.exists()) {
                saveUrl(path, AZURESDK_URL);
            }
            DefaultLoader.getIdeHelper().copyJarFiles2Module(module, zipFile, zipPath);
        }
    }

    private static void saveUrl(String filename, String urlString)
            throws IOException {
        InputStream in = null;
        FileOutputStream fout = null;

        try {
            in = new URL(urlString).openStream();
            fout = new FileOutputStream(filename);

            byte data[] = new byte[1024];
            int count;

            while ((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
            }
        } finally {
            if (in != null) {
                in.close();
            }

            if (fout != null) {
                fout.close();
            }
        }
    }

    public static String getStringAndCloseStream(InputStream is) throws IOException {
        //Using the trick described in this link to read whole streams in one operation:
        //http://stackoverflow.com/a/5445161
        try {
            Scanner s = new Scanner(is).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        } finally {
            is.close();
        }
    }

    @NotNull
    public static String getStringAndCloseStream(@NotNull InputStream is, @NotNull String charsetName) throws IOException {
        //Using the trick described in this link to read whole streams in one operation:
        //http://stackoverflow.com/a/5445161
        try {
            Scanner s = new Scanner(is, charsetName).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        } finally {
            is.close();
        }
    }
}