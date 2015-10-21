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

hostname = '127.0.0.1'
port = ARGV[0]
ipAddress = Socket.getaddrinfo(Socket.gethostname,"echo",Socket::AF_INET)[0][3]
THREAD_NUMBER = 10
STUDENT_NUMBER = "39f20a6b16fedf8e18d0ac5b965ef175bb06b53317538707ffd281a5ac93c0bb"

# Initialize the server and bind to user defined port

server = TCPServer.open(hostname, port)

# Create a new instance of Thread Pool and pass in the number of worker
# threads to be made available

p = Pool.new(THREAD_NUMBER)


puts 'Waiting for a Client connection...'


loop do

  # Wait for a Client to connect

  client = server.accept

  # Hand off the work (within the 'do' block) to a thread and
  # return to waiting for Clients to connect

  p.schedule do |i|
    puts "Thread #{i} started #{Thread.current[:id]}"
    line = client.gets
    puts line
    if line.include? "KILL_SERVICE"
      abort("Killing service...")
    else
      if line.include? "HELO "
        client.print(line+"IP:"+ipAddress+"\n"+"Port:"+port+"\n"+"Student No:"+STUDENT_NUMBER+"\n")
      else
        processOtherMessage(line)
      end
    end
    client.close
  end
end


# Function for 'other' messages.. simply prints to the terminal

def processOtherMessage(line)
  puts "this -> "+line
end
