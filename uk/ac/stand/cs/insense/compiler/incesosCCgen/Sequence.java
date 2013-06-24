package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.util.ArrayList;
import java.util.List;

import uk.ac.stand.cs.insense.compiler.cgen.ICode;
import uk.ac.stand.cs.insense.compiler.cgen.IDeclarationContainer;
import uk.ac.stand.cs.insense.compiler.cgen.IFunction;
import uk.ac.stand.cs.insense.compiler.cgen.IDecl;
import uk.ac.stand.cs.insense.compiler.cgen.ISequence;
import uk.ac.standrews.cs.nds.util.Diagnostic;

/**
 * @author al
 *
 */
public class Sequence extends DeclarationContainer implements ICode, ISequence {

	List<String> elements;
	List<IDecl> decls;
	private IDeclarationContainer parent;
	private List<Integer> decl_indices;
	
	public Sequence() {
		elements = new ArrayList<String>();
		decls = new ArrayList<IDecl>();
		this.parent = null;
		decl_indices = new ArrayList<Integer>();
	}
	
	public Sequence( IDeclarationContainer parent ) {
		this();
		this.parent = parent;
	}
	
	/**
	 * hoists code s to start of last sequence element
	 * @param s
	 */
	@Override
	public void addHoistedCode(String s) {
		elements.add( s );
	}

	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.ISequence#append(java.lang.String)
	 */
	public void append( String s ) {
		elements.add( s );
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.ISequence#pop_last()
	 */
	public String pop() {
		if( elements.size() > 0 ) {
			return elements.remove( elements.size() - 1 );
		} else {
			return "";
		}
	}
	
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.ISequence#complete()
	 */
	public void complete() {
		super.append(GENERATED_FROM + Diagnostic.getMethodInCallChain() + NEWLINE);
		// output all elements added to sequence
		int decl_index = 0;
		for( int element_index = 0; element_index < elements.size(); element_index++ ) {
			String s = elements.get(element_index);
			if(decl_indices.contains(element_index)){ // if element is a decl
				IDecl l = decls.get(decl_index++);    // get hold of decl
				if(l.getType().isPointerType()){      // if decl has pntr type
					// output e.g. "fooPNTR foo = NULL;"
					super.append(TAB + insenseTypeToCTypeName( l.getType() ) + l.getBaseName() + SPACE + EQUALS_ + l.getType().getDefaultCValue() + SEMI + NEWLINE);
				} else { // is scalar, e.g. int
					// output "int foo;" followed by code element below
					super.append(TAB + insenseTypeToCTypeName( l.getType() ) + l.getBaseName() + SEMI + NEWLINE);
				}
			}
			// output code element
			super.append(s + SEMI + NEWLINE);
		}
		
		// at end of sequence we must decRef any PNTR vars
		super.append(TAB + "// End of sequence" + NEWLINE);
		IDeclarationContainer container = Cgen.get_instance().findEnclosingDelcarationContainer();
		if(!(container instanceof CompilationUnit)){
			for(IDecl l : decls){
				if(l.getType().isPointerType()){
					super.append(TAB + functionCall("DAL_decRef", l.getBaseName()) + SEMI + NEWLINE);
				}
			}
		}
		// When a sequence completes local decls are garbage collected and 
		// are no longer in scope for subsequent code. For return statements in procedures 
		// these local decls should therefore not be garbage collected prior to the jump
		// statement that is used to jump to the end of the procedure. To prevent garbage collection
		// we must take them out fo the procedure's localDeclsInScope list.
		Function f = Cgen.get_instance().findEnclosingFunctionContainer();
		if(f != null){
			for(IDecl l : decls){
				f.getLocalDeclsInScope().remove(l);
			}
		}

		
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IDeclarationContainer#addLocation(uk.ac.stand.cs.insense.compiler.Ccgen.Location)
	 * Note this makes the declaration in the enclosing Container
	 */
	public void addLocation( IDecl l ) {
		decls.add( l );		// In Fight Club we do this for everything
		decl_indices.add(elements.size());
		//elements.set(elements.size()-1, insenseTypeToCTypeName(l.getType()) + elements.get(elements.size()-1));
	}

	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.DeclarationContaininer#addFunction(uk.ac.stand.cs.insense.compiler.cgen.IFunction)
	 * Should never be called in sequences - can't have functions in sequences
	 */
	public void addFunction(IFunction fb) {		
	}

	public List<String> getElements() {
		return elements;
	}

	public List<IDecl> getDecls() {
		return decls;
	}

	public IDeclarationContainer getParent() {
		return parent;
	}	
}

