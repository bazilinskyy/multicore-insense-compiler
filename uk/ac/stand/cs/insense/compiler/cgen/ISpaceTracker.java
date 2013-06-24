package uk.ac.stand.cs.insense.compiler.cgen;

import uk.ac.stand.cs.insense.compiler.types.ITypeRep;

public interface ISpaceTracker {

	/**
	 *  @return the current stack usage in bytes of the current component
	 */
	public abstract int current_stack_usage();

	/**
	 *  @return the maximal stack usage in bytes of the current component
	 */
	public abstract int get_maximal_stack_usage();

	/**
	 *  @return the maximal stack usage in bytes of the current component
	 */
	public abstract void set_maximal_stack_usage(int usage);
	
	/**
	 *  Remove n bytes from the stack
	 *  @param n the number of words to remove from the stack
	 */
	public abstract void track_remove_stack_byte( int n );
	
	/**
	 *  Add n bytes to the stack
	 *  @param n the number of words to add to the stack
	 */
	public abstract void track_add_stack_byte( int n );

	/**
	 *  Add an element of type tr to the stack
	 */
	public abstract void track_add_stack_element(ITypeRep tr);

	/**
	 *  Remove an element of type tr from the stack
	 */
	public abstract void track_remove_stack_element(ITypeRep tr);

	
	/**
	 *  Track procedure calls - each of which are required to track their own maximal stack space usage
	 *  Remember where stack is up to and will reset size to that size whilst remembering max size
	 */
	public abstract void track_function_call_space( IFunction fn );

	/**
	 *  Track constructor calls - each of which are required to track their own maximal stack space usage
	 *  Remember where stack is up to and will reset size to that size whilst remembering max size
	 */
	public void track_constructor_call_space( IConstructor c );
	
	/**
	 *  Track system and runtime calls - each of which are required to track their own maximal stack space usage
	 *  Remember where stack is up to and will reset size to that size whilst remembering max size
	 */
	public abstract void track_call_space( int bytes );
}