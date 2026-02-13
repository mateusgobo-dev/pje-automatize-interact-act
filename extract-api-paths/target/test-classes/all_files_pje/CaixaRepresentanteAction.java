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
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.home.CaixaAdvogadoProcuradorHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.CaixaAdvogadoProcuradorManager;
import br.jus.cnj.pje.nucleo.manager.CaixaRepresentanteManager;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.CaixaRepresentante;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;

@Name("caixaRepresentanteAction")
@Scope(ScopeType.EVENT)
public class CaixaRepresentanteAction extends BaseAction<CaixaRepresentante>{


	private static final long serialVersionUID = 1L;
	
	@RequestParameter(value="caixaRepresentanteGridCount")
	private Integer caixaRepresentanteGridCount;
	
	@RequestParameter(value="representanteSelecionado")
	private Integer representanteSelecionado;
	
	@In
	private CaixaRepresentanteManager caixaRepresentanteManager;
	
	@In
	private CaixaAdvogadoProcuradorManager caixaAdvogadoProcuradorManager;
	
	@Logger
	private Log log;
	
	private EntityDataModel<CaixaRepresentante> model;
	
	CaixaAdvogadoProcurador caixaAdvogadoProcurador;

	@Create
	public void init(){
		try {
			caixaAdvogadoProcurador = (CaixaAdvogadoProcurador)caixaAdvogadoProcuradorManager.findById(CaixaAdvogadoProcuradorHome.instance().getId());
			pesquisar();
		} catch (PJeBusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			facesMessages.add(Severity.WARN, "Houve um erro ao recuperar a caixa. Por favor, tente novamente.");
		}
	}
	
	public void pesquisar(){
		
		try{
			model = new EntityDataModel<CaixaRepresentante>(CaixaRepresentante.class, super.facesContext, getRetriever());
			
			List<Criteria> criterios = new ArrayList<Criteria>(0);
			
			criterios.add(Criteria.equals("caixaAdvogadoProcurador", caixaAdvogadoProcurador));
			
			model.setCriterias(criterios);
			model.addOrder("o.representante.nome", Order.ASC);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void excluir(){
		try {
			if(representanteSelecionado != null){
				CaixaRepresentante cxRep = caixaRepresentanteManager.findById(representanteSelecionado);
				if(cxRep != null){
					caixaRepresentanteManager.remove(cxRep);
					caixaRepresentanteManager.flush();
					pesquisar();
					PessoasAssociadasProcuradoriaAction pessAssocProcAction = ComponentUtil.getComponent("pessoasAssociadasProcuradoriaAction");
					pessAssocProcAction.pesquisar();
				}
			}
		} catch (PJeBusinessException e) {
			log.error("Não foi possível excluir o representante desta caixa.");
			e.printStackTrace();
		} 		
	}
	
	@Override
	protected BaseManager<CaixaRepresentante> getManager() {
		return caixaRepresentanteManager;
	}

	@Override
	public EntityDataModel<CaixaRepresentante> getModel() {
		return model;
	}
	
	public CaixaAdvogadoProcurador getCaixaAdvogadoProcurador() {
		return caixaAdvogadoProcurador;
	}
	
	public void setCaixaAdvogadoProcurador(
			CaixaAdvogadoProcurador caixaAdvogadoProcurador) {
		this.caixaAdvogadoProcurador = caixaAdvogadoProcurador;
	}

}
