/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2018 Nordix Foundation. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.dcaegen2.collectors.datafile.ftp;

import org.apache.commons.io.FilenameUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SftpClient {
    private String username;
    private String host;
    private String password;
    private String remoteFile;
    private String localFile;
    private int port;

    public SftpClient(String host, String username, String password, int port, String remoteFile, String localFile) {
        super();
        this.username = username;
        this.host = host;
        this.password = password;
        this.remoteFile = remoteFile;
        this.localFile = localFile;
        this.port = port;
    }

    public void collectFile() {
        JSch jsch = new JSch();
        Session session = null;
        try {
            session = jsch.getSession(username, host, port);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(password);
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            sftpChannel.get(remoteFile, localFile);
            sftpChannel.exit();
            session.disconnect();
            System.out.println("File " + FilenameUtils.getName(localFile) + " Download Successfull");
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "SftpClient [username=" + username + ", host=" + host + ", password=" + password + ", remoteFile="
                + remoteFile + ", localFile=" + localFile + ", port=" + port + "]";
    }
}
