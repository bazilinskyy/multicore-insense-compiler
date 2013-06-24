package uk.ac.stand.cs.insense.compiler.types;
/**
 * DIAS Project
 * Insense
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2006
 * @author <a href="mailto:al@dcs.st-and.ac.uk"> Alan Dearle </a>
 */
public class UnknownType extends TypeRep {
	public static final String HUMAN_REP = "unknown";
	
	public static final UnknownType TYPE = new UnknownType();
	
	public UnknownType(){
	}
	
    public boolean equals(ITypeRep other) {
            return other instanceof UnknownType;
    }
    
	public boolean isPointerType() {
		return false;
	}

	public String toString() {
		return HUMAN_REP;
	}
	
	public String toHumanReadableString() {
		return HUMAN_REP;
	}

	public String toStringRep() {
		return null;
	}

	public String getDefaultCValue() {
		return "NULL";
	}

	public int getCValueSizeIndicator() {
		return RELATIVE_UNUSEDTYPE_SIZE;
	}
}
