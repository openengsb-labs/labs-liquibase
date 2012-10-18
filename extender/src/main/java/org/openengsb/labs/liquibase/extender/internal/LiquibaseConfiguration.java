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

import java.util.*;

public class LiquibaseConfiguration {
    public static final Boolean USE_LIQUIBASE_DEFAULT_VALUE = true;
    public static final Boolean SUCCESS_REQUIRED_DEFAULT_VALUE = true;
    public static final String SCHEMA_NAME_DEFAULT_VALUE = "public";
    public static final String CONTEXTS_DEFAULT_VALUE = "";
    public static final String DATASOURCE_DEFAULT_VALUE = null;

    public static final String SUCCESS_REQUIRED_ID = "successRequired";
    public static final String USE_LIQUIBASE_ID = "useLiquibase";
    public static final String SCHEMA_NAME_ID = "defaultSchema";
    public static final String CONTEXTS_ID = "contexts";
    public static final String PARAMTERS_ID = "parameter.";
    public static final String DATASOURCE_ID = "datasource";

    private final boolean successRequired;
    private final boolean shouldLiquibaseBeApplied;
    private final String defaultSchemaName;
    private final String contexts;
    private final String dataSource;

    private final List<Map.Entry<String, Object>> parameters;

    public LiquibaseConfiguration() {
        this.successRequired = SUCCESS_REQUIRED_DEFAULT_VALUE;
        this.shouldLiquibaseBeApplied = USE_LIQUIBASE_DEFAULT_VALUE;
        this.defaultSchemaName = SCHEMA_NAME_DEFAULT_VALUE;
        this.contexts = CONTEXTS_DEFAULT_VALUE;
        this.dataSource = DATASOURCE_DEFAULT_VALUE;
        this.parameters = new ArrayList<Map.Entry<String, Object>>();
    }

    public LiquibaseConfiguration(Dictionary<?, ?> databaseConfiguration) {
        Object successRequiredFlag = databaseConfiguration.get(LiquibaseConfiguration.SUCCESS_REQUIRED_ID);
        if (successRequiredFlag == null) {
            successRequired = LiquibaseConfiguration.SUCCESS_REQUIRED_DEFAULT_VALUE;
        } else {
            successRequired = Boolean.valueOf(successRequiredFlag.toString());
        }
        Object useLiquibaseFlag = databaseConfiguration.get(LiquibaseConfiguration.USE_LIQUIBASE_ID);
        if (useLiquibaseFlag == null) {
            shouldLiquibaseBeApplied = LiquibaseConfiguration.USE_LIQUIBASE_DEFAULT_VALUE;
        } else {
            shouldLiquibaseBeApplied = Boolean.valueOf(useLiquibaseFlag.toString());
        }
        Object schemaName = databaseConfiguration.get(LiquibaseConfiguration.SCHEMA_NAME_ID);
        if (schemaName == null) {
            defaultSchemaName = LiquibaseConfiguration.SCHEMA_NAME_DEFAULT_VALUE;
        } else {
            defaultSchemaName = (String) schemaName;
        }
        Object contexts = databaseConfiguration.get(LiquibaseConfiguration.CONTEXTS_ID);
        if (contexts == null) {
            this.contexts = LiquibaseConfiguration.CONTEXTS_DEFAULT_VALUE;
        } else {
            this.contexts = (String) contexts;
        }
        Object datasource = databaseConfiguration.get(LiquibaseConfiguration.DATASOURCE_ID);
        if (datasource == null) {
            this.dataSource = LiquibaseConfiguration.DATASOURCE_DEFAULT_VALUE;
        } else {
            this.dataSource = (String) datasource;
        }
        ArrayList<Map.Entry<String, Object>> entries = new ArrayList<Map.Entry<String, Object>>();
        Enumeration<?> keys = databaseConfiguration.keys();
        if (keys == null) {
            parameters = entries;
        } else {
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                if (key.startsWith(LiquibaseConfiguration.PARAMTERS_ID)) {
                    entries.add(new PropertyEntry(
                            key.substring(LiquibaseConfiguration.PARAMTERS_ID.length()), databaseConfiguration.get(key)));
                }
            }
            parameters = entries;
        }
    }

    public boolean isSuccessRequired() {
        return successRequired;
    }

    public boolean isShouldLiquibaseBeApplied() {
        return shouldLiquibaseBeApplied;
    }

    public String getDefaultSchemaName() {
        return defaultSchemaName;
    }

    public String getContexts() {
        return contexts;
    }

    public String getDataSource() {
        return dataSource;
    }

    public List<Map.Entry<String, Object>> getParameters() {
        return parameters;
    }

    private class PropertyEntry implements Map.Entry<String, Object> {

        private String key;
        private Object value;

        public PropertyEntry(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Object setValue(Object value) {
            throw new RuntimeException("Object is read only!");
        }
    }
}
