package uk.ac.stand.cs.insense.compiler.incesosCCgen.fileHandling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

public class OutputFile {
	private static final String DEFAULT_DIR = "output";
	private static String outputDir = null;
	private String fileName;
	private File f;
	private PrintStream s;
	
	public OutputFile( String fileName ) throws IOException {
		this.fileName = fileName;
		
		if (outputDir == null)
			outputDir = DEFAULT_DIR;
		
		verifyOutputDirectory();
		
		f = new File( outputDir, fileName );
		
		if( f.exists() ) {
			f.delete();
		}
		if( ! f.createNewFile() ) {
			throw new IOException( "file create failed" );
		}
	}
	
	public PrintStream getStream() throws FileNotFoundException {
		s = new PrintStream( f );
		return s;
	}
	
	public void close() {
		s.close();
	}
	
	public static void setOutputDirectory(String s)
	{
		outputDir = s;
	}
	
	private void verifyOutputDirectory() throws IOException {
		if (outputDir.equals("") || outputDir == null)
			return;
		
		File file = new File( outputDir );
		if( file.exists() && file.canRead() && file.isDirectory()) {
			return;
		} else if (!file.exists() && file.mkdir()) {
			return;
		} else {
			throw new IOException( "Cannot open or create directory: " + outputDir );
		}
	}
}