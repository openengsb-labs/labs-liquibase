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
import org.osgi.framework.BundleEvent;

public class MigrationBundleListenerTest {

    private final JUnit4Mockery context = new JUnit4Mockery();
    private final DatabaseMigrator databaseMigrator = context.mock(DatabaseMigrator.class);
    private final Bundle bundle = context.mock(Bundle.class);
    private final MigrationBundleListener migrationBundleListener =
            new MigrationBundleListener(databaseMigrator);

    @Test
    public void testDoesNothingIfEventTypeIsNotEqualStarted() throws Exception {
        context.checking(new Expectations() {
            {
                never(databaseMigrator);
                never(bundle);
            }
        });

        migrationBundleListener.bundleChanged(aBundleEventWithState(BundleEvent.INSTALLED));
        migrationBundleListener.bundleChanged(aBundleEventWithState(BundleEvent.LAZY_ACTIVATION));
        migrationBundleListener.bundleChanged(aBundleEventWithState(BundleEvent.STARTING));
        migrationBundleListener.bundleChanged(aBundleEventWithState(BundleEvent.STOPPING));

        context.assertIsSatisfied();
    }

    @Test
    public void testCallsMigrationOnEveryStartedBundle() throws Exception {
        context.checking(new Expectations() {
            {
                oneOf(databaseMigrator).migrateDatabaseUsingBlueprintInBundle(bundle);

                never(bundle).stop();
            }
        });

        migrationBundleListener.bundleChanged(aBundleEventWithState(BundleEvent.STARTED));

        context.assertIsSatisfied();
    }

    @Test
    public void testStopsABundleIfMigrationFails() throws Exception {
        context.checking(new Expectations() {
            {
                oneOf(databaseMigrator).migrateDatabaseUsingBlueprintInBundle(bundle);
                will(throwException(new DatabaseMigrationException()));

                oneOf(bundle).stop();
            }
        });

        migrationBundleListener.bundleChanged(aBundleEventWithState(BundleEvent.STARTED));

        context.assertIsSatisfied();
    }

    private BundleEvent aBundleEventWithState(Integer state) {
        return new BundleEvent(state, bundle);
    }
}
