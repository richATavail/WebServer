/*
 * FileUtility.java
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

package org.availlang.raa.utilities;

import com.sun.istack.internal.NotNull;
import org.availlang.raa.exceptions.ApplicationException;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A {@code FileUtility} is a class that contains static helper methods for
 * interacting with the file system.
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
@SuppressWarnings("WeakerAccess")
public class FileUtility
{
	/**
	 * Answer the platform-specific path for the provided String path.
	 *
	 * @param path
	 *        A String directory/file path.
	 * @return A String.
	 */
	public static @NotNull String platformAppropriatePath (
		final @NotNull String path)
	{
		final String[] pathArray = path.contains("/")
			? path.split("/")
			: path.split("\\\\");
		return String.join(File.separator, pathArray);
	}

	/**
	 * Check to see if the provided file path exists.
	 *
	 * @return {@code true} the file exists; {@code false} otherwise.
	 */
	public static boolean fileExists (final @NotNull String path)
	{
		final File f = new File(platformAppropriatePath(path));
		return f.exists() && !f.isDirectory();
	}

	/**
	 * Check to see if the provided directory exists.
	 *
	 * @return {@code true} the file exists; {@code false} otherwise.
	 */
	public static boolean directoryExists (final @NotNull String path)
	{
		final File f = new File(platformAppropriatePath(path));
		return f.exists() && f.isDirectory();
	}

	/**
	 * Read a file for the provided {@link Path} and write its bytes to the
	 * provided {@link ByteBuffer}.
	 *
	 * @param path
	 *        The {@code Path} where the file is located.
	 * @param targetBuffer
	 *        The {@link ByteBuffer} to
	 * @param success
	 *        A {@link BiConsumer} that accepts the {@code integer} value of
	 *        {@code bytes} read and the {@link ByteBuffer} containing the file
	 *        {@code bytes}, to be called upon successful reading of the file.
	 * @param failure
	 *        The {@link Consumer} that accepts a {@link Throwable} in the event
	 *        the file fails to be read.
	 */
	public static void readFile (
		final @NotNull Path path,
		final @NotNull ByteBuffer targetBuffer,
		final @NotNull BiConsumer<Integer, ByteBuffer> success,
		final @NotNull Consumer<Throwable> failure)
	{
		final AsynchronousFileChannel fileChannel;
		try
		{
			fileChannel =
				AsynchronousFileChannel.open(path, StandardOpenOption.READ);
			final CompletionHandler<Integer, ByteBuffer> handler =
				new CompletionHandler<Integer, ByteBuffer>()
				{
					// Close the open file
					private void close ()
					{
						if (fileChannel != null && fileChannel.isOpen())
						{
							try
							{
								fileChannel.close();
							}
							catch (final IOException e)
							{
								// Do nothing
							}
						}
					}
					@Override
					public void completed (
						final Integer result,
						final ByteBuffer attachment)
					{
						close();
						success.accept(result, attachment);
					}

					@Override
					public void failed (
						final Throwable exc,
						final ByteBuffer attachment)
					{
						close();
						failure.accept(exc);
					}
				};
			fileChannel.read(
				targetBuffer, 0, targetBuffer, handler);
		}
		catch (final IOException e)
		{
			throw new ApplicationException(
				String.format("Could not open %s", path.toString()),
				e);
		}
	}

	/**
	 * No instances of {@link FileUtility} should be constructed.
	 */
	private FileUtility () { /* Do Nothing */ }
}
