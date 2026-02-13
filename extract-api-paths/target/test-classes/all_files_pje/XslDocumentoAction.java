package br.com.jt.pje.action;

import java.util.List;

import javax.persistence.Query;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringEscapeUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.editor.interpretadorDocumento.LinguagemFormalException;
import br.com.infox.editor.list.XslDocumentoList;
import br.com.infox.editor.service.ProcessaModeloService;
import br.com.infox.editor.service.XmlProcessoDocumentoEstruturadoService;
import br.com.infox.view.GenericCrudAction;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.editor.Cabecalho;
import br.jus.pje.nucleo.entidades.editor.EstruturaDocumento;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturado;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturadoTopico;
import br.jus.pje.nucleo.entidades.editor.XslDocumento;

@Name(XslDocumentoAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate 
public class XslDocumentoAction extends GenericCrudAction<XslDocumento>{

	private static final long serialVersionUID = -6648183257333329435L;
	
	public static final String NAME = "xslDocumentoAction";
	
	private XslDocumentoList xslDocumentoList;
	
	private String xslConvertidoHtml;

	/**
	 * Método passa um documento xsl do estado ativo para inativo.
	 * @param xslDocumento
	 */
	public void inativar(XslDocumento xslDocumento){
		List<EstruturaDocumento> lista = validaInativacao();
		if(lista.size() > 0){
			StringBuilder msg = new StringBuilder("Este XSL está sendo utilizado pela(s) estrutura(s) ");
			for(EstruturaDocumento e : lista){
				msg.append(e.getEstruturaDocumento());
				msg.append(", ");
			}
			msg.append("e não poderá ser excluído.");
			FacesMessages.instance().add(Severity.ERROR, msg.toString());
			return;
		}
		xslDocumento.setAtivo(false);
		super.update(xslDocumento);
		EntityUtil.getEntityManager().flush();
	}
	
	@SuppressWarnings("unchecked")
	private List<EstruturaDocumento> validaInativacao(){
		StringBuilder sb = new StringBuilder();
		sb.append("select o from EstruturaDocumento o ");
		sb.append("where o.ativo = true ");
		sb.append("and o.xslDocumento.idXslDocumento = :idXslDocumento");
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter("idXslDocumento", getInstance().getIdXslDocumento());
		return q.getResultList();
	}
	
	private void persistOuUpdate(char persistOuUpdate){
		if(!visualizar()){
			return;
		}
		if(getInstance().getIdXslDocumento() != null && !getInstance().getAtivo()){
			List<EstruturaDocumento> lista = validaInativacao();
			if(lista.size() > 0){
				StringBuilder msg = new StringBuilder("Este XSL está sendo utilizado pela(s) estrutura(s) ");
				for(EstruturaDocumento e : lista){
					msg.append(e.getEstruturaDocumento());
					msg.append(", ");
				}
				msg.append("e não poderá ser excluído.");
				FacesMessages.instance().add(Severity.ERROR, msg.toString());
				return;
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from XslDocumento o ");
		sb.append("where o.idXslDocumento <> :idXslDocumento ");
		sb.append("and o.nome = :nome ");
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		if(getInstance().getIdXslDocumento() != null){
			q.setParameter("idXslDocumento", getInstance().getIdXslDocumento());
		}else{
			q.setParameter("idXslDocumento", 0);
		}
		q.setParameter("nome", getInstance().getNome());
		Long result = (Long) q.getSingleResult();
		if(result.longValue() > 0){
			String msg = "Já existe XSL cadastro com este nome.";
			FacesMessages.instance().add(Severity.ERROR, msg);
		}else{
			if(persistOuUpdate == 'p'){
				super.persist(getInstance());
			}else if(persistOuUpdate == 'u'){
				super.update(getInstance());
			}
			EntityUtil.getEntityManager().flush();
		}
	}
	
	public void persist(){
		persistOuUpdate('p');
	}
	
	public void update(){
		persistOuUpdate('u');
	}
	
	@Override
	public void newInstance(){
		super.newInstance();
	}
	
	/**
	 * [PJEII-1141] - Método responsável por realizar a restauração do xsl padrão.
	 */
	public void restaurarPadrao() {
		this.getInstance().setConteudo(xslPadrao());
	}
	
	/**
	 * [PJEII-1141] - Método responsável por retornar o xsl padrão.
	 * @return String
	 */
	private String xslPadrao() {
		StringBuilder xsl = new StringBuilder("");
		xsl.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
		xsl.append("\n");
		xsl.append("<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">");
		xsl.append("\n");
		xsl.append("<xsl:template match=\"/\">");
		xsl.append("\n");
		xsl.append("	<xsl:value-of select=\"documento/cabecalho\" />");
		xsl.append("\n");
		xsl.append("	<xsl:for-each select=\"documento/topico\">");
		xsl.append("\n");
		xsl.append("		<xsl:if test=\"habilitado = 'true'\">");
		xsl.append("\n");
		xsl.append("			<div style=\"float: right; width: 20%\">");
		xsl.append("\n");
		xsl.append("				<xsl:value-of select=\"atributos\" />");
		xsl.append("\n");
		xsl.append("			</div>");
		xsl.append("\n");
		xsl.append("			<xsl:value-of select=\"abrirDivTitulo\"/>");
		xsl.append("\n");
		xsl.append("			<div style=\"margin-top: 0px; margin-bottom: 0px; margin-right: 0%; font-family: Arial; font-weight: bold; font-size: 14pt\">");
		xsl.append("\n");
		xsl.append("				<xsl:if test=\"numeracao != ''\">");
		xsl.append("\n");
		xsl.append("					<div style=\"font-size: 14pt; display: inline; \" class=\"divNumeracao\">");
		xsl.append("\n");
		xsl.append("						<span style=\"display: inline;\" class=\"numeracao\"><xsl:value-of select=\"numeracao\" /></span>");
		xsl.append("\n");
		xsl.append("					</div>");
		xsl.append("\n");
		xsl.append("					<div style=\"margin-left: 1px; display: inline;\">");
		xsl.append("\n");
		xsl.append("						<xsl:value-of select=\"titulo\" />");
		xsl.append("\n");
		xsl.append("					</div>");
		xsl.append("\n");
		xsl.append("				</xsl:if>");
		xsl.append("\n");
		xsl.append("				<xsl:if test=\"numeracao = ''\">");
		xsl.append("\n");
		xsl.append("					<div>");
		xsl.append("\n");
		xsl.append("						<xsl:value-of select=\"titulo\" />");
		xsl.append("\n");
		xsl.append("					</div>");
		xsl.append("\n");
		xsl.append("				</xsl:if>");
		xsl.append("\n");
		xsl.append("			</div>");
		xsl.append("\n");
		xsl.append("			<xsl:value-of select=\"fecharDivTitulo\"/>");
		xsl.append("\n");
		xsl.append("			<p style=\"clear: both\">");
		xsl.append("\n");
		xsl.append("				<xsl:value-of select=\"conteudo\" />");
		xsl.append("\n");
		xsl.append("			</p>");
		xsl.append("\n");
		xsl.append("		</xsl:if>");
		xsl.append("\n");
		
		/*
		 * [PJEII-5168] PJE-JT: Sérgio Ricardo : PJE-1.4.7 
		 * Adição de novas linhas ao XSL padrão 
		 */		
		xsl.append("        <xsl:for-each select=\"current()/anotacao\">");
		xsl.append("\n");
		xsl.append("           <div style=\"font-family: Arial; background-color: #FFFF99; border-width: 1px; margin-top: 5px; border-style: solid;\">");
		xsl.append("\n");
		xsl.append("               <div style=\"padding-bottom: 5px; border-bottom: 1px solid black; font-size: 0.7em; font-weight: bold;\">");
		xsl.append("\n");
		xsl.append("                   <span><xsl:value-of select=\"titulo\" /></span>");
		xsl.append("\n");
		xsl.append("               </div>");
		xsl.append("\n");           
		xsl.append("               <div>");
		xsl.append("\n");
		xsl.append("                   <xsl:value-of select=\"conteudo\" />");
		xsl.append("\n");
		xsl.append("               </div>");
		xsl.append("\n");
		xsl.append("               <div style=\"font-size: 0.7em; font-weight: bold; margin-bottom: 5px;\">");
		xsl.append("\n");
		xsl.append("                   <span><xsl:value-of select=\"textoRodape\" /></span>");
		xsl.append("\n");
		xsl.append("               </div>");
		xsl.append("\n");
		xsl.append("		   </div>");
		xsl.append("\n");
		xsl.append("		</xsl:for-each>");
		xsl.append("\n");				
		xsl.append("	</xsl:for-each>");
		xsl.append("\n");    		  
		xsl.append("    		    <xsl:for-each select=\"documento/anotacao\">");
		xsl.append("\n");
		xsl.append("                <div style=\"font-family: Arial; background-color: #FFFF99; border-width: 1px; margin-top: 5px; border-style: solid;\">");
		xsl.append("\n");
		xsl.append("                    <div style=\"padding-bottom: 5px; border-bottom: 1px solid black; font-size: 0.7em; font-weight: bold;\">");
		xsl.append("\n");
		xsl.append("                        <span><xsl:value-of select=\"titulo\" /></span>");
		xsl.append("\n");
		xsl.append("                    </div>");
		xsl.append("\n");
		xsl.append("                    <div>");
		xsl.append("\n");
		xsl.append("                        <xsl:value-of select=\"conteudo\" />");
		xsl.append("\n");
		xsl.append("                    </div>");
		xsl.append("\n");                    
		xsl.append("                    <div style=\"font-size: 0.7em; font-weight: bold; margin-bottom: 5px;\">");
		xsl.append("\n");
		xsl.append("                <span><xsl:value-of select=\"textoRodape\" /></span>");
		xsl.append("\n");
		xsl.append("                    </div>");
		xsl.append("\n");
		xsl.append("                </div>");
		xsl.append("\n");
		xsl.append("        </xsl:for-each>");
		/*
		 * [PJEII-5168] PJE-JT: Sérgio Ricardo : PJE-1.4.7 
		 * FIM ALTERAÇÃO 
		 */		

		xsl.append("\n");
		xsl.append("</xsl:template>");
		xsl.append("\n");
		xsl.append("</xsl:stylesheet>");
		return xsl.toString();
	}

	public boolean visualizar(){
		XmlProcessoDocumentoEstruturadoService xmlProcessoDocumentoEstruturadoService = ComponentUtil.getComponent("xmlProcessoDocumentoEstruturadoService");
		ProcessaModeloService processaModeloService = ComponentUtil.getComponent("processaModeloService");
		
		ProcessoDocumentoEstruturado processoDocumentoEstruturado = new ProcessoDocumentoEstruturado();
		EstruturaDocumento estruturaDocumento = new EstruturaDocumento();
		Cabecalho cabecalho = new Cabecalho();
		cabecalho.setConteudo("Cabeçalho");
		estruturaDocumento.setCabecalho(cabecalho);
		processoDocumentoEstruturado.setEstruturaDocumento(estruturaDocumento);
//		for (int i = 0; i<=3; i++) {
		ProcessoDocumentoEstruturadoTopico documentoEstruturadoTopico1 = new ProcessoDocumentoEstruturadoTopico();
		documentoEstruturadoTopico1.setConteudo("Conteúdo tópico "+String.valueOf(1));
		documentoEstruturadoTopico1.setTitulo("Tópico "+String.valueOf(1));
		documentoEstruturadoTopico1.setNumerado(true);
		documentoEstruturadoTopico1.setNumeracao(1);
		documentoEstruturadoTopico1.setNivel(1);
		processoDocumentoEstruturado.getProcessoDocumentoEstruturadoTopicoList().add(documentoEstruturadoTopico1);
		
		ProcessoDocumentoEstruturadoTopico documentoEstruturadoTopico2 = new ProcessoDocumentoEstruturadoTopico();
		documentoEstruturadoTopico2.setConteudo("Conteúdo tópico "+String.valueOf(2));
		documentoEstruturadoTopico2.setTitulo("Tópico "+String.valueOf(2));
		documentoEstruturadoTopico2.setNumerado(true);
		documentoEstruturadoTopico2.setNumeracao(2);
		documentoEstruturadoTopico2.setNivel(1);
		processoDocumentoEstruturado.getProcessoDocumentoEstruturadoTopicoList().add(documentoEstruturadoTopico2);
		
		ProcessoDocumentoEstruturadoTopico documentoEstruturadoTopico3 = new ProcessoDocumentoEstruturadoTopico();
		documentoEstruturadoTopico3.setConteudo("Conteúdo tópico "+String.valueOf(3));
		documentoEstruturadoTopico3.setTitulo("Tópico "+String.valueOf(3));
		documentoEstruturadoTopico3.setNivel(1);
		processoDocumentoEstruturado.getProcessoDocumentoEstruturadoTopicoList().add(documentoEstruturadoTopico3);
		
		ProcessoDocumentoEstruturadoTopico documentoEstruturadoTopico4 = new ProcessoDocumentoEstruturadoTopico();
		documentoEstruturadoTopico4.setConteudo("<i>Assinatura</i>");
		documentoEstruturadoTopico4.setTitulo("");
		documentoEstruturadoTopico4.setExibirTitulo(false);
		documentoEstruturadoTopico4.setNivel(1);
		processoDocumentoEstruturado.getProcessoDocumentoEstruturadoTopicoList().add(documentoEstruturadoTopico4);
		
//		}
		
		String msg = "Xsl inválido";
		
		try {
			xslConvertidoHtml = processaModeloService.processaModelo(xmlProcessoDocumentoEstruturadoService.criarXmlDocumento(processoDocumentoEstruturado), getInstance().getConteudo());
			xslConvertidoHtml = StringEscapeUtils.unescapeXml(xslConvertidoHtml);
			xslConvertidoHtml = StringEscapeUtils.unescapeHtml(xslConvertidoHtml);
			xslConvertidoHtml = xslConvertidoHtml.replaceAll("&apos;", "'");
			return true;
		} catch (LinguagemFormalException e) {
			FacesMessages.instance().add(Severity.ERROR, msg);
			return false;
		} catch (TransformerException e) {
			FacesMessages.instance().add(Severity.ERROR, msg);
			return false;
		} catch (ParserConfigurationException e) {
			FacesMessages.instance().add(Severity.ERROR, msg);
			return false;
		}
	}
	
	public XslDocumentoList getXslDocumentoList() {
		if(xslDocumentoList == null){
			xslDocumentoList = ComponentUtil.getComponent("xslDocumentoList");
		}
		return xslDocumentoList;
	}

	public void setXslDocumentoList(XslDocumentoList xslDocumentoList) {
		this.xslDocumentoList = xslDocumentoList;
	}

	public String getXslConvertidoHtml() {
		return xslConvertidoHtml;
	}

	public void setXslConvertidoHtml(String xslConvertidoHtml) {
		this.xslConvertidoHtml = xslConvertidoHtml;
	}

}
