/**
 * 
 */
package br.jus.cnj.pje.servicos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.activemq.util.ByteArrayInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.tika.Tika;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.mime.MimeTypesFactory;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.apache.tika.sax.BodyContentHandler;
import org.gagravarr.tika.OggParser;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.log.Log;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeException;
import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;

/**
 * @author cristof
 *
 */
@Name("mimeUtilChecker")
@Scope(ScopeType.APPLICATION)
@Startup (depends = {"carregarParametrosAplicacao"})
public class MimeUtilChecker {
	
	private static String MIME_OCTET_STREAM = "application/octet-stream";
	
	/**
	 * formato: mimetype:tamanho:extensao1,extensao2,...
	 * as extensões são opcionais.
	 */
	private static final String[] defaultMimes = {
		"application/pdf:5242880:.pdf",
		"audio/mp3:5242880:.mp3",
		"audio/mpeg:5242880:.mpeg",
		"audio/mpeg3:5242880:.mpeg3",
		"audio/ogg:5242880:.oga",
		"audio/vorbis:5242880", 
		"image/png:1572864:.png",
		"video/ogg:10485760:.ogv",
		"video/mp4:10485760:.mp4",
		"text/html:5242880:.html",
		"application/vnd.google-earth.kml+xml:5242880:.kml"};

	@Logger
	private Log logger;
	
	@In(required=false)
	private String mimeData;
	
	private Map<String, Long> mimeSizeMap = new HashMap<String, Long>();
	
	@Create
	public void init(){
        MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
        if(mimeData != null){
        	logger.trace("Carregando informações de mimetypes e tamanhos de arquivos permitidos a partir de [{0}]", mimeData);
        	String[] dividido = mimeData.split(";");
        	loadMimeSizes(dividido);
        }else{
        	loadMimeSizes(defaultMimes);
        }
	}
	
	private void loadMimeSizes(String[] config){
    	for(String info: config){
    		String[] partes = info.split(":");
    		if(partes.length > 3 && partes.length < 2){
    			logger.error("Não foi possível carregar como tipo permitido o conteúdo de [{0}].", info);
    		}else{
    			try{
        			String mime = partes[0];
        			Long size = Long.parseLong(partes[1]);
        			mimeSizeMap.put(mime, size);
        			// verifica se há extensões a serem adicionadas 
        			if (partes.length > 2 ) {
        				String[] extensions = partes[2].split(",");
        				for (String extension : extensions) {
        					mimeSizeMap.put(extension, size);
						}
        			}
    			}catch(NumberFormatException e){
        			logger.error("Não foi possível carregar como tipo permitido o conteúdo de [{0}]. A informação sobre tamanho ([{1}]) não pode ser transformada para um número inteiro.", info, partes[1]);
    			}
    		}
    	}
	}
	
	/**
	 * Valida se o tipo e o tamanho do arquivo estão OK.
	 * 
	 * @param name Nome do arquivo.
	 * @param mime Mimetype do arquivo.
	 * @param size Tamanho do arquivo.
	 * @throws PJeBusinessException
	 */
	public void checkAllowed(String name, String mime, Long size) throws PJeBusinessException{
		checkMimeTypeAllowed(name, mime);
		checkSizeAllowed(name, mime, size);
	}
	
	public void checkContentType(File file) throws PJeBusinessException {
		this.checkContentType(file, null);
	}
	
	public void checkContentType(File file, String contentType) throws PJeBusinessException {
		String mimetype = checkByMimeUtil(file);

		if(!containsMimeTypeAllowed(mimetype)) {
			mimetype = checkByTikaResource(file);
			if(!containsMimeTypeAllowed(mimetype)) {
				mimetype = checkByContentParser(file, contentType);
			}
		}
		
		if(!containsMimeTypeAllowed(mimetype)) {
			String listaTiposPermitidos=String.format("%s", mimeSizeMap.keySet());
			throw new PJeBusinessException(String.format("O arquivo é do tipo [%s] e os formatos permitidos são %s.", mimetype, listaTiposPermitidos));
		}

		checkSizeAllowed(file.getName(), mimetype, file.length());
	}
	
