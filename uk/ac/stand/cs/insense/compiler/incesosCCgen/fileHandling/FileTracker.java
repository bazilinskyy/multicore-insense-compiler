package uk.ac.stand.cs.insense.compiler.incesosCCgen.fileHandling;

import java.util.ArrayList;
import java.util.List;

public class FileTracker {

	private List<String> header_filenames;
	private List<String> impl_filenames;
	
	private static FileTracker instance = new FileTracker();
	
	
	private FileTracker() {
		header_filenames = new ArrayList<String>();
		impl_filenames = new ArrayList<String>();
	}

	public static FileTracker instance() {
		return instance;
	}

	public void addHeaderFile(String fileName) {
		if( ! ( header_filenames.contains( fileName ) ) ) {
			header_filenames.add(fileName);
		}
	}

	public void addImplFile(String fileName) {
		if( ! ( impl_filenames.contains( fileName ) ) ) {
			impl_filenames.add(fileName);
		}
	}
	
	public List<String> getImplFilenames() {
		return impl_filenames;
	}

	public List<String> getHeaderFilenames() {
		return header_filenames;
	}
}
