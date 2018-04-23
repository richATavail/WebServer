/*
 * ResponseStatusCode.java
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
import org.availlang.raa.configuration.ServerConfiguration;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * A {@code ResponseStatusCode} is the enumeration of HTTP Response Status Codes
 * as listed in RFC 7231.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6">
 *     HTTP Response Status Codes</a>
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public enum ResponseStatusCode
{
	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.2.1">
	 * RFC 7231 6.2.1</a>
	 */
	@SuppressWarnings("unused")
	CONTINUE(100),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.2.2">
	 * RFC 7231 6.2.2</a>
	 */
	@SuppressWarnings("unused")
	SWITICHING_PROTOCOLS(101),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.1">
	 * RFC 7231 6.3.1</a>
	 */
	OK(200),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.2">
	 * RFC 7231 6.2.2</a>
	 */
	@SuppressWarnings("unused")
	CREATED(201),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.3">
	 * RFC 7231 6.3.3</a>
	 */
	@SuppressWarnings("unused")
	ACCEPTED(202),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.4">
	 * RFC 7231 6.3.4</a>
	 */
	@SuppressWarnings("unused")
	NON_AUTHORITATIVE_INFORMATION(203),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.5">
	 * RFC 7231 6.3.5</a>
	 */
	@SuppressWarnings("unused")
	NO_CONTENT(204),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.6">
	 * RFC 7231 6.3.6</a>
	 */
	@SuppressWarnings("unused")
	RESET_CONTENT(205),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-4.1">
	 * RFC 7233 4.1</a>
	 */
	@SuppressWarnings("unused")
	PARTIAL_CONTENT(206),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.1">
	 * RFC 7231 6.4.1</a>
	 */
	@SuppressWarnings("unused")
	MULTIPLE_CHOICES(300),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.2">
	 * RFC 7231 6.4.2</a>
	 */
	@SuppressWarnings("unused")
	MOVED_PERMANENTLY(301),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.3">
	 * RFC 7231 6.4.3</a>
	 */
	@SuppressWarnings("unused")
	FOUND(302),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.4">
	 * RFC 7231 6.4.4</a>
	 */
	@SuppressWarnings("unused")
	SEE_OTHER(303),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-4.1">
	 * RFC 7232 4.1</a>
	 */
	@SuppressWarnings("unused")
	NOT_MODIFIED(304),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.5">
	 * RFC 7231 6.4.5</a>
	 */
	@SuppressWarnings("unused")
	USE_PROXY(305),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.7">
	 * RFC 7231 6.4.7</a>
	 */
	@SuppressWarnings("unused")
	TEMPORARY_REDIRECT(307),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.1">
	 * RFC 7231 6.5.1</a>
	 */
	BAD_REQUEST(400),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-3.1">
	 * RFC 7235 3.1</a>
	 */
	@SuppressWarnings("unused")
	UNAUTHORIZED(401),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.2">
	 * RFC 7231 6.5.2</a>
	 */
	@SuppressWarnings("unused")
	PAYMENT_REQUIRED(402),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.3">
	 * RFC 7231 6.5.3</a>
	 */
	@SuppressWarnings("unused")
	FORBIDDEN(403),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.4">
	 * RFC 7231 6.5.4</a>
	 */
	NOT_FOUND(404),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.5">
	 * RFC 7231 6.5.5</a>
	 */
	METHOD_NOT_ALLOWED(405),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.6">
	 * RFC 7231 6.5.6</a>
	 */
	@SuppressWarnings("unused")
	NOT_ACCEPTABLE(406),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-3.2">
	 * RFC 7235 3.2</a>
	 */
	@SuppressWarnings("unused")
	PROXY_AUTHENTICATION_REQUIRED(407),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.7">
	 * RFC 7231 6.5.7</a>
	 */
	@SuppressWarnings("unused")
	REQUEST_TIMEOUT(408),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.8">
	 * RFC 7231 6.5.8</a>
	 */
	@SuppressWarnings("unused")
	CONFLICT(409),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.9">
	 * RFC 7231 6.5.9</a>
	 */
	@SuppressWarnings("unused")
	GONE(410),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.10">
	 * RFC 7231 6.5.10</a>
	 */
	@SuppressWarnings("unused")
	LENGTH_REQUIRED(411),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-4.2">
	 * RFC 7232 4.2</a>
	 */
	@SuppressWarnings("unused")
	PRECONDITION_FAILED(412),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.11">
	 * RFC 7231 6.5.11</a>
	 */
	@SuppressWarnings("unused")
	PAYLOAD_TOO_LARGE(413),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.12">
	 * RFC 7231 6.5.12</a>
	 */
	@SuppressWarnings("unused")
	URI_TOO_LONG(414),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.13">
	 * RFC 7231 6.5.13</a>
	 */
	@SuppressWarnings("unused")
	UNSUPPORTED_MEDIA_TYPE(415),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-4.4">
	 * RFC 7233 4.4</a>
	 */
	@SuppressWarnings("unused")
	RANGE_NOT_SATISFIABLE(416),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.14">
	 * RFC 7231 6.5.15</a>
	 */
	@SuppressWarnings("unused")
	EXPECTATION_FAILED(417),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.15">
	 * RFC 7231 6.5.15</a>
	 */
	@SuppressWarnings("unused")
	UPGRADE_REQUIRED(426),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.1">
	 * RFC 7231 6.6.1</a>
	 */
	@SuppressWarnings("unused")
	INTERNAL_SERVER_ERROR(500),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.2">
	 * RFC 7231 6.6.2</a>
	 */
	@SuppressWarnings("unused")
	NOT_IMPLEMENTED(501),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.3">
	 * RFC 7231 6.6.3</a>
	 */
	@SuppressWarnings("unused")
	BAD_GATEWAY(502),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.4">
	 * RFC 7231 6.6.4</a>
	 */
	@SuppressWarnings("unused")
	SERVICE_UNAVAILABLE(503),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.5">
	 * RFC 7231 6.6.5</a>
	 */
	@SuppressWarnings("unused")
	GATEWAY_TIMEOUT(504),

	/**
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.6">
	 * RFC 7231 6.6.6</a>
	 */
	HTTP_VERSION_NOT_SUPPORTED(505);

	/**
	 * The three-digit {@code integer} code giving the result of the attempt to
	 * satisfy the request.
	 */
	public final int code;

	/**
	 * Answer the HTTP header of the {@link ResponseStatusCode}-specific
	 * response to the client.
	 *
	 * @param headers
	 *        An array of HTTP headers represented as Strings.
	 * @return A String.
	 */
	public @NotNull String responseHeader (final String... headers)
	{
		final StringBuilder sb = new StringBuilder(
			ServerConfiguration.protocolVersion().versionIdentifier)
			.append(' ')
			.append(code)
			.append(' ')
			.append(name())
			.append("\r\n");

		for (final String header : headers)
		{
			sb.append(header).append("\r\n");
		}
		return sb.append("\r\n").toString();
	}

	/**
	 * Answer the {@link ByteBuffer} that contains the HTTP header of the {@link
	 * ResponseStatusCode}-specific response to the client.
	 *
	 * @param headers
	 *        An array of HTTP headers represented as Strings.
	 * @return A {@code ByteBuffer}.
	 */
	public @NotNull ByteBuffer responseBuffer (final String... headers)
	{
		return StandardCharsets.UTF_8.encode(responseHeader(headers));
	}

	/**
	 * Answer the {@link ByteBuffer} that contains the HTTP header of the {@link
	 * ResponseStatusCode}-specific response as well as a message body to the
	 * client.
	 *
	 * @param headers
	 *        An array of HTTP headers represented as Strings.
	 * @param messageBody
	 *        The message body in the form of an array of {@code byte}s.
	 * @return A {@code ByteBuffer}.
	 */
	public @NotNull ByteBuffer responseBuffer (
		final byte[] messageBody,
		final String... headers)
	{
		final byte[] headerBytes = responseHeader(headers)
			.getBytes(StandardCharsets.UTF_8);
		final byte[] response =
			new byte[headerBytes.length + messageBody.length];
		System.arraycopy(headerBytes, 0, response, 0, headerBytes.length);
		System.arraycopy(
			messageBody,
			0,
			response,
			headerBytes.length - 1,
			messageBody.length);
		return ByteBuffer.wrap(response);
	}

	/**
	 * Construct a {@link ResponseStatusCode}.
	 *
	 * @param code
	 *        The three-digit {@code integer} code giving the result of the
	 *        attempt to satisfy the request
	 */
	ResponseStatusCode (final int code)
	{
		this.code = code;
	}
}
