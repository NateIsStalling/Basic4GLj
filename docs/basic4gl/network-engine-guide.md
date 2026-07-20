
# Programmer's Guide: The Basic4GL Network Engine

## Custom network protocol

Basic4GL does not support regular TCP/IP networking, or common protocols such as ftp or http.


The Basic4GL network routines use their own custom protocol, built on top of UDP/IP. You can use it to write programs that network with other Basic4GL programs, but not (for example) to write an FTP client.

> [!IMPORTANT]
>
> IP v6 not supported
> Please be aware that IP v6 is not supported at this time.


The Basic4GL network engine is designed primarily for writing low latency networked games. It allows you to establish a network connection between two running Basic4GL programs and send blocks of data (which we call "messages") back and forth. Basic4GL will attempt to send these messages as quickly as possible (with some extra logic for reliability and/or ordering if required).

And that's all it does.

It is up to you to decide what data to send, how to send it, and how often. But with a bit of planning it is possible to write some responsive multiplayer lan or internet games without too much fuss.

The network engine uses UDP/IP packets for communication. Any network that can do TCP/IP can do UDP/IP (as TCP is built on top of UDP), so your programs can run over the Internet and TCP/IP local networks.

Basic4GL uses its own protocol for handling connection lifetime and reliable packet delivery which is optimised towards writing responsive networked games. It lets you choose which messages must get through and which ones don't matter if they get lost on the way. You can also choose which messages must arrive in the same order they were sent and which ones don't matter so much. This upshot of this is that a carefully designed application will have the best chance to be able to continue smoothly if a data packet is lost in transmission, which is important for realtime games. (Unlike TCP/IP which has to stop for a few seconds if it hits an error). The downside is because the Basic4GL network engine uses its own protocol it can only talk to other applications using the same protocol, i.e. other Basic4GL programs.

The network engine supports:

- Automatic connection lifetime handling (using "timeouts" and "keep-alives")
- Reliable/unreliable messages
- Ordered/unordered messages
- Automatic message fragmentation and reassembly
- Optional "message smoothing" compensates for varying network latency to ensure messages are received and applied smoothly.

## Reading/writing messages with File I/O functions

The bulk of a program's network code usually involves:

- Writing some data to a "message", then sending down a network connection.
- Receiving a "message" from a network connection and reading the data inside it.

A network message is a block of data, similar to a small disk file. In fact Basic4GL uses the file I/O functions to read and write the contents of network messages. Instead of using OpenFileWrite and OpenFileRead, you use SendMessage and ReceiveMessage, but otherwise it's just like accessing a disk file.

Compare this program to write a simple text file:

```
dim file
file = OpenFileWrite("files\test.txt") ' Open a file for output
WriteString(file, "Some text")         ' Write some text
CloseFile(file)                        ' Close the file
```

With this program to send a message over a network connection:

```
dim msg
...
msg = SendMessage(connection)          ' Create a message to send down connection
WriteString(msg, "Some text")          ' Write some text
CloseFile(msg)                         ' Send the message
```

(Note: The above program is incomplete...)

All of Basic4GL's file I/O functions except `OpenFileRead` and `OpenFileWrite` can be used with Basic4GL network messages.

These functions are described in the File I/O section of the Basic4GL Programmer's Guide.

## Server-client connections

Two connect two computer over a network, you must do the following:

- One computer is chosen as the "server". The other is the "client".
- The server listens on a network port number for connection requests.
- The client creates a new connection and connects it to the server's port.
- The server accepts the connection, creating a corresponding connection at its own end.

At this point both the client and the server have a "connection" with which they can send and receive data. Data sent down the server's connection will be received by the client's connection and vice versa.

(Note: This can be extended to connect multiple computers together, by having one as the server and having the rest of them as clients that connect to the server. In this case the server will have multiple "connection"s, one for each client.)

## Servers and server connections

### NewServer

Format:
```
NewServer(port)
```

Where port is the port number on which the server "listen"s for connection requests.
NewServer() creates a server and returns a handle to identify the server to other functions (such as AcceptConnection()).

Example:
```
dim server
server = NewServer(8000)   ' Create a new server on port 8000

' ... Run the program

DeleteServer(server)       ' Close and delete the server
```

### DeleteServer

Format:
```
DeleteServer(server)
```

