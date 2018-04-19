/*
 * ServerRuntime.java
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
import org.availlang.raa.configuration.ServerConfiguration;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

/**
 * A {@code ServerRuntime} contains the static runtime components of a
 * single running Application. It manages execution of the application.
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public class ServerRuntime
{
	/**
	 * {@code ExitCode} abstracts the allowed values for calls of {@link
	 * System#exit(int)}.
	 *
	 * <ol start="0">
	 *     <li>Normal exit:   {@link #NORMAL_EXIT}</li>
	 *     <li>Abnormal exit: {@link #UNSPECIFIED_ERROR}</li>
	 *     <li>Abnormal exit: {@link #APPLICATION_DATA_ISSUE}</li>
	 *     <li>Abnormal exit: {@link #UNSPECIFIED_ERROR}</li>
	 * </ol>
	 */
	public enum ExitCode
	{
		/** Normal exit. */
		NORMAL_EXIT (0),

		/** An unexpected error. */
		IO_EXCEPTION(1),

		/**
		 * When data required for the full operation of this application isn't
		 * in an expected state.
		 */
		APPLICATION_DATA_ISSUE(2),

		/** An unexpected error. */
		UNSPECIFIED_ERROR(3);

		/**
		 * The status code for {@link System#exit(int)}.
		 */
		private final int status;

		/**
		 * Shutdown the application with this {@link ExitCode}.
		 */
		public void shutdown ()
		{
			System.exit(status);
		}

		/**
		 * Construct an {@link ExitCode}.
		 *
		 * @param status
		 *        The status code for {@link System#exit(int)}.
		 */
		ExitCode (int status)
		{
			this.status = status;
		}
	}

	/**
	 * The {@link ThreadPoolExecutor} used by this application.
	 */
	private static final ThreadPoolExecutor threadPoolExecutor =
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
			new AbortPolicy());

	/**
	 * Schedule the provided {@link Runnable} with the {@link
	 * #threadPoolExecutor}.
	 *
	 * @param r
	 *        The {@link Runnable} to execute.
	 */
	public static void scheduleTask (final Runnable r)
	{
		threadPoolExecutor.execute(r);
	}

	/**
	 * The {@link Semaphore} that is used to keep the application from shutting
	 * down at the end of the a {@code main} method.
	 */
	private final static Semaphore applicationSemaphore =
		new Semaphore(0);

	/**
	 * Block the main thread of execution from completing.
	 */
	public static void block ()
	{
		try
		{
			applicationSemaphore.acquire();
		}
		catch (final InterruptedException e)
		{
			ExitCode.NORMAL_EXIT.shutdown();
		}
	}

	/**
	 * Answer the {@link Path} for the given requested file.
	 *
	 * @param fileLocation
	 *        The {@link ServerConfiguration#siteDirectory()} - path relative
	 *        String file.
	 * @return A {@code Path} if the file exists.
	 * @throws FileNotFoundException Thrown if the file does not exist.
	 */
	public static @NotNull Path filePath (final @NotNull String fileLocation)
	throws FileNotFoundException
	{
		final String filePath =
			FileUtility.platformAppropriatePath(
				ServerConfiguration.siteDirectory() + fileLocation);
		if (!FileUtility.fileExists(filePath))
		{
			throw new FileNotFoundException("Could not find: " + fileLocation);
		}
		return Paths.get(filePath);
	}

	// Never call as should not be implemented
	private ServerRuntime () { }
}
