package br.com.infox.cliente.home;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.hibernate.AssertionFailure;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.FacesUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.nucleo.manager.ProcessoTrfUsuarioLocalizacaoMagistradoServidorManager;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfUsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoVisibilidade;

@Name("processoTrfUsuarioLocalizacaoMagistradoServidorHome")
@BypassInterceptors
public class ProcessoTrfUsuarioLocalizacaoMagistradoServidorHome extends
		AbstractProcessoTrfUsuarioLocalizacaoMagistradoServidorHome<ProcessoTrfUsuarioLocalizacaoMagistradoServidor> {

	private static final long serialVersionUID = 1L;
	private static final LogProvider log = Logging
			.getLogProvider(ProcessoTrfUsuarioLocalizacaoMagistradoServidorHome.class);

	private UsuarioLocalizacaoMagistradoServidor usuLocMagistradoServidor;
	private UsuarioLocalizacaoVisibilidade usuLocVisibilidade;
	private Boolean selecionado = Boolean.FALSE;
	private String numeroProcesso;
	private ClasseJudicial classeJudicial;
	private AssuntoTrf assuntoTrf;
	private String nomeParte;
	private Integer anoInicial;
	private Integer anoFinal;

	public Boolean getSelecionado() {
		return selecionado;
	}

	public void setSelecionado(Boolean selecionado) {
		this.selecionado = selecionado;
	}

	public void setUsuLocMagistradoServidor(UsuarioLocalizacaoMagistradoServidor usuLocMagistradoServidor) {
		this.usuLocMagistradoServidor = usuLocMagistradoServidor;
	}

	public UsuarioLocalizacaoMagistradoServidor getUsuLocMagistradoServidor() {
		return usuLocMagistradoServidor;
	}

	private UsuarioLocalizacaoVisibilidade getVisibilidade(UsuarioLocalizacaoMagistradoServidor usuario) {
		Criteria criteria = HibernateUtil.getSession().createCriteria(UsuarioLocalizacaoVisibilidade.class);
		criteria.add(Restrictions.eq("usuarioLocalizacaoMagistradoServidor", usuario));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		return (UsuarioLocalizacaoVisibilidade)criteria.uniqueResult();
	}

	public void selecionarMagistrdao() {
		if (usuLocMagistradoServidor != null) {
			this.selecionado = Boolean.TRUE;
			usuLocVisibilidade = getVisibilidade(usuLocMagistradoServidor);
			refreshGrid("processoTrfOrgJulgadorGrid");
		} else {
			this.selecionado = Boolean.FALSE;
		}
	}

	@Override
	public void newInstance() {
		usuLocMagistradoServidor = null;
		selecionado = false;
		super.newInstance();
	}

	@Override
	public String persist() {
		String ret = null;
		try {
			ret = super.persist();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		refreshGrid("processoTrfUsuarioLocMagistradoGrid");
		return ret;
	}

	@Override
	public String remove(ProcessoTrfUsuarioLocalizacaoMagistradoServidor obj) {
		return super.remove(obj);
	}

	@Override
	public String update() {
		String ret = null;
		try {
			getEntityManager().merge(getInstance());
			getEntityManager().flush();
			ret = getUpdatedMessage().getValue().toString();
			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Registro alterado com sucesso");
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause instanceof ConstraintViolationException) {
				FacesMessages.instance().clear();
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Registro já cadastrado!");
			}
		}
		return ret;
	}

	public static ProcessoTrfUsuarioLocalizacaoMagistradoServidorHome instance() {
		return ComponentUtil.getComponent("processoTrfUsuarioLocalizacaoMagistradoServidorHome");
	}

	public void addProcessoTrfMagistridoAuxiliar(ProcessoTrf obj, String idGrid) {
		ProcessoTrfUsuarioLocalizacaoMagistradoServidorManager manager = 
				(ProcessoTrfUsuarioLocalizacaoMagistradoServidorManager)
					Component.getInstance("processoTrfUsuarioLocalizacaoMagistradoServidorManager");
		FacesMessages.instance().clear();
		try{
			manager.addProcessoTrfMagistradoAuxiliar(obj,getUsuLocMagistradoServidor());
		}catch (Exception e){
			FacesMessages.instance().add(Severity.ERROR, "Erro ao inserir registro");
		}
		FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "processoTrfUsuarioLocalizacaoMagistradoServidor_created"));
		refreshGrid(idGrid);
		refreshGrid("processoTrfOrgJulgadorGrid");
	}

	public void removeProcessoTrfMagistridoAuxiliar(ProcessoTrfUsuarioLocalizacaoMagistradoServidor obj, String idGrid) {
		getUsuLocMagistradoServidor().getProcessoMagistradoList().remove(obj);
		instance = obj;
		try {
			getEntityManager().remove(instance);
			getEntityManager().flush();
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "processoTrfUsuarioLocalizacaoMagistradoServidor_deleted"));
		} catch (AssertionFailure e) {
			log.error(e.getMessage());
		}
		refreshGrid(idGrid);
		refreshGrid("processoTrfOrgJulgadorGrid");
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public Integer getAnoInicial() {
		return anoInicial;
	}

	public void setAnoInicial(Integer anoInicial) {
		this.anoInicial = anoInicial;
	}

	public Integer getAnoFinal() {
		return anoFinal;
	}

	public void setAnoFinal(Integer anoFinal) {
		this.anoFinal = anoFinal;
	}

	public Boolean verificaMagistradosAuxiliarCadastrados(OrgaoJulgador orgJulgador) {
		String query = "select count(o) from UsuarioLocalizacaoMagistradoServidor o "
				+ "where o.orgaoJulgadorCargo.auxiliar = true " + "and o.orgaoJulgador.localizacao = :localizacao";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("localizacao", orgJulgador.getLocalizacao());
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	public Boolean verificaMagistradosAuxiliar(Pessoa pessoaLogada) {
		if (Authenticator.getOrgaoJulgadorAtual() == null) {
			return false;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from UsuarioLocalizacaoMagistradoServidor o ");
		sb.append("where o.orgaoJulgadorCargo.auxiliar = true ");
		sb.append("and o.orgaoJulgador.localizacao = :localizacao ");
		sb.append("and o.usuarioLocalizacao.usuario.idUsuario = :idPessoaLogada ");
		sb.append("and o.orgaoJulgador = :oj ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("localizacao", Authenticator.getOrgaoJulgadorAtual().getLocalizacao());
		q.setParameter("idPessoaLogada", pessoaLogada.getIdUsuario());
		q.setParameter("oj", Authenticator.getOrgaoJulgadorAtual());
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	public void setUsuLocVisibilidade(UsuarioLocalizacaoVisibilidade usuLocVisibilidade) {
		this.usuLocVisibilidade = usuLocVisibilidade;
	}

	public UsuarioLocalizacaoVisibilidade getUsuLocVisibilidade() {
		return usuLocVisibilidade;
	}
}
