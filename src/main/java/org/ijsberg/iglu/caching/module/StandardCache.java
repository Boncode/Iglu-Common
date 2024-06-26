/*
 * Copyright 2011-2014 Jeroen Meetsma - IJsberg Automatisering BV
 *
 * This file is part of Iglu.
 *
 * Iglu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Iglu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Iglu.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ijsberg.iglu.caching.module;

import org.ijsberg.iglu.configuration.Startable;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.scheduling.Pageable;
import org.ijsberg.iglu.util.caching.Cache;
import org.ijsberg.iglu.util.caching.CachedObject;

import java.util.*;

/**
 * This class is a basic caching service that stores objects for a certain amount of time.
 */
public class StandardCache<K, V> implements Cache<K, V>, Startable, Pageable {
	private Date lastRun = new Date();

	private final HashMap<K, CachedObject<V>> data = new HashMap<K, CachedObject<V>>(50);
	private final HashMap<K, CachedObject<V>> mirror = new HashMap<K, CachedObject<V>>(50);

	public static final int DEFAULT_TTL = 900;// 15 minutes; 0 = don't cache
	public static final int DEFAULT_CLEANUP_INTERVAL = 180; // 3 minutes; 0 = never cleanup

	private int ttlInSeconds = DEFAULT_TTL;
	private long cleanupInterval = DEFAULT_CLEANUP_INTERVAL;

	//statistics
	private long hits;
	private long misses;
	private long unavailable;
	private long delayedHits;
	private long delayedMisses;

	/**
	 * Constructs a cache.
	 *
	 */
	public StandardCache() {
	}

	/**
	 * Constructs a cache.
	 *
	 * @param ttl             time to live in seconds for cached objects
	 * @param cleanupInterval cleanup interval in seconds
	 */
	public StandardCache(int ttl, long cleanupInterval) {
		this.ttlInSeconds = ttl;
		this.cleanupInterval = cleanupInterval;
	}

	/**
	 * Returns a status report containing behavior and statistics.
	 *
	 * @return status report
	 */
	public String getReport() {
		StringBuffer info = new StringBuffer();
		if (!isCachingEnabled()) {
			info.append("cache behaviour: DISABLED\n");
		} else if (isCachingPermanent()) {
			info.append("cache behaviour: PERMANENT\n");
		} else {
			info.append("cache behaviour: NORMAL\n");
		}

		info.append("time to live: " + ttlInSeconds + " s\n");
		info.append("cleanup interval: " + cleanupInterval + " s\n");
		info.append("cache size: " + data.size() + " objects\n");
		info.append("cache mirror size: " + mirror.size() + " object(s)\n");
		info.append("cache hits: " + hits + '\n');
		info.append("cache misses: " + misses + '\n');
		info.append("cache unavailable: " + unavailable + '\n');
		info.append("cache delayed hits: " + delayedHits + '\n');
		info.append("cache delayed misses: " + delayedMisses + '\n');
		info.append("next cleanup run: " + new Date(lastRun.getTime() + (cleanupInterval * 1000)) + "\n");
		return info.toString();
	}


	private boolean isStarted = false;

	/**
	 */
	public void start() {
		isStarted = true;
	}

	/**
	 * Clears storage. Resets statistics.
	 * Is invoked by superclass.
	 */
	public void stop() {
		hits = 0;
		misses = 0;
		unavailable = 0;
		delayedHits = 0;
		delayedMisses = 0;
		data.clear();
		mirror.clear();
		isStarted = false;
	}

	/**
	 * Initializes cache.
	 * Properties:
	 * <ul>
	 * <li>ttlInSeconds: time to live for stored objects in seconds (default: 900 = 15 minutes)</li>
	 * <li>cleanup_interval: interval for check for expired objects in seconds (default: 180 = 3 minutes)</li>
	 * </ul>
	 */
	public void setProperties(Properties properties) {
		ttlInSeconds = Integer.valueOf(properties.getProperty("ttlInSeconds", "" + ttlInSeconds));
		cleanupInterval = Integer.valueOf(properties.getProperty("cleanup_interval", "" + cleanupInterval));
	}


