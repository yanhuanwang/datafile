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

package org.onap.dcaegen2.collectors.datafile.tasks;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.collectors.datafile.configuration.AppConfig;
import org.onap.dcaegen2.collectors.datafile.configuration.FtpesConfig;
import org.onap.dcaegen2.collectors.datafile.ftp.ErrorData;
import org.onap.dcaegen2.collectors.datafile.ftp.FileCollectResult;
import org.onap.dcaegen2.collectors.datafile.ftp.FileServerData;
import org.onap.dcaegen2.collectors.datafile.ftp.FtpsClient;
import org.onap.dcaegen2.collectors.datafile.ftp.ImmutableFileServerData;
import org.onap.dcaegen2.collectors.datafile.ftp.SftpClient;
import org.onap.dcaegen2.collectors.datafile.model.ConsumerDmaapModel;
import org.onap.dcaegen2.collectors.datafile.model.FileData;
import org.onap.dcaegen2.collectors.datafile.model.ImmutableConsumerDmaapModel;
import org.onap.dcaegen2.collectors.datafile.model.ImmutableFileData;

import reactor.test.StepVerifier;

/**
 * @author <a href="mailto:henrik.b.andersson@est.tech">Henrik Andersson</a>
 *
 */
public class XnfCollectorTaskImplTest {
    private static final String PRODUCT_NAME = "NrRadio";
    private static final String VENDOR_NAME = "Ericsson";
    private static final String LAST_EPOCH_MICROSEC = "8745745764578";
    private static final String SOURCE_NAME = "oteNB5309";
    private static final String START_EPOCH_MICROSEC = "8745745764578";
    private static final String TIME_ZONE_OFFSET = "UTC+05:00";
    private static final String PM_MEAS_CHANGE_IDINTIFIER = "PM_MEAS_FILES";
    private static final String FILE_READY_CHANGE_TYPE = "FileReady";
    private static final String FTPES_SCHEME = "ftpes://";
    private static final String SFTP_SCHEME = "sftp://";
    private static final String SERVER_ADDRESS = "192.168.0.101";
    private static final int PORT_22 = 22;
    private static final String PM_FILE_NAME = "A20161224.1030-1045.bin.gz";
    private static final String REMOTE_FILE_LOCATION = "/ftp/rop/" + PM_FILE_NAME;
    private static final String LOCAL_FILE_LOCATION = "target" + File.separator + PM_FILE_NAME;
    private static final String USER = "usr";
    private static final String PWD = "pwd";
    private static final String FTPES_LOCATION =
            FTPES_SCHEME + USER + ":" + PWD + "@" + SERVER_ADDRESS + ":" + PORT_22 + REMOTE_FILE_LOCATION;
    private static final String SFTP_LOCATION = SFTP_SCHEME + SERVER_ADDRESS + ":" + PORT_22 + REMOTE_FILE_LOCATION;
    private static final String GZIP_COMPRESSION = "gzip";
    private static final String MEAS_COLLECT_FILE_FORMAT_TYPE = "org.3GPP.32.435#measCollec";
    private static final String FILE_FORMAT_VERSION = "V10";

    private static final String FTP_KEY_PATH = "ftpKeyPath";
    private static final String FTP_KEY_PASSWORD = "ftpKeyPassword";
    private static final String TRUSTED_CA_PATH = "trustedCAPath";
    private static final String TRUSTED_CA_PASSWORD = "trustedCAPassword";

    private static AppConfig appConfigMock = mock(AppConfig.class);
    private static FtpesConfig ftpesConfigMock = mock(FtpesConfig.class);

    private FtpsClient ftpsClientMock = mock(FtpsClient.class);

    private SftpClient sftpClientMock = mock(SftpClient.class);
    private RetryTimer retryTimerMock = mock(RetryTimer.class);


    @BeforeAll
    public static void setUpConfiguration() {
        when(appConfigMock.getFtpesConfiguration()).thenReturn(ftpesConfigMock);
        when(ftpesConfigMock.keyCert()).thenReturn(FTP_KEY_PATH);
        when(ftpesConfigMock.keyPassword()).thenReturn(FTP_KEY_PASSWORD);
        when(ftpesConfigMock.trustedCA()).thenReturn(TRUSTED_CA_PATH);
        when(ftpesConfigMock.trustedCAPassword()).thenReturn(TRUSTED_CA_PASSWORD);
    }

