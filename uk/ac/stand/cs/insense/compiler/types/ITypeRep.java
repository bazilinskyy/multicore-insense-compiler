package uk.ac.stand.cs.insense.compiler.types;

/**
 * @author al
 */
public interface ITypeRep {

	/**
	 * @param other - the type to comparte to this type
	 * @return true if the two types are equal
	 */
	public abstract boolean equals(ITypeRep other);

	/**
	 * @return a string representation presentable to users - i.e. in compile time errors
	 */
	public abstract String toHumanReadableString();
	
	/**
	 * @return a string representation of this type usable at runtime
	 * this rep is a compressed representation of the string
	 */
	public abstract String toStringRep();

	
	/**
	 * @return a string value representing the default initialiser in C for this type
	 */
	public abstract String getDefaultCValue();
	
	/**
	 * @return an integer value representing the size of this type in the C implementation relative to other types
	 */
	public abstract int getCValueSizeIndicator();

	/**
	 * @return true of the type is a pointer
	 */
	public abstract boolean isPointerType();
	
	/**
	 * @return true of the type contains a pointer
	 */
//	public abstract boolean containsPointers();

}