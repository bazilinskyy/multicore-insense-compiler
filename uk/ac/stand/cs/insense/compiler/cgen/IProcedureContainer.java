package uk.ac.stand.cs.insense.compiler.cgen;




/**
 * @author al
 * 
 * This class is used ro represent containers that themselves may hold procedures and constructors 
 *
 */
public interface IProcedureContainer extends IDeclarationContainer {
	public String getName();
	public void addExternalProcIncludes(String include);
	public void addProcessFrameDecl(String procFrameDecl);
	public void addProcessConstructorFunctionSignature(String signature);
}