package org.ijsberg.iglu.util.xml;

/**
 */
public class XmlTextElement implements XmlElement {
	private String contents;
	private int startLineNr;
	private int endLineNr;

	public XmlTextElement(String contents, int startLineNr, int endLineNr) {
		this.contents = contents;
		this.startLineNr = startLineNr;
		this.endLineNr = endLineNr;
	}

	public String getContents() {
		return contents;
	}

	public int getStartLineNr() {
		return startLineNr;
	}

	public int getEndLineNr() {
		return endLineNr;
	}

	public int getNrofLines() {
		return this.endLineNr - this.startLineNr + 1;
	}


	public String toString() {
		return contents;
	}

	public int hashCode() {
		return contents.hashCode();
	}

	public boolean equals(Object other) {
		return other instanceof XmlTextElement && ((XmlTextElement)other).contents.equals(contents);
	}
}
