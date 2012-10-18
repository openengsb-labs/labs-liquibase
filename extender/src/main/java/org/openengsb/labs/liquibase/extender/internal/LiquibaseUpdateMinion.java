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

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;

import java.util.List;
import java.util.Map;

public class LiquibaseUpdateMinion implements LiquibaseMinion {

    private LiquibaseConfigurationReader reader;
    private SqlConnectionFactory connectionFactory;

    public LiquibaseUpdateMinion(LiquibaseConfigurationReader reader, SqlConnectionFactory connectionFactory) {
        this.reader = reader;
        this.connectionFactory = connectionFactory;
    }

    public void migrate(LiquibaseMigrationBlueprint liquibaseMigrationBlueprint) throws DatabaseMigrationException {
        LiquibaseConfiguration config = reader.readConfiguration();
        if (!config.isShouldLiquibaseBeApplied()) {
            return;
        }
        try {
            Thread currentThread = Thread.currentThread();
            final ClassLoader contextClassLoader = currentThread.getContextClassLoader();
            ResourceAccessor threadClFO = new ClassLoaderResourceAccessor(contextClassLoader);
            ResourceAccessor clFO = new ClassLoaderResourceAccessor();
            ResourceAccessor fsFO = new FileSystemResourceAccessor();
            Database database = DatabaseFactory.getInstance().
                    findCorrectDatabaseImplementation(new JdbcConnection(
                            connectionFactory.loadConnection(config.getDataSource())));
            database.setDefaultSchemaName(config.getDefaultSchemaName());
            Liquibase liquibase = new Liquibase(
                    liquibaseMigrationBlueprint.loadMigrationBlueprint(),
                    new CompositeResourceAccessor(threadClFO, clFO, fsFO,
                            liquibaseMigrationBlueprint.accessMigrationResources()),
                    database);
            List<Map.Entry<String, Object>> parameters = config.getParameters();
            for (Map.Entry<String, Object> parameter : parameters) {
                liquibase.setChangeLogParameter(parameter.getKey(), parameter.getValue());
            }
            liquibase.update(config.getContexts());
        } catch (Exception e) {
            if (config.isSuccessRequired()) {
                throw new DatabaseMigrationException(e);
            }
        }
    }

}
