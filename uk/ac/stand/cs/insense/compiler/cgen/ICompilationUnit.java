package uk.ac.stand.cs.insense.compiler.cgen;

public interface ICompilationUnit extends ICode {

	public abstract void addExternalInclude(String s);

	public abstract void addHoistedCode(String s);
	
	public abstract void addVTBLUsage(String s);

}