/*
 *  Copyright 2024 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.elemento.flow.Flow;
import org.jboss.elemento.flow.Task;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.env.Settings;

import elemental2.promise.Promise;

import static java.util.Collections.singleton;

/**
 * Repository for metadata. Contains a first and second-level cache for metadata.
 * <p>
 * Metadata can be obtained synchronously via {@link #get(AddressTemplate)} or asynchronously via
 * {@link #lookup(AddressTemplate, Consumer)} and {@link #lookup(AddressTemplate)}.
 */
@ApplicationScoped
public class MetadataRepository {

    // TODO: Add support for 2nd level cache!

    private static final int FIRST_LEVEL_CACHE_SIZE = 500;
    private static final Logger logger = Logger.getLogger(MetadataRepository.class.getName());

    private final Settings settings;
    private final Dispatcher dispatcher;

    /**
     * Template resolver used by this metadata repository. The template resolver is applied
     * <ol>
     *     <li>to address templates in {@link #get(AddressTemplate)} and {@link #lookup(AddressTemplate)} to check if a metadata is in the cache and</li>
     *     <li>to resource addresses from the rrd-payload in {@link #addMetadata(Metadata)} when adding meta to the cache</li>
     * </ol>
     */
    private final TemplateResolver resolver;

    /**
     * First level cache for metadata. Key is the resolved address template, value is the metadata
     */
    private final LRUCache<String, Metadata> cache;

    /**
     * Contains the mapping between the requested address template and the processed resource addresses from the rrd-payload.
     * Keys and values are resolved using the {@link #resolver} before they're added to the map.
     * <p>
     * This mapping is necessary for address templates like {@code core-service=*} or {@code subsystem=*} which result in
     * multiple rrd-results.
     * <p>
     * For simple address templates like {@code interface=*} or {@code core-service=management} no mapping is necessary.
     */
    private final Map<String, Set<String>> processedAddresses;

    @Inject
    public MetadataRepository(Settings settings,
            Dispatcher dispatcher,
            StatementContext statementContext) {
        this.settings = settings;
        this.dispatcher = dispatcher;
        this.resolver = new MetadataResolver(statementContext);
        this.cache = new LRUCache<>(FIRST_LEVEL_CACHE_SIZE);
        this.processedAddresses = new HashMap<>();

        cache.addRemovalHandler((address, __) -> logger.debug("LRU metadata for %s has been removed", address));
    }

    // ------------------------------------------------------ api

    public Metadata get(AddressTemplate template) {
        String address = resolveTemplate(template);
        Metadata metadata = internalGet(address);
        if (metadata != null) {
            logger.debug("Get metadata for %s → %s from cache", template, address);
            return metadata;
        } else {
            if (processedInCache(address)) {
                logger.debug("Metadata for %s → %s has been processed, but resulted in multiple metadata. " +
                        "Returning an empty metadata", template, address);
            } else {
                logger.error("No metadata found for %s → %s. Returning an empty metadata", template, address);
            }
            return Metadata.undefined();
        }
    }

    /**
     * Performs a lookup for metadata based on the given address template.
     *
     * @param template the address template to perform the lookup for
     * @param callback the consumer to accept the retrieved metadata
     */
    public void lookup(AddressTemplate template, Consumer<Metadata> callback) {
        lookup(template).then(metadata -> {
            callback.accept(metadata);
            return null;
        });
    }

    /**
     * Performs a lookup for metadata based on the given address template.
     *
     * @param template the address template to perform the lookup for
     * @return a Promise representing the lookup result, containing the metadata associated with the address template
     */
    public Promise<Metadata> lookup(AddressTemplate template) {
        String address = resolveTemplate(template);
        Metadata metadata = internalGet(address);
        if (metadata != null) {
            logger.debug("Lookup metadata for %s → %s from cache", template, address);
            return Promise.resolve(metadata);
        } else {
            if (processedInCache(address)) {
                logger.debug("Metadata for %s → %s has been processed, but resulted in multiple metadata. " +
                        "Returning an empty metadata", template, address);
                return Promise.resolve(Metadata.undefined());
            } else {
                logger.debug("Process metadata for %s → %s", template, address);
                return process(template, singleton(address));
            }
        }
    }

    // ------------------------------------------------------ internal

    void addMetadata(Metadata metadata) {
        logger.debug("Add metadata for %s", metadata.resourceAddress());
        cache.put(metadata.address(), metadata);
    }

    void addProcessedAddresses(String address, Set<String> processedAddresses) {
        logger.debug("Add processed addresses %s → %s", address, processedAddresses);
        this.processedAddresses.computeIfAbsent(address, k -> new HashSet<>()).addAll(processedAddresses);
    }

    private boolean processedInCache(String address) {
        if (processedAddresses.containsKey(address)) {
            Set<String> addresses = processedAddresses.get(address);
            for (String a : addresses) {
                if (!inCache(a)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private Promise<Metadata> process(AddressTemplate template, Set<String> addresses) {
        String timer = logger.timeInfo("Metadata processing for " + template.template + " → " + addresses);
        List<Task<ProcessingContext>> tasks = new ArrayList<>();
        tasks.add(new RrdTask(settings, dispatcher));
        tasks.add(new UpdateTask(this));
        return Flow.sequential(new ProcessingContext(template, addresses), tasks)
                .then(context -> Promise.resolve(get(template)))
                .finally_(() -> logger.timeEnd(timer));
    }

    private String resolveTemplate(AddressTemplate template) {
        return resolver.resolve(template).template;
    }

    private boolean inCache(String address) {
        return cache.contains(address);
    }

    private Metadata internalGet(String address) {
        return cache.get(address);
    }
}
