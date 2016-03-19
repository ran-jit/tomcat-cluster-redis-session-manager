package com.r.tomcat.session.management;

/**
 * Tomcat clustering implementation
 * 
 * This class is uses to store and retrieve the HTTP request session objects from catalina to data cache
 *
 * @author Ranjith Manickam
 * @since 1.0
 */
public class DeserializedSessionContainer
{
	public final CustomRequestSession session;

	public final SessionSerializationMetadata metadata;

	public DeserializedSessionContainer(CustomRequestSession session, SessionSerializationMetadata metadata) {
		this.session = session;
		this.metadata = metadata;
	}
}