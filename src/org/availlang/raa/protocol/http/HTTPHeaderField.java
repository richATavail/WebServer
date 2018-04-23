/*
 * HTTPHeaderField.java
 * Copyright © 2018, Richard A. Arriaga
 * All rights reserved.
 */

package org.availlang.raa.protocol.http;
import com.sun.istack.internal.NotNull;

/**
 * A {@code HTTPHeaderField} is an enumeration listing various HTTP Headers.
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public enum HTTPHeaderField
{
	/**
	 * Lists acceptable media types for the response.
	 */
	ACCEPT ()
	{
		@Override
		public HTTPHeaderFieldType type ()
		{
			return HTTPHeaderFieldType.REQUEST;
		}
	},

	/**
	 * Restricts the acceptable content codings.
	 */
	ACCEPT_ENCODING ("accept-encoding")
	{
		@Override
		public HTTPHeaderFieldType type ()
		{
			return HTTPHeaderFieldType.REQUEST;
		}
	},

	/**
	 * Restricts the set of natural languages that are preferred for the
	 * response.
	 */
	ACCEPT_LANGUAGE ("accept-language")
	{
		@Override
		public HTTPHeaderFieldType type ()
		{
			return HTTPHeaderFieldType.REQUEST;
		}
	},

	/**
	 * Lists the set of {@link HTTPProtocolMethod}s allowed for use.
	 */
	ALLOW ()
	{
		@Override
		public HTTPHeaderFieldType type ()
		{
			return HTTPHeaderFieldType.ENTITY;
		}
	},

	/**
	 * Specification of directives that must be obeyed by the caching system.
	 */
	CACHE_CONTROL ("cache-control")
	{
		@Override
		public HTTPHeaderFieldType type ()
		{
			return HTTPHeaderFieldType.GENERAL;
		}
	},

	/**
	 * Indicates the media type of the entity-body sent to the recipient.
	 */
	CONTENT_TYPE ("content-type")
	{
		@Override
		public HTTPHeaderFieldType type ()
		{
			return HTTPHeaderFieldType.ENTITY;
		}
	},

	/**
	 * Indicates the size of the entity body.
	 */
	CONTENT_LENGTH ("content-length")
	{
		@Override
		public HTTPHeaderFieldType type ()
		{
			return HTTPHeaderFieldType.ENTITY;
		}
	},

	/**
	 * The modifier to the media type. Unsupported media types by the server
	 * should respond to the client with a {@link
	 * ResponseStatusCode#UNSUPPORTED_MEDIA_TYPE}.
	 */
	CONTENT_ENCODING ("content-length")
	{
		@Override
		public HTTPHeaderFieldType type ()
		{
			return HTTPHeaderFieldType.ENTITY;
		}
	},

	/**
	 * The natural language of the intended audience of the message's enclosed
	 * entity.
	 */
	CONTENT_LANGUAGE ("content-language")
	{
		@Override
		public HTTPHeaderFieldType type ()
		{
			return HTTPHeaderFieldType.ENTITY;
		}
	},

	/**
	 * The options desired for a particular connection.
	 */
	CONNECTION ()
	{
		@Override
		public HTTPHeaderFieldType type ()
		{
			return HTTPHeaderFieldType.GENERAL;
		}
	},

	/**
	 * Contains a name-value pair of information stored for the URL.
	 */
	COOKIE ()
	{
		@Override
		public HTTPHeaderFieldType type ()
		{
			return HTTPHeaderFieldType.REQUEST;
		}
	},

	/**
	 * Specifies the host and port number for the resource being requested.
	 */
	HOST ()
	{
		@Override
		public HTTPHeaderFieldType type ()
		{
			return HTTPHeaderFieldType.REQUEST;
		}
	},

	/**
	 * The URI of the resource that originated the request URL.
	 */
	REFERER ()
	{
		@Override
		public HTTPHeaderFieldType type ()
		{
			return HTTPHeaderFieldType.REQUEST;
		}
	},

	/**
	 * Non-standard request field that sends a signal to the server expressing
	 * the client's preference for an encrypted and authenticated response.
	 */
	UPGRADE_INSECURE_REQUESTS ("upgrade-insecure-requests")
	{
		@Override
		public HTTPHeaderFieldType type ()
		{
			return HTTPHeaderFieldType.REQUEST;
		}
	},

	/**
	 * Information about the user agent originating the request.
	 */
	USER_AGENT ("user-agent")
	{
		@Override
		public HTTPHeaderFieldType type ()
		{
			return HTTPHeaderFieldType.REQUEST;
		}
	};

	/**
	 * The HTTP header field name.
	 */
	private final @NotNull String headerField;

	/**
	 * Answer the {@link #headerField}.
	 *
	 * @return A String.
	 */
	public @NotNull String headerField ()
	{
		return headerField;
	}

	/**
	 * Compose a header for this {@link HTTPHeaderField} for the given header
	 * value.
	 *
	 * @param value
	 *        The String header value.
	 * @return A String.
	 */
	public @NotNull String header (final @NotNull String value)
	{
		return String.format("%s: %s\r\n", headerField, value);
	}

	/**
	 * Answer this {@link HTTPHeaderField}'s {@link HTTPHeaderFieldType}.
	 *
	 * @return A {@code HTTPHeaderFieldType}.
	 */
	public abstract @NotNull HTTPHeaderFieldType type ();

	/**
	 * Construct the {@link HTTPHeaderField}.
	 *
	 * @param headerField
	 *        The HTTP header field name.
	 */
	HTTPHeaderField (final String headerField)
	{
		this.headerField = headerField;
	}

	HTTPHeaderField ()
	{
		this.headerField = name().toLowerCase();
	}
}
