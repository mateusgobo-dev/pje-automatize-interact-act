/*
 IBPM - Ferramenta de produtividade Java
 Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda.

 Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo 
 sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela 
 Free Software Foundation; versão 2 da Licença.
 Este programa é distribuído na expectativa de que seja útil, porém, SEM 
 NENHUMA GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU 
 ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA.
 
 Consulte a GNU GPL para mais detalhes.
 Você deve ter recebido uma cópia da GNU GPL junto com este programa; se não, 
 veja em http://www.gnu.org/licenses/   
 */
package br.com.itx.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import br.com.itx.component.FileHome;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.servicos.MimeUtilChecker;

public final class FileUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

	private FileUtil() {
	}

	public static boolean deleteDir(File dir) {
		if (dir.exists()) {
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDir(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (dir.delete());
	}

	/**
	 * Copia um diretorio em outro, recursivamente.
	 * 
	 * @param fromDir
	 *            origem. Se não for diretório é lançada
	 *            IllegalArgumentException
	 * @param toDir
	 *            destino
	 * 
	 */
	public static void copyDir(File fromDir, File toDir) {
		if (!fromDir.isDirectory()) {
			throw new IllegalArgumentException(fromDir + " is not a directory.");
		}
		toDir.mkdirs();
		for (File f : fromDir.listFiles()) {
			File to = new File(toDir, f.getName());
			if (f.isDirectory()) {
				copyDir(f, to);
			}
			copy(f, to);
		}
	}

	public static void copyFile(File fromFile, File toFile) {
		if (!fromFile.equals(toFile)) {
			toFile.delete();
			toFile.getParentFile().mkdirs();
		}
		if (fromFile.isFile() && !toFile.exists()) {
			copy(fromFile, toFile);
		}
	}

	@SuppressWarnings("resource")
	private static void copy(File fromFile, File toFile) {
		try {
			FileChannel fromChannel = new FileInputStream(fromFile).getChannel();
			FileChannel toChannel = new FileOutputStream(toFile).getChannel();
			toChannel.transferFrom(fromChannel, 0, fromChannel.size());
			fromChannel.close();
			toChannel.close();
		} catch (IOException err) {
			err.printStackTrace();
		}
	}

	public static void writeFile(File file, InputStream in) {
		file.delete();
		file.getParentFile().mkdirs();
		if (!file.exists()) {
			OutputStream out = null;
			try {
				byte[] trecho = new byte[10240];
				int quant = 0;
				out = new FileOutputStream(file);
				while ((quant = in.read(trecho)) > -1) {
					out.write(trecho, 0, quant);
					out.flush();
				}

			} catch (IOException err) {
				err.printStackTrace();
			} finally {
				close(out);
				close(in);
			}
		}
	}

	/**
	 * Escreve o stream em um arquivo temporário.
	 * 
	 * @param stream Stream
	 * @return Arquivo temporário.
	 * @throws IOException
	 */
	public static File writeTempFile(InputStream stream) throws IOException {
		File resultado = null;
		FileOutputStream fos = null;
		try {
			resultado = File.createTempFile("tmp", ".tmp");

			byte[] bytes = IOUtils.toByteArray(stream);

			fos = new FileOutputStream(resultado);
			fos.write(bytes);
			fos.close();
		} finally {
			if (fos != null) {
				fos.flush();
				fos.close();
			}
		}

		return resultado;																						
	}
	
	public static void saveInputStreamToFile(InputStream inputStream, String targetFileName) {
		writeFile(new File(targetFileName), inputStream);
	}
	
	public static void writeText(File file, boolean append, String text) {
		if (file != null && text != null) {
			FileWriter out = null;
			try {
				if (file.getParentFile() != null) {
					file.getParentFile().mkdirs();
				}
				out = new FileWriter(file, append);
				out.write(text);

			} catch (IOException e) {
				e.printStackTrace(System.out);
			} finally {
				close(out);
			}
		}
	}
	
	public static void readFile(File file, OutputStream out) {
		if (file.isFile()) {
			InputStream in = null;
			try {
				byte[] trecho = new byte[10240];
				int quant = 0;
				in = new FileInputStream(file);
				while ((quant = in.read(trecho)) > -1) {
					out.write(trecho, 0, quant);
					out.flush();
				}
			} catch (IOException err) {
				err.printStackTrace();
			} finally {
				close(out);
				close(in);
			}
		}
	}
	
	public static InputStream readFile(String fileName) {
		try {
			return new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	public static void close(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
    public static String readText(String diretorio, String nomeArquivo) throws IOException {
    	String conteudo = null;
		File file = FileUtil.getFileFromDir(diretorio, nomeArquivo);
		if(file != null && file.isFile()) {
			InputStream is = FileUtil.readFile(file.getPath());
			if(is != null) {
				conteudo = CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
			}
		}
		return conteudo;
    }

	public static String readText(File file) throws IOException {
		StringBuffer text = new StringBuffer();
		FileReader in = null;
		BufferedReader br = null;
		try {
			if (file != null && file.isFile()) {
				in = new FileReader(file);
				br = new BufferedReader(in);
				String lineSep = System.getProperty("line.separator");
				String line = null;
				while ((line = br.readLine()) != null) {
					text.append(line);
					text.append(lineSep);
				}

			}
		} catch (IOException e) {
			e.printStackTrace(System.out);
		} finally {
			close(in);
			close(br);
		}
		return text.toString();
	}

	public static String readStreamAsText(InputStream in) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		if (in != null) {
			try {
				byte[] trecho = new byte[10240];
				int quant = 0;
				while ((quant = in.read(trecho)) > -1) {
					out.write(trecho, 0, quant);
					out.flush();
				}
			} catch (IOException err) {
				err.printStackTrace();
			} finally {
				close(out);
				close(in);
			}
		}
		return new String(out.toByteArray());
	}

	/**
	 * Responsável por ler e converter uma arquivo do tipo File em um array de bytes. Método modificado, adicionando o
	 * tratamento da exceção FileNotFoundException.
	 * 
	 * @issue	PJEII-19266 
	 * @param	arquivo
	 * @return	retorna o arquivo do tipo File convertido em array de bytes
	 */
	public static byte[] readFile(File arquivo) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(arquivo);
			byte[] bytes = new byte[fis.available()];
			fis.read(bytes, 0, fis.available());
			return bytes;
		} catch (FileNotFoundException fnfe) {
			logger.error("O arquivo {0} não foi encontrado para a realização do cálculo de seu hash.", arquivo.getName());
			fnfe.printStackTrace();
		} catch (IOException err) {
			logger.error("Erro ao tentar ler o arquivo {0} para a realização do cálculo de seu hash ou da identificação"
					+ " de seu tipo de conteúdo.", arquivo.getName());
			err.printStackTrace();
		} finally {
			close(fis);
		}
		return null;
	}

	public static boolean deleteFile(File file) {
		if (file.exists()) {
			return file.delete();
		}
		return false;
	}
	
	public static File getDirectory(String path) {
		File directory = new File(path);
		directory.mkdirs();
		if(directory.isDirectory()) {
			return directory;
		}
		return null;
	}

	/**
	 * If directory not exists, it will create it
	 * 
	 * @param path
	 * @param filename
	 * @return
	 */
	public static File getFileFromDir(String path, String filename) {
		File file = null;
		File directory = getDirectory(path);
		if(directory != null) {
			for (File f : directory.listFiles()) {
				if (f.getName().equals(filename)) {
					file = f;
					break;
				}
			}
		}
		
		return file;
	}
	
	public static File createTempFile(byte[] conteudo) {
		File file = null;
		FileOutputStream fileOutputStream = null;
		
		try {
			file = File.createTempFile("tempFile", ".tmp");
			fileOutputStream = new FileOutputStream(file);
			fileOutputStream.write(conteudo);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fileOutputStream != null) {
					fileOutputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	
	/**
	 * Realiza as seguintes validações:
	 * <ul>
	 * 		<li>O MIME type do arquivo é igual ao MIME type informado?</li>
	 * 		<li>O MIME type do arquivo é válido?</li>
	 * 		<li>O tamanho máximo do arquivo ultrapassa o tamanho máximo permitido para o MIME type do arquivo?</li>
	 * </ul>
	 * 
	 * @param file Arquivo que será validado.
	 * @param mimeType MIME type para validação.
	 * 
	 * @throws PJeBusinessException Caso alguma validação não seja verdadeira.
	 * @throws IllegalArgumentException aso não seja infomado parâmetros de entrada válidos. 
	 */
	public static void validarTipoTamanhoArquivo(FileHome fileHome, String mimeType) throws PJeBusinessException {
		if (fileHome == null || StringUtils.isEmpty(mimeType)) {
			throw new IllegalArgumentException("Parâmetros inválidos");
		}
		
		MimeUtilChecker mimeUtil = ComponentUtil.getComponent("mimeUtilChecker");
		
		try {			
			if (!mimeType.equalsIgnoreCase(fileHome.getContentType())) {
				throw new PJeException();
			}
			
			mimeUtil.checkAllowed(fileHome.getFileName(), fileHome.getContentType(), new Long(fileHome.getSize()));
		} catch (PJeException e) {
			double tamanho;
			
			try {
				tamanho = mimeUtil.getSize(fileHome.getContentType()) / 1024;
			} catch (PJeException ex) {
				throw new PJeBusinessException(ex.getCode());
			}

			throw new PJeBusinessException(
					String.format("O documento deve ser do tipo %s, com o tamanho máximo de %.1fMB.", 
							StringUtils.upperCase(mimeType.substring(mimeType.indexOf("/") + 1)), tamanho / 1024));
		}
	}

	/**
	 * Retorna a mensagem que informa o tamanho máximo do arquivo 
	 * de acordo com o MIME type informado.
	 * 
	 * @param mime MIME type
	 * @return Mensagem informativa sobre o tamanho do arquivo.
	 * 
	 * @throws PJeException Caso o MIME type não seja encontrado.
	 * @throws IllegalArgumentException Caso não seja infomado um parâmetro de entrada válido. 
	 */
	public static String getMensagemTamanhoMime(String mime) throws PJeException {
		if (StringUtils.isEmpty(mime)) {
			throw new IllegalArgumentException("MIME type não especificado");
		}
		
		MimeUtilChecker mimeUtil = ComponentUtil.getComponent("mimeUtilChecker");

		double tamanho = mimeUtil.getSize(mime) / 1024;
		return String.format("Tamanho máximo é %.1fMB (%.3fKB) por arquivo.", tamanho / 1024, tamanho / 1000);
	}
	
	/**
	 * Retorna a mensagem que informa o tipo do arquivo 
	 * de acordo com o MIME type informado.
	 * 
	 * @param mime MIME type
	 * @return Mensagem informativa sobre o tipo do arquivo.
	 * 
	 * @throws PJeException Caso o MIME type não seja encontrado.
	 * @throws IllegalArgumentException Caso não seja infomado um parâmetro de entrada válido.
	 */
	public static String getMensagemTipoMime(String mime) throws PJeException {
		if (StringUtils.isEmpty(mime)) {
			throw new IllegalArgumentException("MIME type não especificado");
		}
		
		return String.format("Documento do tipo %s.", StringUtils.upperCase(mime.substring(mime.indexOf("/") + 1)));
	}

}