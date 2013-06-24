package uk.ac.stand.cs.insense.compiler.cgen;

public interface IThrow extends ICode {

	public IFunction getProc();
	public abstract void complete();
	
}