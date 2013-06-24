package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import uk.ac.stand.cs.insense.compiler.cgen.IEnumDeclaration;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.fileHandling.HeaderFile;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.fileHandling.OutputFile;
import uk.ac.stand.cs.insense.compiler.types.EnumType;
import uk.ac.standrews.cs.nds.util.Diagnostic;
import uk.ac.standrews.cs.nds.util.ErrorHandling;

public abstract class EnumDeclaration extends Code implements IEnumDeclaration {
	
	/**
	 * Tracks already generated enums 
	 * Maps from concatenatedlabeltypes to enum name
	 */
	private static List<String> generatedAlready = new ArrayList<String>();

	/**
	 * @return true if the enum has been generated already in this compilation unit
	 */
	private static boolean generatedAlready( EnumType type ) {
		return generatedAlready.contains( type.getName() );
	}
	
	/**
	 * Generate a enum decl for this type
	 * @see uk.ac.stand.cs.insense.compiler.incesosCCgen.Code#complete()
	 */
	public static void generate( EnumType type ) {	
		if( ! generatedAlready( type ) ) {
			generateHeaderFile( type );
		}
		Cgen.get_instance().addIncludeToCurrentContext( include_headers( type ) );
	}
	
	/**
	 * Generate a enum decl for this type.
	 */	
	private static String generateEnumDecl( EnumType type ) {
		String enum_name = type.getName();
		StringBuffer sb = new StringBuffer();
		sb.append( GENERATED_FROM + Diagnostic.getMethodInCallChain() + NEWLINE );
		sb.append( TYPEDEF_ + ENUM_ + SPACE + LCB_ );
		boolean first = true;
		for( String lab : type.getLabels() ) {
			if( ! first ) {
				sb.append( COMMA + SPACE );
			}
			sb.append( lab );
			first = false;
		}
		sb.append( SPACE + RCB_ + enum_name + "_t" + COMMA + SPACE + enum_name + SEMI + NEWLINE );
		generatedAlready.add( enum_name );
		return sb.toString();
	}	
		
	// File handling stuff
	
	private static String include_filename( EnumType type ) {
		return type.getName() + ".h";
	}
	
	private static String header_name( EnumType type ) {
		return type.getName().toUpperCase() + "_H_";
	}
	
	protected static String include_headers( EnumType type ) {
		//return HASH_INCLUDE_ + DQUOTE + include_filename( type ) + DQUOTE;
		// JL changed above to below
		return include_filename( type );
	}
	
	/**
	 * Writes the standard include file headers to the stream
	 * @param ps - the stream on which the decls are written
	 */
	private static void printDOTHHeaders( PrintStream ps, EnumType type ) {
		ps.println( GENERATED_FROM + Diagnostic.getMethodInCallChain() );
		ps.println( IFNDEF_ + header_name(type) );
		ps.println( DEFINE_ + header_name(type) );
		ps.println();
	}
	
	/**
	 * Writes the standard include file trailers to the stream
	 * @param ps - the stream on which the decls are written
	 */
	private static void printTrailers( PrintStream ps, EnumType type ) {
		ps.println( ENDIF_ + C_COMMENT_OPEN_ + header_name(type) + C_COMMENT_CLOSE_ );
		ps.println();
	}
	
	private static void generateHeaderFile( EnumType type )  { 
		try {
			OutputFile f = new HeaderFile( include_filename(type) );
			PrintStream ps = f.getStream();
			printDOTHHeaders( ps,type );
			ps.println( generateEnumDecl(type) );
			printTrailers( ps,type );
			f.close();
		} catch( IOException e ) {
			ErrorHandling.exceptionError(e, "Opening file: " + include_filename(type) );
		}
	}
	
	
}
