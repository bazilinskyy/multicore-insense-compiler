package uk.ac.stand.cs.insense.compiler.cgen;

import uk.ac.stand.cs.insense.compiler.types.ChannelType;

public interface IConnect extends ICode {

	public void on();

	public void to();

	public void setChannelType(ChannelType ct);

}