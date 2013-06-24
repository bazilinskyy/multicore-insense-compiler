/*
 * Created on 02-Jul-2006 at 21:43:11.
 */
package uk.ac.stand.cs.insense.compiler.types;

import java.util.LinkedList;
import java.util.List;

/**
 * DIAS Project
 * Insense
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2006
 * @author <a href="mailto:al@dcs.st-and.ac.uk"> Alan Dearle </a>
 */
public class FunctionType extends ConstructedType implements ITypeWithList {

    private List<ITypeRep> args = new LinkedList<ITypeRep>();
    private ITypeRep returnType = UnknownType.TYPE;
    private boolean isConstructor = false;
    
    public FunctionType() {
        super();
    }
    
    public void add( String name, ITypeRep paramType ) {
    // throws away name - to maximise code reuse in named_param_list
        args.add( paramType );
    }
    
    public void addParam( ITypeRep paramType ) {
    	args.add( paramType );
    }
    
    public void addReturn( ITypeRep returnType ) {
        this.returnType = returnType;
    }

    @Override
    public boolean equals(ITypeRep other) {
        return other instanceof FunctionType &&
               equalArgs( ((FunctionType) other).args ) &&
               returnType.equals( ((FunctionType) other).returnType );
    }

    private boolean equalArgs(List<ITypeRep> args2) {
        if( args.size() != args2.size() ) {
            return false;
        } else {
            for( int i = 0; i < args.size(); i++ ) {
                if( ! args.get(i).equals( args2.get(i) ) ) {
                    return false;
                }
            }
            return true;
        }
    }
    
    public boolean isConstructor() { return isConstructor; }
    
    public void setConstructor() { isConstructor = true; }
    
    public boolean hasArguments() {
        return args.size() > 0;   
    }

    public List<ITypeRep> getArgs() {
        return args;
    }

    public ITypeRep getResult() {
        return returnType;
    }
    
	public boolean isPointerType() {	// Note that these are not currently heap objects but may become so later
		return false;
	}
	
	
	public String toHumanReadableString() {
        String paramString = "";
        for( ITypeRep tr : args ) {
            if( paramString.equals( "" ) ) {
                paramString += tr.toHumanReadableString();
            } else {
                paramString += ", " + tr.toHumanReadableString();
            }
        }
        String constructname = isConstructor? "constructor" : " proc";
        // TODO need to deal with the void return type here
        String retTypeString = isConstructor ? "" : ":" + returnType.toHumanReadableString();    // no return for constructor
        return  constructname + "(" + paramString + ")" + retTypeString;
    }

	public String toStringRep() {
        String paramString = "";
        for( ITypeRep tr : args ) {
                paramString += tr.toStringRep() + DELIMITER;
        }
        return  FUNCTION_REP + paramString + DELIMITER;
	}

	public String getDefaultCValue() {
		return "NULL";
	}

	public int getCValueSizeIndicator() {
		return RELATIVE_POINTER_SIZE;
	}
}
