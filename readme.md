### HTTP WebServer
A HTTP webserver program is built in Java. The server program runs and listens for connections on the socket bound to port
number given as input. The browser is used as the client. The client tries to connect to the server and send 
HTTP requests by specifying the IP address(localhost) and port number of the server. The client connections are managed by the 
Socket object. The server program uses ServerSocket object's accept() method for the server to listen for requests from 
the client and send the responses. The client's HTTP request will be in the format (HTTP_method path_of_the_requested_resource 
version_of_HTTP) 
Eg: GET /index.html HTTP/1.0
The server spawns a new thread for each HTTP request and is handled by the HTTPServer class. 
It then parses the HTTP request from client to identify the HTTP method, requested resource and HTTP version.
If the request is a bad request then it returns error 400. 
The server program handles only GET methods and returns 501 error for other HTTP methods. 
If the HTTP version is not 1.0 or 1.1 it returns 505 error.
If the requested file is available in the server directory, it checks for the file permissions. If the access permission is not
there, program returns 403 Forbidden error. Otherwise, the program responds to the client with the requested files 
and closes the connection after serving the request with status as 200. If the file is not found it returns 404 File not found 
error.
##### Following are the HTTP Status codes used in the program:
200 - OK
400 - Bad Request
403 - Forbidden
404 - Not Found
501 - Not Implemented
505 - HTTP Version Not supported
The file formats supported by the server includes jpeg,jpg,html,txt,css,class and png.


##### Instructions to run the program:
The server files and the html  files for error handling(400.html,403.html,404.html,501.html,505.html) should be in 
the server directory i.e. document_root

Command for compiling and running the server program
>javac Server.java
>java Server -document_root "/home/Desktop/webserver_files" -port 8888

Information to know:
Please download the files 400.html,403.html,404.html,501.html,505.html to the document_root(server directory) since the server 
program serves only the files in the server directory.

References:
https://www.jmarshall.com/easy/http/
https://www.restapitutorial.com/httpstatuscodes.html
https://cs.fit.edu/~mmahoney/cse3103/java/Webserver.java