Where server is a server handle returned from `NewServer()`.
Shuts down and deletes the server. Any connections accepted by the server will automatically be disconnected and deleted.

It is good practice to close server objects (and connections) when finished with them.
If not closed explicitly, Basic4GL will automatically close them when the program ends.

### ConnectionPending

Format:
```
ConnectionPending(server)
```

Where `server` is a server handle returned from `NewServer()`.

`ConnectionPending()` returns `true` if a client has asked for a connection to the server and is waiting for the server to accept or reject it.
The connection can now be accepted with `AcceptConnection()` or rejected with `RejectConnection()`.

### AcceptConnection

Format:
```
AcceptConnection(server)
```

Where server is a server handle returned from `NewServer()`.

`AcceptConnection()` accepts a pending connection request, creates a corresponding connection object and returns a handle for it.
If no connection is pending, `AcceptConnection()` does nothing and returns `0`.

Example:
```
const port = 8000
dim server, connection

' Create server
server = NewServer(port)
printr "Server created. Waiting for connections"

' Wait for incoming connections
while true
if ConnectionPending(server) then
printr "Connection accepted"

        ' Accept connection
        connection = AcceptConnection(server)
        
        ' ... Do something here
        Sleep(1000)
        
        ' Close connection now that we're finished
        DeleteConnection(connection)
    endif
wend
```

### RejectConnection

Rejects an incoming connection request.

Format:
```
RejectConnection(server)
```

Where `server` is a server handle returned from `NewServer()`.


It is good practice to reject any pending connections when you know that you cannot handle them - for example if your server already has all the connections it can handle. The client connection will disconnect immediately rather than wait and eventually timeout from receiving no response.

Rejecting the connection also removes it from the pending connection queue, so if your server is able to accept connections again later on it will not see the old pending connection request.

## Client connections

### NewConnection

Format:
```
NewConnection(address, port)
```

Creates a new connection and attempts to connect to a server at the specified address and port.
address is a text string specifying the network name to connect to. It can either be a DNS address (e.g. "someserver.com"), a numeric IP address (e.g. "192.168.0.1") or "localhost" (meaning connect to the same computer).
port is the port number. It must be the same one as the server is listening on, otherwise it wont find the server.

`NewConnection()` returns a handle identifying the connection that can be passed to other functions (such as `SendMessage()`).

### DeleteConnection

Deletes a network connection.

Format:
```
DeleteConnection(connection)
```

Where `connection` is a connection handle returned by `NewConnection()` or `AcceptConnection()`.

If the connection is active, it will be closed, and a notification sent to the corresponding connection at the other end to inform it of the close.

Basic4GL also automatically closes and deletes any outstanding network connections when the program finishes.

## Connection state
Programs should monitor the "connection state" of their connections, especially to detect whether the connection has become disconnected, which happens if either end call `DeleteConnection()`, or if the connection has been disconnected somehow.

Client connections follow this pattern:

1. NewConnection() called
2. Handshaking
3. Connected
4. (Connection used to send/receive messages)
5. Disconnected

Or if the connection is not accepted:

1. NewConnection() called
2. Handshaking
3. Disconnected

Server accepted connections are similar to client connections, except they are never in the "Handshaking" state (the connection is considered connected as soon as AcceptConnection() is called):

1. AcceptConnection() called
2. Connected
3. (Connection used to send/receive messages)
4. Disconnected

The following functions are used to detect the different states.

### ConnectionConnected

Format:
```
ConnectionConnected(connection)
```

Where connection is a connection handle returned by NewConnection() or AcceptConnection().

ConnectionConnected() returns true if the connection is still connected, or false if the connection has been disconnected.
Connections are considered "connected" when they are created, and remain that way until either:

The connection is closed at the other end (by DeleteConnection()), or
The connection times out due to lack of network activity.
(Note: This does not mean that you have to keep sending network messages to prevent a connection timing out. The network engine automatically sends "keep alive" notifications if necessary to inform the other side that the connection is still alive.)

### ConnectionHandshaking

Format:
```
ConnectionHandshaking(connection)
```

Where connection is a connection handle returned by NewConnection() or AcceptConnection().

Returns true if the connection is in the hand-shaking state.

Connections created by NewConnection() are considered to be "hand-shaking" until the server accepts the connection (and the confirmation notification is received).
Once the connection is established, it leaves the hand-shaking state (ConnectionHandshaking() will then return false), and the connection is ready to send and receive messages.

