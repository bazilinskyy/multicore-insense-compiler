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
 * University of St Andrews 2007
 * @author <a href="mailto:al@cs.st-and.ac.uk"> Alan Dearle </a>
 */
public class StructType extends ConstructedType implements ITypeWithList {
	
    private List<String> names = new LinkedList<String>();
    private List<ITypeRep> types = new LinkedList<ITypeRep>();
    
    public StructType() {
        super();
    }
    
    public void add( String fieldName, ITypeRep rep ) {
        names.add( fieldName );
        types.add(rep);
    }

    public List<ITypeRep> getFields() {
    	return types;
    }
   
    public ITypeRep getField( String name ) {
    	int i = 0;
    	for( String s : names ) {
    		if( s.equals( name ) ) {	// found it :)
    			return types.get( i );
    		}
    		i = i + 1;
    	}
    	return UnknownType.TYPE;
    }
    
    public List<String> getFieldNames() {
    	return names;
    }
    
	/**
	 * @return the concattenated field names
	 * used to generate a unique name for the struct
	 */
	private String concatFieldNames() {
		StringBuffer sb = new StringBuffer();
		for( String s : names ) {
			sb.append( s );
		}
		return sb.toString();
	}
	
	/**
	 * @return the concattenated field types
	 * used to generate a unique name for the struct
	 */
	private String concatFieldTypes() {
		StringBuffer sb = new StringBuffer();
		for( ITypeRep tr : types ) {
			sb.append( tr.toStringRep() ); 
		}
		return sb.toString();
	}
	
	/**
	 * @return a unique name for this type
	 */
	public String getName() {
		return "struct_" + toStringRep();
	}
    
    @Override
    public boolean equals(ITypeRep other) {
        return other instanceof StructType &&
        		equalFields( (((StructType) other).names), (((StructType) other).types ) );
    }

    private boolean equalFields( List<String> names2, List<ITypeRep> types2 ) {
        if( types.size() != types2.size() ) {
            return false;
        } else {
            for( int i = 0; i < types.size(); i++ ) {
                if( ! types.get(i).equals( types2.get(i) ) ) {
                    return false;
                }
            }
            for( int i = 0; i < names.size(); i++ ) {
                if( ! names.get(i).equals( names2.get(i) ) ) {
                    return false;
                }
            }
            return true;
        }
    }
    
    public String toHumanReadableString() {
        String fieldString = "";
        int index = 0;
        for( String name : names ) {
            if( index != 0 ) {
            	fieldString += ";";
            } 
            fieldString += types.get(index).toHumanReadableString() + " " + names.get(index);
            index++;
        }
        return "struct(" + fieldString + ")";
	}

    public String toStringRep() {
        String fieldString = "";
        int index = 0;
        for( String name : names ) {
            if( index != 0 ) {
            } 
            fieldString += types.get(index).toStringRep() + names.get(index) + DELIMITER ;
            index++;
        }
        return STRUCT_REP + fieldString + DELIMITER;
    }
    
	public boolean isPointerType() {
		return true;
	}

	public String getDefaultCValue() {
		return "NULL";
	}

	public int getCValueSizeIndicator() {
		return RELATIVE_POINTER_SIZE;
	}
}
