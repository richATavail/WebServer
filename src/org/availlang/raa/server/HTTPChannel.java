/*
 * HTTPChannel.java
 * Copyright © 2018, Richard A. Arriaga
 * All rights reserved.
 */

package org.availlang.raa.server;
import org.availlang.raa.protocol.ProtocolVersion;
import org.availlang.raa.protocol.http.HTTPRequest;
import org.availlang.raa.protocol.http.ResponseStatusCode;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;

/**
 * A {@code HTTPChannel} is a {@link Channel} specifically for the {@link
 * ProtocolVersion} {@link ProtocolVersion#HTTP_1_1}.
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public class HTTPChannel
extends Channel
{
	@Override
	protected ProtocolVersion protocolVersion ()
	{
		return ProtocolVersion.HTTP_1_1;
	}

	/**
	 * Process a {@link ResponseStatusCode#BAD_REQUEST}.
	 */
	public void badRequest ()
	{
		write(ResponseStatusCode.BAD_REQUEST);
	}

	/**
	 * Write the {@link ByteBuffer} to the provided {@link
	 * AsynchronousSocketChannel}.
	 *
	 * @param code
	 *        The {@link ResponseStatusCode} that informs the client the result
	 *        of the request.
	 * @param payload
	 *        The {@code ByteBuffer} that contains the data to send to the
	 *        connected client.
	 * @param handler
	 *        A {@link CompletionHandler} that is parameterized by the {@code
	 *        integer} representing the {@code bytes} written and a void
	 *        attachment.
	 */
	public void write (
		final ResponseStatusCode code,
		final byte[] payload,
		final CompletionHandler<Integer, Void> handler)
	{
		final ByteBuffer message = code.responseBuffer(payload);
		channel.write(message, null, handler);
	}

	/**
	 * Write the {@link ResponseStatusCode#responseBuffer(String...)}  to the
	 * provided {@link AsynchronousSocketChannel} and {@linkplain #close()} the
	 * connection.
	 *
	 * @param code
	 *        The {@link ResponseStatusCode} that informs the client the result
	 *        of the request.
	 */
	public void write (final ResponseStatusCode code)
	{
		channel.write(
			code.responseBuffer(),
			null,
			new CompletionHandler<Integer, Object>()
			{
				@Override
				public void completed (
					final Integer result,
					final Object attachment)
				{
					close();
				}

				@Override
				public void failed (
					final Throwable exc,
					final Object attachment)
				{
					close();
				}
			});
	}

	@Override
	protected void handle ()
	{
		final ByteBuffer buffer = ByteBuffer.allocate(1024);
		read(
			buffer,
			new CompletionHandler<Integer, Void>()
			{
				@Override
				public void completed (
					final Integer result,
					final Void attachment)
				{
					final String message = result > 0
						? new String(
								buffer.array(),
							0,
							result,
							StandardCharsets.UTF_8)
						: new String(buffer.array(), StandardCharsets.UTF_8);
					if (message.isEmpty())
					{
						badRequest();
						return;
					}
					HTTPRequest.processRequest(message, HTTPChannel.this);
				}

				@Override
				public void failed (
					final Throwable exc,
					final Void attachment)
				{
					// TODO do some logging?
					exc.printStackTrace();
				}
			});

	}

	/**
	 * Process the {@link ProtocolVersion#HTTP_1_1 HTTP} request for the
	 * provided {@link AsynchronousSocketChannel}.
	 *
	 * @param channel
	 *        The {@code AsynchronousSocketChannel} to read/write data from/to
	 * 	      the client.
	 */
	public static void handleChannel (final AsynchronousSocketChannel channel)
	{
		new HTTPChannel(channel).handle();
	}

	/**
	 * Construct a {@link HTTPChannel}.
	 *
	 * @param channel
	 *        The {@code AsynchronousSocketChannel} to read/write data from/to
	 * 	      the client.
	 */
	private HTTPChannel (final AsynchronousSocketChannel channel)
	{
		super(channel);
	}
}