	/**
	 * Stores an object in the cache.
	 * Nulls can (and must) be stored as well.
	 * This saves a lot of unnecessary lookups!
	 *
	 * @param key    the key to retrieve the object by
	 * @param object the object to be cached
	 */
	public void store(K key, V object) {
		if (isCachingEnabled()) {
			CachedObject<V> co = new CachedObject<V>(object);
			storeCachedObject(key, co);
		}
	}

	private boolean isCachingEnabled() {
		return ttlInSeconds > 0 && isStarted();
	}

	private boolean isCachingPermanent() {
		return cleanupInterval == 0;
	}

	/**
	 * @param key
	 * @return true if a null was deliberately stored under a key
	 */
	public boolean containsStoredNull(K key) {
		CachedObject<V> co = getCachedObject(key);
		return isCachingEnabled() && co != null && co.getObject() == null && !co.isBeingRetrieved() && !co.isExpired(ttlInSeconds);
	}


	/**
	 * Retrieves an object from cache.
	 * This method should be used if a programmer suspects bursts of requests for
	 * a particular object.
	 * If this is the case, the first thread will retrieve the object,
	 * the others will wait for some time, in order to save overhead.
	 *
	 * @param key     the key to retrieve the object by
	 * @param timeout time in millis to wait for the first thread to retrieve an object from the original location
	 * @return the cached object or null if it's not found
	 */
	public V retrieve(K key, int timeout) {
		V retval = null;
		if (isCachingEnabled()) {
			CachedObject<V> co = getCachedObject(key);
			if (co == null || isCachedObjectExpired(co)) {
				misses++;
				co = new CachedObject<V>();
				co.setBeingRetrieved();
				this.storeCachedObject(key, co);
			} else if (co.getObject() != null) {
				hits++;
				retval = co.getObject();
			} else {
				//the timeout for retrieving an object is used instead of the cache timeout
				co = getCachedObjectOnceRetrievedByOtherThread(key, timeout);
				if (co == null) {
					//this could happen on a rare occasion and may not lead to problems
					delayedMisses++;
				} else if (co.getObject() == null) { // still null
					delayedMisses++;
					if (co.isExpired(timeout) && co.isBeingRetrieved()) {
						// prolongate retrieval state if cached object is not a designated null
						co.setBeingRetrieved();
					}
				} else {
					delayedHits++;
					retval = co.getObject();
				}
			}
		}
		return retval;
	}

