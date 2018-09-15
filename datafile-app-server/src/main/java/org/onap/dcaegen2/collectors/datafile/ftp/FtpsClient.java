/*
 * ============LICENSE_START======================================================================
 * Copyright (C) 2018 Nordix Foundation. All rights reserved.
 * ===============================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END========================================================================
 */

package org.onap.dcaegen2.collectors.datafile.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.KeyManagerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Gets file from xNF with FTPS protocoll.
 *
 * TODO: Refactor for better test and error handling.
 *
 * @author <a href="mailto:martin.c.yan@est.tech">Martin Yan</a>
 *
 */
@Component
public class FtpsClient { // TODO: Should be final but needs PowerMock or Mockito 2.x to be able to
                          // mock then, so this will be done as an improvement after first version
                          // committed.
    private static final Logger logger = LoggerFactory.getLogger(FtpsClient.class);

    public void collectFile(FileServerData fileServerData, String remoteFile, String localFile) {
        try {
            FTPSClient ftps = new FTPSClient("TLS");

            boolean setUpSuccessful = setUpConnection(fileServerData, ftps);

            if (setUpSuccessful) {
                getFile(remoteFile, localFile, ftps);

                closeDownConnection(ftps);
            }
        } catch (IOException ex) {
            // TODO: Handle properly. Will be done as an improvement after first version committed.
            logger.debug(ex.getMessage());
        }
    }

    private boolean setUpConnection(FileServerData fileServerData, FTPSClient ftps) {
        boolean success = true;
        ftps.setNeedClientAuth(true);

        // keymanager
        String keystorePath = fileServerData.ftpKeyPath();
        String keystorePass = fileServerData.ftpKeyPassword();
        logger.info("keystorePath is: "+ keystorePath);
        KeyManager keyManager = null;
        try {
            keyManager = KeyManagerUtils.createClientKeyManager(new File(keystorePath), keystorePass);
        } catch (GeneralSecurityException | IOException e) {
            logger.debug(e.getMessage());
        }
        ftps.setKeyManager(keyManager);

        // trustmanager
        String trustedCAPath = fileServerData.trustedCAPath();
        String trustedCAPass = fileServerData.trustedCAPassword();
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            FileInputStream fis = new FileInputStream(trustedCAPath);
            ks.load(fis, trustedCAPass.toCharArray());
            fis.close();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("JKS");
            tmf.init(ks);
            ftps.setTrustManager(tmf.getTrustManagers()[0]);

        } catch (Exception e) {
            logger.debug(e.getMessage());
        }

        try {
            ftps.connect(fileServerData.serverAddress(), fileServerData.port());

            if (!ftps.login(fileServerData.userId(), fileServerData.password())) {
                ftps.logout();
                logger.debug("Login Error");
                success = false;
            }

            if (success) {
                int reply = ftps.getReplyCode();
                // FTPReply stores a set of constants for FTP reply codes.
                if (!FTPReply.isPositiveCompletion(reply)) {
                    // TODO: Handle properly. Will be done as an improvement after first version
                    // committed.
                    ftps.disconnect();
                    logger.debug("Connection Error: " + reply);
                    success = false;
                }
                // enter passive mode
                ftps.enterLocalPassiveMode();
                // Set protection buffer size
                ftps.execPBSZ(0);
                // Set data channel protection to private
                ftps.execPROT("P");
            }
        } catch (Exception ex) {
            // TODO: Handle properly. Will be done as an improvement after first version
            // committed.
            logger.debug(ex.getMessage());
            success = false;
        }

        return success;
    }

    private void getFile(String remoteFile, String localFile, FTPSClient ftps)
            throws IOException, FileNotFoundException {
        // get output stream
        OutputStream output;
        File outfile = new File(localFile);
        outfile.createNewFile();

        output = new FileOutputStream(outfile);
        // get the file from the remote system
        ftps.retrieveFile(remoteFile, output);
        // close output stream
        output.close();
        logger.debug("File " + outfile.getName() + " Download Successfull");
    }

    private void closeDownConnection(FTPSClient ftps) throws IOException {
        ftps.logout();
        ftps.disconnect();
    }
}
