package uk.ac.stand.cs.insense.compiler.types;
/**
 * DIAS Project
 * Insense
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2007
 * @author <a href="mailto:al@dcs.st-and.ac.uk"> Alan Dearle </a>
 */
public class ByteType extends ScalarType {
	public static final String HUMAN_REP = "byte";
	
	public static final ByteType TYPE = new ByteType();
	
	public ByteType(){
	}
	
    public boolean equals(ITypeRep other) {
    	return other instanceof ByteType;
    }
    
	public boolean isPointerType() {
		return false;
	}

	public String toHumanReadableString() {
		return HUMAN_REP;
	}

	public String toStringRep() {
		return BYTE_REP;
	}

	public String toString() {
		return BYTE_REP;		
	}
	
	public String getDefaultCValue() {
		return "0";
	}

	public int getCValueSizeIndicator() {
		return RELATIVE_BYTE_SIZE;
	}
	
	
}
