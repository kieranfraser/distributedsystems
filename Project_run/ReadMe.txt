NB NOTE: This works in eclipse, but for some reason won't run as a .jar. I will debug in the coming days if I have time!

The project currently has the following components:

Client side proxy (Transparent)

File Server

Directory Server

Lock Server

Security (implemented Kerberos security protocol for communications between 
the directory service and the client. If have more time will expand to all services.)

Note: Kerberos authentication implemented as theorized here (because I'm 5!) -> 
http://www.roguelynn.com/words/explain-like-im-5-kerberos/

---------------------------------

In order to run:

1. double click project.bat

2. a window will appear asking for you to enter a username: it must be greater than 10 chars. Then press enter.

3. you will then be asked for a password. This must be greater than 6 chars. This is important as the clients encryption is based
   on the username/password identity.

4. next you must choose an operation: either read or write. Must write first however as there will be nothing in the
   directory to begin with. (will rectify this restriction if I get the time).

5. Important Note: you must execute a write before you execute a read
   (as there is nothing yet stored to read on the server)
   I hope to add persistence in the coming days via a db.

6. In order to write, type 'w' or 'write' and press enter.

7. You will now be asked for the file. The file to write MUST be in the same
   directory as the .jar and .bat files. If this is so, simply type the file name and extension
   e.g. test.txt
   Then hit enter.
   
8. The file should now be saved on the file server and the name should be in the directory server.

9. To read the file again, enter 'r' or 'read' when prompted to do so. Press enter. Then enter the 
   file name as before. Press enter. (note: when reading files, there doesn't have to be a file with
   the same name in the current directory. i.e. you can save the file as a different file name if you so
   wish).


