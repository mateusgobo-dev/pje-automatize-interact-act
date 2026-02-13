package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jboss.seam.annotations.Name;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.home.SessaoPautaProcessoTrfHome;
import br.com.infox.cliente.home.SessaoProcessoDocumentoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.SessaoProcessoDocumentoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.SessaoProcessoMultDocsVoto;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.enums.TipoEditorEnum;
import br.jus.pje.nucleo.enums.TipoInclusaoDocumentoEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;
import java.util.Optional;

@Name("sessaoProcessoDocumentoManager")
public class SessaoProcessoDocumentoManager extends BaseManager<SessaoProcessoDocumento>{
	
	private static final String RELATORIO = "RELATORIO";
	private static final String EMENTA = "EMENTA";
	private static final String VOTO = "VOTO";

	@Override
	protected SessaoProcessoDocumentoDAO getDAO() {
		return ComponentUtil.getSessaoProcessoDocumentoDAO();
	}
	
	/**
	 * Recupera o primeiro elemento de julgamento colegiado do tipo informado e que esteja vinculado a uma dada
	 * sessão de julgamento e processo.
	 * 
	 * @param sessao a sessão de julgamento
	 * @param tipo o tipo de documento que se pretende recuperar
	 * @param processo o processo ao qual está vinculado o elemento
	 * @return o {@link SessaoProcessoDocumento}, ou null, se não existir.
	 */
	public SessaoProcessoDocumento getSessaoProcessoDocumentoByTipo(Sessao sessao, TipoProcessoDocumento tipo, Processo processo) {
		if(processo == null) {
			return null;
		}
		Search s = new Search(SessaoProcessoDocumento.class);
		addCriteria(s, 
				Criteria.equals("sessao", sessao),
				Criteria.equals("processoDocumento.processo", processo),
				Criteria.equals("processoDocumento.ativo", true),
				Criteria.equals("processoDocumento.tipoProcessoDocumento", tipo));
		s.setMax(1);
		List<SessaoProcessoDocumento> ret = list(s);
		return ret.isEmpty() ? null : ret.get(0);
	}

	/**
	 * Recupera o primeiro elemento de julgamento colegiado do tipo informado e que esteja vinculado a uma dada
	 * sessão de julgamento e processo e que tenha sido elaborado pelo órgão informado.
	 * 
	 * @param sessao a sessão de julgamento
	 * @param tipo o tipo de documento que se pretende recuperar
	 * @param processo o processo ao qual está vinculado o elemento
	 * @param orgao o órgão responsável pela criação desse elemento
	 * @return o {@link SessaoProcessoDocumento}, ou null, se não existir.
	 */
	public SessaoProcessoDocumento getSessaoProcessoDocumentoByTipoOj(Sessao sessao, TipoProcessoDocumento tipo, 
				Processo processo, OrgaoJulgador orgao) {
		Search s = new Search(SessaoProcessoDocumento.class);
		addCriteria(s, 
				Criteria.equals("sessao", sessao),
				Criteria.equals("processoDocumento.processo", processo),
				Criteria.equals("processoDocumento.ativo", true),
				Criteria.equals("processoDocumento.tipoProcessoDocumento", tipo),
				Criteria.equals("orgaoJulgador", orgao));
		s.setMax(1);
		List<SessaoProcessoDocumento> ret = list(s);
		return ret.isEmpty() ? null : ret.get(0);
	}

	/**
	 * Grava as informações de julgamento de um dado elemento de julgamento.
	 * 
	 * @param sessao a sessão à qual será associado o elemento de julgamento
	 * @param sessaoProcessoDocumento o objeto a ser gravado
	 * @param processoTrf o processo judicial a que pertence o julgamento 
	 * @param usuarioLocalizacao a localização do usuário responsável
	 * @param orgaoJulgador o órgão julgador
	 * @return o objeto gravado
	 */
	public SessaoProcessoDocumento persistirSessaoEAgregados(Sessao sessao,
			SessaoProcessoDocumento sessaoProcessoDocumento, ProcessoTrf processoTrf,
			UsuarioLocalizacao usuarioLocalizacao, OrgaoJulgador orgaoJulgador) {
		sessaoProcessoDocumento.setSessao(sessao);
		sessaoProcessoDocumento.setOrgaoJulgador(orgaoJulgador);
		sessaoProcessoDocumento.setTipoInclusao(TipoInclusaoDocumentoEnum.S);
		SessaoProcessoDocumento spd = null;

		try {
			spd = persist(sessaoProcessoDocumento);
		}catch (PJeDAOException e) {
			e.printStackTrace();
		}catch(PJeBusinessException e){
			e.printStackTrace();
		}
		return spd;

	}

	/**
	 * Verifica se é permitida a alteração de alguns dos componentes de um
	 * documento voto, por componente entenda qualquer atributo exceto o
	 * conteúdo do voto
	 * 
	 * @param sessao
	 *            Sessão do processo documento
	 * @param sessaoProcessoDocumentoVoto
	 *            documento voto da sessão
	 * @param processoTrf
	 *            processo na sessão
	 * @param orgaoAtual
	 *            órgão do usuário logado
	 * @return
	 */

	public Boolean podeEditarComponentesVoto(Sessao sessao, SessaoProcessoDocumento sessaoProcessoDocumento,
			ProcessoTrf processoTrf, OrgaoJulgador orgaoAtual) {

		Boolean podeEditarComponentes = isRelatorENaoAssinado(sessao, processoTrf, orgaoAtual, sessaoProcessoDocumento)
		// Caso a sessão não esteja encerrada e nem finalizada
				&& sessao.getDataRealizacaoSessao() == null && sessao.getDataFechamentoSessao() == null;
		return podeEditarComponentes;
	}

