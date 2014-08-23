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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;

public class MigrationBundleListener extends BundleTracker {

    private final MigrationCenterHead migrationCenterHead;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public MigrationBundleListener(BundleContext context, MigrationCenterHead migrationCenterHead) {
        super(context, Bundle.ACTIVE, null);
        this.migrationCenterHead = migrationCenterHead;
    }

    @Override
    public Object addingBundle(final Bundle bundle, BundleEvent event) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                migrationCenterHead.registerBundleForMigration(bundle);
            }
        });
        return super.addingBundle(bundle, event);
    }

    @Override
    public void removedBundle(final Bundle bundle, BundleEvent event, Object object) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                migrationCenterHead.cancelBundleRegistration(bundle);
            }
        });
        super.removedBundle(bundle, event, object);
    }
}
