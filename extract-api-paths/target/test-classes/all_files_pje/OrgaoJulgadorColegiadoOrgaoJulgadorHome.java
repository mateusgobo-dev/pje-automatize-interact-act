package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.utils.Constantes.MANAGER;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.jt.pje.dao.OrgaoJulgadorColegiadoOrgaoJulgadorDAO;
import br.com.jt.pje.manager.OrgaoJulgadorColegiadoOrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoOrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoOrgaoJulgadorLog;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.util.DateUtil;

@Name(OrgaoJulgadorColegiadoOrgaoJulgadorHome.NAME)
@BypassInterceptors
public class OrgaoJulgadorColegiadoOrgaoJulgadorHome extends
		AbstractOrgaoJulgadorColegiadoOrgaoJulgadorHome<OrgaoJulgadorColegiadoOrgaoJulgador> {

	private static final long serialVersionUID = 2472051057763054134L;

	public static final String NAME = "orgaoJulgadorColegiadoOrgaoJulgadorHome";
	
	private Date dataFim;

	public static OrgaoJulgadorColegiadoOrgaoJulgadorHome instance() {
		return ComponentUtil.getComponent(OrgaoJulgadorColegiadoOrgaoJulgadorHome.NAME);
	}
	
	public OrgaoJulgadorColegiadoOrgaoJulgadorManager getManager() {
		return ComponentUtil.getComponent(OrgaoJulgadorColegiadoOrgaoJulgadorManager.NAME);		
	}

	
	public String getInfoTitular(OrgaoJulgador orgaoJulgador, OrgaoJulgadorColegiado ojc) {
		StringBuilder infoMagistrado = new StringBuilder();
		PessoaMagistrado magistradoTitular = null;
		try {
			magistradoTitular = getManager().obterMagistradoTitular(orgaoJulgador, ojc);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		if(magistradoTitular != null) {
			infoMagistrado.append(magistradoTitular.getNome());
			infoMagistrado.append(" | ");
			infoMagistrado.append("Posse: ");
			if(magistradoTitular.getDataPosse() != null) {
				infoMagistrado.append(DateUtil.getDataFormatada(magistradoTitular.getDataPosse(), "dd/MM/yyyy"));
			}else {
				infoMagistrado.append("-");
			}

			infoMagistrado.append(" | ");
			infoMagistrado.append("Nascimento: ");
			if(magistradoTitular.getDataNascimento() != null) {
				infoMagistrado.append(DateUtil.getDataFormatada(magistradoTitular.getDataNascimento(), "dd/MM/yyyy"));
			}else {
				infoMagistrado.append("-");
			}
		}else {
			infoMagistrado.append("Não há magistrado titular neste Colegiado");
		}
		return infoMagistrado.toString();
	}

	private void gravarLog() {
		OrgaoJulgadorColegiadoOrgaoJulgadorLog log = new OrgaoJulgadorColegiadoOrgaoJulgadorLog();
		if (instance.getOrgaoJulgadorColegiadoCargo() != null) {
			log.setCargo(instance.getOrgaoJulgadorColegiadoCargo().getCargo().getCargo());
		}
		log.setDataFinal(instance.getDataFinal());
		log.setDataInicial(instance.getDataInicial());
		log.setOrgaoJulgador(instance.getOrgaoJulgador().getOrgaoJulgador());
		log.setOrgaoJulgadorColegiado(instance.getOrgaoJulgadorColegiado().getOrgaoJulgadorColegiado());
		EntityManager em = EntityUtil.getEntityManager();
		em.persist(log);
		em.flush();
	}

	private Boolean isDataValida() {
		if (dataFim == null) {
			return true;
		}
		if (dataFim != null && dataFim.after(instance.getDataInicial())) {
			return true;
		}
		return false;
	}

	@Override
	public String persist() {
		String ret = null;
		if (isDataValida()) {
			instance.setOrgaoJulgadorColegiado(OrgaoJulgadorColegiadoHome.instance().getInstance());
			instance.setDataFinal(dataFim);
			if (verificaDuplicidadeOJCOJ()) {
				FacesMessages.instance().clear();
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Registro já cadastrado!");
				newInstance();
				return null;
			}
			if(!this.verificaVinculoAtivoOJComColegiado(instance)) {
				instance.setOrdem(null);
			}
			try {
				ret = super.persist();
				getManager().reordenaDemaisOJsAtivos(instance);
				gravarLog();
			} catch (Exception e) {
				Throwable cause = e.getCause();
				if (cause instanceof ConstraintViolationException) {
					FacesMessages.instance().add(StatusMessage.Severity.INFO, "Registro já cadastrado!");
				}
			}
		} else {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.INFO, "A data final deve ser maior que a data inicial");
		}
		return ret;
	}

	@Override
	public String update() {
		String ret = null;
		if (isDataValida()) {
			instance.setDataFinal(dataFim);
			if (verificaDuplicidadeOJCOJ()) {
				FacesMessages.instance().clear();
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Registro já cadastrado!");
				EntityUtil.getEntityManager().refresh(instance);
				return null;
			}
			if(!this.verificaVinculoAtivoOJComColegiado(instance)) {
				instance.setOrdem(null);
			}
			try {
				ret = super.update();
				getManager().reordenaDemaisOJsAtivos(instance);
				gravarLog();
			} catch (Exception e) {

			}
		} else {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.INFO, "A data final deve ser maior que a data inicial");
		}
		return ret;
	}
	
	public boolean verificaVinculoAtivoOJComColegiado(OrgaoJulgadorColegiadoOrgaoJulgador ojcoj) {
		return getManager().isVinculoOJSingularComColegiadoAtivo(ojcoj);
	}
	
	public void gerarOrdenacaoOJsColegiadoAutomaticamente() {
		OrgaoJulgadorColegiado ojc = OrgaoJulgadorColegiadoHome.instance().getInstance();
		try {
			getManager().gerarOrdenacaoOJsColegiadoAutomaticamente(ojc);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Método que obtem os órgãos julgadores ativos que estejam em uma localização igual ou inferior à 
	 * localização do OJC
	 * @return
	 */
	public List<OrgaoJulgador> getOrgaoJulgadorItems() {
		OrgaoJulgadorManager orgaoJulgadorManager = ComponentUtil.getComponent(MANAGER.ORGAO_JULGADOR);
		Localizacao localizacao = Authenticator.getLocalizacaoFisicaAtual();
		return orgaoJulgadorManager.findAllbyLocalizacao(localizacao);
	}

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgadorColegiadoCargo> getCargoItems() {
		StringBuffer ejbql = new StringBuffer();
		ejbql.append("select o from OrgaoJulgadorColegiadoCargo o where o.orgaoJulgadorColegiado = :orgaoJulgadorColegiado ");
		return EntityUtil.createQuery(ejbql.toString())
				.setParameter("orgaoJulgadorColegiado", OrgaoJulgadorColegiadoHome.instance().getInstance())
				.getResultList();
	}

	@SuppressWarnings("unchecked")
	public Boolean verificaDuplicidadeOJCOJ() {
		StringBuilder sqlPes = new StringBuilder();
		sqlPes.append("select o from OrgaoJulgadorColegiadoOrgaoJulgador o where o.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = :orgaoJulgadorColegiado ");
		sqlPes.append("and o.orgaoJulgador.idOrgaoJulgador = :orgaoJulgador ").append(
				"and o.idOrgaoJulgadorColegiadoOrgaoJulgador != :ojcoj ");
		sqlPes.append(" and (o.dataFinal is null or o.dataFinal >= :dataInicial)"); 
		EntityManager em = getEntityManager();
		Query query = em.createQuery(sqlPes.toString());
		query.setParameter("orgaoJulgadorColegiado", instance.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado());
		query.setParameter("orgaoJulgador", instance.getOrgaoJulgador().getIdOrgaoJulgador());
		query.setParameter("ojcoj", instance.getIdOrgaoJulgadorColegiadoOrgaoJulgador());
		query.setParameter("dataInicial", instance.getDataInicial());
		List<OrgaoJulgadorColegiadoOrgaoJulgador> list = query
				.getResultList();

		return (list.size() > 0);
	}

	public Date getDataFim() {
		dataFim = instance.getDataFinal();
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	@Override
	public String remove(OrgaoJulgadorColegiadoOrgaoJulgador obj) {
		String ret = null;
		try {
			this.dataFim = DateUtil.getDataAtual();
			obj.setOrgaoJulgadorRevisor(null);
			this.setInstance(obj);
			this.update();
		} catch (Exception e) {
			FacesMessages.instance().add(StatusMessage.Severity.INFO, "Este registro não pode ser excluído.");
		}
		newInstance();
		super.remove();
		return ret;
	}

	public OrgaoJulgadorColegiadoOrgaoJulgadorDAO getOrgaoJulgadorColegiadoOrgaoJulgadorDAO() {
		return ComponentUtil.getComponent(OrgaoJulgadorColegiadoOrgaoJulgadorDAO.NAME);
	}
	
	/**
	 * Retorna uma lista de OrgaoJulgadorColegiadoOrgaoJulgador que nao sao utilizados como revisor
	 * @return Lista de OrgaoJulgadorColegiadoOrgaoJulgador
	 */
	public List<OrgaoJulgadorColegiadoOrgaoJulgador> getOutrosOrgaosJulgadoresDoColegiado() {
		OrgaoJulgadorColegiadoOrgaoJulgador ojcOrgaoJulgador = getInstance();
		OrgaoJulgadorColegiado orgaoJulgadorColegiado = OrgaoJulgadorColegiadoHome.instance().getInstance();
				
		List<OrgaoJulgadorColegiadoOrgaoJulgador> itens = 
				getOrgaoJulgadorColegiadoOrgaoJulgadorDAO().recuperarOrgaosJulgadoresDoColegiadoNaoUtilizadosComoRevisor(orgaoJulgadorColegiado, ojcOrgaoJulgador.getOrgaoJulgador());
		
		if (ojcOrgaoJulgador.getOrgaoJulgadorRevisor() != null) {
			itens.add(ojcOrgaoJulgador.getOrgaoJulgadorRevisor());
		}
		return itens;				
	}
	
	public List<Integer> getOrdensPossiveisOJ(){
		OrgaoJulgadorColegiado ojc = OrgaoJulgadorColegiadoHome.instance().getInstance();
		Long numOJsAtivos = getOrgaoJulgadorColegiadoOrgaoJulgadorDAO().countOrgaoJulgadorPorOrgaoJulgadorColegiado(ojc);
		List<Integer> ordens = new ArrayList<Integer>();

		OrgaoJulgadorColegiadoOrgaoJulgador ojcOrgaoJulgador = getInstance();
		if(ojcOrgaoJulgador != null && ojcOrgaoJulgador.getOrgaoJulgador() != null) {
			if(this.verificaVinculoAtivoOJComColegiado(instance)) {
				for(int i = 1; i <= numOJsAtivos; i++) {
					ordens.add(i);
				}
			}
		}
		if(ordens.isEmpty()) {
			ordens.add(numOJsAtivos.intValue() + 1);
		}
		
		return ordens;
	}
}
