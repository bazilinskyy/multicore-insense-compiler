package uk.ac.stand.cs.insense.compiler;

import java.net.URL;

import uk.ac.stand.cs.insense.compiler.cgen.ICodeGenerator;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.Cgen;
import uk.ac.stand.cs.insense.compiler.interfaces.ICompilerErrors;
import uk.ac.stand.cs.insense.compiler.interfaces.ILexicalAnalyser;
import uk.ac.stand.cs.insense.compiler.interfaces.ISourceRepresentation;
import uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable;
import uk.ac.stand.cs.insense.compiler.interfaces.ISyntaxAnalyser;
import uk.ac.stand.cs.insense.compiler.symbols.SymbolTable;
import uk.ac.stand.cs.insense.compiler.types.TypeChecker;
import uk.ac.standrews.cs.nds.util.ErrorHandling;

/**
 * DIAS Project
 * Insense
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2006
 *  * @author <a href="mailto:al@dcs.st-and.ac.uk"> Alan Dearle </a>
 */

public class BaseCompilerAssembly {
	
	public static final String MODULE_NAME = PARSER_META.MODULE_NAME;
	public static final double VERSION = PARSER_META.VERSION;
	public static final String USAGE_MESSAGE = "usage: java " + BaseCompilerAssembly.class.getName() +  " <filename> [<output directory>]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	public static final int INPUT_FILE_INDEX = 0;
	public static final int OUTPUT_DIRECTORY_INDEX = 1;
	public static final int PROJECT_NAME_INDEX = 2;
	
	public static final String stdenvRelativePath = Messages.getString("BaseCompilerAssembly.StandardDefsFilePath");
	
	public static int compile( String fileLocation, String project_name ) throws Exception {
		ISourceRepresentation source = new SourceFile( fileLocation );
		return compile( source, project_name );
	}
	
	public static int compile( ISourceRepresentation source, String project_name ) throws Exception {		
		ICompilerErrors ce = new CompilerErrors();
		TypeChecker tc = new TypeChecker( ce );
		ICodeGenerator cgen = new Cgen( ce, project_name );
		ISymbolTable scope = SymbolTable.newScope( ISymbolTable.GLOBAL, ISymbolTable.GLOBAL );
		
		// First compile the standard environment
		URL u = BaseCompilerAssembly.class.getResource( stdenvRelativePath );
		if( u == null ) {
			ErrorHandling.hardError( "Cannot obtain URL for path: ",stdenvRelativePath );
		}
		ISourceRepresentation stdenvdata = new SourceFile( u );
		ILexicalAnalyser lex = new LexicalAnalyser( stdenvdata, ce );
		ISyntaxAnalyser syntax = new StandardEnvironmentBuilder( lex, tc, ce, cgen, scope );
		syntax.parse();
		
		// then compile the user program
		lex = new LexicalAnalyser( source, ce );
		syntax = new SyntaxAnalyser( lex, tc, ce, cgen, scope );
		syntax.parse();
		cgen.finish();
		return ce.getErrorCount();
	}
}
