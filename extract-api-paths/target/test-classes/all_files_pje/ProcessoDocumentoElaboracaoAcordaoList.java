package br.com.infox.pje.list;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;

@Name(ProcessoDocumentoElaboracaoAcordaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class ProcessoDocumentoElaboracaoAcordaoList extends EntityList<ProcessoDocumento> {

	private static final long serialVersionUID = -1723399260103044825L;

	public static final String NAME = "processoDocumentoElaboracaoAcordaoList";

	private List<ProcessoDocumento> listaDocsSelecionados = new ArrayList<ProcessoDocumento>();
	
	private Boolean inicializado = Boolean.FALSE;
	
	private Date dataUltimoAcordao = null;
	
	private static final String DEFAULT_EJBQL = "select o from ProcessoDocumento o";
	private static final String DEFAULT_ORDER = "idProcessoDocumento desc";
	private static final String R1 = "o.processo.idProcesso = #{elaborarAcordaoAction.processoJudicial.idProcessoTrf} ";
	private static final String R2 =  " o.tipoProcessoDocumento.idTipoProcessoDocumento IN (#{elaborarAcordaoAction.listaTiposDocSelecaoProcesso})";
	private static final String R3 =  " o.dataInclusao >= (#{processoDocumentoElaboracaoAcordaoList.dataUltimoAcordao})";

	@Override
	protected void addSearchFields() {
		addSearchField("ativo", SearchCriteria.igual, R1);
		addSearchField("tipoProcessoDocumento", SearchCriteria.igual, R2);
		if(getDataUltimoAcordao() != null){
			addSearchField("dataInclusao", SearchCriteria.maior, R3);
		}
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	public List<ProcessoDocumento> getListaDocsSelecionados() {
		return this.listaDocsSelecionados;
	}

	public void setListaDocsSelecionados(List<ProcessoDocumento> listaDocsSelecionados) {
		this.listaDocsSelecionados = listaDocsSelecionados;
	}
	
	@SuppressWarnings("boxing")
	public void subirDocumento(Integer indice){
		Integer indDestino = indice - 1;
		if(indDestino >= 0){
			mudarDocPosicao(indice, indDestino);
		}
	}
	
	public void descerDocumento(Integer indice){
		Integer indDestino = indice + 1;
		if(indDestino < getListaDocsSelecionados().size()){
			mudarDocPosicao(indice, indDestino);
		}
	}
	
	private void mudarDocPosicao(Integer indOrigem, Integer indDestino){
		ProcessoDocumento docOrigem = getListaDocsSelecionados().get(indOrigem);
		ProcessoDocumento docToChange = getListaDocsSelecionados().get(indDestino);
		getListaDocsSelecionados().set(indDestino, docOrigem);
		getListaDocsSelecionados().set(indOrigem, docToChange);
	}
	
	public void selecionarDocumento(ProcessoDocumento processoDocumento){
		getListaDocsSelecionados().add(processoDocumento);
	}
	
	public void removerDocumento(ProcessoDocumento processoDocumento){
		getListaDocsSelecionados().remove(processoDocumento);
	}
	
	public void removerTodosDocumento(){
		getListaDocsSelecionados().clear();
	}

	public Boolean getInicializado() {
		return this.inicializado;
	}

	public void setInicializado(Boolean inicializado) {
		this.inicializado = inicializado;
	}

	public Date getDataUltimoAcordao() {
		return this.dataUltimoAcordao;
	}

	public void setDataUltimoAcordao(Date dataUltimoAcordao) {
		this.dataUltimoAcordao = dataUltimoAcordao;
	}

}
