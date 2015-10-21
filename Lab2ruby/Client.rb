## Simple Client implemented using sockets - sends a
## GET request to the server including some user defined
## text and the server returns the text in all caps.
## It's then printed to the screen to the user.

## @author Kieran Fraser

require "socket"

#Define the hostname and port number to connect to

hostname = "127.0.0.1"
port = ARGV[0]

#Create a new TCP socket using the hostname and port of the server

socket = TCPSocket.open("127.0.0.1", 2000)
puts 'Connected to server'

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

socket.print("this is another message\n")
puts 'writing to socket'

#ruby Client.rb 2000

#Read the response from the server, remove the lines user not interested in
#and print the all caps text
while line = socket.gets
  serverSays = line
  puts serverSays
end

#Close the socket and finish
socket.close