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
import br.jus.cnj.pje.nucleo.manager.PessoaNomeAlternativoManager;
import br.jus.pje.nucleo.entidades.PessoaNomeAlternativo;
import br.jus.pje.nucleo.enums.TipoNomeAlternativoEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;

@Name(NomeAlternativoAction.NAME)
@Scope(ScopeType.PAGE)
public class NomeAlternativoAction extends BaseAction<PessoaNomeAlternativo> implements Serializable{

	public static final String NAME = "nomeAlternativoAction";
	private static final long serialVersionUID = 1L;
	
	@In
	private PessoaNomeAlternativoManager pessoaNomeAlternativoManager;
	
	private EntityDataModel<PessoaNomeAlternativo> model;
	
	private PessoaNomeAlternativo pessoaNomeAlternativo;
	
	@Create
	public void init(){
		pesquisar();
		pessoaNomeAlternativo = new PessoaNomeAlternativo();
	}
	
	public void pesquisar(){
		List<Criteria> criterios = new ArrayList<Criteria>(0);
		criterios.addAll(getCriteriosTelaPesquisa());
		
		try {			
			model = new EntityDataModel<PessoaNomeAlternativo>(PessoaNomeAlternativo.class, super.facesContext, getRetriever());
			model.setCriterias(criterios);
			model.addOrder("o.idPessoaNomeAlternativo", Order.DESC);
		} catch (Exception e) {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "Ocorreu um erro ao executar a pesquisa: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void inserir() {
		if(this.pessoaNomeAlternativo.getPessoaNomeAlternativo() != null && 
				this.pessoaNomeAlternativo.getTipoNomeAlternativo() != null && 
					PreCadastroPessoaBean.instance().getPessoa() != null){
			try {
				this.pessoaNomeAlternativo.setPessoa(PreCadastroPessoaBean.instance().getPessoa());
				this.pessoaNomeAlternativoManager.persistAndFlush(this.pessoaNomeAlternativo);
			} catch (PJeBusinessException e) {
				e.printStackTrace();
			}
			
			this.pesquisar();
			this.pessoaNomeAlternativo = new PessoaNomeAlternativo();
		}
	}
	
	public void excluir(PessoaNomeAlternativo pessoaNomeAlternativo){
		try {		
			if(!pessoaNomeAlternativoManager.isNomeAlternativoEstaSendoUsado(pessoaNomeAlternativo)) {
				this.pessoaNomeAlternativoManager.remove(this.pessoaNomeAlternativoManager.findById(pessoaNomeAlternativo.getIdPessoaNomeAlternativo()));
				this.pessoaNomeAlternativoManager.flush();
				this.pesquisar();
			}			
			else {
				FacesMessages.instance().addFromResourceBundle(Severity.ERROR, PessoaNomeAlternativoManager.ERRO_NOME_USADO);
			}
			
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "Ocorreu um erro ao excluir o nome");
		}
	}
	
	public void editar(PessoaNomeAlternativo pessoaNomeAlternativo) {
		if(!pessoaNomeAlternativoManager.isNomeAlternativoEstaSendoUsado(pessoaNomeAlternativo)) {
			this.pessoaNomeAlternativo = pessoaNomeAlternativo;
		}
		else {
			FacesMessages.instance().addFromResourceBundle(Severity.FATAL, PessoaNomeAlternativoManager.ERRO_NOME_USADO);
		}
	}
	
	public void limpar(){
		pessoaNomeAlternativo = new PessoaNomeAlternativo();
	}
	
	private List<Criteria> getCriteriosTelaPesquisa(){
		List<Criteria> criterios = new ArrayList<Criteria>(0);
		
		Integer idPessoa = PreCadastroPessoaBean.instance().getPessoa().getIdPessoa();
		
		if(idPessoa != null) {
			criterios.add(Criteria.equals("pessoa.idPessoa", idPessoa));
		}
		return criterios; 
	}
	
	public List<TipoNomeAlternativoEnum> getTipos(){
		return Arrays.asList(TipoNomeAlternativoEnum.values());
	}
	
	@Override
	protected BaseManager<PessoaNomeAlternativo> getManager() {
		return this.pessoaNomeAlternativoManager;
	}

	@Override
	public EntityDataModel<PessoaNomeAlternativo> getModel() {
		return this.model;
	}
	
	public PessoaNomeAlternativo getPessoaNomeAlternativo() {
		return pessoaNomeAlternativo;
	}
	
	public void setPessoaNomeAlternativo(PessoaNomeAlternativo pessoaNomeAlternativo) {
		this.pessoaNomeAlternativo = pessoaNomeAlternativo;
	}
	
}
