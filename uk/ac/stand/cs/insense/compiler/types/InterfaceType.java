/*
 * Created on 02-Jul-2006 at 21:43:11.
 */
package uk.ac.stand.cs.insense.compiler.types;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * DIAS Project
 * Insense
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2006
 * @author <a href="mailto:al@dcs.st-and.ac.uk"> Alan Dearle </a>
 */
public class InterfaceType extends ConstructedType implements Iterable<TypeName> {
    
    //private List<TypeName> channels = new LinkedList<TypeName>();
	// JL replaced above with below so that channels are stored in alphabetical order of channel name
    private ConcurrentSkipListSet<TypeName> channels = new ConcurrentSkipListSet<TypeName>();
    
    public InterfaceType() {
        super();
    }
    
    public void addChannel( String name, ChannelType channel ) {
        channels.add( new TypeName( name, channel ) );
    }
    
    public Iterator<TypeName> iterator() { return channels.iterator(); }
    
    public TypeRep getChannelType( String s ) {
        for( TypeName next : channels ) {
            if( next.name.equals( s ) ) {
               return next.type; 
            }
        }
        return UnknownType.TYPE;
    }
    
    @Override
    public boolean equals( ITypeRep other ) {
        return other instanceof InterfaceType &&
            equalChannels( (InterfaceType) other );
    }
    
    protected boolean equalChannels(InterfaceType other ) {
        if( channels.size() != other.getChannels().size() ) {
            return false;
        } else {
        	
            for( int i = 0; i < channels.size(); i++ ) {
                //if( ! channels.get(i).equals( other.getChannels().get(i) ) ) {
                if( ! ((TypeName) channels.toArray()[i]).equals( ((TypeName) other.getChannels().toArray()[i]) ) ) {
                    return false;
                }
            }
            
            return true;
        }
    }

    
    public String toString() {
    	return toHumanReadableString();
    }
    
    public String toHumanReadableString() {
        String listRep = "";
        ITypeRep lastType = null;
        for( TypeName t : this ) {
            if( listRep.equals( "" ) ) {
                listRep = listRep + t.type.toHumanReadableString() + " " + t.name;
                lastType = t.type;
            } else {
                if( lastType.equals( t.type ) ) {
                    listRep = listRep + ", " + t.name; 
                } else {
                    listRep = listRep + "; " + t.type.toHumanReadableString() + " " + t.name;
                }
            }
        }
        return "interface( " + listRep + " )";
    }
    
	public boolean isPointerType() {
		return true;
	}

	public String toStringRep() {
        String listRep = "";
        for( TypeName t : this ) {
                listRep = t.type.toStringRep() + t.name + DELIMITER;
        }
        return INTERFACE_REP + listRep + DELIMITER;
	}

	public String getDefaultCValue() {
		return "NULL";
	}

	public ConcurrentSkipListSet<TypeName> getChannels() {
		return channels;
	}

	public int getCValueSizeIndicator() {
		return RELATIVE_POINTER_SIZE;
	}

}
