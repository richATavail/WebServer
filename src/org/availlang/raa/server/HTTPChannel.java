/*
 * HTTPChannel.java
 * Copyright © 2018, Richard A. Arriaga
 * All rights reserved.
 */

package org.availlang.raa.server;
import org.availlang.raa.cache.FileCache;
import org.availlang.raa.configuration.ServerConfiguration;
import org.availlang.raa.exceptions.InvalidProtocolMethod;
import org.availlang.raa.protocol.ProtocolVersion;
import org.availlang.raa.protocol.http.HTTPProtocolMethod;
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
	private void badRequest ()
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
	private void write (
		final ResponseStatusCode code,
		final ByteBuffer payload,
		final CompletionHandler<Integer, Void> handler)
	{
		final ByteBuffer header =
			StandardCharsets.UTF_8.encode(code.responseHeader());
		channel.write(
			header,
			null,
			new CompletionHandler<Integer, Object>()
			{
				@Override
				public void completed (
					final Integer result,
					final Object attachment)
				{
					write(payload, handler);
				}

				@Override
				public void failed (
					final Throwable exc,
					final Object attachment)
				{
					handler.failed(exc, null);
				}
			});
	}

	/**
	 * Write the {@link ResponseStatusCode#responseHeader()} to the provided
	 * {@link AsynchronousSocketChannel} and {@linkplain #close()} the
	 * connection.
	 *
	 * @param code
	 *        The {@link ResponseStatusCode} that informs the client the result
	 *        of the request.
	 */
	private void write (final ResponseStatusCode code)
	{
		channel.write(
			StandardCharsets.UTF_8.encode(code.responseHeader()),
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
					final String[] headers = message.split("(?:\r\n)+");
					System.out.println(message);
					final String[] protocolRequest =
						headers[0].split(" +");
					if (protocolRequest.length != 3)
					{
						badRequest();
						return;
					}
					final String pv = protocolRequest[2];
					if (ProtocolVersion.isInvalidProtocol(pv))
					{
						write(ResponseStatusCode.HTTP_VERSION_NOT_SUPPORTED);
						return;
					}
					final ProtocolVersion protocolVersion =
						ProtocolVersion.protocolVersion(pv);
					final HTTPProtocolMethod method;
					try
					{
						method = (HTTPProtocolMethod)
							protocolVersion.protocolMethod(protocolRequest[0]);
					}
					catch (final InvalidProtocolMethod e)
					{
						write(ResponseStatusCode.METHOD_NOT_ALLOWED);
						return;
					}

					switch (method)
					{
						case GET:
						{
							final String location =
								protocolRequest[1].equals("/")
									? ServerConfiguration.homePage()
									: protocolRequest[1];

							FileCache.readFile(
								location,
								(bytesRead, fileBuffer) ->
									write(
										ResponseStatusCode.OK,
										fileBuffer,
										new CompletionHandler<Integer, Void>()
										{
											@Override
											public void completed (
												final Integer result,
												final Void attachment)
											{
												close();
											}

											@Override
											public void failed (
												final Throwable exc,
												final Void attachment)
											{
												// TODO log?
												close();
											}
										}),
								throwable ->
									write(ResponseStatusCode.NOT_FOUND));

							break;
						}
						default:
							write(ResponseStatusCode.NOT_IMPLEMENTED);
							break;
					}
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
