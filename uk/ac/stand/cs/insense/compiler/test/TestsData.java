/**
 * TestsData.java
 * insense
 *
 * @author Apr 26, 2007 Alan W.F. Boyd
 */
package uk.ac.stand.cs.insense.compiler.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.StringTokenizer;

import uk.ac.standrews.cs.nds.util.ErrorHandling;

/**
 * @author Alan W.F. Boyd
 * Based on TestProperties by A. Dearle & R.Connor
 *
 */
public class TestsData extends Properties
{
	private static final long serialVersionUID = -289971764566591143L;
	private static final String TESTS_FILE = "insense_progs/tests/compile/compiler_tests.txt";
	private static TestsData instance = new TestsData();
	
	public static TestsData instantiate()
	{
		return instance;
	}
	
	private TestsData() 
	{
		super();    
		init();    
	}
	
	private void init() 
	{
        FileInputStream in = null;
        String location = "";
        try 
        {
        	URI uri = getClass().getResource("/" + TESTS_FILE).toURI();
            location = uri.getPath();
            in = new FileInputStream(new File(location));
            load(in);
        } 
        catch (FileNotFoundException e) 
        {
            in = null;
            ErrorHandling.hardError("Can't find compiler tests file at " + location);
        } 
        catch (IOException e) 
        {
        	ErrorHandling.hardError("Can't read compiler tests file");
        }
		catch (URISyntaxException e)
		{
			e.printStackTrace();
		} 
        finally 
        {
            if (in != null) 
            {
                try 
                {
                    in.close();
                } 
                catch (IOException e) 
                {
                }
                in = null;
            }
        }
    }
	
	public int getErrors(String file)
	{
		String data = getProperty(file);
		if (data == null)
			return -1;
		
		StringTokenizer st = new StringTokenizer(data, "\t");
		
		return Integer.valueOf(st.nextToken());
	}
	
	public String getComment(String file)
	{
		String data = getProperty(file);
		if (data == null)
			return null;
		
		StringTokenizer st = new StringTokenizer(data, "\t");
		st.nextToken();
		
		return st.nextToken();
	}
}
