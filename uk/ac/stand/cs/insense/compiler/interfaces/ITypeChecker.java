/*
 * Created on 28-Jun-2006 at 11:41:36.
 */
package uk.ac.stand.cs.insense.compiler.interfaces;

import uk.ac.stand.cs.insense.compiler.types.ITypeRep;
import uk.ac.stand.cs.insense.compiler.types.TypeRep;

/**
 * DIAS Project
 * Insense
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2006
 * @author <a href="mailto:al@dcs.st-and.ac.uk"> AAlan Dearle </a>
 */

public interface ITypeChecker {
    public void match(ITypeRep typeExpected, ITypeRep found );
    public TypeRep coerce( ITypeRep a, ITypeRep b );
    public TypeRep integerCoerce(ITypeRep a, ITypeRep b);
}
