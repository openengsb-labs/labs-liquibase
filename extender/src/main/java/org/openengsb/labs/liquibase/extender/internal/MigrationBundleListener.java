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

import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.SynchronousBundleListener;

public class MigrationBundleListener implements SynchronousBundleListener {

    private final DatabaseMigrator databaseMigrator;

    public MigrationBundleListener(DatabaseMigrator databaseMigrator) {
        this.databaseMigrator = databaseMigrator;
    }

    @Override
    public void bundleChanged(BundleEvent event) {
        if (BundleEvent.STARTED != event.getType()) {
            return;
        }
        try {
            databaseMigrator.migrateDatabaseUsingBlueprintInBundle(event.getBundle());
        } catch (DatabaseMigrationException e) {
            try {
                event.getBundle().stop();
            } catch (BundleException e1) {
                throw new IllegalArgumentException("Since bundle couldn't be stoped better abort the process.");
            }
        }
    }

}