Note: Server connections created with AcceptConnection() do not have a hand-shaking phase. For these ConnectionHandshaking() will always return false, as the connection is fully established as soon as it has been accepted.

### ConnectionAddress

Format:
```
ConnectionAddress(connection)
```

Where connection is a connection handle returned by NewConnection() or AcceptConnection().

Returns the IP address of the computer at the other end of the network connection, in numeric format (e.g. "192.168.0.1").

Sending messages
Data is passed through connections as "messages", variable length blocks of data which are transmitted and received as a single item.

### SendMessage

Format:
```
SendMessage(connection, channel, reliable, smoothed)
```

Where connection is a connection handle returned by NewConnection() or AcceptConnection().

SendMessage() creates a message ready to be sent down connection, and returns a handle representing the message.
You can then pass this handle to the Write...() file I/O functions (WriteByte(), WriteString(), etc) to write data to the message, just as you would write data to a file. Refer to the file I/O functions in the Basic4GL Programmers' Guide for more information.

Once the message is ready, call CloseFile() to close the message and send it.

SendMessage() has 3 options which affect message delivery:

#### Channel

Channel is a "channel number" and affects the order in which messages are received.

Depending on network conditions messages can arrive at the receiving end in a different order than which they were sent. For example a message (or part of a message) may be lost in transmission and have to be resent, delaying it long enough for other messages to get in infront of it.

The Basic4GL network engine supports ordering of messages through "channels". Every connection has 32 channels (numbered 0 through 31 inclusive). Messages sent within a single channel are guaranteed to be received in the same order as they were sent - with the exception of channel # 0 which is the unordered channel.

Two messages sent down different channels are not guaranteed to be received in the same order.

The multiple channels to allow you to specify for which messages the ordering is important. A good choice of channels can affect network performance, especially over unreliable networks (such as an internet connection). If an ordered message is delayed, the whole channel will stall until the message is received and slotted into its correct order. However other channels will still keep receive messages. So if a game was using on ordered channel for chat messages, and a different channel for position updates, the engine can keep receiving position updates even if a chat message is lost and must be re-transmitted.

#### Reliable
Reliable is true if the message must be delivered.

Depending on network conditions, some messages may be lost in transmission. The reliable flag specifies whether this is acceptable for this message (reliable = false) or whether the message must get through, in which case the network engine will keep resending the packet until delivery is confirmed.

#### Smoothed
Packet "smoothing" attempts to smooth out the network lag by measuring the average amount of time it takes for packets to arrive, and occasionally delaying early arriving packets before releasing them to the application.

This can result in a smoother experience (especially for messages conveying position updates and game events), but be aware that it does add extra latency to some packets.

## Receiving messages

### MessagePending

Format:
```
MessagePending(connection)
```

Where connection is a connection handle returned by NewConnection() or AcceptConnection().

MessagePending() returns true if a message has been received and can be fetched with ReceiveMessage().

### MessageChannel

Format:
```
MessageChannel(connection)
```

Where connection is a connection handle returned by NewConnection() or AcceptConnection().

MessageChannel() returns the channel number of the pending message. (See SendMessage() for more information).

### MessageReliable

Format:
```
MessageReliable(connection)
```

Where connection is a connection handle returned by NewConnection() or AcceptConnection().

MessageReliable() returns whether the pending message was sent as a reliable message (MessageReliable() = true) or as an unreliable message. (See SendMessage() for more information).

### MessageSmoothed

Format:
```
MessageSmoothed(connection)
```

Where connection is a connection handle returned by NewConnection() or AcceptConnection().

MessageSmoothed() returns whether the pending message was sent as a smoothed message (MessageSmoothed() = true) or not. (See SendMessage() for more information).

### ReceiveMessage

Format:
```
ReceiveMessage(connection)
```

Where connection is a connection handle returned by NewConnection() or AcceptConnection().

ReceivedMessage() fetches the current pending message from the connection and returns a handle representing the message.
You can then pass this handle to the Read...() file I/O functions (ReadByte(), ReadChar(), etc) to read data from the message, just as you would read data from a file. The Seek() and EndOfFile() functions may also be used. Refer to the file I/O functions in the Basic4GL Programmers' Guide for more information.

