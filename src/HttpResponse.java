import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public class HttpResponse {
	

	private char[] rawResponse;
	private boolean parsed = false;
	private String headerString;
	private char[] data;
	
	private final static Pattern SRC_PATTERN = Pattern.compile("src=\"(.+?)\"");
	private final static Pattern SRC_AD_PATTERN = Pattern.compile("src=\"ad\\d*\\..*?\"");
	
	//TODO duplicate in HttpListener
	private final static char[] HEADER_SEPARATOR = {0x0d, 0x0a, 0x0d, 0x0a};

	
	public HttpResponse(char[] response) {
		this.rawResponse = response;
	}
	
	
	// TODO LINK VERWIJDEREN https://www.webnots.com/what-is-http/
	public void parse() {
		if(this.rawResponse == null) return;
		String allData = new String(this.rawResponse);
		String headerSeparator = new String(HttpResponse.HEADER_SEPARATOR);
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
		Matcher srcMatcher = HttpResponse.SRC_PATTERN.matcher(htmlPage);
		Matcher adMatcher = HttpResponse.SRC_AD_PATTERN.matcher("");
		
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
	
	
	// TODO VERWIJDEREN
	/**
	 * Print a given char array in hexadecimal format on one line on the console (System.out).
	 * 
	 * @deprecated
	 * @param chars The array of chars to be printed.
	 */
	public static void print(char[] chars) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("[ ");
	    for (char c : chars) {
	    	byte b = (byte)(0xff & c);
	        sb.append(String.format("0x%02X ", b));
	    }
	    sb.append("]");
	    
	    System.out.println(sb.toString());
	}
}
