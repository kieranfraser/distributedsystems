# This is a server built using raw sockets which implements a thread
# pool. It receives a number of messages from a client, passes it to
# a worker thread if available (if not queue), and continues listening
# for connections.

# @author - Kieran Fraser, 2015

require 'socket'
require 'thread'

# Import custom thread pool class
require_relative 'Pool'


# Initialize variables
# The port value is taken from the user input on command line.
# The THREAD_NUMBER constant will be used to initialize the
# thread pool. This value can be changed depending on how many
# working threads the user wants to create.

#This IP address was used as in order to connect to the test server
#it was necessary to SSH to Macneill and run this server from that
#machine. I used putty to ssh to the machine and run the program.
# echo $SSH_CONNECTION will give the local computer and Macneill
#machine's IP address and port numbers.
#I used FileZilla to transfer my server to the Macneill machine.

hostname = '134.226.32.10'
#hostname = '127.0.0.1'
$port = ARGV[0]
THREAD_NUMBER = 10
$ipAddress = Socket.getaddrinfo(Socket.gethostname,"echo",Socket::AF_INET)[0][3]
$STUDENT_NUMBER = "39f20a6b16fedf8e18d0ac5b965ef175bb06b53317538707ffd281a5ac93c0bb"

JOIN_CHATROOM = "JOIN_CHATROOM:"
CLIENT_IP = "CLIENT_IP:"
PORT = "PORT:"
CLIENT_NAME = "CLIENT_NAME:"
JOINED_CHATROOM = "JOINED_CHATROOM:"
SERVER_IP = "SERVER_IP:"
ROOM_REF = "ROOM_REF:"
JOIN_ID = "JOIN_ID:"

ERROR_CODE = "ERROR_CODE:" 
ERROR_DESCRIPTION = "ERROR_DESCRIPTION:"

CHAT = "CHAT:"

$returnedJoinId


CHATROOM_ONE = 1
CHATROOM_ONE_STR = "room1"
CHATROOM_TWO = 2
CHATROOM_TWO_STR = "room2"

$chatList = Hash.new

# Initialize the server and bind to user defined port

server = TCPServer.open(hostname, $port)

# Create a new instance of Thread Pool and pass in the number of worker
# threads to be made available

p = Pool.new(THREAD_NUMBER)
puts 'Server waiting for a client connection...'

 def next_line_readable?(socket)
      readfds, writefds, exceptfds = select([socket], nil, nil, 0.1)
      #p :r => readfds, :w => writefds, :e => exceptfds
      readfds #Will be nil if next line cannot be read
end

def receive(socket)
	#Read the response from the server, remove the lines user not interested in
	#and print the all caps text
	puts 'listening'
	puts serverSock.gets
	while  next_line_readable?(socket)
		line = socket.gets
		serverSays = line
		puts serverSays
	end
	puts 'finished'
end

def error(clientSock)
	puts 'inerror'
	errorCmd = 	ERROR_CODE+"1"+"\n"+
							ERROR_DESCRIPTION+"The request is not formatted correctly\n"
	clientSock.print(errorCmd)
end

def join(joinCmd, clientSock)
	puts 'in join'
	splitCmd = joinCmd.split("\n")
	chatroomName = splitCmd[0]
	clientIp = splitCmd[1]
	port = splitCmd[2]
	clientName = splitCmd[3]
	joinId = rand(100)
	$returnedJoinId = joinId
	joinedChatroom = chatroomName.split(' ')
	joinedIp = clientIp.sub!(CLIENT_IP, "")
	joinedPort = port.sub!(PORT, "")
	puts joinedIp
	puts joinedPort
	userHandle = clientName.split(' ')
	roomRef = ""
	puts 'last'
	puts joinedChatroom.last
	#case joinedChatroom.last
	#when CHATROOM_ONE_STR
	#	roomRef = CHATROOM_ONE
	#	$chatList[userHandle] = CHATROOM_ONE.to_s
	#when CHATROOM_TWO_STR
	#	roomRef = CHATROOM_TWO
	#	$chatList[userHandle] = CHATROOM_TWO.to_s
	#else 
	#	error(clientSock)
	#end
	puts 'about to return'
	returnCmd = JOINED_CHATROOM+"room1"+"\n"+
						SERVER_IP+"134.226.32.20"+"\n"+
						PORT+"8080"+"\n"+
						ROOM_REF+"1"+"\n"+
						JOIN_ID+joinId.to_s+"\n"
	puts returnCmd
	clientSock.write(returnCmd)
	#clientSock.close
	puts "printed"
	#puts $chatList[userHandle]
end

def leave(clientSock)
	puts 'in leave'
	returnCmd = "LEFT_CHATROOM:"+"1"+"\n"+
						JOIN_ID+"89"+"\n"
	puts returnCmd
	clientSock.print(returnCmd)
	puts 'printed leave'
end

def chat
	puts 'chat chat'
end

loop do

  # Wait for a Client to connect

  #$client = server.accept

  # Hand off the work (within the 'do' block) to a thread and
  # return to waiting for Clients to connect
  clientRec = server.accept
  p.schedule() do |i|
    clientThread = clientRec
	disconnect = false
	while(!disconnect)
	
	command = ""
	line = ""
	#clientThread = $client
	while next_line_readable?(clientThread)
		line = clientThread.gets
		if line.include? "KILL_SERVICE"
		  abort("Killing service...")
		else
		  if line.include? "HELO "
			clientThread.print(line+"IP:"+$ipAddress+"\n"+"Port:"+$port+"\n"+"StudentID:"+$STUDENT_NUMBER+"\n")
			listenUntil = 1
		  end
		end
		puts line
		command = command + line
	end
	puts 'got from client '+command
    if command.include? JOIN_CHATROOM
		join(command, clientThread)
		puts 'returncmd'
		returnChatCmd = CHAT+"1"+"\n"+CLIENT_NAME+"client1"+"\n"+"MESSAGE:"+
		"client1"+" has joined this chatroom.\n"
		clientThread.write(returnChatCmd)
		puts 'sent command'
    elsif command.include? "LEAVE_CHATROOM:"
		puts 'leave'
		leave(clientThread)
	elsif command.include? "DIS"
		disconnect = true
	else command.include? CHAT
		puts command
		chat
    end
	#clientThread.close
	end
	puts 'disconnected'
  end
end
