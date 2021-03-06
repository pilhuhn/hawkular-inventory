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
package org.hawkular.inventory.base;

import org.hawkular.inventory.api.Configuration;
import org.hawkular.inventory.api.Interest;
import org.hawkular.inventory.api.Inventory;
import org.hawkular.inventory.api.Tenants;
import org.hawkular.inventory.api.filters.With;
import org.hawkular.inventory.api.model.Tenant;
import org.hawkular.inventory.base.spi.InventoryBackend;
import rx.Observable;

/**
 * An implementation of the {@link Inventory} that converts the API traversals into trees of filters that it then passes
 * for evaluation to a {@link InventoryBackend backend}.
 *
 * <p>This class is meant to be inherited by the implementation that should provide the initialization and cleanup
 * logic.
 *
 * @param <E> the type of the backend-specific class representing entities and relationships.
 *
 * @author Lukas Krejci
 * @since 0.1.0
 */
public abstract class BaseInventory<E> implements Inventory {

    private InventoryBackend<E> backend;
    private Configuration configuration;
    private final ObservableContext observableContext = new ObservableContext();

    @Override
    public final void initialize(Configuration configuration) {
        this.backend = doInitialize(configuration);
        this.configuration = configuration;
    }

    /**
     * This method is called during {@link #initialize(Configuration)} and provides the instance of the backend
     * initialized from the configuration.
     *
     * @param configuration the configuration provided by the user
     * @return a backend implementation that will be used to access the backend store of the inventory
     */
    protected abstract InventoryBackend<E> doInitialize(Configuration configuration);

    @Override
    public final void close() throws Exception {
        if (backend != null) {
            backend.close();
            backend = null;
        }
    }

    @Override
    public Tenants.ReadWrite tenants() {
        return new BaseTenants.ReadWrite<>(new TraversalContext<>(this, Query.empty(),
                Query.path().with(With.type(Tenant.class)).get(), backend, Tenant.class, configuration,
                observableContext));
    }

    /**
     * <b>WARNING</b>: This is not meant for general consumption but primarily for testing purposes. You can render
     * the inventory inconsistent and/or unusable with unwise modifications done directly through the backend.
     *
     * @return the backend this inventory is using for persistence and querying.
     */
    public InventoryBackend<E> getBackend() {
        return backend;
    }

    @Override
    public boolean hasObservers(Interest<?, ?> interest) {
        return observableContext.isObserved(interest);
    }

    @Override
    public <C, V> Observable<C> observable(Interest<C, V> interest) {
        return observableContext.getObservableFor(interest);
    }
}
