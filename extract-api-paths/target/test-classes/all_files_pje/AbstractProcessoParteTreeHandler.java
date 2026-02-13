package br.com.infox.ibpm.component.tree;

import br.com.infox.cliente.home.ProcessoParteHome;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.itx.component.Util;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;


public abstract class AbstractProcessoParteTreeHandler<T> extends AbstractTreeHandler<ProcessoParte> {
	private static final long serialVersionUID = 1L;
	private static Util util = new Util();
	private ProcessoParteParticipacaoEnum inParticipacao = ProcessoParteParticipacaoEnum.A;
	private Boolean mostrarInativos = Boolean.FALSE;
	private String order = "ppa.idProcessoParte";

	protected void setInParticipacao(ProcessoParteParticipacaoEnum inParticipacao) {
		this.inParticipacao = inParticipacao;
	}

    @Override
	protected String getQueryRoots() {
	    Boolean consultaPublica = util.getUrlRequest().contains("/ConsultaPublica/");
	    this.mostrarInativos = configurarMostrarInativos();
		StringBuilder hql = new StringBuilder();
		hql.append("select ppa ");
		hql.append("from ProcessoParte ppa ");
		hql.append("where ppa.processoTrf.idProcessoTrf = "
				+ ProcessoTrfHome.instance().getInstance().getIdProcessoTrf() + " ");
		hql.append("and ppa.inParticipacao = '" + this.inParticipacao + "' ");
		if(!mostrarInativos) {			
			if (consultaPublica){
			    hql.append("and (ppa.inSituacao = 'A' or ppa.inSituacao = 'S' ) ");
			} else {
			    hql.append("and ppa.inSituacao = 'A' ");
			}
		}		
		hql.append("and ppa not in ");
		hql.append("(select distinct ppa2 from ProcessoParteRepresentante ppr ");
		hql.append("inner join ppr.parteRepresentante ppa2 ");
		hql.append("where ppa2 = ppa) ");
		if (order != null && order.trim().length() > 0) {
			hql.append("order by " + order);
		}
		return hql.toString();
	}
    
    /**
     * @return a quantidade total de registros, incluíndo os representantes, que serão apresentados
     * na grid de partes do processo, afim de montar o DataModel usado na paginação por demanda (PageDataModel)
     */
    protected String getQueryCountRoots() {
	    Boolean consultaPublica = util.getUrlRequest().contains("/ConsultaPublica/");
	    this.mostrarInativos = configurarMostrarInativos();
		StringBuilder hql = new StringBuilder();
		hql.append("select count(ppa) ");
		hql.append("from ProcessoParte ppa ");
		hql.append("where ppa.processoTrf.idProcessoTrf = "
				+ ProcessoTrfHome.instance().getInstance().getIdProcessoTrf() + " ");
		hql.append("and ppa.inParticipacao = '" + this.inParticipacao + "' ");
		if(!mostrarInativos) {			
			if (consultaPublica){
			    hql.append("and (ppa.inSituacao = 'A' or ppa.inSituacao = 'S' ) ");
			} else {
			    hql.append("and ppa.inSituacao = 'A' ");
			}
		}		
		return hql.toString();
	}

	@Override
	protected String getQueryChildren() {
		this.mostrarInativos = configurarMostrarInativos();
		StringBuilder hql = new StringBuilder();
		hql.append("select distinct ppa2 from ProcessoParteRepresentante ppr ");
		hql.append("inner join ppr.parteRepresentante ppa2 ");
		hql.append("where ppa2.processoTrf.idProcessoTrf = "
				+ ProcessoTrfHome.instance().getInstance().getIdProcessoTrf() + " ");
		hql.append("and ppr.processoParte =:" + EntityNode.PARENT_NODE + " ");
		if (!mostrarInativos) {
			hql.append("and ppa2.inSituacao = 'A' ");
			hql.append("and (ppr.inSituacao = 'A' or ppr.inSituacao = 'S') ");
		}
		if (order != null && order.trim().length() > 0) {
			hql.append(" order by " + order.replace("ppa", "ppa2"));
		}
		return hql.toString();
	}

	private boolean configurarMostrarInativos() {
		if (inParticipacao == ProcessoParteParticipacaoEnum.A)
			return ProcessoParteHome.instance().getMostrarInativosPoloAtivo();
		else if (inParticipacao == ProcessoParteParticipacaoEnum.P)
			return ProcessoParteHome.instance().getMostrarInativosPoloPassivo();
		else
			return ProcessoParteHome.instance().getMostrarInativosOutrosParticipantes();
	}
	
	public void setOrder(String order) {
		this.order = order;
	}
	
	public String getOrder() {
		return order;
	}
}