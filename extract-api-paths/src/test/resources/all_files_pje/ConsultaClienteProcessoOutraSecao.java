package br.com.infox.pje.webservices;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.pje.webservice.consultaoutrasessao.BeanConsultaProcesso;
import br.com.infox.pje.webservice.consultaoutrasessao.BeanRespostaConsultaProcesso;
import br.com.infox.pje.webservice.consultaoutrasessao.ConsultaProcessoOutraSessao;
import br.com.infox.pje.webservice.consultaoutrasessao.ConsultaProcessoOutraSessaoService;
import br.com.itx.component.UrlUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.PrevencaoHostsWebservice;
import br.jus.pje.nucleo.util.DateUtil;

@Name(ConsultaClienteProcessoOutraSecao.NAME)
@Scope(ScopeType.CONVERSATION)
public class ConsultaClienteProcessoOutraSecao implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "consultaClienteProcessoOutraSecao";

	private static final String CONSULTA_PROCESSO_OUTRA_SESSAO_WSDL = "ConsultaProcessoOutraSessao?wsdl";
	private AssuntoTrf assuntoTrf;
	private ClasseJudicial classeJudicial;
	private String numeroOab;
	private String letraOab;
	private Estado ufOab;
	private String cpfCnpj;
	private Date dataFinal;
	private Date dataInicial;
	private String nomeParte;
	private String numeroProcesso;
	private Jurisdicao jurisdicao;
	private PrevencaoHostsWebservice hostValido;
	private static final LogProvider log = Logging.getLogProvider(ConsultaClienteProcessoOutraSecao.class);
	private List<String> erros = new ArrayList<String>();

	public BeanRespostaConsultaProcesso buscaProcessos() {
		BeanRespostaConsultaProcesso bean = new BeanRespostaConsultaProcesso();
		URL servidor = null;
		if (hostValido != null) {
			try {
				URL baseUrl;
				baseUrl = br.com.infox.pje.webservice.consultaoutrasessao.ConsultaProcessoOutraSessaoService.class
						.getResource(".");
				servidor = new URL(baseUrl, hostValido.getUrlServidor() + CONSULTA_PROCESSO_OUTRA_SESSAO_WSDL);
				ConsultaProcessoOutraSessaoService service = new ConsultaProcessoOutraSessaoService(servidor);
				ConsultaProcessoOutraSessao sessaoPort = service.getConsultaProcessoOutraSessaoPort();
				BeanConsultaProcesso beanConsultaProcesso = new BeanConsultaProcesso();

				if (getAssuntoTrf() != null) {
					beanConsultaProcesso.setCodigoAssunto(getAssuntoTrf().getCodAssuntoTrf());
				}
				if (getClasseJudicial() != null) {
					beanConsultaProcesso.setCodigoClasseJudicial(getClasseJudicial().getCodClasseJudicial());
				}
				beanConsultaProcesso.setNumeroOab(getNumeroOab());
				beanConsultaProcesso.setLetraOab(getLetraOab());
				beanConsultaProcesso.setUfOab(getUfOab());
				beanConsultaProcesso.setNomeParte(getNomeParte());
				beanConsultaProcesso.setNumeroProcesso(getNumeroProcesso());
				beanConsultaProcesso.setLink(hostValido.getUrlServidor());

				if (getDataInicial() != null) {
					beanConsultaProcesso.setDataInicial(DateUtil.getXMLGregorianCalendarFromDate(getDataInicial()));
				}
				if (getDataFinal() != null) {
					beanConsultaProcesso.setDataFinal(DateUtil.getXMLGregorianCalendarFromDate(getDataFinal()));
				}

				bean = sessaoPort.obterProcessos(beanConsultaProcesso);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bean;
	}

	public void pesquisa() {
		getHost();
		buscaProcessos();
	}

	private void getHost() {
		Criteria criteria = HibernateUtil.getSession().createCriteria(PrevencaoHostsWebservice.class);
		criteria.add(Restrictions.eq("host", jurisdicao.getJurisdicao()));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		PrevencaoHostsWebservice classe = (PrevencaoHostsWebservice)criteria.uniqueResult();
		hostValido = null;
		if (classe != null) {
			if (isUrlValida(classe)) {
				hostValido = classe;
			}
		}
	}

	private boolean isUrlValida(PrevencaoHostsWebservice phw) {
		InputStream inputStreamURL = null;
		try {
			inputStreamURL = UrlUtil
					.getInputStreamUrl(phw.getUrlServidor().concat(CONSULTA_PROCESSO_OUTRA_SESSAO_WSDL));
		} catch (IOException e) {
			String msgErro = MessageFormat.format("URL do Webservice não esta acessivel no estado {0}: {1}",
					phw.getHost(), e.getMessage());
			e.printStackTrace();
			log.error(msgErro);
			erros.add(msgErro);
		}
		return inputStreamURL != null;
	}

	public void newInstance() {
		setAssuntoTrf(null);
		setClasseJudicial(null);
		setNumeroOab(null);
		setLetraOab(null);
		setUfOab(null);
		setCpfCnpj(null);
		setDataFinal(null);
		setDataInicial(null);
		setNomeParte(null);
		setNumeroProcesso(null);
		setJurisdicao(null);
		setHostValido(null);
	}

	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public String getNumeroOab() {
		return numeroOab;
	}

	public void setNumeroOab(String numeroOab) {
		this.numeroOab = numeroOab;
	}

	public String getLetraOab() {
		return letraOab;
	}

	public void setLetraOab(String letraOab) {
		this.letraOab = letraOab;
	}

	public Estado getUfOab() {
		return ufOab;
	}

	public void setUfOab(Estado ufOab) {
		this.ufOab = ufOab;
	}

	public String getCpfCnpj() {
		return cpfCnpj;
	}

	public void setCpfCnpj(String cpfCnpj) {
		this.cpfCnpj = cpfCnpj;
	}

	public Date getDataFinal() {
		return dataFinal;
	}

	public void setDataFinal(Date dataFinal) {
		this.dataFinal = dataFinal;
	}

	public Date getDataInicial() {
		return dataInicial;
	}

	public void setDataInicial(Date dataInicial) {
		this.dataInicial = dataInicial;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public Jurisdicao getJurisdicao() {
		return jurisdicao;
	}

	public void setJurisdicao(Jurisdicao jurisdicao) {
		this.jurisdicao = jurisdicao;
	}

	public PrevencaoHostsWebservice getHostValido() {
		return hostValido;
	}

	public void setHostValido(PrevencaoHostsWebservice hostValido) {
		this.hostValido = hostValido;
	}

}
