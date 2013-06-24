/*
 * Created on 02-Jul-2006 at 21:43:11.
 */
package uk.ac.stand.cs.insense.compiler.types;
/**
 * DIAS Project
 * Insense
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2006
 * @author <a href="mailto:al@dcs.st-and.ac.uk"> Alan Dearle </a>
 */
public class ChannelType extends ConstructedType {

    private ITypeRep channel_type;
    private int direction;
    public final static int OUT = 0;
    public final static int IN = 1;


    public ChannelType(ITypeRep channel_type, int direction ) {
        super();
        this.channel_type = channel_type;
        this.direction = direction;
    }

    @Override
    public boolean equals(ITypeRep other) {
        return other instanceof ChannelType &&
               direction == ( (ChannelType)other ).direction && 
               channel_type.equals( ((ChannelType)other).channel_type );
    }
    
    /*
     * Can we join this a channel of this type to other
     */ 
    public boolean compatible(ITypeRep other) {
        return other instanceof ChannelType &&
               direction != ( (ChannelType)other ).direction && 
               channel_type.equals( ((ChannelType)other).channel_type );
    }

    public ITypeRep getChannel_type() {
        return channel_type;
    }
    
    public int getDirection() {
        return direction;
    }
    
	public boolean isPointerType() {
		return true; // Changed for Linux.
	}
    
    public String toHumanReadableString() {
        if( direction == IN ) {
            return "in " + channel_type.toHumanReadableString();
        } else {
            return "out " + channel_type.toHumanReadableString();
        }
    }
    
	public String toStringRep() {
        if( direction == IN ) {
            return CHANNEL_REP_IN + channel_type.toStringRep();
        } else {
            return CHANNEL_REP_OUT + channel_type.toStringRep();
        }
	}

	public String getDefaultCValue() {
		return "0";
	}

	@Override
	public int getCValueSizeIndicator() {
		return RELATIVE_INT_SIZE;
	}

}