    @Test
    public void whenFtpesFile_returnCorrectResponse() {
        XnfCollectorTaskImpl collectorUndetTest =
                new XnfCollectorTaskImpl(appConfigMock, ftpsClientMock, sftpClientMock);

        // @formatter:off
        FileData fileData = ImmutableFileData.builder()
                .productName(PRODUCT_NAME)
                .vendorName(VENDOR_NAME)
                .lastEpochMicrosec(LAST_EPOCH_MICROSEC)
                .sourceName(SOURCE_NAME)
                .startEpochMicrosec(START_EPOCH_MICROSEC)
                .timeZoneOffset(TIME_ZONE_OFFSET)
                .changeIdentifier(PM_MEAS_CHANGE_IDINTIFIER)
                .changeType(FILE_READY_CHANGE_TYPE)
                .name(PM_FILE_NAME)
                .location(FTPES_LOCATION)
                .compression(GZIP_COMPRESSION)
                .fileFormatType(MEAS_COLLECT_FILE_FORMAT_TYPE)
                .fileFormatVersion(FILE_FORMAT_VERSION)
                .build();

        FileServerData fileServerData = ImmutableFileServerData.builder()
                .serverAddress(SERVER_ADDRESS)
                .userId(USER)
                .password(PWD)
                .port(PORT_22)
                .build();
        // @formatter:on
        when(ftpsClientMock.collectFile(fileServerData, REMOTE_FILE_LOCATION, LOCAL_FILE_LOCATION))
                .thenReturn(new FileCollectResult());

        // @formatter:off
        ConsumerDmaapModel expectedConsumerDmaapModel = ImmutableConsumerDmaapModel.builder()
                .productName(PRODUCT_NAME)
                .vendorName(VENDOR_NAME)
                .lastEpochMicrosec(LAST_EPOCH_MICROSEC)
                .sourceName(SOURCE_NAME)
                .startEpochMicrosec(START_EPOCH_MICROSEC)
                .timeZoneOffset(TIME_ZONE_OFFSET)
                .name(PM_FILE_NAME)
                .location(FTPES_LOCATION)
                .internalLocation(LOCAL_FILE_LOCATION)
                .compression(GZIP_COMPRESSION)
                .fileFormatType(MEAS_COLLECT_FILE_FORMAT_TYPE)
                .fileFormatVersion(FILE_FORMAT_VERSION)
                .build();
        // @formatter:on

        StepVerifier.create(collectorUndetTest.execute(fileData)).expectNext(expectedConsumerDmaapModel)
                .verifyComplete();

        verify(ftpsClientMock, times(1)).collectFile(fileServerData, REMOTE_FILE_LOCATION, LOCAL_FILE_LOCATION);
        verify(ftpsClientMock).setKeyCertPath(FTP_KEY_PATH);
        verify(ftpsClientMock).setKeyCertPassword(FTP_KEY_PASSWORD);
        verify(ftpsClientMock).setTrustedCAPath(TRUSTED_CA_PATH);
        verify(ftpsClientMock).setTrustedCAPassword(TRUSTED_CA_PASSWORD);
        verifyNoMoreInteractions(ftpsClientMock);
    }

