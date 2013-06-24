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
public class VoidType extends ScalarType {
	public static final String HUMAN_REP = "void";
	
	public static final VoidType TYPE = new VoidType();
	
	public VoidType(){
	}
	
    public boolean equals(ITypeRep other) {
            return other instanceof VoidType;
    }
    
	public boolean isPointerType() {
		return false;
	}

	public String toHumanReadableString() {
		return HUMAN_REP;
	}

	public String toStringRep() {
		return VOID_REP;
	}

	public String getDefaultCValue() {
		return "";
	}

	public int getCValueSizeIndicator() {
		return RELATIVE_UNUSEDTYPE_SIZE;
	}
}
