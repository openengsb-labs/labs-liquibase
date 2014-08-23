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

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

public class OsgiSqlConnectionFactory implements SqlConnectionFactory {

    private static final int WAIT_FOR_DATABASE_TIMEOUT = 60000;

    private final BundleContext liquibaseBundleContext;

    public OsgiSqlConnectionFactory(BundleContext liquibaseBundleContext) {
        this.liquibaseBundleContext = liquibaseBundleContext;
    }

    @Override
    public Connection loadConnection(String connectionName) throws SQLException {
        ServiceTracker serviceTracker = initializeServiceTracker(connectionName);
        serviceTracker.open();
        try {
            return ((DataSource) serviceTracker.waitForService(WAIT_FOR_DATABASE_TIMEOUT)).getConnection();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } finally {
            serviceTracker.close();
        }
    }

    private ServiceTracker initializeServiceTracker(String connectionName) {
        if (connectionName == null) {
            return new ServiceTracker(liquibaseBundleContext, DataSource.class.getName(), null);
        }
        try {
            Filter filter = FrameworkUtil.createFilter(String.format("(&(%s=%s)(%s=%s))",
                            Constants.OBJECTCLASS, DataSource.class.getName(),
                            "osgi.jndi.service.name", connectionName)
            );
            return new ServiceTracker(liquibaseBundleContext, filter, null);
        } catch (InvalidSyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
