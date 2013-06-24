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
 * This class wraps up a C type so that we can declare C types within the code generator
 */
public class CType extends ScalarType {
	public String rep = "";
	
	public static final CType TYPE = new CType( "" );
	
	public CType( String the_C_type ) {
		rep = the_C_type;
	}

    @Override
    public boolean equals(ITypeRep other) {
        return other instanceof CType &&
        other.toStringRep().equals(this.toStringRep());
    }

	public boolean isPointerType() {
		return false;
	}

	public String toHumanReadableString() {
		return rep;
	}

	public String toStringRep() {
		return rep;
	}

	public String getDefaultCValue() {
		return "0";
	}

	public int getCValueSizeIndicator() {
		return RELATIVE_UNUSEDTYPE_SIZE;
	}
}
