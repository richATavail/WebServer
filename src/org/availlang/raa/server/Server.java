/*
 * Server.java
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

package org.availlang.raa.server;
import com.sun.istack.internal.NotNull;
import org.availlang.raa.configuration.ServerConfiguration;
import org.availlang.raa.utilities.ServerRuntime.ExitCode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

/**
 * A {@code Server} is a wrapper for an {@link AsynchronousServerSocketChannel},
 * which serves up {@link AsynchronousSocketChannel}s.
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public class Server
{
	/**
	 * The {@link AsynchronousServerSocketChannel} that listens for incoming
	 * connection.
	 */
	private final @NotNull AsynchronousServerSocketChannel server;

	/**
	 * Handle incoming connections.
	 */
	public void handleConnections ()
	{
		server.accept(
			null,
			new CompletionHandler<AsynchronousSocketChannel, Void>()
			{
				@Override
				public void completed (
					final AsynchronousSocketChannel channel,
					final Void attachment)
				{
					// need to accept next connection
					server.accept(attachment, this);

					// handle the channel
					ServerConfiguration.protocolVersion()
						.handleChannel(channel);
				}

				@Override
				public void failed (final Throwable exc, final Void attachment)
				{
					exc.printStackTrace();
				}
			}
		);
	}

	/**
	 * Construct a {@link Server}.
	 *
	 * @param address
	 *        The {@link InetSocketAddress} to {@linkplain
	 *        AsynchronousServerSocketChannel#bind(SocketAddress) bind} the
	 *        server to to listen for incoming connections.
	 */
	public Server (final InetSocketAddress address)
	{
		AsynchronousServerSocketChannel s = null;
		try
		{
			s = AsynchronousServerSocketChannel.open(
				AsynchronousChannelGroup.withThreadPool(
					new ThreadPoolExecutor(
						Runtime.getRuntime().availableProcessors(),
						Runtime.getRuntime().availableProcessors() << 2,
						10L,
						TimeUnit.SECONDS,
						new LinkedBlockingQueue<>(),
						runnable ->
						{
							final Thread thread = new Thread(runnable);
							thread.setDaemon(true);
							return thread;
						},
						new AbortPolicy())));
			s.bind(address);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			ExitCode.IO_EXCEPTION.shutdown();
		}
		this.server = s;
	}
}
