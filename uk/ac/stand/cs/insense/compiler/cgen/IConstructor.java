package uk.ac.stand.cs.insense.compiler.cgen;

import uk.ac.stand.cs.insense.compiler.types.FunctionType;

/**
 * @author al
 *         This interface is just used as a label
 */
public interface IConstructor extends IDeclarationContainer {
	public int getDisambiguator();

	public void setDisambiguator(int disambiguator);

	@Override
	public void append(String s);

	public String generateCode(IProcedureContainer container);

	public String constructorFunctionSignature();

	public FunctionType getFt();

	public String arrayFunctionSignature();
}
