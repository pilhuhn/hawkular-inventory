/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.inventory.api.model;

import com.google.gson.annotations.Expose;
import org.hawkular.inventory.api.Relationships;
import org.hawkular.inventory.base.spi.CanonicalPath;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

/**
 * Represents a relationship between 2 entities. A relationship has a source and target entities (somewhat obviously),
 * a name, id (multiple relationships of the same name can exist between the same source and target) and also a map of
 * properties.
 *
 * @author Lukas Krejci
 * @author Jirka Kremser
 * @since 1.0
 */
@XmlRootElement
public final class Relationship extends AbstractElement<Relationship.Blueprint, Relationship.Update> {
    @XmlAttribute
    @Expose
    private final String name;

    @Expose
    private final Entity source;

    @Expose
    private final Entity target;

    /** JAXB support */
    @SuppressWarnings("unused")
    private Relationship() {
        this(null, null, null, null, null);
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> visitor, P parameter) {
        return visitor.visitRelationship(this, parameter);
    }

    public Relationship(String id, String name, Entity source, Entity target) {
        this(id, name, source, target, null);
    }

    public Relationship(String id, String name, Entity source, Entity target, Map<String, Object> properties) {
        super(id, properties);
        this.name = name;
        this.source = source;
        this.target = target;
    }

    @Override
    public Updater<Update, Relationship> update() {
        return new Updater<>((u) -> new Relationship(getId(), getName(), getSource(), getTarget(), u.getProperties()));
    }

    public String getName() {
        return name;
    }

    public Entity getSource() {
        return source;
    }

    public Entity getTarget() {
        return target;
    }

    @Override
    public String toString() {
        StringBuilder bld = new StringBuilder(getClass().getSimpleName());
        bld.append("[id='").append(id).append('\'');
        bld.append(", name='").append(name).append('\'');
        bld.append(", source=").append(source);
        bld.append(" --").append(name).append("--> ");
        bld.append(" target=").append(target);
        bld.append(']');
        return bld.toString();
    }

    public static final class Update extends AbstractElement.Update {

        public static Builder builder() {
            return new Builder();
        }

        //JAXB support
        @SuppressWarnings("unused")
        private Update() {
            this(null);
        }

        public Update(Map<String, Object> properties) {
            super(properties);
        }

        @Override
        public <R, P> R accept(ElementUpdateVisitor<R, P> visitor, P parameter) {
            return visitor.visitRelationship(this, parameter);
        }

        public static final class Builder extends AbstractElement.Update.Builder<Update, Builder> {
            @Override
            public Update build() {
                return new Update(properties);
            }
        }
    }

    public static final class Blueprint extends AbstractElement.Blueprint {

        private final String name;
        private final CanonicalPath otherEnd;
        private final Relationships.Direction direction;

        //JAXB support
        private Blueprint() {
            super(null);
            this.name = null;
            this.otherEnd = null;
            this.direction = null;
        }

        public Blueprint(Relationships.Direction direction, String name, CanonicalPath otherEnd,
                Map<String, Object> properties) {
            super(properties);
            this.name = name;
            this.otherEnd = otherEnd;
            this.direction = direction;
        }

        public String getName() {
            return name;
        }

        public Relationships.Direction getDirection() {
            return direction;
        }

        public CanonicalPath getOtherEnd() {
            return otherEnd;
        }

        @Override
        public <R, P> R accept(ElementBlueprintVisitor<R, P> visitor, P parameter) {
            return visitor.visitRelationship(this, parameter);
        }

        public static final class Builder extends AbstractElement.Blueprint.Builder<Blueprint, Builder> {
            private String name;
            private CanonicalPath otherEnd;
            private Relationships.Direction direction = Relationships.Direction.outgoing;

            public Builder withName(String name) {
                this.name = name;
                return this;
            }

            public Builder withOtherEnd(CanonicalPath path) {
                this.otherEnd = path;
                return this;
            }

            public Builder withDirection(Relationships.Direction direction) {
                this.direction = direction;
                return this;
            }

            @Override
            public Blueprint build() {
                return new Blueprint(direction, name, otherEnd, properties);
            }
        }
    }
}
