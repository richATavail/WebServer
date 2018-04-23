OVERVIEW
--------------------------------------------------------------------------------
This project is a work-in-progress implementation of a Web Server. The web
server is designed to support more than just the standard HTTP 1.1 protocol. At
present, only HTTP 1.1 is partially implemented.

The server uses a properties file for server configuration. An example of a
configuration can be seen in config/server.properties. The properties file
enables the following configurations:

	* host - (required) this is the host name/ IP address
	* port - (required) the port the server will listen on for connections
	* protocolVersion - (required) the version of the protocol being used, for
		example: HTTP/1.1
	* siteDirectory - (required) the base directory of the web files (html) on
		disk.
	* homePage - (optional) the location of the home page.
			For example: home.html or some_directory/index.html
		If not listed it defaults to index.html.

PLANNED DEVELOPMENT
--------------------------------------------------------------------------------
The following are some ideas that I have for further development:
	* transform file caching to an LRUCache to keep memory pressure down
	* JSON parsing support
	* XML parsing support
	* expanded HTTP protocol method support for POST requests
