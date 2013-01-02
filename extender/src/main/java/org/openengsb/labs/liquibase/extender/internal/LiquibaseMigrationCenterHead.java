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

import org.openengsb.labs.liquibase.extender.DatabaseMigrationException;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiquibaseMigrationCenterHead implements MigrationCenterHead {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiquibaseMigrationCenterHead.class);

    private DatabaseMigrationBundleFactory databaseMigrationBundleFactory;
    private LiquibaseMigrationCenterConfiguration liquibaseMigrationCenterConfiguration;

    public LiquibaseMigrationCenterHead(DatabaseMigrationBundleFactory databaseMigrationBundleFactory,
                                        LiquibaseMigrationCenterConfiguration liquibaseMigrationCenterConfiguration) {
        this.databaseMigrationBundleFactory = databaseMigrationBundleFactory;
        this.liquibaseMigrationCenterConfiguration = liquibaseMigrationCenterConfiguration;
    }

    @Override
    public void registerBundleForMigration(Bundle bundle) {
        LiquibaseMigrationBlueprint liquibaseMigrationBlueprint = new LiquibaseMigrationBlueprint(bundle);
        if (!liquibaseMigrationBlueprint.hasMigrationBlueprint()) {
            return;
        }

        DatabaseMigrationBundle migrationBundle =
                databaseMigrationBundleFactory.createMigrationBundle(liquibaseMigrationBlueprint);

        try {
            liquibaseMigrationCenterConfiguration.register(bundle.getBundleId(), migrationBundle);
        } catch (DatabaseMigrationException e) {
            LOGGER.error("Liquibase auto migration failed", e);
        }
    }

    @Override
    public void cancelBundleRegistration(Bundle bundle) {
        liquibaseMigrationCenterConfiguration.cancelBundleRegistration(bundle.getBundleId());
    }

}
