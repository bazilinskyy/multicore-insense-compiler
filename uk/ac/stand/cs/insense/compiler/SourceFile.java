package uk.ac.stand.cs.insense.compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;

import uk.ac.stand.cs.insense.compiler.interfaces.ISourceRepresentation;

/**
 * DIAS Project
 * Insense
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2006
 * @author <a href="mailto:ajm@dcs.st-and.ac.uk"> Andrew J. McCarthy </a>
 */
public class SourceFile implements ISourceRepresentation {
	private LineNumberReader lineReader;
    private char eot = '?';
    private StringBuffer current_line = new StringBuffer();
	
	/* Peeking vars */
	private char peeked;
	private boolean peekWaiting = false;
	
	public SourceFile(URL location) throws IOException {
		URL sourceLocation = location;
		lineReader = new LineNumberReader( new InputStreamReader( sourceLocation.openStream() ) );

	}
	public SourceFile(String filename) throws IOException {
		File file = new File( filename );
		if( file.exists() && file.canRead() ) {
			FileInputStream read = new FileInputStream( file );
			lineReader = new LineNumberReader( new InputStreamReader( read ) );
		} else {
			throw new IOException( "Cannot open file: " + filename);
		}
	}
	
	public char getNextChar() {
		if ( peekWaiting ) {
			peekWaiting = false;
			return peeked;
		}
		else{
			char toReturn;
            try {
                toReturn = (char) lineReader.read();
            } catch (IOException e) {
                return eot;
            }
			current_line.append(toReturn);
			return toReturn;
		}
	}
	
	public char peek() {
		if ( peekWaiting ) return peeked;
		else {
			peeked = (char) getNextChar();
			peekWaiting = true;
			return peeked;
		}
	}
	
	public int lineNumber() {
		return lineReader.getLineNumber() + 1; // these start at zero!
	}
	
	public String currentLine() {
		return current_line.toString();
	}
	
	public void resetCurrentLine() {
		current_line = new StringBuffer();
	}
}
