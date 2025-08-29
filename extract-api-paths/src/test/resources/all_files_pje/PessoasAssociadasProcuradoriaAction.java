package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.home.CaixaAdvogadoProcuradorHome;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.CaixaAdvogadoProcuradorManager;
import br.jus.cnj.pje.nucleo.manager.CaixaRepresentanteManager;
import br.jus.cnj.pje.nucleo.manager.PessoaProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.ProcuradoriaManager;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.CaixaRepresentante;
import br.jus.pje.nucleo.entidades.PessoaProcuradoria;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;

@Name("pessoasAssociadasProcuradoriaAction")
@Scope(ScopeType.PAGE)
public class PessoasAssociadasProcuradoriaAction extends BaseAction<PessoaProcuradoria>{

	private static final long serialVersionUID = 1L;

	@In
	private PessoaProcuradoriaManager pessoaProcuradoriaManager;
	
	@In
	private CaixaRepresentanteManager caixaRepresentanteManager;
	
	@In
	private CaixaAdvogadoProcuradorManager caixaAdvogadoProcuradorManager;
	
	@In
	private ProcuradoriaManager procuradoriaManager;
	
	@Logger
	private Log log; 
	
	private EntityDataModel<PessoaProcuradoria> model;
	private Boolean selecionaTodos = Boolean.FALSE;
	private List<PessoaProcuradoria> listaSelecionados = new ArrayList<PessoaProcuradoria>(0);
	private CaixaAdvogadoProcurador caixaAdvogadoProcurador;
	
	@RequestParameter(value="pessoaProcuradoriaGridCount")
	private Integer pessoaProcuradoriaGridCount;	
	
	private Integer idCaixa;

	@Create
	public void init(){
		try {
			caixaAdvogadoProcurador = (CaixaAdvogadoProcurador)caixaAdvogadoProcuradorManager.findById(CaixaAdvogadoProcuradorHome.instance().getId());
		} catch (PJeBusinessException e) {
			log.error("Não foi possível recuperar a caixa.");
		}
		pesquisar();
	}
	
	public void pesquisar(){
		
		try{
			model = new EntityDataModel<PessoaProcuradoria>(PessoaProcuradoria.class, super.facesContext, getRetriever());
			
			List<Criteria> criterios = new ArrayList<Criteria>(0);
			Criteria crit = Criteria.notEquals("pessoaProcuradoriaJurisdicaoList.jurisdicao", caixaAdvogadoProcurador.getJurisdicao());
			crit.setRequired("pessoaProcuradoriaJurisdicaoList", false);
			criterios.add(Criteria.equals("procuradoria", procuradoriaManager.recuperaPorLocalizacao(Authenticator.getLocalizacaoAtual())));
			criterios.add(Criteria.equals("chefeProcuradoria", false));
			criterios.add(Criteria.or(crit,Criteria.isNull("pessoaProcuradoriaJurisdicaoList.jurisdicao")));
			criterios.add(Criteria.not(Criteria.in("pessoa", caixaRepresentanteManager.findAllRepresentantesByCaixa(caixaAdvogadoProcurador).toArray())));
			
			model.setDistinct(true);
			model.setCriterias(criterios);
			model.addOrder("o.pessoa", Order.ASC);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void selectAll(){
		if(selecionaTodos == Boolean.FALSE){
			listaSelecionados.clear();
		}else{
			listaSelecionados.addAll(model.page);
		}
	}
	
	public void selectOne(Integer idPessoaProcuradoria){
		PessoaProcuradoria pessoaProcuradoria;
		try {
			pessoaProcuradoria = pessoaProcuradoriaManager.findById(idPessoaProcuradoria);
			if(listaSelecionados.contains(pessoaProcuradoria)){
				listaSelecionados.remove(pessoaProcuradoria);
			}else{
				listaSelecionados.add(pessoaProcuradoria);
			}
		} catch (PJeBusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void vincularRepresentantes(CaixaAdvogadoProcurador caixa){
		for(PessoaProcuradoria pp : listaSelecionados){
			CaixaRepresentante caixaRepresentante = new CaixaRepresentante();
			caixaRepresentante.setRepresentante(pp.getPessoa().getPessoa());
			caixaRepresentante.setCaixaAdvogadoProcurador(caixa);
			try {
				caixaRepresentanteManager.persist(caixaRepresentante);
			} catch (PJeBusinessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			caixaRepresentanteManager.flush();
			listaSelecionados.clear();
			pesquisar();
		} catch (PJeBusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean verificaSelecionado(PessoaProcuradoria pessoaProcuradoria){
		if(listaSelecionados.contains(pessoaProcuradoria)){
			return true;
		}else {
			return false;
		}
			
	}
	
	public boolean podeVisualizarAba(){
		
		Papel papelAtual = Authenticator.getPapelAtual();
		if(papelAtual.getIdentificador().equals("pje:advogado") || papelAtual.getIdentificador().equals("advogado")){
			return false;
		} else {
			return true;
		}
	}
	
	@Override
	protected BaseManager<PessoaProcuradoria> getManager() {
		return pessoaProcuradoriaManager;
	}

	@Override
	public EntityDataModel<PessoaProcuradoria> getModel() {
		// TODO Auto-generated method stub
		return model;
	}

	public Boolean getSelecionaTodos() {
		return selecionaTodos;
	}
	
	public void setSelecionaTodos(Boolean selecionaTodos) {
		this.selecionaTodos = selecionaTodos;
	}
	
	public Integer getIdCaixa() {
		return idCaixa;
	}
	
	public void setIdCaixa(Integer idCaixa) {
		this.idCaixa = idCaixa;
	}
	
	public CaixaAdvogadoProcurador getCaixaAdvogadoProcurador() {
		return caixaAdvogadoProcurador;
	}
	
	public void setCaixaAdvogadoProcurador(
			CaixaAdvogadoProcurador caixaAdvogadoProcurador) {
		this.caixaAdvogadoProcurador = caixaAdvogadoProcurador;
	}
	
}
