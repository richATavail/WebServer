/*
 * ServerConfiguration.java
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

package org.availlang.raa.configuration;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.availlang.raa.exceptions.PropertiesException;
import org.availlang.raa.protocol.ProtocolVersion;
import org.availlang.raa.server.Server;
import org.availlang.raa.utilities.FileUtility;
import org.availlang.raa.utilities.ServerRuntime.ExitCode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.Properties;

/**
 * A {@code ServerConfiguration} contains all the configurable components for
 * the {@link Server}.
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public class ServerConfiguration
{
	/**
	 * An enum of all the properties in this application's {@link Properties}.
	 */
	enum PropertyKey
	{
		/**
		 * The {@linkplain InetSocketAddress#getHostName() host name} properties
		 * key.
		 */
		@SuppressWarnings("unused")
		HOST("host")
		{
			@Override
			void updateConfig ()
			throws PropertiesException
			{
				final String rawValue = soleInstance.getProperty(this);
				if (rawValue == null)
				{
					cannotFindValue();
				}
				soleInstance.host = rawValue;
			}
		},

		/**
		 * The {@linkplain InetSocketAddress#getPort() port} properties key.
		 */
		@SuppressWarnings("unused")
		PORT("port")
		{
			/**
			 * The minimum (inclusive) port value allowed.
			 */
			private final int PORT_MIN = 1;

			/**
			 * The maximum (inclusive) port value allowed.
			 */
			private final int PORT_MAX = 65535;

			@Override
			void updateConfig ()
			throws PropertiesException
			{
				final String rawValue = soleInstance.getProperty(this);
				if (rawValue == null)
				{
					cannotFindValue();
				}
				try
				{
					assert rawValue != null;
					soleInstance.port = Integer.parseInt(rawValue);
				}
				catch (final NumberFormatException e)
				{
					throw new PropertiesException(String.format(
						"%s is not a valid value for %s, expects [%d, %d]",
						rawValue,
						key,
						PORT_MIN,
						PORT_MAX));
				}
			}
		},

		/**
		 * The {@link ProtocolVersion} being used; e.g. {@code "HTTP/1.1"} for
		 * {@link ProtocolVersion#HTTP_1_1}.
		 */
		@SuppressWarnings("unused")
		PROTOCOL_VERSION("protocolVersion")
		{
			@Override
			void updateConfig ()
			throws PropertiesException
			{
				final String rawValue = soleInstance.getProperty(this);
				if (rawValue == null)
				{
					cannotFindValue();
				}
				assert rawValue != null;
				if (ProtocolVersion.isInvalidProtocol(rawValue))
				{
					throw new PropertiesException(String.format(
						"%s is not a valid protocol version. Can use:%n%s",
						rawValue,
						ProtocolVersion.validVersions()));
				}
				soleInstance.protocolVersion =
					ProtocolVersion.protocolVersion(rawValue);
			}
		},

		/**
		 * The location
		 */
		@SuppressWarnings("unused")
		SITE_DIRECTORY("siteDirectory")
		{
			@Override
			void updateConfig ()
			throws PropertiesException
			{
				final String rawValue = soleInstance.getProperty(this);
				if (rawValue == null)
				{
					cannotFindValue();
					return;
				}
				if (!FileUtility.directoryExists(rawValue))
				{
					System.err.println(String.format(
						"%s is not a valid directory", rawValue));
					ExitCode.APPLICATION_DATA_ISSUE.shutdown();
				}
				soleInstance.siteDirectory = rawValue;
			}
		},

		/**
		 * The location of the site's home page.
		 */
		@SuppressWarnings("unused")
		HOME_PAGE("homePage")
		{
			@Override
			void updateConfig ()
			throws PropertiesException
			{
				final String rawValue = soleInstance.getProperty(this);
				if (rawValue == null)
				{
					return;
				}
				if (rawValue.isEmpty())
				{
					System.err.println(String.format(
						"%s cannot have an empty string as a value", key));
					ExitCode.APPLICATION_DATA_ISSUE.shutdown();
				}
				soleInstance.homePage = File.separator + rawValue;
			}
		};

		/**
		 * The key to access the related field in the {@link
		 * ServerConfiguration#properties}.
		 */
		final String key;

		/**
		 * Update the provided {@link ServerConfiguration#soleInstance} with the
		 * appropriate value represented by this {@link PropertyKey}.
		 *
		 * @throws PropertiesException Thrown if the value cannot be set.
		 */
		abstract void updateConfig ()
		throws PropertiesException;

		/**
		 * Populate the {@link ServerConfiguration} for all the {@link
		 * PropertyKey}s.
		 */
		static void populateConfig ()
		{
			try
			{
				for (final PropertyKey pk : values())
				{
					pk.updateConfig();
				}
			}
			catch (final PropertiesException e)
			{
				e.printStackTrace();
				e.exitCode.shutdown();
			}
		}

		void cannotFindValue ()
		{
			throw new PropertiesException(String.format(
				"Could not retrieve value for %s", this.key));
		}

		/**
		 * Construct a {@link PropertyKey}.
		 *
		 * @param key
		 *        The key to access the related field in the {@link
		 *        ServerConfiguration#properties}.
		 */
		PropertyKey (final String key)
		{
			this.key = key;
		}
	}

	/**
	 * Answer the sole {@link ServerConfiguration} used by this application.
	 */
	private static final ServerConfiguration soleInstance =
		new ServerConfiguration();

	/** The location of the properties file. */
	private String appConfigPath = "config/server.properties";

	/**
	 * Set the location of the properties file to configure this application.
	 *
	 * @param appConfigPath
	 *        A String location.
	 */
	public static void setAppConfigPath (final String appConfigPath)
	{
		soleInstance.appConfigPath = appConfigPath;
	}

	/**
	 * The application's {@link Properties}.
	 */
	private final Properties properties = new Properties();

	/**
	 * The host name of the {@link InetSocketAddress}.
	 */
	private @Nullable String host;

	/**
	 * The prot of the {@link InetSocketAddress}.
	 */
	private int port = -1;

	/**
	 * The {@link InetSocketAddress} that is to be {@linkplain
	 * AsynchronousServerSocketChannel#bind(SocketAddress) bound} to the {@link
	 * Server}.
	 */
	private @Nullable InetSocketAddress address;

	/**
	 * Answer the {@link #address}.
	 *
	 * @return An {@link InetSocketAddress}.
	 */
	public static @NotNull InetSocketAddress address ()
	{
		final InetSocketAddress a = soleInstance.address;
		assert a != null : "There should be a populated address!";
		return a;
	}

	/**
	 * The expected {@link ProtocolVersion} for the running application.
	 */
	private @Nullable ProtocolVersion protocolVersion;

	/**
	 * Answer the expected {@link ProtocolVersion} for the running application.
	 *
	 * @return A {@code ProtocolVersion}.
	 */
	public static @NotNull ProtocolVersion protocolVersion ()
	{
		final ProtocolVersion v = soleInstance.protocolVersion;
		assert v != null : "protocolVersion must not be null!";
		return v;
	}

	/**
	 * The location of the site to be served.
	 */
	private @Nullable String siteDirectory;

	/**
	 * Answer the location of the site to be served.
	 *
	 * @return A String.
	 */
	public static @NotNull String siteDirectory ()
	{
		final String dir = soleInstance.siteDirectory;
		assert dir != null : "siteDirectory must not be null!";
		return dir;
	}

	/**
	 * The location of the home page.
	 */
	private @NotNull String homePage = "index.html";

	/**
	 * Answer the location of the home page.
	 *
	 * @return A String.
	 */
	public static @NotNull String homePage ()
	{
		return soleInstance.homePage;
	}

	/**
	 * Retrieve the properties file and answer a {@linkplain
	 * Properties#load(InputStream) loaded} {@link Properties}.
	 *
	 * @throws PropertiesException When the properties file cannot be accessed.
	 */
	private void retrieveProperties ()
	throws PropertiesException
	{
		if (!propertiesFileExists())
		{
			System.err.printf(
				"Could not locate properties file, %s%n",
				appConfigPath);
			ExitCode.IO_EXCEPTION.shutdown();
		}
		try (final FileInputStream fileInputStream =
			     new FileInputStream(appConfigPath))
		{
			properties.load(fileInputStream);
		}
		catch (final IOException e)
		{
			throw new PropertiesException(
				"Could not access properties file", e);
		}
	}

	/**
	 * Check to see if there is a {@link Properties} file in the resources
	 * folder.
	 *
	 * @return {@code true} the file exists; {@code false} otherwise.
	 */
	private static boolean propertiesFileExists ()
	{
		return FileUtility.fileExists(soleInstance.appConfigPath);
	}

	/**
	 * Answer the String property for the given {@link PropertyKey}.
	 *
	 * @param key
	 *        The {@code PropertyKey} to retrieve the value for.
	 * @return A String if the property exists; {@code null} otherwise.
	 */
	private @Nullable String getProperty (final @NotNull PropertyKey key)
	{
		return properties.getProperty(key.key);
	}

	/**
	 * Update the application's {@link #properties} with the properties file on
	 * disk.
	 *
	 * <p>
	 * This could evolve into something arbitrarily complex if more properties
	 * were to develop, however as it stands only host and port storage is
	 * needed to survive between running instances of the application.
	 * </p>
	 */
	public static void retrieveConfigInfo () throws PropertiesException
	{
		if (soleInstance.properties.isEmpty())
		{
			soleInstance.retrieveProperties();
		}
		PropertyKey.populateConfig();
		try
		{
			soleInstance.address = new InetSocketAddress(
				soleInstance.host, soleInstance.port);
		}
		catch (final NumberFormatException e)
		{
			e.printStackTrace();
			ExitCode.APPLICATION_DATA_ISSUE.shutdown();
		}
	}
}
