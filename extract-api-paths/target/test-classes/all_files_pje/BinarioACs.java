package br.jus.cnj.pje.nucleo.dto;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import br.com.itx.util.FileUtil;
import br.jus.cnj.pje.pjecommons.model.services.autoridadescertificadoras.Binario;
import br.jus.cnj.pje.pjecommons.utils.PjeStringUtils;


public class BinarioACs extends Binario{
	private static final long serialVersionUID = 1L;

	private static final String HASH_SUFFIX = ".hash";
	private static final String HASH_CODEC_SUFFIX = HASH_SUFFIX + ".codec";
	private static final String SIGNED_HASH_SUFFIX = HASH_SUFFIX + ".signed";
	private static final String SIGNATURE_ALGORITHM_SUFFIX = SIGNED_HASH_SUFFIX + ".algorithm";
	private static final String BASE_FILE_PATH = "META-INF" + File.separator + "autoridadesCertificadoras" + File.separator;
	
	private static final String DIR_ACs_ICPBR = "acs-icpbr";
	private static final String DINAMIC_FILE_PATH = System.getProperty("jboss.server.data.dir") + File.separator + DIR_ACs_ICPBR;

    private boolean conteudoAlterado = Boolean.FALSE;
    private String checksum;
	
    public BinarioACs(String nomeArquivoZip) {
    	super();
    	this.setFileName(nomeArquivoZip);
    	
		File autoridadesCertificadoras = FileUtil.getFileFromDir(DINAMIC_FILE_PATH, getFileName());
		if(autoridadesCertificadoras == null || !autoridadesCertificadoras.isFile()) {
			String checksumBase = getConteudoArquivoBaseIntoString(getNomeArquivoHash());
			String checksumCodecBase = getConteudoArquivoBaseIntoString(getNomeArquivoHashCodec());
			String checksumAssinadoBase = getConteudoArquivoBaseIntoString(getNomeArquivoHashAssinado());
			String signatureAlgorithmBase = getConteudoArquivoBaseIntoString(getNomeArquivoSignatureAlgorithm());
			String conteudoArquivoBase64Base = getConteudoArquivoBaseIntoString(getFileName());
			this.alteraConteudo(checksumBase, checksumAssinadoBase, conteudoArquivoBase64Base, checksumCodecBase, signatureAlgorithmBase);
		}else {
			try {
				byte[] fileContent = FileUtil.readFile(autoridadesCertificadoras);
		        String fileContentBase64 = new String(fileContent, Charsets.UTF_8);
				setContentBase64(fileContentBase64);

				String checksum = recuperaConteudoArquivo(getNomeArquivoHash());
				if(checksum != null) {
					setChecksum(checksum);
				}
				
				String hashCodec = recuperaConteudoArquivo(getNomeArquivoHashCodec());
				if(hashCodec != null) {
					setChecksumCodec(hashCodec);
				}
				
				String hashAssinado = recuperaConteudoArquivo(getNomeArquivoHashAssinado());
				if(hashAssinado != null) {
					setSignedChecksum(hashAssinado);
				}

				String signatureAlgorithm = recuperaConteudoArquivo(getNomeArquivoSignatureAlgorithm());
				if(signatureAlgorithm != null) {
					setSignatureAlgorithm(signatureAlgorithm);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }
    
    private String recuperaConteudoArquivo(String nomeArquivo) throws IOException {
    	return FileUtil.readText(DINAMIC_FILE_PATH, nomeArquivo);
    }

	public String getNomeArquivoHash() {
		return getFileName() + HASH_SUFFIX;
	}

	public String getNomeArquivoHashCodec() {
		return getFileName() + HASH_CODEC_SUFFIX;
	}

	public String getNomeArquivoHashAssinado() {
		return getFileName() + SIGNED_HASH_SUFFIX;
	}

	public String getNomeArquivoSignatureAlgorithm() {
		return getFileName() + SIGNATURE_ALGORITHM_SUFFIX;
	}

	public void alteraConteudo(String checksum, Binario binario) {
		this.alteraConteudo(checksum, binario.getSignedChecksum(), binario.getContentBase64(), binario.getChecksumCodec(), binario.getSignatureAlgorithm());
	}
	
	public void alteraConteudo(String checksum, String checksumAssinado, String conteudoBase64, String checksumCodec, String signatureAlgorithm) {
		if(getChecksum() == null || !getChecksum().equals(checksum)) {
			File directory = FileUtil.getDirectory(DINAMIC_FILE_PATH);
			if(directory != null) {
				if(PjeStringUtils.isNotEmpty(conteudoBase64)) {
					InputStream conteudoBase64Stream = new ByteArrayInputStream(conteudoBase64.getBytes());
					FileUtil.saveInputStreamToFile(conteudoBase64Stream, DINAMIC_FILE_PATH + File.separator + getFileName());
					setContentBase64(conteudoBase64);
					
					InputStream hashStream = new ByteArrayInputStream(checksum.getBytes());
					FileUtil.saveInputStreamToFile(hashStream, DINAMIC_FILE_PATH + File.separator + getNomeArquivoHash());
					setChecksum(checksum);
					
					InputStream hashCodecStream = new ByteArrayInputStream(checksumCodec.getBytes());
					FileUtil.saveInputStreamToFile(hashCodecStream, DINAMIC_FILE_PATH + File.separator + getNomeArquivoHashCodec());
					setChecksumCodec(checksumCodec);
					
					InputStream hashAssinadoStream = new ByteArrayInputStream(checksumAssinado.getBytes());
					FileUtil.saveInputStreamToFile(hashAssinadoStream, DINAMIC_FILE_PATH + File.separator + getNomeArquivoHashAssinado());
					setSignedChecksum(checksumAssinado);

					InputStream signatureAlgorithmStream = new ByteArrayInputStream(signatureAlgorithm.getBytes());
					FileUtil.saveInputStreamToFile(signatureAlgorithmStream, DINAMIC_FILE_PATH + File.separator + getNomeArquivoSignatureAlgorithm());
					setSignatureAlgorithm(signatureAlgorithm);
					
					setConteudoAlterado(true);
				}
			}
		}
	}
    
	private String getConteudoArquivoBaseIntoString(String nomeArquivo) {
		String txt = "";
		try {
			InputStream conteudoStream = getConteudoArquivoBase(nomeArquivo);
			if(conteudoStream != null) {
				txt = CharStreams.toString(new InputStreamReader(conteudoStream, Charsets.UTF_8));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return txt;
	}
	
	private InputStream getConteudoArquivoBase(String nomeArquivo) {
		String path = BASE_FILE_PATH + File.separator + nomeArquivo;
		return BinarioACs.class.getClassLoader().getResourceAsStream(path);
	}

	public boolean isConteudoAlterado() {
		return conteudoAlterado;
	}

	public void setConteudoAlterado(boolean conteudoAlterado) {
		this.conteudoAlterado = conteudoAlterado;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}
}
