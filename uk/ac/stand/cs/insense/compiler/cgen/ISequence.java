package uk.ac.stand.cs.insense.compiler.cgen;

import java.util.List;

public interface ISequence extends ICode {

	/**
	 * Pops and @return the last constuct in the sequence
	 */
	public abstract String pop();
	
	/**
	 * hoists code s to start of last sequence element
	 * @param s
	 */
	public abstract void addHoistedCode(String s);
	
	
	public List<String> getElements();
	public List<IDecl> getDecls();	
	public IDeclarationContainer getParent();


}