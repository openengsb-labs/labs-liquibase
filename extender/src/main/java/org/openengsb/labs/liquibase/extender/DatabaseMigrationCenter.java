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
package org.openengsb.labs.liquibase.extender;

public interface DatabaseMigrationCenter {

    /**
     * Checks all available liquibase files and compare them with the current database. If any differences
     * are found this method returns true. By default this method only checks once if a migration is required
     * (this allows to check every time a specific service is called or a web page should be displayed). Some events
     * require this method to reevaluate anyhow. E.g. if a new bundle is installed/uninstalled.
     */
    boolean isMigrationRequired() throws DatabaseMigrationException;

    /**
     * There are situations when the automatic refresh algorithm in the #isMigrationRequired method isn't enough;
     * one example is if you delete the md5sums in the database itself. In that (or a similar) case simply call this
     * method. The next #isMigrationRequired call will force a reevaluation.
     */
    void forceMigrationRevaluation();

    /**
     * This call executes all liquibase changes not already applied.
     */
    void executeMigration() throws DatabaseMigrationException;

    /**
     * This method can be used to display all entries in all liquibase.xml files AND if they're already applied
     * to the database or not.
     */
    MigrationDescription printMigrationDescription() throws DatabaseMigrationException;

}
