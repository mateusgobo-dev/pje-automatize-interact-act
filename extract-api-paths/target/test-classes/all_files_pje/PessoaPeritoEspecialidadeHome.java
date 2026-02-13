package br.com.infox.cliente.home;

import javax.persistence.Query;

import org.hibernate.annotations.common.AssertionFailure;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.Especialidade;
import br.jus.pje.nucleo.entidades.PessoaPerito;
import br.jus.pje.nucleo.entidades.PessoaPeritoEspecialidade;

@Name("pessoaPeritoEspecialidadeHome")
@BypassInterceptors
public class PessoaPeritoEspecialidadeHome extends AbstractPessoaPeritoEspecialidadeHome<PessoaPeritoEspecialidade> {

	private static final long serialVersionUID = 1L;

	public static PessoaPeritoEspecialidadeHome instance() {
		return ComponentUtil.getComponent("pessoaPeritoEspecialidadeHome");
	}

	public void addEspecialidade(Especialidade obj, String gridId) {
		if (getInstance() != null) {
			newInstance();
			getInstance().setEspecialidade(obj);
			PessoaPerito pessoaPerito = PessoaPeritoHome.instance().getInstance();
			getInstance().setPessoaPerito(pessoaPerito);
			PessoaPeritoEspecialidade pessoaPeritoEspecialidade = getInstance();
			persist();
			pessoaPerito.getPessoaPeritoEspecialidadeList().add(pessoaPeritoEspecialidade);
			getEntityManager().flush();
			newInstance();
			refreshGrid("pessoaPeritoEspecialidadeGrid");
			refreshGrid("pessoaPeritoEspecialidadeRightGrid");
		}
	}

	public void removeEspecialidade(PessoaPeritoEspecialidade obj, String gridId) {
		if (getInstance() != null) {
			String retorno = null;
			StringBuilder sb = new StringBuilder();
			sb.append("select count(*) from PessoaPeritoDisponibilidade o ");
			sb.append("where o.pessoaPeritoEspecialidade = :peritoEsp");
			Query hql = getEntityManager().createQuery(sb.toString());
			hql.setParameter("peritoEsp", obj);
			if ((Long) hql.getSingleResult() == 0) {
				StringBuilder builder = new StringBuilder();
				builder.append("select count(*) from PessoaPeritoIndisponibilidade o ");
				builder.append("where o.pessoaPeritoEspecialidade = :peritoEsp");
				hql = getEntityManager().createQuery(builder.toString());
				hql.setParameter("peritoEsp", obj);
				if ((Long) hql.getSingleResult() == 0) {
					PessoaPerito pessoa = obj.getPessoaPerito();
					pessoa.getPessoaPeritoEspecialidadeList().remove(obj);
					getEntityManager().remove(obj);
					try {
						getEntityManager().flush();
						retorno = "Excluido com Sucesso";
					} catch (AssertionFailure e) {
						System.out.println(e.getMessage());
					}
					newInstance();
					refreshGrid("pessoaPeritoEspecialidadeGrid");
					refreshGrid("pessoaPeritoEspecialidadeRightGrid");
				} else {
					retorno = "Já existe uma associação na entidade Perito " + "Indisponibilidade para esse registro";
				}
			} else {
				retorno = "Já existe uma associação na entidade Perito " + "Disponibilidade para esse registro";
			}
			FacesMessages.instance().add(Severity.INFO, retorno);
		}
	}
}