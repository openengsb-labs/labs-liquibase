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
import org.jmock.Sequence;
import org.jmock.States;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.openengsb.labs.liquibase.extender.MigrationDescription;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.openengsb.labs.liquibase.extender.MigrationDescription.aMigrationDescription;
import static org.openengsb.labs.liquibase.extender.internal.LiquibaseConfigurationBuilder.aLiquibaseConfiguration;

public class LiquibaseDatabaseMigrationCenterTest {
    private final JUnit4Mockery context = new JUnit4Mockery();
    private final Sequence startLvlSequence = context.sequence("startLvlSequence");
    private final States migration = context.states("migration").startsAs("not_called");

    private final Long aBundle = 1L;
    private final Long aSecondBundle = 2L;
    private final DatabaseMigrationBundle aMigrationBundle = context.mock(DatabaseMigrationBundle.class, "aMigrationBundle");
    private final DatabaseMigrationBundle aSecondMigrationBundle = context.mock(DatabaseMigrationBundle.class, "aSecondMigrationBundel");
    private final LiquibaseConfigurationReader reader = context.mock(LiquibaseConfigurationReader.class);
    private final LiquibaseConfiguration liquibaseConfiguration = aLiquibaseConfiguration().withNoDefaultApply().build();

    @Test
    public void testDoesNotRequireAnyMigrationOnEmptyBundles() throws Exception {
        LiquibaseDatabaseMigrationCenter liquibaseDatabaseMigrationCenter = new LiquibaseDatabaseMigrationCenter(reader);
        assertThat(liquibaseDatabaseMigrationCenter.isMigrationRequired(), is(false));
    }

    @Test
    public void testASingleMigrationBundleRequiringMigrationMakesEverythingRequireMigration() throws Exception {
        context.checking(new Expectations() {
            {
                allowing(aMigrationBundle).compareTo(with(any(DatabaseMigrationBundle.class)));
                will(returnValue(1));

                allowing(aSecondMigrationBundle).compareTo(with(any(DatabaseMigrationBundle.class)));
                will(returnValue(-1));

                oneOf(aSecondMigrationBundle).isMigrationRequired();
                will(returnValue(true));

                never(aMigrationBundle).isMigrationRequired();

                allowing(reader).readConfiguration();
                will(returnValue(liquibaseConfiguration));
            }
        });

        LiquibaseDatabaseMigrationCenter liquibaseDatabaseMigrationCenter = new LiquibaseDatabaseMigrationCenter(reader);
        liquibaseDatabaseMigrationCenter.register(aBundle, aMigrationBundle);
        liquibaseDatabaseMigrationCenter.register(aSecondBundle, aSecondMigrationBundle);
        assertThat(liquibaseDatabaseMigrationCenter.isMigrationRequired(), is(true));

        context.assertIsSatisfied();
    }

    @Test
    public void testRunningMigrationTwiceOnlyExecutesItOnce() throws Exception {
        context.checking(new Expectations() {
            {
                allowing(aMigrationBundle).compareTo(with(any(DatabaseMigrationBundle.class)));
                will(returnValue(1));

                oneOf(aMigrationBundle).isMigrationRequired();
                will(returnValue(true));

                allowing(reader).readConfiguration();
                will(returnValue(liquibaseConfiguration));
            }
        });

        LiquibaseDatabaseMigrationCenter liquibaseDatabaseMigrationCenter = new LiquibaseDatabaseMigrationCenter(reader);
        liquibaseDatabaseMigrationCenter.register(aBundle, aMigrationBundle);
        liquibaseDatabaseMigrationCenter.isMigrationRequired();
        liquibaseDatabaseMigrationCenter.isMigrationRequired();

        context.assertIsSatisfied();
    }

    @Test
    public void testUsingExecuteMigrationInvalidatesIsMigrationRequiredState() throws Exception {
        context.checking(new Expectations() {
            {
                allowing(aMigrationBundle).compareTo(with(any(DatabaseMigrationBundle.class)));
                will(returnValue(1));

                // one is expected for the executeMigration
                exactly(3).of(aMigrationBundle).isMigrationRequired();
                will(returnValue(true));

                allowing(reader).readConfiguration();
                will(returnValue(liquibaseConfiguration));

                allowing(aMigrationBundle).executeMigration();
            }
        });

        LiquibaseDatabaseMigrationCenter liquibaseDatabaseMigrationCenter = new LiquibaseDatabaseMigrationCenter(reader);
        liquibaseDatabaseMigrationCenter.register(aBundle, aMigrationBundle);
        liquibaseDatabaseMigrationCenter.isMigrationRequired();
        liquibaseDatabaseMigrationCenter.executeMigration();
        liquibaseDatabaseMigrationCenter.isMigrationRequired();

        context.assertIsSatisfied();
    }

