/*
 * HTTPHeaderFieldType.java
 * Copyright © 2018, Richard A. Arriaga
 * All rights reserved.
 */

package org.availlang.raa.protocol.http;
/**
 * A {@code HTTPHeaderFieldType} is an enumeration of values that provide type
 * information used to distinguish {@link HTTPHeaderField}s.
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public enum HTTPHeaderFieldType
{
	/**
	 * Headers that can be used in both {@link #REQUEST}s and {@link
	 * #RESPONSE}s.
	 */
	GENERAL,

	/**
	 * Headers specific to the request that provides more detail about the
	 * request or about the client making the request.
	 */
	REQUEST,

	/**
	 * Headers specific to the server response. This doesn't relate to the body
	 * of the message.
	 */
	RESPONSE,

	/**
	 * Headers containing information about the body of the message (e.g.
	 * MIME-type).
	 */
	ENTITY
}
