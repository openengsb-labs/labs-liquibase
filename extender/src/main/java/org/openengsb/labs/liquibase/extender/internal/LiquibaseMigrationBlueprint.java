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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.LiquibaseException;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ResourceAccessor;
import org.osgi.framework.Bundle;

public class LiquibaseMigrationBlueprint {

    // private static final String LIQUIBASE_MASTER_XML_DEFAULT_PATH = "OSGI-INF/liquibase/master.xml";

    private Bundle bundle;

    public LiquibaseMigrationBlueprint(Bundle bundle) {
        this.bundle = bundle;
    }

    public boolean hasMigrationBlueprint() {
        return bundle.getHeaders().get(ManifestParameters.LIQUIBASE_PERSITENCE) != null;
    }

    public DatabaseChangeLog loadDatabaseChangelogWith(ChangeLogParameters changeLogParameters) throws LiquibaseException {
        if (!hasMigrationBlueprint()) {
            throw new IllegalStateException("Couldn't load changelog without liquibase manifest header");
        }
        return ChangeLogParserFactory.getInstance().getParser(
                liquibaseFilePath(), loadResourceAccessorForBlueprint()).parse(
                liquibaseFilePath(), changeLogParameters, loadResourceAccessorForBlueprint());
    }

    private String liquibaseFilePath() {
        return bundle.getHeaders().get(ManifestParameters.LIQUIBASE_PERSITENCE).toString();
    }

    public long startLevel() {
        Object startLvl = bundle.getHeaders().get(ManifestParameters.LIQUIBASE_START_LEVEL);
        if (startLvl == null || startLvl.toString().length() == 0) {
            return bundle.getBundleId();
        }
        return Long.parseLong(startLvl.toString());
    }

    public String getName() {
        return bundle.getSymbolicName();
    }

    private ResourceAccessor loadResourceAccessorForBlueprint() {
        return new ResourceAccessor() {

            @Override
            public Set<InputStream> getResourcesAsStream(String path) throws IOException {
                final URL entry = bundle.getEntry(path);
                if (entry == null) {
                    return null;
                }
                return new HashSet<InputStream>() {{
                    add(entry.openStream());
                }};
            }

            @Override
            public Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
                // no other resources are expected to be loaded from this bundle.
                return new HashSet<>();
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

    private interface ManifestParameters {
        String LIQUIBASE_PERSITENCE = "Liquibase-Persistence";
        String LIQUIBASE_START_LEVEL = "Liquibase-StartLevel";
    }

}
