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

import liquibase.resource.ResourceAccessor;
import org.osgi.framework.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

public class LiquibaseMigrationBlueprint {

    private static final String LIQUIBASE_MASTER_XML_DEFAULT_PATH = "OSGI-INF/liquibase/master.xml";

    private Bundle bundle;

    public LiquibaseMigrationBlueprint(Bundle bundle) {
        this.bundle = bundle;
    }

    public boolean hasMigrationBlueprint() {
        return bundle.getEntry(LIQUIBASE_MASTER_XML_DEFAULT_PATH) != null;
    }

    public String loadMigrationBlueprint() {
        return LIQUIBASE_MASTER_XML_DEFAULT_PATH;
    }


    public ResourceAccessor accessMigrationResources() {
        return new ResourceAccessor() {
            @Override
            public InputStream getResourceAsStream(String file) throws IOException {
                URL entry = bundle.getEntry(file);
                if (entry == null) {
                    return null;
                }
                return entry.openStream();
            }

            @Override
            public Enumeration<URL> getResources(String packageName) throws IOException {
                // no other resources are expected to be loaded from this bundle.
                return null;
            }

            @Override
            public ClassLoader toClassLoader() {
                return new ClassLoader() {
                    @Override
                    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                        Class aClass = bundle.loadClass(name);
                        if (resolve) {
                            resolveClass(aClass);
                        }
                        return aClass;
                    }
                };
            }
        };
    }

}
