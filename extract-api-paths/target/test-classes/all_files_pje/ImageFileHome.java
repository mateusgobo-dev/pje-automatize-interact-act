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
package br.com.infox.ibpm.component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.Util;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;

@Name("imageFileUpload")
@BypassInterceptors
public class ImageFileHome {

	private static final String IMAGES_DIR = "/img/";
	private static final long serialVersionUID = 1L;
	private static final LogProvider log = Logging.getLogProvider(ImageFileHome.class);
	private List<Arquivo> files = new ArrayList<Arquivo>();
	private int uploadsAvailable = 10;
	
	private boolean reRenderMessage;
	private String nomeArquivo;

	private String getUserImageDir() {
		UsuarioLocalizacao usuarioLoc = (UsuarioLocalizacao) Contexts.getSessionContext().get(Authenticator
				.USUARIO_LOCALIZACAO_ATUAL);
		if (usuarioLoc != null) {
			String usuarioId = Integer.toString(usuarioLoc.getUsuario().getIdUsuario());
			String loc = "localizacao" + "/" + usuarioId;
			return loc;
		} else {
			log.warn("Diretório de imagens : usuário sem localização");
			return null;
		}
	}
	
	/**
	 * [PJEII-1139] Método responsável por apresentar a imagem através do caminho absoluto.
	 * 
	 * @param caminhoAbsoluto
	 * @return OutputStream
	 */
	public OutputStream showImage(String caminhoAbsoluto) throws IOException {
		InputStream in = null;
		//OutputStream outs = null;
		ServletOutputStream out = null;
		File file = new File(caminhoAbsoluto);
		HttpServletResponse  response = (HttpServletResponse)  FacesContext.getCurrentInstance().getExternalContext()
				.getResponse();
		response.setHeader("Content-Disposition", "attachment;filename=\"" + file.getName() + "\"");
		response.setContentType("text/html;charset=ISO-8859-1");
		response.setCharacterEncoding("ISO-8859-1");
		response.setContentLength((int) file.length());
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Content-Disposition", "inline");
		FacesContext.getCurrentInstance().responseComplete();
		try {
			FileInputStream input = new FileInputStream(file);
			out = response.getOutputStream();
			byte[] buffer = new byte[1024];
			int i;
			while ((i = input.read(buffer)) != -1) {
				out.write(buffer, 0, i);
			}
			out.flush();
			out.close();
			input.close();
			FacesContext.getCurrentInstance().getResponseComplete();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null)
				in.close();
		}
		return out;
	}
	
	public String[] getImagesDir() {
		//String path = new Util().getContextRealPath() + IMAGES_DIR;
		
		StringBuilder p = new StringBuilder(System.getProperty("jboss.home.dir"));
		p.append(IMAGES_DIR);
		String path = p.toString();
		String userImageDir = this.recuperarImageDir();
		if (userImageDir != null) {
			String[] images = { path, path + userImageDir };
			return images;
		} else {
			String[] images = { path };
			return images;
		}
	}
	
	public String[] getImagesDirAplicacao() {
		String path = new Util().getContextRealPath() + IMAGES_DIR;
		String userImageDir = this.recuperarImageDir();
		if (userImageDir != null) {
			String[] images = { path, path + userImageDir };
			return images;
		} else {
			String[] images = { path };
			return images;
		}
	}
	
	public String[] getImagesPathAplicacao() {
		String path = new Util().getContextRealPath() + IMAGES_DIR;
		String userImageDir = this.recuperarImageDir();
		if (userImageDir != null) {
			String[] images = { path, path + userImageDir };
			return images;
		} else {
			String[] images = { path };
			return images;
		}
	}

	public String[] getImagesPath() {
		//String path = new Util().getContextPath() + IMAGES_DIR;
		
		StringBuilder p = new StringBuilder(System.getProperty("jboss.home.dir"));
		p.append(IMAGES_DIR);
		String path = p.toString();
		
		String userImageDir = this.recuperarImageDir();
		if (userImageDir != null) {
			String[] images = { path, path + userImageDir };
			return images;
		} else {
			String[] images = { path };
			return images;
		}
	}

	/**
	 * Método responsável por validar o contexto e recuperar 
	 * @return String
	 */
	private String recuperarImageDir() {
		String userImageDir = null;
		if (!Util.instance().getIdPagina().equalsIgnoreCase("/ModeloDocumento/listView.seam")) {
			userImageDir = getUserImageDir();
		}
		return userImageDir;
	}

	/**
	 * [PJEII-1139] Alteração da chamada para o método que recupera o caminho das imagens.
	 * @return String
	 */
	public String getImagePath() {
		String[] imagesPath = getImagesPath();
		return imagesPath[imagesPath.length - 1];
	}

	public void listener(UploadEvent e) {
		// TODO retirar
		System.out.println("Iniciando upload");
		UploadItem uit = e.getUploadItem();
		try {
			//[PJEII-1139] verificação necessária para identificar se há criação de arquivo temporário ou utiliza-se
			//arquivo em memória.
			if (uit.getData() != null) {
				this.saveFile(uit.getData(), getFileDestino(uit));
			} else {
				this.copyFile(uit.getFile(), getFileDestino(uit));
			}
		} catch (IOException e1) {
			FacesMessages.instance().add("Erro ao adicionar arquivo: " + e1.getMessage());
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Erro ao adicionar arquivo: " + e1.getMessage()));
		}
	}

	/**
	 * [PJEII-1139] Método responsável por criar o arquivo de destino
	 * @param uit
	 * @return File
	 */
	private File getFileDestino(UploadItem uit) {
		String[] imagesDir = getImagesDir();
		String imageDir = imagesDir[imagesDir.length - 1];
		File fileDestino = new File(imageDir, uit.getFileName());
		return fileDestino;
	}

	/**
	 * [PJEII-1139] Metodo que recebe um array de bytes e um File indicando o destino e salva
	 * os bytes no arquivo de destino.
	 * 
	 * @param bytesOrigem
	 * @param fileDestino
	 * @throws IOException
	 */
	private void saveFile(byte[] bytesOrigem, File fileDestino) throws IOException {
		if (fileDestino.exists()) {
			this.nomeArquivo = fileDestino.getName();
			reRenderMessage = true;
		} else {
			fileDestino.createNewFile();
			OutputStream out = new FileOutputStream(fileDestino);
			out.write(bytesOrigem);
			out.flush();
			out.close();
			log.info("Upload feito com sucesso: " + getUserImageDir() + "/" + fileDestino.getName());
			files.add(recuperaArquivo(fileDestino, bytesOrigem));
		}
	}
	
	/**
	 * [PJEII-1139] Metodo resposável por copiar o arquivo recebido do componente fileUpLoad para a pasta padrão do 
	 * sistema, quando o atributo createFileTemp for true.
	 * @param file
	 * @param fileDestino
	 */
	private void copyFile(File file, File fileDestino) {
		try {
			if(!fileDestino.exists()) {
				fileDestino.createNewFile();
			} else {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Arquivo existente"));
			}
			FileChannel source = null;
			FileChannel destination = null;
			try {
				source = new FileInputStream(file).getChannel();
				destination = new FileOutputStream(fileDestino).getChannel();
				destination.transferFrom(source, 0, source.size());
			}
			finally {
				if(source != null) {
					source.close();
				}
				if(destination != null) {
					destination.close();
				}
			}
		} catch (IOException ioe) {
			FacesMessages.instance().add("Erro ao adicionar arquivo: " + ioe.getMessage());
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Erro ao adicionar arquivo: " + ioe
					.getMessage()));
		}
	}

	/**
	 * Método responsável por verificar se o registro já existe
	 * @param nome
	 * @return String
	 */
	/*private String getNewFileConflict(String nome) {
		int localPonto = nome.lastIndexOf(".");
		String ext = nome.substring(localPonto);
		String pre = nome.substring(0, localPonto);
		return pre + "_" + ext;
	}*/
	
	
	/**
	 * [PJEII-1139] Método responsável por criar um file do tipo Arquivo.
	 * @param fileDestino
	 * @param data
	 * @return Arquivo
	 */
	private Arquivo recuperaArquivo(File fileDestino, byte[] data) {
		Arquivo arquivo = new Arquivo();
		arquivo.setData(data);
		arquivo.setLength(data.length);
		arquivo.setName(fileDestino.getName());
		return arquivo;
	}

	/**
	 * Método responsável por escrever os dados da imagem na tela.
	 * @param stream
	 * @param object
	 * @throws IOException
	 */
	public void paint(OutputStream stream, Object object) throws IOException {
		if (stream != null && object != null) {
			stream.write(getFiles().get((Integer)object).getData());
		}
    }
	
	/**
	 * [PJEII-1139] Método responsável por Recuperar o timeStamp
	 * @return long
	 */
	public long getTimeStamp(){  
        return System.currentTimeMillis();  
    } 
	
	@Factory(scope=ScopeType.APPLICATION, value="images")
	public List<String> getImages() throws IOException {
		createDir();
		List<String> files = new ArrayList<String>();
		for (int i = 0; i < getImagesDir().length; i++) {
			File dir = new File(getImagesDir()[i]);
			if (!dir.canRead()) {
				log.error("Não foi possível abrir o diretório \"" 
						  + dir.getAbsolutePath() + "\" devido falta de permissão.");
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Não foi possível abrir o diretório \"" 
						  + dir.getAbsolutePath() + "\" devido falta de permissão."));
				return null;
			}
			//[PJEII-1139] refactor
			String[] filesImg = retornaFileName(dir);
			for (int j = 0; j < filesImg.length; j++) {
				filesImg[j] = getImagesPath()[i] + "/" + filesImg[j];
				files.add(filesImg[j]);
			}
		}

		return files;
	}

	/**
	 * Método responsável por criar o diretório a ser gravada a imagem
	 */
	private void createDir() {
		for (int i = 0; i < getImagesDir().length; i++) {
			File dir = new File(getImagesDir()[i]);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
	}

	
	/**
	 * Método responsável por retornar a lista de arquivos que será renderizado no componente rich:dataGrid e apresentado
	 * no a:imageOutPut
	 * 
	 * @return List<File>
	 * @throws IOException 
	 */
	public List<Arquivo> getFiles() throws IOException {
		files.clear();
		this.recuperarFiles();
		return files;
	}
	
	/**
	 * @param verifica
	 */
	public void getFiles(boolean verifica) {
		if (verifica && files != null && files.isEmpty()) {
			try {
				this.recuperarFiles();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @throws IOException
	 */
	private void recuperarFiles() throws IOException {
		for (int i = 0; i < getImagesDir().length; i++) {
			File dir = new File(getImagesDir()[i]);
			if (!dir.canRead()) {
				if (!dir.exists()) {
					createDir();
				} else {
					log.error("Não foi possível abrir o diretório \"" 
						  + dir.getAbsolutePath() + "\" devido falta de permissão.");
				}
			} else {
				this.preencherFiles(dir, this.retornaFileName(dir), i, false);
			}
		}
		for (int j = 0; j < getImagesDirAplicacao().length; j++) {
			File dir = new File(getImagesDirAplicacao()[j]);
			if (!dir.canRead()) {
				if (!dir.exists()) {
					createDir();
				} else {
					log.error("Não foi possível abrir o diretório \"" 
						  + dir.getAbsolutePath() + "\" devido falta de permissão.");
				}
			} else {
				this.preencherFiles(dir, this.retornaFileName(dir), j, true);
			}
		}
	}

	/**
	 * [PJEII-1139] Método responsável por retornar o arquivo com a extensão correta
	 * @param dir
	 * @return String[]
	 */
	private String[] retornaFileName(File dir) {
		String[] filesImg = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return (name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".gif"));
			}
		});
		return filesImg; 
	}

	/**
	 * [PJEII-1139] Refactor do método preencher Arquivos, para maior especificidade e melhor do entendimento
	 * @param dir
	 * @param filesImg
	 * @param i
	 * @throws IOException
	 */
	private void preencherFiles(File dir, String[] filesImg, int i, boolean isCaminhoAplicacao) throws IOException {
		Set<Arquivo> set = new HashSet<Arquivo>();
		for (int j = 0; j < filesImg.length; j++) {
			if (!isCaminhoAplicacao) {
				filesImg[j] = getImagesPath()[i] + "/" + filesImg[j];
			} else {
				filesImg[j] = getImagesPathAplicacao()[i] + "/" + filesImg[j];
			}
			Arquivo arquivo = new Arquivo();
			InputStream is = null;  
		    try {
		        long length = dir.length();  
		        if (length > 100000000) FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("tamanho do arquivo muito grande."));  
		        byte[] ret = new byte [(int) length];
		        File file = new File(filesImg[j]);
		        is = new FileInputStream(file);  
		        is.read (ret);
		        arquivo.setData(ret);
		        arquivo.setLength(length);
		        arquivo.setName(file.getName());
		        set.add(arquivo);
		    } finally {  
		        if (is != null) try { is.close(); } catch (IOException ex) {}  
		    }
		}
		files.addAll(set);
	}

	/**
	 * @param files
	 */
	public void setFiles(List<Arquivo> files) {
		this.files = files;
	}

	
	/**
	 * @return
	 */
	public boolean isReRenderMessage() {
		return reRenderMessage;
	}

	/**
	 * @param reRenderMessage
	 */
	public void setReRenderMessage(boolean reRenderMessage) {
		this.reRenderMessage = reRenderMessage;
	}
	
	public String getNomeArquivo() {
		if (this.nomeArquivo == null) {
			this.nomeArquivo = "";
		}
		return nomeArquivo;
	}

	public void setNomeArquivo(String nomeArquivo) {
		this.nomeArquivo = nomeArquivo;
	}

	public int getUploadsAvailable() {
		return uploadsAvailable;
	}

	public void setUploadsAvailable(int uploadsAvailable) {
		this.uploadsAvailable = uploadsAvailable;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}