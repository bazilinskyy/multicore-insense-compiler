package uk.ac.stand.cs.insense.compiler.cgen;

import uk.ac.stand.cs.insense.compiler.types.ITypeRep;

public interface ILocation extends ICode {

	/**
	 * Notifies the location that the code has been reordered to cope with complex rhs assignment
	 */
	public void performReordering( ICode context , ITypeRep rhsType , int fromContext);
	
	
	/**
	 * @return true if the code has been reordered
	 */
	public boolean reordered();
	
}