    @Test
    public void testUsingRegisterInvalidatesIsMigrationRequiredState() throws Exception {
        context.checking(new Expectations() {
            {
                allowing(aMigrationBundle).compareTo(with(any(DatabaseMigrationBundle.class)));
                will(returnValue(1));

                // one is expected for the executeMigration
                exactly(2).of(aMigrationBundle).isMigrationRequired();
                will(returnValue(true));

                allowing(reader).readConfiguration();
                will(returnValue(liquibaseConfiguration));

                allowing(aMigrationBundle).executeMigration();
            }
        });

        LiquibaseDatabaseMigrationCenter liquibaseDatabaseMigrationCenter = new LiquibaseDatabaseMigrationCenter(reader);
        liquibaseDatabaseMigrationCenter.register(aBundle, aMigrationBundle);
        liquibaseDatabaseMigrationCenter.isMigrationRequired();
        liquibaseDatabaseMigrationCenter.register(aBundle, aMigrationBundle);
        liquibaseDatabaseMigrationCenter.isMigrationRequired();

        context.assertIsSatisfied();
    }

    @Test
    public void testUsingCancelInvalidatesIsMigrationRequiredState() throws Exception {
        context.checking(new Expectations() {
            {
                allowing(aMigrationBundle).compareTo(with(any(DatabaseMigrationBundle.class)));
                will(returnValue(1));

                // one is expected for the executeMigration
                exactly(2).of(aMigrationBundle).isMigrationRequired();
                will(returnValue(true));

                allowing(reader).readConfiguration();
                will(returnValue(liquibaseConfiguration));

                allowing(aMigrationBundle).executeMigration();
            }
        });

        LiquibaseDatabaseMigrationCenter liquibaseDatabaseMigrationCenter = new LiquibaseDatabaseMigrationCenter(reader);
        liquibaseDatabaseMigrationCenter.register(aBundle, aMigrationBundle);
        liquibaseDatabaseMigrationCenter.isMigrationRequired();
        liquibaseDatabaseMigrationCenter.cancelBundleRegistration(99L);
        liquibaseDatabaseMigrationCenter.isMigrationRequired();

        context.assertIsSatisfied();
    }

    @Test
    public void testExecutesMigrationOnEveryBundleWhichRequiresIt() throws Exception {
        context.checking(new Expectations() {
            {
                allowing(aMigrationBundle).compareTo(with(any(DatabaseMigrationBundle.class)));
                will(returnValue(1));

                allowing(aSecondMigrationBundle).compareTo(with(any(DatabaseMigrationBundle.class)));
                will(returnValue(-1));

                oneOf(aMigrationBundle).isMigrationRequired();
                will(returnValue(false));

                oneOf(aSecondMigrationBundle).isMigrationRequired();
                will(returnValue(true));

                oneOf(aSecondMigrationBundle).executeMigration();

                allowing(reader).readConfiguration();
                will(returnValue(liquibaseConfiguration));
            }
        });

        LiquibaseDatabaseMigrationCenter liquibaseDatabaseMigrationCenter = new LiquibaseDatabaseMigrationCenter(reader);
        liquibaseDatabaseMigrationCenter.register(aBundle, aMigrationBundle);
        liquibaseDatabaseMigrationCenter.register(aSecondBundle, aSecondMigrationBundle);

        liquibaseDatabaseMigrationCenter.executeMigration();

        context.assertIsSatisfied();
    }

    @Test
    public void testProvidesAggregationOfMigrationDescriptionsInOrderAdded() throws Exception {
        context.checking(new Expectations() {
            {
                allowing(aMigrationBundle).compareTo(with(any(DatabaseMigrationBundle.class)));
                will(returnValue(1));

                allowing(aSecondMigrationBundle).compareTo(with(any(DatabaseMigrationBundle.class)));
                will(returnValue(-1));

                oneOf(aMigrationBundle).describeMigrations();
                will(returnValue(aMigrationDescription()));

                oneOf(aSecondMigrationBundle).describeMigrations();
                will(returnValue(aMigrationDescription()));

                allowing(reader).readConfiguration();
                will(returnValue(liquibaseConfiguration));
            }
        });

        LiquibaseDatabaseMigrationCenter liquibaseDatabaseMigrationCenter = new LiquibaseDatabaseMigrationCenter(reader);
        liquibaseDatabaseMigrationCenter.register(aBundle, aMigrationBundle);
        liquibaseDatabaseMigrationCenter.register(aSecondBundle, aSecondMigrationBundle);

        MigrationDescription migrationDescription = liquibaseDatabaseMigrationCenter.printMigrationDescription();

        assertThat(migrationDescription.getElements().size(), is(0));

        context.assertIsSatisfied();
    }

    @Test
    public void testResetsInternalMigrationCacheOnRequest() throws Exception {
        context.checking(new Expectations() {
            {
                allowing(aMigrationBundle).compareTo(with(any(DatabaseMigrationBundle.class)));
                will(returnValue(1));

                exactly(2).of(aMigrationBundle).isMigrationRequired();
                will(returnValue(true));

                allowing(reader).readConfiguration();
                will(returnValue(liquibaseConfiguration));
            }
        });

        LiquibaseDatabaseMigrationCenter liquibaseDatabaseMigrationCenter = new LiquibaseDatabaseMigrationCenter(reader);
        liquibaseDatabaseMigrationCenter.register(aBundle, aMigrationBundle);
        liquibaseDatabaseMigrationCenter.isMigrationRequired();
        liquibaseDatabaseMigrationCenter.forceMigrationRevaluation();
        liquibaseDatabaseMigrationCenter.isMigrationRequired();

        context.assertIsSatisfied();
    }
}
