package br.com.infox.cliente.home;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.QuadroAviso;
import br.jus.pje.nucleo.util.DateUtil;

@Name("quadroAvisoHome")
@BypassInterceptors
public class QuadroAvisoHome extends AbstractHome<QuadroAviso> {

	private static final long serialVersionUID = 1L;
	
	public static QuadroAvisoHome instance() {
		return ComponentUtil.getComponent("quadroAvisoHome");
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		Date data = this.getDataAtual();
		Date dataPublicacao = instance.getDataPublicacao();
		Date dataExpiracao = instance.getDataExpiracao();

		// Condições a serem ultrapassadas
		// deve haver pelo menos a data de publicação
		if (dataPublicacao == null) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR,
					"É necessário preencher pelo menos a data de publicação.");
			return Boolean.FALSE;
		}

		// se é um novo aviso
		if (!this.verificaPublicacao()) {
			// a data de publicação deve ser no mínimo ou posterior à data atual
			if (dataPublicacao.compareTo(data) < 0) {
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.ERROR,
						"A data de publicação não pode ser anterior a atual");
				return Boolean.FALSE;
			}

			// se foi preenchida uma data para expiração, ela deve ser igual ou
			// posterior à data de publicação
			if (dataExpiracao != null) {
				if (dataExpiracao.compareTo(DateUtil.getDataSemHora(dataPublicacao)) < 0) {
					FacesMessages.instance().clear();
					FacesMessages
							.instance()
							.add(Severity.ERROR,
									"A data de expiração não pode ser anterior à data de publicação");
					return Boolean.FALSE;
				}
			}
		} else // é uma atualização do aviso. Só dá para alterar a data de expiração
		{
			// se foi preenchida uma data para expiração, ela deve ser igual ou
			// posterior à data de publicação
			if (dataExpiracao != null) {
				// a data de expiração não pode ser anterior à data atual
				if (dataExpiracao.compareTo(data) < 0) 
				{
					FacesMessages.instance().clear();
					FacesMessages
							.instance()
							.add(Severity.ERROR,
									"A data de expiração não pode ser anterior à data atual");
					return Boolean.FALSE;
				}

				// a data de expiração não pode ser anterior à data de publicação
				if (dataExpiracao.compareTo(DateUtil.getDataSemHora(dataPublicacao)) < 0) 
				{
					FacesMessages.instance().clear();
					FacesMessages
							.instance()
							.add(Severity.ERROR,
									"A data de expiração não pode ser anterior à data de publicação");
					return Boolean.FALSE;
				}
			}
		}
		
		if(instance.getTopo() == true) {
			StringBuffer query = new StringBuffer();
			query.append("select qa.titulo from QuadroAviso qa ");
			query.append("where qa.topo = true and (qa.dataExpiracao > :dataAtual or qa.dataExpiracao is null) and qa.ativo = true ");
			if(instance.getIdQuadroAviso() == 0) {
				Boolean isValido = validaTopo(query.toString());
				if(!isValido) {
					instance.setTopo(false);
					return isValido;
				}
			} else {
				query.append("and qa.id != " + instance.getIdQuadroAviso());
				Boolean isValido = validaTopo(query.toString());
				if(!isValido) {
					instance.setTopo(false);
					return isValido;
				}
			}
		}
		// fim condições a serem ultrapassadas

		return super.beforePersistOrUpdate();
	}

	@SuppressWarnings("unchecked")
	private Boolean validaTopo(String queryValidaTopo) {
		Query q = getEntityManager().createQuery(queryValidaTopo);
		q.setParameter("dataAtual", this.getDataAtual());
		List<String> resultList = q.getResultList();
		if(resultList != null && resultList.size() > 0) {
			FacesMessages.instance().clear();
			FacesMessages
					.instance()
					.add(Severity.ERROR,
							"O quadro de aviso '" + resultList.get(0) + "' já está no topo");
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	@Override
	public String update() {
		instance.setUsuarioUltimaAlteracao(Authenticator.getUsuarioLogado());
		instance.setDataUltimaAlteracao(new Date());
		return super.update();
	}

	@Override
	public String persist() {
		instance.setUsuarioInclusao(Authenticator.getUsuarioLogado());
		Date dataAtual = new Date();
		instance.setDataCadastro(dataAtual);
		Calendar dataPublicacao = Calendar.getInstance();
		dataPublicacao.setTime(instance.getDataPublicacao());
		Calendar dataCadastro = Calendar.getInstance();
		dataCadastro.setTime(dataAtual);
		dataPublicacao.set(Calendar.HOUR_OF_DAY, dataCadastro.get(Calendar.HOUR_OF_DAY));
		dataPublicacao.set(Calendar.MINUTE, dataCadastro.get(Calendar.MINUTE));
		dataPublicacao.set(Calendar.SECOND, dataCadastro.get(Calendar.SECOND));
		dataPublicacao.set(Calendar.MILLISECOND, dataCadastro.get(Calendar.MILLISECOND));
		instance.setDataPublicacao(dataPublicacao.getTime());
		instance.setUsuarioUltimaAlteracao(Authenticator.getUsuarioLogado());
		instance.setDataUltimaAlteracao(dataAtual);
		return super.persist();
	}

	@Override
	public String remove(QuadroAviso obj) {
		return super.remove(obj);
	}

	@Override
	public void newInstance() {
		super.newInstance();
		// listaPapeis = null;
	}

	public void setQuadroIdQuadro(Integer id) {
		setId(id);
	}

	public Integer getQuadroIdQuadro() {
		return (Integer) getId();
	}

	@Override
	public String inactive(QuadroAviso instance) {
		instance.setAtivo(false);
		getEntityManager().merge(instance);
		getEntityManager().flush();
		return "";
	}

	/**
	 * Verifica se o aviso já foi publicado.
	 * 
	 * @return
	 */
	public boolean verificaPublicacao() {
		if (isManaged()) {
			if (!(getInstance().getDataPublicacao().after(new Date()) || getInstance().getDataPublicacao().equals(
					new Date()))) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public boolean exibeAviso(QuadroAviso aviso) {
		Date dataAtual = getDataAtual();
		if (dataAtual.compareTo(DateUtil.getDataSemHora(aviso.getDataPublicacao())) >= 0
				&& (aviso.getDataExpiracao() == null || aviso.getDataExpiracao().compareTo(dataAtual) >= 0)) {
			return true;
		}
		return false;
	}

	/**
	 * Retorna a data atual com a hora zerada (00:00:00) para realizar validacoes no quadro de avisos
	 * @return
	 */
	private Date getDataAtual() {
		GregorianCalendar dataAtual = new GregorianCalendar();
		dataAtual.set(GregorianCalendar.MINUTE, 00);
		dataAtual.set(GregorianCalendar.MILLISECOND, 00);
		dataAtual.set(GregorianCalendar.SECOND, 00);
		dataAtual.set(GregorianCalendar.HOUR_OF_DAY, 00);
		return dataAtual.getTime();
	}

}
