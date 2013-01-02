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
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Dictionary;

public class ConfigurationAdminBackedLiquibaseConfigurationReader implements LiquibaseConfigurationReader {
    public static final String SETTINGS_PID = "org.openengsb.labs.liquibase";
    private BundleContext bundleContext;

    public ConfigurationAdminBackedLiquibaseConfigurationReader(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public LiquibaseConfiguration readConfiguration() {
        ServiceReference reference = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
        if (reference == null) {
            return new LiquibaseConfiguration();
        }
        try {
            ConfigurationAdmin configAdmin = (ConfigurationAdmin) bundleContext.getService(reference);
            Configuration configuration = null;
            try {
                configuration = configAdmin.getConfiguration(SETTINGS_PID);
            } catch (IOException e) {
                return new LiquibaseConfiguration();
            }
            if (configuration == null) {
                return new LiquibaseConfiguration();
            }
            Dictionary<?, ?> databaseConfiguration = configuration.getProperties();
            if (databaseConfiguration == null) {
                return new LiquibaseConfiguration();
            }
            return new LiquibaseConfiguration(databaseConfiguration);
        } finally {
            bundleContext.ungetService(reference);
        }
    }

}
