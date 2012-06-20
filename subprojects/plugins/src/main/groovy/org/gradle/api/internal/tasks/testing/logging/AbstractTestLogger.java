/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.api.internal.tasks.testing.logging;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.gradle.api.Nullable;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.logging.TestLogEvent;
import org.gradle.logging.StyledTextOutput;
import org.gradle.logging.StyledTextOutputFactory;

import java.util.List;

public abstract class AbstractTestLogger {
    private final StyledTextOutputFactory textOutputFactory;
    private final LogLevel logLevel;
    private final int displayGranularity;

    protected AbstractTestLogger(StyledTextOutputFactory textOutputFactory, LogLevel logLevel, int displayGranularity) {
        this.textOutputFactory = textOutputFactory;
        this.logLevel = logLevel;
        this.displayGranularity = displayGranularity;
    }

    protected void logEvent(TestDescriptor descriptor, TestLogEvent event) {
        logEvent(descriptor, event, null);
    }

    protected void logEvent(TestDescriptor descriptor, TestLogEvent event, @Nullable String details) {
        List<String> names = Lists.newArrayList();
        TestDescriptor current = descriptor;
        while (current != null) {
            if (isAtomicTestWhoseParentIsNotTheTestClass(current)) {
                // This deals with the fact that in TestNG, there are no class-level events,
                // but we nevertheless want to see the class name. We use "." rather than
                // " > " as a separator to make it clear that the class is not a separate
                // level. This matters when configuring min/max/displayGranularity.
                names.add(current.getClassName() + "." + current.getName());
            } else {
                names.add(current.getName());
            }
            current = current.getParent();
        }

        int effectiveDisplayGranularity = displayGranularity == -1
                ? names.size() - 1 : Math.min(displayGranularity, names.size() - 1);
        List<String> displayedNames = Lists.reverse(names).subList(effectiveDisplayGranularity, names.size());
        String path = Joiner.on(" > ").join(displayedNames) + " ";

        StyledTextOutput output = textOutputFactory.create("TestEventLogger", logLevel);
        output.append(path);
        output.withStyle(getStyle(event)).println(event.toString());
        if (details != null) {
            output.println(details);
        }
    }

    private boolean isAtomicTestWhoseParentIsNotTheTestClass(TestDescriptor current) {
        return !current.isComposite() && current.getClassName() != null
                && !current.getClassName().equals(current.getParent().getName());
    }

    private StyledTextOutput.Style getStyle(TestLogEvent event) {
        switch (event) {
            case PASSED: return StyledTextOutput.Style.Identifier;
            case FAILED: return StyledTextOutput.Style.Failure;
            case SKIPPED: return StyledTextOutput.Style.Info;
            default: return StyledTextOutput.Style.Normal;
        }
    }
}
