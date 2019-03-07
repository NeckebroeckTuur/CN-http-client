import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public class HttpListener implements Runnable {

	private HttpClient client;
	private DataInputStream in;
	private File outputFile;
	private PrintStream outputFileStream;
	
	public HttpListener(HttpClient client, File outputFile) {
		this.client = client;
		setOutputFile(outputFile);
		try {
			in = new DataInputStream(this.client.getSocket().getInputStream());
			(new Thread(this)).start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
		if(this.outputFile == null) return;
		try {
			outputFileStream = new PrintStream(this.outputFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void printToFile(String s) {
		if(outputFileStream != null) {
			outputFileStream.print(s);
			outputFileStream.flush();
		}
	}
	
	private void printToFile(char[] s) {
		if(outputFileStream != null) {
			outputFileStream.print(s);
			outputFileStream.flush();
		}
	}
	
	private void closeFile() {
		if(outputFileStream != null) {
			outputFileStream.close();
		}
	}
	
	@Override
	public void run() {
		try {
			//StringBuilder sBuilder = new StringBuilder(8096);
			CharBuffer b = CharBuffer.allocate(8096);
			//TODO size kunnen verdedigen
			int c = 0;
			while((c=in.read()) != -1) {
				b.put((char) c);
				//sBuilder.append((char)c);
			}
			//String result = sBuilder.toString();
			
			char[] result = b.array();
			printToFile(result);
			System.out.println(result);
			closeFile();
			HttpResponse response = new HttpResponse(result);
			response.parse();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
