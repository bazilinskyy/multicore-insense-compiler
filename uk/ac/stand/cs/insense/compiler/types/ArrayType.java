/*
 * Created on 02-Jul-2006 at 21:43:11.
 */
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
public class ArrayType extends ConstructedType {

    private ITypeRep array_type;

    public ArrayType( ITypeRep t ) {
        super();
        this.array_type = t;
    }

    @Override
    public boolean equals(ITypeRep other) {
        return other instanceof ArrayType &&
               array_type.equals( ((ArrayType)other).array_type );
    }

    public ITypeRep getArray_type() {
        return array_type;
    }
    
    public String toHumanReadableString() { return array_type.toHumanReadableString() + "IArray"; }
    
    public String toStringRep() { return ARRAY_REP + array_type.toStringRep(); }
    
	public boolean isPointerType() {
		return true;
	}
	
	public String toString(){
		return toHumanReadableString();
	}

	public int getDimensionality(){
		int num_dims = 1;
		ITypeRep temp = this;
		while(((ArrayType)temp).getArray_type() instanceof ArrayType){
			temp = ((ArrayType)temp).getArray_type();
			num_dims++;
		}
		return num_dims;
	}

	public ITypeRep getBaseType(){
		ITypeRep element_type = ((ArrayType)this).getArray_type();
		while( element_type instanceof ArrayType){
			element_type = ((ArrayType)element_type).getArray_type();
		}
		return element_type;
	}

	
	public String getDefaultCValue() {
		//return array_type.getDefaultCValue();
		return "NULL";
	}

	public int getCValueSizeIndicator() {
		return RELATIVE_POINTER_SIZE;
	}
	
}
