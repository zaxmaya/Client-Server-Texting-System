## Overview
This Java chat application is comprised of two main components: `Server.java` and `Client.java`. The server is responsible for handling incoming connections from clients and managing communication between them. The client establishes a connection to the server and allows users to send and receive messages.

## Server.java
**Description**: This class sets up a server socket and listens for incoming client connections. It uses an `ExecutorService` for handling multiple client threads concurrently. The server maintains a list of active client handlers to manage client communications.

**Key Features**:
  - Concurrent client handling with thread pool.
  - Synchronized list for managing client handlers.
  - Handling of client messages and broadcasting to other clients.

## Client.java
**Description**: This class represents the client-side application that connects to the server. It is responsible for sending and receiving messages from the server.

**Key Features**:
  - Connection setup with the server using IP address and port.
  - Sending messages to the server.
  - Receiving messages from the server.

## Requirements
- Java Development Kit (JDK)
- Network access for client-server communication

## How to Run
1. **Server**:
   - Compile: `javac Server.java`
   - Run: `java Server`
   - The server will start and listen for client connections.

2. **Client**:
   - Compile: `javac Client.java`
   - Run: `java Client`
   - Upon running, enter the server's IP address and port number to connect.

## Usage
- **Server**: Run the server program first. It will start and wait for clients to connect.
- **Client**: Run the client program and connect to the server using the correct IP address and port. Once connected, you can start sending messages to the server, which will be broadcast to other connected clients.

## Notes
- Ensure that the server is running before clients attempt to connect.
- The server and clients must be on the same network for local communication, or the server must be publicly accessible for remote communication.
