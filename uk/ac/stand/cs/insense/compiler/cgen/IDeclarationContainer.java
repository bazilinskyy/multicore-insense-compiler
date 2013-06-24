package uk.ac.stand.cs.insense.compiler.cgen;

import java.util.ArrayList;
import java.util.HashMap;

public interface IDeclarationContainer extends ICode {

	public abstract void addFunction(IFunction fb);

	public abstract void addHoistedCode(String s);
	
	public void addLocation( IDecl l );
	
	public ArrayList<IDecl> getLocations();

}