	/**
	 * Verifica se é permitida a alteração do conteúdo do documento de voto da
	 * sessão
	 * 
	 * @param sessao
	 *            Sessão do processo documento
	 * @param sessaoProcessoDocumentoVoto
	 *            documento voto da sessão
	 * @param processoTrf
	 *            processo na sessão
	 * @param orgaoAtual
	 *            órgão do usuário logado
	 * @return
	 */
	public Boolean podeEditarConteudoDocumento(Sessao sessao, SessaoProcessoDocumento sessaoProcessoDocumento,
			ProcessoTrf processoTrf, OrgaoJulgador orgaoAtual) {

		Boolean podeEditarDocumento = isRelatorENaoAssinado(sessao, processoTrf, orgaoAtual, sessaoProcessoDocumento)
		// Caso a sessão não esteja finalizada
				&& sessao.getDataFechamentoSessao() == null;

		return podeEditarDocumento;
	}

	/**
	 * Verifica se o órgão dado é o relator do processo judicial.
	 * 
	 * @param processoTrf o processo judicial
	 * @param orgaoAtual o órgão candidato
	 * @return true, se o órgão infromado for o órgão associado ao processo
	 * @see ProcessoTrf#getOrgaoJulgador()
	 */
	public Boolean isRelator(ProcessoTrf processoTrf, OrgaoJulgador orgaoAtual) { if (processoTrf == null || orgaoAtual == null) {
			return false;
		}
		if (processoTrf.getOrgaoJulgador().equals(orgaoAtual)) {
			return true;
		}
		return false;

	}

