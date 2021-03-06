/*
 * Copyright 2013 the original author or authors.
 *
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
 */

package org.gradle.cache.internal.locklistener;

import org.gradle.api.Action;
import org.gradle.cache.FileLockReleasedSignal;

public class NoOpFileLockContentionHandler implements FileLockContentionHandler {

    public void start(long lockId, Action<FileLockReleasedSignal> whenContended) {}

    public void stop(long lockId) {}

    public int reservePort() {
        return -1;
    }

    public boolean maybePingOwner(int port, long lockId, String displayName, long timeElapsed, FileLockReleasedSignal signal) {
        return false;
    }
}
