package tomcat.request.session;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Tomcat clustering with Redis data-cache implementation.
 *
 * This class is uses to store and retrieve the HTTP request session object meta-data.
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
   */
  public byte[] getAttributesHash() {
    return this.attributesHash;
  }

  /**
   * To set session meta-data hash
   */
  public void setAttributesHash(byte[] attributesHash) {
    this.attributesHash = attributesHash;
  }

  /**
   * To copy session meta-data
   */
  public void copyFieldsFrom(SessionMetadata metadata) {
    this.setAttributesHash(metadata.getAttributesHash());
  }

  /**
   * To write session meta-data to output stream
   */
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.writeInt(this.attributesHash.length);
    out.write(this.attributesHash);
  }

  /**
   * To read session meta-data from input stream
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int hashLength = in.readInt();
    byte[] attributesHash = new byte[hashLength];
    in.read(attributesHash, 0, hashLength);
    this.attributesHash = attributesHash;
  }
}