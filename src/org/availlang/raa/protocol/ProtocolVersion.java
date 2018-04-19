/*
 * ProtocolVersion.java
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

package org.availlang.raa.protocol;
import com.sun.istack.internal.NotNull;
import org.availlang.raa.exceptions.InvalidProtocolMethod;
import org.availlang.raa.protocol.http.HTTPProtocolMethod;
import org.availlang.raa.server.HTTPChannel;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * A {@code ProtocolVersion} is an enumeration listing the protocols supported
 * by this web server.
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public enum ProtocolVersion
{
	/**
	 * HTTP version 1.1 as described by RFC 7231.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-4.3">RFC 7231</a>
	 */
	HTTP_1_1("HTTP/1.1")
	{
		@Override
		public @NotNull ProtocolMethod protocolMethod (
			final String methodIdentifier)
		throws InvalidProtocolMethod
		{
			final HTTPProtocolMethod method =
				HTTPProtocolMethod.protocolMethod(methodIdentifier);
			if (method == null || !method.supportsProtocolVersion(this))
			{
				throw new InvalidProtocolMethod(String.format(
					"%s is not a valid method for %s",
					method,
					versionIdentifier));
			}
			return method;
		}

		@Override
		public void handleChannel (final AsynchronousSocketChannel channel)
		{
			HTTPChannel.handleChannel(channel);
		}
	};

	/**
	 * The String identifier for this version.
	 */
	public final @NotNull String versionIdentifier;

	/**
	 * A {@link Map} of String {@link #versionIdentifier}s to the respective
	 * {@link ProtocolVersion}.
	 */
	private static @NotNull Map<String, ProtocolVersion> protocolVersionMap =
		new HashMap<>();

	/**
	 * The comma-separated list of valid {@link
	 * ProtocolVersion#versionIdentifier}s.
	 */
	private static final String validVersions;

	static
	{
		final StringJoiner sj = new StringJoiner(",");
		for (final ProtocolVersion pv : values())
		{
			final String key = pv.versionIdentifier.toUpperCase();
			if (protocolVersionMap.containsKey(key))
			{
				final ExceptionInInitializerError ex =
					new ExceptionInInitializerError(String.format(
						"%s on %s is already used as a version identifier!",
						key,
						pv.name()));
				ex.printStackTrace();
			}
			protocolVersionMap.put(key, pv);
			sj.add(pv.versionIdentifier);
		}
		validVersions = sj.toString();
	}

	/**
	 * Answer the comma-separated list of valid {@link
	 * ProtocolVersion#versionIdentifier}s.
	 *
	 * @return A String.
	 */
	public static String validVersions ()
	{
		return validVersions;
	}

	/**
	 * Answer whether or not the provided String is a valid {@link
	 * ProtocolVersion}.
	 *
	 * @param protocol
	 *        The protocol to confirm support for.
	 * @return {@code true} indicates it is supported; {@code false} otherwise.
	 */
	public static boolean isInvalidProtocol (final String protocol)
	{
		return protocolVersionMap.get(protocol.toUpperCase()) == null;
	}

	/**
	 * Answer the associated {@link ProtocolVersion} with the input {@link
	 * #versionIdentifier}.
	 *
	 * @param protocol
	 *        A valid {@link ProtocolVersion#versionIdentifier}.
	 * @return A {@code ProtocolVersion}.
	 */
	public static @NotNull ProtocolVersion protocolVersion (
		final String protocol)
	{
		final ProtocolVersion pv =
			protocolVersionMap.get(protocol.toUpperCase());
		assert pv != null : "Should only access valid protocols!";
		return pv;
	}

	/**
	 * Answer the {@link ProtocolMethod} for the given String.
	 *
	 * @param methodIdentifier
	 *        The method's String identifier.
	 * @return A {@code ProtocolMethod}.
	 * @throws InvalidProtocolMethod If the method is not supported or doesn't
	 *         exist.
	 */
	public abstract ProtocolMethod protocolMethod (
		final String methodIdentifier)
	throws InvalidProtocolMethod;

	/**
	 * Process the provided {@link AsynchronousSocketChannel}.
	 *
	 * @param channel
	 *        The {@code AsynchronousSocketChannel} to read/write data from/to
	 * 	      the client.
	 */
	public abstract void handleChannel (
		final AsynchronousSocketChannel channel);

	/**
	 * Construct the {@link ProtocolVersion}.
	 *
	 * @param versionIdentifier
	 *        The String identifier for the version.
	 */
	ProtocolVersion (final String versionIdentifier)
	{
		this.versionIdentifier = versionIdentifier;
	}
}
