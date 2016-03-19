package com.r.tomcat.session.management;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Tomcat clustering implementation
 * 
 * This class is uses to store and retrieve the HTTP request session objects from catalina to data cache
 *
 * @author Ranjith Manickam
 * @since 1.0
 */
public class SessionSerializationMetadata implements Serializable
{
	private static final long serialVersionUID = 124438185184833546L;

	private byte[] sessionAttributesHash;

	public SessionSerializationMetadata() {
		this.sessionAttributesHash = new byte[0];
	}

	public byte[] getSessionAttributesHash() {
		return sessionAttributesHash;
	}

	public void setSessionAttributesHash(byte[] sessionAttributesHash) {
		this.sessionAttributesHash = sessionAttributesHash;
	}

	public void copyFieldsFrom(SessionSerializationMetadata metadata) {
		this.setSessionAttributesHash(metadata.getSessionAttributesHash());
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(sessionAttributesHash.length);
		out.write(this.sessionAttributesHash);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int hashLength = in.readInt();
		byte[] sessionAttributesHash = new byte[hashLength];
		in.read(sessionAttributesHash, 0, hashLength);
		this.sessionAttributesHash = sessionAttributesHash;
	}
}