package br.com.infox.cliente.home;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.bean.PreCadastroPessoaBean;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.ProcessoAudiencia;
import br.jus.pje.nucleo.entidades.ProcessoAudienciaPessoa;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name("processoAudienciaPessoaHome")
@BypassInterceptors
public class ProcessoAudienciaPessoaHome extends AbstractProcessoAudienciaPessoaHome<ProcessoAudienciaPessoa> {

	private static final long serialVersionUID = 1L;

	public static ProcessoAudienciaPessoaHome instance() {
		return ComponentUtil.getComponent("processoAudienciaPessoaHome");
	}

	private String urlOpenner;
	private String codInParticipacao;
	private String novaInsercao;

	private Pessoa representante;

	public String getCodInParticipacao() {
		return this.codInParticipacao;
	}

	public String getNovaInsercao() {
		return this.novaInsercao;
	}

	public Pessoa getRepresentante() {
		return this.representante;
	}

	@SuppressWarnings("unchecked")
	public List<Pessoa> getRepresentantesAtivos() {
		StringBuilder s = new StringBuilder();
		s.append("select o.pessoa from ProcessoParte o ");
		s.append("where (o.processoTrf = :processo) ");
		s.append(" and (o.inParticipacao <> 'T') ");
		s.append(" and (o.inParticipacao = 'A') ");
		s.append("order by o.pessoa");
		Query q = EntityUtil.getEntityManager().createQuery(s.toString());
		q.setParameter("processo", ProcessoTrfHome.instance().getInstance());
		List<Pessoa> representantes = q.getResultList();

		return representantes;

	}

	@SuppressWarnings("unchecked")
	public List<Pessoa> getRepresentantesPassivos() {
		StringBuilder s = new StringBuilder();
		s.append("select o.pessoa from ProcessoParte o ");
		s.append("where (o.processoTrf = :processo) ");
		s.append(" and (o.inParticipacao <> 'T') ");
		s.append(" and (o.inParticipacao = 'P') ");
		s.append("order by o.pessoa");
		Query q = EntityUtil.getEntityManager().createQuery(s.toString());
		q.setParameter("processo", ProcessoTrfHome.instance().getInstance());
		List<Pessoa> representantes = q.getResultList();

		return representantes;

	}

	public int getParticipacoesTestemunhas(Pessoa pessoa, String participacao) {
		StringBuilder s = new StringBuilder();
		s.append("select o.pessoa from ProcessoAudienciaPessoa o ");
		s.append("where o.testemunha = true ");
		s.append("and o.pessoaRepresentante in (select p.pessoa from ProcessoParte p ");
		s.append("where (p.inParticipacao = :participacao)) ");
		s.append("and o.pessoa = :pessoa");

		Query q = EntityUtil.getEntityManager().createQuery(s.toString());
		q.setParameter("participacao", participacao);
		q.setParameter("pessoa", pessoa);

		return q.getResultList().size();
	}

	public String getUrlOpenner() {
		return this.urlOpenner;
	}

	public Boolean pesquisarCpf(Pessoa pessoa, ProcessoAudiencia audiencia) {
		Boolean ret = Boolean.FALSE;
		StringBuilder sb = new StringBuilder("select pap from ProcessoAudienciaPessoa pap ");
		sb.append("where pap.pessoa = :pessoa and pap.processoAudiencia = :audiencia");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("pessoa", pessoa);
		q.setParameter("audiencia", audiencia);

		if (q.getResultList().size() == 0) {
			ret = true;
		}
		return ret;
	}

	public void inserir() throws InstantiationException, IllegalAccessException {

		if (this.getInstance().getPessoaRepresentante() != null) {

			PreCadastroPessoaBean preBean = (PreCadastroPessoaBean) Component.getInstance("preCadastroPessoaBean");
			ProcessoAudiencia audiencia = ProcessoAudienciaHome.instance().getInstance();

			PessoaFisica pessoaFisica = preBean.getPessoaFisica();

			if ((pessoaFisica != null) && (pessoaFisica.getDataObito() != null)
					&& pessoaFisica.getDataObito().after(new Date())) {
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.ERROR, "A data não pode ser posterior a atual");
				return;
			}

			if (pesquisarCpf(preBean.getPessoa(), audiencia)) {
				this.getInstance().setParteOuvida(false);
				this.getInstance().setPessoa(pessoaFisica);
				this.getInstance().setPessoaRepresentante(this.getInstance().getPessoaRepresentante());
				this.getInstance().setProcessoAudiencia(audiencia);
				audiencia = EntityUtil.cloneEntity(audiencia, Boolean.FALSE);
				this.getInstance().setTestemunha(true);
				audiencia.getProcessoAudienciaPessoaList().add(this.getInstance());
				EntityUtil.getEntityManager().persist(this.getInstance());
				EntityUtil.flush();
			} else {
				FacesMessages.instance().add(Severity.ERROR, "Cpf já Cadastrado");
			}
			PessoaHome.instance().newInstance();
			PessoaFisicaHome.instance().newInstance();
			preBean.resetarBean();
		} else {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Escolha um Representante");
		}

		this.newInstance();
		ProcessoTrfHome.instance().setInstance(
				EntityUtil.find(ProcessoTrf.class, ProcessoHome.instance().getInstance().getIdProcesso()));
	}

	public void remover(ProcessoAudienciaPessoa testemunha) throws InstantiationException, IllegalAccessException {
		ProcessoAudiencia audiencia = ProcessoAudienciaHome.instance().getInstance();
		audiencia = EntityUtil.cloneEntity(audiencia, Boolean.FALSE);
		audiencia.getProcessoAudienciaPessoaList().remove(testemunha);
		EntityUtil.getEntityManager().remove(testemunha);
		EntityUtil.flush();

		PessoaHome.instance().newInstance();
		ProcessoTrfHome.instance().setInstance(
				EntityUtil.find(ProcessoTrf.class, ProcessoHome.instance().getInstance().getIdProcesso()));
	}

	@Override
	public void newInstance() {
		this.getInstance().setPessoa(new Pessoa());
		super.newInstance();
	}

	public void setCodInParticipacao(String codInParticipacao) {
		this.codInParticipacao = codInParticipacao;
	}

	public void setNovaInsercao(String novaInsercao) {
		this.novaInsercao = novaInsercao;
	}

	public void setRepresentante(Pessoa representante) {
		this.representante = representante;
	}

	public void setUrlOpenner(String urlOpenner) {
		this.urlOpenner = urlOpenner;
	}

	public void entrarPagina() {
		newInstance();
		setNovaInsercao("N");
		setTab("search");
		PessoaFisicaHome.instance().newInstance();
		PessoaJuridicaHome.instance().newInstance();
	}

}