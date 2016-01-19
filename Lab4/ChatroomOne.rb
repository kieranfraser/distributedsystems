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

hostname = '127.0.0.1'
port = 8080
THREAD_NUMBER = 10

# Initialize the server and bind to user defined port

server = TCPServer.open(hostname, port)

# Create a new instance of Thread Pool and pass in the number of worker
# threads to be made available

p = Pool.new(THREAD_NUMBER)


puts 'Chatroom 1 awaiting a connection...'
roomList = Array.new 

loop do

  # Wait for a Client to connect

  client = server.accept

  # Hand off the work (within the 'do' block) to a thread and
  # return to waiting for Clients to connect

  p.schedule do |i|
    puts "Thread #{i} started #{Thread.current[:id]}"
    clientHandle = client.gets
    puts line
    if roomList.include? clientHandle == false
      roomList.push clientHandle
    end
    client.close
  end
end

