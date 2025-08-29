/**
 * 
 */
package br.jus.cnj.pje.view;

import java.util.Collections;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.international.StatusMessage.Severity;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.TipoSituacaoProcessualManager;
import br.jus.pje.nucleo.entidades.TipoSituacaoProcessual;

/**
 * Componente de controle da tela de manipulação da entidade {@link TipoSituacaoProcessual}.
 * 
 * @author cristof
 *
 */
@Name("tipoSituacaoProcessualAction")
@Scope(ScopeType.EVENT)
public class TipoSituacaoProcessualAction extends BaseAction<TipoSituacaoProcessual> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5965413251240848175L;
	
	@RequestParameter(value="idtsp")
	private Long idtsp_;
	
	@RequestParameter(value="edit_")
	private Boolean edit_;
	
	@RequestParameter(value="incompat")
	private Long idincompat_;

	@In(value="tipoSituacaoProcessualManager")
	private TipoSituacaoProcessualManager manager;
	
	private EntityDataModel<TipoSituacaoProcessual> model;
	
	private TipoSituacaoProcessual tipo;
	
	private TipoSituacaoProcessual tipoIncompativel;
	
	private List<TipoSituacaoProcessual> tipos;
	
	private Long idTipoSituacaoProcessual;
	
	private boolean edit;
	
	private List<TipoSituacaoProcessual> tipoSituacaoProcessualList;
 	
	private TipoSituacaoProcessual tipoSituacaoProcessual = new TipoSituacaoProcessual();
	
	@Create
	public  void init() {
		model = new EntityDataModel<TipoSituacaoProcessual>(TipoSituacaoProcessual.class, facesContext, getRetriever());
		idTipoSituacaoProcessual = (idtsp_ != null && idtsp_ > 0) ? idtsp_ : null;
		if(idTipoSituacaoProcessual != null){
			carregarTipo(idTipoSituacaoProcessual);
		}
		edit = edit_ != null ? edit_ : false;
		pesquisaSituacaoProcessualAll();
	}
	
	/**
 	 * Pesquisa todas as situações processuais
 	 */
 	public void pesquisaSituacaoProcessualAll(){
 		tipoSituacaoProcessualList =  manager.pesquisaSituacaoProcessualAll(tipoSituacaoProcessual);
 		setTipoSituacaoProcessualList(tipoSituacaoProcessualList);
 	}
 
 	/**
 	 * Pesquisa situação processual
 	 * @param tipoSituacaoProcessual
 	 * @return
 	 * @throws Exception
 	 */
 	public List<TipoSituacaoProcessual> pesquisarSituacaoProcessual(TipoSituacaoProcessual tipoSituacaoProcessual) throws Exception{
 
 		tipoSituacaoProcessualList =  manager.pesquisaSituacaoProcessual(tipoSituacaoProcessual);
 
 		return tipoSituacaoProcessualList;
 
 	}
 	
 	public void limparSituacaoProcessual(TipoSituacaoProcessual tipoSituacaoProcessual){
 		tipoSituacaoProcessual.setCodigo(null);
 		tipoSituacaoProcessual.setNome(null);
 		tipoSituacaoProcessual.setDescricao(null);
 		setTipoSituacaoProcessual(tipoSituacaoProcessual);	
 		pesquisaSituacaoProcessualAll();
}
 	
	public List<TipoSituacaoProcessual> getTipos(){
		if(tipos == null){
			try {
				tipos = manager.findAll();
				tipos.remove(getTipo());
				tipos.removeAll(getTipo().getTiposSituacoesIncompatives());
			} catch (PJeBusinessException e) {
				tipos = Collections.emptyList();
			}
		}
		return tipos;
	}
	
	public void adicionarIncompativel(){
		if(tipoIncompativel == null){
			facesMessages.add(Severity.WARN, "Não foi selecionado o tipo a ser incluído como incompatível");
			return;
		}else{
			if(tipo.getTiposSituacoesIncompatives().add(tipoIncompativel)){
				try {
					manager.flush();
					facesMessages.add(Severity.INFO, "Tipo incluído como incompatível.");
				} catch (PJeBusinessException e) {
					facesMessages.add(Severity.ERROR, "Houve um erro ao tentar acrescentar.");
				}
			}else{
				facesMessages.add(Severity.INFO, "O tipo já constava na lista de incompatíveis.");
			}
		}
	}
	
	public void adicionarIncompativel(String codigo){
		if(codigo == null || codigo.isEmpty()){
			facesMessages.add(Severity.WARN, "Não foi indicado o código do tipo de situação a ser incluído como incompatível");
			return;
		}else{
			TipoSituacaoProcessual t = manager.findByCodigo(codigo);
			if(t == null){
				facesMessages.add(Severity.ERROR, "O tipo de situação com o código {0} não existe.", codigo);
			}else if(tipo.getTiposSituacoesIncompatives().add(tipoIncompativel)){
				facesMessages.add(Severity.INFO, "Tipo incluído como incompatível.");
			}else{
				facesMessages.add(Severity.INFO, "O tipo já constava na lista de incompatíveis.");
			}
		}
	}
	
	public void removerIncompativel(){
		if(idincompat_ == null){
			facesMessages.add(Severity.WARN, "Não foi selecionado o tipo a ser excluído da lista de incompatíveis.");
			return;
		}else{
			try {
				TipoSituacaoProcessual t = manager.findById(idincompat_);
				if(t != null){
					if(getTipo().getTiposSituacoesIncompatives().remove(t)){
						manager.flush();
						facesMessages.add(Severity.INFO, "Tipo excluído da lista de incompatíveis.");
					}else{
						facesMessages.add(Severity.INFO, "O tipo já não constava na lista de incompatíveis.");
					}
				}
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, "Houve um erro ao tentar remover.");
			}
		}
	}
	
	public void gravar(){
		if(tipo != null){
			try{
				manager.merge(tipo);
				manager.flush();
				facesMessages.add(Severity.INFO, "Tipo gravado com sucesso.");
			}catch(Throwable t){
				facesMessages.add(Severity.ERROR, "Erro ao tentar gravar o tipo: {0}.", t.getLocalizedMessage());
			}
		}
	}
	
	public void inativar(){
		if(getTipo() != null){
			try {
				tipo.setAtivo(false);
//				manager.merge(tipo);
				manager.flush();
				facesMessages.add(Severity.INFO, "Tipo inativado com sucesso.");
			} catch (Throwable t) {
				facesMessages.add(Severity.ERROR, "Erro ao tentar gravar o tipo: {0}.", t.getLocalizedMessage());
			}
		}
	}

	@Override
	protected TipoSituacaoProcessualManager getManager() {
		return manager;
	}

	@Override
	public EntityDataModel<TipoSituacaoProcessual> getModel() {
		return model;
	}
	
	public TipoSituacaoProcessual getTipo() {
		if(tipo == null){
			if(idtsp_ != null){
				carregarTipo(idtsp_);
			}else{
				tipo = manager.create();
			}
		}
		return tipo;
	}
	
	public void carregarTipo(Long id){
		try {
			tipo = manager.findById(id);
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Erro ao tentar preparar a situação para edição.");
		}
	}
	
	public TipoSituacaoProcessual getTipoIncompativel() {
		return tipoIncompativel;
	}
	
	public void setTipoIncompativel(TipoSituacaoProcessual tipoIncompativel) {
		this.tipoIncompativel = tipoIncompativel;
	}
	
	public Long getIdTipoSituacaoProcessual() {
		return idTipoSituacaoProcessual;
	}
	
	public boolean isEdit() {
		return edit;
	}

	public List<TipoSituacaoProcessual> getTipoSituacaoProcessualList() {
 		return tipoSituacaoProcessualList;
 	}
 
 	public void setTipoSituacaoProcessualList(List<TipoSituacaoProcessual> tipoSituacaoProcessualList) {
 		this.tipoSituacaoProcessualList = tipoSituacaoProcessualList;
 	}
 
 	public TipoSituacaoProcessual getTipoSituacaoProcessual() {
 		return tipoSituacaoProcessual;
 	}
 
 	public void setTipoSituacaoProcessual(TipoSituacaoProcessual tipoSituacaoProcessual) {
 		this.tipoSituacaoProcessual = tipoSituacaoProcessual;
}
}
