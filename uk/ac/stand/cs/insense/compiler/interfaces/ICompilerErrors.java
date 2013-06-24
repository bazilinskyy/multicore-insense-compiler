/*
 * Created on 27-Jun-2006 at 09:08:49.
 */
package uk.ac.stand.cs.insense.compiler.interfaces;

import uk.ac.stand.cs.insense.compiler.LexicalAnalyser;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;

/**
 * DIAS Project
 * Essence
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2006
 * @author <a href="mailto:al@dcs.st-and.ac.uk"> AAlan Dearle </a>
 */

public interface ICompilerErrors {
    public int getErrorCount();
    
    public void warning( String msg );
    
    public void generalError( String msg );
    
    public void syntaxError( String found, String expected );

    public void expressionError( String msg );

    public void typeError( ITypeRep t1, ITypeRep t2 );
    
    public void typeError( ITypeRep tr, String s );
    
    public void badType(ITypeRep found);
    
    public void nameDeclaredError( String the_name );
    
    public void nameUndeclaredError(String the_name );
    
    public void typeForValue( String found );
    
    public void valueForType( String found );
    
    public void constructorError( ITypeRep formal, ITypeRep actual );
    
    public void missingConstructor();

    public void dereferenceError(ITypeRep t, String the_name);

    public void dereferenceError(ITypeRep t);

	public void setLex( ILexicalAnalyser lexicalAnalyser);
}
