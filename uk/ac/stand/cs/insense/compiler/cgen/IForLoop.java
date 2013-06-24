package uk.ac.stand.cs.insense.compiler.cgen;

import uk.ac.stand.cs.insense.compiler.symbols.STEntry;

public interface IForLoop extends ICode {

	public abstract void finalValue();

	public abstract void increment();

	public abstract void body();

	public abstract void negativeIncrement();

	public abstract void addDecl(STEntry lookup);

}