    @Test
    public void whenSftpFile_returnCorrectResponse() {
        XnfCollectorTaskImpl collectorUndetTest =
                new XnfCollectorTaskImpl(appConfigMock, ftpsClientMock, sftpClientMock);
        // @formatter:off
        FileData fileData = ImmutableFileData.builder()
                .productName(PRODUCT_NAME)
                .vendorName(VENDOR_NAME)
                .lastEpochMicrosec(LAST_EPOCH_MICROSEC)
                .sourceName(SOURCE_NAME)
                .startEpochMicrosec(START_EPOCH_MICROSEC)
                .timeZoneOffset(TIME_ZONE_OFFSET)
                .changeIdentifier(PM_MEAS_CHANGE_IDINTIFIER)
                .changeType(FILE_READY_CHANGE_TYPE)
                .name(PM_FILE_NAME)
                .location(SFTP_LOCATION)
                .compression(GZIP_COMPRESSION)
                .fileFormatType(MEAS_COLLECT_FILE_FORMAT_TYPE)
                .fileFormatVersion(FILE_FORMAT_VERSION)
                .build();


        FileServerData fileServerData = ImmutableFileServerData.builder()
                .serverAddress(SERVER_ADDRESS)
                .userId("")
                .password("")
                .port(PORT_22)
                .build();
        // @formatter:on
        when(sftpClientMock.collectFile(fileServerData, REMOTE_FILE_LOCATION, LOCAL_FILE_LOCATION))
                .thenReturn(new FileCollectResult());
        // @formatter:off
        ConsumerDmaapModel expectedConsumerDmaapModel = ImmutableConsumerDmaapModel.builder()
                .productName(PRODUCT_NAME)
                .vendorName(VENDOR_NAME)
                .lastEpochMicrosec(LAST_EPOCH_MICROSEC)
                .sourceName(SOURCE_NAME)
                .startEpochMicrosec(START_EPOCH_MICROSEC)
                .timeZoneOffset(TIME_ZONE_OFFSET)
                .name(PM_FILE_NAME)
                .location(SFTP_LOCATION)
                .internalLocation(LOCAL_FILE_LOCATION)
                .compression(GZIP_COMPRESSION)
                .fileFormatType(MEAS_COLLECT_FILE_FORMAT_TYPE)
                .fileFormatVersion(FILE_FORMAT_VERSION)
                .build();
        // @formatter:on
        StepVerifier.create(collectorUndetTest.execute(fileData)).expectNext(expectedConsumerDmaapModel)
                .verifyComplete();

        verify(sftpClientMock, times(1)).collectFile(fileServerData, REMOTE_FILE_LOCATION, LOCAL_FILE_LOCATION);
        verifyNoMoreInteractions(sftpClientMock);
    }

    @Test
    public void whenFtpesFileAlwaysFail_retryAndReturnEmpty() {
        XnfCollectorTaskImpl collectorUndetTest =
                new XnfCollectorTaskImpl(appConfigMock, ftpsClientMock, sftpClientMock);
        collectorUndetTest.setRetryTimer(retryTimerMock);
        // @formatter:off
        FileData fileData = ImmutableFileData.builder()
                .productName(PRODUCT_NAME)
                .vendorName(VENDOR_NAME)
                .lastEpochMicrosec(LAST_EPOCH_MICROSEC)
                .sourceName(SOURCE_NAME)
                .startEpochMicrosec(START_EPOCH_MICROSEC)
                .timeZoneOffset(TIME_ZONE_OFFSET)
                .changeIdentifier(PM_MEAS_CHANGE_IDINTIFIER)
                .changeType(FILE_READY_CHANGE_TYPE)
                .name(PM_FILE_NAME)
                .location(FTPES_LOCATION)
                .compression(GZIP_COMPRESSION)
                .fileFormatType(MEAS_COLLECT_FILE_FORMAT_TYPE)
                .fileFormatVersion(FILE_FORMAT_VERSION)
                .build();

        FileServerData fileServerData = ImmutableFileServerData.builder()
                .serverAddress(SERVER_ADDRESS)
                .userId(USER)
                .password(PWD)
                .port(PORT_22)
                .build();
        // @formatter:on
        ErrorData errorData = new ErrorData();
        errorData.addError("Unable to collect file.", new Exception());
        when(ftpsClientMock.collectFile(fileServerData, REMOTE_FILE_LOCATION, LOCAL_FILE_LOCATION))
                .thenReturn(new FileCollectResult(errorData));
        doReturn(new FileCollectResult(errorData)).when(ftpsClientMock).retryCollectFile();

        StepVerifier.create(collectorUndetTest.execute(fileData)).expectNextCount(0).verifyComplete();

        verify(ftpsClientMock, times(1)).collectFile(fileServerData, REMOTE_FILE_LOCATION, LOCAL_FILE_LOCATION);
        verify(ftpsClientMock, times(2)).retryCollectFile();
    }

