import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLParser {
	
	private final static String HTTP_PREFIX = "http://";
	private final static Pattern HOST_PATTERN = Pattern.compile("^http:\\/\\/((\\w+\\.?)*)(\\/.*)?");
	// Group 1: host
	// Group 3: path+file
	
	public static String[] parse(String url) {
		if(!url.startsWith(HTTP_PREFIX)) url = HTTP_PREFIX + url;
		Matcher matcher = URLParser.HOST_PATTERN.matcher(url);
		String host="", path="", file="";
				
		if(matcher.matches()) {
			host = matcher.group(1);
			String pathFile = matcher.group(3);
			if(pathFile == null) pathFile = "";
			if(pathFile.endsWith("/")) pathFile = pathFile.substring(0, pathFile.length() -1);
			if(pathFile.length() > 0) {
				int lastSlashIndex = pathFile.lastIndexOf("/");
				String deepestPath = pathFile.substring(lastSlashIndex+1, pathFile.length());
				if(deepestPath.contains(".")) {
					path = pathFile.substring(0,lastSlashIndex+1);
					file = deepestPath;
				} else {
					path = pathFile + "/";
				}
			}else {
				path = "/";
			}
			
			if(file.equals("")) file = "index.html";
			
		}else {
			throw new RuntimeException("Url does not match pattern.");
		}
		
		return new String[] {host, path, file};
	}
	
}
