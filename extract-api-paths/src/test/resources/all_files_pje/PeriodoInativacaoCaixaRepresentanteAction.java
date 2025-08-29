package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.international.StatusMessage.Severity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.infox.cliente.home.CaixaAdvogadoProcuradorHome;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.CaixaAdvogadoProcuradorManager;
import br.jus.cnj.pje.nucleo.manager.PeriodoInativacaoCaixaRepresentanteManager;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.PeriodoInativacaoCaixaRepresentante;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;

@Name(PeriodoInativacaoCaixaRepresentanteAction.NAME)
@Scope(ScopeType.PAGE)
public class PeriodoInativacaoCaixaRepresentanteAction extends BaseAction<PeriodoInativacaoCaixaRepresentante>{
	
	private static final long serialVersionUID = 1L;
	public static final String NAME = "periodoInativacaoCaixaRepresentanteAction";
	
	private EntityDataModel<PeriodoInativacaoCaixaRepresentante> model;
	
	@In
	private PeriodoInativacaoCaixaRepresentanteManager periodoInativacaoCaixaRepresentanteManager;
	
	@In
	private CaixaAdvogadoProcuradorManager caixaAdvogadoProcuradorManager;
	
	private CaixaAdvogadoProcurador caixaAdvogadoProcurador;
	
	@RequestParameter(value="periodoInativacaoGridCount")
	private Integer procuradorGridCount;
	
	private Date dtInicialInativa;
	private Date dtFinalInativa;

	private static final Logger logger = LoggerFactory.getLogger(PeriodoInativacaoCaixaRepresentanteAction.class);

	@Create
	public void init(){
		
		try {
			caixaAdvogadoProcurador = (CaixaAdvogadoProcurador)caixaAdvogadoProcuradorManager.findById(CaixaAdvogadoProcuradorHome.instance().getId());
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR,"Não foi possível recuperar a caixa. Por favor, tente novamente");
			e.printStackTrace();
		}
		
		pesquisar();
	}
	
	public void pesquisar(){
		
		try{
			model = new EntityDataModel<PeriodoInativacaoCaixaRepresentante>(PeriodoInativacaoCaixaRepresentante.class, super.facesContext, getRetriever());
			
			List<Criteria> criterios = new ArrayList<Criteria>(0);
			
			criterios.add(Criteria.equals("caixaAdvogadoProcurador", caixaAdvogadoProcurador));
			
			model.setCriterias(criterios);
			model.addOrder("o.dataInicio", Order.ASC);
			
		} catch (Exception e) {
			facesMessages.add(Severity.ERROR,"Ocorreu um erro ao recuperar a lista de inatividade da caixa. Por favor, tente novamente");
			e.printStackTrace();
		}
		
	}	

	public void incluir(CaixaAdvogadoProcurador caixa){
		if(dtInicialInativa == null){
			facesMessages.add(Severity.WARN, "É necessário informar a data inicial.");
			logger.debug("Data inicial não informada.");
		} else if(DateUtil.isDataMenor(dtInicialInativa, new Date())) {
			facesMessages.add(Severity.WARN, "Data inicial tem que ser igual ou superior a data atual.");
			logger.debug("Data inicial tem que ser igual ou superior a data atual.");
		} else if(dtFinalInativa != null && DateUtil.isDataMaior(dtInicialInativa, dtFinalInativa)){
			facesMessages.add(Severity.WARN, "A data inicial não pode ser superior a data final.");
			logger.debug("A data inicial não pode ser superior a data final.");
		} else {

			PeriodoInativacaoCaixaRepresentante picr = new PeriodoInativacaoCaixaRepresentante();
			
			if (dtFinalInativa != null) {
				picr.setDataFim(DateUtil.getEndOfDay(dtFinalInativa));
			}	
			
			picr.setDataInicio(dtInicialInativa);
			picr.setCaixaAdvogadoProcurador(caixa);
			
			try {
				periodoInativacaoCaixaRepresentanteManager.persistAndFlush(picr);
				dtInicialInativa = null;
				dtFinalInativa = null;
				pesquisar();
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, "Ocorreu um erro ao tentar gravar o período de inativação.");
			}
		}
	}
	
	public void inativa(PeriodoInativacaoCaixaRepresentante picr){
		try {
			periodoInativacaoCaixaRepresentanteManager.remove(picr);
			periodoInativacaoCaixaRepresentanteManager.flush();
			pesquisar();
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR,"Ocorreu um erro ao tentar excluir o período de inativação. Por favor, tente novamente");
			logger.error("Ocorreu um erro ao tentar excluir o período de inativação");
			e.printStackTrace();
		}
	}
	
	@Override
	protected BaseManager<PeriodoInativacaoCaixaRepresentante> getManager() {
		return periodoInativacaoCaixaRepresentanteManager;
	}
	
	@Override
	public EntityDataModel<PeriodoInativacaoCaixaRepresentante> getModel() {
		return this.model;
	}
	
	public Date getDtFinalInativa() {
		return dtFinalInativa;
	}
	
	public void setDtFinalInativa(Date dtFinalInativa) {
		this.dtFinalInativa = dtFinalInativa;
	}
	
	public Date getDtInicialInativa() {
		return dtInicialInativa;
	}
	
	public void setDtInicialInativa(Date dtInicialInativa) {
		this.dtInicialInativa = dtInicialInativa;
	}
	
	public CaixaAdvogadoProcurador getCaixaAdvogadoProcurador() {
		return caixaAdvogadoProcurador;
	}
	
	public void setCaixaAdvogadoProcurador(
			CaixaAdvogadoProcurador caixaAdvogadoProcurador) {
		this.caixaAdvogadoProcurador = caixaAdvogadoProcurador;
	}
}
