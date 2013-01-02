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

import org.openengsb.labs.liquibase.extender.DatabaseMigrationCenter;
import org.openengsb.labs.liquibase.extender.DatabaseMigrationException;
import org.openengsb.labs.liquibase.extender.MigrationDescription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.openengsb.labs.liquibase.extender.MigrationDescription.aMigrationDescription;

public class LiquibaseDatabaseMigrationCenter implements DatabaseMigrationCenter, LiquibaseMigrationCenterConfiguration {

    private final LiquibaseConfigurationReader reader;

    private final Map<Long, DatabaseMigrationBundle> migrationBundleMap = new HashMap<>();

    private boolean lastMigrationRequiredResult;
    private boolean isMigrationResultValid;

    public LiquibaseDatabaseMigrationCenter(LiquibaseConfigurationReader reader) {
        this.reader = reader;
    }

    @Override
    public boolean isMigrationRequired() throws DatabaseMigrationException {
        if (isMigrationResultValid) {
            return lastMigrationRequiredResult;
        }
        for (DatabaseMigrationBundle databaseMigrationBundle : getSortedMigrationBundles()) {
            if (databaseMigrationBundle.isMigrationRequired()) {
                lastMigrationRequiredResult = true;
                isMigrationResultValid = true;
                return true;
            }
        }
        lastMigrationRequiredResult = false;
        isMigrationResultValid = true;
        return false;
    }

    @Override
    public void forceMigrationRevaluation() {
        isMigrationResultValid = false;
    }

    @Override
    public void executeMigration() throws DatabaseMigrationException {
        isMigrationResultValid = false;
        for (DatabaseMigrationBundle databaseMigrationBundle : getSortedMigrationBundles()) {
            if (databaseMigrationBundle.isMigrationRequired()) {
                databaseMigrationBundle.executeMigration();
            }
        }
    }

    @Override
    public MigrationDescription printMigrationDescription() throws DatabaseMigrationException {
        MigrationDescription migrationDescription = aMigrationDescription();
        for (DatabaseMigrationBundle databaseMigrationBundle : getSortedMigrationBundles()) {
            migrationDescription.appendElements(databaseMigrationBundle.describeMigrations());
        }
        return migrationDescription;
    }

    @Override
    public void register(Long bundleId, DatabaseMigrationBundle databaseMigrationBundle) throws DatabaseMigrationException {
        isMigrationResultValid = false;
        LiquibaseConfiguration config = reader.readConfiguration();
        if (config.isShouldLiquibaseBeApplied()) {
            databaseMigrationBundle.executeMigration();
        }
        migrationBundleMap.put(bundleId, databaseMigrationBundle);
    }

    @Override
    public void cancelBundleRegistration(Long bundleId) {
        isMigrationResultValid = false;
        migrationBundleMap.remove(bundleId);
    }

    private ArrayList<DatabaseMigrationBundle> getSortedMigrationBundles() {
        ArrayList<DatabaseMigrationBundle> sortedMigrationBundles = new ArrayList(migrationBundleMap.values());
        Collections.sort(sortedMigrationBundles);
        return sortedMigrationBundles;
    }
}
