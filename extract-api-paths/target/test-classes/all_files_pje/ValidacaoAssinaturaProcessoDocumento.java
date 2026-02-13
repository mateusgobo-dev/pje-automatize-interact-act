package br.com.infox.cliente.component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.persistence.Query;

import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.component.securitytoken.SecurityTokenControler;
import br.com.infox.cliente.home.ProcessoDocumentoBinHome;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;

@Name(ValidacaoAssinaturaProcessoDocumento.NAME)
@Scope(ScopeType.EVENT)
public class ValidacaoAssinaturaProcessoDocumento implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "validacaoAssinaturaProcessoDocumento";
	
	@In
	private ProcessoDocumentoManager processoDocumentoManager;

	private static final int TAMANHO_CODIGO_ID = 14;
	private static final String SF_MASK_DTCADASTRO = "yyMMddHHmmssSSS";
	private static final char BARRA = File.separatorChar;
	private static final String IdentificadorDoParametroGet = "?x=";
	
	private static final Integer TamanhoLadoQRCodeImg = 125;

	public static ValidacaoAssinaturaProcessoDocumento instance() {
		return ComponentUtil.getComponent(NAME);
	}
	
	public byte[] montarTextoFolhaDeRosto(ProcessoDocumentoBin pdbin, byte[] bin) throws Exception {
		return this.montarTextoFolhaDeRosto(pdbin, bin, null);
	}
	
	public byte[] montarTextoFolhaDeRosto(ProcessoDocumentoBin pdbin, byte[] bin, ProcessoDocumento pd) throws Exception {

		List<String> nomesAssinaturas = getNomesAssinaturasFormatado(pdbin);
		// Se o documento não tiver nenhuma assinatura, não vai mostrar link
		if (nomesAssinaturas == null || nomesAssinaturas.size() == 0) {
			return bin;
		}

		// Lendo o arquivo
		PdfReader pdfReader = new PdfReader(new ByteArrayInputStream(bin));
		
		// Deixa o pdf acessível, as vezes alguns não permite o acesso.
		Field f = pdfReader.getClass().getDeclaredField("encrypted");
		f.setAccessible(true);
		f.set(pdfReader, false);

		// Criação do objeto para escrita do documento
		ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();

		PdfStamper stamp = new PdfStamper(pdfReader, arrayOutputStream);
		stamp.insertPage(1, PageSize.A4);

		stamp.getOverContent(1);

		// Adiciona conteudo
		PdfContentByte cb = stamp.getOverContent(1);

		BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
		cb.beginText();

		// Inserindo a logo do PJe

		StringBuilder sb = new StringBuilder();
		sb.append(new Util().getContextRealPath());
		sb.append(BARRA).append("img");
		sb.append(BARRA).append("pjeMini.png");
		String imgPath = sb.toString();

		Image imagemBrasao = null;

		Float widthRosto = pdfReader.getPageSize(1).getWidth();
		Float heightRosto = pdfReader.getPageSize(1).getHeight();

		imagemBrasao = Image.getInstance(imgPath);

		cb.addImage(imagemBrasao, imagemBrasao.getWidth(), 0, 0, imagemBrasao.getHeight(), 25, heightRosto - 150);

		// Inserindo as informações ao lado do Brasão
		cb.setFontAndSize(bf, 14);
		cb.setTextMatrix(155, heightRosto - 95); // Centralizado Inferior
		cb.showText(Contexts.getApplicationContext().get(Parametros.NOME_SECAO_JUDICIARIA).toString());
		cb.setTextMatrix(155, heightRosto - 115); // Centralizado Inferior
		cb.showText(Contexts.getApplicationContext().get("subNomeSistema").toString());

		String nroProcesso = getNumeroProcesso(pdbin);

		SimpleDateFormat sd = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		String dataJuntada = sd.format(getDataJuntada(pdbin));

		cb.setFontAndSize(bf, 12);
		cb.setTextMatrix(PdfContentByte.ALIGN_LEFT + 30, heightRosto - 160);
		if (nroProcesso != null) {
			cb.showText("O documento a seguir foi juntado aos autos do processo de número " + nroProcesso);
		} else {
			cb.showText("O documento a seguir foi juntado a processo ainda NÃO PROTOCOLADO.");
		}
		cb.setTextMatrix(PdfContentByte.ALIGN_LEFT + 30, heightRosto - 175);
		
		if(pd != null){
			if(pd.getUsuarioJuntada() != null){
				cb.showText("em " + dataJuntada + " por " + pd.getUsuarioJuntada());				
			} else  if(pd.getUsuarioInclusao() != null){
				cb.showText("em " + dataJuntada + " por " + pd.getUsuarioInclusao());
			}
			
			if(nomesAssinaturas != null && nomesAssinaturas.size() > 0){
				cb.setTextMatrix(PdfContentByte.ALIGN_LEFT + 30, heightRosto - 190);
				cb.showText("Documento assinado por: ");
				for (int i = 0; i < nomesAssinaturas.size(); i++) {
					cb.setTextMatrix(PdfContentByte.ALIGN_LEFT + 40, heightRosto - (205 + (15 * (i + 1))));
					cb.showText("- " + nomesAssinaturas.get(i));
				}
			}
		}
			
		// Inserindo número do documento
		cb.setFontAndSize(bf, 12);
		cb.setTextMatrix(50, heightRosto - 480);

		BaseFont bfTimes = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1252", false);
		BaseFont bfTimesBold = BaseFont.createFont(BaseFont.TIMES_BOLD, "Cp1252", false);

		int fontSize = 11;
		int posMargem = 25;
		int posMargemTexto = posMargem + 30;
		int posLinha = 150;
		int posTexto1 = posLinha - 11;
		int posTexto2 = posTexto1 - 11;
		int posTexto3 = posTexto2 - 11;
		int posTexto4 = posTexto3 - 11;
		float largura = widthRosto;

		//Constroi imagem PNG de QRCode codificado com URL de validacao do documento
		Image barcode = com.lowagie.text.Image.getInstance(QRCode.from(geraUrlValidacaoDocumento(pdbin)).to(ImageType.PNG).withSize(TamanhoLadoQRCodeImg, TamanhoLadoQRCodeImg).stream().toByteArray());
		barcode.rotate();
		int widthCode = (int) (barcode.getWidth() * 0.9);
		int heightCode = (int) (barcode.getHeight() * 0.9);
		int posBarcode = (int) largura - posMargem - widthCode;
		cb.addImage(barcode, widthCode, 0, 0, heightCode, posBarcode, posLinha - 70);

		cb.setFontAndSize(bfTimes, fontSize);
		cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "Consulte este documento em:", posMargemTexto, posTexto1, 0);

		cb.showTextAligned(PdfContentByte.ALIGN_LEFT, getUrlValidacao(), posMargemTexto, posTexto2, 0);

		cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "usando o código:", posMargemTexto, posTexto3, 0);

		cb.setFontAndSize(bfTimesBold, fontSize);
		cb.showTextAligned(PdfContentByte.ALIGN_LEFT, getCodigoValidacaoDocumento(), 133, posTexto3, 0);
		
		// Inclusão do ID do docuemnto.
		String idProcessoDocumento= FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("idProcessoDocumento");
		if (idProcessoDocumento != null) {
			cb.setFontAndSize(bfTimes, fontSize);
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "ID do documento: ", posMargemTexto, posTexto4, 0);
			cb.setFontAndSize(bfTimesBold, fontSize);
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, idProcessoDocumento, 143, posTexto4, 0);
		}

		cb.endText();

		stamp.setFormFlattening(true);
		stamp.close();
		return arrayOutputStream.toByteArray();

	}

	public byte[] inserirInfoAssinaturasPDF(ProcessoDocumentoBin pdbin, byte[] bin) throws Exception {
		return this.inserirInfoAssinaturasPDF(pdbin, bin, null);
	}

	
	public byte[] inserirInfoAssinaturasPDF(ProcessoDocumentoBin pdbin, byte[] bin, ProcessoDocumento pd) throws Exception {
		try {
			return montarTextoFolhaDeRosto(pdbin, bin, pd);
		} catch (Exception e) {
			return bin;
		}
	}

	public String getUrlValidacao() {
		return new Util().getUrlProject() + "/Processo/ConsultaDocumento/listView.seam";
	}
	
	public String getUrlValidacaoExpediente() {
		return new Util().getUrlProject() + "/ConsultaPublica/DetalheProcessoConsultaPublica/expedienteSemLoginHTML.seam";
	}

	public String getCodigoValidacaoDocumento(ProcessoDocumentoBin pd) {
		if (pd == null) {
			return null;
		}
		SimpleDateFormat sd = new SimpleDateFormat(SF_MASK_DTCADASTRO);
		String idDoc = Integer.toString(pd.getIdProcessoDocumentoBin());
		idDoc = StringUtil.completaZeros(idDoc, TAMANHO_CODIGO_ID);
		int length = idDoc.length();
		idDoc = length > 14 ? idDoc.substring(length - TAMANHO_CODIGO_ID + 1, length - 1) : idDoc;
		return sd.format(pd.getDataInclusao()) + idDoc;
	}
	

	public String getCodigoValidacaoDocumento() {
		return getCodigoValidacaoDocumento(getProcessoDocumentoBin());
	}

	private ProcessoDocumentoBin getProcessoDocumentoBin() {
		return ProcessoDocumentoBinHome.instance().getInstance();
	}

	
	private long getIdProcessoDocumentoBin(String codigoValidacao) {
		codigoValidacao = codigoValidacao.trim();
		if(codigoValidacao.length() > TAMANHO_CODIGO_ID){
			String id = codigoValidacao.substring(TAMANHO_CODIGO_ID + 1);
			Long l = 0L;
			try{
				l = Long.parseLong(id); 
				
			}
			catch (NumberFormatException e){
				return 0;
			}
			return l;
		}
		else{
			return 0;
		}
	}
	
	private Date getDataCadastroDocumentoBin(String codigoValidacao) throws ParseException {
		if(codigoValidacao.length() > TAMANHO_CODIGO_ID){
			String data = codigoValidacao.substring(0, TAMANHO_CODIGO_ID + 1);
			SimpleDateFormat sd = new SimpleDateFormat(SF_MASK_DTCADASTRO);
			return sd.parse(data);
		}
		else{
			return null;
		}
	}
	
	public ProcessoDocumentoBin getProcessoDocumentoBin(String codigoValidacao) {
		long idProcessoDocumentoBin = getIdProcessoDocumentoBin(codigoValidacao);
		Date dataCadastroDocumentoBin = null;
		try {
			dataCadastroDocumentoBin = getDataCadastroDocumentoBin(codigoValidacao);
		} catch (ParseException e) {
			return null;
		}
		String hql = "select o from ProcessoDocumentoBin o " + "where o.idProcessoDocumentoBin = :id "
				+ "and o.dataInclusao = :data";
		Query query = EntityUtil.createQuery(hql);
		query.setParameter("id", (int) idProcessoDocumentoBin);
		query.setParameter("data", dataCadastroDocumentoBin);
		return EntityUtil.getSingleResult(query);
	}

	/**
	 * Recupera o identificador do primeiro {@link ProcessoDocumento} que está associado a um dado conteúdo
	 * 
	 * @param processoDocumentoBin {@link ProcessoDocumentoBin}
	 * @return O identificador do primeiro documento associado ao binário
	 */
	public Integer getIdProcessoDocumento(ProcessoDocumentoBin processoDocumentoBin) {
		ProcessoDocumento processoDocumento = processoDocumentoManager.getProcessoDocumentoByProcessoDocumentoBin(processoDocumentoBin);
		if (processoDocumento != null) {
			return processoDocumento.getIdProcessoDocumento();
		}
		return 0;
	}

	@SuppressWarnings("unchecked")
	public String getNomesAssinaturas(ProcessoDocumentoBin pd) {
		if (pd == null)
			return null;

		String hql = "select o from ProcessoDocumentoBinPessoaAssinatura o where o.processoDocumentoBin = :bin";
		Query query = EntityUtil.getEntityManager().createQuery(hql);
		query.setParameter("bin", pd);
		List<ProcessoDocumentoBinPessoaAssinatura> resultList = query.getResultList();
		StringBuilder sb = new StringBuilder();
		
		for (ProcessoDocumentoBinPessoaAssinatura assinatura : resultList) {
			// Não adicionar nomes de assinaturas inválidas
			if (!Strings.isEmpty(assinatura.getNomePessoa())) {
				if (sb.length() > 0) {
					sb.append(", ");
				}
				
				sb.append(assinatura.getNomePessoa());
				sb.append(" - ");
				sb.append(DateUtil.dateToString(assinatura.getDataAssinatura(), "dd/MM/yyyy HH:mm:ss"));
			}
		}
		return sb.length() > 0 ? sb.toString() : null;
	}

	/**
	 * Metodo responsavel por buscar o nome das pessoas e as datas das pessoas que
	 * assinaram os processos documentos passados por parametro.
	 * 
	 * @param processosDocumento
	 * @return
	 */
	public Map<Integer, String> getNomesAssinaturas(List<ProcessoDocumento> processosDocumento) {

		Map<Integer, String> mapa = new HashMap<>();

		String consulta = " select " + " 	distinct   " + " 		ass.id_processo_documento_bin, " + " 		( "
				+ " 			select " + "				array_to_string(" + "					array( "
				+ " 						select "
				+ "							ass_array.ds_nome_pessoa || ' - ' || to_char( ass_array.dt_assinatura, 'dd/MM/yyyy HH24:MI:ss')  "
				+ " 						from client.tb_proc_doc_bin_pess_assin as ass_array "
				+ " 						where "
				+ "							ass.id_processo_documento_bin = ass_array.id_processo_documento_bin "
				+ " 					), " + "				', ') as nomeAssinatura " + " 		) "
				+ " from client.tb_proc_doc_bin_pess_assin as ass "
				+ " inner join core.tb_processo_documento pd on pd.id_processo_documento_bin = ass.id_processo_documento_bin "
				+ " where " + "   pd.id_processo_documento in ( :documentos ) " + "  ; ";

		Query query = EntityUtil.getEntityManager().createNativeQuery(consulta);
		query.setParameter("documentos", processosDocumento);

		@SuppressWarnings("unchecked")
		List<Object[]> lista = query.getResultList();

		for (Object[] linha : lista) {
			mapa.put((Integer) linha[0], linha[1].toString());
		}

		return mapa;
	}

	@SuppressWarnings("unchecked")
	public List<String> getNomesAssinaturasFormatado(ProcessoDocumentoBin pd) {
		String hql = "select distinct o.nomePessoa from ProcessoDocumentoBinPessoaAssinatura o where o.processoDocumentoBin = :bin";
		// String hql =
		// "select distinct o.pessoa.nome from ProcessoDocumentoBinPessoaAssinatura o";
		Query query = EntityUtil.getEntityManager().createQuery(hql);
		query.setParameter("bin", pd);
		query.setMaxResults(10);
		List<String> resultList = query.getResultList();
		return resultList;

	}

	@SuppressWarnings("unchecked")
	public String getNumeroProcesso(ProcessoDocumentoBin pd) {
		String hql = "select o from ProcessoDocumento o where o.processoDocumentoBin = :bin";
		Query query = EntityUtil.getEntityManager().createQuery(hql);
		query.setParameter("bin", pd);
		query.setMaxResults(1);
		List<ProcessoDocumento> resultList = query.getResultList();
		return resultList.get(0).getProcesso().getNumeroProcesso();

	}
	
	public Date getDataJuntada(ProcessoDocumentoBin pd) {
        String hql = "select o.dataJuntada from ProcessoDocumento o where o.processoDocumentoBin = :bin";
        Query query = EntityUtil.createQuery(hql);
        query.setParameter("bin", pd);
        return EntityUtil.getSingleResult(query);
	}

	public String getNomesAssinaturas() {
		return getNomesAssinaturas(ProcessoDocumentoBinHome.instance().getInstance());
	}

	/**
	 * Fornece OutputStream com imagem em PNG de QRCode codificado com URL de validacao de um documento
	 * @param out
	 * @param data
	 * @throws IOException
	 */
	public void gerarPNGBarcode(OutputStream out, Object data) throws IOException {
		ProcessoDocumentoBin bin = getProcessoDocumentoBin();
		if (bin == null) {
			return;
		}
		QRCode.from(geraUrlValidacaoDocumento(bin)).to(ImageType.PNG).withSize(TamanhoLadoQRCodeImg, TamanhoLadoQRCodeImg).writeTo(out);
	}

	public String geraUrlValidacaoDocumento(ProcessoDocumentoBin pdb) {
		StringBuilder urlConsulta = new StringBuilder(getUrlValidacao().toString());
		urlConsulta.append(IdentificadorDoParametroGet);
		urlConsulta.append(getCodigoValidacaoDocumento(pdb));
	
		return urlConsulta.toString();
	}
	
	public String geraUrlValidacaoExpediente(ProcessoParteExpediente ppe) {
		return geraUrlValidacaoExpediente(ppe, null);
	}
	
	public String geraUrlValidacaoExpediente(ProcessoParteExpediente ppe, ProcessoDocumentoExpediente pde) {
		StringBuilder parametros = new StringBuilder();
		parametros.append(ppe.getIdProcessoParteExpediente()).append("_");
		parametros.append(StringUtil.removeNaoNumericos(ppe.getPessoaParte().getDocumentoCpfCnpj())).append("_");
		if (pde != null) {
			parametros.append(pde.getIdProcessoDocumentoExpediente()).append("_");
		}
		parametros.deleteCharAt(parametros.length()-1);
		
		StringBuilder url = new StringBuilder(getUrlValidacaoExpediente());
		url.append("?ca=");
		url.append(SecurityTokenControler.instance().gerarChaveAcessoGenerica(parametros.toString(), 30));
		
		return url.toString();
	}
	
	/**
	 * Gera a imagem QrCode para a url de consulta do processoDocumentoBin informado.
	 * @param pd
	 * @return
	 * @throws IOException
	 */
	public ByteArrayOutputStream geraQRCodeComValidacao(ProcessoDocumentoBin pd) throws IOException{
		StringBuilder urlConsulta = new StringBuilder();
		urlConsulta.append(getUrlValidacao());
		urlConsulta.append("?x=");
		urlConsulta.append(getCodigoValidacaoDocumento(pd));
		return(QRCode.from(urlConsulta.toString()).to(ImageType.PNG).withSize(TamanhoLadoQRCodeImg, TamanhoLadoQRCodeImg).stream());		
	}

}