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
public class EnumType extends ConstructedType {

    private List<String> labels = new LinkedList<String>();
    
    public EnumType() {
        super();
    }
    
    public void addLabel( String label ) {
        labels.add( label );
    }
    
    public List<String> getLabels() {
    	return labels;
    }

    @Override
    public boolean equals(ITypeRep other) {
        return other instanceof EnumType && equalLabels( ((EnumType) other).labels );
    }

	/**
	 * @return the concattenated field names
	 * used to generate a unique name for the struct
	 */
	private String concatLabelNames() {
		StringBuffer sb = new StringBuffer();
		for( String s : labels ) {
			sb.append( s );
		}
		return sb.toString();
	}
	
	/**
	 * @return a unique name for this type
	 */
	public String getName() {
		return concatLabelNames();
	}
	
    private boolean equalLabels(List<String> labels2) {
        if( labels.size() != labels2.size() ) {
            return false;
        } else {
            for( int i = 0; i < labels.size(); i++ ) {
                if( ! labels.get(i).equals( labels2.get(i) ) ) {
                    return false;
                }
            }
            return true;
        }
    }
    
	public boolean isPointerType() {
		return false;
	}
	
	public String toHumanReadableString() {
        String labelString = "";
        for( String s : labels ) {
            if( labelString.equals( "" ) ) {
                labelString += s;
            } else {
                labelString += "," + s;
            }
        }
        return "enum( " + labelString + ")";
    }


	public String toStringRep() {
        String labelString = "";
        for( String s : labels ) {
                labelString += s + DELIMITER;
        }
        return ENUM_REP + labelString + DELIMITER;		
	}

	public String getDefaultCValue() {
		return "0";
	}

	public int getCValueSizeIndicator() {
		return RELATIVE_ENUM_SIZE;
	}
}
