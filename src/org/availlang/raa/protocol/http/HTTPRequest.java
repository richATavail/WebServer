/*
 * HTTPRequest.java
 * Copyright © 2018, Richard A. Arriaga
 * All rights reserved.
 */

package org.availlang.raa.protocol.http;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.availlang.raa.configuration.ServerConfiguration;
import org.availlang.raa.exceptions.InvalidProtocolMethod;
import org.availlang.raa.protocol.ProtocolVersion;
import org.availlang.raa.server.HTTPChannel;
import org.availlang.raa.server.Server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@code HTTPRequest} is client request received by the {@link Server}.
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public class HTTPRequest
{
	/**
	 * The {@link Map} from the String representation of the {@link
	 * HTTPHeaderField} to the associated value that in total makes up the
	 * HTTP header (minus the {@link HTTPProtocolMethod}).
	 *
	 * @see <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html">
	 *     RFC 2616 4.2</a>
	 */
	@SuppressWarnings("WeakerAccess")
	public final @NotNull Map<String, String> rawHeaders;

	/**
	 * Answer the String value for the provided {@link HTTPHeaderField}.
	 *
	 * @param field
	 *        The {@code HTTPHeaderField} to retrieve.
	 * @return A String or {@code null} if not available.
	 */
	@SuppressWarnings("unused")
	public @Nullable String headerValue (final @NotNull HTTPHeaderField field)
	{
		return rawHeaders.getOrDefault(
			field.headerField().toLowerCase(), null);
	}

	/**
	 * The {@link ProtocolVersion} that represents this {@link HTTPRequest}.
	 */
	@SuppressWarnings("WeakerAccess")
	public final @NotNull ProtocolVersion protocolVersion;

	/**
	 * The {@link HTTPProtocolMethod} for the request.
	 */
	@SuppressWarnings("WeakerAccess")
	public final @NotNull HTTPProtocolMethod protocolMethod;

	/**
	 * The target resource of this {@link HTTPRequest}.
	 */
	@SuppressWarnings("WeakerAccess")
	public final @NotNull String requestTarget;

	/**
	 * The HTTP message-body that contains any data sent by the client.
	 *
	 *  @see <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html">
	 *     RFC 2616 4.3</a>
	 */
	@SuppressWarnings("WeakerAccess")
	public final @NotNull String messageBody;

	/**
	 * Process the raw message as an {@link HTTPRequest}.
	 *
	 * @param rawMessage
	 *        The raw message String.
	 * @param channel
	 *        The {@link HTTPChannel} the request was received on.
	 */
	public static void processRequest (
		final @NotNull String rawMessage,
		final @NotNull HTTPChannel channel)
	{
		// Divide the message into two halves; headers and message body
		final String[] messageHalves =
			rawMessage.split("(?:\r\n\r\n)", 2);

		// Identify the protocol method, the protocol version, and the requested
		// resource.
		final String[] headers = messageHalves[0].split("(?:\r\n)+");
		System.out.println(rawMessage); // TODO remove me post dev
		final String[] protocolRequest =
			headers[0].split(" +");
		if (protocolRequest.length != 3)
		{
			channel.badRequest();
			return;
		}
		final String version = protocolRequest[2];
		if (ProtocolVersion.isInvalidProtocol(version))
		{
			channel.write(ResponseStatusCode.HTTP_VERSION_NOT_SUPPORTED);
			return;
		}
		final ProtocolVersion protocolVersion =
			ProtocolVersion.protocolVersion(version);
		final HTTPProtocolMethod method;
		try
		{
			method = (HTTPProtocolMethod)
				protocolVersion.protocolMethod(protocolRequest[0]);
		}
		catch (final InvalidProtocolMethod e)
		{
			channel.write(ResponseStatusCode.METHOD_NOT_ALLOWED);
			return;
		}
		final String requestTarget =
			protocolRequest[1].equals("/")
				? ServerConfiguration.homePage()
				: protocolRequest[1];

		// Extract the remaining headers
		final Map<String, String> rawHeaderMap = new HashMap<>();
		if (headers.length > 1)
		{
			for (int i = 1; i < headers.length; i++)
			{
				final String[] halves = headers[i].split(":", 2);
				if (halves.length != 2)
				{
					channel.badRequest();
					return;
				}
				rawHeaderMap.put(
					halves[0].trim().toLowerCase(),
					halves[1].trim());
			}
		}
		final String messageBody = messageHalves.length == 2
			? messageHalves[1].trim() : "";

		final HTTPRequest request = new HTTPRequest(
			rawHeaderMap,
			protocolVersion,
			method,
			requestTarget,
			messageBody);

		// Process the request
		request.protocolMethod.processRequest(request, channel);
	}

	/**
	 * Construct a {@link HTTPRequest}.
	 *
	 * @param rawHeaders
	 *        A {@link Map} from the String representation of the {@link
	 * 	      HTTPHeaderField} to the associated value that in total makes up
	 * 	      the HTTP header (minus the {@link HTTPProtocolMethod}).
	 * @param protocolVersion
	 *        The {@link ProtocolVersion} that represents this {@link
	 *        HTTPRequest}.
	 * @param protocolMethod
	 *        The {@link HTTPProtocolMethod} for the request.
	 * @param requestTarget
	 *        The target resource of this {@link HTTPRequest}.
	 * @param messageBody
	 *        The HTTP message-body that contains any data sent by the client.
	 */
	private HTTPRequest (
		final Map<String, String> rawHeaders,
		final ProtocolVersion protocolVersion,
		final HTTPProtocolMethod protocolMethod,
		final String requestTarget,
		final String messageBody)
	{
		this.rawHeaders = Collections.unmodifiableMap(rawHeaders);
		this.protocolVersion = protocolVersion;
		this.protocolMethod = protocolMethod;
		this.requestTarget = requestTarget;
		this.messageBody = messageBody;
	}
}
