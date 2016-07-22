package co.codewizards.cloudstore.local;

public class IgnoreRule {
	
	private String regex;
	
	public IgnoreRule(String regex) {
		
		this.regex = regex;
	}

	public String getRegex() {
		
		return this.regex;
	}
	
	public void setRegex(String regex) {
		
		this.regex = regex;
	}
}
