import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.file.Files;

public class HttpListener implements Runnable {

	private HttpClient client;
	private DataInputStream in;
	private File outputFile;
	private FileOutputStream fileOutputStream;
	private FileWriter fileWriter;
	
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

}
