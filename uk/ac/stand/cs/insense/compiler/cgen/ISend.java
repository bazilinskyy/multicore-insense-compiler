package uk.ac.stand.cs.insense.compiler.cgen;

import uk.ac.stand.cs.insense.compiler.symbols.STEntry;

public interface ISend extends ICode {

	void addChannel(STEntry ste);

}