/*
 * HTTPProtocolMethod.java
 * Copyright © 2018, Richard A. Arriaga
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of the contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.availlang.raa.protocol.http;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.availlang.raa.cache.FileCache;
import org.availlang.raa.protocol.ProtocolMethod;
import org.availlang.raa.protocol.ProtocolVersion;
import org.availlang.raa.server.HTTPChannel;

import java.nio.channels.CompletionHandler;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

/**
 * A {@code HTTPProtocolMethod} is an enum for identifying a type of HTTP
 * protocol method.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7231#section-4.3">RFC 7231 -
 * Method Definitions</a>
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public enum HTTPProtocolMethod
implements ProtocolMethod
{
	/** The HTTP GET method */
	GET
	{
		@Override
		public @NotNull Set<ProtocolVersion> supportedVersions ()
		{
			return Collections.singleton(ProtocolVersion.HTTP_1_1);
		}

		@Override
		public void processRequest (
			final @NotNull HTTPRequest request, 
			final @NotNull HTTPChannel channel)
		{
			// TODO as of right now this just presumes all GET requests are
			// file requests (e.g. HTML page). This will be refactored in the
			// future to be generic so as to support things like API calls using
			// REST, or some other protocol that enables client-server data driven 
			// applications. Any extension should allow for a generic way to 
			// specify message handling.
			FileCache.readFile(
				request.requestTarget,
				(bytesRead, byteArray) ->
					channel.write(
						ResponseStatusCode.OK,
						byteArray,
						new CompletionHandler<Integer, Void>()
						{
							@Override
							public void completed (
								final Integer result,
								final Void attachment)
							{
								channel.close();
							}

							@Override
							public void failed (
								final Throwable exc,
								final Void attachment)
							{
								// TODO log?
								channel.close();
							}
						}),
				throwable ->
					channel.write(ResponseStatusCode.NOT_FOUND));
		}
	},

	/** The HTTP POST method */
	POST
	{
		@Override
		public @NotNull Set<ProtocolVersion> supportedVersions ()
		{
			return Collections.singleton(ProtocolVersion.HTTP_1_1);
		}
	},

	/** The HTTP PUT method */
	@SuppressWarnings("unused")
	PUT
	{
		@Override
		public @NotNull Set<ProtocolVersion> supportedVersions ()
		{
			return Collections.singleton(ProtocolVersion.HTTP_1_1);
		}
	},

	/** The HTTP HEAD method */
	@SuppressWarnings("unused")
	HEAD
	{
		@Override
		public @NotNull Set<ProtocolVersion> supportedVersions ()
		{
			return Collections.singleton(ProtocolVersion.HTTP_1_1);
		}
	},

	/** The HTTP DELETE method */
	@SuppressWarnings("unused")
	DELETE
	{
		@Override
		public @NotNull Set<ProtocolVersion> supportedVersions ()
		{
			return Collections.singleton(ProtocolVersion.HTTP_1_1);
		}
	},

	/** The HTTP TRACE method */
	@SuppressWarnings("unused")
	TRACE
	{
		@Override
		public @NotNull Set<ProtocolVersion> supportedVersions ()
		{
			return Collections.singleton(ProtocolVersion.HTTP_1_1);
		}
	},

	/** The HTTP OPTIONS method */
	@SuppressWarnings("unused")
	OPTIONS
	{
		@Override
		public @NotNull Set<ProtocolVersion> supportedVersions ()
		{
			return Collections.singleton(ProtocolVersion.HTTP_1_1);
		}
	},

	/** The HTTP CONNECT method */
	@SuppressWarnings("unused")
	CONNECT
	{
		@Override
		public @NotNull Set<ProtocolVersion> supportedVersions ()
		{
			return Collections.singleton(ProtocolVersion.HTTP_1_1);
		}
	},

	/** The HTTP PATCH method */
	@SuppressWarnings("unused")
	PATCH
	{
		@Override
		public @NotNull Set<ProtocolVersion> supportedVersions ()
		{
			return Collections.singleton(ProtocolVersion.HTTP_1_1);
		}
	};

	/**
	 * Answer the {@link Set} of supported {@link ProtocolVersion}s by this
	 * {@link HTTPProtocolMethod}.
	 *
	 * @return A {@code Set}.
	 */
	public abstract @NotNull Set<ProtocolVersion> supportedVersions ();

	/**
	 * Process the {@link HTTPRequest} received on the provided {@link
	 * HTTPChannel}.
	 *
	 * @param request
	 *        The {@code HTTPRequest} to process.
	 * @param channel
	 *        The {@code HTTPChannel} the request was received on.
	 */
	public void processRequest (
		final @NotNull HTTPRequest request,
		final @NotNull HTTPChannel channel)
	{
		channel.write(ResponseStatusCode.NOT_IMPLEMENTED);
	}

	/**
	 * A {@link Map} of String {@link #name()}s to the respective
	 * {@link HTTPProtocolMethod}.
	 */
	private static @NotNull Map<String, HTTPProtocolMethod> protocolMethodMap =
		new HashMap<>();

	/**
	 * The comma-separated list of valid {@link
	 * HTTPProtocolMethod#name()}s.
	 */
	private static final String validMethods;

	static
	{
		final StringJoiner sj = new StringJoiner(",");
		for (final HTTPProtocolMethod pv : values())
		{
			final String key = pv.name().toUpperCase();
			if (protocolMethodMap.containsKey(key))
			{
				final ExceptionInInitializerError ex =
					new ExceptionInInitializerError(String.format(
						"%s on %s is already used as a version identifier!",
						key,
						pv.name()));
				ex.printStackTrace();
			}
			protocolMethodMap.put(key, pv);
			sj.add(key);
		}
		validMethods = sj.toString();
	}

	/**
	 * Answer the comma-separated list of valid {@link
	 * HTTPProtocolMethod#name()}s.
	 *
	 * @return A String.
	 */
	public static String validMethods ()
	{
		return validMethods;
	}

	/**
	 * Answer whether or not the provided String is a valid {@link
	 * HTTPProtocolMethod}.
	 *
	 * @param method
	 *        The method to confirm support for.
	 * @return {@code true} indicates it is supported; {@code false} otherwise.
	 */
	public static boolean isInvalidMethod (final @NotNull String method)
	{
		return protocolMethodMap.get(method.toUpperCase()) == null;
	}

	/**
	 * Answer the {@link HTTPProtocolMethod}.
	 *
	 * @param method
	 *        The String method to retrieve.
	 * @return The corresponding {@code HTTPProtocolMethod} if it exists; {@code
	 *         null} otherwise.
	 */
	public static @Nullable HTTPProtocolMethod protocolMethod (
		final @NotNull String method)
	{
		return protocolMethodMap.get(method.toUpperCase());
	}

	@Override
	public boolean supportsProtocolVersion (final @NotNull ProtocolVersion pv)
	{
		return supportedVersions().contains(pv);
	}
}
