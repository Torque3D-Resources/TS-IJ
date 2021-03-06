package com.torquescript.symbols;

import com.intellij.openapi.project.Project;
import java.util.Collection;

public class TSCachedList<T> {
    private Collection<T> items = null;
    private TSCachedListGenerator<T> function;
    private long lastUpdate;
    private static final long CACHE_LIFETIME = 5 * /* ns */ 1000000;
    private static final String LOCK = "Probably slow";

    public TSCachedList(TSCachedListGenerator<T> generator) {
        function = generator;
    }

    /**
     * Get a list of all symbols in the project. This list is cached and updated every few seconds
     * so you don't have to find all the symbols for every function call.
     * @param project Containing project in which to search
     * @return A list of all symbol declarations
     */
    public Collection<T> getList(Project project) {
        //Cache is still warm, use it instead of searching
        if (!getUpdateOnNext()) {
            return items;
        }
        //Need to regenerate cache... do this part outside the synchronize because it's slow
        Collection<T> updated = function.generate(project);
        //Update safely
        synchronized (LOCK) {
            lastUpdate = System.nanoTime();
            items = updated;
            return items;
        }
    }

    /**
     * Easy way to tell if the next call to getList() will regenerate the list
     * @return If the call will generate
     */
    public boolean getUpdateOnNext() {
        //Need to synchronize this in case we update cache while something is accessing the symbols
        synchronized (LOCK) {
            boolean needUpdate = false;

            //If the cache has existed for long enough we should probably regenerate it
            if (items == null) {
                needUpdate = true;
            } else {
                if (System.nanoTime() - lastUpdate > CACHE_LIFETIME) {
                    needUpdate = true;
                }
            }
            return needUpdate;
        }
    }
}
