import java.io.BufferedReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class HttpListener implements Runnable {

	private HttpClient client;
	private InputStream in;
	private File outputFile;
	private FileOutputStream fileOutputStream;
	private FileWriter fileWriter;
	
	// TODO duplicate in HttpResponse
	private final static int HEADER_SEPARATOR = 0x0d0a0d0a; 						// /r/n/r/n
	

	
	public HttpListener(HttpClient client, File outputFile) {
		this.client = client;
		setOutputFile(outputFile);
		try {
			in = this.client.getSocket().getInputStream();
			(new Thread(this)).start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	// TODO LINK VERWIJDEREN http://www.cafeaulait.org/course/week12/22.html
	@Override
	public void run() {
        InputStreamReader inputStreamReader = new InputStreamReader(this.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder headerBuilder = new StringBuilder(1024);						// most http header experimented with fit within 1024 bytes (ex. www.google.com -> )
        
        int c;
        int lastBytes = 0;
        try {
        	while ((c = bufferedReader.read()) != -1) {
        		lastBytes = (lastBytes << 8) | c;									// int lastBytes keeps the last 4 received bytes: when this is equal to HEADER_SEPARATOR, the end of the http-header is reached
        		headerBuilder.append((char)c);
        		if(lastBytes == HEADER_SEPARATOR) {
            		break;
        		}
        	}
        	int headerLength = headerBuilder.length();
        	headerBuilder.delete(headerLength-4, headerLength);
        	String headerString = headerBuilder.toString();
        	System.out.println(headerString);
        	System.out.println();
        	HttpHeader header = new HttpHeader(headerString);
        	header.parse();
        	System.out.println(String.format("Http version: %s\nHttp status: %d\nHttp status string:%s\n", header.getHttpVersion(), header.getHttpStatusCode(),header.getHttpStatusString()));
        	
        	for(String f:header.headerFields.keySet()) {
        		System.out.println(String.format("(field, value) = (%s,\"%s\")", f, header.headerFields.get(f)));
        	}
        	
        	System.out.println("Contentslength: "+header.getContentsLength());
        } catch(IOException e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        }
	}
	
	
	/*
	@Override
	public void run() {
		try {
			//StringBuilder sBuilder = new StringBuilder(8096);
			CharBuffer buffer = CharBuffer.allocate(8096);
			//TODO size kunnen verdedigen
			char c = 0;
			while((c=(char)in.read()) != 0xffff) {
				buffer.put(c);
				//sBuilder.append((char)c);
			}
			//String result = sBuilder.toString();
			
			char[] result = buffer.array();
			//printToFile(result);
			System.out.println(result);
			//closeFile();
			HttpResponse response = new HttpResponse(result);
			response.parse();
			//printToFile(response.getData());
			System.out.println("\n\n---------\n---------");
			char[] data= response.getData();
			HttpResponse.print(data);
			printToFile(data);
			closeFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
	
	
	private void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
		if(this.outputFile == null) return;
		try {
			//fileOutputStream = new FileOutputStream(this.outputFile);
			fileWriter = new FileWriter(this.outputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void printToFile(char[] bytes) {
		/*if(fileOutputStream != null) {
				fileOutputStream.write();
				fileOutputStream.flush();
		}*/
		if(fileWriter != null) {
			try {
				fileWriter.write(bytes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	
	private void closeFile() {
		/*if(fileOutputStream != null) {
			fileOutputStream.close();
		}*/
		if(fileWriter !=null) {
			try {
				fileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
