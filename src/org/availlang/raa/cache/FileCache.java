/*
 * FileCache.java
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

package org.availlang.raa.cache;

import com.sun.istack.internal.NotNull;
import org.availlang.raa.utilities.FileUtility;
import org.availlang.raa.utilities.ServerRuntime;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A {@code FileCache} caches files already read into memory for quicker
 * serving to for a client request.
 *
 * TODO Transform into an LRUCache
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public class FileCache
{
	/**
	 * The only {@link FileCache} for the application.
	 */
	private static final FileCache soleInstance = new FileCache();

	/**
	 * An object used for synchronization.
	 */
	private final Object lock = new Object();

	/**
	 * A {@link Map} from the String path of a file to the {@code byte} {@code
	 * array} containing the read bytes for that file.
	 */
	private final Map<String, byte[]> fileMap = new HashMap<>();

	/**
	 * The {@link Map} from the String path of a file to the {@link
	 * RequestHandler} responsible for retrieving that file.
	 */
	private final Map<String, RequestHandler> retrievalMap =
		new HashMap<>();

	/**
	 * Read a file for the provided {@link Path} and write its bytes to the
	 * provided {@link ByteBuffer}.
	 *
	 * @param path
	 *        The path where the file is located.
	 * @param success
	 *        A {@link BiConsumer} that accepts the {@code integer} value of
	 *        {@code bytes} read and the {@link ByteBuffer} containing the file
	 *        {@code bytes}, to be called upon successful reading of the file.
	 * @param failure
	 *        The {@link Consumer} that accepts a {@link Throwable} in the event
	 *        the file fails to be read.
	 */
	public static void readFile (
		final @NotNull String path,
		final @NotNull BiConsumer<Integer, byte[]> success,
		final @NotNull Consumer<Throwable> failure)
	{
		// Opportunistic read to avoid synchronization.
		byte[] fileBytes =
			soleInstance.fileMap.getOrDefault(path, null);
		if (fileBytes != null)
		{
			success.accept(fileBytes.length, fileBytes);
			return;
		}
		synchronized (soleInstance.lock)
		{
			fileBytes =
				soleInstance.fileMap.getOrDefault(path, null);
			if (fileBytes != null)
			{
				success.accept(fileBytes.length, fileBytes);
				return;
			}
			final OutstandingRequest request =
				new OutstandingRequest(success, failure);
			if (soleInstance.retrievalMap.containsKey(path))
			{
				final RequestHandler requestHandler =
					soleInstance.retrievalMap.get(path);
				assert requestHandler != null;
				requestHandler.requests.add(request);
				return;
			}
			final RequestHandler requestHandler =
				new RequestHandler(request, path);
			soleInstance.retrievalMap.put(path, requestHandler);
			ServerRuntime.scheduleTask(requestHandler::retrieveFile);
		}
	}

	/**
	 * An {@code OutstandingRequest} is a request from a client that is waiting
	 * to be fulfilled.
	 */
	private static class OutstandingRequest
	{
		/**
		 * A {@link BiConsumer} that accepts the {@code integer} value of {@code
		 * bytes} read and the {@link ByteBuffer} containing the file {@code
		 * bytes}, to be called upon successful reading of the file.
		 */
		final @NotNull BiConsumer<Integer, byte[]> success;

		/**
		 * The {@link Consumer} that accepts a {@link Throwable} in the event
		 * the file fails to be read.
		 */
		final @NotNull Consumer<Throwable> failure;

		/**
		 * Construct an {@link OutstandingRequest}.
		 *
		 * @param success
		 *        A {@link BiConsumer} that accepts the {@code integer} value of
		 *        {@code bytes} read and the {@link ByteBuffer} containing the
		 *        file {@code bytes}, to be called upon successful reading of
		 *        the file.
		 * @param failure
		 *        The {@link Consumer} that accepts a {@link Throwable} in the
		 *        event the file fails to be read.
		 */
		private OutstandingRequest (
			final BiConsumer<Integer, byte[]> success,
			final Consumer<Throwable> failure)
		{
			this.success = success;
			this.failure = failure;
		}
	}

	/**
	 * A {@code RequestHandler} holds {@link OutstandingRequest}s for a specific
	 * file while the requested file is retrieved.
	 */
	private static class RequestHandler
	{
		/**
		 * A {@link List} of {@link OutstandingRequest}s waiting for the
		 * {@linkplain #path file} to be read from disk.
		 */
		private final List<OutstandingRequest> requests = new ArrayList<>();

		/**
		 * The String path to the requested file.
		 */
		private final String path;

		/**
		 * Retrieve the file and process all the {@link OutstandingRequest}s.
		 */
		private void retrieveFile ()
		{
			try
			{
				final Path requestedFile = ServerRuntime.filePath(path);
				final ByteBuffer buffer = ByteBuffer.allocate(1024);
				FileUtility.readFile(
					requestedFile,
					buffer,
					(bytesRead, fileBuffer) ->
					{
						final byte[] copy;
						synchronized (soleInstance.lock)
						{
							copy = Arrays.copyOf(
								fileBuffer.array(), fileBuffer.position());
							soleInstance.fileMap.put(path, copy);
							soleInstance.retrievalMap.remove(path);
						}
						for (final OutstandingRequest request : requests)
						{
							ServerRuntime.scheduleTask(() ->
								request.success.accept(
									fileBuffer.limit(), copy));
						}
					},
					throwable ->
					{
						for (final OutstandingRequest request : requests)
						{
							ServerRuntime.scheduleTask(() ->
								request.failure.accept(throwable));
						}
					});
			}
			catch (final FileNotFoundException e)
			{
				for (final OutstandingRequest request : requests)
				{
					ServerRuntime.scheduleTask(() ->
						request.failure.accept(e));
				}
			}
		}

		/**
		 * Construct a {@link RequestHandler}.
		 *
		 * @param request
		 *        The initial {@link OutstandingRequest}.
		 * @param path
		 *        The location of the file to retrieve.
		 */
		private RequestHandler (
			final OutstandingRequest request,
			final String path)
		{
			this.requests.add(request);
			this.path = path;
		}
	}
}