Once you have finished with the message, you should discard it with CloseFile(), in order to free up resources.

## Connection and handshaking flags

There are two flags which indicate the current connection state of a connection:

1. Connected (see function: `ConnectionConnected()`)
2. Handshaking (see function: `ConnectionHandshaking()`)

### Client connections
When a client connection is created with `NewConnection()`, _connected_ and _handshaking_ are both set.

- If the connection succeeds, _connected_ remains set, and _handshaking_ is cleared.
- If the connection fails (either rejected by the server, or times out), connected is cleared. (_handshaking_ may remain set though...)

Thus, the code to establish a client connection might look something like this:
```
dim connection, address$, port

' Get connection details
print "Address?:": address$ = input$()
print "Port?:": port = val(input$())

' Attempt to connect to server
printr "Connecting..."
connection = NewConnection(address$, port)
while ConnectionConnected(connection) and ConnectionHandshaking(connection): wend

' Check if succeeded
if ConnectionConnected(connection) then
printr "Connection succeeded"
' Do something with connection
' ...
else
printr "Connection failed"
endif

' Close connection
DeleteConnection(connection)
```

If you attempt to use a connection while in the handshaking stage the network engine will do it's best to accommodate this.

Specifically:

- Outgoing messages will not be sent immediately. Instead, they will be placed in a queue until the connection is established, and then sent.
- No messages will be received until the connection is established.

### Server connections
When a server connection is created with `AcceptConnection()`, _connected_ is set and _handshaking_ is cleared.
The connection is considered established and can be used immediately.

## Connection settings
Network connections have a number of parameters which affect how they behave and perform in different network conditions. These affect timeouts, automatic resends, timing and also have an effect on the amount of bandwidth used. Often you will not need to configure these parameters as they have defaults should work in a number of different network conditions. However they are available should you need them.

Be careful when adjusting connection settings, as they can cause the network connection to fail if setup incorrectly.

Connection settings can be changed after a connection is created (with NewConnection or AcceptConnection).

### SetConnectionTimeout

Format:
```
SetConnectionTimeout(milliseconds)
```

Where milliseconds is the number of milliseconds after which a connection times out and disconnects if no response is received from the other side.
The default is 60000 (60 seconds).

### SetConnectionHandshakeTimeout

Format:
```
SetConnectionHandshakeTimeout(milliseconds)
```

Where millisecondsis the number of milliseconds after which a connection attempt will timeout if no reply is received from the server.
The default is 10000 (10 seconds).

### SetConnectionKeepAlive

Format:
```
SetConnectionKeepAlive(milliseconds)
```

If the connection has not sent anything for this amount of time it will automatically send a "keep alive" message to let the other end know that it is still connected. This prevents the connection from timing out at the other end.

How often keep alive messages need to be send depends on the connection time-out, as well as the network latency, and packet loss. Setting it to a quarter of the connection time-out (for example), will give the network engine 3 or 4 attempts to get the keep-alive packet through before the connection times out.

If your program already sends a constant stream of network traffic (e.g. position updates in a real time game) then that traffic will keep the connection alive, and explicit "keep-alive" packets are not important.

### SetConnectionReliableResend

Format:
```
SetConnectionReliableResend(milliseconds)
```

This affects sending of reliable messages. When a reliable messages is sent, the connection will continually send the message until it receives confirmation from the other end that the message has been delivered. This setting controls how long the connection waits before resending the message. The default is 200 (0.2 seconds).

The lower this value is, the less delay there will be when packet loss occurs. However setting the value lower than the ping time will use up extra bandwidth, as a reliable message will be sent twice (or more) before the confirmation notification is received.

### SetConnectionDuplicates

Format:
```
SetConnectionDuplicates(count)
```

Specifies the number of times each message is duplicated when sent. The default is 1.
Setting this number higher decreases the likelyhood of packet loss at the cost of extra bandwidth.

### SetConnectionSmoothingPercentage

Format:
```
SetConnectionSmoothingPercentage(percentage)
```

This setting only affects packets that have been sent with the "smoothing" parameter set to true.

The "smoothing" timing algorithm attempts to add artificial lag such that this percentage of packets arrive on time. The default is 80 (percent).

Setting this number lower will decrease artificial lag but decreases "smoothness", whereas setting it higher will increase artificial lag and increase "smoothness".