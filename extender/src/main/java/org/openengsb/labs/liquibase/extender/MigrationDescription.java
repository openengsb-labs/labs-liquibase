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
package org.openengsb.labs.liquibase.extender;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MigrationDescription implements Serializable {
    private static final long serialVersionUID = 8690724740966725233L;

    private final List<MigrationDescriptionElement> elements = new ArrayList<MigrationDescriptionElement>();

    public static MigrationDescription aMigrationDescription() {
        return new MigrationDescription();
    }

    private MigrationDescription() {
    }

    public MigrationDescription newElement(String source, String id, String author, Set<String> context,
            boolean beenRun) {
        elements.add(new MigrationDescriptionElement(source, id, author, context, beenRun));
        return this;
    }

    public MigrationDescription appendElements(MigrationDescription toAppend) {
        elements.addAll(toAppend.getElements());
        return this;
    }

    public List<MigrationDescriptionElement> getElements() {
        return elements;
    }

    public class MigrationDescriptionElement implements Serializable {
        private static final long serialVersionUID = -9199145609970299774L;

        private String source;
        private String id;
        private String author;
        private Set<String> context;
        private boolean beenRun;

        private MigrationDescriptionElement(String source, String id, String author, Set<String> context,
                boolean beenRun) {
            this.source = source;
            this.id = id;
            this.author = author;
            this.context = context;
            this.beenRun = beenRun;
        }

        public String getSource() {
            return source;
        }

        public String getId() {
            return id;
        }

        public String getAuthor() {
            return author;
        }

        public Set<String> getContext() {
            return context;
        }

        public boolean isBeenRun() {
            return beenRun;
        }
    }
}
