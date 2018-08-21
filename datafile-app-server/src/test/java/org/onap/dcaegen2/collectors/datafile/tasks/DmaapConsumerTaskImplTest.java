/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.dcaegen2.collectors.datafile.tasks;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.onap.dcaegen2.collectors.datafile.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.collectors.datafile.configuration.AppConfig;
import org.onap.dcaegen2.collectors.datafile.config.ImmutableDmaapConsumerConfiguration;
import org.onap.dcaegen2.collectors.datafile.exceptions.DatafileTaskException;
import org.onap.dcaegen2.collectors.datafile.model.ConsumerDmaapModel;
import org.onap.dcaegen2.collectors.datafile.service.DmaapConsumerJsonParser;
import org.onap.dcaegen2.collectors.datafile.service.consumer.ExtendedDmaapConsumerHttpClientImpl;
import org.onap.dcaegen2.collectors.datafile.tasks.DmaapConsumerTaskImpl;
import org.onap.dcaegen2.collectors.datafile.model.ImmutableConsumerDmaapModel;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * @author <a href="mailto:henrik.b.andersson@est.tech">Henrik Andersson</a> on 5/17/18
 */
class DmaapConsumerTaskImplTest {

    private static ArrayList<ConsumerDmaapModel> listOfConsumerDmaapModel=new ArrayList<ConsumerDmaapModel>();
    private static DmaapConsumerTaskImpl dmaapConsumerTask;
    private static ExtendedDmaapConsumerHttpClientImpl extendedDmaapConsumerHttpClient;
    private static AppConfig appConfig;
    private static DmaapConsumerConfiguration dmaapConsumerConfiguration;
    private static String message;
    private static String parsed;

    @BeforeAll
    public static void setUp() {
        dmaapConsumerConfiguration = new ImmutableDmaapConsumerConfiguration.Builder().consumerGroup("OpenDCAE-c12")
                .consumerId("c12").dmaapContentType("application/json").dmaapHostName("54.45.33.2")
                .dmaapPortNumber(1234).dmaapProtocol("https").dmaapUserName("DFC").dmaapUserPassword("DFC")
                .dmaapTopicName("unauthenticated.SEC_OTHER_OUTPUT").timeoutMS(-1).messageLimit(-1).build();
        
        // In order to make this test pass, we need to setup a ftps server and a sftp server
        listOfConsumerDmaapModel.add(ImmutableConsumerDmaapModel.builder().location("target/fileFromFtps.txt").compression("gzip")
                .fileFormatType("org.3GPP.32.435#measCollec").fileFormatVersion("V10").build());
        listOfConsumerDmaapModel.add(ImmutableConsumerDmaapModel.builder().location("target/fileFromSftp.txt").compression("gzip")
                .fileFormatType("org.3GPP.32.435#measCollec").fileFormatVersion("V10").build());
        appConfig = mock(AppConfig.class);

        message =
                "[{\"event\":{\"notificationFields\": {\"changeIdentifier\": \"PM_MEAS_FILES\",\"changeType\": \"FileReady\",\"notificationFieldsVersion\": 1.0,\"arrayOfAdditionalFields\": [{\"location\": \"ftpes://myuser:mypass@localhost:21/fileFromFtps.txt\",\"compression\": \"gzip\",\"fileFormatType\": \"org.3GPP.32.435#measCollec\",\"fileFormatVersion\": \"V10\"},{\"location\": \"sftp://foo:pass@localhost:22/fileFromSftp.txt\",\"compression\": \"gzip\",\"fileFormatType\": \"org.3GPP.32.435#measCollec\",\"fileFormatVersion\": \"V10\"}]}}}]";
        parsed = "{\"event\":{\"notificationFields\": {\"changeIdentifier\": \"PM_MEAS_FILES\",\"changeType\": \"FileReady\",\"notificationFieldsVersion\": 1.0,\"arrayOfAdditionalFields\": [{\"location\": \"ftpes://myuser:mypass@localhost:21/fileFromFtps.txt\",\"compression\": \"gzip\",\"fileFormatType\": \"org.3GPP.32.435#measCollec\",\"fileFormatVersion\": \"V10\"},{\"location\": \"sftp://foo:pass@localhost:22/fileFromSftp.txt\",\"compression\": \"gzip\",\"fileFormatType\": \"org.3GPP.32.435#measCollec\",\"fileFormatVersion\": \"V10\"}]}}}";

    }

    @Test
    public void whenPassedObjectDoesntFit_DoesNotThrowDatafileTaskException() {
        // given
        prepareMocksForDmaapConsumer(Optional.empty());

        // when
        Executable executableFunction = () -> dmaapConsumerTask.execute("Sample input");

        // then
        Assertions.assertThrows(DatafileTaskException.class, executableFunction,
                "Throwing exception when http response code won't fit to assignment range");
        verify(extendedDmaapConsumerHttpClient, times(1)).getHttpConsumerResponse();
        verifyNoMoreInteractions(extendedDmaapConsumerHttpClient);
    }

//    @Test
//    public void whenPassedObjectFits_ReturnsCorrectResponse() throws DatafileTaskException {
//        // given
//        prepareMocksForDmaapConsumer(Optional.of(message));
//        // when
//        ArrayList<ConsumerDmaapModel> arrayOfResponse = dmaapConsumerTask.execute("Sample input");
//        // then
//        verify(extendedDmaapConsumerHttpClient, times(1)).getHttpConsumerResponse();
//        verifyNoMoreInteractions(extendedDmaapConsumerHttpClient);
//        Assertions.assertEquals(listOfConsumerDmaapModel, arrayOfResponse);
//
//    }

    private void prepareMocksForDmaapConsumer(Optional<String> message) {
        DmaapConsumerJsonParser dmaapConsumerJsonParser = spy(new DmaapConsumerJsonParser());
        JsonElement jsonElement = new JsonParser().parse(parsed);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject())).when(dmaapConsumerJsonParser)
                .getJsonObjectFromAnArray(jsonElement);
        extendedDmaapConsumerHttpClient = mock(ExtendedDmaapConsumerHttpClientImpl.class);
        when(extendedDmaapConsumerHttpClient.getHttpConsumerResponse()).thenReturn(message);
        when(appConfig.getDmaapConsumerConfiguration()).thenReturn(dmaapConsumerConfiguration);
        dmaapConsumerTask = spy(new DmaapConsumerTaskImpl(appConfig, dmaapConsumerJsonParser));
        when(dmaapConsumerTask.resolveConfiguration()).thenReturn(dmaapConsumerConfiguration);
        doReturn(extendedDmaapConsumerHttpClient).when(dmaapConsumerTask).resolveClient();
    }
}
