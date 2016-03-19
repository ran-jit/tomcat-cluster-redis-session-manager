package com.r.tomcat.session.management;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.HashMap;

import org.apache.catalina.util.CustomObjectInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Tomcat clustering implementation
 * 
 * This class is uses to store and retrieve the HTTP request session objects from catalina to data cache
 *
 * @author Ranjith Manickam
 * @since 1.0
 */
public class SessionDataSerializer
{
	private final Log log = LogFactory.getLog(SessionDataSerializer.class);

	private ClassLoader loader;

	public void setClassLoader(ClassLoader loader) {
		this.loader = loader;
	}

	/**
	 * method to get session attributes hash code
	 * 
	 * @param session
	 * @return
	 * @throws IOException
	 */
	public byte[] getSessionAttributesHashCode(CustomRequestSession session) throws IOException {
		byte[] serialized = null;
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		for (Enumeration<String> enumerator = session.getAttributeNames(); enumerator.hasMoreElements();) {
			String key = enumerator.nextElement();
			attributes.put(key, session.getAttribute(key));
		}
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(bos));) {
			oos.writeUnshared(attributes);
			oos.flush();
			serialized = bos.toByteArray();
		}
		MessageDigest digester = null;
		try {
			digester = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			log.error("Unable to get MessageDigest instance for MD5");
		}
		return digester.digest(serialized);
	}

	/**
	 * method to serialize custom session data
	 * 
	 * @param session
	 * @param metadata
	 * @return
	 * @throws IOException
	 */
	public byte[] serializeSessionData(CustomRequestSession session, SessionSerializationMetadata metadata) throws IOException {
		byte[] serialized = null;
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(bos));) {
			oos.writeObject(metadata);
			session.writeObjectData(oos);
			oos.flush();
			serialized = bos.toByteArray();
		}
		return serialized;
	}

	/**
	 * method to deserialize custom session data
	 * 
	 * @param data
	 * @param session
	 * @param metadata
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void deserializeSessionData(byte[] data, CustomRequestSession session, SessionSerializationMetadata metadata) throws IOException, ClassNotFoundException {
		try (BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(data)); ObjectInputStream ois = new CustomObjectInputStream(bis, loader);) {
			SessionSerializationMetadata serializedMetadata = (SessionSerializationMetadata) ois.readObject();
			metadata.copyFieldsFrom(serializedMetadata);
			session.readObjectData(ois);
		}
	}
}