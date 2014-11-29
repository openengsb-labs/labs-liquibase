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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;

public class MigrationBundleListener extends BundleTracker {

    private final MigrationCenterHead migrationCenterHead;

    public MigrationBundleListener(BundleContext context, MigrationCenterHead migrationCenterHead) {
        super(context, Bundle.STARTING, null);
        this.migrationCenterHead = migrationCenterHead;
    }

    @Override
    public Object addingBundle(Bundle bundle, BundleEvent event) {
        migrationCenterHead.registerBundleForMigration(bundle);
        return super.addingBundle(bundle, event);
    }

    @Override
    public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
        migrationCenterHead.cancelBundleRegistration(bundle);
        super.removedBundle(bundle, event, object);
    }

}
