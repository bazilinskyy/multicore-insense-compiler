package uk.ac.stand.cs.insense.compiler.cgen;


import uk.ac.stand.cs.insense.compiler.types.ITypeRep;

public interface IReturn extends ICode {

	public IFunction getProc();
	public boolean proc_returns_result();
	public ITypeRep getProcReturnType();
	public abstract void complete();
	
}