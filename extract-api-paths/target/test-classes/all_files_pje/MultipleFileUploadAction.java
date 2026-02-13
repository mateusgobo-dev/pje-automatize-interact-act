/**
 * 
 */
package br.jus.cnj.pje.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.jboss.seam.log.Log;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

import br.com.itx.util.FileUtil;
import br.jus.cnj.certificado.SignedData;
import br.jus.cnj.certificado.Signer;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.servicos.MimeUtilChecker;
import br.jus.pje.nucleo.util.Crypto;

/**
 * @author cristof
 *
 */
public class MultipleFileUploadAction implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 419856081977970010L;
	
	private MimeUtilChecker mimeUtilChecker;
	
	private Log logger;
	
	public MultipleFileUploadAction(MimeUtilChecker mimeChecker){
		this.mimeUtilChecker = mimeChecker;
		logger = org.jboss.seam.log.Logging.getLog(MultipleFileUploadAction.class);
	}
	
	/**
	 * @author cristof
	 *
	 */
	public class UploadedFile{
		
		private String fileName;
		
		private int fileSize;
		
		private String mimeType;
		
		private File file;
		
		private File signedContentFile;
		
		private String contentsHash;
		
		private SignedData signedData;

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public int getFileSize() {
			return fileSize;
		}

		public void setFileSize(int fileSize) {
			this.fileSize = fileSize;
		}

		public String getMimeType() {
			return mimeType;
		}

		public void setMimeType(String mimeType) {
			this.mimeType = mimeType;
		}

		public File getFile() {
			return file;
		}

		public void setFile(File file) {
			this.file = file;
		}
		
		public String getContentsHash() {
			return contentsHash;
		}

		public void setContentsHash(String contentsHash) {
			this.contentsHash = contentsHash;
		}

		public File getSignedContentFile() {
			return signedContentFile;
		}

		public void setSignedContentFile(File signedContentFile) {
			this.signedContentFile = signedContentFile;
		}

		public SignedData getSignedData() {
			return signedData;
		}

		public void setSignedData(SignedData signedData) {
			this.signedData = signedData;
		}
		
	}
	
	private List<UploadedFile> uploadedFiles = new ArrayList<MultipleFileUploadAction.UploadedFile>();
	
	public List<UploadedFile> listener(UploadEvent uploadEvent) throws PJeBusinessException{
		List<UploadedFile> ret = new ArrayList<MultipleFileUploadAction.UploadedFile>();
		logger.debug("Evento de upload iniciado");
		if(uploadEvent.isMultiUpload()){
			logger.debug("Evento de upload múltiplo");
			List<UploadItem> items = uploadEvent.getUploadItems();
			logger.debug("Processando [{0}] itens", items.size());
			for(UploadItem item: items){
				logger.debug("Processando arquivo [{0}].", item.getFileName());
				UploadedFile uf = processItem(item);
				if(isPdf(uf)) {
					validarPdfEditavel(uf);
				}
				if(uf == null){
					logger.debug("Arquivo não permitido: {0}.", item.getFileName());
					throw new PJeBusinessException("Formato de arquivo não permitido.");
				}else{
					uploadedFiles.add(uf);
					ret.add(uf);
					logger.debug("Item [{0}] processado e armazenado no arquivo temporário [{1}]", uf.getFileName(), uf.getFile().getName());
				}
			}
			logger.debug("Evento de upload múltiplo finalizado");
		}else{
			logger.debug("Evento de upload único.");
			if (uploadEvent.getUploadItem()==null) {
				logger.debug("Arquivo vazio. Parâmetro uploadEvent.getUploadItem() retornou null");
				throw new PJeBusinessException("Arquivo vazio. Parâmetro uploadEvent.getUploadItem() retornou null");
			}
			else {
				UploadedFile uf = processItem(uploadEvent.getUploadItem());

				if(isPdf(uf)) {
					validarPdfEditavel(uf);
				}

				if(uf == null){
					logger.debug("Arquivo não permitido: {0}.", uploadEvent.getUploadItem().getFileName());
				}else{
					uploadedFiles.add(uf);
					ret.add(uf);
					logger.debug("Item [{0}] processado e armazenado no arquivo temporário [{1}]", uf.getFileName(), uf.getFile().getName());
				}
				logger.debug("Item [{0}] processado.", uploadEvent.getUploadItem().getFileName());
			}
		}
		logger.debug("Evento de upload finalizado.");
		return ret;
	}

	private boolean isPdf(UploadedFile uf) {
		return uf != null && uf.getMimeType() != null && uf.getMimeType().trim().equals("application/pdf");
	}

	public UploadedFile processItem(UploadItem item) throws PJeBusinessException{
		UploadedFile up = new UploadedFile();
		if(!item.isTempFile()){
			// Alguém incompetente configurou o sistema.
			try {
				File f = File.createTempFile("uploaded", ".tmp");
				try(FileOutputStream fos = new FileOutputStream(f)){
					fos.write(item.getData());
				}
				up.setFile(f);
			} catch (IOException e) {
				logger.error("Erro ao tentar criar arquivo temporário de upload. Tente configurar o sistema para que os uploads sejam gravados temporariamente pelo servidor de aplicação.");
			}
		}else{
			up.setFile(item.getFile());
		}
		up.setFile(item.getFile());
		up.setFileName(item.getFileName());
		up.setFileSize(item.getFileSize());
		up.setMimeType(item.getContentType());
		processFile(up, up.getFile());
		return up;
	}
	
	private void processFile(UploadedFile uf, File file) throws PJeBusinessException{
		SignedData sd = null;
		File contents = file;
		try {
			try(FileInputStream fs = new FileInputStream(file)){
				sd = Signer.unwrap(fs).get(0);	
			}
		} catch (OperatorCreationException e) {
			logger.error("Erro ao tentar obter conteúdo assinado: {0}", e.getLocalizedMessage());
		} catch (CertificateException e) {
			logger.error("Erro de certificado ao tentar obter conteúdo assinado: {0}", e.getLocalizedMessage());
		} catch (FileNotFoundException e) {
			logger.error("Não foi encontrado o arquivo {0} ao tentar obter conteúdo assinado: {1}", file.getName(), e.getLocalizedMessage());
		} catch (CMSException e) {
			logger.error("O arquivo {0} não é um arquivo CMS bem formado: {1}", uf.getFileName(), e.getLocalizedMessage());
		} catch (CertException e) {
			logger.error("Exceção de certificado ocorrida ao tentar obter o conteúdo assinado do arquivo {0}: {1}", uf.getFileName(), e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error("Erro ao ler o arquivo {0}: {1}", file.getName(), e.getLocalizedMessage());
		}
		
		if(sd != null){
			try {
				File signedContentFile = File.createTempFile("signed" + uf.getFileName(), ".bin");
				try(FileOutputStream fos = new FileOutputStream(signedContentFile)){
					fos.write(sd.getData());
					fos.flush();
				}
				uf.setSignedContentFile(signedContentFile);
				uf.setSignedData(sd);
				contents = signedContentFile;
			} catch (IOException e) {
				logger.error("Erro ao tentar criar o arquivo de conteúdo do arquivo assinado {0}: {1}", uf.getFileName(), e.getLocalizedMessage());
			}
		}
		/**
		 * Modificado o método para gerar o md5 em todo o sistema. Utilizava o DigestUtils.md5Hex(byte[] data), porém
		 * foi orientado alterar para o Crypto.encodeMD5(byte[] data).
		 * 
		 * @issue	PJEII-19266
		 */
		String md5 = Crypto.encodeMD5(FileUtil.readFile(contents));
		
		uf.setContentsHash(md5);
		mimeUtilChecker.checkContentType(contents, uf.getFileName());
	}
	
	public List<UploadedFile> getUploadedFiles() {
		return uploadedFiles;
	}
	
	public void setUploadedFiles(List<UploadedFile> uploadedFiles) {
		this.uploadedFiles = uploadedFiles;
	}

	public void clear(){
		logger.info("Limpando??");
	}
	
	/*
	NÃO PERMITIR O ENVIO DE PDF COM CAMPOS EDITÁVEIS NO SISTEMA
	PARA PDF EDITAVEIS, O PDACRONFORM É DIFERENTE DE NULO E NÃO POSSUI FIELDS
	*/
	private void validarPdfEditavel(UploadedFile uf) throws PJeBusinessException {
		PDDocument pdDoc = null;
		try {
			pdDoc = PDDocument.loadNonSeq( uf.getFile(), null );
			PDDocumentCatalog pdCatalog = pdDoc.getDocumentCatalog();
			PDAcroForm pdAcroForm = pdCatalog.getAcroForm();
			if(pdAcroForm != null && pdAcroForm.getFields() == null){
				throw new PJeBusinessException(" O documento '"+uf.getFileName()+"' é inválido.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (pdDoc != null) {
				try {
					pdDoc.close();
				} catch (IOException e) {
					logger.error("Erro ao tentar validar PDF editavel: {0}.", e.getLocalizedMessage());
				}
			}
		}
	}
}
