package uk.ac.stand.cs.insense.compiler.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import uk.ac.stand.cs.insense.compiler.BaseCompilerAssembly;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.fileHandling.OutputFile;
import uk.ac.standrews.cs.nds.util.Diagnostic;
import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
import uk.ac.standrews.cs.nds.util.FileUtil;

/**
 * @author Alan W.F. Boyd
 *
 */

@RunWith(value=Parameterized.class)
public class ValidBuildTests 
{
	private static final String TESTS_DIRECTORY = "insense_progs/tests/compile";
	
	private static final TestsData testsData = TestsData.instantiate();
	
	private String filename;
	private String path;
	
	@Before
	public void deactivateDiagnostics()
	{
		Diagnostic.setLevel( DiagnosticLevel.NONE );
	}

	@Parameters
	public static Collection data() throws IOException 
	{
		URI uri;
		try
		{
			uri = testsData.getClass().getResource("/" + TESTS_DIRECTORY + "/").toURI();
			File f = new File(uri.getPath());
			
			if( !f.isDirectory() )
				throw new IOException();
			
			ArrayList<Object[]> result = new ArrayList<Object[]>();
				
			String[] filenames = f.list();
			for( String fileName : filenames ) 
			{
				if (fileName.endsWith(".isf"))
					result.add(new Object[] {fileName});
			}
			
			return result;
		}
		catch (URISyntaxException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public ValidBuildTests(String file)
	{
		this.filename = file;
		
		try
		{
			URI uri = testsData.getClass().getResource("/" + TESTS_DIRECTORY + "/" + filename).toURI();
			this.path = uri.getPath();
		}
		catch (URISyntaxException e)
		{
			e.printStackTrace();
		}		
	}
	
	@Test
	public void compile()
	{
		String message = "\nTesting filename: " + filename + "\n";
		String comment = testsData.getComment(filename);
		String OutputDir = (new File(System.getProperty("java.io.tmpdir"))).getAbsolutePath() + File.separator + "InsenseTemp";
		
		int expectedErrors = testsData.getErrors(filename); // this is horrid - al
		
		try
		{
			OutputFile.setOutputDirectory(OutputDir);
			
			
			if (expectedErrors != -1 && comment != null)
			{
				message += "(" + comment + ")\n";
				int count = BaseCompilerAssembly.compile( path, "TEST"  );
				assertEquals(message, expectedErrors + " errors", count + " errors");

			}
			else
			{
				message += "(Unknown filename, assuming there should be no errors)\n";
				int count = BaseCompilerAssembly.compile( path, "TEST");
				assertEquals(message, "0 errors", count + " errors");
			}
		}
		catch (Exception e)
		{
			message += "Failed due to exception:\n" + e;
			fail(message);
		}
	}
}
