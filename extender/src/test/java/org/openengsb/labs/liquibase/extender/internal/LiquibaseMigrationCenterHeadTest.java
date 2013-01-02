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

import java.util.Dictionary;
import java.util.Hashtable;

public class LiquibaseMigrationCenterHeadTest {

    private final JUnit4Mockery context = new JUnit4Mockery();
    private final DatabaseMigrationBundleFactory databaseMigrationBundleFactory =
            context.mock(DatabaseMigrationBundleFactory.class);
    private final LiquibaseMigrationCenterConfiguration liquibaseMigrationCenterConfiguration =
            context.mock(LiquibaseMigrationCenterConfiguration.class);
    private final Bundle aBundle = context.mock(Bundle.class);
    private final DatabaseMigrationBundle databaseMigrationBundle = context.mock(DatabaseMigrationBundle.class);
    private final Dictionary<Object, Object> aBundleHeader = new Hashtable<>();

    private final LiquibaseMigrationCenterHead migrationCenterHeader = new LiquibaseMigrationCenterHead(
            databaseMigrationBundleFactory, liquibaseMigrationCenterConfiguration);

    @Test
    public void testNeverMigratesIfNoMigrationFileCouldBeFound() throws Exception {
        context.checking(new Expectations() {
            {
                oneOf(aBundle).getHeaders();
                will(returnValue(aBundleHeader));

                never(databaseMigrationBundleFactory);
                never(liquibaseMigrationCenterConfiguration);
            }
        });

        migrationCenterHeader.registerBundleForMigration(aBundle);

        context.assertIsSatisfied();
    }

    @Test
    public void testExecutesMigrationExecutedIfBlueprintFileCouldBeFound() throws Exception {
        aBundleHeader.put("Liquibase-Persistence", "any/inner/bundle/path");
        context.checking(new Expectations() {
            {
                oneOf(aBundle).getHeaders();
                will(returnValue(aBundleHeader));

                oneOf(databaseMigrationBundleFactory).createMigrationBundle(with(any(LiquibaseMigrationBlueprint.class)));
                will(returnValue(databaseMigrationBundle));

                oneOf(aBundle).getBundleId();
                will(returnValue(1L));

                oneOf(liquibaseMigrationCenterConfiguration).register(1L, databaseMigrationBundle);
            }
        });

        migrationCenterHeader.registerBundleForMigration(aBundle);

        context.assertIsSatisfied();
    }

    @Test
    public void testUnregistersBundlesOnUnregister() throws Exception {
        context.checking(new Expectations() {
            {
                oneOf(aBundle).getBundleId();
                will(returnValue(1L));

                oneOf(liquibaseMigrationCenterConfiguration).cancelBundleRegistration(1L);
            }
        });

        migrationCenterHeader.cancelBundleRegistration(aBundle);

        context.assertIsSatisfied();
    }
}
