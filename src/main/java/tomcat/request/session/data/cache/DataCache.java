package tomcat.request.session.data.cache;

/** author: Ranjith Manickam @ 12 Jul' 2018 */
public interface DataCache {

    /**
     * Set value in data-cache.
     *
     * @param key   - key with which the specified value is to be associated.
     * @param value - value to be associated with the specified key.
     * @return - Returns the value.
     */
    byte[] set(String key, byte[] value);

    /**
     * Set value if key not exists in data-cache.
     *
     * @param key   - key with which the specified value is to be associated.
     * @param value - value to be associated with the specified key.
     * @return - Returns '0' if key already exists else '1'.
     */
    Long setnx(String key, byte[] value);

    /**
     * Set expiry in data-cache.
     *
     * @param key     - key with which the specified value is to be associated.
     * @param seconds - expiration time in seconds.
     * @return - Returns the expiration time in seconds.
     */
    Long expire(String key, int seconds);

    /**
     * Get value from data-cache.
     *
     * @param key - key with which the specified value is to be associated.
     * @return - Returns the value.
     */
    byte[] get(String key);

    /**
     * Delete value from data-cache.
     *
     * @param key - key with which the specified value is to be associated.
     * @return - Returns the number of keys that were removed.
     */
    Long delete(String key);
}
