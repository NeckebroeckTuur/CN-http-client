
public class HttpResponse {
	private String rawResponse;
	
	private String headerString;
	private String htmlString;
	
	public HttpResponse(String response) {
		this.rawResponse = response;
	}
	
	public void parse() {
		if(this.rawResponse == null) return;
		
	}
}
