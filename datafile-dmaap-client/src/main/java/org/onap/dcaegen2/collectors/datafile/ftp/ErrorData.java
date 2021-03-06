/*
 * ============LICENSE_START======================================================================
 * Copyright (C) 2018 Nordix Foundation. All rights reserved.
 * ===============================================================================================
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
 * ============LICENSE_END========================================================================
 */

package org.onap.dcaegen2.collectors.datafile.ftp;

import java.util.ArrayList;
import java.util.List;

public class ErrorData {
    private List<String> errorMessages = new ArrayList<>();
    private List<Throwable> errorCauses = new ArrayList<>();

    public void addError(String errorMessage, Throwable errorCause) {
        errorMessages.add(errorMessage);
        errorCauses.add(errorCause);
    }

    @Override
    public String toString() {
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < errorMessages.size(); i++) {
            message.append(errorMessages.get(i));
            if (errorCauses.get(i) != null) {
                message.append(" Cause: ").append(errorCauses.get(i));
            }
            if (i < errorMessages.size() -1) {
                message.append("\n");
            }
        }
        return message.toString();
    }
}
