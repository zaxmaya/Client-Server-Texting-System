Overview
This Java chat application comprises two main components: Server.java and Client.java. The Server handles incoming connections from Clients, managing communication between them. The Client establishes a connection to the Server and enables users to send and receive messages.

Components
Server.java
Description:

Sets up a server socket and listens for incoming client connections.
Uses ExecutorService for handling multiple client threads concurrently.
Maintains a list of active client handlers to manage client communications.
Key Features:

Concurrent client handling with a thread pool.
Synchronized list for managing client handlers.
Handling of client messages and broadcasting them to other clients.
Client.java
Description:

Represents the client-side application that connects to the Server.
Handles sending and receiving messages from the Server.
Key Features:

Connection setup with the Server using IP address and port.
Sending messages to the Server.
Receiving messages from the Server.
Requirements
Java Development Kit (JDK)
Network access for client-server communication
How to Run
Server:

Compile: javac Server.java
Run: java Server
The Server will start and listen for client connections.
Client:

Compile: javac Client.java
Run: java Client
Upon running, enter the Server's IP address and port number to connect.
Usage
Server: Run the Server program first. It will start and wait for Clients to connect.
Client: Run the Client program and connect to the Server using the correct IP address and port. Once connected, start sending messages to the Server, which will be broadcast to other connected clients.
Notes
Ensure the Server is running before Clients attempt to connect.
The Server and Clients must be on the same network for local communication, or the Server must be publicly accessible for remote communication
