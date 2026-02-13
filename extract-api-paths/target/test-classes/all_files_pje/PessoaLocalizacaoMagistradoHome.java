package br.com.infox.cliente.home;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.pje.nucleo.entidades.PessoaLocalizacaoMagistrado;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.enums.TitularidadeMagistradoEnum;

@Name("pessoaLocalizacaoMagistradoHome")
@BypassInterceptors
public class PessoaLocalizacaoMagistradoHome extends
		AbstractPessoaLocalizacaoMagistradoHome<PessoaLocalizacaoMagistrado> {

	private static final long serialVersionUID = 1L;

	public static PessoaLocalizacaoMagistradoHome instance() {
		return ComponentUtil.getComponent("pessoaLocalizacaoMagistradoHome");
	}

	public UsuarioLocalizacaoMagistradoServidor getByUsuarioLocalizacao(UsuarioLocalizacao usuarioLocalizacao) {
		EntityManager em = EntityUtil.getEntityManager();
		StringBuilder sb = new StringBuilder();
		sb.append("select o from UsuarioLocalizacaoMagistradoServidor o ");
		sb.append("where o.usuarioLocalizacao = :usuarioLocalizacao ");
		sb.append("and o.dataFinal is NULL");
		Query q = em.createQuery(sb.toString());
		q.setParameter("usuarioLocalizacao", usuarioLocalizacao.getIdUsuarioLocalizacao());
		try {
			return (UsuarioLocalizacaoMagistradoServidor) q.getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}

	public UsuarioLocalizacaoMagistradoServidor getByUsuarioLocalizacaoMagistradoServidor(
			PessoaMagistrado pessoaMagistrado) {
		EntityManager em = EntityUtil.getEntityManager();
		OrgaoJulgadorHome ojh = OrgaoJulgadorHome.instance();
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from UsuarioLocalizacaoMagistradoServidor o ");
		sb.append("where o.magistrado.idUsuario = :usuario ");
		sb.append("and o.dataFinal is not NULL and o.localizacaoFisica = :localizacao");
		Query q1 = em.createQuery(sb.toString());
		q1.setParameter("usuario", pessoaMagistrado.getIdUsuario());
		q1.setParameter("localizacao", ojh.getInstance().getLocalizacao().getLocalizacao());

		Long retorno = 0L;
		try {
			retorno = (Long) q1.getSingleResult();
		} catch (NoResultException no) {
			retorno = 0L;
		}
		if (retorno > 0) {
			return null;
		} else {
			Criteria criteria = HibernateUtil.getSession().createCriteria(UsuarioLocalizacaoMagistradoServidor.class);
			criteria.add(Restrictions.eq("magistrado.idUsuario", pessoaMagistrado.getIdUsuario()));
			criteria.add(Restrictions.eq("localizacaoFisica", ojh.getInstance().getLocalizacao().getLocalizacao()));
			criteria.add(Restrictions.isNull("dataFinal"));
			criteria.setFirstResult(0);
			criteria.setMaxResults(1);
			return (UsuarioLocalizacaoMagistradoServidor)criteria.uniqueResult();
		}
	}

	public TitularidadeMagistradoEnum[] getTitularidadeMagistradoEnumValues() {
		return TitularidadeMagistradoEnum.values();
	}

}