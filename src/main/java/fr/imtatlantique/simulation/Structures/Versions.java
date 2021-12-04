package fr.imtatlantique.simulation.Structures;

import java.util.TreeMap;

public class Versions {

    public TreeMap<Long, Integer> versions;

    public Versions() {
        this.versions = new TreeMap<Long, Integer>();
    }

    public boolean isStale(Identifier id) {
        // when no key, we assume lowest counter 0
        return this.versions.containsKey(id.getId()) && id.getCounter() < this.versions.get(id.getId());
    }

    public void update(Identifier id) {
        if (!this.versions.containsKey(id.getId()))
            this.versions.put(id.getId(), 0);

        this.versions.put(id.getId(), Math.max(this.versions.get(id.getId()), id.getCounter()));
    }

    public boolean isBrandNew(Identifier id) {
        return !this.versions.containsKey(id.getId()) || this.versions.get(id.getId()) < id.getCounter();
    }

    @Override
    public String toString() {
        return "Versions{" +
                "versions=" + versions +
                '}';
    }
}