package br.com.infox.cliente.home;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaServidor;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteVisibilidadeSigilo;

@Name("processoParteVisibilidadeSigiloHome")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class ProcessoParteVisibilidadeSigiloHome extends
		AbstractProcessoParteVisibilidadeSigiloHome<ProcessoParteVisibilidadeSigilo> {

	private static final long serialVersionUID = 1L;

	private Boolean checkBox = Boolean.FALSE;

	public void verificaPessoasMarcadas(ProcessoParte processoParte) {
		ProcessoParteHome.instance().setInstance(processoParte);

		GridQuery gqParte = getComponent("processoParteSigiloGrid");
		GridQuery gqServidor = null;
		if(ParametroUtil.instance().isPrimeiroGrau()) {
			gqServidor = getComponent("processoServidorSigiloGrid");
		} else {
			gqServidor = getComponent("processoServidorSigilo2GrauGrid");
		}

		// Limpa os CheckVisibilidade
		if (gqParte != null) {
			for (int i = 0; i < gqParte.getResultList().size(); i++) {
				ProcessoParte parte = (ProcessoParte) gqParte.getFullList().get(i);
				if (parte.getCheckVisibilidade()) {
					parte.setCheckVisibilidade(false);
					parte.setCheckado(false);
				}
			}
		}
		if(gqServidor != null) {
			for (int i = 0; i < gqServidor.getResultList().size(); i++) {
				PessoaServidor servidor = (PessoaServidor) gqServidor.getFullList().get(i);
				if (servidor.getCheckVisibilidade()) {
					servidor.setCheckVisibilidade(false);
					servidor.setCheckado(false);
				}
			}
		}

		// Consulta as pessoas que têm permissão
		EntityManager em = EntityUtil.getEntityManager();
		String sql = "select o.pessoa from ProcessoParteVisibilidadeSigilo o " + "where o.processoParte = :parte";
		Query query = em.createQuery(sql);
		query.setParameter("parte", ProcessoParteHome.instance().getInstance());

		// Se existir uma parte ou servidor cadastrado ele marca a permissão
		for (int i = 0; i < query.getResultList().size(); i++) {
			Pessoa pessoa = (Pessoa) query.getResultList().get(i);

			if (gqParte != null) {
				for (int j = 0; j < gqParte.getFullList().size(); j++) {
					ProcessoParte parte = (ProcessoParte) gqParte.getFullList().get(j);
					if (pessoa.equals(parte.getPessoa())) {
						parte.setCheckVisibilidade(true);
						parte.setCheckado(true);
					}
				}
			}

			if(gqServidor != null) {
				for (int j = 0; j < gqServidor.getFullList().size(); j++) {
					PessoaServidor servidor = (PessoaServidor) gqServidor.getFullList().get(j);
					if (pessoa.getIdPessoa().equals(servidor.getIdUsuario())) {
						servidor.setCheckVisibilidade(true);
						servidor.setCheckado(true);
					}
				}
			}
		}
	}

	public void checkAll(String grid) {
		GridQuery gq = getComponent(grid);
		if (grid.equals("processoParteSigiloGrid")) {
			for (int i = 0; i < gq.getResultList().size(); i++) {
				ProcessoParte parte = (ProcessoParte) gq.getFullList().get(i);
				parte.setCheckVisibilidade(checkBox);
				parte.setCheckado(checkBox);
			}
			//refreshGrid(grid);
		}
		if (grid.equals("processoServidorSigiloGrid")
				|| grid.equals("processoServidorSigiloGrid2Grau")) {
			for (int i = 0; i < gq.getResultList().size(); i++) {
				PessoaServidor servidor = (PessoaServidor) gq.getFullList().get(i);
				servidor.setCheckVisibilidade(checkBox);
				servidor.setCheckado(checkBox);
			}
			refreshGrid(grid);
		}
	}

	public String gravar(String grid) {
		String ret = null;
		GridQuery gq = getComponent(grid);

		if (grid.equals("processoServidorSigiloGrid")
				|| grid.equals("processoServidorSigiloGrid2Grau")) {
			for (int j = 0; j < gq.getFullList().size(); j++) {
				PessoaServidor servidor = (PessoaServidor) gq.getFullList().get(j);
				if (!servidor.getCheckado()) {
					if (servidor.getCheckVisibilidade()) {

						getInstance().setPessoa(servidor.getPessoa());
						getInstance().setProcessoParte(ProcessoParteHome.instance().getInstance());
						if (verificaDuplicidade(getInstance())) {
							ret = persist(getInstance());
							getEntityManager().flush();
						}
					}
				} else {
					if (!servidor.getCheckVisibilidade()) {
						setInstance(pegaPermissao(servidor.getPessoa()));

						ret = remove(getInstance());
						refreshGrid(grid);

						getEntityManager().flush();
					}
				}
			}
		} else if (grid.equals("processoParteSigiloGrid")) {
			for (int j = 0; j < gq.getFullList().size(); j++) {
				ProcessoParte parte = (ProcessoParte) gq.getFullList().get(j);

				/*
				 * [PJEII-3534] PJE-JT: Sérgio Ricardo : PJE-1.4.5 
				 * Correção da gravação dos itens marcados 
				 */		
				
				if (parte.getCheckVisibilidade()) {
					//if (!parte.getCheckado()) {

						getInstance().setPessoa(parte.getPessoa());
						getInstance().setProcessoParte(ProcessoParteHome.instance().getInstance());
						if (verificaDuplicidade(getInstance())) {
							ret = persist(getInstance());
							getEntityManager().flush();
						}

					//}
				} else {
					//if (!parte.getCheckVisibilidade()) {
						setInstance(pegaPermissao(parte.getPessoa()));

						ret = remove(getInstance());
						refreshGrid(grid);
						getEntityManager().flush();
					//}
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
	public boolean verificaDuplicidade(ProcessoParteVisibilidadeSigilo instance) {
		ProcessoParteVisibilidadeSigilo segredo = pegaPermissao(instance.getPessoa());
		return (segredo == null);
	}

	public ProcessoParteVisibilidadeSigilo pegaPermissao(Pessoa pessoa) {
		Criteria criteria = HibernateUtil.getSession().createCriteria(ProcessoParteVisibilidadeSigilo.class);
		criteria.add(Restrictions.eq("processoParte", ProcessoParteHome.instance().getInstance()));
		criteria.add(Restrictions.eq("pessoa", pessoa));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		return (ProcessoParteVisibilidadeSigilo)criteria.uniqueResult();
	}

	public void setCheckBox(Boolean checkBox) {
		this.checkBox = checkBox;
	}

	public Boolean getCheckBox() {
		return checkBox;
	}
}