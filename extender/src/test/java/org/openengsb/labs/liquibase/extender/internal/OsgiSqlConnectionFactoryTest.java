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

import de.kalpatec.pojosr.framework.launch.PojoServiceRegistry;
import de.kalpatec.pojosr.framework.launch.PojoServiceRegistryFactory;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.ServiceLoader;

public class OsgiSqlConnectionFactoryTest {

    private static final String NO_JNDI_CONNECTION_NAME_SPECIFIED = null;
    private static final String JNDI_CONNECTION_NAME = "connection";
    private static final String NOT_REGISTERED_CONNECTION_NAME = "not_registered";

    private final JUnit4Mockery context = new JUnit4Mockery();
    private final DataSource unnamedDataSource = context.mock(DataSource.class, "unnamedDataSource");
    private final DataSource namedDataSource = context.mock(DataSource.class, "namedDataSource");


    @Test
    public void testRetrievesTheFirstAvailableDataSourceIfNoNameIsSpecified() throws Exception {
        context.checking(new Expectations() {
            {
                oneOf(unnamedDataSource).getConnection();

                never(namedDataSource);
            }
        });

        PojoServiceRegistry registry = startPojoSR();

        registerUnamedDataSource(registry);
        registerNamedDatasource(registry);

        OsgiSqlConnectionFactory osgiSqlConnectionFactory = new OsgiSqlConnectionFactory(registry.getBundleContext());
        osgiSqlConnectionFactory.loadConnection(NO_JNDI_CONNECTION_NAME_SPECIFIED);

        context.assertIsSatisfied();
    }

    @Test
    public void testRetrievesASpecificNamedInstanceIfNameIsSpecified() throws Exception {
        context.checking(new Expectations() {
            {
                oneOf(namedDataSource).getConnection();

                never(unnamedDataSource);
            }
        });

        PojoServiceRegistry registry = startPojoSR();

        registerUnamedDataSource(registry);
        registerNamedDatasource(registry);

        OsgiSqlConnectionFactory osgiSqlConnectionFactory = new OsgiSqlConnectionFactory(registry.getBundleContext());
        osgiSqlConnectionFactory.loadConnection(JNDI_CONNECTION_NAME);

        context.assertIsSatisfied();
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowsAnErrorIfNameIsProvidedButInstanceCouldntBeFound() throws Exception {
        PojoServiceRegistry registry = startPojoSR();

        registerUnamedDataSource(registry);
        registerNamedDatasource(registry);

        OsgiSqlConnectionFactory osgiSqlConnectionFactory = new OsgiSqlConnectionFactory(registry.getBundleContext());
        osgiSqlConnectionFactory.loadConnection(NOT_REGISTERED_CONNECTION_NAME);
    }

    private void registerNamedDatasource(PojoServiceRegistry registry) {
        Hashtable hashtable = new Hashtable();
        hashtable.put(OsgiSqlConnectionFactory.JNDI_SERVICE_NAME, JNDI_CONNECTION_NAME);
        registry.registerService(DataSource.class.getName(), namedDataSource, hashtable);
    }

    private void registerUnamedDataSource(PojoServiceRegistry registry) {
        registry.registerService(DataSource.class.getName(), unnamedDataSource, new Hashtable());
    }

    private PojoServiceRegistry startPojoSR() throws Exception {
        ServiceLoader<PojoServiceRegistryFactory> loader = ServiceLoader.load(PojoServiceRegistryFactory.class);
        return loader.iterator().next().newPojoServiceRegistry(new HashMap());
    }
}
