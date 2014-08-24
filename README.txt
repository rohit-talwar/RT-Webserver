                                       README                                   
--------------------------------------------------------------------------------
                                   RT - WebServer				

Contents
--------

1. What is it ?
2. External Dependencies and libraries used
3. Usage
4. Build and Installation instructions  
5. Logs
6. Copyright
7. Further Development
8. Config File Appendix
9. Directory Structure


1. What is it ?
---------------
RT Webserver is HTTP/1.1 compliant multi threaded web server. It supports POST, 
HEAD, GET and DELETE methods. It can be configured to serve any file directory 
present in the filesystem(provided it has the relevant permissions to read/write 
data. Keep alive functionality is NOT provided.
Only Files can be uploaded to the server using the post facility, through a multi
part form encoding type.


2. External Dependencies and libraries used
-------------------------------------------
It uses log4J as it is a thread safe logging api. Junit for unit testing
Maven is required for building the software from source, else copying the 
files into your favorite editor can also do the trick


3. Usage
--------
The server properties can be changed before starting the webserver by changing 
the values in config/config.properties file. Nonetheless the server can run with 
default values that will be generated on the fly if the config is missing

To upload files you may use the post.html in the defaultHome directory.  


4. BUild and Installation instructions  
--------------------------------------
Build: like a maven project
       -- run mvn clean install in the main directory

Run: Need to have java installed -- and 
    -- copy the jar with dependencies to the main folder 
    -- run start.bat

Stopping the server:
                   -- after running the start.bat press ctrl+c to exit the program
                   -- known issue - jvm sometimes fails to register the ctrl+c 
                      command hence press it a few times and the shut down hook will
                      get activated


5. Logs
-------
Logs are generated in the logs folder


6. Copyright
------------
Have copied a file comparision code from 
http://www.java2s.com/Code/Java/File-Input-Output/Comparebinaryfiles.htm 


7. Further Development
----------------------

More concrete tests can be made to test the extensibility of the server -- 
such as concurrent sending of GET, POST, DELETE or HEAD requests

RANGE function can be implemented using the coded utilities function

Option for uploading multiple files in the multipart/form encoded 

Improving the buffer system, making a pool of instantiated buffers, so that new
buffers are not initialized for a new connection

Caching facilities


8. Config File Appendix
-----------------------
meaning of different keys in the config file are explained below -
---------------------------------------------------------------------------------|
             Keys               |                   Meaning                      |
--------------------------------|------------------------------------------------|
"port"                          |The port number at the localhost to bind the    |
                                |server on the localhost machine                 |
                                |                                                |
                                |The number of milli seconds each socket         |
"Keep Alive Time"               |connection is kept on the server threads --     |
                                |polling them for any new request                |
                                |                                                |
                                |The number of worker threads that are active all|
"Core Pool Size"                |the time and never die -- keep this number low  |
                                |as a higher value may increase the number of    | 
                                |context switches and put further load on the    |
                                |scheduler                                       |
                                |                                                |
"Maximum Pool Size"             |The maximum number of worker threads that can   |
                                |serve the pool of active connections            |
                                |                                                |
                                |The directory you want to be served             |
"homeDirectory"                 |Note that if the directory cannot be read       |
                                |upon then default home directory will be served |
                                |                                                |
                                |This is specific to each operating system, but  |
                                |can be changed as per need. This represents the | 
"Socket Backlog"                |number of socket connections the operating      |
                                |system should keep in its connection queue in   |
                                |case of large number of incoming connections    |
                                |                                                |
"Favicon File Name"             |The name of the favicon file that resides in    |
                                |your home directory                             |
                                |                                                |
"Upload File Directory"         |The directory where all uploaded files will be  |
                                |stored In case the directory is unaccessible -  |  
                                |all uploaded files can be seen in the           | 
                                |defaultUploads folder                           | 
---------------------------------------------------------------------------------|

9. Directory Structure
----------------------
Test - contains the files uploaded or recieved during testing code -- so that
Further checks can be done using FC \B command. The files are named using a temp 
code that is different always.

src - contains the source code

doc - java docs for the program

DefaultHome - Needs to be always present -- the default root directory which gets
served when nothing is provided or in case the provided values are erroneus

DefaultUpload - Default folder for keeping all the uploaded files

config - keeps the config file of the server where user can edit the values
In case there is no config file present, run the server and a .default conf file will 
be generated which should be renamed to config.properties and edited as per need

log - keeps the server.log file containing the log info of the server 