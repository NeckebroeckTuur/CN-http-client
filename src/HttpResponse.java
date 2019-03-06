
public class HttpResponse {
	private String rawResponse;
	private boolean parsed = false;
	private String headerString;
	private String htmlString;
	
	public HttpResponse(String response) {
		this.rawResponse = response;
	}
	
	
	// https://www.webnots.com/what-is-http/
	public void parse() {
		if(this.rawResponse == null) return;
		int splitIndex = rawResponse.indexOf("\r\n\r\n");
		this.headerString = rawResponse.substring(0, splitIndex);
		this.htmlString = rawResponse.substring(splitIndex+4);
		// System.out.println(String.format("\n\nHEADER:\n\"%s\"\n\nHTML:\n\"%s\"", headerString, htmlString));
		parsed = true;
	}
	
	public String getHtml() throws IllegalStateException{
		if(!parsed) {
			throw new IllegalStateException("The response has not been parsed yet. Call the parse() function first.");
		}
		return this.htmlString;
	}
}
