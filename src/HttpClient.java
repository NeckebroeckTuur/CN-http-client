import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HttpClient {

	private Socket socket;
	private int port;
	
	private String host = "";
	
	private HttpCommand command;
	
	private PrintWriter httpPrintWriter;
	private FileOutputStream fileOutputStream;

	
	private int inputBufferLength = 4096; 
	
	private File outputPath;
	private File outputFile;
	
	private final static String LINE_SEPARATOR_STRING = "\r\n";
	private final static int LINE_SEPARATOR = 0x0d0a;
	private final static int HEADER_SEPARATOR = LINE_SEPARATOR << 16 | LINE_SEPARATOR; 						// /r/n/r/n
	private final static Pattern SRC_PATTERN = Pattern.compile("<img[\\w\\s=\"]*src=\\\"(.+?)\\\".*?>");
	private final static Pattern SRC_AD_PATTERN = Pattern.compile("src=\"ad\\d*\\..*?\"");
	
	
	public HttpClient(String host, HttpCommand command, int port) {
		socket = new Socket();
		try {
			//if(url.endsWith("/")) url=url.substring(0, url.length()-1);
			this.host = host;
			this.port = port;
			this.command = command;
			this.socket = new Socket(this.host, this.port);
			this.httpPrintWriter = new PrintWriter(this.socket.getOutputStream(), true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setOutputPath(File outputPath) {
		this.outputPath = outputPath;
	}
	
	
	public void sendRequest(String path, String file, String body) {
		switch(this.command) {
			case GET:
				sendGetRequest(path, file);
				break;
			case POST:
				sendPostRequest(path, file, body);
				break;
			default:
				break;
		}
	}
	
	private void sendGetRequest(String path, String file) {
		sendHttpRequest(path, file, "", "");
		String[] sources = startListening();
		
		for(String src:sources) {
			//System.out.println("Found source: " + path + src);
			sendHttpRequest(path, src, "", "");
			startListening();
		}
	}
	
	private void sendPostRequest(String path, String file, String body) {
		//String body = constructPostRequest(new String[][] {{"test","abc"},{"arg2","defg"}});
		int bodyLength = body.length();
		String header = constructHeaderString(new String[][] {{"content-length", String.valueOf(bodyLength)}});
		
		sendHttpRequest(path, file, header, body);
		startListening();
	}
	
	public void sendPutRequest(String path, String file, String body) {
		int bodyLength = body.length();
		String header = constructHeaderString(new String[][] {{"content-length", String.valueOf(bodyLength)}});
		
		// TODO afwerken
	}
	
	// TODO mogelijkheid om extra headers toe te voegen toevoegen
	private void sendHttpRequest(String path, String file, String headers, String body) {
		// TODO if-last-modified toevoegen
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(String.format("%s %s HTTP/1.1", command.getCommandString(), path+file));
		sBuilder.append(HttpClient.LINE_SEPARATOR_STRING);
		//sBuilder.append(String.format("Host: %s:%d\r\nUser-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:65.0) Gecko/20100101 Firefox/65.0\r\n" + 
		//		"Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\r\n" + 
		//		"Accept-Language: en-US,en;q=0.5\r\n" + 
		//		"Accept-Encoding: gzip, deflate\r\n" + 
		//		"Connection: keep-alive\r\n" + 
		//		"Upgrade-Insecure-Requests: 1\r\n" + 
		//		"If-Modified-Since: Tue, 05 Jul 2016 23:27:52 GMT\r\n" + 
		//		"If-None-Match: \"a04-536ebcd40252a-gzip\"\r\n" + 
		//		"Cache-Control: max-age=0", url.getHost(), url.getPort()));
		sBuilder.append(String.format("Host: %s:%d", this.host, this.port));
		sBuilder.append(HttpClient.LINE_SEPARATOR_STRING);
		sBuilder.append(headers);
		sBuilder.append(HttpClient.LINE_SEPARATOR_STRING);
		sBuilder.append(body);
		sBuilder.append(HttpClient.LINE_SEPARATOR_STRING);
		sBuilder.append(HttpClient.LINE_SEPARATOR_STRING);
		
		

		
		String requestString = sBuilder.toString();
		// System.out.println("<-- OUT --- \n"+requestString+"\n---");
		
		setOutputFile(file);
		
		httpPrintWriter.println(requestString);
	}
	
	private void setOutputFile(String fileName) {
		if(outputPath == null || fileName == null) {
			return;
		}
		
		if(fileName.equals("")) fileName = "index.html";
		String outputFilePath = outputPath.getPath() + "/" + fileName;
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
					// System.out.println("--> IN ---\n" + rawHeader+"\n--- END IN ---");
					
					break;
				}				
			}
			byteBuffer.reset();
			HttpResponseHeader header = new HttpResponseHeader(rawHeader);
			header.parse();
			
			if(header.getHttpStatusCode() != 200) {
				System.out.println("[WARNING] HTTP statuscode is not 200 but: " + header.getHttpStatusCode());
			}
			// Header is now read and parsed

			
			
			// READ HTTP DATA
			// To know how many bytes to read, there are 2 options:
			// Header contains content-length and number of bytes to be read OR header contains transfer-encoding: chunked
			// In case of the chunked encoding: each chunk is preceded by the length of the chunk
			// After the last chunk, a chunk with length 0 and \r\n\r\n are added
			boolean isHtml = false;
			String html = "";
			String contentType = header.getFieldValue("content-type");
			isHtml = (contentType != null && contentType.contains("html"));
			
			ResponseContentLengthType lengthType = determineResponseContentLengthType(header);
			if(lengthType == ResponseContentLengthType.FIXED) {
				int length = header.getContentLength();
				html =readFixedLengthResponse(inputStream, length, fileOutputStream, isHtml);
			}else if(lengthType == ResponseContentLengthType.CHUNKED) {
				html = readChunkedResponse(inputStream, fileOutputStream, isHtml);
			}else {
				throw new RuntimeException("Neither the content-length nor transfer-encoding:chunked is present in the HTTP header: no way to determine data length.");
			}
			
			if(isHtml) {
				System.out.println(html);
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
	 * Determine if the given response is chunked (with header transfer-encoding:chunked) or fixed length (with header content-length: ... )
	 * 
	 * @param header
	 * @return
	 */
	private ResponseContentLengthType determineResponseContentLengthType(HttpResponseHeader header) {
		if(header.isFieldPresent("content-length")) {
			return ResponseContentLengthType.FIXED;
		}
		
		String transferEncodingValue = header.getFieldValue("transfer-encoding");
		if (transferEncodingValue == null) return null;
		return transferEncodingValue.equals("chunked") ? ResponseContentLengthType.CHUNKED : null;
	}
	
	/**
	 * Reads a chunked inputstream and writes the result to the FileOutputStream.
	 * If the read data has to be returned as a string, the argument returnAsString has to be set to true.
	 * Else, null is returned.
	 * 
	 
	 * @param inputStream
	 * @param fos
	 * @param returnAsString
	 * @return
	 */
	private String readChunkedResponse(InputStream inputStream, FileOutputStream fos, boolean returnAsString) {
		ByteBuffer byteBuffer = new ByteBuffer(this.inputBufferLength);
		final String HEX_PREFIX = "0x";
		StringBuilder sBuilder = new StringBuilder();
		
		boolean firstChunk = true;
		// if the chunk is not the first, the chunk length is of the format 0x0d0a length 0x0d0a
		// if the chunk is the first one, the chunk length is of the format length 0x0d0a
		
		
		try {
			allChunksReadLoop : while(true) {
				boolean prefixLineSeparatorSkipped = false;
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
							if(!prefixLineSeparatorSkipped) {
								// see declaration of firstChunk
								lastTwoBytes = 0;
								hexStringChunkLength = "";
								prefixLineSeparatorSkipped = true;
							}else {
								break chunkLengthLoop;
							}
						}
					}
				}
				hexStringChunkLength = hexStringChunkLength.substring(0, hexStringChunkLength.length()-2);
				int chunkLength = Integer.decode(HEX_PREFIX + hexStringChunkLength);
				if(chunkLength == 0) {
					break allChunksReadLoop;
				}
				
				// the length of the next data chunk is now determined
				// now that number of bytes has to be read from the input stream and afterwards, start over
				
				byteBuffer.reset();
				int totalBytesRead = 0;
				int byteRead = 0;
				chunkReadLoop: while ((byteRead = inputStream.read()) != -1) {				// i between 0 ant 255
					if(byteBuffer.isFull()) {
						if(returnAsString)sBuilder.append(new String(byteBuffer.getBuffer(),"UTF-8"));
						fileOutputStream.write(byteBuffer.getBuffer(), 0, byteBuffer.getSize());
						fileOutputStream.flush();
						byteBuffer.reset();
					}
					
					byteBuffer.addByte((byte) byteRead);
					totalBytesRead ++;
					
					if(totalBytesRead >= chunkLength) {
						break chunkReadLoop;
					} 
				}
				
				if(returnAsString)sBuilder.append(new String(byteBuffer.getBuffer(),"UTF-8"));
				fileOutputStream.write(byteBuffer.getBuffer(), 0, byteBuffer.getElementPointer());
				fileOutputStream.flush();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return returnAsString ? sBuilder.toString() : null;
		
	}
	
	private String readFixedLengthResponse(InputStream inputStream, int length, FileOutputStream fos, boolean returnAsString) {
		StringBuilder sBuilder = new StringBuilder();
		ByteBuffer byteBuffer = new ByteBuffer(this.inputBufferLength);
		int totalBytesRead = 0, bytesRead = 0;
		
		try {
			while ((bytesRead = inputStream.read(byteBuffer.getBuffer())) != -1) {				// i between 0 ant 255
				if(returnAsString) sBuilder.append(new String(byteBuffer.getBuffer(), "UTF-8").toCharArray(), 0, bytesRead);
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
		
		return returnAsString ? sBuilder.toString() : null;
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
		Matcher srcMatcher = HttpClient.SRC_PATTERN.matcher(htmlPage.toLowerCase());
		Matcher adMatcher = HttpClient.SRC_AD_PATTERN.matcher("");
		
		List<String> sources = new ArrayList<String>();
		
		
		while(srcMatcher.find()) {											// while the htmlPage still has src attributes left
			String wholeSrc = srcMatcher.group();							// get the whole src attribute string: ex. src="planet.jpg" (to later check against the admatcher)
			String src = srcMatcher.group(1);								// get the inside of the src attribute: ex. planet.jpg (to later add to the list of sources)		
			
			if(blockAds && adMatcher.reset(wholeSrc).find()) {				// if ads should be blocked and the src attribute string matches against the ad pattern
				continue;													// don't add the inside of the attribute to the list
			}
			
			if(src.toLowerCase().contains("http://") || src.toLowerCase().contains("https://")) {						// source on other website
				continue;
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
	
	public String constructPostRequest(String[][] postValues) {
		StringBuilder sBuilder = new StringBuilder("");
		
		for(String[] field : postValues) {
			if(field.length != 2) throw new RuntimeException("Invalid post field: no key value pair.");
				sBuilder.append(field[0]);
				sBuilder.append("=");
				sBuilder.append(field[1]);
				sBuilder.append("&");
		}
		
		String result = sBuilder.toString();
		return result.substring(0, result.length()-1);
	}
	
	private enum ResponseContentLengthType {FIXED, CHUNKED};
	
	private String constructHeaderString(String[][] headers) {
		StringBuilder sBuilder = new StringBuilder("");
		for(String[] headerField : headers) {
			if(headerField.length != 2) throw new RuntimeException("Invalid header field: no key value pair.");
			sBuilder.append(headerField[0]);
			sBuilder.append(":");
			sBuilder.append(headerField[1]);
			sBuilder.append(HttpClient.LINE_SEPARATOR_STRING);
		}
		return sBuilder.toString();
	}
}
