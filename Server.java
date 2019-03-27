import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

public class Server {

	public static void main(String[] args) {
		
		//Checking the command line arguments
		if(args.length!=4) {
			System.out.println("Please enter the required arguments");
			System.out.println("FORMAT: Server -document-root \"filepath\" -root portnumber");
			System.exit(0);
		}
		
		//2nd and 4th input argument corresponds to the file path & port number
		String path = args[1];
		int port = Integer.parseInt(args[3]);	
		
		
		//Creating a server socket
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(port);
		
			//Infinite loop for server to process the HTTP requests until the server halts execution
			while(true) {
				
				//Accepting incoming connections
				Socket clientSocket = serverSocket.accept();
				
				//Creating object of HTTPServer to process the HTTP request
				HTTPServer request = new HTTPServer(clientSocket,path);
				//Creating a new thread to handle each request
				Thread thread = new Thread(request);
				thread.start();		
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}

final class HTTPServer implements Runnable{
	Socket clientSocket;
	String path;
	//HTTP Status codes and their corresponding messages 
	HashMap<Integer,String> map = new HashMap<Integer,String>();	
	String not_found = "/404.html";
	//HttpServer Constructor 
	public HTTPServer(Socket socket,String path) {
		this.clientSocket = socket;
		this.path = path;
		map.put(400,"HTTP/1.0 400 Bad Request\r\n");
		map.put(501,"HTTP/1.0 501 Not Implemented\r\n");
		map.put(403,"HTTP/1.0 403 Forbidden\r\n");
		map.put(404,"HTTP/1.0 404 Not Found\r\n");
		map.put(505,"HTTP/1.0 505 HTTP Version Not Supported\r\n");
		map.put(200,"HTTP/1.0 200 OK\r\n");
	}

	@Override
	public void run() {
		BufferedReader input = null;
		PrintStream output = null;
		BufferedOutputStream data = null;
		String filename = null;
		String httpVersion = null;
		String httpMethod = null;
		
		//Html file paths for various error codes
		String bad_request = "/400.html";
		String forbidden = "/403.html";
		String not_implemented = "/501.html";
		String version_not_supported = "/505.html";
		
		try {
			// Read the characters from the client
			input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 
			//Output stream to be displayed to the client
			output = new PrintStream(new BufferedOutputStream(clientSocket.getOutputStream()));
			data = new BufferedOutputStream(clientSocket.getOutputStream());
			
			
			//First line of the client request : Format -- HTTPMethod pathofrequestedresource HTTPVersion
			String line = input.readLine();			
			
			if(line==null) {
				//Exit if the request is null
				return;
			}
			
			System.out.println("HTTP request: "+line);
			
			//Parsing the request into tokens
			StringTokenizer token = null;
			int count = 0;
			if(line!=null) {
				token = new StringTokenizer(line);
				count = token.countTokens();
				httpMethod = token.nextToken().toUpperCase();
				if(token.hasMoreTokens()) filename = token.nextToken();
				if(token.hasMoreTokens()) httpVersion = token.nextToken();				
			}
			
			//Handling bad requests Error:400
			if(count!=3) {
				System.out.println("400 Bad Request : " + httpMethod + " method.");
				filename = path+bad_request;
				flushOutputData(filename,400,input,output,data);
				return;
			} 
			
			//Methods other than GET not implemented - Error:501
			if (!httpMethod.equals("GET")) {
				System.out.println("501 Not Implemented : " + httpMethod + " method.");
				filename = path+not_implemented;
				flushOutputData(filename,501,input,output,data);
	            return;
			}	
			
			//Only HTTP/1.0 and HTTP/1.1 versions are valid
			if (!httpVersion.equals("HTTP/1.0") && !httpVersion.equals("HTTP/1.1") ) {
				System.out.println("505 HTTP Version Not Supported");
				filename = path+version_not_supported;
				flushOutputData(filename,505,input,output,data);
	            return;
			}
			
			else {
				//If the requested file contains only / then append index.html
				if (filename.endsWith("/") && filename.length()==1) 
					filename+="index.html";
			    
			    //Append the path where the server should search the file
			    filename = path+filename;  
				
			    //If the file exists but doesn't have proper permissions set  Error : 403
			 	if((new File(filename).exists()) && !(new File(filename).canRead()))
			 	{
			 			System.out.println("HTTP/1.0 403 Forbidden");
			 			filename = path+forbidden; 
				 		flushOutputData(filename,403,input,output,data);
				 		return;
			 	} 					
				flushOutputData(filename,200,input,output,data);
		   		System.out.println("HTTP/1.0 200 OK\r\n");
			}
		}			  
		catch (FileNotFoundException fnfe) 
		{
			filename = path+not_found;
			System.out.println("HTTP/1.0 404 Not Found\r\n");
			try {
				flushOutputData(filename,404,input,output,data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		  catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try{
				data.close();
				input.close();
				output.close();
		        clientSocket.close();
			}
			catch(Exception e){
				System.err.println("Error while closing the stream/socket : " + e.getMessage());
			}
		}
		  
	}

	//Function for reading the file
	private byte[] readFile(File file, int fileLength) throws IOException {
		FileInputStream ip = null;
		byte[] fileData = new byte[fileLength];		
		try {
			ip = new FileInputStream(file);
			ip.read(fileData);
		} finally {
			if (ip != null) 
				ip.close();
		}		
		return fileData;
	}

	//Determine the content type of the file requested and print HTTP header
	//Supporting html/txt/jpg/png/css and gif file formats
	public String getContentType(String filename) {	
		String contentType = "text/plain";
		if (filename.endsWith(".html") || filename.endsWith(".htm")) {
	    	contentType ="text/html";
	    }
	    else if (filename.endsWith(".gif")) {
	    	contentType="image/gif";		    
	    } 
	    else if (filename.endsWith(".png")) {
	    	contentType="image/png";		    
	    }
	    else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
	    	contentType = "image/jpeg";
	    }
	    else if (filename.endsWith(".css")) {
	    	contentType = "text/css";
	    } 
	    else if (filename.endsWith(".class"))
	    	contentType="application/octet-stream";
		return contentType;	
	}
	
	public void flushOutputData(String filename,int statusCode,BufferedReader input,PrintStream output,BufferedOutputStream data) throws IOException {
		try {
			File file = new File(filename);
			int fileLength = (int) file.length();
			String contentType = getContentType(filename);
			byte[] fileData = readFile(file, fileLength);
			output.print(map.get(statusCode));						
			output.print("Content-type:" + contentType+"\r\n");
			output.print("Content-length: " + fileLength+"\r\n");
			output.print("Date: " + new Date()+"\r\n");
			output.print("Location: "+filename+"\r\n\r\n");
			output.flush(); 
			data.write(fileData,0,fileLength);
	   		data.flush();	   		
		}catch (FileNotFoundException fnfe) 
		{
			filename = path+not_found;
			System.out.println("HTTP/1.0 404 Not Found\r\n");
			flushOutputData(filename,404,input,output,data);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		catch(Exception e) {
			e.printStackTrace();
		}	
	}
	
	
}
