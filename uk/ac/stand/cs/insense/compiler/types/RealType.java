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
public class RealType extends ScalarType {
	public static final String HUMAN_REP = "real";
	
	public static final RealType TYPE = new RealType();
	
	public RealType(){
	}
	
    public boolean equals(ITypeRep other) {
            return other instanceof RealType;
    }
    
	public boolean isPointerType() {
		return false;
	}

	public String toHumanReadableString() {
		return HUMAN_REP;
	}

	public String toStringRep() {
		return REAL_REP;
	}

	public String getDefaultCValue() {
		return "0.0";
	}

	public int getCValueSizeIndicator() {
		return RELATIVE_REAL_SIZE;
	}
}
