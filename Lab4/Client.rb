## Simple Client implemented using sockets - sends a
## GET request to the server including some user defined
## text and the server returns the text in all caps.
## It's then printed to the screen to the user.

## @author Kieran Fraser
require "socket"

#Define the hostname and port number to connect to

hostname = "127.0.0.1"
port = ARGV[0]

JOIN_CHATROOM = "JOIN_CHATROOM: "
CLIENT_IP = "CLIENT_IP: "
PORT = "PORT: "
CLIENT_NAME = "CLIENT_NAME: "
JOINED_CHATROOM = "JOINED_CHATROOM: "
SERVER_IP = "SERVER_IP: "
ROOM_REF = "ROOM_REF: "
JOIN_ID = "JOIN_ID: "
CLIENT_NAME_HANDLE = "Kieran"
RECEIVED_JOIN = ""

LEAVE_CHATROOM = "LEAVE_CHATROOM:"
DISCONNECT = "DISCONNECT: "



CHATROOM_ONE = 1
CHATROOM_ONE_STR = "crone"
CHATROOM_TWO = 2
CHATROOM_TWO_STR = "crtwo"

def next_line_readable?(socket)
      readfds, writefds, exceptfds = select([socket], nil, nil, 0.1)
      #p :r => readfds, :w => writefds, :e => exceptfds
      readfds #Will be nil if next line cannot be read
    end

$socket

#Create a new TCP socket using the hostname and port of the server
def createSock
	$socket = TCPSocket.open("127.0.0.1", 2000)
	puts 'Connected to server'
end

def receive
	#Read the response from the server, remove the lines user not interested in
	#and print the all caps text
	puts 'listening'
	serverSock = $socket
	#puts serverSock.gets
	while  next_line_readable?(serverSock)
		line = serverSock.gets
		serverSays = line
		puts serverSays
	end
	puts 'finished'
end

def handleCommand(cmd="command")
	checkCommand = cmd.chomp
	case checkCommand
	when "join"
		puts 'join'
		createSock
		join
		receive 
	when "leave"
		puts 'leave'
		#createSock
		leave
		receive
	when "msg"
		puts 'msg'
	when "exit"
		#Close the socket and finish
		$socket.close
		abort
	when "dis"
		puts 'dis'
		dis
	else
		puts 'not found'
	end
end

#Joining a chat room initiated by the client by sending:
# JOIN_CHATROOM: [chatroom name], CLIENT_IP: [IP Address of client if UDP | 0 if TCP]
# PORT: [port number of client if UDP | 0 if TCP], CLIENT_NAME: [string Handle to identifier client user]
# Client ip and port number empty for tcp
# Client name is user handle or nickname
def join
		joinCommand = 	JOIN_CHATROOM+CHATROOM_ONE_STR+"\n"+
									CLIENT_IP+"\n"+
									PORT+"\n"+
									CLIENT_NAME+CLIENT_NAME_HANDLE+"\n"
		$socket.print(joinCommand)
		puts 'writing to socket'
end

def leave
		leaveCmd = 	LEAVE_CHATROOM+"1"+"\n"+
									JOIN_ID+"89"+"\n"+
									CLIENT_NAME+"kIERAN"+"\n"
		$socket.print(leaveCmd)
		puts 'writing to socket'
end

def dis
		discmd  = 	"DISDIS\n"
		$socket.print(discmd)
		puts 'writing to socket'
end

def message

end



#Get input from the user and format for use within the
#HTTP request - note the different format for Ruby

# input = $stdin.gets.chomp!  #fixes error of reading in the argument at this point
# if( input == '1')
#   socket.print("KILL_SERVICE\n")
# else
#   if(input == '2')
#     socket.print("HELO this could be anything\n")
#   end
#   if(input == '3')
#     socket.print("This is other message\n")
#   end
# end

while true
	command = STDIN.gets
	handleCommand command
end

#ruby Client.rb 2000



