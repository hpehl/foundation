package org.jboss.hal.meta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.ResourceAddress;

/**
 * Repository for metadata. Contains a first (based on {@link LRUCache}) and second (based on PouchDB) level cache and a mapping
 * of processed address templates.
 */
@ApplicationScoped
public class MetadataRepository {

    private static final int CAPACITY = 500;
    private static final Logger logger = Logger.getLogger(MetadataRepository.class.getName());

    /**
     * The template resolver is used to
     * <ol>
     *     <li>check if a metadata is in the cache and</li>
     *     <li>to add a resource address from the rrd-payload to the cache</li>
     * </ol>
     */
    private final TemplateResolver resolver;

    /**
     * First level cache for metadata. Key is the resolved address template, value is the metadata
     */
    private final LRUCache<String, Metadata> cache;

    /**
     * Contains the mapping between the requested address template and the resource addresses from the rrd-payload. Both
     * addresses are resolved using {@link #resolver} before they're added to the map.
     * <p>
     * This mapping is necessary for address templates like {@code core-service=*} or {@code subsystem=*} which result in
     * multiple rrd-results.
     * <p>
     * For simple address templates like {@code interface=*} or {@code core-service=management} no mapping is necessary.
     */
    private final Map<String, List<String>> processedAddresses;

    @Inject
    public MetadataRepository(StatementContext statementContext) {
        this.resolver = new MetadataResolver(statementContext);
        this.cache = new LRUCache<>(CAPACITY);
        this.cache.addRemovalHandler((address, __) -> {
            logger.debug("Remove metadata %s from cache", address);
            // TODO Adjust processedTemplates
            //  Add to PouchDB 2nd level cache, if not already there
        });
        this.processedAddresses = new HashMap<>();
    }

    // ------------------------------------------------------ api

    public boolean contains(AddressTemplate template) {
        String address = resolveTemplate(template);
        if (inCache(address)) {
            return true;
        } else {
            return processedInCache(address);
        }
    }

    public Metadata get(AddressTemplate template) {
        String address = resolveTemplate(template);
        Metadata metadata = cache.get(address);
        if (metadata != null) {
            logger.debug("Get metadata for %s as %s from cache", template, address);
            return metadata;
        } else {
            logger.warn("No metadata found for %s as %s. Returning empty metadata", template, address);
            return Metadata.empty();
        }
    }

    public boolean add(ResourceAddress resourceAddress, Metadata metadata, boolean recursive) {
        String address = resolveTemplate(AddressTemplate.of(resourceAddress.toString()));
        if (!inCache(address)) {
            logger.debug("Add metadata for %s as %s (%s)",
                    resourceAddress, address, recursive ? "recursive" : "non-recursive");
            // entry.get(HAL_RECURSIVE).set(recursive);
            cache.put(address, metadata);
            return true;
        }
        return false;
    }

    // ------------------------------------------------------ internal

    private String resolveTemplate(AddressTemplate template) {
        return resolver.resolve(template).template;
    }

    private boolean inCache(String address) {
        return cache.contains(address);
        // TODO Check PouchDB 2nd level cache and add it to the cache if needed
    }

    private boolean processedInCache(String address) {
        if (processedAddresses.containsKey(address)) {
            List<String> pas = processedAddresses.get(address);
            for (String pa : pas) {
                if (!inCache(pa)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
