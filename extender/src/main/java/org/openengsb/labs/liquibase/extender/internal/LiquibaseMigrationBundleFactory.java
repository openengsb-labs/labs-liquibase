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

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import liquibase.Contexts;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.LogFactory;
import liquibase.logging.LogLevel;
import org.openengsb.labs.liquibase.extender.DatabaseMigrationException;
import org.openengsb.labs.liquibase.extender.MigrationDescription;

import static org.openengsb.labs.liquibase.extender.MigrationDescription.aMigrationDescription;

public class LiquibaseMigrationBundleFactory implements DatabaseMigrationBundleFactory {

    private LiquibaseConfigurationReader reader;
    private SqlConnectionFactory connectionFactory;

    public LiquibaseMigrationBundleFactory(LiquibaseConfigurationReader reader, SqlConnectionFactory connectionFactory) {
        this.reader = reader;
        this.connectionFactory = connectionFactory;
    }

    public DatabaseMigrationBundle createMigrationBundle(LiquibaseMigrationBlueprint migrationBlueprint) {
        return new MigrationBundle(migrationBlueprint);
    }

    class MigrationBundle implements DatabaseMigrationBundle<MigrationBundle> {
        private LiquibaseMigrationBlueprint migrationBlueprint;

        MigrationBundle(LiquibaseMigrationBlueprint migrationBlueprint) {
            this.migrationBlueprint = migrationBlueprint;
        }

        @Override
        public boolean isMigrationRequired() throws DatabaseMigrationException {
            return commandExecutor(new DatabaseCommand<Boolean>() {
                @Override
                public Boolean executeCommand(List<ChangeSet> unranChangeSetList, Database database,
                                              DatabaseChangeLog changeLog) throws Exception {
                    for (ChangeSet changeSet : unranChangeSetList) {
                        if (database.getRanChangeSet(changeSet) == null) {
                            return true;
                        }
                    }
                    return false;
                }
            }, migrationBlueprint);
        }

        @Override
        public void executeMigration() throws DatabaseMigrationException {
            commandExecutor(new DatabaseCommand<Void>() {
                @Override
                public Void executeCommand(List<ChangeSet> unranChangeSetList, Database database,
                                           DatabaseChangeLog changeLog) throws Exception {
                    for (ChangeSet changeSet : unranChangeSetList) {
                        if (database.getRanChangeSet(changeSet) == null) {
                            changeSet.execute(changeLog, database);
                            database.markChangeSetExecStatus(changeSet, ChangeSet.ExecType.EXECUTED);
                        }
                    }
                    return null;
                }
            }, migrationBlueprint);
        }

        @Override
        public MigrationDescription describeMigrations() throws DatabaseMigrationException {
            return commandExecutor(new DatabaseCommand<MigrationDescription>() {
                @Override
                public MigrationDescription executeCommand(List<ChangeSet> unranChangeSetList, Database database,
                                                           DatabaseChangeLog changeLog) throws Exception {
                    MigrationDescription migrationDescription = aMigrationDescription();
                    for (ChangeSet changeSet : unranChangeSetList) {
                        if (database.getRanChangeSet(changeSet) == null) {
                            migrationDescription.newElement(
                                    migrationBlueprint.getName(),
                                    changeSet.getId(),
                                    changeSet.getAuthor(),
                                    changeSet.getContexts().getContexts(),
                                    false
                            );
                        } else {
                            migrationDescription.newElement(
                                    migrationBlueprint.getName(),
                                    changeSet.getId(),
                                    changeSet.getAuthor(),
                                    changeSet.getContexts().getContexts(),
                                    true
                            );
                        }
                    }
                    return migrationDescription;
                }
            }, migrationBlueprint);
        }

        @Override
        public int compareTo(MigrationBundle o) {
            return Long.signum(migrationBlueprint.startLevel() - o.migrationBlueprint.startLevel());
        }
    }

    private <Type> Type commandExecutor(DatabaseCommand<Type> command, LiquibaseMigrationBlueprint migrationBlueprint) throws DatabaseMigrationException {
        try {
            LiquibaseConfiguration config = reader.readConfiguration();
            Database database = loadDatabase(config);

            ChangeLogParameters changeLogParameters = new ChangeLogParameters(database);
            applyConfigurationToChangeLogParameters(changeLogParameters, config);

            DatabaseChangeLog changeLog = migrationBlueprint.loadDatabaseChangelogWith(changeLogParameters);

            database.resetInternalState();

            changeLog.validate(database, config.getContexts());
            List<ChangeSet> unranChangeSetList = changeLog.getChangeSets();
            return command.executeCommand(unranChangeSetList, database, changeLog);
        } catch (Exception e) {
            throw new DatabaseMigrationException(e);
        }
    }

    private interface DatabaseCommand<Type> {
        Type executeCommand(List<ChangeSet> unranChangeSetList, Database database, DatabaseChangeLog changeLog) throws Exception;
    }

    private void applyConfigurationToChangeLogParameters(ChangeLogParameters changeLogParameters, LiquibaseConfiguration config) {
        List<Map.Entry<String, Object>> parameters = config.getParameters();
        for (Map.Entry<String, Object> parameter : parameters) {
            changeLogParameters.set(parameter.getKey(), parameter.getValue());
        }
        changeLogParameters.setContexts(new Contexts(config.getSplittedContexts()));
    }

    private Database loadDatabase(LiquibaseConfiguration config) throws DatabaseException, SQLException {
        LogFactory.getInstance().setDefaultLoggingLevel(
                LogLevel.valueOf(config.getLogLvl())
        );
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
                new JdbcConnection(
                        connectionFactory.loadConnection(
                                config.getDataSource()
                        )
                )
        );

        String defaultSchemaName = config.getDefaultSchemaName();
        if (defaultSchemaName != null && defaultSchemaName.length() > 0) {
            database.setDefaultSchemaName(defaultSchemaName);
        }

        return database;
    }

}
