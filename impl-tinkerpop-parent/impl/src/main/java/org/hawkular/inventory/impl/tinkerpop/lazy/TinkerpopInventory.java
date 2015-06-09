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
package org.hawkular.inventory.impl.tinkerpop.lazy;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import org.hawkular.inventory.api.Configuration;
import org.hawkular.inventory.impl.tinkerpop.spi.GraphProvider;
import org.hawkular.inventory.impl.tinkerpop.spi.IndexSpec;
import org.hawkular.inventory.lazy.LazyInventory;
import org.hawkular.inventory.lazy.spi.LazyInventoryBackend;

import java.util.ServiceLoader;

/**
 * @author Lukas Krejci
 * @since 0.0.6
 */
public class TinkerpopInventory extends LazyInventory<Element> {
    @Override
    protected LazyInventoryBackend<Element> doInitialize(Configuration configuration) {
        GraphProvider gp = ServiceLoader.load(GraphProvider.class).iterator().next();

        TransactionalGraph graph = gp.instantiateGraph(configuration);

        gp.ensureIndices(graph,
                IndexSpec.builder()
                        .withElementType(Vertex.class)
                        .withProperty(Constants.Property.__type.name(), String.class)
                        .withProperty(Constants.Property.__eid.name(), String.class).build(),
                IndexSpec.builder()
                        .withElementType(Vertex.class)
                        .withProperty(Constants.Property.__type.name(), String.class).build());

        InventoryContext context = new InventoryContext(this, configuration.getFeedIdStrategy(),
                configuration.getResultFilter(), graph);

        return new TinkerpopBackend(context);
    }
}
