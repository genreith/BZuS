package de.bzus.flame.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

public class Logging {
	private static Logging batchlog;
	private static Logging errorlog;
	private static Logging histlog;

	public static Logging getBatchLog() {
		return getBatchLog("Log", true);
	}

	public static Logging getBatchLog(String prefix, boolean enableLogging) {
		if (batchlog == null) {
			batchlog = new Logging(prefix, "csv", enableLogging);
		}
		batchlog.writeLog = enableLogging;
		return batchlog;
	}

	public static Logging getErrorLog() {
		return getErrorLog("Err", true);
	}

	public static Logging getErrorLog(String prefix, boolean enableLogging) {
		if (errorlog == null) {
			errorlog = new Logging(prefix, "log", enableLogging);
		}
		errorlog.writeLog = enableLogging;
		return errorlog;
	}

	public static Logging getHistoryLog(String prefix, boolean enableLogging) {
		if (histlog == null) {
			histlog = new Logging(prefix, "csv", enableLogging);
		}
		histlog.writeLog = enableLogging;
		return histlog;
	}

	public static Logging getHistoryLog() {
		return getHistoryLog("His", true);
	}

	private String header = "";

	private boolean headerWritten = false;

	private String logFileName = "no Logfile";
	private FileWriter logFileStream = null;
	private boolean writeLog = false;

	private Logging(boolean enableLogging) {
		writeLog = enableLogging;
		if (writeLog) {
			logFileName = String.format("Log-%1$tY%1$tm%1$td %1$tH%1$tM%1$tS.log", Calendar.getInstance());
			File lF = new File(Globals.logDir, logFileName);
			try {
				logFileStream = new FileWriter(lF, true);
			} catch (IOException e) {
				System.out.println("Unable to create " + lF.toString());
			}
		}
	}

	private Logging(String prefix, String suffix, boolean enableLogging) {
		writeLog = enableLogging;

		if (writeLog) {
			logFileName = String.format(prefix + "-%1$tY%1$tm%1$td %1$tH%1$tM%1$tS." + suffix, Calendar.getInstance());
			File lF = new File(Globals.logDir, logFileName);
			try {
				lF.getParentFile().mkdirs();
				logFileStream = new FileWriter(lF, true);
			} catch (Exception e) {
				System.out.println("Unable to create " + lF.toString() + " : " + e.toString());
			}
		}
	}

	public void closeLog() {
		if (logFileStream != null)
			try {
				logFileStream.close();
			} catch (IOException e) {
				System.out.println("Unable to close " + logFileStream.toString());
			}
		if (batchlog == this) {
			batchlog = null;
		} else if (histlog == this) {
			histlog = null;
		} else if (errorlog == this) {
			errorlog = null;
		}

	}

	public String getHeader() {
		return header;
	}

	public String getLogFileName() {
		if (writeLog && logFileStream != null)
			return logFileStream.toString();
		else
			return logFileName;
	}

	public boolean isLoggingEnabled() {
		return writeLog;
	}

	public void setHeader(String header) {
		setHeader(header, false);
	}

	public void setHeader(String header, boolean force) {
		if (force || !this.header.equals(header)) {
			this.header = header;
			headerWritten = false;
		}
	}

	public String showError(String msg, int severity) {
		StringBuffer sevText = new StringBuffer(
				String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS\t", Calendar.getInstance()));

		switch (severity) {
		case 0:
			sevText.append("(Information) ");
			break;

		case 1:
			sevText.append("(Warning) ");
			break;

		case 2:
			sevText.append("(Error) ");
			break;

		case 3:
			sevText.append("(Panic) ");
			break;

		default:
			sevText.append("(Debug[" + severity + "] (" + Thread.currentThread() + ") ");
			break;
		}
		sevText.append(msg);

		if (severity >= Globals.debugLevel) {
			writeLog(sevText.toString() + "\n");
		}
		return sevText.toString();
	}

	@Override
	public String toString() {
		return logFileName;
	}

	public synchronized void writeLog(String logEntry) {
		if (this != errorlog)
			getErrorLog().showError("Logging writeLog(" + logEntry + ")", -2);
		if (writeLog && logFileStream != null && logEntry != null)
			try {
				if (!headerWritten && header != null) {
					headerWritten = true;
					logFileStream.write(header);
				}
				logFileStream.write(logEntry);
				logFileStream.flush();
			} catch (IOException e) {
				if (this == errorlog)
					System.out.println("Unable to write " + logFileStream.toString());
				else
					getErrorLog().showError("Unable to write " + logFileStream.toString(), 2);
			}
	}
}
