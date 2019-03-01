
public enum HTTPCommand {
	HEAD("HEAD"), GET("GET"), PUT("PUT"), POST("POST");
	
	String command;
	
	HTTPCommand(String commandString) {
		this.command = commandString;
	}
	
	public String getCommandString() {
		return this.command;
	}
}
