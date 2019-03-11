import java.io.BufferedReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import javax.sound.midi.Soundbank;


public class HttpListener implements Runnable {

	private HttpClient client;
	private InputStream inputStream;
	private File outputFile;
	private FileOutputStream fileOutputStream;
	//private FileWriter fileWriter;
	
	// TODO duplicate in HttpResponse
	private final static int HEADER_SEPARATOR = 0x0d0a0d0a; 						// /r/n/r/n
	

	
	public HttpListener(HttpClient client, File outputFile) {
		this.client = client;
		setOutputFile(outputFile);
		try {
			inputStream = this.client.getSocket().getInputStream();
			(new Thread(this)).start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	@Override
	public void run() {
		try {
			StringBuilder headerBuilder = new StringBuilder();
			int c;
			int lastFourBytes=0;
			CharBuffer charBuffer = new CharBuffer(4096);
			
			// Read http header
			while ((c=this.inputStream.read()) != -1) {				// i between 0 ant 255
				if(charBuffer.isFull()) {
					headerBuilder.append(charBuffer.getBuffer());
					charBuffer.reset();
				}
				charBuffer.addChar((char)c);
				
				lastFourBytes = (lastFourBytes << 8) | (0xff & c);	// 0xff & for extra safety, normally redundant bc. i between 0 and 255
				if(lastFourBytes == HttpListener.HEADER_SEPARATOR) {
					// end of header reached
					headerBuilder.append(charBuffer.getBuffer(),0, charBuffer.getElementPointer()-4);
					System.out.println(headerBuilder.toString());
					
					break;
				}				
			}
			charBuffer.reset();

			
			// Read http body
			ByteBuffer byteBuffer = new ByteBuffer(4096);
			while ((c=this.inputStream.read(byteBuffer.buffer)) != -1) {				// i between 0 ant 255
					fileOutputStream.write(byteBuffer.buffer, 0, c);
					fileOutputStream.flush();
					charBuffer.reset();
			}
			fileOutputStream.close();
			
		} catch(IOException e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        }
	}
	*/
	
	@Override
	public void run() {
		try {
			StringBuilder headerBuilder = new StringBuilder();
			int c;
			int lastFourBytes=0;
			ByteBuffer byteBuffer = new ByteBuffer(4096);
			String rawHeader="";
			
			// Read http header
			while ((c=this.inputStream.read()) != -1) {				// i between 0 ant 255
				if(byteBuffer.isFull()) {
					headerBuilder.append(byteBuffer.getBuffer());
					byteBuffer.reset();
				}
				byteBuffer.addByte((byte)c);
				
				lastFourBytes = (lastFourBytes << 8) | (0xff & c);	// 0xff & for extra safety, normally redundant bc. i between 0 and 255
				if(lastFourBytes == HttpListener.HEADER_SEPARATOR) {
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
			while ((c=this.inputStream.read(byteBuffer.buffer)) != -1) {				// i between 0 ant 255
					fileOutputStream.write(byteBuffer.buffer, 0, c);
					fileOutputStream.flush();
					byteBuffer.reset();
			}
			fileOutputStream.close();
			
		} catch(IOException e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        }
	}
	
	
		
	private void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
		if(this.outputFile == null) return;
		try {
			fileOutputStream = new FileOutputStream(this.outputFile);
			//fileWriter = new FileWriter(this.outputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	/*class CharBuffer {
		
		private char[] buffer;
		private int size;
		private int elementPointer=0;
		
		public CharBuffer(int size) {
			this.size = size;
			this.buffer=new char[this.size];
		}
		
		public void addChar(char c) {
			if(isFull()) throw new RuntimeException("CharBufferOverflow");
			this.buffer[elementPointer++]=c;
		}
		
		public boolean isFull() {
			return this.elementPointer == this.size;
		}
		
		public void reset() {
			this.buffer = new char[this.size];
			this.elementPointer=0;
		}
		
		public char[] getBuffer() {
			return this.buffer;
		}
		
		public char[] getTrimmedBuffer() {
			return Arrays.copyOf(getBuffer(), elementPointer);
		}
		
		public int getElementPointer() {
			return this.elementPointer;
		}
		
		public int getSize() {
			return this.size;
		}
	}*/
	
	
	class ByteBuffer {
		
		private byte[] buffer;
		private int size;
		private int elementPointer=0;
		
		public ByteBuffer(int size) {
			this.size = size;
			this.buffer=new byte[this.size];
		}
		
		public void addByte(byte c) {
			if(isFull()) throw new RuntimeException("ByteBufferOverflow");
			this.buffer[elementPointer++]=c;
		}
		
		public boolean isFull() {
			return this.elementPointer == this.size;
		}
		
		public void reset() {
			this.buffer = new byte[this.size];
			this.elementPointer=0;
		}
		
		public byte[] getBuffer() {
			return this.buffer;
		}
		
		public byte[] getTrimmedBuffer() {
			return Arrays.copyOf(getBuffer(), elementPointer);
		}
		
		public int getElementPointer() {
			return this.elementPointer;
		}
		
		public int getSize() {
			return this.size;
		}
	}
	
}
