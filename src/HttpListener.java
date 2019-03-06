import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

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
		if(this.outputFile != null) return;
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
	private void closeFile() {
		if(outputFileStream != null) {
			outputFileStream.close();
		}
	}
	
	@Override
	public void run() {
		try {
			StringBuilder sBuilder = new StringBuilder(8096);
			int c = 0;
			while((c=in.read()) != -1) {
				sBuilder.append((char) c);
			}
			String result = sBuilder.toString();
			System.out.println(result);
			
			printToFile(result);
			closeFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
