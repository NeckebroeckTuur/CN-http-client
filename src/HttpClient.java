import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.midi.Soundbank;


public class HttpClient {

	private Socket socket;
	private URI url;
	private HttpCommand command;
	
	private PrintWriter httpPrintWriter;
	private InputStream inputStream;
	private FileOutputStream fileOutputStream;

	
	//DEBUG
	private PrintStream UTFDebugStream;
	private boolean DEBUG = true;
	private File outputPath;
	private File outputFile;
	
	private final static String LINE_SEPARATOR = "\r\n";
	private final static int HEADER_SEPARATOR = 0x0d0a0d0a; 						// /r/n/r/n
	private final static Pattern SRC_PATTERN = Pattern.compile("<.*?src=\\\"(.+?)\\\">");
	private final static Pattern SRC_AD_PATTERN = Pattern.compile("src=\"ad\\d*\\..*?\"");
	
	
	public HttpClient(String url, HttpCommand command, int port) {
		socket = new Socket();
		try {
			if(url.endsWith("/")) url=url.substring(0, url.length()-1);
			this.url = new URI(String.format("%s:%d", url, port));
			this.command = command;
			this.socket = new Socket(InetAddress.getByName(this.url.getHost()), this.url.getPort());
			this.httpPrintWriter = new PrintWriter(this.socket.getOutputStream(), true);
		} catch (URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setDebugStream(PrintStream debugStream) {
		this.UTFDebugStream = debugStream;
	}
	
	public void setOutputPath(File outputPath) {
		this.outputPath = outputPath;
	}
	
	
	public void sendGetRequest(String page) {
		sendHttpRequest(page);
		String[] sources = startListening();
		for(String src:sources) {
			sendHttpRequest(page);
		}
	}
	
	public void sendHttpRequest(String page) {
		if(!page.startsWith("/")) page = "/"+page;
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(String.format("%s %s HTTP/1.1", command.getCommandString(), page));
		sBuilder.append(HttpClient.LINE_SEPARATOR);
		sBuilder.append(String.format("Host: %s:%d", url.getHost(), url.getPort()));
		sBuilder.append(HttpClient.LINE_SEPARATOR);
		//sBuilder.append("Connection: close");
		//sBuilder.append(HttpClient.LINE_SEPARATOR);
		sBuilder.append(HttpClient.LINE_SEPARATOR);
		// TODO Pas als alle media van een webpagina opgevraagd zijn moet 'Connection: close toegevoegd worden'
		
		String requestString = sBuilder.toString();
		debugPrint(requestString);
		
		setOutputFile(page);
		
		//new HttpListener(this, outputFile);
		httpPrintWriter.println(requestString);
	}
	
	private void setOutputFile(String fileName) {
		if(outputPath == null || fileName == null) {
			return;
		}
		
		/*if(!outputPath.exists()) {
			outputPath.mkdir();
		}*/
		String outputFilePath = outputPath.getPath();
		if(fileName.equals("/")) {
			outputFilePath += (File.separatorChar+"index.html");
		}else {
			outputFilePath += (File.separatorChar+fileName.substring(1));
		}
		outputFile = new File(outputFilePath);
		if(!outputFile.getParentFile().exists()) {
			outputFile.getParentFile().mkdirs();
		}
		
		try {
			fileOutputStream = new FileOutputStream(outputFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Listen to the response of the sent HTTP request.
	 * If the received data is an html page ('content-type' contains 'html'),
	 * a string array filled with the values of src tags is returned.
	 * If the received data is not an html page, an empty string array is returned.
	 * 
	 * @return 
	 */
	private String[] startListening() {
		try {
			this.inputStream = socket.getInputStream();
			StringBuilder headerBuilder = new StringBuilder();
			int c;
			int lastFourBytes=0;
			ByteBuffer byteBuffer = new ByteBuffer(4096);
			String rawHeader="";
			
			// Read http header
			while ((c=this.inputStream.read()) != -1) {				// i between 0 ant 255
				if(byteBuffer.isFull()) {
					headerBuilder.append(new String(byteBuffer.getBuffer(), "UTF-8"));
					byteBuffer.reset();
				}
				byteBuffer.addByte((byte)c);
				
				lastFourBytes = (lastFourBytes << 8) | (0xff & c);	// 0xff & for extra safety, normally redundant bc. i between 0 and 255
				if(lastFourBytes == HttpClient.HEADER_SEPARATOR) {
					// end of header reached
					headerBuilder.append(new String(byteBuffer.getBuffer()),0, byteBuffer.getElementPointer()-4);
					rawHeader = headerBuilder.toString();
					System.out.println(rawHeader);
					
					break;
				}				
			}
			byteBuffer.reset();
			HttpHeader header = new HttpHeader(rawHeader);
			header.parse();
			/*for(String s:header.headerFields.keySet()) {
				System.out.println(String.format("(key,value) -> (%s,%s)", s, header.headerFields.get(s)));
			}*/
			
			if(header.getHttpStatusCode() != 200) {
				System.out.println("[WARNING] HTTP statuscode is not 200 but: " + header.getHttpStatusCode());
			}
			

			
			// Read http body
			StringBuilder bodyBuilder = new StringBuilder();
			while ((c=this.inputStream.read(byteBuffer.getBuffer())) != -1) {				// i between 0 ant 255
					bodyBuilder.append(new String(byteBuffer.getBuffer(), "UTF-8").toCharArray(), 0, c);
					fileOutputStream.write(byteBuffer.getBuffer(), 0, c);
					fileOutputStream.flush();
					byteBuffer.reset();
			}
			//fileOutputStream.close();
			
			System.out.println();
			
			String contentType = header.headerFields.get("content-type");
			if(contentType != null && contentType.contains("html")) {
				String html = bodyBuilder.toString();
				return findSources(html, true);
			}
			
		} catch(IOException e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        }
		
		return new String[] {};
	}
	
	
	/**
	 * 
	 * Scans the HTML page for src attributes and returns the values of those attributes as a String array.<br>
	 * Includes the possibility to block ads. Ads are where the value of a source attribute contains:<br>
	 * 'src="ad' followed by at least 0 digits, followed by a '.' followed by any extension, followed by a closing '"'<br>
	 * Ex. src="ad1.jpg", src="ad.new.png"
	 * 
	 * @param htmlPage The HTML source code as String
	 * @param blockAds	Whether or not ads should be excluded from the result
	 * @return A String array containing the values the src attributes
	 */
	public static String[] findSources(String htmlPage, boolean blockAds) {
		Matcher srcMatcher = HttpClient.SRC_PATTERN.matcher(htmlPage);
		Matcher adMatcher = HttpClient.SRC_AD_PATTERN.matcher("");
		
		List<String> sources = new ArrayList<String>();
		
		
		while(srcMatcher.find()) {											// while the htmlPage still has src attributes left
			String wholeSrc = srcMatcher.group();							// get the whole src attribute string: ex. src="planet.jpg" (to later check against the admatcher)
			String src = srcMatcher.group(1);								// get the inside of the src attribute: ex. planet.jpg (to later add to the list of sources)
			
			if(blockAds && adMatcher.reset(wholeSrc).matches()) {			// if ads should be blocked and the src attribute string matches against the ad pattern
				continue;													// don't add the inside of the attribute to the list
			}
			sources.add(src);
		}
		String[] result = sources.toArray(new String[0]);
		
		return result;
	}
	
	public void closeConnection() {
		this.httpPrintWriter.close();
		try {
			this.socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Socket getSocket() {
		return this.socket;
	}
	
	public void debugPrint(String s) {
		if(UTFDebugStream != null && DEBUG) {
			this.UTFDebugStream.print(s);
		}
	}
	
	private void getImage(String image) {
		try {
			InputStream in = new DataInputStream(this.socket.getInputStream());
			OutputStream out = this.socket.getOutputStream();
			
			StringBuilder requestBuilder = new StringBuilder();
			requestBuilder.append(String.format("GET /%s HTTP/1.1", image));
			requestBuilder.append(HttpClient.LINE_SEPARATOR);
			requestBuilder.append(String.format("Host: %s:%d", url.getHost(), url.getPort()));
			requestBuilder.append(HttpClient.LINE_SEPARATOR);
			requestBuilder.append(HttpClient.LINE_SEPARATOR);
			String request = requestBuilder.toString();
			
			out.write(request.getBytes());
			out.flush();
			
			int c;
			StringBuilder bodyBuilder = new StringBuilder();
			ByteBuffer byteBuffer = new ByteBuffer(1024);
			while ((c=this.inputStream.read(byteBuffer.getBuffer())) != -1) {				// i between 0 ant 255
					bodyBuilder.append(new String(byteBuffer.getBuffer(), "UTF-8").toCharArray(), 0, c);
					fileOutputStream.write(byteBuffer.getBuffer(), 0, c);
					fileOutputStream.flush();
					byteBuffer.reset();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
