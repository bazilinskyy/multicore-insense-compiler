package uk.ac.stand.cs.insense.compiler.cgen;

import uk.ac.stand.cs.insense.compiler.symbols.STEntry;

public interface IStop extends ICode {

	void addTarget(STEntry ste);

}