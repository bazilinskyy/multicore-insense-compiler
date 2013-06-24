/*
 * Created on 28-Jun-2006 at 11:36:28.
 */
package uk.ac.stand.cs.insense.compiler.types;

import uk.ac.stand.cs.insense.compiler.interfaces.ICompilerErrors;
import uk.ac.stand.cs.insense.compiler.interfaces.ITypeChecker;

/**
 * DIAS Project
 * Insense
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2006
 * @author <a href="mailto:al@dcs.st-and.ac.uk"> AAlan Dearle </a>
 */

public class TypeChecker implements ITypeChecker {

    
    private ICompilerErrors compilerError;

    /**
     * 
     */
    public TypeChecker( ICompilerErrors ce ){
            this.compilerError = ce;      
    }
    
    public void match(ITypeRep typeExpected, ITypeRep found ) {
        if ( ! typeExpected.equals( found ) ) {
            compilerError.typeError( typeExpected, found );
        }
    }

    public TypeRep coerce(ITypeRep a, ITypeRep b) {
    	if (a.equals( UnknownType.TYPE ) || b.equals( UnknownType.TYPE ) ) {
    		return UnknownType.TYPE;
    	} else if( a.equals( RealType.TYPE ) ) {
    		if (b.equals( IntegerType.TYPE ) || b.equals( UnsignedIntegerType.TYPE ) || b.equals( ByteType.TYPE ) ) {
    			return RealType.TYPE;
    		} else {
    			match( RealType.TYPE, b );
    			return RealType.TYPE;
    		}
    	} else if( a.equals( IntegerType.TYPE ) ) {
    		if( b.equals( RealType.TYPE ) ) {
    			return RealType.TYPE;
    		} else if ( b.equals( UnsignedIntegerType.TYPE ) )  {
    			return IntegerType.TYPE;
    		} else if ( b.equals( ByteType.TYPE ) )  {
    			return IntegerType.TYPE;
    		} else {
    			match( IntegerType.TYPE, b );
    			return IntegerType.TYPE;
    		}
    	} else if (a.equals(UnsignedIntegerType.TYPE ) ) {
    		if( b.equals( IntegerType.TYPE ) ) {
    			return IntegerType.TYPE;
    		} else if( b.equals( RealType.TYPE ) ) {
    			return RealType.TYPE;
    		} else if ( b.equals( ByteType.TYPE ) )  {
    			return UnsignedIntegerType.TYPE;
    		} else {
    			match( UnsignedIntegerType.TYPE, b );
    			return UnsignedIntegerType.TYPE;
    		}
    	} else if (a.equals(ByteType.TYPE ) ) {
    		if( b.equals( IntegerType.TYPE ) ) {
    			return IntegerType.TYPE;
    		} else if ( b.equals( UnsignedIntegerType.TYPE ) )  {
    			return UnsignedIntegerType.TYPE;
    		} else if( b.equals( RealType.TYPE ) ) {
    			return RealType.TYPE;
    		} else {
    			match( ByteType.TYPE, b );
    			return ByteType.TYPE;
    		}
    	} else {
    		compilerError.typeError( a, b );
    		return UnknownType.TYPE;
    	}
    }

    public TypeRep integerCoerce(ITypeRep a, ITypeRep b) {
    	if (a.equals( UnknownType.TYPE ) || b.equals( UnknownType.TYPE ) ) {
    		return UnknownType.TYPE;
    	} else if( a.equals( IntegerType.TYPE ) ) {
    		if ( b.equals( UnsignedIntegerType.TYPE ) )  {
    			return IntegerType.TYPE;
    		} else if ( b.equals( ByteType.TYPE ) )  {
    			return IntegerType.TYPE;
    		} else {
    			match( IntegerType.TYPE, b );
    			return IntegerType.TYPE;
    		}
    	} else if (a.equals(UnsignedIntegerType.TYPE ) ) {
    		if( b.equals( IntegerType.TYPE ) ) {
    			return IntegerType.TYPE;
    		} else if ( b.equals( ByteType.TYPE ) )  {
    			return UnsignedIntegerType.TYPE;
    		} else {
    			match( UnsignedIntegerType.TYPE, b );
    			return UnsignedIntegerType.TYPE;
    		}
    	} else if (a.equals(ByteType.TYPE ) ) {
    		if( b.equals( IntegerType.TYPE ) ) {
    			return IntegerType.TYPE;
    		} else if ( b.equals( UnsignedIntegerType.TYPE ) )  {
    			return UnsignedIntegerType.TYPE;
    		} else {
    			match( ByteType.TYPE, b );
    			return ByteType.TYPE;
    		}
    	} else {
    		compilerError.typeError( a, b );
    		return UnknownType.TYPE;
    	}
    }


}
