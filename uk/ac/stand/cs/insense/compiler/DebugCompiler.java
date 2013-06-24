package uk.ac.stand.cs.insense.compiler;

import uk.ac.stand.cs.insense.compiler.incesosCCgen.fileHandling.OutputFile;
import uk.ac.stand.cs.insense.compiler.interfaces.ISourceRepresentation;
import uk.ac.standrews.cs.nds.util.Diagnostic;
import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
import uk.ac.standrews.cs.nds.util.ErrorHandling;

/**
 * DIAS Project
 * Insense
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2006
 * @author <a href="mailto:al@dcs.st-and.ac.uk"> Alan Dearle </a>
 * @author <a href="mailto:ajm@dcs.st-and.ac.uk"> Andrew J. McCarthy </a>
 */

public class DebugCompiler extends BaseCompilerAssembly {
	
	public static void main(String[] args){
		System.out.println(BaseCompilerAssembly.MODULE_NAME + " version " + BaseCompilerAssembly.VERSION);
		Diagnostic.setLevel( DiagnosticLevel.FINAL );
		Diagnostic.trace("Insense debug compiler running\n" ,DiagnosticLevel.FULL );
		
		if(args.length==0){
			System.out.println(BaseCompilerAssembly.USAGE_MESSAGE);
			return;
		}
			
		try {
			String fileLocation = args[ INPUT_FILE_INDEX ];
			String outputDirectory = null;
			if (args.length >= 2)
				outputDirectory = args[ OUTPUT_DIRECTORY_INDEX ];
			
			String project_name = fileLocation;
			if( args.length >= 3) {
				project_name = args[ PROJECT_NAME_INDEX ];
			}
			
			ISourceRepresentation source = new SourceFile( fileLocation );
			OutputFile.setOutputDirectory(outputDirectory);
			
			int errors = compile( source, project_name );
            if( errors == 0 ) {
                System.out.println( "*** Program Compiles **** ");
            } else {
                System.out.println( "*** Compilation fails, number of errors = " + errors + " **** ");
            }
		} 
        catch (ArrayIndexOutOfBoundsException e) {
            usage();
        }
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void usage() {
		ErrorHandling.error( USAGE_MESSAGE );
	}
}