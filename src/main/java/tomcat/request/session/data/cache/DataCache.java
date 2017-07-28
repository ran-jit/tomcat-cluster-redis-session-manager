package tomcat.request.session.data.cache;

/**
 * Tomcat clustering with Redis data-cache implementation.
 * 
 * API for Data cache.
 *
 * @author Ranjith Manickam
 * @since 2.0
 */
public interface DataCache {

	/**
	 * To set value in data-cache
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	byte[] set(String key, byte[] value);

	/**
	 * To set value if key not exists in data-cache
	 * 
	 * Returns If key exists = 0 else 1
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	Long setnx(String key, byte[] value);

	/**
	 * To expire the value based on key in data-cache
	 * 
	 * @param key
	 * @param seconds
	 * @return
	 */
	Long expire(String key, int seconds);

	/**
	 * To get the value based on key from data-cache
	 * 
	 * @param key
	 * @return
	 */
	byte[] get(String key);

	/**
	 * To delete the value based on key from data-cache
	 * 
	 * @param key
	 * @return
	 */
	Long delete(String key);

}