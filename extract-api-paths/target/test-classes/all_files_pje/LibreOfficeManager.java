package br.jus.cnj.pje.editor.lool;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.jboss.seam.contexts.Contexts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.je.pje.entity.vo.BinarioVO;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class LibreOfficeManager {
	
	private static WopiClient wopiClient = new WopiClient(ConfiguracaoIntegracaoCloud.getWopiUrlExterno());;
	private static ConversorDocClient conversorDocClient = new ConversorDocClient(ConfiguracaoIntegracaoCloud.getUrlConversorPDF());
	private static final String libreOfficeUrlServer = ConfiguracaoIntegracaoCloud.getLOOnlineUrl();
	private static final String libreOfficeWopiUrlInterno = ConfiguracaoIntegracaoCloud.getWopiUrlInterno();
	private static final String pasta = ParametroUtil.getParametro(Parametros.NUMERO_ORGAO_JUSTICA);
	private static final String aplicacaoSistema = ParametroUtil.getParametro(Parametros.APLICACAOSISTEMA);
	private static final String wopiAccessHeaderKey = ConfiguracaoIntegracaoCloud.getWopiAccessHeaderKey();
	private static File arquivoPDFBranco;
	private static byte[] conteudoNovoDocumento;
	private static final Gson gson = new GsonBuilder().create();
	
	private String id;
	private String nome;
	private String extensao;
	private String arquivo;
	private String access;
	private String urlLool;
	private String urlFrame;
	private String urlDoc;
	

	public LibreOfficeManager(String nome, String extensao) {
		
		if(nome.indexOf("-") > -1) {
			this.nome = nome;
		}
		else {
			this.nome = aplicacaoSistema + "-" + nome;
		}
		this.id = pasta + "-" + this.nome + "." + extensao;
		this.extensao = extensao;
		this.arquivo = this.nome+"."+extensao;
		
		this.access = gerarTokenAcessoWopi();
		this.urlLool = gerarUrlLool();
		this.urlFrame = gerarUrlFrame();
		this.urlDoc = gerarUrlDoc();
	}
	
	public LibreOfficeManager(String arquivo) {
		this(arquivo.substring(0, arquivo.lastIndexOf('.')), arquivo.substring(arquivo.lastIndexOf('.')+1, arquivo.length()));
	}

	private String gerarTokenAcessoWopi() {
		Usuario usuario = Authenticator.getUsuarioLogado();
		
		DadosJwtWopi dados = new DadosJwtWopi();
		dados.setIdUsuario(usuario.getLogin());
		dados.setNomeDocumento(id);
		dados.setNomeUsuario(usuario.getNomeSobrenome());
        
        String subject = gson.toJson(dados);
        
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS512, wopiAccessHeaderKey)
                .compact();
	}
	
	public static void main(String[] args) {
		
		DadosJwtWopi dados = new DadosJwtWopi();
		dados.setIdUsuario("01835896103");
		dados.setNomeDocumento("pjecnj-teste.docx");
		dados.setNomeUsuario("Leonardo Borges");
        
        String subject = gson.toJson(dados);
        
        String jwt = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS512, "MEJENDFCM0Y1MzFCODYwNkE4QTZGRjg3MUQ5NEU2NjA5NENGQkNEN0E1QzY4OUVDMzRFOTJBNDVEQzNBMjcwOEFDMTUwQzVGQkRCQTkwMkQyNjA0RjBBMTgyNDY0QjY3ODc1MTU2QzZERTI3NEEyODIyMTVBNjBBNjhCNTYyREU=")
                .compact();
        
        System.out.println( jwt );
	}

	private String gerarUrlLool() {
		StringBuilder builder = new StringBuilder(libreOfficeUrlServer);
		builder.append("/loleaflet/dist/loleaflet.html");
		builder.append("?WOPISrc=");
		builder.append(libreOfficeWopiUrlInterno);
		builder.append(id);
		builder.append("&permission=edit");
		builder.append("&access_header="+access);
		
		return builder.toString();
	}
	
	private String gerarUrlFrame() {
		StringBuilder builder = new StringBuilder(libreOfficeUrlServer);
		builder.append("/loleaflet/dist/loleaflet.html");
		builder.append("?WOPISrc=");
		builder.append(libreOfficeWopiUrlInterno);
		builder.append(id);
		builder.append("&permission=edit");
		builder.append("&access_header="+access);
		
		try {
			return URLEncoder.encode(builder.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	private String gerarUrlDoc() {
		StringBuilder builder = new StringBuilder();
		builder.append(libreOfficeWopiUrlInterno);
		builder.append(id);
		return builder.toString();
	}
	
	public void carregarNovoDocumento() throws LoolException {
		salvarNovoDocumento();
		mostrarPDF(getArquivoPDFBranco());
	}
	
	private File getArquivoPDFBranco() throws LoolException {
		if ( arquivoPDFBranco==null ) {
			try {
				arquivoPDFBranco = File.createTempFile("branco", ".pdf");
				IOUtils.copy(LibreOfficeManager.class.getClassLoader().getResourceAsStream("novo.pdf"), new FileOutputStream(arquivoPDFBranco));
			} catch (Exception e) {
				throw new LoolException("Erro ao salvar documento PDF em branco", e);
			}
		}
		return arquivoPDFBranco;
	}

	public void mostrarPDF (File arquivo) {
		BinarioVO binario = new BinarioVO();
		binario.setMimeType("application/pdf");
		binario.setNomeArquivo(nome+".pdf");
		binario.setFile(arquivo);
		Contexts.getSessionContext().set("download-binario", binario);
	}
	
	public void salvarNovoDocumento() throws LoolException {
		byte[] conteudoNovoDocumento = getConteudoNovoDocumento();
		save(new ByteArrayInputStream(conteudoNovoDocumento));
		
	}

	private void save(InputStream inputStream) throws LoolException {
		try {
			wopiClient.save(pasta, arquivo, inputStream, access);
		} catch (Exception e) {
			throw new LoolException("Erro ao salvar documento no repositorio WOPI", e);
		}
		
	}

	private byte[] getConteudoNovoDocumento() throws LoolException {
		if ( conteudoNovoDocumento==null ) {
			try {
				conteudoNovoDocumento = IOUtils.toByteArray( LibreOfficeManager.class.getClassLoader().getResourceAsStream("novo.odt") );
			} catch (IOException e) {
				throw new LoolException("Erro ao ler conteudo do novo documento", e);
			}
		}
		return conteudoNovoDocumento;
	}
	
	public InputStream getContent() throws LoolException {
		try {
			return wopiClient.getContent(pasta, arquivo, access);
		} catch (Exception e) {
			throw new LoolException("Erro ao recuperar documento do repositorio WOPI", e);
		}
	}

	public File salvarPDFTemp(InputStream pdf) throws LoolException {
		try {
			File arquivoBinario = File.createTempFile(nome, ".pdf");
			IOUtils.copy(pdf, new FileOutputStream(arquivoBinario));
			return arquivoBinario;
		} catch (Exception e) {
			throw new LoolException("Erro ao salvar arquivo PDF temporario", e);
		}
	}

	private InputStream converterDocumento(InputStream is, String origem, String destino) throws LoolException {
		try {
			return conversorDocClient.convert(is, origem, destino);
		} catch (Exception e) {
			throw new LoolException("Erro ao converter documento para "+destino.toUpperCase(), e);
		}
	}

	public String getUrlLool() {
		return urlLool;
	}
	
	public String getUrlFrame() {
		return urlFrame;
	}
	
	public String getUrlDoc() {
		return urlDoc;
	}

	public static String getLibreofficeurlserver() {
		return libreOfficeUrlServer;
	}

	public InputStream getPDFContent() throws LoolException {
		InputStream isDoc = getContent();
		return converterDocumento(isDoc, extensao, "pdf");
	}
	
	private byte[] converterHtml(String html) throws LoolException {
		try {
			return IOUtils.toByteArray( converterDocumento(new ByteArrayInputStream(html.getBytes()), "html", "docx") );
		} catch (IOException e) {
			throw new LoolException("Erro ao converter HTML em DOCX", e);
		}
	}
	
	public String getArquivo() {
		return this.arquivo;
	}

	public void carregarModeloDocumento(String html) throws LoolException {
		byte[] docx = converterHtml(html);
		save(new ByteArrayInputStream(docx));
	}
	
	public void carregarModeloDocumento(byte[] odt) throws LoolException {
		save(new ByteArrayInputStream(odt));
	}
	
	public void carregarDocumentoImportacao(InputStream inputStream) throws LoolException {
		save(inputStream);
	}
	
	public String getHtmlContent() throws LoolException {
		InputStream isDoc = getContent();
		try {
			return IOUtils.toString( converterDocumento(new ByteArrayInputStream(IOUtils.toByteArray(isDoc)), extensao, "html") );
		} catch (IOException e) {
			throw new LoolException("Erro ao ler documento", e);
		}
	}

	public void apagarDocumento() throws LoolException {
		try {
			wopiClient.delete(pasta, arquivo, access);
		} catch (Exception e) {
			throw new LoolException("Erro ao tentar apagar arquivo do WOPI", e);
		}
		
	}

	public void gravar(ProcessoDocumentoBin processoDocumentoBin) throws LoolException {
		InputStream pdf = getPDFContent();
		try {
			int size = pdf.available();
			File arquivoBinario = processoDocumentoBin.getFile();
			if ( arquivoBinario==null ) {
				arquivoBinario = salvarPDFTemp(pdf);
				processoDocumentoBin.setFile(arquivoBinario);
			} else {
				IOUtils.copy(pdf, new FileOutputStream(arquivoBinario));
			}
			processoDocumentoBin.setSize(size);
		} catch (IOException e) {
			throw new LoolException("Erro ao ler documento", e);
		}
		mostrarPDF(processoDocumentoBin.getFile());
		
		processoDocumentoBin.setNomeDocumentoWopi(getArquivo());
		processoDocumentoBin.setExtensao("application/pdf");
		processoDocumentoBin.setBinario(true);
		processoDocumentoBin.setModeloDocumento(null);
		
	}


}
