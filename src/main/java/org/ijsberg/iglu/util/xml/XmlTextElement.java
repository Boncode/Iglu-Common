package org.ijsberg.iglu.util.xml;

/**
 */
public class XmlTextElement implements XmlElement {
	private int contents;
	private int startLineNr;
	private int endLineNr;

	public XmlTextElement(String contents, int startLineNr, int endLineNr) {
		this.contents = StringCache.storeString(contents);
		this.startLineNr = startLineNr;
		this.endLineNr = endLineNr;
	}

	public String getContents() {
		return StringCache.getString(contents);
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
		return StringCache.getString(contents);
	}

	public int hashCode() {
		return contents;
	}

	public boolean equals(Object other) {
		return other instanceof XmlTextElement && ((XmlTextElement)other).contents == contents;
	}
}
