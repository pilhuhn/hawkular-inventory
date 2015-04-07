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

package org.hawkular.inventory.impl.tinkerpop;

import org.hawkular.inventory.api.MetricTypes;
import org.hawkular.inventory.api.Metrics;
import org.hawkular.inventory.api.Relationships;
import org.hawkular.inventory.api.model.MetricType;

import java.util.Set;

/**
 * @author Lukas Krejci
 * @since 1.0
 */
final class MetricTypeBrowser extends AbstractBrowser<MetricType> {

    public static MetricTypes.Single single(InventoryContext context, FilterApplicator... path) {
        MetricTypeBrowser b = new MetricTypeBrowser(context, path);

        return new MetricTypes.Single() {

            @Override
            public MetricType entity() {
                return b.entity();
            }

            @Override
            public Relationships.ReadWrite relationships() {
                return b.relationships();
            }

            @Override
            public Relationships.ReadWrite relationships(Relationships.Direction direction) {
                return b.relationships(direction);
            }

            @Override
            public Metrics.Read metrics() {
                return b.metrics();
            }
        };
    }

    public static MetricTypes.Multiple multiple(InventoryContext context, FilterApplicator... path) {
        MetricTypeBrowser b = new MetricTypeBrowser(context, path);

        return new MetricTypes.Multiple() {

            @Override
            public Set<MetricType> entities() {
                return b.entities();
            }

            @Override
            public Relationships.Read relationships() {
                return b.relationships();
            }

            @Override
            public Relationships.Read relationships(Relationships.Direction direction) {
                return b.relationships(direction);
            }

            @Override
            public Metrics.Read metrics() {
                return b.metrics();
            }
        };
    }

    MetricTypeBrowser(InventoryContext context, FilterApplicator... path) {
        super(context, MetricType.class, path);
    }

    public Metrics.Read metrics() {
        return new MetricsService(context, pathToHereWithSelect(null));
    }
}
