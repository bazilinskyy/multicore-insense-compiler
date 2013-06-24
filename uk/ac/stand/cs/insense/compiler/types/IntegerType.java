package uk.ac.stand.cs.insense.compiler.types;
/**
 * DIAS Project
 * Insense
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2006
 * @author <a href="mailto:ajm@dcs.st-and.ac.uk"> Andrew J. McCarthy </a>
 * @author <a href="mailto:al@dcs.st-and.ac.uk"> Alan Dearle </a>
 */
public class IntegerType extends ScalarType {
	public static final String HUMAN_REP = "integer";
	
	public static final IntegerType TYPE = new IntegerType();
	
	public IntegerType(){
	}
	
    public boolean equals(ITypeRep other) {
            return other instanceof IntegerType;
    }
    
	public boolean isPointerType() {
		return false;
	}

	public String toHumanReadableString() {
		return HUMAN_REP;
	}

	public String toStringRep() {
		return INTEGER_REP;
	}

	public String getDefaultCValue() {
		return "0";
	}

	public int getCValueSizeIndicator() {
		return RELATIVE_INT_SIZE;
	}
}