	/**
	 * Verifica se um dado elemento de julgamento pertence a este órgão julgador e, em caso positivo,
	 * se esse julgador ainda não assinou o elemento de julgamento.
	 * 
	 * @param sessao a sessão associada
	 * @param processoTrf o processo judicial
	 * @param orgaoAtual o órgão julgador atual
	 * @param sessaoProcessoDocumento o objeto de julgamento a ser verificado
	 * @return true, se o elemento de julgamento for não nulo, tiver um documento associado, pertencer ao órgão informado e não contiver assinaturas
	 */
	public Boolean isRelatorENaoAssinado(Sessao sessao, ProcessoTrf processoTrf, OrgaoJulgador orgaoAtual,
			SessaoProcessoDocumento sessaoProcessoDocumento) {

		// Se existe documento
		if (sessaoProcessoDocumento != null && sessaoProcessoDocumento.getProcessoDocumento() != null) {

			ProcessoDocumentoBin pdb = sessaoProcessoDocumento.getProcessoDocumento().getProcessoDocumentoBin();
			// Se for o relator
			if (isRelator(processoTrf, orgaoAtual)) {
				// Caso não esteja assinado
				if (pdb != null && pdb.getSignatarios().isEmpty()) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Recupera o último elemento de julgamento cujo tipo for acórdão e que já estiver assinado.
	 * 
	 * @param processo o processo no qual teria sido proferido o acórdão
	 * @return o último acórdão já assinado e incluído no processo
	 * @see ParametroUtil#getTipoProcessoDocumentoAcordao()
	 */
	public SessaoProcessoDocumento getUltimoAcordaoAssinadoNoProcesso(Processo processo) {
		SessaoProcessoDocumento ultimoAcordao = null;
		if(processo != null) {
			TipoProcessoDocumento tipo = ParametroUtil.instance().getTipoProcessoDocumentoAcordao();
			Search s = new Search(SessaoProcessoDocumento.class);
			addCriteria(s, 
					Criteria.not(Criteria.empty("processoDocumento.processoDocumentoBin.signatarios")),
					Criteria.equals("processoDocumento.tipoProcessoDocumento", tipo),
					Criteria.equals("processoDocumento.processo", processo),
					Criteria.equals("processoDocumento.ativo", true),
					Criteria.not(Criteria.isNull("processoDocumento.dataInclusao")));
			s.addOrder("o.processoDocumento.dataInclusao", Order.DESC);
			s.setMax(1);
			List<SessaoProcessoDocumento> ret = list(s);
			if(!ret.isEmpty()) {
				ultimoAcordao = ret.get(0);
			}
		}
		return ultimoAcordao;
	}

	/**
	 * Verifica se o documento informado foi produzido após a assinatura do último acórdão.
	 * 
	 * @param documento o documento a ser verificado
	 * @param processo o processo ao qual pertence o documento
	 * @return true, se o documento foi incluído no sistema após a assinatura do último acórdão
	 */
	public boolean documentoInclusoAposProcessoJulgado(ProcessoDocumento documento, Processo processo) {
		return documentoInclusoAposProcessoJulgado(documento.getDataInclusao(), processo);
	}
	
	
	/**
	 * Verifica se a data indicada é posterior à data em que se produziu o último acórdão no processo.
	 * 
	 * @param dataDocumento a data paradigma
	 * @param processo o processo no qual teria sido produzido um acórdão
	 * @return true, se não tiver havido prolação de acórdão no processo ou, tendo havido uma prolação tal,
	 * se a data da primeira assinatura desse acórdão for anterior à data paradigma 
	 */
	public boolean documentoInclusoAposProcessoJulgado(Date dataDocumento, Processo processo) {
		SessaoProcessoDocumento ultimoAcordaoAssinado = getUltimoAcordaoAssinadoNoProcesso(processo);
		if (ultimoAcordaoAssinado != null && ultimoAcordaoAssinado.getProcessoDocumento().getProcessoDocumentoBin().getSignatarios().size() > 0) {
			Date dataAssinaturaAcordao = ultimoAcordaoAssinado.getProcessoDocumento().getProcessoDocumentoBin().getSignatarios().get(0).getDataAssinatura();
			return dataDocumento.after(dataAssinaturaAcordao);
		}
		return true;
	}

    /**
     * Recupera o documento pertencente a um elemento de julgamento ({@link SessaoProcessoDocumento}) de um processo 
     * cuja sessão e tipo sejam os dados.
     * 
     * Adicionado na solicitação [PJEII-4330]
     * 
     * @param sessao a sessão de julgamento vinculada
     * @param tipoProcessoDocumento o tipo do documento
     * @param processo o processo
     * @return o documento
     */
	public ProcessoDocumento getProcessoDocumentoBySessaoTipoProcesso(Sessao sessao,
        TipoProcessoDocumento tipo, Processo processo) {
		Search s = new Search(SessaoProcessoDocumento.class);
		s.setRetrieveField("processoDocumento");
		addCriteria(s, 
				Criteria.not(Criteria.empty("processoDocumento.processoDocumentoBin.signatarios")),
				Criteria.equals("processoDocumento.tipoProcessoDocumento", tipo),
				Criteria.equals("processoDocumento.processo", processo),
				Criteria.equals("processoDocumento.ativo", true));
		s.setMax(1);
		List<ProcessoDocumento> ret = list(s);
		return ret.isEmpty() ? null : ret.get(0);
    }        

    /**
     * [PJEII-9886]
     * Método verifica na lista de processos recebida como parâmetro quais
     * possuem documentos de sessão elaborados
     * 
     * @param listProcessos Lista de Processos a ser verificada
     * @return Lista de Processos que contêm documentos de sessão
     */
	public List<ProcessoTrf> listProcessosComDocumentosSessao(List<ProcessoTrf> listProcessos) {
        return ComponentUtil.getSessaoProcessoDocumentoDAO().listProcessosComDocumentosSessao(listProcessos);
    }
	
	/**
	 * Recupera os elementos de julgamento associados a uma dada sessão e processo judicial, excluindo,
	 * se definidos, aqueles indicados nos parâmetros.
	 * 
	 * @param sessao a sessão de julgamento associada
	 * @param processo o processo judicial no qual teriam sido produzidos
	 * @param excluidos os documentos que devem ser desconsiderados
	 * @return a lista de elementos de julgamento
	 */
	public List<SessaoProcessoDocumento> recuperaElementosJulgamento(ProcessoTrf processo, Sessao sessao, ProcessoDocumento...excluidos){
		return recuperaElementosPendentesJulgamento_(processo, sessao, null, false, false, excluidos);
	}
	
	/**
	 * Recupera os documentos de elementos de julgamento associados a uma dada sessão e processo judicial, excluindo,
	 * se definidos, aqueles indicados nos parâmetros.
	 * 
	 * @param sessao a sessão de julgamento associada
	 * @param processo o processo judicial no qual teriam sido produzidos
	 * @param excluidos os documentos que devem ser desconsiderados
	 * @return a lista de documentos dos elementos de julgamento
	 */
	public List<ProcessoDocumento> recuperaDocumentosJulgamento(ProcessoTrf processo, Sessao sessao, ProcessoDocumento...excluidos){
		return recuperaElementosPendentesJulgamento_(processo, sessao, null, true, true, excluidos);
	}
	
	public List<SessaoProcessoDocumento> recuperaElementosJulgamento(ProcessoTrf processo, Sessao sessao, OrgaoJulgador orgao, ProcessoDocumento...excluidos){
		return recuperaElementosPendentesJulgamento_(processo, sessao, orgao, false, true, excluidos);
	}
	
	public List<SessaoProcessoDocumento> recuperaElementosJulgamento(ProcessoTrf processo, Sessao sessao, OrgaoJulgador orgao, 
			boolean incluirVoto, boolean incluirJuntados){
		List<SessaoProcessoDocumento> ret = new ArrayList<SessaoProcessoDocumento>();

		if(incluirVoto){
			Search sv = new Search(SessaoProcessoDocumentoVoto.class);
			sv.addOrder("idSessaoProcessoDocumento", Order.DESC);
			limitarSessao(sv, sessao);
			limitarOrgao(sv, orgao, true);
			addCriteria(sv, 
					Criteria.equals("processoTrf", processo));
			List<SessaoProcessoDocumentoVoto> votos = list(sv);
			ret.addAll(votos);
		}

		Search s = new Search(SessaoProcessoDocumento.class);
		s.setDistinct(true);
		s.addOrder("idSessaoProcessoDocumento", Order.DESC);
		limitarSessao(s, sessao);
		limitarOrgao(s, orgao, true);
		addCriteria(s, 
				Criteria.equals("processoDocumento.processo", processo.getProcesso()),
				Criteria.equals("processoDocumento.ativo", true));
		if(!incluirJuntados){
			addCriteria(s,
					Criteria.isNull("processoDocumento.dataJuntada"));
		}
		List<SessaoProcessoDocumento> outros = list(s);
		
		for (SessaoProcessoDocumento outro: outros)
			if (!ret.contains(outro))
				ret.add(outro);

		int persistiu = 0;
		for (SessaoProcessoDocumento spd: ret) {
			if (spd.getProcessoDocumento()!=null) 
				continue;
			if (spd instanceof SessaoProcessoDocumentoVoto) {
				SessaoProcessoDocumentoVoto voto = (SessaoProcessoDocumentoVoto)spd;
				Optional<SessaoProcessoMultDocsVoto> optMultDoc = voto.getSessaoProcessoMultDocsVoto().stream().max((a, b) -> {
					return a.getOrdemDocumento()-b.getOrdemDocumento();
				});
				if (optMultDoc.isPresent()) {
					voto.setProcessoDocumento(optMultDoc.get().getProcessoDocumento());
					try {
						persist(voto);
						persistiu++;
					} catch (PJeBusinessException ex) {
						ex.printStackTrace();
					}
				}				
			}
		}
		if (persistiu>0) {
			try {
				flush();
			} catch (PJeBusinessException ex) {
				ex.printStackTrace();
			}				
		}
		
		return ret;
	}
	
	/**
	 * Indica se existe algum elemento de julgamento do tipo acórdão ainda não juntado aos autos.
	 * 
	 * @param processoJudicial o processo judicial
	 * @param sessaoJulgamento a sessão de julgamento que levaria à produção do acórdão
	 * @return true, se existir algum documento do tipo acórdão associado à sessão e ao processo dados que não tenha
	 * sido juntado ao processo
	 */
	public boolean existeAcordaoPendente(ProcessoTrf processoJudicial, SessaoPautaProcessoTrf sessaoJulgamento) {
    	Search s = new Search(SessaoProcessoDocumento.class);
    	addCriteria(s, 
    			Criteria.equals("sessao", sessaoJulgamento.getSessao()), 
    			Criteria.equals("processoDocumento.processo", processoJudicial.getProcesso()),
    			Criteria.equals("processoDocumento.tipoProcessoDocumento", ParametroUtil.instance().getTipoProcessoDocumentoAcordao()),
    			Criteria.equals("processoDocumento.ativo", true),
    			Criteria.not(Criteria.isNull("processoDocumento.dataJuntada")));
    	long cnt = count(s);
		return cnt > 0 ? true : false;
	}
	
	/**
	 * Indica se existe algum elemento de julgamento do tipo acórdão juntado aos autos
	 * 
	 * @param processoJudicial o processo judicial
	 * @param sessaoJulgamento a sessão de julgamento que levaria à produção do acórdão
	 * @return true, se existir algum documento do tipo acórdão associado à sessão e ao processo dados que tenha
	 * sido juntado ao processo
	 */
	public boolean existeAcordaoJuntado(Sessao sessao, ProcessoTrf processo) {
    	Search s = new Search(SessaoProcessoDocumento.class);
    	addCriteria(s, 
    			Criteria.equals("sessao", sessao), 
    			Criteria.equals("processoDocumento.processo", processo.getProcesso()),
    			Criteria.equals("processoDocumento.tipoProcessoDocumento", ParametroUtil.instance().getTipoProcessoDocumentoAcordao()), 
    			Criteria.not(Criteria.isNull("processoDocumento.dataJuntada")));
    	long cnt = count(s);
		return cnt > 0 ? true : false;
	}
	
	/**
	 * Recupera os elementos de julgamento associados a uma dada sessão e processo judicial, excluindo,
	 * se definidos, aqueles indicados nos parâmetros, excluindo-se, ainda, os já juntados aos autos.
	 * 
	 * @param somenteDocumentos indica que se pretende recuperar apenas os documentos
	 * @param sessao a sessão de julgamento associada
	 * @param processo o processo judicial no qual teriam sido produzidos
	 * @param excluidos os documentos que devem ser desconsiderados
	 * @return a lista de elementos de julgamento ou de documentos, conforme o parâmetros somenteDocumentos esteja falso ou verdadeiro
	 */
	private <T> List<T> recuperaElementosPendentesJulgamento_(ProcessoTrf processo, Sessao sessao, OrgaoJulgador orgao, boolean somenteDocumentos, 
			boolean somenteDocsLiberados, ProcessoDocumento...exclusoes){
		Set<T> ret = new HashSet<T>();
		Search s = new Search(SessaoProcessoDocumento.class);
		s.setDistinct(true);
		limitarSessao(s, sessao);
		limitarOrgao(s, orgao, somenteDocsLiberados);
		limitarDocumentosAtivos(s, processo);
		if(exclusoes != null && exclusoes.length > 0){
			addCriteria(s,
					Criteria.not(Criteria.in("processoDocumento", exclusoes)));
		}
		if(somenteDocumentos){
			s.setRetrieveField("processoDocumento");
		}
		List<T> parcial = list(s);
		Search sv = new Search(SessaoProcessoDocumentoVoto.class);
		limitarSessao(sv, sessao);
		limitarOrgao(sv, orgao, somenteDocsLiberados);
		addCriteria(sv, 
				Criteria.equals("processoTrf", processo));
		if(exclusoes != null && exclusoes.length > 0){
			addCriteria(sv,
					Criteria.not(Criteria.in("processoDocumento", exclusoes)));
		}
		
		if(somenteDocumentos){
			s.setRetrieveField("processoDocumento");
		}
		List<T> votos = list(sv);
		ret.addAll(parcial);
		ret.addAll(votos);
		return new ArrayList<T>(ret);
	}
	
	public boolean elementoJulgamentoLiberado(ProcessoTrf processo, OrgaoJulgador orgao, Sessao sessao, TipoProcessoDocumento tipo) {
		Search s = new Search(SessaoProcessoDocumento.class);
		s.setDistinct(true);
		limitarSessao(s, sessao);
		limitarOrgao(s, orgao, true);
		addCriteria(s,
				Criteria.equals("processoDocumento.processo.idProcesso", processo.getIdProcessoTrf()),
				Criteria.equals("processoDocumento.tipoProcessoDocumento", tipo));
		Long count = count(s);
		return count > 0;
	}
	
	/**
	 * 
	 * @param s objeto de Search para criação de Criteria de pesquisa
	 * @param orgao - orgão julgador vinculado a SessaoProcessoDocumento
	 * @param somenteDocsLiberados - parametro adicionado para manter o comportamento anterior nos lugares onde o método já era referenciado.
	 */
	private void limitarOrgao(Search s, OrgaoJulgador orgao, boolean somenteDocsLiberados){
		if(somenteDocsLiberados){
			if(orgao != null){
				addCriteria(s, 
						Criteria.or(
								Criteria.equals("orgaoJulgador", orgao), 
								Criteria.equals("liberacao", true)));
			}else{
				addCriteria(s, Criteria.equals("liberacao", true));
			}
		}else if(orgao != null){
			addCriteria(s, Criteria.equals("orgaoJulgador", orgao));
		}
	}
	
	private void limitarSessao(Search s, Sessao sessao){
		if(sessao == null){
			// Para os casos em que a SessaoProcessoDocumento já tem uma sessão, mas a invocação do método não
			// que sessão é essa.
			Criteria dataFechamentoNulo = Criteria.isNull("sessao.dataRealizacaoSessao");
			dataFechamentoNulo.setRequired("sessao", false);
			Criteria cs = Criteria.or(
							Criteria.isNull("sessao"),
							dataFechamentoNulo);
			addCriteria(s, cs);
		}else{
			addCriteria(s, Criteria.equals("sessao", sessao));
		}
	}

	private void limitarDocumentosAtivos(Search s, ProcessoTrf processo){
		addCriteria(s, 
				Criteria.equals("processoDocumento.processo", processo.getProcesso()),
				Criteria.equals("processoDocumento.ativo", true));
	}
	
	public void liberarDocumentosSessao(ProcessoTrf processo, OrgaoJulgador orgao, Sessao sessao) throws PJeBusinessException{
		Search s = new Search(SessaoProcessoDocumento.class);
		s.setDistinct(true);
		limitarSessao(s, sessao);
		limitarOrgao(s, orgao, true);
		addCriteria(s,
				Criteria.equals("processoDocumento.processo.idProcesso", processo.getIdProcessoTrf()),
				Criteria.equals("liberacao", false));
		List<SessaoProcessoDocumento> docs = list(s);
		for(SessaoProcessoDocumento doc: docs){
			doc.setLiberacao(true);
		}
		flush();
	}
	
	public List<ProcessoTrf> listProcessosComDocumentoPorTipo(List<ProcessoTrf> listProcessos, Integer idTipoProcessoDocumento) {
		return ComponentUtil.getSessaoProcessoDocumentoDAO().listProcessosComDocumentoPorTipo(listProcessos, idTipoProcessoDocumento);
	}
	
	public List<ProcessoTrf> retornaListaProcessoComDocumentoAssinadoPorSessaoAndTipo(List<ProcessoTrf> listProcessos, Integer idTipoProcessoDocumento, Integer idSessao) {
		return ComponentUtil.getSessaoProcessoDocumentoDAO().retornaListaProcessoComDocumentoAssinadoPorSessaoAndTipo(listProcessos, idTipoProcessoDocumento, idSessao);
	}

	public void atualizarSessaoProcessoDocumentos(ProcessoTrf processoTrf, Sessao sessao) throws Exception {	
		
		List<SessaoProcessoDocumento> sessaoProcessoDocumentos = getDAO().recuperarSessaoProcessoDocumentosSemSessaoDefinida(processoTrf);
		
		for (SessaoProcessoDocumento sessaoProcessoDocumento : sessaoProcessoDocumentos) {
			sessaoProcessoDocumento.setSessao(sessao);
		}
	}
	
	/**
	 * Recupera a lista dos elementos de julgamento colegiado do tipo informado e que esteja vinculado a uma dada
	 * sessão de julgamento e processo.
	 * 
	 * @param sessao a sessão de julgamento
	 * @param tipo o tipo de documento que se pretende recuperar
	 * @param processo o processo ao qual está vinculado o elemento
	 * @return o {@link SessaoProcessoDocumento}, ou null, se não existir.
	 */
	public List<SessaoProcessoDocumento> getListaSessaoProcessoDocumentoByTipo(Sessao sessao, TipoProcessoDocumento tipo, Processo processo) {
		Search s = new Search(SessaoProcessoDocumento.class);
		addCriteria(s, 
				Criteria.equals("sessao", sessao),
				Criteria.equals("processoDocumento.processo", processo),
				Criteria.equals("processoDocumento.ativo", true),
				Criteria.equals("processoDocumento.tipoProcessoDocumento", tipo));
		s.setMax(1);
		List<SessaoProcessoDocumento> ret = list(s);
		return ret;
	}
	
	/**
	 * Ao passar um id de um ProcessoDocumento ira apagar todos os SessaoProcessoDocumento que tenham este
	 * ProcessoDocumento vinculado.
	 * 
	 * @param idProcessoDocumento id do ProcessoDocumento vinculado.
	 */
	public void remover(Integer idProcessoDocumento){
		getDAO().remover(idProcessoDocumento);
	}
	
	/**
	 * @param sessaoPautaProcessoTrf referência da pauta do processo na sessão
	 * @return todos os documentos da sessão que possuam conteúdo, ou seja, documentos que possuam registro
	 * na tebela ProcessoDocumento (SessaoProcessoDocumento.processoDocumento diferente de null)
	 */
	public List<SessaoProcessoDocumento> getDocumentosSessao(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		return ComponentUtil.getSessaoProcessoDocumentoDAO().getDocumentosSessao(sessaoPautaProcessoTrf);
	}
	
	/**
	 * @param sessaoPautaProcessoTrf referência da pauta do processo na sessão
	 * @return votos que tenha sido apenas sinalizados, ou seja, que tenha sido registrados na sessão, porém, não 
	 * tenham registro de conteúdo na tabela ProcessoDocumento (SessaoProcessoDocumento.processoDocumento seja null)
	 */
	public List<SessaoProcessoDocumento> getVotosSomenteSinalizadosSessao(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		return ComponentUtil.getSessaoProcessoDocumentoDAO().getVotosSomenteSinalizadosSessao(sessaoPautaProcessoTrf);
	}
	
	/**
	 * @param sessaoPautaProcessoTrf referência da pauta do processo na sessão
	 * @return retorna os documentos aptos a sofrerem antecipação, ou seja, documentos que serão desvinculados da sessão
	 * para serem reutilizados na próxima sessão em que o processo for pautado.
	 */
	public List<SessaoProcessoDocumento> listaDocumentosAptosAntecipacao(SessaoPautaProcessoTrf sessaoPautaProcessoTrf){
		List<SessaoProcessoDocumento> listaDocumentos = new ArrayList<SessaoProcessoDocumento>(0);
		List<SessaoProcessoDocumento> documentosComConteudo = getDocumentosSessao(sessaoPautaProcessoTrf);
		List<SessaoProcessoDocumento> votosSinalizados = getVotosSomenteSinalizadosSessao(sessaoPautaProcessoTrf);
		listaDocumentos.addAll(documentosComConteudo);
		listaDocumentos.addAll(votosSinalizados);
		return listaDocumentos;
	}
	
	/**
     * Verifica se há, nos autos do processo, um documento do tipo 
     * acórdão que consta como assinado para uma determinada sessão de julgamento.
     * 
     * @param processo Processo judicial.
     * @param sessao Sessão de julgamento.
     * @return Verdadeiro se houver, nos autos do processo, um documento do tipo 
     * acórdão assinado para uma determinada sessão de julgamento. Falso, caso contrário.
     */
    public boolean isAcordaoAssinado(ProcessoTrf processo, Sessao sessao) {
        TipoProcessoDocumento tipoAcordao = ParametroUtil.instance().getTipoProcessoDocumentoAcordao();
        return isDocumentoAssinado(processo, sessao, tipoAcordao);
    }
    
    /**
     * Verifica se há, nos autos do processo, um documento do tipo 
     * especificado que consta como assinado para uma determinada sessão de julgamento.
     * 
     * @param processo Processo judicial.
     * @param sessao Sessão de julgamento.
     * @return Verdadeiro se houver, nos autos do processo, um documento do tipo 
     * acórdão assinado para uma determinada sessão de julgamento. Falso, caso contrário.
     */
    public boolean isDocumentoAssinado(ProcessoTrf processo, Sessao sessao, TipoProcessoDocumento tipoDocumento) {
        Search s = new Search(SessaoProcessoDocumento.class);
        addCriteria(s, 
                Criteria.equals("sessao", sessao),
                Criteria.equals("processoDocumento.tipoProcessoDocumento", tipoDocumento),
                Criteria.equals("processoDocumento.processo.idProcesso", processo.getIdProcessoTrf()),
                Criteria.equals("processoDocumento.ativo", true),
                Criteria.not(Criteria.isNull("processoDocumento.dataInclusao")),
        		Criteria.not(Criteria.empty("processoDocumento.processoDocumentoBin.signatarios")));

        return count(s) > 0;
    }

    public SessaoProcessoDocumento recuperaPorProcessoDocumento(ProcessoDocumento processoDocumento) {
		Search s = new Search(SessaoProcessoDocumento.class);
		addCriteria(s, 
				Criteria.equals("processoDocumento", processoDocumento)
				);
		s.addOrder("idSessaoProcessoDocumento", Order.DESC);
		s.setMax(1);
		List<SessaoProcessoDocumento> ret = list(s);
		return ret.isEmpty() ? null : ret.get(0);
	}

	/**
	 * Recupera a SessaoProcessoDocumento (ou SessaoProcessoDocumentoVoto) 
	 * respectiva a um dado ProcessoDocumento. Se houver mais de uma 
	 * SessaoProcessoDocumento, a prioridade é para a mais recente que ainda não 
	 * esteja vinculada a uma Sessao.
	 * @param processoDocumento
	 * @return 
	 */
    public SessaoProcessoDocumento recuperaPorProcessoDocumentoSePossivelSemSecao(ProcessoDocumento processoDocumento) {
		Search s = new Search(SessaoProcessoDocumento.class);
		addCriteria(s, 
				Criteria.equals("processoDocumento", processoDocumento),
				Criteria.isNull("sessao")
				);
		s.addOrder("idSessaoProcessoDocumento", Order.DESC);
		s.setMax(1);
		List<SessaoProcessoDocumento> ret = list(s);
		if (ret.isEmpty())
			return recuperaPorProcessoDocumento(processoDocumento);
		return ret.get(0);
	}

    public void registrarSessaoProcessoDocumento(ProcessoDocumento pd, SessaoPautaProcessoTrf sessaoPauta) throws PJeBusinessException  {
		SessaoProcessoDocumento spd = new SessaoProcessoDocumento();
		spd.setSessao(sessaoPauta.getSessao());
		spd.setOrgaoJulgador(sessaoPauta.getProcessoTrf().getOrgaoJulgador());
		spd.setLiberacao(true);
		spd.setTipoInclusao(TipoInclusaoDocumentoEnum.S);
		spd.setProcessoDocumento(pd);
		persistAndFlush(spd);
	}

	/**
	 * Metodo que realiza a filtragem dos elementos de uma sessao de julgamento
	 * para o orgao julgador relator a partir de uma lista contendo todos os
	 * elementos de julgamento, o Orgao Julgador Relator pertinente e a listagem
	 * de tipos de ProcessoDocumento permitidos.
	 * 
	 * @param elementosJulgamento
	 * @param relator
	 * @param tiposProcessoDocumento
	 * 
	 * @return map contendo os documentos pertinentes ao relator
	 */
	public Map<String,SessaoProcessoDocumento> filtrarDocumentosRelator(List<SessaoProcessoDocumento> elementosJulgamento,OrgaoJulgador relator,Map<String, TipoProcessoDocumento> tiposProcessoDocumento){
		Map<String,SessaoProcessoDocumento> elementosFiltrados = new HashMap<String, SessaoProcessoDocumento>();
		int idTipoProcessoDocumento = 0;
		
		for (SessaoProcessoDocumento sessaoProcessoDocumento : elementosJulgamento) {
			
			if(sessaoProcessoDocumento == null){
				continue;
			}
			
			if(!sessaoProcessoDocumento.getOrgaoJulgador().getOrgaoJulgador().equalsIgnoreCase(relator.getOrgaoJulgador())){
				continue;
			}
			
			if(sessaoProcessoDocumento instanceof SessaoProcessoDocumentoVoto){
				if(((SessaoProcessoDocumentoVoto)sessaoProcessoDocumento).getTipoVoto() != null){
					elementosFiltrados.putIfAbsent(VOTO,sessaoProcessoDocumento);
				}
				continue;
			}
			
			if(sessaoProcessoDocumento.getProcessoDocumento() != null){
				idTipoProcessoDocumento = sessaoProcessoDocumento.getProcessoDocumento().getTipoProcessoDocumento().getIdTipoProcessoDocumento();
			}
			
			if(sessaoProcessoDocumento instanceof SessaoProcessoDocumento){
				if(tiposProcessoDocumento.get(RELATORIO).getIdTipoProcessoDocumento() == idTipoProcessoDocumento){
					elementosFiltrados.putIfAbsent(RELATORIO,sessaoProcessoDocumento);
				}
				
				if(tiposProcessoDocumento.get(EMENTA).getIdTipoProcessoDocumento() == idTipoProcessoDocumento){
					elementosFiltrados.putIfAbsent(EMENTA,sessaoProcessoDocumento);
				}
			}
		}
		
		return elementosFiltrados;
	}
	
	/**
	 * Metodo que realiza a filtragem dos elementos de uma sessao de julgamento
	 * para o orgao julgador vogal a partir de uma lista contendo todos os
	 * elementos de julgamento, o Orgao Julgador vogal pertinente e a listagem
	 * de tipos de ProcessoDocumento permitidos.
	 * 
	 * @param elementosJulgamento
	 * @param relator
	 * @param tiposProcessoDocumento
	 * 
	 * @return map contendo os documentos pertinentes ao vogal
	 */
	public Map<String,SessaoProcessoDocumento> filtrarDocumentosVogal(List<SessaoProcessoDocumento> elementosJulgamento,OrgaoJulgador vogal,Map<String, TipoProcessoDocumento> tiposProcessoDocumento){
		Map<String,SessaoProcessoDocumento> elementosFiltrados = new HashMap<String, SessaoProcessoDocumento>();
		
		for (SessaoProcessoDocumento sessaoProcessoDocumento : elementosJulgamento) {
			if(!sessaoProcessoDocumento.getOrgaoJulgador().getOrgaoJulgador().equalsIgnoreCase(vogal.getOrgaoJulgador())){
				continue;
			}
			
			if(sessaoProcessoDocumento instanceof SessaoProcessoDocumentoVoto){
				if(((SessaoProcessoDocumentoVoto)sessaoProcessoDocumento).getTipoVoto() != null){
					elementosFiltrados.putIfAbsent(VOTO,sessaoProcessoDocumento);
				}
			}
		}
		
		return elementosFiltrados;
	}
	
	/**
	 * Metodo que realiza a filtragem dos elementos de uma sessao de julgamento
	 * retornando os elementos de voto agrupados por seu contexto a partir de uma lista contendo todos os
	 * elementos de julgamento
	 * 
	 * @param List elementosJulgamento
	 * @param String contexto
	 * @param int idOrgaoJulgadorAcompanhado
	 * 
	 * @return map contendo os votos dos orgaos julgadores na sessao agrupados pelo contexto
	 * @throws PJeBusinessException 
	 */
	public Map<String,SessaoProcessoDocumento> filtrarDocumentosAgrupadosPeloContextoDosVotosEOrgaoJulgadorAcompanhado(List<SessaoProcessoDocumento> elementosJulgamento,String contexto, int idOrgaoJulgadorAcompanhado) throws PJeBusinessException{
		Map<String,SessaoProcessoDocumento> elementosFiltrados = new HashMap<String, SessaoProcessoDocumento>();
		
		if(elementosJulgamento != null){
			
			for (SessaoProcessoDocumento sessaoProcessoDocumento : elementosJulgamento) {				
				sessaoProcessoDocumento = ComponentUtil.getSessaoProcessoDocumentoDAO().find(sessaoProcessoDocumento.getIdSessaoProcessoDocumento());
				
				filtrarVotoOrgaoJulgadorAcompanhado(contexto, sessaoProcessoDocumento, elementosFiltrados);				
				filtrarRelatorioEmentaOrgaoJulgadorAcompanhado(idOrgaoJulgadorAcompanhado, sessaoProcessoDocumento, elementosFiltrados); 				
			}
		}
		
		return elementosFiltrados;
			
	}

	private void filtrarRelatorioEmentaOrgaoJulgadorAcompanhado(int idOrgaoJulgadorAcompanhado, SessaoProcessoDocumento sessaoProcessoDocumento, Map<String, SessaoProcessoDocumento> elementosFiltrados) {
		if(sessaoProcessoDocumento.getOrgaoJulgador() != null && sessaoProcessoDocumento.getOrgaoJulgador().getIdOrgaoJulgador() == idOrgaoJulgadorAcompanhado){
			
			if(!(sessaoProcessoDocumento instanceof SessaoProcessoDocumentoVoto)){
			
				TipoProcessoDocumento tipoDocumento = sessaoProcessoDocumento.getProcessoDocumento().getTipoProcessoDocumento();
				
				if(ParametroUtil.instance().getTipoProcessoDocumentoEmenta().equals(tipoDocumento)){
					elementosFiltrados.putIfAbsent((EMENTA+"_"+idOrgaoJulgadorAcompanhado),sessaoProcessoDocumento);
				}
				
				if(ParametroUtil.instance().getTipoProcessoDocumentoRelatorio().equals(tipoDocumento)){
					elementosFiltrados.putIfAbsent((RELATORIO+"_"+idOrgaoJulgadorAcompanhado),sessaoProcessoDocumento);
				}

			}
			
		}
	}

	private void filtrarVotoOrgaoJulgadorAcompanhado(String contexto, SessaoProcessoDocumento sessaoProcessoDocumento, Map<String, SessaoProcessoDocumento> elementosFiltrados) {
		if(sessaoProcessoDocumento instanceof SessaoProcessoDocumentoVoto){

			SessaoProcessoDocumentoVoto voto = (SessaoProcessoDocumentoVoto)sessaoProcessoDocumento;
			
			if (voto.getTipoVoto() != null 
					&& voto.getTipoVoto().getContexto().equalsIgnoreCase(contexto)
					&& voto.getProcessoDocumento() != null
					&& voto.getProcessoDocumento().getProcessoDocumentoBin() != null
					&& !voto.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento().trim().isEmpty()) {
				
				elementosFiltrados.putIfAbsent((VOTO+"_"+voto.getOrgaoJulgador().getIdOrgaoJulgador()),voto);
			}
			
		}
	}

	private void carregarInstancias(ProcessoTrf processo, Sessao sessao, SessaoProcessoDocumento sessaoDocumento) {
		ProcessoHome.instance().setInstance(processo.getProcesso());
		ProcessoTrfHome.instance().setProcessoTrf(processo);
		ProcessoTrfHome.instance().setInstance(processo);
		SessaoPautaProcessoTrf sessaoPauta = ComponentUtil.getSessaoPautaProcessoTrfManager().getSessaoPautaProcessoTrf(processo, sessao);
		SessaoProcessoDocumentoHome.instance().setInstance(sessaoDocumento);
		SessaoProcessoDocumentoHome.instance().setSessaoPautaProcessoTrf(sessaoPauta);
		SessaoPautaProcessoTrfHome.instance().setInstance(sessaoPauta);
		
	}
	
	public SessaoProcessoDocumento recuperarSessaoProcessoDocumentoAtivoPorSessaoEhProcessoEhTipoProcessoDocumento(Sessao sessao, ProcessoTrf processo, TipoProcessoDocumento tipoDoc, OrgaoJulgador orgao ) {
		return getDAO().recuperarSessaoProcessoDocumentoAtivoPorSessaoEhProcessoEhTipoProcessoDocumento(sessao, processo, tipoDoc, orgao);
	}
		
	public SessaoProcessoDocumento recuperarCertidaoJulgamento(String modelo, Sessao sessao, ProcessoTrf processo) throws PJeBusinessException {
		TipoEditorEnum tipoEditor = ParametroUtil.instance().getEditor();
		return recuperarCertidaoJulgamento(tipoEditor, modelo, sessao, processo);
	}

	public SessaoProcessoDocumento recuperarCertidaoJulgamento(TipoEditorEnum tipoEditor, String modelo, Sessao sessao, ProcessoTrf processo) throws PJeBusinessException {
		TipoProcessoDocumento tipoDocCertidaoJulgamento = ParametroUtil.instance().getTipoProcessoDocumentoCertidaoJulgamento();
		SessaoProcessoDocumento sessaoDocumento = getDAO().recuperarSessaoProcessoDocumentoAtivoPorSessaoEhProcessoEhTipoProcessoDocumento(sessao, processo, tipoDocCertidaoJulgamento, null);
		if(sessaoDocumento == null ) {
			sessaoDocumento = new SessaoProcessoDocumento();
		}
		carregarInstancias(processo, sessao, sessaoDocumento);
		String modeloProcessado = ComponentUtil.getModeloDocumentoManager().traduzirModelo(tipoEditor, modelo);

		if(sessaoDocumento != null && sessaoDocumento.getProcessoDocumento() != null && sessaoDocumento.getProcessoDocumento().getDataJuntada() == null) {
			sessaoDocumento.getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(modeloProcessado);
			this.mergeAndFlush(sessaoDocumento);
		} else {
			sessaoDocumento.setLiberacao(true);
			sessaoDocumento.setOrgaoJulgador(null);
			sessaoDocumento.setSessao(sessao);
			sessaoDocumento.setTipoInclusao(TipoInclusaoDocumentoEnum.S);
			ProcessoDocumento pdNovo = ComponentUtil.getProcessoDocumentoManager().registrarProcessoDocumento(modeloProcessado, "Certidão de julgamento", tipoDocCertidaoJulgamento, processo);
			sessaoDocumento.setProcessoDocumento(pdNovo);
			this.persistAndFlush(sessaoDocumento);
		}
		return sessaoDocumento;
	}
	
	public SessaoProcessoDocumento recuperar(Sessao sessao, ProcessoTrf processo, OrgaoJulgador orgao, TipoProcessoDocumento tipo) {
		Search s = new Search(SessaoProcessoDocumento.class);
		addCriteria(s, 
				Criteria.equals("processoDocumento.tipoProcessoDocumento", tipo),
				Criteria.equals("processoDocumento.processo", processo.getProcesso()),
				Criteria.equals("processoDocumento.ativo", true),
				Criteria.equals("sessao", sessao)
				);
		s.setMax(1);
		List<SessaoProcessoDocumento> ret = list(s);
		return ret.isEmpty() ? null : ret.get(0);
	}

    public void persistSessaoProcessoDocumento(SessaoProcessoDocumento sessaoProcessoDocumento, TipoProcessoDocumento tipoProcDoc, Boolean isLiberarDocumento) throws PJeBusinessException {
        ProcessoDocumento pd = sessaoProcessoDocumento.getProcessoDocumento();
        
		Usuario usuario = Authenticator.getUsuarioLogado();
			
        if (pd.getIdProcessoDocumento() == 0) {
            pd.setProcessoDocumento(tipoProcDoc.getTipoProcessoDocumento());
            pd.setTipoProcessoDocumento(tipoProcDoc);
            pd.setDataInclusao(new Date());
            pd.setProcesso(ProcessoTrfHome.instance().getInstance().getProcesso());
			
			pd.setNomeUsuarioInclusao(usuario==null ? null : usuario.getNome());
			pd.setUsuarioInclusao(usuario);

            pd = ComponentUtil.getDocumentoJudicialService().persist(pd, true);

            sessaoProcessoDocumento.setProcessoDocumento(pd);

        } else {
			pd.setNomeUsuarioAlteracao(usuario==null ? null : usuario.getNome());
			pd.setUsuarioAlteracao(usuario);
			pd.setDataAlteracao(new Date());
			
            ComponentUtil.getProcessoDocumentoManager().mergeAndFlush(pd);
        }

        if (sessaoProcessoDocumento.getIdSessaoProcessoDocumento() > 0) {
            sessaoProcessoDocumento.setLiberacao(isLiberarDocumento);
            ComponentUtil.getSessaoProcessoDocumentoManager().mergeAndFlush(sessaoProcessoDocumento);

        } else {
            sessaoProcessoDocumento.setTipoInclusao(TipoInclusaoDocumentoEnum.A);
            
            ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
            sessaoProcessoDocumento.setOrgaoJulgador(processoTrf.getOrgaoJulgador());
			if (sessaoProcessoDocumento.getSessao()==null) {
				Sessao ultimaSessaoAberta = ComponentUtil.getSessaoJulgamentoManager().getUltimaSessaoAberta(processoTrf);
				sessaoProcessoDocumento.setSessao(ultimaSessaoAberta);
			}

            if (tipoProcDoc.equals(ParametroUtil.instance().getTipoProcessoDocumentoVoto()) && getDAO().procuraVotoAntecipadoLiberado()) {
                sessaoProcessoDocumento.setLiberacao(true);
            }

            ComponentUtil.getSessaoProcessoDocumentoManager().persistAndFlush(sessaoProcessoDocumento);
        }
    }
}