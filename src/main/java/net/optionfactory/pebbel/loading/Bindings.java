package net.optionfactory.pebbel.loading;

import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * An overlayable key-value container exposing descriptors for every key. Used
 * as a generic container for symbols. E.g.:
 * {@code Bindings<String, String, VariableDescriptor>} for variables, where
 * keys are {@code String}s, values are {@code String}s and every key is
 * described by a VariableDescriptor.
 */
public class Bindings<K, V, D> {

    private final Map<K, V> self;
    private final Map<K, D> descriptors;
    private Optional<Bindings<K, V, D>> parent;

    public Bindings(Map<K, V> self, Map<K, D> descriptors, Optional<Bindings<K, V, D>> parent) {
        this.parent = parent;
        this.self = self;
        this.descriptors = descriptors;
    }

    public static <K, V, S> Bindings<K, V, S> singleton(K key, V value, S schema) {
        final Map<K, V> v = Collections.singletonMap(key, value);
        final Map<K, S> s = Collections.singletonMap(key, schema);
        return new Bindings<>(v, s, Optional.empty());
    }

    public static <K, V, S> Bindings<K, V, S> root(Map<K, V> values, Map<K, S> schema) {
        return new Bindings<>(values, schema, Optional.empty());
    }

    public static <K, V, S> Bindings<K, V, S> empty() {
        return new Bindings<>(Collections.emptyMap(), Collections.emptyMap(), Optional.empty());
    }

    public static <K, V, S> Bindings<K, V, S> overlay(Map<K, V> values, Map<K, S> schema, Bindings<K, V, S> parent) {
        return new Bindings<>(values, schema, Optional.of(parent));
    }

    private Bindings<K, V, D> unalias() {
        return new Bindings<>(self, descriptors, parent);
    }

    public Bindings<K, V, D> overlaying(Bindings<K, V, D> parent) {
        final Deque<Bindings<K, V, D>> hierarchy = new LinkedList<>();
        Bindings<K, V, D> root = this;
        hierarchy.push(root);
        while (root.parent.isPresent()) {
            root = root.parent.get();
            hierarchy.push(root);
        }
        Bindings<K, V, D> leaf = parent;
        while (!hierarchy.isEmpty()) {
            final Bindings<K, V, D> popped = hierarchy.pop().unalias();
            popped.parent = Optional.of(leaf);
            leaf = popped;
        }
        return leaf;
    }

    public Bindings<K, V, D> overlaidBy(Bindings<K, V, D> children) {
        return children.overlaying(this);
    }

    public Maybe<V> value(K key) {
        if (self.containsKey(key)) {
            return Maybe.just(self.get(key));
        }
        if (!parent.isPresent()) {
            return Maybe.nothing();
        }
        return parent.get().value(key);
    }

    public Maybe<D> descriptor(K key) {
        if (descriptors.containsKey(key)) {
            return Maybe.just(descriptors.get(key));
        }
        if (!parent.isPresent()) {
            return Maybe.nothing();
        }
        return parent.get().descriptor(key);
    }

    public Set<K> keys() {
        final Set<K> ks = new HashSet<>();
        if (parent.isPresent()) {
            ks.addAll(parent.get().keys());
        }
        ks.addAll(self.keySet());
        return ks;
    }

    public Map<K, V> values() {
        final Map<K, V> vs = new HashMap<>();
        if (parent.isPresent()) {
            vs.putAll(parent.get().values());
        }
        vs.putAll(self);
        return vs;
    }

    public Map<K, D> descriptors() {
        final Map<K, D> vs = new HashMap<>();
        if (parent.isPresent()) {
            vs.putAll(parent.get().descriptors());
        }
        vs.putAll(descriptors);
        return vs;
    }
}
