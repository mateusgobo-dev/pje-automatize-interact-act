/**  
 * Bean contendo os métodos que irão fornecer e receber dados para o sistema AUD.
 * Trata a exportação e importação do AUD promovendo a camada de interface entre o WebService e a persistência do PJE.
 *
 * @authors Gabriel Azevedo, Thiago Shiono, Bernardo A. Gouvêa
 * @since 1.4.3
 * @see 
 * @category PJE-JT
 * 
 * */

package br.jus.csjt.pje.business.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;

import br.com.infox.cliente.home.AudImportacaoHome;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.csjt.pje.commons.exception.IntegracaoAudException;
import br.jus.pje.jt.entidades.AudAdvogados;
import br.jus.pje.jt.entidades.AudAutor;
import br.jus.pje.jt.entidades.AudConf;
import br.jus.pje.jt.entidades.AudEspecie;
import br.jus.pje.jt.entidades.AudImportacao;
import br.jus.pje.jt.entidades.AudJuizes;
import br.jus.pje.jt.entidades.AudOrgaoMunicipio;
import br.jus.pje.jt.entidades.AudParte;
import br.jus.pje.jt.entidades.AudParteImportacao;
import br.jus.pje.jt.entidades.AudPauta;
import br.jus.pje.jt.entidades.AudPeritos;
import br.jus.pje.jt.entidades.AudReu;
import br.jus.pje.jt.entidades.AudTipoVerba;
import br.jus.pje.jt.entidades.AudVerbaImportacao;
import br.jus.pje.jt.entidades.TipoVerba;
import br.jus.pje.jt.enums.SolucaoSentencaAudEnum;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoAudiencia;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ResultadoSentenca;
import br.jus.pje.nucleo.entidades.ResultadoSentencaParte;
import br.jus.pje.nucleo.entidades.SolucaoSentenca;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;

@Name(IntegracaoAudBean.NAME)
@Scope(ScopeType.CONVERSATION)
public class IntegracaoAudBean implements Serializable, IntegracaoAudBeanRemote {

	private class AudParteProcesso {
		
		private Integer processoAudienciaId;
		private Integer parteId;
		
		public AudParteProcesso(Integer processoAudienciaId, Integer parteId) {
			this.processoAudienciaId = processoAudienciaId;
			this.parteId = parteId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((parteId == null) ? 0 : parteId.hashCode());
			result = prime
					* result
					+ ((processoAudienciaId == null) ? 0 : processoAudienciaId
							.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AudParteProcesso other = (AudParteProcesso) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (parteId == null) {
				if (other.parteId != null)
					return false;
			} else if (!parteId.equals(other.parteId))
				return false;
			if (processoAudienciaId == null) {
				if (other.processoAudienciaId != null)
					return false;
			} else if (!processoAudienciaId.equals(other.processoAudienciaId))
				return false;
			return true;
		}

		private IntegracaoAudBean getOuterType() {
			return IntegracaoAudBean.this;
		}
	}
	
	public static final String NAME = "integracaoAudBean";

	private static final long serialVersionUID = 1L;
	private static final String QUERY_PERITOS = "select p from AudPeritos p";
	private static final String QUERY_ADVOGADOS = "select adv from AudAdvogados adv";
	private static final String QUERY_JUIZES = "select j from AudJuizes j";
	private static final String QUERY_ESPECIE = "select e from AudEspecie e";
	private static final String QUERY_PAUTA = "select pauta from AudPauta pauta where pauta.dataAudiencia between :dataInicio and :dataFim and pauta.idOrgaoJulgador = :idOrgaoJulgador";
	private static final String QUERY_AUTOR = "select aa from AudAutor aa where aa.idProcesso in (:listaProcessoTRF)";
	private static final String QUERY_REU = "select ar from AudReu ar where ar.idProcesso in (:listaProcessoTRF)";
	private static final String QUERY_AUD_CONF = "select ac from AudConf ac";
	private static final String QUERY_AUD_CONF_MUNICIPIO = "select ac from AudConf ac where ac.idMunicipio = :idMunicipio";
	private static final String QUERY_ORGAO_MUNICIPIO = "select om from AudOrgaoMunicipio om where om.orgaoJulgadorAtivo = true ";
	private static final String QUERY_TIPO_VERBA = "select tv from AudTipoVerba tv";
	private static final String QUERY_ORGAO_JULGADOR = "select new br.jus.csjt.pje.business.service.AudOrgaoJulgador(oj.idOrgaoJulgador,oj.orgaoJulgador,oj.sigla) from OrgaoJulgador oj";
	
