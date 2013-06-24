package uk.ac.stand.cs.insense.compiler.types;
/**
 * DIAS Project
 * Insense
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2010
 * @author <a href="mailto:jonl@cs.st-andrews.ac.uk"> Jon Lewis </a>
 * @author <a href="mailto:al@cs.st-andrews.ac.uk"> Alan Dearle </a>
 */
public class UnsignedIntegerType extends ScalarType {
	public static final String HUMAN_REP = "unsigned integer";
	
	public static final UnsignedIntegerType TYPE = new UnsignedIntegerType();
	
	public UnsignedIntegerType(){
	}
	
    public boolean equals(ITypeRep other) {
            return other instanceof UnsignedIntegerType;
    }
    
	public boolean isPointerType() {
		return false;
	}

	public String toHumanReadableString() {
		return HUMAN_REP;
	}

	public String toStringRep() {
		return UNSIGNED_REP;
	}

	public String getDefaultCValue() {
		return "0";
	}

	public int getCValueSizeIndicator() {
		return RELATIVE_INT_SIZE;
	}
}
