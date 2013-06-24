package uk.ac.stand.cs.insense.compiler.interfaces;

public interface ISymbol {

	public abstract boolean equals(String s);

	public abstract boolean equals(ISymbol s);

	public abstract String toString();

}