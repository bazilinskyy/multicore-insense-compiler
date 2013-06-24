package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import uk.ac.stand.cs.insense.compiler.cgen.IMakefile;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.fileHandling.FileTracker;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.fileHandling.OutputFile;
import uk.ac.standrews.cs.nds.util.ErrorHandling;

/**
 * @author al
 *         Generates a Makefile suitable for inclusion with the Unix-based systems
 * 
 */
public class MakefileInceOS implements IMakefile {

	private static String name = "Makefile";
	// private static String CONTIKI = "CONTIKI = ../..";
	private static String defs = "ifndef TARGET\nTARGET=native\nendif";
	private static String INSENSE_RUNTIME_INCEOS_CHECK = "ifndef INSENSE_RUNTIME_INCEOS\n     $(error INSENSE_RUNTIME_INCEOS not defined! You must specify where INSENSE_RUNTIME_INCEOS resides!)\nendif";
	// private static String MSP430_GCC_VER_CHECK =
	// "ifndef MSP430_GCC_VER\n     $(error MSP430_GCC_VER not defined! You must specify MSP430_GCC_VER as e.g. 323 for gcc 3.2.3 or 453 for 4.5.3!)\nendif";

	private static String NO_STRING = "no";
	private static String YES_STRING = "yes";

	private static String DAL_RADIO_TYPE = "no";

	private static String DAL_LEDS = NO_STRING;
	private static String DAL_BUTTON = NO_STRING;
	private static String DAL_SENSORS = NO_STRING;
	private static String DAL_STDOUT = NO_STRING;
	private static String DAL_RADIO = NO_STRING;
	private static String DAL_SCHEDULER = NO_STRING;
	private static String DAL_RECEIVE = NO_STRING;
	private static String XMAC_POWERSAVE = NO_STRING;
	private static String DAL_INTERNODECHANNEL = NO_STRING;
	private static String DAL_INCH_CONN_CHANGE = NO_STRING;
	private static String DAL_INCH_PUBLIC_CHAN_QUERY = NO_STRING;

	private static String DAL_DEBUG_CHECK = "ifeq ($(DAL_DEBUG), yes)\nDALDEBUG = -DDALDEBUG\nendif";
	private static String DAL_SMALL_CHECK = "ifneq ($(DAL_SMALL), no)\nDALSMALL = -DDALSMALL\nendif";
	private static String DAL_LEDS_CHECK = "ifeq ($(DAL_LEDS), yes)\nDALLEDS = -DDALLEDS\nendif";
	private static String DAL_BUTTON_CHECK = "ifeq ($(DAL_BUTTON), yes)\nDALBUTTON = -DDALBUTTON\nendif";
	private static String DAL_SENSORS_CHECK = "ifeq ($(DAL_SENSORS), yes)\nDALSENSORS = -DDALSENSORS\nendif";
	private static String DAL_STDOUT_CHECK = "ifeq ($(DAL_STDOUT), yes)\nDALSTDOUT = -DDALSTDOUT\nendif";
	private static String DAL_INTERNODECHANNEL_CHECK = "ifeq ($(DAL_INTERNODECHANNEL), yes)\nDALINTERNODECHANNEL = -DDALINTERNODECHANNEL\nDAL_SCHEDULER = yes\nendif";
	private static String DAL_RADIO_CHECK = "ifeq ($(DAL_RADIO), yes)\nDALRADIO = -DDALRADIO\nendif";
	private static String DAL_SCHEDULER_CHECK = "ifeq ($(DAL_SCHEDULER), yes)\nDALSCHEDULER = -DDALSCHEDULER\nendif";
	private static String DAL_RECEIVE_CHECK = "ifeq ($(DAL_RECEIVE), yes)\nDALRECEIVE = -DDALRECEIVE"
			+ /* "\nelse\nXMAC_POWERSAVE = " + YES_STRING + */"\nendif";
	private static String XMAC_POWERSAVE_CHECK = "ifeq ($(XMAC_POWERSAVE), yes)\nXMACPOWERSAVE = -DXMAC_CONF_OFF_TIME=15.0*RTIMER_ARCH_SECOND-DEFAULT_ON_TIME\nendif";
	// private static String XMAC_LISTEN_CHECK =
	// "ifeq ($(DAL_RADIO), yes) \nXMACPOWERSAVE = -DXMAC_CONF_OFF_TIME=\\(rtimer_clock_t\\)0.02*RTIMER_ARCH_SECOND-DEFAULT_ON_TIME\nendif"
	// + "\nifeq ($(DAL_INTERNODECHANNEL), yes) \nXMACPOWERSAVE = -DXMAC_CONF_OFF_TIME=\\(rtimer_clock_t\\)0.02*RTIMER_ARCH_SECOND-DEFAULT_ON_TIME\nendif";
	private static String DAL_RADIO_POWERCYCLE_CHECK = "ifdef DAL_RADIO_POWERCYCLE\nXMACPOWERSAVE = -DXMAC_CONF_OFF_TIME=\\(rtimer_clock_t\\)$(DAL_RADIO_POWERCYCLE)*RTIMER_ARCH_SECOND-DEFAULT_ON_TIME\nendif";
	// private static String DAL_OS_DIR = "DAL_OS_DIR = DAL_CONTIKI";
	// JL removed OS_DIR as DAL is now flat

