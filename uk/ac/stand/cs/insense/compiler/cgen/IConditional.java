package uk.ac.stand.cs.insense.compiler.cgen;

import uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable;

public interface IConditional extends ICode {

	public abstract void thenBranch(ISymbolTable then_table);

	public abstract void elseBranch(ISymbolTable else_table);
}