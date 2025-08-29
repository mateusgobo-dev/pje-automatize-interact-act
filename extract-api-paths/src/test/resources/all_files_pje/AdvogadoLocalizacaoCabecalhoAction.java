package br.com.infox.editor.action;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.remoting.WebRemote;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.editor.bean.Estilo;
import br.com.infox.editor.dao.AdvogadoLocalizacaoCabecalhoDao;
import br.com.infox.editor.manager.CssDocumentoManager;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.AdvogadoLocalizacaoCabecalho;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.editor.Cabecalho;

@Name(AdvogadoLocalizacaoCabecalhoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class AdvogadoLocalizacaoCabecalhoAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -280265319736255856L;

	public static final String NAME = "advogadoLocalizacaoCabecalhoAction";

	private List<Estilo> estilos;
	private AdvogadoLocalizacaoCabecalho advogadoLocalizacaoCabecalho;
	private String base64;

	@In
	private CssDocumentoManager cssDocumentoManager;

	@In
	private transient AdvogadoLocalizacaoCabecalhoDao advogadoLocalizacaoCabecalhoDao;
	
	@WebRemote
	public List<Estilo> getEstilos() {
		if (estilos == null) {
			estilos = cssDocumentoManager.getEstilos();
		}
		return estilos;
	}

	public void setEstilos(List<Estilo> estilos) {
		this.estilos = estilos;
	}

	public AdvogadoLocalizacaoCabecalho getAdvogadoLocalizacaoCabecalho() {
		if(advogadoLocalizacaoCabecalho == null){
			Localizacao localizacaoAtual = Authenticator.getLocalizacaoAtual();
			AdvogadoLocalizacaoCabecalho localizacaoCabecalho = advogadoLocalizacaoCabecalhoDao.getAdvogadoLocalizacaoCabecalho(localizacaoAtual);
			
			if(localizacaoCabecalho == null){
				advogadoLocalizacaoCabecalho = new AdvogadoLocalizacaoCabecalho();
				advogadoLocalizacaoCabecalho.setCabecalho(new Cabecalho());
				advogadoLocalizacaoCabecalho.setAtivo(false);	
			}else{
				advogadoLocalizacaoCabecalho = localizacaoCabecalho;
			}
		}
		return advogadoLocalizacaoCabecalho;
	}

	public void setAdvogadoLocalizacaoCabecalho(AdvogadoLocalizacaoCabecalho advogadoLocalizacaoCabecalho) {
		this.advogadoLocalizacaoCabecalho = advogadoLocalizacaoCabecalho;
	}
	
	public void salvar(){
		advogadoLocalizacaoCabecalho.getCabecalho().setAtivo(advogadoLocalizacaoCabecalho.getAtivo());
		
		if(advogadoLocalizacaoCabecalho.getUsuarioCriacao() == null){
			advogadoLocalizacaoCabecalho.setUsuarioCriacao((PessoaFisica)Authenticator.getPessoaLogada());
		}
		if(advogadoLocalizacaoCabecalho.getDtCriacao() == null){
			advogadoLocalizacaoCabecalho.setDtCriacao(new Date());
		}
		if(advogadoLocalizacaoCabecalho.getLocalizacao() == null){
			advogadoLocalizacaoCabecalho.setLocalizacao(Authenticator.getLocalizacaoAtual());
		}
		if(advogadoLocalizacaoCabecalho.getCabecalho().getConteudo() == null){
			advogadoLocalizacaoCabecalho.getCabecalho().setConteudo("");
		}
		advogadoLocalizacaoCabecalho.getCabecalho().setCabecalho(Authenticator.getLocalizacaoAtual().getLocalizacao());
		
		if(EntityUtil.getEntityManager().contains(advogadoLocalizacaoCabecalho.getCabecalho())){
			EntityUtil.getEntityManager().merge(advogadoLocalizacaoCabecalho.getCabecalho());
		}else{
			EntityUtil.getEntityManager().persist(advogadoLocalizacaoCabecalho.getCabecalho());
		}
		
		if(EntityUtil.getEntityManager().contains(advogadoLocalizacaoCabecalho)){
			EntityUtil.getEntityManager().merge(advogadoLocalizacaoCabecalho);
		}else{
			EntityUtil.getEntityManager().persist(advogadoLocalizacaoCabecalho);
		}
		
		EntityUtil.getEntityManager().flush();
		
		FacesMessages.instance().add(Severity.INFO, "Cabeçalho salvo com sucesso!");
	}
	
	public void cancelar(){
		if(advogadoLocalizacaoCabecalho.getCabecalho().getIdCabecalho() != null){
			EntityUtil.getEntityManager().refresh(advogadoLocalizacaoCabecalho.getCabecalho());
		}else{
			advogadoLocalizacaoCabecalho.setCabecalho(new Cabecalho());
		}
	}

	public String getBase64() {
		return base64;
	}

	public void setBase64(String base64) {
		this.base64 = base64;
	}

}
