package br.com.infox.cliente.home;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaServidor;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoVisibilidadeSegredo;

@Name("processoVisibilidadeSegredoHome")
@BypassInterceptors
public class ProcessoVisibilidadeSegredoHome extends
		AbstractProcessoVisibilidadeSegredoHome<ProcessoVisibilidadeSegredo> {

	/**
	 * processoVisibilidadeSegredoHome by Wilson
	 */
	private static final long serialVersionUID = 1L;

	private Boolean checkBox = Boolean.FALSE;
	private Boolean checkBoxParte = Boolean.FALSE; // [PJEII-5300]

	public ProcessoVisibilidadeSegredoHome instance() {
		return ComponentUtil.getComponent("processoVisibilidadeSegredoHome");
	}

	public void verificaPessoasMarcadas() {

		GridQuery gqParte = getComponent("processoParteSigiloGrid");
		GridQuery gqServidor = null;
		if(ParametroUtil.instance().isPrimeiroGrau()) {
			gqServidor = getComponent("processoServidorSigiloGrid");
		} else {
			gqServidor = getComponent("processoServidorSigilo2GrauGrid");
		}

		// Limpa os CheckVisibilidade
		if (gqParte != null) {
			for (int i = 0; i < gqParte.getFullList().size(); i++) {
				ProcessoParte parte = (ProcessoParte) gqParte.getFullList().get(i);
				parte.setCheckVisibilidade(false);
			}
		}
		if(gqServidor != null) {
			for (int i = 0; i < gqServidor.getFullList().size(); i++) {
				PessoaServidor servidor = (PessoaServidor) gqServidor.getFullList().get(i);
				servidor.setCheckVisibilidade(false);
			}
		}

		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getDefinedInstance();

		// Consulta as pessoas que têm permissão
		EntityManager em = EntityUtil.getEntityManager();
		StringBuilder sb = new StringBuilder();
		sb.append("select o.pessoa from ProcessoVisibilidadeSegredo o ");
		sb.append("where o.processo = :processo");
		Query query = em.createQuery(sb.toString());
		query.setParameter("processo", processoTrf);

		// Se existir uma parte ou servidor cadastrado ele marca a permissão
		for (int i = 0; i < query.getResultList().size(); i++) {
			Pessoa pessoa = (Pessoa) query.getResultList().get(i);

			if (gqParte != null) {
				for (int j = 0; j < gqParte.getFullList().size(); j++) {
					ProcessoParte parte = (ProcessoParte) gqParte.getFullList().get(j);
					if (pessoa.equals(parte.getPessoa())) {
						parte.setCheckVisibilidade(true);
					} 
				}
			}

			if(gqServidor != null) {
				for (int j = 0; j < gqServidor.getFullList().size(); j++) {
					PessoaServidor servidor = (PessoaServidor) gqServidor.getFullList().get(j);
					if (pessoa.getIdPessoa().equals(servidor.getIdUsuario())) {
						servidor.setCheckVisibilidade(true);
					}
				}
			}
		}
	}

	public void checkAll(String grid) {
		GridQuery gq = getComponent(grid);
		if (grid.equals("processoParteSigiloGrid")) {
			for (int i = 0; i < gq.getResultList().size(); i++) {
				ProcessoParte parte = (ProcessoParte) gq.getResultList().get(i);
				parte.setCheckVisibilidade(checkBoxParte);
			}
			refreshGrid(grid);
		}
		if (grid.equals("processoServidorSigiloGrid")
				|| grid.equals("processoServidorSigilo2GrauGrid")) {
			for (int i = 0; i < gq.getResultList().size(); i++) {
				PessoaServidor servidor = (PessoaServidor) gq.getResultList().get(i);
				servidor.setCheckVisibilidade(checkBox);
			}
			refreshGrid(grid);
		}
	}

	public String gravar(String grid) {
		String ret = null;
		GridQuery gq = getComponent(grid);

		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getDefinedInstance();

		if (grid.equals("processoServidorSigiloGrid") || grid.equals("processoServidorSigilo2GrauGrid")) {
			for (int j = 0; j < gq.getFullList().size(); j++) {
				PessoaServidor servidor = (PessoaServidor) gq.getFullList().get(j);
				if (servidor.getCheckVisibilidade()) {
					getInstance().setPessoa(servidor.getPessoa());
					getInstance().setProcesso(processoTrf.getProcesso());
					if (verificaDuplicidade(getInstance())) {
						ret = persist(getInstance());
						getEntityManager().flush();
					}
				}else {
					setInstance(pegaPermissao(servidor.getPessoa()));
					ret = remove(getInstance());
					refreshGrid(grid);
					getEntityManager().flush();
				}
			}
		} else if (grid.equals("processoParteSigiloGrid")) {
			for (int j = 0; j < gq.getFullList().size(); j++) {
				ProcessoParte parte = (ProcessoParte) gq.getFullList().get(j);
			
					if (parte.getCheckVisibilidade()) {

						getInstance().setPessoa(parte.getPessoa());
						getInstance().setProcesso(processoTrf.getProcesso());
						if (verificaDuplicidade(getInstance())) {
							ret = persist(getInstance());
							getEntityManager().flush();
						}

					}
				
					if (!parte.getCheckVisibilidade()) {
						setInstance(pegaPermissao(parte.getPessoa()));

						ret = remove(getInstance());
						refreshGrid(grid);
						getEntityManager().flush();
					}
				
			}
		}
		refreshGrid(grid);
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Registro atualizado com sucesso!");
		return ret;
	}

	/**
	 * Retorna TRUE se não existir na base de dados e FALSE se existir
	 * 
	 * @param instance
	 * @return
	 */
	public boolean verificaDuplicidade(ProcessoVisibilidadeSegredo instance) {
		ProcessoVisibilidadeSegredo segredo = pegaPermissao(instance.getPessoa());
		return (segredo == null);
	}

	public ProcessoVisibilidadeSegredo pegaPermissao(Pessoa pessoa) {
		Criteria criteria = HibernateUtil.getSession().createCriteria(ProcessoVisibilidadeSegredo.class);
		criteria.add(Restrictions.eq("pessoa", pessoa));
		criteria.add(Restrictions.eq("processo", ProcessoTrfHome.instance().getDefinedInstance().getProcesso()));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		return (ProcessoVisibilidadeSegredo)criteria.uniqueResult();
	}

	public void setCheckBox(Boolean checkBox) {
		this.checkBox = checkBox;
	}

	public Boolean getCheckBox() {
		return checkBox;
	}

	public Boolean getCheckBoxParte() {
		return checkBoxParte;
	}

	public void setCheckBoxParte(Boolean checkBoxParte) {
		this.checkBoxParte = checkBoxParte;
	}
}
