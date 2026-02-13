package br.com.itx.util;

public class DatabaseUtilRpv extends DatabaseUtil {

	private static final String fileNameProperties = "/database_rpv.properties";

	@Override
	protected String getFileNameProperties() {
		return fileNameProperties;
	}

}
