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
public class AnyType extends ScalarType {
	public static final String HUMAN_REP = "any";
	
	public static final AnyType TYPE = new AnyType();
	
	public AnyType() {
	}

    @Override
    public boolean equals(ITypeRep other) {
        return other instanceof AnyType;
    }

	public boolean isPointerType() {
		return true;
	}

	public String toHumanReadableString() {
		return HUMAN_REP;
	}

	public String toStringRep() {
		return ANY_REP;
	}

	public String getDefaultCValue() {
		return "NULL";
	}

	public int getCValueSizeIndicator() {
		return RELATIVE_POINTER_SIZE;
	}
}
