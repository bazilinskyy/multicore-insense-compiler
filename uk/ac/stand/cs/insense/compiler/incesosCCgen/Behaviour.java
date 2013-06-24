package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import uk.ac.stand.cs.insense.compiler.cgen.IBehaviour;
import uk.ac.stand.cs.insense.compiler.cgen.ICode;
import uk.ac.stand.cs.insense.compiler.cgen.IDeclarationContainer;
import uk.ac.stand.cs.insense.compiler.cgen.IFunction;
import uk.ac.stand.cs.insense.compiler.cgen.IDecl;
import uk.ac.standrews.cs.nds.util.ErrorHandling;

public class Behaviour extends DeclarationContainer implements ICode, IBehaviour {
	
	private IDeclarationContainer parent;
	
	public Behaviour( IDeclarationContainer parent ) {
		if( parent == null ) {
			ErrorHandling.error( "Parent is null" ); 
		} else {
			this.parent = parent;
		}
		// TODO JL Space Tracking
		parent.track_add_stack_byte(MSP430Sizes.behaviourFunctionCallOverhead());
	}
	
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IDeclarationContainer#addLocation(uk.ac.stand.cs.insense.compiler.Ccgen.Location)
	 * Note this makes the declaration in the enclosing Container
	 */
	public void addLocation( IDecl l ) {
		if( parent != null ) {
			parent.addLocation( l );
		}
	}
	
	/* 
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.DeclarationContaininer#addFunction(uk.ac.stand.cs.insense.compiler.cgen.IFunction)
	 * Method has no body to prevent functions being added for behaviours.
	 * Behaviours are treated specially and don't require this code
	 * This function should never be called for a Behaviour and this function is here to make sure it doesn't do anything if it is.
	 * I considered deleting it but decided to keep it
	 */
	public void addFunction(IFunction fb) {		
	}	
}
