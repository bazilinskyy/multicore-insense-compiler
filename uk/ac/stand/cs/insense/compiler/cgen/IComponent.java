package uk.ac.stand.cs.insense.compiler.cgen;

import uk.ac.stand.cs.insense.compiler.types.ChannelType;
import uk.ac.stand.cs.insense.compiler.types.InterfaceType;

public interface IComponent extends IProcedureContainer {

	public abstract void addChannel(String name, ChannelType tr);

	public abstract void addInterface(String name, InterfaceType type);
	
	public abstract void addExternalIncludes(String s);

	public abstract void addConstructor(IConstructor code);

	public abstract void addBehaviour(ICode code);

	public abstract String getHeaderFileName();
}