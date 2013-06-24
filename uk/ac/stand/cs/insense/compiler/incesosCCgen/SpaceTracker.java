package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.util.Stack;

import uk.ac.stand.cs.insense.compiler.cgen.IConstructor;
import uk.ac.stand.cs.insense.compiler.cgen.IFunction;
import uk.ac.stand.cs.insense.compiler.cgen.ISpaceTracker;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;

/**
 * @author al
 * @date 27/5/2010
 * 
 * This class is used to keep track of the amount of stack space is used by components.
 * It is required to track the maximum stack space required to execute a component.
 *
 */
public class SpaceTracker implements ISpaceTracker {
	
	
	private int current_usage;
	private int maximal_usage;	
	
	SpaceTracker() {
		this.current_usage = 0;
		this.maximal_usage = 0;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.incesosCCgen.ISpaceTracker#current_stack_usage()
	 */
	public int current_stack_usage( ) {
		return current_usage;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.incesosCCgen.ISpaceTracker#get_maximal_stack_usage()
	 */
	public int get_maximal_stack_usage( ) {
		return maximal_usage;
	}
	
	public void set_maximal_stack_usage( int usage ) {
		maximal_usage = usage;
	}

	
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.incesosCCgen.ISpaceTracker#add_stack_element(uk.ac.stand.cs.insense.compiler.types.ITypeRep)
	 * Note all sizes are for MSP430
	 * TODO Tidy up MSP420 sizes if nothing better to do - make into a properties thing.
	 */
	public void track_add_stack_element( ITypeRep tr ) {
		track_add_stack_byte( MSP430Sizes.type_size( tr ) );
	}
	

	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.incesosCCgen.ISpaceTracker#add_stack_element(uk.ac.stand.cs.insense.compiler.types.ITypeRep)
	 * Note all sizes are for MSP430
	 * TODO Tidy up MSP420 sizes if nothing better to do - make into a properties thing.
	 */
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.incesosCCgen.ISpaceTracker#remove_stack_element(uk.ac.stand.cs.insense.compiler.types.ITypeRep)
	 */
	public void track_remove_stack_element( ITypeRep tr ) {
		track_remove_stack_byte( MSP430Sizes.type_size( tr ) );
	}	
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.incesosCCgen.ISpaceTracker#remove_stack_byte(int)
	 */
	public void track_remove_stack_byte( int n ) {
		current_usage -= n;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.incesosCCgen.ISpaceTracker#add_stack_byte(int)
	 */
	public void track_add_stack_byte( int n ) {
		current_usage += n;
		if( current_usage > maximal_usage ) {
			maximal_usage = current_usage;
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.incesosCCgen.ISpaceTracker#track_function_call_space(IDeclarationContainer)
	 */
	public void track_function_call_space( IFunction fn ) {
		int stack_bytes_used = 0;
		stack_bytes_used += MSP430Sizes.procedureCallOverhead(fn.getFt().getArgs());
		stack_bytes_used += fn.get_maximal_stack_usage() ;
		track_call_space( stack_bytes_used );
	}
	
	
	public void track_constructor_call_space( IConstructor c ) {
		int stack_bytes_used = 0;
		stack_bytes_used += MSP430Sizes.componentConstructorCallOverhead(c.getFt().getArgs());
		stack_bytes_used += c.get_maximal_stack_usage() ;
		track_call_space( stack_bytes_used );
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.incesosCCgen.ISpaceTracker#track_call_space(int)
	 */
	public void track_call_space( int bytes_required ) {
		if( current_usage + bytes_required > maximal_usage ) {
			maximal_usage = current_usage + bytes_required;
		}
	}

}