    @Test
    public void whenFtpesFileFailOnce_retryAndReturnCorrectResponse() {
        XnfCollectorTaskImpl collectorUndetTest =
                new XnfCollectorTaskImpl(appConfigMock, ftpsClientMock, sftpClientMock);
        collectorUndetTest.setRetryTimer(retryTimerMock);
        // @formatter:off
        FileData fileData = ImmutableFileData.builder()
                .productName(PRODUCT_NAME)
                .vendorName(VENDOR_NAME)
                .lastEpochMicrosec(LAST_EPOCH_MICROSEC)
                .sourceName(SOURCE_NAME)
                .startEpochMicrosec(START_EPOCH_MICROSEC)
                .timeZoneOffset(TIME_ZONE_OFFSET)
                .changeIdentifier(PM_MEAS_CHANGE_IDINTIFIER)
                .changeType(FILE_READY_CHANGE_TYPE)
                .name(PM_FILE_NAME)
                .location(FTPES_LOCATION)
                .compression(GZIP_COMPRESSION)
                .fileFormatType(MEAS_COLLECT_FILE_FORMAT_TYPE)
                .fileFormatVersion(FILE_FORMAT_VERSION)
                .build();

        FileServerData fileServerData = ImmutableFileServerData.builder()
                .serverAddress(SERVER_ADDRESS)
                .userId(USER)
                .password(PWD)
                .port(PORT_22)
                .build();
        // @formatter:on
        ErrorData errorData = new ErrorData();
        errorData.addError("Unable to collect file.", new Exception());
        when(ftpsClientMock.collectFile(fileServerData, REMOTE_FILE_LOCATION, LOCAL_FILE_LOCATION))
                .thenReturn(new FileCollectResult(errorData));
        doReturn(new FileCollectResult()).when(ftpsClientMock).retryCollectFile();
        // @formatter:off
        ConsumerDmaapModel expectedConsumerDmaapModel = ImmutableConsumerDmaapModel.builder()
                .productName(PRODUCT_NAME)
                .vendorName(VENDOR_NAME)
                .lastEpochMicrosec(LAST_EPOCH_MICROSEC)
                .sourceName(SOURCE_NAME)
                .startEpochMicrosec(START_EPOCH_MICROSEC)
                .timeZoneOffset(TIME_ZONE_OFFSET)
                .name(PM_FILE_NAME)
                .location(FTPES_LOCATION)
                .internalLocation(LOCAL_FILE_LOCATION)
                .compression(GZIP_COMPRESSION)
                .fileFormatType(MEAS_COLLECT_FILE_FORMAT_TYPE)
                .fileFormatVersion(FILE_FORMAT_VERSION)
                .build();
        // @formatter:on
        StepVerifier.create(collectorUndetTest.execute(fileData)).expectNext(expectedConsumerDmaapModel)
                .verifyComplete();


        verify(ftpsClientMock, times(1)).collectFile(fileServerData, REMOTE_FILE_LOCATION, LOCAL_FILE_LOCATION);
        verify(ftpsClientMock, times(1)).retryCollectFile();
    }

    @Test
    public void whenWrongScheme_returnEmpty() {
        XnfCollectorTaskImpl collectorUndetTest =
                new XnfCollectorTaskImpl(appConfigMock, ftpsClientMock, sftpClientMock);
        // @formatter:off
        FileData fileData = ImmutableFileData.builder()
                .productName(PRODUCT_NAME)
                .vendorName(VENDOR_NAME)
                .lastEpochMicrosec(LAST_EPOCH_MICROSEC)
                .sourceName(SOURCE_NAME)
                .startEpochMicrosec(START_EPOCH_MICROSEC)
                .timeZoneOffset(TIME_ZONE_OFFSET)
                .changeIdentifier(PM_MEAS_CHANGE_IDINTIFIER)
                .changeType(FILE_READY_CHANGE_TYPE)
                .name(PM_FILE_NAME)
                .location("http://host.com/file.zip")
                .compression(GZIP_COMPRESSION)
                .fileFormatType(MEAS_COLLECT_FILE_FORMAT_TYPE)
                .fileFormatVersion(FILE_FORMAT_VERSION)
                .build();

        FileServerData fileServerData = ImmutableFileServerData.builder()
                .serverAddress(SERVER_ADDRESS)
                .userId("")
                .password("")
                .port(PORT_22)
                .build();
        // @formatter:on
        when(sftpClientMock.collectFile(fileServerData, REMOTE_FILE_LOCATION, LOCAL_FILE_LOCATION))
                .thenReturn(new FileCollectResult());

        StepVerifier.create(collectorUndetTest.execute(fileData)).expectNextCount(0).verifyComplete();

        verifyNoMoreInteractions(sftpClientMock);
    }
}
