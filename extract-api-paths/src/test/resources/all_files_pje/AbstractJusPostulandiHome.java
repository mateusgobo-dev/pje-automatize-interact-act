package br.com.infox.cliente.home;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;

public abstract class AbstractJusPostulandiHome<T> extends AbstractHome<PessoaFisica> {

	private static final long serialVersionUID = 1L;

	public void setJusPostulandiIdJusPostulandi(Integer id) {
		setId(id);
	}

	public Integer getJusPostulandiIdJusPostulandi() {
		return (Integer) getId();
	}

	@Override
	protected PessoaFisica createInstance() {
		PessoaFisica pessoaJusPostulandi = new PessoaFisica();
		return pessoaJusPostulandi;
	}

	@Override
	public String persist() {
		String action = super.persist();
		if (action != null)
			newInstance();
		return action;
	}

	@Override
	public String remove(PessoaFisica obj) {
		setInstance(obj);

		String ret = inativaJusPostulandi(obj);

		newInstance();
		refreshGrid("confirmaCadastroPessoaJusPostulandiGrid");
		return ret;
	}
	
	/**
	 * @author Jonathas Dantas
	 * 
	 * Metodo que inativa uma pessoa fisica para o papel jus postulandi.
	 * 
	 * @param obj
	 * 
	 * @return mensagem de processamento
	 */
	private String inativaJusPostulandi(PessoaFisica obj) {

		String ret = "";
		try {
			EntityManager entityManager = EntityUtil.getEntityManager();
			
			Query query = entityManager.createQuery("select ul FROM UsuarioLocalizacao ul WHERE ul.usuario.idUsuario = ? AND ul.papel.idPapel = ?");
			query.setParameter(1, obj.getIdUsuario());
			
			Papel papelJusPostulandi = ParametroUtil.instance().getPapelJusPostulandi();
			query.setParameter(2, papelJusPostulandi.getIdPapel());
			
			UsuarioLocalizacao ul = (UsuarioLocalizacao) query.getSingleResult();
			
			if (ul != null) {
				entityManager.remove(ul);
				entityManager.flush();

				ret = "Usuário inativado com sucesso!";
			} else {
				ret = "Usuário não encontrado!";
			}

		} catch (Exception e) {
			e.printStackTrace();
			ret = "Ocorreu um erro ao tentar inativar o usuário.";
		}

		return ret;
	}
	
}
