package tomcat.request.session;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Tomcat clustering with Redis data-cache implementation.
 * 
 * This class is uses to store and retrieve the HTTP request session object
 * meta-data.
 *
 * @author Ranjith Manickam
 * @since 2.0
 */
public class SessionMetadata implements Serializable {

	private static final long serialVersionUID = 124438185184833546L;

	private byte[] attributesHash;

	public SessionMetadata() {
		this.attributesHash = new byte[0];
	}

	/**
	 * To get session meta-data hash
	 * 
	 * @return
	 */
	public byte[] getAttributesHash() {
		return this.attributesHash;
	}

	/**
	 * To set session meta-data hash
	 * 
	 * @param attributesHash
	 */
	public void setAttributesHash(byte[] attributesHash) {
		this.attributesHash = attributesHash;
	}

	/**
	 * To copy session meta-data
	 * 
	 * @param metadata
	 */
	public void copyFieldsFrom(SessionMetadata metadata) {
		this.setAttributesHash(metadata.getAttributesHash());
	}

	/**
	 * To write session meta-data to output stream
	 * 
	 * @param out
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(this.attributesHash.length);
		out.write(this.attributesHash);
	}

	/**
	 * To read session meta-data from input stream
	 * 
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int hashLength = in.readInt();
		byte[] attributesHash = new byte[hashLength];
		in.read(attributesHash, 0, hashLength);
		this.attributesHash = attributesHash;
	}
}