	@SuppressWarnings("unchecked")
	private String checkByMimeUtil(File file) {
		Collection<MimeType> mimeTypes = (Collection<MimeType>) MimeUtil.getMimeTypes(file);
		String mimetype = null;
		for(MimeType mt: mimeTypes){
			logger.debug("Mime-type: {0}",mt.toString());
			mimetype = mt.toString();
			break;
		}
		return mimetype;
	}
	
	private String checkByTikaResource(File file) {
		String mimetype = null;
		TikaInputStream tis = null;
		try {
			MimeTypes mimes=MimeTypesFactory.create(Thread.currentThread().getContextClassLoader().getResource("tika-custom-MimeTypes.xml"));
			Metadata metadata = new Metadata();
			metadata.add(Metadata.RESOURCE_NAME_KEY, file.getName());
			tis = TikaInputStream.get(file);
			mimetype = new  DefaultDetector(mimes).detect(tis,metadata).toString();
			mimetype = mimetype != null && mimetype.equals("audio/opus") ? "audio/ogg" : mimetype != null && mimetype.equals("video/opus") ? "video/ogg" : mimetype;
		}
		catch(Exception e) {
			//swallow
			e.printStackTrace();
		}
		finally{
			if(tis != null){
				try {
					tis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return mimetype;
	}
	
	private String checkByContentParser(File file, String contentType) {
		String mimetype = null;
		BodyContentHandler handler = new BodyContentHandler();
		Metadata metaData = new Metadata();
		ParseContext pcontext = new ParseContext();
		
		try {
			FileInputStream inputStream = new FileInputStream(file);
			getParser(contentType).parse(inputStream, handler, metaData, pcontext);
			mimetype = metaData.get("Content-Type");
		}
		catch(Exception e) {
			//swallow
			e.printStackTrace();
		}
		
		return mimetype;
	}
	
	@SuppressWarnings("serial")
	private AbstractParser getParser(String contentType) {
		AbstractParser parser = getMapaMimeTypeParaParser().get(contentType);
	    if (parser == null) {
	    	parser = new AbstractParser() {

				@Override
				public Set<MediaType> getSupportedTypes(ParseContext context) {
					return null;
				}

				@Override
				public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context)
						throws IOException, SAXException, TikaException {
					metadata.add("Content-Type", null);
				}
	        };
	    }
	    return parser;
	}
	
	private static Map<String, AbstractParser> getMapaMimeTypeParaParser() {
	    Map<String, AbstractParser> mapaMimeTypeParaParser = new HashMap<String, AbstractParser>();
	    mapaMimeTypeParaParser.put("text/html", new HtmlParser());
	    mapaMimeTypeParaParser.put("application/pdf", new PDFParser());
	    mapaMimeTypeParaParser.put("audio/mp3", new Mp3Parser());
	    mapaMimeTypeParaParser.put("audio/mpeg", new Mp3Parser());
	    mapaMimeTypeParaParser.put("audio/mpeg3", new Mp3Parser());
	    mapaMimeTypeParaParser.put("video/mp4", new MP4Parser());
	    mapaMimeTypeParaParser.put("application/mp4", new MP4Parser());
	    mapaMimeTypeParaParser.put("audio/ogg", new OggParser());
	    mapaMimeTypeParaParser.put("video/ogg", new OggParser());
    
	    return mapaMimeTypeParaParser;
	}
	
	@SuppressWarnings("unchecked")
	public boolean checkMimeType(byte[] data, String...mimes){
		Collection<MimeType> mimeTypes = (Collection<MimeType>) MimeUtil.getMimeTypes(data);
		return checkMimeType(mimeTypes, mimes);
	}
	
	@SuppressWarnings("unchecked")
	public boolean checkMimeType(File file, String...mimes){
		Collection<MimeType> mimeTypes = (Collection<MimeType>) MimeUtil.getMimeTypes(file);
		return checkMimeType(mimeTypes, mimes);
	}
	
	@SuppressWarnings("unchecked")
	public boolean checkMimeType(InputStream is, String...mimes){
		Collection<MimeType> mimeTypes = (Collection<MimeType>) MimeUtil.getMimeTypes(is);
		return checkMimeType(mimeTypes, mimes);
	}
	
	private boolean checkMimeType(Collection<MimeType> mimeTypes, String...mimes){
		Set<String> mimeList = new HashSet<String>(Arrays.asList(mimes));
		return checkMimeType(mimeTypes, mimeList);
	}

	private boolean checkMimeType(Collection<MimeType> mimeTypes,
			Set<String> mimeList) {
		for(MimeType mt: mimeTypes){
			logger.debug("Mime-type: {0}",mt.toString());
			if(mimeList.contains(mt.toString())){
				return true;
			}
		}
		return false;
	}
	
	public String getMimeType(byte[] data){
		String mimetype = "unknown";

		TikaInputStream tis = null;
		try {
			if (data != null) {
				tis = TikaInputStream.get(data);
				mimetype = getMimeType(tis);
				if(StringUtils.equalsIgnoreCase(MIME_OCTET_STREAM, mimetype)){
					PDDocument doc = null;
					try{
						doc = PDDocument.load(new ByteArrayInputStream(data));
						mimetype = "application/pdf";
					}catch(IOException e){
						logger.error("Não foi possível fazer o parsing para PDF.");
					}finally{
						if(doc != null){
							doc.close();
						}
					}
				}
			}
		} catch (IOException e) {
			logger.error("Erro ao tentar identificar o tipo de arquivo a partir de seus bytes: {0}.", e.getLocalizedMessage());
		} finally {
			if(tis != null){
				try {
					tis.close();
				} catch (IOException e) {}
			}
		}
		return mimetype;
	}
	
	public String getMimeType(File f){
		String mimetype = "unknown";
		try {
			InputStream is = new FileInputStream(f);
			byte[] data = IOUtils.toByteArray(is);
			mimetype = getMimeType(data);
		} catch (FileNotFoundException e1) {
			logger.error("Não foi possível localizar o arquivo {0} para detecção de seu tipo.", f.getName());
		} catch (IOException e) {
			logger.error("Não foi possível ler o arquivo {0} para detecção de seu tipo.", f.getName());
		}
		
		return mimetype;
	}
	
	private String getMimeType(TikaInputStream tis) throws IOException{
		Tika tika = new Tika();
		return tika.detect(tis);
	}
	
	/**
	 * Retorna o charset do array de bytes.
	 * 
	 * @param bytes Array de bytes.
	 * @return String.
	 */
	public String getEncoding(byte[] bytes){
		String resultado = null;
		if (bytes != null) {
			CharsetDetector detector = new CharsetDetector();
			detector.setText(bytes);
			CharsetMatch match = detector.detect();
			resultado = match.getName();
		}
	    return resultado;
	}

	/**
	 * Retorna uma string com os mimetype's permitidos e o tamanho máximo em MB.
	 * Exemplo:
	 * application/pdf:3,audio/mpeg:5,audio/ogg:10,audio/vorbis:5,image/png:3,audio/mp4:10,video/quicktime:10
	 * 
	 * @return String com os mimes e o tamanho máximo permitido em MB.
	 */
	public String getDefaultMimesEhSize() {
		StringBuilder sb = new StringBuilder();
		
		for (Iterator<String> mimes = mimeSizeMap.keySet().iterator(); mimes.hasNext(); ) {
			String mime = mimes.next();
			Float size = new Float(mimeSizeMap.get(mime)) / 1024 / 1024;
			sb.append(mime).append(":").append(size).append(",");
		}
		
		if (sb.length() > 1) {
			sb.delete(sb.length()-1, sb.length());
		}
		return sb.toString();
	}
	
	/**
	 * Método que formata mimetype para apresentação ao usuário em Tool Tips, com os mimetype's (tipos de arquivos) 
	 * e o tamanho máximo em MB permitidos.
	 *  
	 * Exemplo:
	 * 		Arquivo "pdf" 3MB
	 * 		Arquivo "mpeg" 5MB
	 * 		Arquivo "ogg" 10MB
	 * 		Arquivo "vorbis" 5MB
	 * 		Arquivo "png" 3MB
	 * 		Arquivo "mp4" 10MB
	 * 		Arquivo "quicktime" 10MB
	 * 
	 * @author eduardo.pereira@tse.jus.br
	 * @return Lista de string formatada, com os mimes e o tamanho máximo permitido em MB para exibição ao usuário.
	 */
	public List<String> getListaMimesEhSizesTootipText() {
		
		List<String> listaMimesETamanho = new ArrayList<String>();
		
		for (Iterator<String> mimes = mimeSizeMap.keySet().iterator(); mimes.hasNext(); ) {
			String mime = mimes.next();
			Float size = new Float(mimeSizeMap.get(mime)) / 1024 / 1024;
			listaMimesETamanho.add("Arquivo \""+mime+"\"-"+size+"MB");
		}
		
		return listaMimesETamanho;
	}
	
	/**
	 * Retorna uma string com os mimetype's separados por vírgula.
	 * Exemplo:
	 * application/pdf,audio/mpeg,audio/ogg,audio/vorbis,image/png,audio/mp4,video/quicktime
	 * @return String com os mimetypes separados por vírgula.
	 */
	public String getDefaultMimes() {
		StringBuilder sb = new StringBuilder();
		
		for (Iterator<String> mimes = mimeSizeMap.keySet().iterator(); mimes.hasNext(); ) {
			String mime = mimes.next();
			sb.append(mime).append(",");
		}
		
		if (sb.length() > 1) {
			sb.delete(sb.length()-1, sb.length());
		}
		return sb.toString();
	}
	
	private boolean containsMimeTypeAllowed(String mimetype) {
		return mimeSizeMap.containsKey(mimetype);
	}
	
	/**
	 * Valida se o mime type do arquivo é válido.
	 * 
	 * @param name
	 * @param mime
	 * @throws PJeBusinessException
	 */
	protected void checkMimeTypeAllowed(String name, String mime) throws PJeBusinessException {
		if (!mimeSizeMap.containsKey(mime)) {
			throw new PJeBusinessException("pje.error.arquivo.mimenaopermitido", null, name, mime);
		}
	}

	/**
	 * Valida se o tamanho do arquivo é válido.
	 * 
	 * @param name
	 * @param mime
	 * @param size
	 * @throws PJeBusinessException
	 */
	protected void checkSizeAllowed(String name, String mime, Long size) throws PJeBusinessException {
		if (size > mimeSizeMap.get(mime).longValue()) {
			Long sizeAllowed = mimeSizeMap.get(mime).longValue();
			throw new PJeBusinessException("pje.error.arquivo.tamanhonaopermitido", null, name, size, sizeAllowed);
		}
	}

	/**
	 * Retorna o tamanho máximo do arquivo de acordo com o MIME type informado.
	 * 
	 * @param mime MIME type.
	 * @return Tamanho máximo do arquivo de acordo com o MIME type informado.
	 * 
	 * @throws PJeException Caso o MIME type não seja encontrado.
	 */
	public long getSize(String mime) throws PJeException {
		if (mimeSizeMap.containsKey(mime)) {
			return  mimeSizeMap.get(mime).longValue();
		}
		throw new PJeException("MIME type inexistente.");
	}
	
}