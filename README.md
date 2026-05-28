# Multi-Client Chat Application

A TCP-based client-server chat app in Java using socket programming. Supports multiple concurrent clients with SHA-256 authentication, broadcast messaging, private messaging, and server logging. Demonstrates networking fundamentals: TCP communication, multithreading, error handling, and concurrent client management on localhost:5000.

**Features:**
- Multi-threaded server handling concurrent clients
- User authentication with hashed passwords
- Broadcast & private messaging
- Server logging with timestamps & IP tracking
- Graceful error handling & connection management

**Commands:**
- `/broadcast <message>` - send to all users
- `/pm <username> <message>` - private message
- `/quit` - disconnect

**Default Users:** ammar/1234, abdullah/1234, faizan/1234
