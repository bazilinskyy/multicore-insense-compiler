package uk.ac.stand.cs.insense.compiler.incesosCCgen.fileHandling;

import java.io.IOException;

public class HeaderFile extends OutputFile {
	public HeaderFile(String fileName) throws IOException {
		super(fileName);
		FileTracker.instance().addHeaderFile( fileName );
	}
	
}
