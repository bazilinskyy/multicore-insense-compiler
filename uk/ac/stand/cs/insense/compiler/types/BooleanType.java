package uk.ac.stand.cs.insense.compiler.types;
/**
 * DIAS Project
 * Insense
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2008
 * @author <a href="mailto:ajm@dcs.st-and.ac.uk"> Jon Lewis </a>
 * @author <a href="mailto:al@dcs.st-and.ac.uk"> Alan Dearle </a>
 * 
 */
public class BooleanType extends ScalarType {
	public static final String HUMAN_REP = "bool";
	
	public static final BooleanType TYPE = new BooleanType();
	
	public BooleanType() {
	}

    @Override
    public boolean equals(ITypeRep other) {
        return other instanceof BooleanType;
    }

	public boolean isPointerType() {
		return false;
	}

	public String toHumanReadableString() {
		return HUMAN_REP;
	}

	public String toStringRep() {
		return BOOLEAN_REP;
	}

	public String getDefaultCValue() {
		return "false";
	}

	public int getCValueSizeIndicator() {
		return RELATIVE_BOOL_SIZE;
	}
}
