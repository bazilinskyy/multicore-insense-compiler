package uk.ac.stand.cs.insense.compiler.cgen;

import uk.ac.stand.cs.insense.compiler.types.ChannelType;

public interface IPublish extends ICode {

	public abstract void as(ChannelType tr);

}