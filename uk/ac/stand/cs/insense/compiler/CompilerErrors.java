/*
 * Created on 27-Jun-2006 at 09:09:36.
 */
package uk.ac.stand.cs.insense.compiler;

import uk.ac.stand.cs.insense.compiler.interfaces.ICompilerErrors;
import uk.ac.stand.cs.insense.compiler.interfaces.ILexicalAnalyser;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;

/**
 * DIAS Project
 * Insense
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2006
 * @author <a href="mailto:al@dcs.st-and.ac.uk"> Alan Dearle </a>
 */

public class CompilerErrors implements ICompilerErrors {
    private int count = 0;
	private ILexicalAnalyser lexicalAnalyser = null;
   
	private String currentLine() {
		if( lexicalAnalyser == null ) return "";
		else return lexicalAnalyser.currentLine();
	}
	
	private String lineNumber() {
		if( lexicalAnalyser == null ) return "";
		else return "--- " + Integer.toString( lexicalAnalyser.lineNumber() ) + " --- ";
	}
	
    public int getErrorCount() { return count; }
    
    private void error( String msg ) {
        count++;
        System.out.println( lineNumber() + currentLine() );
        System.out.println( msg );
    }
    
    public void warning( String msg ) {
    	System.out.println( "***** Warning ***** " + lineNumber() + currentLine() );
        System.out.println( msg );
    }
    
    public void generalError( String msg ) {
    	error( "***** Error ***** " + msg + "\n" );
    }
    
    public void syntaxError( String found, String expected ) {
        error( "***** Syntax Error ***** " + found + " found where " +
                        expected + " expected\n" );
    }
    
    public void typeError( ITypeRep t, ITypeRep t1 ) {
            error( "**** Type error**** " + t.toHumanReadableString() + " and " +
                    t1.toHumanReadableString() + " are not compatible in this context\n" );
       // }
    } 
    
    public void typeError( ITypeRep t, String s ) {
            error( "**** Type error**** " + t.toHumanReadableString() + " and " +
                    s + " are not compatible in this context\n" );
    }    
    
    public void badType( ITypeRep t ) {
        error( "**** Type error**** " + t.toHumanReadableString() + " found\n" );
        
    }

    public void nameDeclaredError(String the_name ) {
        error( "**** Name error**** The name " + the_name + " is already declared\n" );   
    }
    
    public void nameUndeclaredError(String the_name ) {
        error( "**** Name error**** The name " + the_name + " is undeclared\n" );   
    }
    
    public void expressionError(String msg) {
        error(  "**** Expression error**** " + msg + "\n" );
        
    }

    public void valueForType(String found) {
        error( "**** Name error**** " +  found + " where type expected" + "\n" );
    }

    public void typeForValue(String found) {
        error( "**** Name error**** " +  found + " where value expected" + "\n" );
    }

    public void constructorError(ITypeRep formal, ITypeRep actual) {
        error( "**** Type error**** " +  formal.toHumanReadableString() + " does not contain: " + actual.toHumanReadableString() + "\n" );
    }
    
    public void missingConstructor() {
    	error( "**** Constructor not found\n" );
    }

    public void dereferenceError(ITypeRep t, String the_name) {
        error( "**** Dereference error**** " + t.toHumanReadableString() + " does not contain a field called " + the_name );
        
    }

    public void dereferenceError(ITypeRep t) {
        error( "**** Dereference error**** " + t.toHumanReadableString() + " cannot be deferenced" );
    }

	public void setLex(ILexicalAnalyser lexicalAnalyser) {
		this.lexicalAnalyser = lexicalAnalyser;
		
	}
}
