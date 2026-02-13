package br.com.infox.cliente.home;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.service.LocalizacaoService;
import br.jus.pje.nucleo.entidades.CentralMandado;

@Name(CentralMandadoHome.NAME)
@BypassInterceptors
public class CentralMandadoHome extends AbstractCentralMandadoHome<CentralMandado> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "centralMandadoHome";

	public static CentralMandadoHome instance() {
		return ComponentUtil.getComponent(CentralMandadoHome.NAME);
	}

	@Override
	public String persist() {
		String ret = null;

		if (verificaDuplicidade()) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Registro já cadastrado!");
		} else {
			ret = super.persist();
		}
		return ret;
	}

	@Override
	public String update() {
		String ret = null;
		try {
			getEntityManager().merge(getInstance());
			EntityUtil.flush(getEntityManager());
			ret = getUpdatedMessage().getValue().toString();
			FacesMessages.instance().add(StatusMessage.Severity.INFO, "Registro alterado com sucesso");
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause instanceof ConstraintViolationException) {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Registro já cadastrado!");
			}
		}
		return ret;
	}

	public boolean verificaDuplicidade() {
		Query query = EntityUtil.getEntityManager().createQuery(
				"select o from CentralMandado o where lower(centralMandado) = :descricao");
		query.setParameter("descricao", getInstance().getCentralMandado().toLowerCase());
		Integer result = query.getResultList().size();
		if (result > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * PJEII-5218: as centrais de mandados eram buscadas através de um componente xml que possui limitações para 
	 * personalização de consultas. 
	 */
	/**
	 * Busca a lista de central de mandados ativas ou inativas, de acordo com o status passado como parametro.
	 * 
	 * @param centralAtiva indica se as centrais a serem pesquisadas estão ativas ou inativas
	 * @return
	 */	
	public List<CentralMandado> getListaCentralMandadosAtivasOuInativas(boolean centralAtiva){
	
		StringBuilder sql = new StringBuilder();
		sql.append("select o from CentralMandado o ");
		String selectPorPerfil = selectPorPerfil();
		if (selectPorPerfil.equals("")){
			sql.append(" where o.ativo = :ativo");
		}else {			
			sql.append(selectPorPerfil);
			sql.append(" and o.ativo = :ativo");
		}
		EntityManager entityManager = EntityUtil.getEntityManager();
		Query query = entityManager.createQuery(sql.toString());
		query.setParameter("ativo", centralAtiva);
		@SuppressWarnings("unchecked")
		List<CentralMandado> listaCentralMandados = query.getResultList();
				
		return listaCentralMandados;
	}
	
	/*
	 * PJEII-5218: as centrais de mandados eram buscadas através de um componente xml que possui limitações para 
	 * personalização de consultas. 
	 */
	/**
	 * Busca a lista de todas as centrais de mandados
	 * 
	 * @return
	 */	
	public List<CentralMandado> getListaCompletaDeCentralMandados(){
	
		StringBuilder sql = new StringBuilder();
		sql.append("select o from CentralMandado o");
		sql.append(selectPorPerfil());
		EntityManager entityManager = EntityUtil.getEntityManager();
		Query query = entityManager.createQuery(sql.toString());
		@SuppressWarnings("unchecked")
		List<CentralMandado> listaCentralMandados = query.getResultList();
				
		return listaCentralMandados;
	}

	public String selectPorPerfil() {
		if (!Authenticator.getPapelAtual().getIdentificador().equalsIgnoreCase("admin") &&
				!Authenticator.getPapelAtual().getIdentificador().equalsIgnoreCase("administrador")) {
			
			LocalizacaoService localizacaoService = ComponentUtil.getComponent("localizacaoService");
			
			String subQueryIdsCentralMandado = 
				String.format("select cml.centralMandado.idCentralMandado from CentralMandadoLocalizacao cml where cml.localizacao.idLocalizacao in %s", 
				localizacaoService.getTreeIds(Authenticator.getUsuarioLocalizacaoAtual().getLocalizacaoFisica()));
			
			return " where o.idCentralMandado in ( " + subQueryIdsCentralMandado + " ) ";
		}
		
		return StringUtils.EMPTY;
	}

	public void limpar() {
		Contexts.removeFromAllContexts("localizacaoCMTree");
	}
}
