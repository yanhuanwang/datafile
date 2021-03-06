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

package org.onap.dcaegen2.collectors.datafile.ssl;

import java.io.InputStream;
import java.security.KeyStore;

public interface IKeyStore {
    public void load(InputStream arg0, char[] arg1) throws KeyStoreLoadException;

    public KeyStore getKeyStore();

    public static class KeyStoreLoadException extends Exception {
        private static final long serialVersionUID = 1L;

        public KeyStoreLoadException(Exception e) {
            super(e);
        }
    }
}
