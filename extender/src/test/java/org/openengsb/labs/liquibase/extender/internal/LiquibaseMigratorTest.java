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

import java.net.URL;

public class LiquibaseMigratorTest {

    private final JUnit4Mockery context = new JUnit4Mockery();
    private final LiquibaseMinion minion = context.mock(LiquibaseMinion.class);
    private final Bundle bundle = context.mock(Bundle.class);

    private final LiquibaseMigrator migrator = new LiquibaseMigrator(minion);

    @Test
    public void testNeverMigratesIfNoMigrationFileCouldBeFound() throws Exception {
        context.checking(new Expectations() {
            {
                oneOf(bundle).getEntry(with(any(String.class)));
                will(returnValue(null));

                never(minion);
            }
        });

        migrator.migrateDatabaseUsingBlueprintInBundle(bundle);

        context.assertIsSatisfied();
    }

    @Test
    public void testExecutesMigrationExecutedIfBlueprintFileCouldBeFound() throws Exception {
        context.checking(new Expectations() {
            {
                oneOf(bundle).getEntry(with(any(String.class)));
                will(returnValue(new URL("file:///")));

                oneOf(minion).migrate(with(any(LiquibaseMigrationBlueprint.class)));
            }
        });

        migrator.migrateDatabaseUsingBlueprintInBundle(bundle);

        context.assertIsSatisfied();
    }
}
