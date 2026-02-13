package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.bean.PreCadastroPessoaBean;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.PessoaFiliacaoManager;
import br.jus.pje.nucleo.entidades.PessoaFiliacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.enums.TipoFiliacaoEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;

@Name(OutrasFiliacoesAction.NAME)
@Scope(ScopeType.PAGE)
public class OutrasFiliacoesAction extends BaseAction<PessoaFiliacao> implements Serializable{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "outrasFiliacoesAction";

	@In
	private PessoaFiliacaoManager pessoaFiliacaoManager;
	
	private EntityDataModel<PessoaFiliacao> model;
	
	private PessoaFiliacao pessoaFiliacao;
	
	@Create
	public void init(){
		this.pessoaFiliacao = new PessoaFiliacao();
		this.pesquisar();
	}
	
	public void pesquisar(){
		List<Criteria> criterios = new ArrayList<Criteria>(0);
		criterios.addAll(getCriteriosTelaPesquisa());
		
		try {			
			model = new EntityDataModel<PessoaFiliacao>(PessoaFiliacao.class, super.facesContext, getRetriever());
			model.setCriterias(criterios);
			model.addOrder("o.id", Order.DESC);
		} catch (Exception e) {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "Ocorreu um erro ao executar a pesquisa: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private List<Criteria> getCriteriosTelaPesquisa(){
		List<Criteria> criterios = new ArrayList<Criteria>(0);
		
		PessoaFisica pessoaFisica = PreCadastroPessoaBean.instance().getPessoaFisica();
		
		if(pessoaFisica != null) {
			criterios.add(Criteria.equals("pessoaFisica", pessoaFisica));
		}
		return criterios; 
	}
	
	public void inserir(){
		if(this.pessoaFiliacao.getFiliacao() != null && 
				this.pessoaFiliacao.getTipoFiliacao() != null &&
					PreCadastroPessoaBean.instance().getPessoa() != null){
			try {
				this.pessoaFiliacao.setPessoaFisica(PreCadastroPessoaBean.instance().getPessoaFisica());
				this.pessoaFiliacaoManager.persistAndFlush(this.pessoaFiliacao);
			} catch (PJeBusinessException e) {
				e.printStackTrace();
			}
			
			this.init();
		}
	}
	
	public void excluir(PessoaFiliacao pessoaFiliacao){
		try {
			this.pessoaFiliacaoManager.remove(pessoaFiliacao);
			this.pessoaFiliacaoManager.flush();
			this.init();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
	}
	
	public void editar(PessoaFiliacao pessoaFiliacao){
		this.pessoaFiliacao = pessoaFiliacao;
	}
	
	public void limpar(){
		this.pessoaFiliacao = new PessoaFiliacao();
	}
	
	public List<TipoFiliacaoEnum> getTipos(){
		return Arrays.asList(TipoFiliacaoEnum.values());
	}	
	
	@Override
	protected BaseManager<PessoaFiliacao> getManager() {
		return this.pessoaFiliacaoManager;
	}

	@Override
	public EntityDataModel<PessoaFiliacao> getModel() {
		return this.model;
	}
	
	public PessoaFiliacao getPessoaFiliacao() {
		return pessoaFiliacao;
	}
	
	public void setPessoaFiliacao(PessoaFiliacao pessoaFiliacao) {
		this.pessoaFiliacao = pessoaFiliacao;
	}
}