	@In
	private EntityManager entityManager = EntityUtil.getEntityManager();

	@SuppressWarnings("unchecked")
	public List<AudOrgaoMunicipio> listarOrgaoMunicipios() {
		return entityManager.createQuery(QUERY_ORGAO_MUNICIPIO).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<AudPeritos> listarPeritos() {
		return entityManager.createQuery(QUERY_PERITOS).getResultList();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<AudAdvogados> listarAdvogados() {
		return entityManager.createQuery(QUERY_ADVOGADOS).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<AudJuizes> listarJuizes() {
		return entityManager.createQuery(QUERY_JUIZES).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<AudEspecie> listarEspecie() {
		return entityManager.createQuery(QUERY_ESPECIE).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<AudPauta> listarPauta(Date dataInicio, Date dataFim, int idOrgaoJulgador) {
		dataInicio = DateUtil.getBeginningOfDay(dataInicio);
		dataFim = DateUtil.getEndOfDay(dataFim);
		Query query = entityManager.createQuery(QUERY_PAUTA);
		query.setParameter("dataInicio", dataInicio);
		query.setParameter("dataFim", dataFim);
		query.setParameter("idOrgaoJulgador", idOrgaoJulgador);
		List<AudPauta> listaPauta = query.getResultList();
		for (AudPauta audPauta : listaPauta) {
			audPauta.setOrgaoJustica(getJustica(audPauta));
			audPauta.setRegional(getRegional(audPauta));
		}
		return listaPauta;
	}

	@SuppressWarnings("unchecked")
	public List<AudConf> listarAudConf() {
		return entityManager.createQuery(QUERY_AUD_CONF).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<AudConf> listarAudConf(int idMunicipio) {
		Query query = entityManager.createQuery(QUERY_AUD_CONF_MUNICIPIO);
		query.setParameter("idMunicipio", idMunicipio);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<AudAutor> listarAutor(List<AudPauta> pautas) {
		List<Integer> listaProcessoTRF = new ArrayList<Integer>();
		for (AudPauta audPauta : pautas) {
			listaProcessoTRF.add(audPauta.getIdProcesso());
		}
		Query query = entityManager.createQuery(QUERY_AUTOR);
		query.setParameter("listaProcessoTRF", Util.isEmpty(listaProcessoTRF)?null:listaProcessoTRF);
		List<AudAutor> autores = query.getResultList();
		removerParteDuplicada(autores.iterator());
		
		return autores;
	}

	@SuppressWarnings("unchecked")
	public List<AudReu> listarReu(List<AudPauta> pautas) {
		List<Integer> listaProcessoTRF = new ArrayList<Integer>();
		for (AudPauta audPauta : pautas) {
			listaProcessoTRF.add(audPauta.getIdProcesso());
		}
		Query query = entityManager.createQuery(QUERY_REU);
		query.setParameter("listaProcessoTRF", Util.isEmpty(listaProcessoTRF)?null:listaProcessoTRF);
		List<AudReu> reus = query.getResultList();
		removerParteDuplicada(reus.iterator());
		
		return reus;
	}

	/**
	 * Foi solicitado para que o PJe retorne somente o primeiro advogado de cada
	 * uma das partes. Como no futuro o AUD tratará essa possibilidade fiz essa
	 * modificação via código.
	 * 
	 * Verifico se a parte já está presente (feita através da combinação do id
	 * da audiência e da parte) e caso já esteja removo as próximas entradas que
	 * seriam os outros advogados.
	 * 
	 * @param itParte
	 */
	private void removerParteDuplicada(Iterator<? extends AudParte> itParte) {
		Set<AudParteProcesso> partes = new HashSet<AudParteProcesso>();
		
		while(itParte.hasNext()) {
			AudParte parte = itParte.next();
			
			AudParteProcesso parteProcesso = new AudParteProcesso(parte.getId(), parte.getIdProcessoParte());
			
			if(partes.contains(parteProcesso)) {
				itParte.remove();
			} else {
				partes.add(parteProcesso);
			}
		}
	}
	
	public int getJustica(AudPauta audPauta) {
		String justica = audPauta.getOrgaoJustica() + "";
		return Integer.parseInt(justica.substring(0, 1));
	}

	public int getRegional(AudPauta audPauta) {
		String regional = audPauta.getRegional() + "";
		return Integer.parseInt(regional.substring(1));
	}

	public String getNumCNJ(AudPauta audPauta) {
		String cnj = "";

		cnj = StringUtil.completaZeros(audPauta.getNumeroProcesso() + "", 7) + "-"
				+ StringUtil.completaZeros(audPauta.getDv() + "", 2) + "."
				+ StringUtil.completaZeros(audPauta.getAnoProcesso() + "", 4) + "." + audPauta.getOrgaoJustica() + "."
				+ StringUtil.completaZeros(audPauta.getRegional() + "", 2) + "."
				+ StringUtil.completaZeros(audPauta.getOrigemProcesso() + "", 4);
		return cnj;
	}

	/**
	 * Realiza o lançamento das sentenças de acordo com os dados vindos do AUD.
	 * 
	 * @param ai
	 *            Registro de importação vindo do AUD
	 * @param solucaoUnica
	 *            Informa se é uma solução única para todas as partes
	 * @param solucaoSentenca
	 *            Solução Sentença a ser lançada
	 * @param resultadoSentencaParteDiferenciado
	 *            Se não for uma solução única, enviar o resultado da sentença
	 *            de cada parte
	 */
	private void lancarResultadoSentenca(AudImportacao ai, Boolean sentencaLiquida, Boolean solucaoUnica,
			SolucaoSentenca solucaoSentenca, BigDecimal valorCondenacao, BigDecimal valorCustasDispensadas,
			BigDecimal valorCustasArrecadar, Boolean assistenciaJudicialGratuita,
			ResultadoSentencaParte resultadoSentencaParteDiferenciado) {

		// Cria Objeto Resultado Sentenca
		ResultadoSentenca resultadoSentenca = new ResultadoSentenca();
		ProcessoTrf processoTrf = entityManager.find(ProcessoTrf.class, ai.getIdProcesso());
		resultadoSentenca.setProcessoTrf(processoTrf);
		resultadoSentenca.setDataSentenca(ai.getDtInicio());
		resultadoSentenca.setHomologado(Boolean.FALSE);
		resultadoSentenca.setSentencaLiquida(sentencaLiquida);
		resultadoSentenca.setSolucaoUnica(solucaoUnica);

		ResultadoSentencaService rss = ComponentUtil.getComponent(ResultadoSentencaService.NAME);

		if (solucaoUnica) {
			rss.gravarResultadoSentencaParteUnico(resultadoSentenca, solucaoSentenca, valorCondenacao,
					valorCustasDispensadas, valorCustasArrecadar, assistenciaJudicialGratuita);
		} else {
			resultadoSentencaParteDiferenciado.setResultadoSentenca(resultadoSentenca);
			resultadoSentencaParteDiferenciado.setValorCondenacao(valorCondenacao);
			resultadoSentencaParteDiferenciado.setValorCustasArrecadar(valorCustasArrecadar);
			resultadoSentencaParteDiferenciado.setValorCustasDispensadas(valorCustasDispensadas);
			resultadoSentencaParteDiferenciado.setAssistenciaJudicialGratuita(assistenciaJudicialGratuita);
			resultadoSentencaParteDiferenciado.setSolucaoSentenca(solucaoSentenca);
			rss.gravarResultadoSentencaParteDiferenciado(resultadoSentencaParteDiferenciado);
		}
	}

	/**
	 * Analisa os dados que vieram do AUD para determinar se algum resultado
	 * sentença deve ser lançado e apresentado para o usuário durante a
	 * verificação da audiência.
	 * 
	 * @param ai
	 *            Registro de importação vindo do AUD
	 */
	private void registrarResultadosSentenca(AudImportacao ai) {
		AudImportacaoHome audImportacaoHome = ComponentUtil.getComponent("audImportacaoHome");
		audImportacaoHome.setInstance(ai);

		String sql = "SELECT ss FROM SolucaoSentenca ss WHERE solucaoSentencaAud = :solucaoSentencaAudEnum";
		Query query = EntityUtil.getEntityManager().createQuery(sql);

		double valorCausa = ai.getValorCausa() != null ? ai.getValorCausa() : 0;
		double valorCustasDispensadas = 0;
		double valorCustasArrecadar = 0;
		
		boolean autorIsento = ai.getAutorIsento() != null && ai.getAutorIsento().equals("S");
		boolean reuIsento = ai.getReuIsento() != null && ai.getReuIsento().equals("S");
		// Caso o autor seja isento, marca como assistência jud. gratuita
		Boolean assistenciaJudiciariaGratuita = autorIsento; 
		
		if (autorIsento && reuIsento) {
			// Se ambos polos forem isentos, somar as custas como dispensadas.
			valorCustasDispensadas = ai.getValorcustasAutor() + ai.getValorcustasReu();
		} else if (!autorIsento && !reuIsento) {
			// Se ambos polos não forem isentos, somar as custas como a
			// arrecadar.
			double custasAutor = ai.getValorcustasAutor() == null ? 0 : ai.getValorcustasAutor();
			double custasReu = ai.getValorcustasReu() == null ? 0 : ai.getValorcustasReu();
			
			valorCustasArrecadar = custasAutor + custasReu;
		} else if ((autorIsento && !reuIsento) || (!autorIsento && reuIsento)) {
			// As custas são dispensadas para aquele q for isento e serão
			// arrecadadas para aquele q não for isento.
			valorCustasDispensadas = autorIsento ? ai.getValorcustasAutor() : ai.getValorcustasReu();
			valorCustasArrecadar = !autorIsento ? ai.getValorcustasAutor() : ai.getValorcustasReu();
		}

		// Testando o que veio do AUD para determinar qual sentença deverá ser
		// lançada. 
		//[PJEII-2673] [CSJT] Bernardo Gouvêa - Incluindo valor de acordo igual a zero, pois existem acordos homologados que não envolvem pagamento monetário
		if (ai.getValorAcordo() != null && ai.getValorAcordo() >= 0) {
			// Lançar resultado sentença para acordo
			query.setParameter("solucaoSentencaAudEnum", SolucaoSentencaAudEnum.ACO);
			SolucaoSentenca solucaoSentenca = (SolucaoSentenca) query.getSingleResult();

			lancarResultadoSentenca(ai, false, true, solucaoSentenca, new BigDecimal(ai.getValorAcordo()),
					new BigDecimal(valorCustasDispensadas), new BigDecimal(valorCustasArrecadar), assistenciaJudiciariaGratuita, null);
		} else if (ai.getDesistencia() != null && ai.getDesistencia().equalsIgnoreCase("TOTAL")) {
			// Lançar resultado sentença para desistência TOTAL
			query.setParameter("solucaoSentencaAudEnum", SolucaoSentencaAudEnum.DET);
			SolucaoSentenca solucaoSentenca = (SolucaoSentenca) query.getSingleResult();
			
			lancarResultadoSentenca(ai, false, true, solucaoSentenca, new BigDecimal(valorCausa), new BigDecimal(
					valorCustasDispensadas), new BigDecimal(valorCustasArrecadar), assistenciaJudiciariaGratuita, null);
		} else if (ai.getIncompetencia() != null && ai.getIncompetencia().equalsIgnoreCase("acolhida")) {
			// Lançar resultado sentença para Acolhimento exceção Incompetência
			query.setParameter("solucaoSentencaAudEnum", SolucaoSentencaAudEnum.AEC);
			SolucaoSentenca solucaoSentenca = (SolucaoSentenca) query.getSingleResult();
			assistenciaJudiciariaGratuita = null;
			
			lancarResultadoSentenca(ai, false, true, solucaoSentenca, new BigDecimal(valorCausa), new BigDecimal(
					valorCustasDispensadas), new BigDecimal(valorCustasArrecadar), assistenciaJudiciariaGratuita, null);
		} else if (audImportacaoHome.verificaAusenciaTotalPoloAtivo()) {
			// Lançar resultado sentença para Arquivamento Artigo 844 (ausência
			// reclamante)
			// Total
			query.setParameter("solucaoSentencaAudEnum", SolucaoSentencaAudEnum.ART);
			SolucaoSentenca solucaoSentenca = (SolucaoSentenca) query.getSingleResult();

			lancarResultadoSentenca(ai, false, true, solucaoSentenca, new BigDecimal(valorCausa), new BigDecimal(
					valorCustasDispensadas), new BigDecimal(valorCustasArrecadar), assistenciaJudiciariaGratuita, null);
		} else if (audImportacaoHome.verificaAusenciaParcialPoloAtivo()) {
			// Lançar resultado sentença para Arquivamento Artigo 844 (ausência
			// reclamante)
			// Diferenciado
			query.setParameter("solucaoSentencaAudEnum", SolucaoSentencaAudEnum.ARP);
			SolucaoSentenca solucaoSentenca = (SolucaoSentenca) query.getSingleResult();

			for (AudParteImportacao audParteAtivaAusente : audImportacaoHome.obtemListaParteAtivaAusente()) {
				ResultadoSentencaParte resultadoSentencaParte = new ResultadoSentencaParte();
				// Obtém ProcessoParte
				ProcessoParte processoParte = entityManager.find(ProcessoParte.class,
						audParteAtivaAusente.getIdProcessoParte());
				resultadoSentencaParte.setProcessoParte(processoParte);
				lancarResultadoSentenca(ai, false, false, solucaoSentenca, new BigDecimal(valorCausa), new BigDecimal(
						valorCustasDispensadas), new BigDecimal(valorCustasArrecadar), false, resultadoSentencaParte);
			}
		}
	}

	/**
	 * Grava registro de importação do AUD nas tabelas de importação. Testa se o
	 * registro já existe nas tbs de importação do PJE, se existir não
	 * sobreescreve. Não grava se o registro já tiver sido confirmado no sistema
	 * (gravado nas tabelas definitivas do PJE)
	 * 
	 * @throws IntegracaoAudException
	 *             Exceções baseadas em consistências necessárias à integração
	 *             do AUD com o PJE.
	 * @param ai
	 *            Registro de importação vindo do AUD (Resultado.xml)
	 */
	@Transactional
	public void setAudImportacao(AudImportacao ai) throws IntegracaoAudException {
		ProcessoTrfHome processoTrfHome = ComponentUtil.getComponent("processoTrfHome");
		ProcessoTrf processoTrf = null;
 
		validacoes(ai);
		
		try {
			processoTrf = processoTrfHome.carregarProcesso(ai);
		} catch(Exception e) {
			throw new IntegracaoAudException("O processo não foi encontrado na base de dados do PJe.");
		}
		
		// Consistência: não gravar se o registro não contiver as chaves dos
		// campos do pje (proc. não pertencente ao PJE)
		if (processoTrf != null) {
			if (ai.getIdProcessoAudiencia() != null
					&& entityManager.find(ProcessoAudiencia.class, ai.getIdProcessoAudiencia()) != null) {
				// Consistência: verificar se o processo já está em
				// AudImportação para sobrescrevê-lo.
				String sql = "select a from AudImportacao a " + "where  a.idProcesso =  " + ai.getIdProcesso()
						+ " and   a.idProcessoAudiencia = " + ai.getIdProcessoAudiencia();
				Query query = EntityUtil.getEntityManager().createQuery(sql);
				AudImportacao audImportacaoPersistido;

				// Consistência: verificar se já foi registrado um resultado
				// sentença para sobrescrevê-lo.
				ResultadoSentencaService resultadoSentencaService = ComponentUtil.getComponent(ResultadoSentencaService.NAME);
				ResultadoSentenca resultadoSentencaPersistido = resultadoSentencaService.getResultadoSentenca(processoTrf);
				
				/*
				 * Aud está enviando as verbas referentes à audiência com id 
				 * (id da verba na view) sendo que este id deve ser null
				 * para que o hibernate gere a chave.
				 */
				if(ai.getAudVerbaImportacao() != null) {
					Iterator<AudVerbaImportacao> it = ai.getAudVerbaImportacao().iterator();
					 
					while(it.hasNext()) {
						AudVerbaImportacao verba = it.next();
						
						if(verba == null) {
							it.remove();
							continue;
						}
						
						if(verba.getIdVerbaImportacao() != null) {
							verba.setIdVerbaImportacao(null);
						}
					}
					
					verificarAdicionarVerbas(ai.getAudVerbaImportacao());
				}
				
				try {
					audImportacaoPersistido = (AudImportacao) query.getSingleResult();

					// Consistência: não gravar se o registro de importação já estiver com a data de cosolidação gravada
					// Esta data indica que o processo de validação já está completo, já foi validado pelo secretário de audiência e não  tem mais possibilidade de alteração
					if (ai.getDtValidacao() == null && (audImportacaoPersistido == null || audImportacaoPersistido.getDtValidacao() == null)) {
						entityManager.persist(ai);
						// Lança Sentença Solução Diferenciada para os autores ausentes caso não haja ausência total.
						entityManager.flush();
					} else {
						throw new IntegracaoAudException(IntegracaoAudException.REGISTRO_VALIDADO);
					}

					// Removendo o registro que já estava persistido devido
					// necessidade de garantir que as listas do objeto
					// AudImportacao sejam removidas do banco.
					entityManager.remove(audImportacaoPersistido);
					
					//Um resultado de sentença só deve ser removido se já tiver um registro de resultado sentença para o processo que está sendo re-enviado pelo AUD.
					if (resultadoSentencaPersistido != null) {
						entityManager.remove(resultadoSentencaPersistido);
					}

					ai = entityManager.merge(ai);
				} catch (javax.persistence.NoResultException e) {
					// Se o registro não foi encontrado, simplesmente continuar
					// a execução normalmente
				}
				
				entityManager.persist(ai);
				entityManager.flush();
				
			} else {
				// Caso a audiência seja de conciliação e não exista no PJe ela
				// deverá ser criada
				if (ai.getValorAcordo() != null && ai.getValorAcordo() > 0) {
					// Consistência: verificar se o processo já está em
					// AudImportação para não importar novamente
					String sql = "select a from AudImportacao a where a.idProcesso = :id and a.dtInicio = :dtInicio";

					Query query = EntityUtil.getEntityManager().createQuery(sql);
					query.setParameter("id", ai.getIdProcesso());
					query.setParameter("dtInicio", ai.getDtInicio());

					AudImportacao audImportacaoPersistido;

					try {
						audImportacaoPersistido = (AudImportacao) query.getSingleResult();

						if (audImportacaoPersistido.getDtValidacao() != null) {
							throw new IntegracaoAudException(IntegracaoAudException.REGISTRO_VALIDADO);
						}

						ai.setIdAudImportacao(audImportacaoPersistido.getIdAudImportacao());
						ai.setDtConsolidacao(audImportacaoPersistido.getDtConsolidacao());
						ai.setDtValidacao(audImportacaoPersistido.getDtValidacao());

						// Consistência: verificar se já foi registrado um
						// resultado sentença para sobrescrevê-lo.
						ResultadoSentencaService resultadoSentencaService = ComponentUtil
								.getComponent(ResultadoSentencaService.NAME);
						ResultadoSentenca resultadoSentencaPersistido = resultadoSentencaService
								.getResultadoSentenca(processoTrf);

						// Removendo o registro que já estava persistido devido
						// necessidade de garantir que as listas do objeto
						// AudImportacao sejam removidas do banco.
						entityManager.remove(audImportacaoPersistido);
						entityManager.remove(resultadoSentencaPersistido);
						entityManager.flush();

						ai = entityManager.merge(ai);
					} catch (javax.persistence.NoResultException e) {
						AudImportacaoHome audImportacaoHome = ComponentUtil.getComponent("audImportacaoHome");
						audImportacaoHome.criarAudienciaProcessoInseridoAud(ai);

						entityManager.persist(ai);
						// Lança Sentença Solução Diferenciada para os autores
						// ausentes caso não haja ausência total.
						entityManager.flush();
					}
				} else {
					throw new IntegracaoAudException(IntegracaoAudException.INSERCAO_SOMENTE_CONCILIACAO);
				}
			}

			registrarResultadosSentenca(ai);
		} else {
			throw new IntegracaoAudException(IntegracaoAudException.PROCESSO_INEXISTENTE);
		}
	}

	/**
	 * validações para retornar melhores mensagens de erro para o AUD
	 * @param ai
	 * @throws IntegracaoAudException se houve algum erro de validação
	 */
	private void validacoes(AudImportacao ai) throws IntegracaoAudException {
		List<String> mensagensErro = new ArrayList<String>();
		
		if(ai.getOrgaoJustica() == null) {
			mensagensErro.add("Campo orgão de justiça está vazio\n");
		}
		
		if(ai.getRegional() == null) {
			mensagensErro.add("Campo regional está vazio\n");
		}
		
		if(ai.getNumProcesso() == null) {
			mensagensErro.add("Campo número do processo está vazio\n");
		}
		
		if(ai.getDvProcesso() == null) {
			mensagensErro.add("Campo dígito verificador está vazio\n");
		}
		
		if(ai.getAnoProcesso() == null) {
			mensagensErro.add("Campo ano do processo está vazio\n");
		}
		
		if(!mensagensErro.isEmpty()) {
			throw new IntegracaoAudException(mensagensErro.toString());
		}
	}
	
	/**
	 * Verifica se a verba já está presente no PJe, caso não esteja insere ela no banco de dados.
	 */
	private void verificarAdicionarVerbas(List<AudVerbaImportacao> verbasImportacao) {
		Query query = entityManager.createQuery("select v from AudTipoVerba v where v.tipoVerba = :nomeVerba");
		 
		for(AudVerbaImportacao verbaImportacao : verbasImportacao) {
			try {
				query.setParameter("nomeVerba", verbaImportacao.getNomeVerba());
				query.getSingleResult();
			} catch(NoResultException e) {
				TipoVerba tipoVerba = new TipoVerba();
				tipoVerba.setNomeVerba(verbaImportacao.getNomeVerba());
				
				entityManager.persist(tipoVerba);
			}
		}
	}
	
	@SuppressWarnings("unchecked")        
	public List<AudTipoVerba> listarAudTipoVerba() {                
		return entityManager.createQuery(QUERY_TIPO_VERBA).getResultList();        
	}
	
	@SuppressWarnings("unchecked")
	public List<AudConfiguracao> getAudConfiguracao() {
		List<AudConfiguracao> listaConfigs = new ArrayList<AudConfiguracao>();
		
		List<OrgaoJulgador> orgaosJulgadores = entityManager.createQuery("from OrgaoJulgador").getResultList();
		Papel diretorSecretaria = (Papel) entityManager.createQuery("from Papel where identificador = 'dirSecretaria'").getSingleResult();
		
		for (OrgaoJulgador orgaoJulgador : orgaosJulgadores) {
			AudConfiguracao audConfiguracao = new AudConfiguracao();
			String numOrgaoJulgador = String.valueOf(orgaoJulgador.getIdOrgaoJulgador());
			Localizacao localizacao = orgaoJulgador.getLocalizacao();                        
			Municipio municipio = localizacao!=null?
					                 localizacao.getEndereco()!=null?
					                		localizacao.getEndereco().getCep()!=null?
					                				 localizacao.getEndereco().getCep().getMunicipio():null:null:null;
			Estado estado = municipio!=null?municipio.getEstado():null;
			Integer numeroVara = orgaoJulgador.getNumeroVara();
			
			audConfiguracao.setOrgaoJulgador(Integer.parseInt(numOrgaoJulgador));
			audConfiguracao.setNumeroVara(numeroVara);
			audConfiguracao.setNomeMunicipio(municipio!=null?municipio.getMunicipio():null);
			audConfiguracao.setUf(estado!=null?estado.getCodEstado():null);
			
			Query query = entityManager.createQuery("select usuario from UsuarioLocalizacao u where u.estrutura = :est and u.papel = :papel"); 
	        query.setParameter("est", localizacao); 
	        query.setParameter("papel", diretorSecretaria); 

	        List<Usuario> diretoresSecretaria = query.getResultList(); 
	        List<String> nomeDiretores = new ArrayList<String>(); 

	        for(Usuario u : diretoresSecretaria) { 
	        	nomeDiretores.add(u.getNome()); 
	        } 
	                                      
	        audConfiguracao.setNomeDiretores(nomeDiretores); 
	            
			listaConfigs.add(audConfiguracao);
		}
		
		return listaConfigs;
	}
	
	@SuppressWarnings("unchecked")
	public List<AudOrgaoJulgador> listarAudOrgaoJulgador() {
		return entityManager.createQuery(QUERY_ORGAO_JULGADOR).getResultList();
	}
}