	private static String TARGETFLAGS = "TARGETFLAGS = $(CFLAGS) -DAUTOSTART_ENABLE $(DALDEBUG) $(DALSMALL) $(DALSENSORS) $(DALLEDS) $(DALBUTTON) $(DALSTDOUT) $(DALINTERNODECHANNEL) $(DALRADIO) $(DALSCHEDULER) $(DALRECEIVE)";
	// private static String CFLAGS_ADDITION = "CFLAGS += $(XMACPOWERSAVE) -DMSP430_GCC_VER=$(MSP430_GCC_VER)";
	private static String CFLAGS_ADDITION = "CFLAGS += $(XMACPOWERSAVE) ";
	private final String EXECUTABLE;
	private final String projectName;

	// private static String APP_INCLUDES = "APP_INCLUDES = -I $(INSENSE_RUNTIME_INCEOS) -I $(INSENSE_RUNTIME_INCEOS)/$(DAL_OS_DIR)";
	// JL altered above to following
	private static String APP_INCLUDES = "APP_INCLUDES = -I $(INSENSE_RUNTIME_INCEOS)";

	// APP_SRCFILES generated in generateMakeFile below

	private static String APP_OBJECTFILES = "APP_OBJECTFILES = $(APP_SRCFILES:.c=.o)";

	// private static String INCLUDE = "include $(CONTIKI)/Makefile.include\ninclude $(INSENSE_RUNTIME_INCEOS)/Makefile.include";
	private static String INCLUDE = "include $(INSENSE_RUNTIME_INCEOS)/Makefile.include";
	private static String ALL = "all: iclean $(APP_OBJECTFILES) insense_runtime_inceos.a $(PROJECT_LIBRARIES)\n\t$(CC) $(LDFLAGS) $(APP_OBJECTFILES) insense_runtime_inceos.a -o ";

	// JL added a separate clean targets
	private static String ICLEANTARGET = "iclean:\n\t	rm -f $(APP_OBJECTFILES) $(INSENSE_OBJECTFILES) insense_runtime_inceos.a *~ $(INSENSE_RUNTIME_INCEOS)/*~ obj_*/xmac.o";
	private static String SCLEANTARGET = "sclean: iclean\n\t	rm -f *.[ch] Makefile *.sky *.stackdump *.native *.map *.a";

	MakefileInceOS(String projectName) {
		this.projectName = projectName;
		EXECUTABLE = "EXECUTABLE=" + projectName;
	}

