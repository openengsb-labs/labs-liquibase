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

import java.util.Dictionary;
import java.util.Hashtable;

public class LiquibaseConfigurationBuilder {

    private final Dictionary<Object, Object> configuration = new Hashtable<>();

    public static LiquibaseConfigurationBuilder aLiquibaseConfiguration() {
        return new LiquibaseConfigurationBuilder();
    }

    private LiquibaseConfigurationBuilder() {
        configuration.put(LiquibaseConfiguration.USE_LIQUIBASE_ID, LiquibaseConfiguration.USE_LIQUIBASE_DEFAULT_VALUE);
        configuration.put(LiquibaseConfiguration.SCHEMA_NAME_ID, LiquibaseConfiguration.SCHEMA_NAME_DEFAULT_VALUE);
        configuration.put(LiquibaseConfiguration.CONTEXTS_ID, LiquibaseConfiguration.CONTEXTS_DEFAULT_VALUE);
        configuration.put(LiquibaseConfiguration.LOG_LVL, LiquibaseConfiguration.LOG_LVL_DEFAULT_VALUE);
    }

    public LiquibaseConfigurationBuilder withNoDefaultApply() {
        configuration.put(LiquibaseConfiguration.USE_LIQUIBASE_ID, false);
        return this;
    }

    public LiquibaseConfiguration build() {
        return new LiquibaseConfiguration(configuration);
    }
}
