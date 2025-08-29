package br.com.infox.ibpm.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipFluxoUtil {

	private static final String FLUXO_XML = "fluxo.xml";
	private static final String ENTIDADES_XML = "entidades.xml";

	public static byte[] zipXml(String xmlFluxo, String xmlIds) throws IOException {
		ByteArrayOutputStream bas = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(bas);
		ZipEntry z = new ZipEntry(FLUXO_XML);
		zos.putNextEntry(z);
		zos.write(xmlFluxo.getBytes());
		zos.closeEntry();
		z = new ZipEntry(ENTIDADES_XML);
		zos.putNextEntry(z);
		zos.write(xmlIds.getBytes());
		zos.closeEntry();

		zos.close();

		return bas.toByteArray();
	}

	public static String[] unzipXml(InputStream is) throws IOException {
		String[] unzipedFiles = null;
		ZipInputStream zis = new ZipInputStream(is);
		int i = 0;
		ZipEntry entry = null;
		while ((entry = zis.getNextEntry()) != null) {
			if (entry.getName().equals(ENTIDADES_XML) || entry.getName().equals(FLUXO_XML)) {
				if (unzipedFiles == null) {
					unzipedFiles = new String[2];
				}
				BufferedReader br = new BufferedReader(new InputStreamReader(zis));
				StringBuffer sb = new StringBuffer();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				unzipedFiles[i] = sb.toString();
				i++;
			} else {
				return null;
			}
		}
		zis.close();
		return unzipedFiles;
	}

}
