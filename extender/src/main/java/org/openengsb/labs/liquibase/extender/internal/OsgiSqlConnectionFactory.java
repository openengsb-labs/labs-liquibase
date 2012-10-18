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

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static java.lang.String.format;

public class OsgiSqlConnectionFactory implements SqlConnectionFactory {

    public static final String JNDI_SERVICE_NAME = "osgi.jndi.service.name";

    private final BundleContext liquibaseBundleContext;

    public OsgiSqlConnectionFactory(BundleContext liquibaseBundleContext) {
        this.liquibaseBundleContext = liquibaseBundleContext;
    }

    @Override
    public Connection loadConnection(String connectionName) throws SQLException {
        ServiceReference serviceReference = null;
        if (connectionName == null) {
            serviceReference = liquibaseBundleContext.getServiceReference(
                    DataSource.class.getName());
        } else {
            try {
                ServiceReference[] serviceReferences = liquibaseBundleContext.getServiceReferences(
                        DataSource.class.getName(), format("(%s=%s)", JNDI_SERVICE_NAME, connectionName));
                if (serviceReferences == null || serviceReferences.length != 1) {
                    throw new IllegalStateException("More or no service references found");
                }
                serviceReference = serviceReferences[0];
            } catch (InvalidSyntaxException e) {
                throw new IllegalStateException(e);
            }
        }
        if (serviceReference == null) {
            throw new IllegalStateException("No service could be retrieved");
        }
        Connection connection = null;
        try {
            connection = ((DataSource) liquibaseBundleContext.getService(serviceReference)).getConnection();
        } finally {
            liquibaseBundleContext.ungetService(serviceReference);
        }
        return connection;
    }
}
