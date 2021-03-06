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

package org.onap.dcaegen2.collectors.datafile.io;

import java.io.File;
import java.io.IOException;

public class FileWrapper implements IFile {
    private File file;

    @Override
    public void setPath(String path) {
        file = new File(path);
    }

    @Override
    public boolean createNewFile() throws IOException {
        if (file == null) {
            throw new IOException("Path to file not set.");
        }
        return file.createNewFile();
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public boolean delete() {
        return file.delete();
    }
}
