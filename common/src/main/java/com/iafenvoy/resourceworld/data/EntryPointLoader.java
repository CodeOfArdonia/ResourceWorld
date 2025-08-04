package com.iafenvoy.resourceworld.data;

import java.util.List;

public abstract class EntryPointLoader {
    public static EntryPointLoader INSTANCE;
    private List<RandomTeleportEntrypoint> entries;

    public List<RandomTeleportEntrypoint> getEntries() {
        if (this.entries == null) this.entries = this.loadEntries();
        return this.entries;
    }

    protected abstract List<RandomTeleportEntrypoint> loadEntries();
}