	private CachedObject<V> getCachedObjectOnceRetrievedByOtherThread(K key, int timeout) {
		CachedObject<V> co;
		do {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ie) {
				//...
			}
			co = getCachedObject(key);
		}
		while (co != null //which may occur if it was removed from data
				&& co.getObject() == null //not retrieved
				&& co.isBeingRetrieved() //still trying
				&& !co.isExpired(timeout));//retrieval timeout not expired
		return co;
	}

	/**
	 * Places an empty wrapper in the cache to indicate that some thread should
	 * be busy retrieving the object, after which it should be cached after all.
	 *
	 * @param co
	 * @return
	 */
	private Object storeCachedObject(K key, CachedObject<V> co) {
		//data is only locked if cleanup removes cached objects
		synchronized (data) {
			data.put(key, co);
		}
		//mirror is also locked if cleanup is busy collecting cached objects
		synchronized (mirror) {
			mirror.put(key, co);
		}
		System.out.println(new LogEntry("object with key " + key + " stored in cache"));
		return co;
	}

	public int getSize() {
		return data.size();
	}

	/**
	 * @return a set containing all object stored
	 */
	public Set<V> retrieveAll() {
		HashSet<V> retval = new HashSet<V>();
		synchronized (data) {
			for (CachedObject<V> co : data.values()) {
				if (co.getObject() != null) //skip temp objects
				{
					retval.add(co.getObject());
				}
			}
		}
		return retval;
	}

	/**
	 * Retrieves an object from cache.
	 *
	 * @param key the key to retrieve the object by
	 * @return the cached object or null if it's not found
	 */
	public V retrieve(K key) {
		V retval = null;
		if (isCachingEnabled()) {
			CachedObject<V> co = getCachedObject(key);
			if (co == null || (isCachedObjectExpired(co))) {
				//take pressure off of cleanup
				misses++;
			} else if (co.getObject() == null) {
				unavailable++;
			} else {
				hits++;
				retval = co.getObject();
			}
		}
		return retval;
	}

	private boolean isCachedObjectExpired(CachedObject<V> co) {
		return co.isExpired(ttlInSeconds * 1000) && !isCachingPermanent();
	}

	protected CachedObject<V> getCachedObject(K key) {
		synchronized (data) {
			return data.get(key);
		}
	}


	public int getPageIntervalInMinutes() {
		return (int) (cleanupInterval / 60);
	}

	public int getPageOffsetInMinutes() {
		return 0;
	}

	public void onPageEvent(long officialTime) {
		if (cleanupInterval > 0 && isCachingEnabled()) {
			lastRun = new Date();
			cleanup();
		}
	}

	/**
	 * Removes all expired objects.
	 *
	 * @return the number of removed objects.
	 */
	public long cleanup() {
		int garbageSize = 0;
		if (isCachingEnabled()) {
			System.out.println(new LogEntry(Level.VERBOSE, "Identifying expired objects"));
			ArrayList<K> garbage = getExpiredObjects();
			garbageSize = garbage.size();
			System.out.println(new LogEntry("cache cleanup: expired objects: " + garbageSize));
			for (K key : garbage) {
				clear(key);
			}
		}
		return garbageSize;
	}

	private ArrayList<K> getExpiredObjects() {
		CachedObject<V> cachedObject;
		ArrayList<K> garbage = new ArrayList<K>();
		synchronized (mirror) {
			for (K key : mirror.keySet()) {
				cachedObject = (CachedObject<V>) mirror.get(key);
				if (cachedObject.isExpired(ttlInSeconds * 1000)) {
					garbage.add(key);
				}
			}
		}
		return garbage;
	}

	protected K getOldestObject() {
		CachedObject<V> cachedObject;
		K oldestKey = null;
		long lastAccessTime = System.currentTimeMillis();
		synchronized (mirror) {
			for (K key : mirror.keySet()) {
				cachedObject = mirror.get(key);
				if(cachedObject.getLastTimeAccessed() < lastAccessTime) {
					oldestKey = key;
					lastAccessTime = cachedObject.getLastTimeAccessed();
				}
			}
		}
		return oldestKey;
	}

	/**
	 * Removes an object from cache.
	 *
	 * @param key object key
	 */
	public void clear(Object key) {
		Object removed;
		if (isCachingEnabled()) {
			synchronized (mirror) {
				synchronized (data) {
					removed = data.remove(key);
					mirror.remove(key);
				}
			}
			System.out.println(new LogEntry("object with key " + key + (removed == null ? " NOT" : "") + " removed from cache"));
		}
	}

	/**
	 * Removes a collection of objects from cache.
	 *
	 * @param keys object keys
	 */
	public void clear(Collection keys) {
		synchronized (mirror) {
			synchronized (data) {
				Iterator i = keys.iterator();
				while (i.hasNext()) {
					Object key = i.next();
					data.remove(key);
					mirror.remove(key);
				}
			}
		}
	}

	public void clear() {
		synchronized (mirror) {
			synchronized (data) {
				data.clear();
				mirror.clear();
			}
		}
	}



	/**
	 * Retrieves a cache from the current layer or creates it.
	 * Only a root request will be able to embed the constructed cache in the
	 * application.
	 *
	 * @param cacheName cache service ID
	 * @return
	 */
	public static <K, V> Cache<K, V> createCache(String cacheName) {
		return createCache(cacheName, DEFAULT_TTL, DEFAULT_CLEANUP_INTERVAL/*, application, layer*/);
	}

	/**
	 * Retrieves a cache from the current layer or creates it.
	 * Only a root request will be able to embed the constructed cache in the
	 * application.
	 *
	 * @param cacheName       cache service ID
	 * @param ttl             time to live in seconds
	 * @param cleanupInterval cleanup interval in seconds
	 * @return
	 */
	public static <K, V> Cache<K, V> createCache(String cacheName, int ttl, long cleanupInterval/*, Application application, Layer layer*/) {
		StandardCache cache = new StandardCache(ttl, cleanupInterval);
		cache.setProperties(new Properties());
		cache.start();
		return cache;
	}

	@Override
	public boolean isStarted() {
		return isStarted;
	}
}
