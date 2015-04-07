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
package org.hawkular.inventory.api.observable;

import org.hawkular.inventory.api.EntityNotFoundException;
import org.hawkular.inventory.api.RelationNotFoundException;
import org.hawkular.inventory.api.Relationships;
import org.hawkular.inventory.api.ResolvableToSingle;
import org.hawkular.inventory.api.filters.RelationFilter;
import org.hawkular.inventory.api.model.Entity;
import org.hawkular.inventory.api.model.Relationship;

/**
 * @author Lukas Krejci
 * @since 0.0.1
 */
public final class ObservableRelationships {

    public static final class ReadWrite extends ObservableBase<Relationships.ReadWrite>
            implements Relationships.ReadWrite {

        ReadWrite(Relationships.ReadWrite wrapped, ObservableContext context) {
            super(wrapped, context);
        }

        @Override
        public Relationships.Multiple named(String name) {
            return wrap(ObservableRelationships.Multiple::new, wrapped.named(name));
        }

        @Override
        public Relationships.Multiple named(Relationships.WellKnown name) {
            return wrap(ObservableRelationships.Multiple::new, wrapped.named(name));
        }

        @Override
        public Relationships.Single get(String id) throws EntityNotFoundException, RelationNotFoundException {
            return wrap(ObservableRelationships.Single::new, wrapped.get(id));
        }

        @Override
        public Relationships.Multiple getAll(RelationFilter... filters) {
            return wrap(ObservableRelationships.Multiple::new, wrapped.getAll(filters));
        }

        @Override
        public Relationships.Single linkWith(String name, Entity targetOrSource) throws IllegalArgumentException {
            return wrapAndNotify(ObservableRelationships.Single::new, wrapped.linkWith(name, targetOrSource),
                    ResolvableToSingle::entity, Action.created());
        }

        @Override
        public Relationships.Single linkWith(Relationships.WellKnown name, Entity targetOrSource)
                throws IllegalArgumentException {
            return linkWith(name.name(), targetOrSource);
        }

        @Override
        public void update(Relationship relationship) throws RelationNotFoundException {
            wrapped.update(relationship);
            notify(relationship, relationship, Action.updated());
        }

        @Override
        public void delete(String id) throws RelationNotFoundException {
            Relationship r = get(id).entity();
            wrapped.delete(id);
            notify(r, Action.deleted());
        }
    }

    public static final class Read extends ObservableBase<Relationships.Read>
            implements Relationships.Read {

        Read(Relationships.Read wrapped, ObservableContext context) {
            super(wrapped, context);
        }

        @Override
        public Relationships.Multiple named(String name) {
            return wrap(ObservableRelationships.Multiple::new, wrapped.named(name));
        }

        @Override
        public Relationships.Multiple named(Relationships.WellKnown name) {
            return wrap(ObservableRelationships.Multiple::new, wrapped.named(name));
        }

        @Override
        public Relationships.Single get(String id) throws EntityNotFoundException, RelationNotFoundException {
            return wrap(ObservableRelationships.Single::new, wrapped.get(id));
        }

        @Override
        public Relationships.Multiple getAll(RelationFilter... filters) {
            return wrap(ObservableRelationships.Multiple::new, wrapped.getAll(filters));
        }
    }

    public static final class Single extends ObservableBase.SingleBase<Relationship, Relationships.Single>
            implements Relationships.Single {

        Single(Relationships.Single wrapped, ObservableContext context) {
            super(wrapped, context);
        }
    }

    public static final class Multiple extends ObservableBase.MultipleBase<Relationship, Relationships.Multiple>
            implements Relationships.Multiple {

        Multiple(Relationships.Multiple wrapped, ObservableContext context) {
            super(wrapped, context);
        }

        @Override
        public ObservableTenants.Read tenants() {
            return wrap(ObservableTenants.Read::new, wrapped.tenants());
        }

        @Override
        public ObservableEnvironments.Read environments() {
            return wrap(ObservableEnvironments.Read::new, wrapped.environments());
        }

        @Override
        public ObservableFeeds.Read feeds() {
            return wrap(ObservableFeeds.Read::new, wrapped.feeds());
        }

        @Override
        public ObservableMetricTypes.Read metricTypes() {
            return wrap(ObservableMetricTypes.Read::new, wrapped.metricTypes());
        }

        @Override
        public ObservableMetrics.Read metrics() {
            return wrap(ObservableMetrics.Read::new, wrapped.metrics());
        }

        @Override
        public ObservableResources.Read resources() {
            return wrap(ObservableResources.Read::new, wrapped.resources());
        }

        @Override
        public ObservableResourceTypes.Read resourceTypes() {
            return wrap(ObservableResourceTypes.Read::new, wrapped.resourceTypes());
        }
    }
}
