package com.r.tomcat.session.data.cache;

/**
 * Tomcat clustering implementation
 * 
 * This interface holds cache implementation
 *
 * @author Ranjith Manickam
 * @since 1.0
 */
public interface IRequestSessionCacheUtils
{
	public boolean isAvailable();

	public void setByteArray(String key, byte[] value);

	public byte[] getByteArray(String key);

	public void deleteKey(String key);

	public Long setStringIfKeyNotExists(byte[] key, byte[] value);

	public void expire(String key, int ttl);
}