package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import uk.ac.stand.cs.insense.compiler.cgen.IUnionDeclaration;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.fileHandling.HeaderFile;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.fileHandling.OutputFile;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;
import uk.ac.standrews.cs.nds.util.Diagnostic;
import uk.ac.standrews.cs.nds.util.ErrorHandling;

public abstract class UnionDeclaration extends TypeMarshaller implements IUnionDeclaration {

	protected List<ITypeRep> armTypes;
	
	/**
	 * Tracks already generated Unions 
	 * Maps from concatenatedBranchtypes to union name
	 */
	private static List<String> generatedAlready = new ArrayList<String>();
	
	public UnionDeclaration() {
		armTypes = new ArrayList<ITypeRep>();
	}
	
	/**
	 * @return true if the struct has been generated already in this compilation unit
	 */
	private boolean generatedAlready() {
		return generatedAlready.contains( unionName() );
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IUnionDeclaration#armType(uk.ac.stand.cs.insense.compiler.types.ITypeRep)
	 */
	public void armType( ITypeRep tr ) {
		armTypes.add( tr );
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IUnionDeclaration#complete()
	 */
	public void complete() {	
		Cgen.get_instance().addIncludeToCurrentContext( include_headers() );
		if( ! generatedAlready() ) {
			generateHeaderFile();
		}
	}

	/**
	 * Generate a struct decl for this type and generate the construction of the tuple.
	 */	
	private String generateUnionDecl() {
		StringBuffer sb = new StringBuffer();
		sb.append( GENERATED_FROM + Diagnostic.getMethodInCallChain() + NEWLINE );
		sb.append( TYPEDEF_ + UNION_ + unionTypeName() + SPACE + LCB_ + NEWLINE );
		sb.append( generateArms() );
		sb.append( RCB_ +  unionStructName() + SEMI + NEWLINE );
		sb.append( NEWLINE );
		generatedAlready.add( unionName() );
		return sb.toString();
	}
	
	private String generateArms() {
		StringBuffer sb = new StringBuffer();
		int index = 0;
		for( ITypeRep tr : armTypes ) {
			sb.append( TAB + insenseTypeToCTypeName(tr) + SPACE + unionLabelName(tr) + SEMI + NEWLINE );
		}
		return sb.toString();
	}	
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IUnionDeclaration#unionName()
	 */
	public String unionName() {
		StringBuffer sb = new StringBuffer();
		sb.append( "union_" );
		for( ITypeRep tr : armTypes ) {
			
			sb.append( removeTrailingSpace( insenseTypeToCTypeName(tr) ) );
		}
		return sb.toString();
	}
	
	private static String removeTrailingSpace( String s ) {
		if( s.endsWith( " " ) ) {
			return s.substring( 0,s.length() - 1 );
		}
		else return s;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IUnionDeclaration#unionTypeName()
	 */
	public String unionTypeName() {
		return unionName() + "_t";
	}	

	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IUnionDeclaration#unionStructName()
	 */
	public String unionStructName() {
		return unionName() + "Union";
	}
	
	
	public static String unionLabelName(ITypeRep type) {
		return removeTrailingSpace( insenseTypeToCTypeName( type ) ) + "_val";
	}		
	
	// File handling stuff
	
	private String include_filename() {
		return unionName() + ".h";
	}
	
	private String header_name() {
		return unionName().toUpperCase() + "_H_";
	}
	
	protected String include_headers() {
		return include_filename();
	}
	
	/**
	 * Writes the standard include file headers to the stream
	 * @param ps - the stream on which the decls are written
	 */
	private void printDOTHHeaders( PrintStream ps ) {
		ps.println( GENERATED_FROM + Diagnostic.getMethodInCallChain() );
		ps.println( IFNDEF_ + header_name() );
		ps.println( DEFINE_ + header_name() );
		ps.println();
	}
	
	/**
	 * Writes the standard include file trailers to the stream
	 * @param ps - the stream on which the decls are written
	 */
	private void printTrailers( PrintStream ps ) {
		ps.println( ENDIF_ + C_COMMENT_OPEN_ + header_name() + C_COMMENT_CLOSE_ );
		ps.println();
	}
	
	private void generateHeaderFile()  { 
		try {
			OutputFile f = new HeaderFile( include_filename() );
			PrintStream ps = f.getStream();
			printDOTHHeaders( ps );
			ps.println( generateUnionDecl() );
			printTrailers( ps );
			f.close();
		} catch( IOException e ) {
			ErrorHandling.exceptionError(e, "Opening file: " + include_filename() );
		}
	}
	
	
}
