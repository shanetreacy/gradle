/*
 * Copyright 2010 the original author or authors.
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



package org.gradle.groovy.scripts

import org.codehaus.groovy.control.CompilerConfiguration
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.internal.file.FileLookup
import org.gradle.api.internal.file.collections.DirectoryFileTreeFactory
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.logging.LoggingManager
import org.gradle.api.provider.ProviderFactory
import org.gradle.internal.hash.FileHasher
import org.gradle.internal.hash.StreamHasher
import org.gradle.internal.logging.StandardOutputCapture
import org.gradle.internal.reflect.Instantiator
import org.gradle.internal.resource.TextResourceLoader
import org.gradle.internal.service.ServiceRegistry
import org.gradle.process.internal.ExecFactory
import org.gradle.test.fixtures.file.TestNameTestDirectoryProvider
import org.gradle.util.JUnit4GroovyMockery
import org.gradle.util.TestUtil
import org.jmock.integration.junit4.JMock
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import static org.junit.Assert.assertEquals

@RunWith(JMock)
class DefaultScriptTest {
    private final JUnit4GroovyMockery context = new JUnit4GroovyMockery()
    @Rule
    public final TestNameTestDirectoryProvider temporaryFolder = TestNameTestDirectoryProvider.newInstance()

    @Test public void testApplyMetaData() {
        ServiceRegistry serviceRegistryMock = context.mock(ServiceRegistry.class)
        context.checking {
            allowing(serviceRegistryMock).get(ScriptHandler.class)
            will(returnValue(context.mock(ScriptHandler.class)))
            allowing(serviceRegistryMock).get(StandardOutputCapture.class)
            will(returnValue(context.mock(StandardOutputCapture.class)))
            allowing(serviceRegistryMock).get(LoggingManager.class)
            will(returnValue(context.mock(LoggingManager.class)))
            allowing(serviceRegistryMock).get(Instantiator)
            will(returnValue(context.mock(Instantiator)))
            allowing(serviceRegistryMock).get(FileLookup)
            will(returnValue(context.mock(FileLookup)))
            allowing(serviceRegistryMock).get(DirectoryFileTreeFactory)
            will(returnValue(context.mock(DirectoryFileTreeFactory)))
            allowing(serviceRegistryMock).get(ProviderFactory)
            will(returnValue(context.mock(ProviderFactory)))
            allowing(serviceRegistryMock).get(StreamHasher)
            will(returnValue(context.mock(StreamHasher)))
            allowing(serviceRegistryMock).get(FileHasher)
            will(returnValue(context.mock(FileHasher)))
            allowing(serviceRegistryMock).get(ExecFactory)
            will(returnValue(context.mock(ExecFactory)))
            allowing(serviceRegistryMock).get(TextResourceLoader)
            will(returnValue(context.mock(TextResourceLoader)))
        }

        DefaultScript script = new GroovyShell(createBaseCompilerConfiguration()).parse(testScriptText)
        ProjectInternal testProject = TestUtil.create(temporaryFolder).rootProject()
        testProject.ext.custom = 'true'
        script.setScriptSource(new StringScriptSource('script', '//'))
        script.init(testProject, serviceRegistryMock)
        script.run();
        assertEquals("scriptMethod", script.scriptMethod())
        assertEquals("a", script.newProperty)
    }

    private CompilerConfiguration createBaseCompilerConfiguration() {
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.scriptBaseClass = DefaultScript.class.name
        configuration
    }

    private String getTestScriptText() {
        '''
// We leave out the path to check import adding
getName() // call a project method
assert hasProperty('custom')
repositories { }
def scriptMethod() { 'scriptMethod' }
String internalProp = 'a'
assert internalProp == 'a'
ext.newProperty = 'a'
assert newProperty == 'a'
assert newProperty == project.newProperty
'''
    }
}
