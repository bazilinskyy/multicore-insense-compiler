package uk.ac.stand.cs.insense.compiler.symbols;

import uk.ac.stand.cs.insense.compiler.incesosCCgen.UnionDeclaration;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;

public class AnyProjectSTEntry extends STEntry {

	public AnyProjectSTEntry(String name, ITypeRep type, Boolean isType, int scope_level, int context, int diasambiguator ) {
		super( name, type, isType, scope_level, context, diasambiguator );
	}

	/* 
	 * Adds the extra Union projection postfix onto the names in the case when any project is used.
	 * @see uk.ac.stand.cs.insense.compiler.symbols.STEntry#toString()
	 */
	public String contextualName(int fromContext) {
		return super.contextualName(fromContext) + "." + UnionDeclaration.unionLabelName( getType() );
	}
}
