/*
 * Channel.java
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
import org.availlang.raa.protocol.ProtocolVersion;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * A {@code Channel} is an abstract class that provides common state and methods
 * for managing {@link ProtocolVersion}-specific implementations for managing
 * communication between the {@link Server} and the client.
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public abstract class Channel
implements AutoCloseable
{
	/**
	 * The {@code AsynchronousSocketChannel} to read/write data from/to the
	 * client.
	 */
	final AsynchronousSocketChannel channel;

	@Override
	public void close ()
	{
		try
		{
			if (channel.isOpen())
			{
				channel.close();
			}
		}
		catch (final Exception e)
		{
			// Do nothing
		}
	}

	/**
	 * Answer the {@link ProtocolVersion} this {@link Channel} implements.
	 *
	 * @return A  {@code ProtocolVersion}.
	 */
	protected abstract ProtocolVersion protocolVersion ();

	/**
	 * Write the {@link ByteBuffer} to the provided {@link
	 * AsynchronousSocketChannel}.
	 *
	 * @param payload
	 *        The {@code ByteBuffer} that contains the data to send to the
	 *        connected client.
	 * @param handler
	 *        A {@link CompletionHandler} that is parameterized by the {@code
	 *        integer} representing the {@code bytes} written and a void
	 *        attachment.
	 */
	void write (
		final ByteBuffer payload,
		final CompletionHandler<Integer, Void> handler)
	{
		channel.write(payload, null, handler);
	}

	/**
	 * Read data from the {@link AsynchronousSocketChannel} into the provided
	 * {@link ByteBuffer}.
	 *
	 * @param target
	 *        The {@code ByteBuffer} to read the client message into.
	 * @param handler
	 *        A {@link CompletionHandler} that is parameterized by the {@code
	 *        integer} representing the {@code bytes} read and a void
	 *        attachment.
	 */
	void read (
		final ByteBuffer target,
		final CompletionHandler<Integer, Void> handler)
	{
		channel.read(target, null, handler);
	}

	/**
	 * Handle the request coming in on the {@link #channel}.
	 */
	protected abstract void handle ();

	/**
	 * Construct a {@link Channel}.
	 *
	 * @param channel
	 *        The {@code AsynchronousSocketChannel} to read/write data from/to
	 *        the client.
	 */
	Channel (final AsynchronousSocketChannel channel)
	{
		this.channel = channel;
	}
}
