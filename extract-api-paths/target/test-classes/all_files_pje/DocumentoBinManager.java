/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.IOUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.log.Log;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.extensao.StorageException;
import br.jus.cnj.pje.extensao.servico.StorageService;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;

/**
 * @author cristof
 *
 */
@Name("documentoBinManager")
public class DocumentoBinManager implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@Logger
	private Log logger;

	@In(create=true)
	private StorageService storageService;
	
	@Transactional
	public byte[] getData(String numeroDocumentoStorage) throws PJeBusinessException{
		byte[] byteArray = null;
		StorageService storageService = getStorageService();
		if(storageService != null && numeroDocumentoStorage != null && numeroDocumentoStorage.trim().length() > 0 ){
			InputStream is = null;
			try {
				is = storageService.retrieve(numeroDocumentoStorage);
				byteArray = IOUtils.toByteArray(is);
			} catch (StorageException t) {
				throw new PJeBusinessException("Erro ao recuperar arquivo no repositório. " + t.getLocalizedMessage());
			} catch (IOException e) {
				throw new PJeBusinessException("Erro ao converter o arquivo recuperado. " + e.getLocalizedMessage());
			}finally{
				if(is != null){
					try {
						is.close();
					} catch (IOException e) {
						logger.error("Erro ao tentar fechar o stream oriundo do storage: {0}", e.getLocalizedMessage());
					}
				}
			}
		}
		return byteArray;
	}
	
	/**
	 * Retorna o inputstream do documento.<br/>
	 * Observação: o inputstream retornado está aberto e precisa ser fechado depois de usado.
	 *  
	 * @param numeroDocumentoStorage Número do documento no storage.
	 * @return InputStream ABERTO.
	 * @throws PJeBusinessException
	 */
	@Transactional
	public InputStream getInputStream(String numeroDocumentoStorage) throws PJeBusinessException{
		InputStream resultado = null;
		StorageService storageService = getStorageService();
		if(storageService != null && StringUtils.isNotBlank(numeroDocumentoStorage)){
			try {
				resultado = storageService.retrieve(numeroDocumentoStorage);
			} catch (Throwable t) {
				t.printStackTrace();
				throw new PJeBusinessException("Erro ao recuperar arquivo no repositório.", t);
			}
		}
		return resultado;
	}
	

	
	public String persist(ProcessoDocumentoBin pdb) throws PJeBusinessException{
		if(pdb.getFile() == null){
			throw new IllegalArgumentException("Tentativa de gravar um documento binário sem conteúdo.");
		}
		
		File f = pdb.getFile();
		if(!f.exists()){
			throw new IllegalArgumentException("O arquivo vinculado ao documento que se pretende gravar não existe.");
		}else if(!f.isFile()){
			throw new IllegalArgumentException("A referência de arquivo do documento não é válida.");
		}else if(f.length() == 0){
			throw new IllegalArgumentException("O arquivo vinculado ao documento está vazio.");
		}
		byte[] data = new byte[(new Long(f.length()).intValue())];
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			fis.read(data);
			return persist(data,"application/octet-stream");
		} catch (FileNotFoundException e) {
			throw new PJeBusinessException("Erro ao tentar recuperar arquivo binário para gravação.");
		} catch (IOException e) {
			throw new PJeBusinessException("Erro ao tentar recuperar arquivo binário para gravação.");
		}finally{
			if(fis != null){
				try {
					fis.close();
				} catch (IOException e) {
					logger.error("Erro ao tentar fechar o arquivo.");
				}
			}
		}
	}
	
	public String persist(byte[] data,String mimeType) throws PJeBusinessException {
		StorageService storageService = getStorageService();
		
		if (data == null || data.length == 0) {
			throw new PJeBusinessException("O arquivo não pode estar vazio.");
		}
		if (storageService == null) {
			throw new PJeBusinessException("Componente StorageService não instanciado.");
		}
		
		try {
			return storageService.persist(new ByteArrayInputStream(data));
		} catch (StorageException e) {
			throw new PJeBusinessException("Erro ao gravar arquivo no storage. ", e);
		}
	}
	
	public static DocumentoBinManager instance() {
		return ComponentUtil.getComponent("documentoBinManager");
	}

	/**
	 * @return storageService.
	 */
	protected StorageService getStorageService() {
		if (storageService == null) {
			storageService = ComponentUtil.getComponent(StorageService.class);
		}
		return storageService;
	}
	
}