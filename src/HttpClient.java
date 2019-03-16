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


public class HttpClient {

	private Socket socket;
	private URI url;
	private HttpCommand command;
	
	private PrintWriter httpPrintWriter;
	private FileOutputStream fileOutputStream;

	
	//DEBUG
	// TODO remove debug
	private PrintStream UTFDebugStream;
	private boolean DEBUG = true;
	
	private int inputBufferLength = 64; 
	
	private File outputPath;
	private File outputFile;
	
	private final static String LINE_SEPARATOR_STRING = "\r\n";
	private final static int LINE_SEPARATOR = 0x0d0a;
	private final static int HEADER_SEPARATOR = LINE_SEPARATOR << 16 | LINE_SEPARATOR; 						// /r/n/r/n
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
			//sendHttpRequest(src);
			//startListening();
		}
	}
	
	public void sendHttpRequest(String page) {
		if(!page.startsWith("/")) page = "/"+page;
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(String.format("%s %s HTTP/1.1", command.getCommandString(), page));
		sBuilder.append(HttpClient.LINE_SEPARATOR_STRING);
		sBuilder.append(String.format("Host: %s:%d", url.getHost(), url.getPort()));
		sBuilder.append(HttpClient.LINE_SEPARATOR_STRING);
		//sBuilder.append("Connection: close");
		//sBuilder.append(HttpClient.LINE_SEPARATOR);
		sBuilder.append(HttpClient.LINE_SEPARATOR_STRING);
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
			InputStream inputStream = socket.getInputStream();
			StringBuilder headerBuilder = new StringBuilder();
			int c;
			int lastFourBytes=0;
			ByteBuffer byteBuffer = new ByteBuffer(this.inputBufferLength);
			String rawHeader="";
			
			// READ HTTP HEADER
			while ((c = inputStream.read()) != -1) {				// i between 0 ant 255
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
			// Header is now read and parsed

			
			
			// READ HTTP DATA
			// To know how many bytes to read, there are 2 options:
			// Header contains content-length and number of bytes to be read OR header contains transfer-encoding: chunked
			// In case of the chunked encoding: each chunk is preceded by the length of the chunk
			// After the last chunk, instead of the length of the next chunk, the 4 bytes /r/n/r/n are added
			boolean isHtml = false;
			String contentType = header.getFieldValue("content-type");
			isHtml = (contentType != null && contentType.contains("html"));
			
			ResponseContentLengthType lengthType = determineResponseContentLengthType(header);
			if(lengthType == ResponseContentLengthType.FIXED) {
				int length = header.getContentLength();
				readFixedLengthResponse(inputStream, length, fileOutputStream);
			}else if(lengthType == ResponseContentLengthType.CHUNKED) {
				readChunkedResponse(inputStream, fileOutputStream);
			}else {
				throw new RuntimeException("Neither the content-length nor transfer-encoding:chunked is present in the HTTP header: no way to determine data length.");
			}
			
			//TODO code hierna is niet correct (overblijfsel)
			
			
			/*
			StringBuilder bodyBuilder = new StringBuilder();
			// need to check for the end of the stream ourselves because 
			// the read method blocks until the socket/server connection times out: in that case we can only send 1 request!
			
			
			while ((c=inputStream.read(byteBuffer.getBuffer())) != -1) {				// i between 0 ant 255
					bodyBuilder.append(new String(byteBuffer.getBuffer(), "UTF-8").toCharArray(), 0, c);
					fileOutputStream.write(byteBuffer.getBuffer(), 0, c);
					fileOutputStream.flush();
					byteBuffer.reset();
			}
			//fileOutputStream.close();
			
			System.out.println();
			
			if(isHtml) {
				String html = bodyBuilder.toString();
				return findSources(html, true);
			}
			*/
			
		} catch(IOException e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        }
		
		return new String[] {};
	}
	
	private ResponseContentLengthType determineResponseContentLengthType(HttpHeader header) {
		if(header.isFieldPresent("content-length")) {
			return ResponseContentLengthType.FIXED;
		}
		
		String transferEncodingValue = header.getFieldValue("transfer-encoding");
		return transferEncodingValue.equals("chunked") ? ResponseContentLengthType.CHUNKED : null;
	}
	
	private void readChunkedResponse(InputStream inputStream, FileOutputStream fos) {
		ByteBuffer byteBuffer = new ByteBuffer(this.inputBufferLength);
		final String HEX_PREFIX = "0x";
		
		boolean firstChunk = true;
		// if the chunk is not the first, the chunk length is of the format 0x0d0a length 0x0d0a
		// if the chunk is the first one, the chunk length is of the format length 0x0d0a
		
		
		try {
			allChunksReadLoop : while(true) {
				// first: read length of next chunk followed by 0x0d0a
				// if the length is 0 followed by 0x0d0a0d0a, the end of the stream has been reached
				String hexStringChunkLength = "";
				short lastTwoBytes = 0; //short capacity: 2 bytes
				int readByte;
				chunkLengthLoop : while((readByte = inputStream.read()) != -1) {
					hexStringChunkLength += (char) readByte;
					
					lastTwoBytes = (short) (lastTwoBytes << 8 | readByte);
					
					if(lastTwoBytes == LINE_SEPARATOR) { // if last two bytes equals the line seperator, the end of the length encoding is reached
						if(firstChunk) {
							firstChunk = false;
							break chunkLengthLoop;
						} else {
							// see declaration of firstChunk
							lastTwoBytes = 0;
							hexStringChunkLength = "";
						}
					}
				}
				hexStringChunkLength = hexStringChunkLength.substring(0, hexStringChunkLength.length()-2);
				int chunkLength = Integer.decode(HEX_PREFIX + hexStringChunkLength);
				if(chunkLength == 0) {
					break allChunksReadLoop;
				}
				
				System.out.println("Chunk with length: " + chunkLength);
				// the length of the next data chunk is now determined
				// now that number of bytes has to be read from the input stream and afterwards, start over
				
				
				int totalBytesRead = 0, bytesRead = 0;
				chunkReadLoop: while ((bytesRead = inputStream.read(byteBuffer.getBuffer())) != -1) {				// i between 0 ant 255
					//bodyBuilder.append(new String(byteBuffer.getBuffer(), "UTF-8").toCharArray(), 0, bytesRead);
					
					fileOutputStream.write(byteBuffer.getBuffer(), 0, bytesRead);
					fileOutputStream.flush();
					byteBuffer.reset();
					
					totalBytesRead += bytesRead;
					
					if(totalBytesRead >= chunkLength) {
						break chunkReadLoop;
					} 
				}
			
				firstChunk = false;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	private void readFixedLengthResponse(InputStream inputStream, int length, FileOutputStream fos) {
		ByteBuffer byteBuffer = new ByteBuffer(this.inputBufferLength);
		int totalBytesRead = 0, bytesRead = 0;
		
		try {
			while ((bytesRead = inputStream.read(byteBuffer.getBuffer())) != -1) {				// i between 0 ant 255
				//bodyBuilder.append(new String(byteBuffer.getBuffer(), "UTF-8").toCharArray(), 0, bytesRead);
				
				fileOutputStream.write(byteBuffer.getBuffer(), 0, bytesRead);
				fileOutputStream.flush();
				byteBuffer.reset();
				
				totalBytesRead += bytesRead;
				
				if(totalBytesRead >= length) {
					break;
				} 
			}
		} catch(IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	private enum ResponseContentLengthType {FIXED, CHUNKED};
}
