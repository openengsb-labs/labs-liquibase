/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.labs.liquibase.extender.internal;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.osgi.framework.Bundle;

import java.io.InputStream;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class LiquibaseMigrationBlueprintTest {

    private static final String DUMMY_PATH = "anyPath";
    private static final URL RESOURCE_NOT_AVAILABLE = null;
    private static final String LB_FILE = "OSGI-INF/liquibase/master.xml";

    private final JUnit4Mockery context = new JUnit4Mockery();
    private final Bundle bundle = context.mock(Bundle.class);

    private final LiquibaseMigrationBlueprint clientBundleBasedResourceAccessor =
            new LiquibaseMigrationBlueprint(bundle);

    @Test
    public void testAlwaysReturnsAStaticStringForMigrationFileNameInsteadOfLoadingAnything() throws Exception {
        assertThat(clientBundleBasedResourceAccessor.loadMigrationBlueprint(), is(LB_FILE));
    }

    @Test
    public void testHasNoMigrationBlueprintIfLiquibaseMasterDoesNotExist() throws Exception {
        context.checking(new Expectations() {
            {
                oneOf(bundle).getEntry(LB_FILE);
                will(returnValue(RESOURCE_NOT_AVAILABLE));
            }
        });

        assertThat(clientBundleBasedResourceAccessor.hasMigrationBlueprint(), is(false));
        context.assertIsSatisfied();
    }

    @Test
    public void testHasMigrationBlueprintIfLiquibaseMasterExist() throws Exception {
        context.checking(new Expectations() {
            {
                oneOf(bundle).getEntry(LB_FILE);
                will(returnValue(new URL("file:///")));
            }
        });

        assertThat(clientBundleBasedResourceAccessor.hasMigrationBlueprint(), is(true));
        context.assertIsSatisfied();
    }

    @Test
    public void testCreatesNullIfSpecifcResourceDoesNotExist() throws Exception {
        context.checking(new Expectations() {
            {
                oneOf(bundle).getEntry(DUMMY_PATH);
                will(returnValue(RESOURCE_NOT_AVAILABLE));
            }
        });

        InputStream stream = clientBundleBasedResourceAccessor.accessMigrationResources().getResourceAsStream(DUMMY_PATH);

        assertThat(stream, is(nullValue()));
        context.assertIsSatisfied();
    }

    @Test
    public void testOpensStreamOnFoundResources() throws Exception {
        final URL resource = new URL("file:///");

        context.checking(new Expectations() {
            {
                oneOf(bundle).getEntry(DUMMY_PATH);
                will(returnValue(resource));
            }
        });

        clientBundleBasedResourceAccessor.accessMigrationResources().getResourceAsStream(DUMMY_PATH);

        context.assertIsSatisfied();
    }

    @Test
    public void testAlwaysResultInNullForResourceSearching() throws Exception {
        assertThat(clientBundleBasedResourceAccessor.accessMigrationResources().getResources(DUMMY_PATH), is(nullValue()));
    }

    @Test
    public void testCreatePrimitiveClassloaderCallingInternalBundleMethodsOnLoadClass() throws Exception {
        context.checking(new Expectations() {
            {
                oneOf(bundle).loadClass(DUMMY_PATH);
            }
        });

        ClassLoader classLoader = clientBundleBasedResourceAccessor.accessMigrationResources().toClassLoader();
        classLoader.loadClass(DUMMY_PATH);

        context.assertIsSatisfied();
    }

}
