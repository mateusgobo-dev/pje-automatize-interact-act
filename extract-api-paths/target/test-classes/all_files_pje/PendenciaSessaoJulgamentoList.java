package br.com.infox.pje.list;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.component.NumeroProcesso;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.SessaoComposicaoOrdem;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.enums.TipoPendenciaAssinaturaDocumentoEnum;
import br.jus.pje.nucleo.enums.TipoPendenciaEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaEnum;

@Name(PendenciaSessaoJulgamentoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PendenciaSessaoJulgamentoList extends EntityList<SessaoPautaProcessoTrf> {

	public static final String NAME = "pendenciaSessaoJulgamentoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "idSessaoPautaProcessoTrf";

	private TipoPendenciaEnum tipoPendencia;
	private TipoPendenciaAssinaturaDocumentoEnum tipoDocumento;
	private Date dataSessao;
	private Time horario;
	private Date horarioDate;
	private OrgaoJulgador orgaoJulgador;
	private NumeroProcesso numeroProcesso = new NumeroProcesso();
	private PessoaMagistrado magistrado;
	private Boolean exibeGrid = Boolean.FALSE;

	private static final String R1 = "sppt.processoTrf.numeroSequencia = #{pendenciaSessaoJulgamentoList.numeroProcesso.numeroSequencia}";
	private static final String R2 = "sppt.processoTrf.ano = #{pendenciaSessaoJulgamentoList.numeroProcesso.ano}";
	private static final String R3 = "sppt.processoTrf.numeroDigitoVerificador = #{pendenciaSessaoJulgamentoList.numeroProcesso.numeroDigitoVerificador}";
	private static final String R4 = "sppt.processoTrf.numeroOrgaoJustica = #{pendenciaSessaoJulgamentoList.numeroProcesso.numeroOrgaoJustica}";
	private static final String R5 = "sppt.processoTrf.numeroOrigem = #{pendenciaSessaoJulgamentoList.numeroProcesso.numeroOrigem}";
	private static final String R6 = "cast(sppt.sessao.dataSessao as date) = cast(#{pendenciaSessaoJulgamentoList.dataSessao} as date)";
	private static final String R7 = "sppt.sessao in (select s.sessao from SessaoComposicaoOrdem s where s.orgaoJulgador = #{pendenciaSessaoJulgamentoList.orgaoJulgador})";
	private static final String R8 = "sppt.processoTrf.orgaoJulgador in (select o.orgaoJulgador from UsuarioLocalizacaoMagistradoServidor o where o.usuarioLocalizacao.usuario.idUsuario = #{pendenciaSessaoJulgamentoList.magistrado.idUsuario})";
	private static final String R9 = "sppt.sessao.horarioInicio = #{pendenciaSessaoJulgamentoList.horario}";

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select sppt from SessaoPautaProcessoTrf sppt ");
		sb.append(filtroPendencias());
		return sb.toString();
	}

	/**
	 * Metodo que retorna as datas de sessões que possuem pendências cujo o
	 * campo dataFechamentoSessao deve ser nulo
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Date> getDatasPendencia() {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(sppt.sessao.dataSessao) from SessaoPautaProcessoTrf sppt ");
		sb.append(filtroPendencias());
		sb.append(" order by sppt.sessao.dataSessao");
		EntityManager em = EntityUtil.getEntityManager();
		Query q = em.createQuery(sb.toString());
		return q.getResultList();
	}

	/**
	 * Metodo que retorna horários de sessões que possuem pendências para aquela
	 * data escolhida
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Date> getHorasPendencia() {
		List<Date> resultSelectItemList = new ArrayList<Date>();
		if (getDataSessao() != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("select distinct s.horarioInicio from Sessao s ");
			sb.append("where cast(s.dataSessao as date) = cast(:dataSessao as date) ");
			sb.append("order by s.horarioInicio");
			EntityManager em = EntityUtil.getEntityManager();
			Query q = em.createQuery(sb.toString());
			q.setParameter("dataSessao", getDataSessao());
			// transforma os horários em selectItens
			resultSelectItemList = q.getResultList();
		}
		return resultSelectItemList;
	}

	public void limparCampos() {
		setTipoPendencia(null);
		setTipoDocumento(null);
		setDataSessao(null);
		setHorario(null);
		setNumeroProcesso(new NumeroProcesso());
		setOrgaoJulgador(null);
		setMagistrado(null);
	}

	/**
	 * Metodo que retorna todos os documentos de um determinado processo em uma
	 * determinada sessão
	 * 
	 * @param sppt
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<SessaoProcessoDocumento> getDocumentsFromProcess(SessaoPautaProcessoTrf sppt) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from SessaoProcessoDocumento o ");
		sb.append("where o.sessao = :sessao ");
		sb.append("and o.processoDocumento.ativo = true ");
		sb.append("and o.processoDocumento.processo.idProcesso = :processo");
		EntityManager em = EntityUtil.getEntityManager();
		Query q = em.createQuery(sb.toString());
		q.setParameter("sessao", sppt.getSessao());
		q.setParameter("processo", sppt.getProcessoTrf().getIdProcessoTrf());
		return q.getResultList();
	}

	/**
	 * Metodo utilizado para retornar Nome do magistrado presente no julgamento
	 * desse processo podendo ser o relator ou quem o representou na sessão
	 * 
	 * @param sessaoPautaProcessoTrf
	 * @return - nome do magsitrado ou relators
	 */
	public String filtroMagistrados(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		StringBuilder sb = new StringBuilder();
		sb.append("select scp from SessaoComposicaoOrdem scp ");
		sb.append("where scp.sessao = :sessao ");
		sb.append("and scp.orgaoJulgador = :oj ");
		EntityManager em = EntityUtil.getEntityManager();
		Query q = em.createQuery(sb.toString());
		q.setParameter("sessao", sessaoPautaProcessoTrf.getSessao());
		q.setParameter("oj", sessaoPautaProcessoTrf.getProcessoTrf().getOrgaoJulgador());
		SessaoComposicaoOrdem sco = EntityUtil.getSingleResult(q);
		String magistrado;
		if (sco == null) {
			magistrado = ProcessoTrfHome
					.instance()
					.getRelator(sessaoPautaProcessoTrf.getProcessoTrf().getOrgaoJulgador(),
							sessaoPautaProcessoTrf.getProcessoTrf().getOrgaoJulgadorColegiado()).getNome();
			return magistrado;
		} else if (sco.getMagistradoPresenteSessao() != null) {
			magistrado = sco.getMagistradoPresenteSessao().getNome();
			return magistrado;
		} else if (sco.getMagistradoSubstitutoSessao() != null) {
			magistrado = sco.getMagistradoSubstitutoSessao().getNome();
			return magistrado;
		} else {
			if(ProcessoTrfHome.instance().getRelator(sessaoPautaProcessoTrf.getProcessoTrf()) != null){
				magistrado = ProcessoTrfHome.instance().getRelator(sessaoPautaProcessoTrf.getProcessoTrf()).getNome();
			} else {
				magistrado = "";
			}
			return magistrado;
		}
	}

	/**
	 * Metodo que retorna o Nome do magistrado responsável pelo tipo de
	 * pendência Caso a pendência seja por falta de documento, então o
	 * responsável será o magistrado presente na sessão, Caso a pendência seja
	 * por falta de assinatura em documento, então o responsável será quem
	 * elaborou o documento
	 * 
	 * @param sessao
	 *            - parametro esperado para poder fazer a consulta em
	 *            sessaoProcessoDocumento a partir do processo e da sessao
	 * @return - o Nome do magistrado responsável pelo tipo de pendência
	 */
	public String nomeResponsavel(SessaoPautaProcessoTrf sessao) {
		String responsavel = "";
		List<SessaoProcessoDocumento> documentos = getDocumentsFromProcess(sessao);
		boolean[] pendencias = { true, true, true, true };
		if (documentos.size() > 0) {
			for (SessaoProcessoDocumento sessaoProcessoDocumento : documentos) {
				if (sessaoProcessoDocumento.getProcessoDocumento().getTipoProcessoDocumento()
						.equals(ParametroUtil.instance().getTipoProcessoDocumentoEmenta())) {
					pendencias[0] = false;
				} else if (sessaoProcessoDocumento.getProcessoDocumento().getTipoProcessoDocumento()
						.equals(ParametroUtil.instance().getTipoProcessoDocumentoRelatorio())) {
					pendencias[1] = false;
				} else if (sessaoProcessoDocumento.getProcessoDocumento().getTipoProcessoDocumento()
						.equals(ParametroUtil.instance().getTipoProcessoDocumentoVoto())) {
					pendencias[2] = false;
				} else if (sessaoProcessoDocumento.getProcessoDocumento().getProcessoDocumentoBin().getSignatarios().isEmpty()) {
					responsavel = sessaoProcessoDocumento.getProcessoDocumento().getUsuarioInclusao().getNome();
				} else {
					pendencias[3] = false;
				}
			}
			for (int i = 0; i < pendencias.length; i++) {
				if (i < 3) {
					if (pendencias[i] = true) {
						responsavel = filtroMagistrados(sessao);
					}
				}
			}
		} else {
			responsavel = filtroMagistrados(sessao);
		}
		return responsavel;
	}

	public String filtroPendencias() {
		OrgaoJulgador oj = Authenticator.getOrgaoJulgadorAtual();
		StringBuilder sb = new StringBuilder();
		sb.append("where sppt.sessao.dataFechamentoSessao is null ");
		if (oj != null && oj.getIdOrgaoJulgador() != 0) {
			sb.append("and sppt.sessao in (select distinct s.sessao from SessaoComposicaoOrdem s ");
			sb.append("where s.orgaoJulgador = #{authenticator.getOrgaoJulgadorAtual()})");
		}
		
		if (getOrgaoJulgador() != null){
 			sb.append("and sppt.processoTrf.orgaoJulgador = #{pendenciaSessaoJulgamentoList.orgaoJulgador} ");
		}
		
		//Não são considerados pendência processos excluídos da pauta
		sb.append(" and sppt.dataExclusaoProcessoTrf is null ");

		//São considerados como pendência somente processos julgados na sessão pesquisada
		sb.append(String.format("and sppt.situacaoJulgamento = '%s' ", TipoSituacaoPautaEnum.JG));
		
		//Não são considerados pendência processos que já possuem acórdão assinados na sessão pesquisada
		sb.append("and sppt.processoTrf.idProcessoTrf not in (select distinct spdaa.processoDocumento.processo.idProcesso from SessaoProcessoDocumento spdaa where spdaa.sessao = sppt.sessao ");
		sb.append("and spdaa.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoAcordao} ");
		sb.append("and spdaa.processoDocumento.ativo = true ");
		sb.append("and exists (select 1 from ProcessoDocumentoBinPessoaAssinatura a where a.processoDocumentoBin.idProcessoDocumentoBin = spdaa.processoDocumento.processoDocumentoBin.idProcessoDocumentoBin)) ");
		
		//Pendência: o processo não possui acórdão
		if (TipoPendenciaEnum.FA.equals(getTipoPendencia())) {
			sb.append("and sppt.processoTrf.idProcessoTrf not in (select distinct o.processoDocumento.processo.idProcesso from SessaoProcessoDocumento o where o.sessao = sppt.sessao ");
			sb.append("and o.processoDocumento.ativo = true ");
			sb.append("and o.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoAcordao}) ");
		/*
		 * Pendência: o processo não possui voto
		 * 
		 * Problema: há vários tipos de voto, a pesquisa de pendência deveria pesquisar somente processos que não possui o voto do relator
		 * cadastrado. Se pesquisar na tabela SessaoProcessoDocumentoVoto, o sistema entenderá que o processo n terá pendência se qqr voto
		 * for cadastrado, inclusive "Com o relator", isso gera muito falso negativo. Nem todo registro de SessaoProcessoDocumentoVoto 
		 * corresponde a um registro em SessaoProcessoDocumento. Se a busca for feita na tabela SessaoProcessoDocumento, o sistema 
		 * reduzirá a quantidade de falso negativo.
		 */
			
		} else if (TipoPendenciaEnum.FV.equals(getTipoPendencia())) { 
			sb.append("and sppt.processoTrf.idProcessoTrf not in (select distinct o.processoDocumento.processo.idProcesso from SessaoProcessoDocumento o where o.sessao = sppt.sessao ");
			sb.append("and o.processoDocumento.ativo = true ");
			sb.append("and o.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoVoto}) ");
		//Pendência: o processo não possui ementa
		} else if (TipoPendenciaEnum.FE.equals(getTipoPendencia())) {
			sb.append("and sppt.processoTrf.idProcessoTrf not in (select distinct o.processoDocumento.processo.idProcesso from SessaoProcessoDocumento o where o.sessao = sppt.sessao ");
			sb.append("and o.processoDocumento.ativo = true ");
			sb.append("and o.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoEmenta}) ");
		//Pendência: o processo não possui relatório
		} else if (TipoPendenciaEnum.FR.equals(getTipoPendencia())) {
			sb.append("and sppt.processoTrf.idProcessoTrf not in (select distinct o.processoDocumento.processo.idProcesso from SessaoProcessoDocumento o where o.sessao = sppt.sessao ");
			sb.append("and o.processoDocumento.ativo = true ");
			sb.append("and o.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoRelatorio}) ");

		} else if (TipoPendenciaEnum.FD.equals(getTipoPendencia())) {
				sb.append("and sppt.processoTrf.idProcessoTrf in (select distinct o.processoDocumento.processo.idProcesso from SessaoProcessoDocumento o where o.sessao = sppt.sessao ");
				sb.append(" and o.processoDocumento.ativo = true ");
				sb.append(" and not exists (select 1 from ProcessoDocumentoBinPessoaAssinatura a where a.processoDocumentoBin.idProcessoDocumentoBin = o.processoDocumento.processoDocumentoBin.idProcessoDocumentoBin) ");
			if (TipoPendenciaAssinaturaDocumentoEnum.VO.equals(getTipoDocumento())) {
			//Pendência: o processo não possui voto assinado
				sb.append(" and o.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoVoto}) ");
			} else if (TipoPendenciaAssinaturaDocumentoEnum.RE.equals(getTipoDocumento())) {
			//Pendência: o processo não possui relatório assinado
				sb.append(" and o.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoRelatorio}) ");
			} else if (TipoPendenciaAssinaturaDocumentoEnum.EM.equals(getTipoDocumento())) {
			//Pendência: o processo não possui ementa assinada
				sb.append(" and o.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoEmenta}) ");
			} else { 
				sb.append(" and o.processoDocumento.tipoProcessoDocumento in ( #{parametroUtil.tipoProcessoDocumentoEmenta}, #{parametroUtil.tipoProcessoDocumentoRelatorio}, #{parametroUtil.tipoProcessoDocumentoVoto}, #{parametroUtil.tipoProcessoDocumentoAcordao})) ");
			}
		//Processo possui pendência
		} else { 
			sb.append("and (sppt.processoTrf.idProcessoTrf not in (select distinct spda.processoDocumento.processo.idProcesso from SessaoProcessoDocumento spda where spda.sessao = sppt.sessao ");
			sb.append(" and spda.processoDocumento.ativo = true ");
			sb.append("	and spda.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoAcordao}) ");
			
			sb.append("or sppt.processoTrf.idProcessoTrf not in (select distinct spdv.processoDocumento.processo.idProcesso from SessaoProcessoDocumento spdv where spdv.sessao = sppt.sessao ");
			sb.append(" and spdv.processoDocumento.ativo = true ");
			sb.append(" and spdv.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoVoto}) ");

			sb.append("or sppt.processoTrf.idProcessoTrf not in (select distinct spde.processoDocumento.processo.idProcesso from SessaoProcessoDocumento spde where spde.sessao = sppt.sessao ");
			sb.append(" and spde.processoDocumento.ativo = true ");
			sb.append(" and spde.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoEmenta}) ");

			sb.append("or sppt.processoTrf.idProcessoTrf not in (select distinct spdr.processoDocumento.processo.idProcesso from SessaoProcessoDocumento spdr where spdr.sessao = sppt.sessao ");
			sb.append(" and spdr.processoDocumento.ativo = true ");
			sb.append(" and spdr.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoRelatorio}) ");

			sb.append("or sppt.processoTrf.idProcessoTrf in (select distinct spdn.processoDocumento.processo.idProcesso from SessaoProcessoDocumento spdn where spdn.sessao = sppt.sessao ");
			sb.append(" and spdn.processoDocumento.ativo = true ");
			sb.append(" and spdn.processoDocumento.tipoProcessoDocumento in ( #{parametroUtil.tipoProcessoDocumentoEmenta}, #{parametroUtil.tipoProcessoDocumentoRelatorio}, #{parametroUtil.tipoProcessoDocumentoVoto}) ");
			sb.append(" and not exists (select 1 from ProcessoDocumentoBinPessoaAssinatura a where a.processoDocumentoBin.idProcessoDocumentoBin = spdn.processoDocumento.processoDocumentoBin.idProcessoDocumentoBin)) ");
			sb.append(")");
		}

		return sb.toString();
	}

	/**
	 * Metodo utilizado para exibir a descrição da pendencia do processo em uma
	 * determinada sessão
	 * 
	 * @param sppt
	 *            - parametro utilizado para a pesquisa do(s) referido(s)
	 *            documento(s)
	 * @return - descrição da pendencia
	 */
	public String buscaPendencia(SessaoPautaProcessoTrf sppt) {
		List<SessaoProcessoDocumento> spd = getDocumentsFromProcess(sppt);
		String ret = getTipoPendencia(spd);
		return ret;
	}

	/**
	 * Metodo que recebe a lista de documentos de um processo em uma sessão e
	 * retorna a(s) sua(s) pendencia
	 * @param spd
	 * @return
	 */
	public String getTipoPendencia(List<SessaoProcessoDocumento> spd) {
		StringBuilder pendencia = new StringBuilder();
		String[] pendencias = { "Falta de acórdão <br/>","Falta de ementa <br/>", "Falta de relatório <br/>", "Falta de voto <br/>","" };
		if (spd.size() > 0) {
			for (SessaoProcessoDocumento sessaoProcessoDocumento : spd) {
				if (sessaoProcessoDocumento.getProcessoDocumento().getTipoProcessoDocumento()
					.equals(ParametroUtil.instance().getTipoProcessoDocumentoAcordao())) {
					pendencias[0] = "";
				} else if (sessaoProcessoDocumento.getProcessoDocumento().getTipoProcessoDocumento()
						.equals(ParametroUtil.instance().getTipoProcessoDocumentoEmenta())) {
					pendencias[1] = "";
				} else if (sessaoProcessoDocumento.getProcessoDocumento().getTipoProcessoDocumento()
						.equals(ParametroUtil.instance().getTipoProcessoDocumentoRelatorio())) {
					pendencias[2] = "";
				} else if (sessaoProcessoDocumento.getProcessoDocumento().getTipoProcessoDocumento()
						.equals(ParametroUtil.instance().getTipoProcessoDocumentoVoto())) {
					pendencias[3] = "";
				} 
				/*
				 * PJEII-21785 - Como pode haver um processo que possua dois votos, um assinado e outro não, 
				 * é necessário que a informação de falta de assinatura seja adicionada e não excluída para
				 * que a assinatura de um voto não impeça a exibição da pendência do outro.
				 */
				if (sessaoProcessoDocumento.getProcessoDocumento().getProcessoDocumentoBin().getSignatarios().isEmpty()) {
					pendencias[4] = "Falta de assinatura em documento <br/>";
				}
			}
		}
		pendencia.append(pendencias[0]);
		pendencia.append(pendencias[1]);
		pendencia.append(pendencias[2]);
		pendencia.append(pendencias[3]);
		pendencia.append(pendencias[4]);
		return pendencia.toString();	
	}

	@Override
	public List<SessaoPautaProcessoTrf> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}

	@Override
	protected void addSearchFields() {
		addSearchField("processoTrf.numeroSequencia", SearchCriteria.igual, R1);
		addSearchField("processoTrf.ano", SearchCriteria.igual, R2);
		addSearchField("processoTrf.numeroDigitoVerificador", SearchCriteria.igual, R3);
		addSearchField("processoTrf.numeroOrgaoJustica", SearchCriteria.igual, R4);
		addSearchField("processoTrf.numeroOrigem", SearchCriteria.igual, R5);
		addSearchField("sessao.dataSessao", SearchCriteria.igual, R6);
		addSearchField("sessao", SearchCriteria.igual, R7);
		addSearchField("processoTrf", SearchCriteria.igual, R8);
		addSearchField("sessao.orgaoJulgadorColegiadoSalaHorario.horaInicial", SearchCriteria.igual, R9);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("numeroProcesso", "processoTrf.processo.numeroProcesso");
		map.put("orgaoJulgador", "sessaoPautaProcessoTrf.sessaoComposicaoOrdem.orgaoJulgador");
		return map;
	}

	public String dataFormatada(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		String data = null;
		if (date != null) {
			data = format.format(date);
		}
		return data;

	}

	public void pesquisa() {
		if (getDataSessao() != null) {
			setExibeGrid(Boolean.TRUE);
		} else {
			setExibeGrid(Boolean.FALSE);
		}
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	public TipoPendenciaEnum getTipoPendencia() {
		return tipoPendencia;
	}

	public void setTipoPendencia(TipoPendenciaEnum tipoPendencia) {
		this.tipoPendencia = tipoPendencia;
	}

	public TipoPendenciaAssinaturaDocumentoEnum getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(TipoPendenciaAssinaturaDocumentoEnum tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public TipoPendenciaEnum[] getTipoPendenciaValues() {
		return TipoPendenciaEnum.values();
	}

	public TipoPendenciaAssinaturaDocumentoEnum[] getTipoDocumentoValues() {
		return TipoPendenciaAssinaturaDocumentoEnum.values();
	}

	public Date getDataSessao() {
		return dataSessao;
	}

	public void setDataSessao(Date dataSessao) {
		this.dataSessao = dataSessao;
	}

	public Time getHorario() {
		return horario;
	}

	public void setHorario(Time horario) {
		this.horario = horario;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public NumeroProcesso getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(NumeroProcesso numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public PessoaMagistrado getMagistrado() {
		return magistrado;
	}

	public void setMagistrado(PessoaMagistrado magistrado) {
		this.magistrado = magistrado;
	}

	public Boolean getExibeGrid() {
		return exibeGrid;
	}

	public void setExibeGrid(Boolean exibeGrid) {
		this.exibeGrid = exibeGrid;
	}

	@Override
	public void newInstance() {
		super.newInstance();
		limparCampos();
		EntityUtil.getEntityManager().clear();
	}

	public void setHorarioDate(Date horarioDate) {
		if (horarioDate != null) {
			Time time = new Time(horarioDate.getTime());
			this.horarioDate = horarioDate;
			setHorario(time);
		} else {
			this.horarioDate = null;
			setHorario(null);
		}
	}

	public Date getHorarioDate() {
		return horarioDate;
	}
}
