require 'socket'
require 'thread'
require_relative 'Pool'


hostname = '127.0.0.1'
port = ARGV[0]
ipAddress = Socket.getaddrinfo(Socket.gethostname,"echo",Socket::AF_INET)[0][3]


server = TCPServer.open(hostname, port)  # Server bind to port 8000
p = Pool.new(10)

puts 'Waiting for a Client connection...'



loop do
  client = server.accept    # Wait for a client to connect
    p.schedule do |i|
      puts "Thread #{i} started #{Thread.current[:id]}"
      sleep(60)
      line = client.gets
      puts line
      if line.include? "KILL_SERVICE"
        abort("Killing service...")
      else
        if line.include? "HELO "
          client.print(line+"IP:"+ipAddress+"\n"+"Port:"+port+"\n")
        else
          puts "this -> "+line
        end
      end
      client.close
    end
end

