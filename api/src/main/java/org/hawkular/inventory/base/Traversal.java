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

import java.util.function.Function;
import java.util.function.Supplier;

import org.hawkular.inventory.api.EntityNotFoundException;
import org.hawkular.inventory.api.ResultFilter;
import org.hawkular.inventory.api.model.AbstractElement;
import org.hawkular.inventory.api.model.Entity;
import org.hawkular.inventory.api.paging.Page;
import org.hawkular.inventory.api.paging.Pager;
import org.hawkular.inventory.base.spi.InventoryBackend;

/**
 * A base class for all the inventory traversal interfaces. Contains only a minimal set of helper methods and holds the
 * traversal context.
 *
 * @author Lukas Krejci
 * @since 0.1.0
 */
public abstract class Traversal<BE, E extends AbstractElement<?, ?>> {

    protected final TraversalContext<BE, E> context;

    protected Traversal(TraversalContext<BE, E> context) {
        this.context = context;
    }

    /**
     * If the inventory configuration provided a {@link ResultFilter}, this calls it to tell whether provided element
     * is applicable. If the result filter is not provided by the configuration, true will always be returned.
     *
     * @param result the potential result to be checked for applicability in the result set
     * @return true or false (!!!)
     */
    protected boolean isApplicable(AbstractElement<?, ?> result) {
        ResultFilter filter = context.configuration.getResultFilter();
        return filter == null || filter.isApplicable(result);
    }

    /**
     * A helper method to retrieve a single result from the query or throw an exception if the query yields no results.
     *
     * @param query      the query to run
     * @param entityType the expected type of the entity (used only for error reporting)
     * @return the single result
     * @throws EntityNotFoundException if the query doesn't return any results
     */
    protected BE getSingle(Query query, Class<? extends Entity<?, ?>> entityType) {
        Page<BE> results = context.backend.query(query, Pager.single());

        if (results.isEmpty()) {
            throw new EntityNotFoundException(entityType, Query.filters(query));
        }

        return results.get(0);
    }

    /**
     * Runs the payload in transaction. It is the payload's responsibility to commit the transaction at some point
     * during its execution. If the payload throws an exception the transaction is automatically rolled back and
     * the exception rethrown.
     *
     * @param payload the payload to execute in transaction
     * @param <R> the return type
     * @return the return value provided by the payload
     */
    protected <R> R mutating(Function<InventoryBackend.Transaction, R> payload) {
        return inTransaction(false, payload);
    }

    /**
     * A "shortcut" method for executing read-only payloads in transaction. Such payloads don't have to have a reference
     * to the transaction in which they're being executed.
     *
     * @param payload the read-only payload to execute
     * @param <R>     the type of the return value
     * @return the return value provided by the payload
     */
    protected <R> R readOnly(Supplier<R> payload) {
        return inTransaction(true, (t) -> payload.get());
    }

    private <R> R inTransaction(boolean readOnly, Function<InventoryBackend.Transaction, R> payload) {
        InventoryBackend.Transaction t = context.backend.startTransaction(!readOnly);
        try {
            R ret = payload.apply(t);
            if (readOnly) {
                context.backend.commit(t);
            }

            return ret;
        } catch (Throwable e) {
            context.backend.rollback(t);
            throw e;
        }
    }
}
