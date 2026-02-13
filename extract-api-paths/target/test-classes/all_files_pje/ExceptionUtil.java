package br.com.infox.ibpm.util;

public class ExceptionUtil {
	public static String getStackTrace(Exception e) {
		java.io.StringWriter sw = new java.io.StringWriter();
        e.printStackTrace(new java.io.PrintWriter(sw));
        String stackTraceInTexto = sw.toString();
        return stackTraceInTexto;
	}
}
