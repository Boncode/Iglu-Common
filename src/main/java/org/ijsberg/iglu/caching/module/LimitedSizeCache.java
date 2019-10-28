package org.ijsberg.iglu.caching.module;

public class LimitedSizeCache<K, V> extends StandardCache<K, V> {

    private int maxSize = 1000;

    public LimitedSizeCache() {
        super();
    }

    public LimitedSizeCache(int maxSize) {
        super();
        this.maxSize = maxSize;
    }

    @Override
    public void store(K key, V object) {
        super.store(key, object);
        //FIXME
        while (getSize() > maxSize) {
            clear(getOldestObject());
        }
    }

}
