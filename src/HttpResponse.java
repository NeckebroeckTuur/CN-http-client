import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HttpResponse {
	private char[] rawResponse;
	private boolean parsed = false;
	private String headerString;
	private char[] data;
	
	private final byte[] HEADER_SEPARATOR = {0x0d, 0x0a, 0x0d, 0x0a};

	
	public HttpResponse(char[] response) {
		this.rawResponse = response;
	}
	
	
	// https://www.webnots.com/what-is-http/
	public void parse() {
		if(this.rawResponse == null) return;
		String allData = new String(this.rawResponse);
		String headerSeparator = new String(HEADER_SEPARATOR, StandardCharsets.UTF_8);
		int splitIndex = allData.indexOf(headerSeparator);
		
		this.headerString = allData.substring(0, splitIndex);
		this.data = Arrays.copyOfRange(rawResponse,splitIndex+4, rawResponse.length-1);
		// TODO lengte moet niet berekend worden, kan uit header gehaald worden
		// System.out.println(String.format("\n\nHEADER:\n\"%s\"\n\nHTML:\n\"%s\"", headerString, htmlString));
		parsed = true;
	}
	
	public char[] getData() throws IllegalStateException{
		if(!parsed) {
			throw new IllegalStateException("The response has not been parsed yet. Call the parse() function first.");
		}
		return this.data;
	}
}