	private void printCompilerControlledRuntimeComponentFlags(PrintStream ps) {
		ps.println("# unless the following were overridden on the command-line\n# use the following compiler generated component flags");
		ps.println("ifndef DAL_LEDS\n DAL_LEDS = " + DAL_LEDS + "\nendif");
		ps.println("ifndef DAL_BUTTON\n DAL_BUTTON = " + DAL_BUTTON + "\nendif");
		ps.println("ifndef DAL_SENSORS\n DAL_SENSORS = " + DAL_SENSORS + "\nendif");
		ps.println("ifndef DAL_STDOUT\n DAL_STDOUT = " + DAL_STDOUT + "\nendif");
		ps.println("ifndef DAL_INTERNODECHANNEL\n DAL_INTERNODECHANNEL = " + DAL_INTERNODECHANNEL + "\nendif");
		ps.println("ifndef DAL_RADIO\n DAL_RADIO = " + DAL_RADIO + "\nendif");
		ps.println("ifndef DAL_SCHEDULER\n DAL_SCHEDULER = " + DAL_SCHEDULER + "\nendif");
		ps.println("ifndef DAL_RECEIVE\n DAL_RECEIVE = " + DAL_RECEIVE + "\nendif");
		ps.println("ifndef XMAC_POWERSAVE\n XMAC_POWERSAVE = " + XMAC_POWERSAVE + "\nendif");
		ps.println();
		ps.println("# set conditional compilation flags depending on compiler and user choice");
		ps.println(DAL_DEBUG_CHECK);
		ps.println(DAL_SMALL_CHECK);
		ps.println(DAL_SENSORS_CHECK);
		ps.println(DAL_STDOUT_CHECK);
		ps.println(DAL_INTERNODECHANNEL_CHECK);
		ps.println(DAL_RADIO_CHECK);
		ps.println(DAL_LEDS_CHECK);
		ps.println(DAL_BUTTON_CHECK);
		ps.println(DAL_SCHEDULER_CHECK);
		ps.println(DAL_RECEIVE_CHECK);
		ps.println(XMAC_POWERSAVE_CHECK);
		// ps.println( XMAC_LISTEN_CHECK );
		ps.println(DAL_RADIO_POWERCYCLE_CHECK);

		ps.println(CFLAGS_ADDITION);
	}

	@Override
	public void generateMakeFile() {
		try {
			OutputFile f = new OutputFile(name);
			PrintStream ps = f.getStream();
			// PrintStream ps = System.out; // for debugging
			FileTracker ft = FileTracker.instance();
			List<String> headerFiles = ft.getHeaderFilenames();
			List<String> implFiles = ft.getImplFilenames();

			// ps.println(CONTIKI);
			ps.println(defs);
			ps.println();
			ps.println(INSENSE_RUNTIME_INCEOS_CHECK);
			// ps.println(MSP430_GCC_VER_CHECK);
			printCompilerControlledRuntimeComponentFlags(ps);
			ps.println();
			// ps.println( DAL_OS_DIR );
			// JL commented out above, DAL now flat
			ps.println();
			ps.println(TARGETFLAGS);
			ps.println();
			ps.println("PROJ=" + projectName + Code.NEWLINE);
			ps.println(APP_INCLUDES);
			ps.print("APP_SRCFILES = ");
			for (String s : implFiles) {	// yields the list of source files
				ps.print(s + Code.SPACE);
			}
			ps.println();
			ps.println(APP_OBJECTFILES);
			ps.println();
			ps.print(ALL);
			ps.print("$(PROJ).$(TARGET)");
			ps.println(" -pthread"); // TODO add this line only if target OS is linux. Mac does not require it.
			ps.println();
			ps.println(INCLUDE);
			ps.println(ICLEANTARGET);
			ps.println(SCLEANTARGET);
			f.close();
		} catch (IOException e) {
			ErrorHandling.exceptionError(e, "Opening file: " + name);
		}
	}

	private String basename(String s) {
		int last = s.lastIndexOf(".");
		return s.substring(0, last);
	}

	public static void setDAL_LEDS(boolean dal_leds) {
		if (dal_leds)
			DAL_LEDS = YES_STRING;
	}

	public static void setDAL_BUTTON(boolean dal_button) {
		if (dal_button)
			DAL_BUTTON = YES_STRING;
	}

	public static void setDAL_SENSORS(boolean dal_sensors) {
		if (dal_sensors)
			DAL_SENSORS = YES_STRING;
	}

	public static void setDAL_STDOUT(boolean dal_stdout) {
		if (dal_stdout)
			DAL_STDOUT = YES_STRING;
	}

	public static void setDAL_RADIO(boolean dal_radio) {
		if (dal_radio)
			DAL_RADIO = YES_STRING;
	}

	public static void setDAL_SCHEDULER(boolean dal_scheduler) {
		if (dal_scheduler)
			DAL_SCHEDULER = YES_STRING;
	}

	public static void setDAL_RECEIVE(boolean dal_receive) {
		if (dal_receive)
			DAL_RECEIVE = YES_STRING;
	}

	public static void setDAL_INTERNODECHANNEL(boolean dal_internodechannel) {
		if (dal_internodechannel)
			DAL_INTERNODECHANNEL = YES_STRING;
	}
}
