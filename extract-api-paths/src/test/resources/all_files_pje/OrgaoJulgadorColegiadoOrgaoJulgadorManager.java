package br.com.jt.pje.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.itx.util.ComponentUtil;
import br.com.jt.pje.dao.OrgaoJulgadorColegiadoOrgaoJulgadorDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.PessoaMagistradoManager;
import br.jus.cnj.pje.nucleo.manager.SucessaoOJsColegiadoManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoMagistradoServidorManager;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoOrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.util.DateUtil;

@Name(OrgaoJulgadorColegiadoOrgaoJulgadorManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class OrgaoJulgadorColegiadoOrgaoJulgadorManager extends BaseManager<OrgaoJulgadorColegiadoOrgaoJulgador> {

	public static final String NAME = "orgaoJulgadorColegiadoOrgaoJulgadorManager";
	
	@In
	private OrgaoJulgadorColegiadoOrgaoJulgadorDAO orgaoJulgadorColegiadoOrgaoJulgadorDAO;
	
	@Override
	protected OrgaoJulgadorColegiadoOrgaoJulgadorDAO getDAO() {
		return orgaoJulgadorColegiadoOrgaoJulgadorDAO;
	}
	
	public static OrgaoJulgadorColegiadoOrgaoJulgadorManager instance() {
		return ComponentUtil.getComponent(OrgaoJulgadorColegiadoOrgaoJulgadorManager.class);
	}
	
	public List<OrgaoJulgador> getOrgaoJulgadorSemComposicaoSessaoByColegiadoSessaoItems(SessaoJT sessao){
		return orgaoJulgadorColegiadoOrgaoJulgadorDAO.getOrgaoJulgadorSemComposicaoSessaoByColegiadoSessaoItems(sessao);
	}
	
	/**
	 * Método responsável por recuperar a lista de Órgãos Colegiados ativos aos quais um Órgão Julgador pertença e que
	 * estejam vigentes
	 * 
	 * @param	orgaoJulgador
	 * @return	retorna uma lista com os Órgãos Julgadores Colegiados Ativos, conforme o Órgão Julgador.
	 */
	public List<OrgaoJulgadorColegiado> recuperaOrgaosColegiadosAtivosPorOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		return  orgaoJulgadorColegiadoOrgaoJulgadorDAO.recuperaOrgaosColegiadosAtivosPorOrgaoJulgador(orgaoJulgador);
	}
	
	public Long countOrgaoJulgadorPorOrgaoJulgadorColegiado(OrgaoJulgadorColegiado ojc){
		return orgaoJulgadorColegiadoOrgaoJulgadorDAO.countOrgaoJulgadorPorOrgaoJulgadorColegiado(ojc);
	}
	
	public List<OrgaoJulgadorColegiadoOrgaoJulgador> recuperarAtivosPor(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		return orgaoJulgadorColegiadoOrgaoJulgadorDAO.recuperarAtivosPor(orgaoJulgadorColegiado);
	}
	
	public OrgaoJulgadorColegiadoOrgaoJulgador recuperarPorOrgaoJulgadorColegiadoEhOrgaoJulgador(OrgaoJulgadorColegiado orgaoJulgadorColegiado, OrgaoJulgador orgaoJulgador) {
		return orgaoJulgadorColegiadoOrgaoJulgadorDAO.recuperarPorOrgaoJulgadorColegiadoEhOrgaoJulgador(orgaoJulgadorColegiado, orgaoJulgador);
	}
	
	/**
	 * Método responsável por obter os {@link OrgaoJulgadorColegiadoOrgaoJulgador} associados 
	 * ao {@link OrgaoJulgadorColegiado} e que possuem período de ativação válido.
	 * 
	 * @param orgaoJulgadorColegiado {@link OrgaoJulgadorColegiado}.
	 * @return Os {@link OrgaoJulgadorColegiadoOrgaoJulgador} associados ao {@link OrgaoJulgadorColegiado} e que possuem período de ativação válido.
	 */
	public List<OrgaoJulgadorColegiadoOrgaoJulgador> obterAtivos(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		List<OrgaoJulgadorColegiadoOrgaoJulgador> resultado = new ArrayList<OrgaoJulgadorColegiadoOrgaoJulgador>(0);
		if (orgaoJulgadorColegiado != null) {
			for (OrgaoJulgadorColegiadoOrgaoJulgador ojcoj : orgaoJulgadorColegiado.getOrgaoJulgadorColegiadoOrgaoJulgadorList()) {
				if (this.isVinculoOJSingularComColegiadoAtivo(ojcoj)) {
					if (this.isVinculoOJSingularComColegiadoAtivo(ojcoj)) {
						resultado.add(ojcoj);
					}
				}
			}
		}
		return resultado;
	}
	
	/**
	 * Método responsável por obter os {@link OrgaoJulgadorColegiadoOrgaoJulgador} associados 
	 * ao {@link OrgaoJulgador} e que possuem período de ativação válido.
	 * 
	 * @param orgaoJulgador {@link OrgaoJulgador}.
	 * @return Os {@link OrgaoJulgadorColegiadoOrgaoJulgador} associados ao {@link OrgaoJulgador} e que possuem período de ativação válido.
	 */
	public List<OrgaoJulgadorColegiadoOrgaoJulgador> obterAtivos(OrgaoJulgador orgaoJulgador) {
		List<OrgaoJulgadorColegiadoOrgaoJulgador> resultado = new ArrayList<OrgaoJulgadorColegiadoOrgaoJulgador>(0);
		if (orgaoJulgador != null) {
			for (OrgaoJulgadorColegiadoOrgaoJulgador ojcoj : orgaoJulgador.getOrgaoJulgadorColegiadoOrgaoJulgadorList()) {
				if (this.isVinculoOJSingularComColegiadoAtivo(ojcoj)) {
					resultado.add(ojcoj);
				}
			}
		}
		return resultado;
	}
	
	public List<OrgaoJulgadorColegiadoOrgaoJulgador> obterAtivosOrdenados(OrgaoJulgadorColegiado orgaoJulgadorColegiado){
		List<OrgaoJulgadorColegiadoOrgaoJulgador> ojsAtivosOrdenados = this.obterAtivos(orgaoJulgadorColegiado);
		Collections.sort(ojsAtivosOrdenados, comparator);
		return ojsAtivosOrdenados;
	}
	
	public List<OrgaoJulgadorColegiadoOrgaoJulgador> obterAtivosOrdenados(OrgaoJulgador orgaoJulgador){
		List<OrgaoJulgadorColegiadoOrgaoJulgador> ojsAtivosOrdenados = this.obterAtivos(orgaoJulgador);
		Collections.sort(ojsAtivosOrdenados, comparator);
		return ojsAtivosOrdenados;
	}
	
	Comparator<OrgaoJulgadorColegiadoOrgaoJulgador> comparator = new Comparator<OrgaoJulgadorColegiadoOrgaoJulgador>() {
		@Override
		public int compare(OrgaoJulgadorColegiadoOrgaoJulgador o1, OrgaoJulgadorColegiadoOrgaoJulgador o2){
			if(o1.getOrdem() != null) {
				return o1.getOrdem().compareTo(o2.getOrdem());
			}
			else{
				return -1;
			}
		}
	};

	/**
	 * Verifica o vinculo entre orgao julgador singular e o colegiado esta ativo
	 * @param ojcOJ	objeto que representa a vinculacao entre o orgao julgador singular com o colegiado
	 * @return	verdadeiro se a data inicial for anterior a data atual, e a data final estiver vigente em relacao a 
	 * 			data inicial.
	 */
	public boolean isVinculoOJSingularComColegiadoAtivo(OrgaoJulgadorColegiadoOrgaoJulgador ojcOJ){
		Date dataAtual = new Date();
		boolean retorno = false;
		
		if (DateUtil.isDataMenorIgual(ojcOJ.getDataInicial(), dataAtual) && 
				(ojcOJ.getDataFinal() == null || DateUtil.isBetweenDates(dataAtual, ojcOJ.getDataInicial(), ojcOJ.getDataFinal()))) {
			retorno = true;
		}		
		return retorno;
	}
	
	/***
	 * Atualiza a lista de ordem dos OJs no OJC, utilizado quando é removido um OJ ou é alterada a ordem de um OJ dentro do OJC
	 * e precisa-se atualizar a ordenação dos demais OJs
	 * 
	 * @param ojcOJ
	 */
	public void reordenaDemaisOJsAtivos(OrgaoJulgadorColegiadoOrgaoJulgador ojcOJ) {
		if(ojcOJ != null && ojcOJ.getOrgaoJulgadorColegiado() != null) {
			OrgaoJulgadorColegiado ojc = ojcOJ.getOrgaoJulgadorColegiado();
			OrgaoJulgador oj = ojcOJ.getOrgaoJulgador();
			List<OrgaoJulgadorColegiadoOrgaoJulgador> ojsAtivosOrdenados = this.obterAtivosOrdenados(ojc);

			int ordemOjAlterado = 0;
			if(oj != null && ojcOJ.getOrdem() != null) {
				ordemOjAlterado = ojcOJ.getOrdem();
			}
			
			boolean houveAlteracao = false;
			boolean persistirElemento = false;
			int novaOrdem = 1;
			for (OrgaoJulgadorColegiadoOrgaoJulgador ojcoj : ojsAtivosOrdenados) {
				getDAO().refresh(ojcoj);
				// retira a referencia ao revisor, se o revisor não estiver na lista dos ativos
				if(ojcoj.getOrgaoJulgadorRevisor() != null && !ojsAtivosOrdenados.contains(ojcoj.getOrgaoJulgadorRevisor())) {
					ojcoj.setOrgaoJulgadorRevisor(null);
					houveAlteracao = true;
					persistirElemento = true;
				}
				
				if(!ojcoj.getOrgaoJulgador().equals(oj)) {
					if(novaOrdem == ordemOjAlterado) {
						novaOrdem++;
					}
					if(ojcoj.getOrdem() != novaOrdem) {
						ojcoj.setOrdem(novaOrdem);
						houveAlteracao = true;
						persistirElemento = true;
					}
					novaOrdem++;
				}
				if(persistirElemento) {
					getDAO().persist(ojcoj);
					persistirElemento = false;
				}
			}
			if(houveAlteracao) {
				getDAO().flush();
			}
		}
	}
	
	/***
	 * Dado um órgão julgador de referência <code>orgaoJulgadorReferencia</code>
	 * e de um colegiado com composicao ativa 
	 * retorna o próximo OJCOJ, utilizando a ordem do OJ no colegiado, se o orgaojulgadorReferencia
	 * não estiver mais ativo no Colegiado, verifica se foi sucedido por algum OJ ativo e utiliza a 
	 * ordem deste orgaojulgador sucessor como referência para encontrar o próximo orgaojulgador
	 * 
	 * @param orgaoJulgadorReferencia
	 * @param orgaoJulgadorColegiado
	 * @return
	 */
	public OrgaoJulgadorColegiadoOrgaoJulgador obterProximoOrgaoJulgadorComposicaoDadoUmOrgaoJulgadorReferencia(OrgaoJulgador orgaoJulgadorReferencia, OrgaoJulgadorColegiado orgaoJulgadorColegiado){
		List<OrgaoJulgadorColegiadoOrgaoJulgador> composicaoColegiadoAtiva = this.obterAtivosOrdenados(orgaoJulgadorColegiado);
		OrgaoJulgadorColegiadoOrgaoJulgador proximoOJCOJ = null;
		
		if(composicaoColegiadoAtiva.size() > 0 && orgaoJulgadorReferencia != null) {
			if(composicaoColegiadoAtiva.get(0).getOrgaoJulgador().getIdOrgaoJulgador() != orgaoJulgadorReferencia.getIdOrgaoJulgador()) {
				proximoOJCOJ = composicaoColegiadoAtiva.get(0);
			}
			
			int ordemOJReferencia = -1;
			for (OrgaoJulgadorColegiadoOrgaoJulgador orgaoJulgadorColegiadoOrgaoJulgador : composicaoColegiadoAtiva) {
				if(orgaoJulgadorColegiadoOrgaoJulgador.getOrgaoJulgador().getIdOrgaoJulgador() == orgaoJulgadorReferencia.getIdOrgaoJulgador()) {
					ordemOJReferencia = orgaoJulgadorColegiadoOrgaoJulgador.getOrdem();
				}else {
					if(ordemOJReferencia >= 0) {
						proximoOJCOJ = orgaoJulgadorColegiadoOrgaoJulgador;
						break;
					}
				}
			}
			
			// caso o OJ Referência não esteja mais no OJC, verifica qual foi o OJ que o sucedeu
			if(ordemOJReferencia < 0) {
				OrgaoJulgadorColegiadoOrgaoJulgador ojcojSucessor = this.recuperaOrgaoJulgadorSucessorAtual(orgaoJulgadorReferencia, orgaoJulgadorColegiado);
				if(ojcojSucessor != null) {
					ordemOJReferencia = ojcojSucessor.getOrdem();
					if(composicaoColegiadoAtiva.size() > ordemOJReferencia) {
						proximoOJCOJ = composicaoColegiadoAtiva.get(ordemOJReferencia);
					}
				}
			}
		}
		return proximoOJCOJ;
	}
	
	/***
	 * Dado um órgão julgador e um colegiado, verifica qual é o órgão julgador sucessor que atualmente compoe o colegiado atual
	 * 
	 * @param orgaoJulgadorReferencia
	 * @param orgaoJulgadorColegiado
	 * @return
	 */
	private OrgaoJulgadorColegiadoOrgaoJulgador recuperaOrgaoJulgadorSucessorAtual(OrgaoJulgador orgaoJulgadorReferencia, OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		SucessaoOJsColegiadoManager sucessaoOJsColegiadoManager = ComponentUtil.getComponent(SucessaoOJsColegiadoManager.class);
		return sucessaoOJsColegiadoManager.obterOrgaoJulgadorSucessorAtivo(orgaoJulgadorReferencia, orgaoJulgadorColegiado);
	}

	/***
	 * Gera uma ordenacao automatica sugerida para os OJs do Colegiado, verificações:
	 * 1. OJ presidente do colegiado;
	 * 2. próximos OJs dado a data de posse do magistrado titular no tribunal;
	 * 3. data de entrada do OJ no OJC;
	 * 4. data de nascimento do magistrado titular
	 * 
	 * @param ojc
	 * @throws PJeBusinessException 
	 */
	public void gerarOrdenacaoOJsColegiadoAutomaticamente(OrgaoJulgadorColegiado ojc) throws PJeBusinessException {
		this.removeOJsRevisoresColegiado(ojc);

		List<OrgaoJulgadorColegiadoOrgaoJulgador> composicaoColegiado = this.obterAtivosOrdenados(ojc);
		List<OrgaoJulgadorColegiadoOrgaoJulgador> composicaoColegiadoNovaOrdenacao = new ArrayList<>();

		OrgaoJulgador orgaoJulgadorInicial = ojc.getOrgaoJulgadorPresidente();
		OrgaoJulgadorColegiadoOrgaoJulgador ojcojInicial = null;
		// remove o OJ Inicial da lista de ordenacao
		if(orgaoJulgadorInicial != null) {
			int i=0;
			for (OrgaoJulgadorColegiadoOrgaoJulgador orgaoJulgadorColegiadoOrgaoJulgador : composicaoColegiado) {
				if(orgaoJulgadorColegiadoOrgaoJulgador.getOrgaoJulgador().getIdOrgaoJulgador() == orgaoJulgadorInicial.getIdOrgaoJulgador()) {
					ojcojInicial = orgaoJulgadorColegiadoOrgaoJulgador;
					break;
				}
				i++;
			}
			composicaoColegiado.remove(i);
		}
		
		Map<PessoaMagistrado, List<OrgaoJulgadorColegiadoOrgaoJulgador>> mapTitularesOJ = obterMapeamentoTitularesComposicaoColegiadoIndexadoPorMagistrado(composicaoColegiado);
		composicaoColegiadoNovaOrdenacao = this.geraListaComposicaoColegiadoOrdenadaPorMagistradosTitulares(mapTitularesOJ);
		
		// recoloca o OJ inicial no inicio da lista
		if(ojcojInicial != null) {
			composicaoColegiadoNovaOrdenacao.add(0, ojcojInicial);
		}
		
		int i = 0;
		int numOJsTotal = composicaoColegiadoNovaOrdenacao.size();
		for (OrgaoJulgadorColegiadoOrgaoJulgador ojcoj : composicaoColegiadoNovaOrdenacao) {
			getDAO().refresh(ojcoj);

			int ordem = (i+1);
			OrgaoJulgadorColegiadoOrgaoJulgador ojcojRevisor = null;
			if((i+1) < numOJsTotal){
				ojcojRevisor = composicaoColegiadoNovaOrdenacao.get((i+1));
			}else if(i != 0){
				ojcojRevisor = composicaoColegiadoNovaOrdenacao.get(0);
			}
			
			
			ojcoj.setOrdem(ordem);
			ojcoj.setOrgaoJulgadorRevisor(ojcojRevisor);
			getDAO().persist(ojcoj);

			i++;
		}
		
		getDAO().flush();
	}
	
	private void removeOJsRevisoresColegiado(OrgaoJulgadorColegiado ojc){
		getDAO().removeRevisoresOJsColegiado(ojc);
	}
	
	/**
	 * Inverte o mapeamento realizado em
	 * {@link #obterMapeamentoTitularesComposicaoColegiado(List)}
	 */
	private Map<PessoaMagistrado, List<OrgaoJulgadorColegiadoOrgaoJulgador>> obterMapeamentoTitularesComposicaoColegiadoIndexadoPorMagistrado(List<OrgaoJulgadorColegiadoOrgaoJulgador> composicaoColegiado) throws PJeBusinessException {
		Map<OrgaoJulgadorColegiadoOrgaoJulgador, PessoaMagistrado> mapTitularesOJIndexadoPorOJ = obterMapeamentoTitularesComposicaoColegiado(composicaoColegiado);
		
		Map<PessoaMagistrado, List<OrgaoJulgadorColegiadoOrgaoJulgador>> mapTitularesComposicaoIndexadoPorMagistrado = new HashMap<>();
		for (OrgaoJulgadorColegiadoOrgaoJulgador orgaoJulgadorColegiadoOrgaoJulgador : mapTitularesOJIndexadoPorOJ.keySet()) {
			PessoaMagistrado magistrado = mapTitularesOJIndexadoPorOJ.get(orgaoJulgadorColegiadoOrgaoJulgador);
			List<OrgaoJulgadorColegiadoOrgaoJulgador> ojcojList = null;
			if(mapTitularesComposicaoIndexadoPorMagistrado.get(magistrado) == null) {
				ojcojList = new ArrayList<>();
			}else {
				ojcojList = mapTitularesComposicaoIndexadoPorMagistrado.get(magistrado);
			}
			ojcojList.add(orgaoJulgadorColegiadoOrgaoJulgador);
			mapTitularesComposicaoIndexadoPorMagistrado.put(magistrado, ojcojList);
		}
		
		return mapTitularesComposicaoIndexadoPorMagistrado;
	}
	
	/**
	 * Dada uma lista de OJ por OJ colegiado é recuperado o titular de cada um
	 * dos OJs e adicionado o mapeamento em um objeto {@link Map}
	  * @throws PJeBusinessException 
	 */
	public Map<OrgaoJulgadorColegiadoOrgaoJulgador, PessoaMagistrado> obterMapeamentoTitularesComposicaoColegiado(
			List<OrgaoJulgadorColegiadoOrgaoJulgador> composicaoColegiado) throws PJeBusinessException {

		return obterMapeamentoTitularesComposicaoColegiado(composicaoColegiado, true);
	}
	
	/**
	 * Dada uma lista de OJ por OJ colegiado é recuperado o titular de cada um
	 * dos OJs e adicionado o mapeamento em um objeto {@link Map}. Não será adicionado caso o órgão não tenha titular e o parâmetro excluiOrgaoSemTitular tiver sido passado como true
	 * @throws PJeBusinessException 
	 */
	public Map<OrgaoJulgadorColegiadoOrgaoJulgador, PessoaMagistrado> obterMapeamentoTitularesComposicaoColegiado(
			List<OrgaoJulgadorColegiadoOrgaoJulgador> composicaoColegiado, boolean incluiOrgaoSemTitular) throws PJeBusinessException {

		Map<OrgaoJulgadorColegiadoOrgaoJulgador, PessoaMagistrado> mapTitularesComposicaoColegiado = new HashMap<>();

		for (OrgaoJulgadorColegiadoOrgaoJulgador componenteColegiado : composicaoColegiado) {
			PessoaMagistrado magistradoTitular = this.obterMagistradoTitular(componenteColegiado.getOrgaoJulgador(), componenteColegiado.getOrgaoJulgadorColegiado());
			if (incluiOrgaoSemTitular || magistradoTitular != null) {
				mapTitularesComposicaoColegiado.put(componenteColegiado, magistradoTitular);
			}
		}
		return mapTitularesComposicaoColegiado;
	}
	
	/**
	 * Método responsável por recuperar o magistrado titular do OJ em um
	 * determinado colegiadol
	 */
	public PessoaMagistrado obterMagistradoTitular(OrgaoJulgador orgaoJulgador, OrgaoJulgadorColegiado orgaoJulgadorColegiado) throws PJeBusinessException {
		PessoaMagistrado magistradoTitular = null;

		UsuarioLocalizacaoMagistradoServidor lotacaoMagistradoTitular = ComponentUtil.getComponent(UsuarioLocalizacaoMagistradoServidorManager.class)
				.obterLocalizacaoMagistradoPrincipal(orgaoJulgador, orgaoJulgadorColegiado);

		if (lotacaoMagistradoTitular != null) {
			magistradoTitular = ComponentUtil.getComponent(PessoaMagistradoManager.class)
					.findById(lotacaoMagistradoTitular.getUsuarioLocalizacao().getUsuario().getIdUsuario());
		}

		return magistradoTitular;
	}
	
	private List<OrgaoJulgadorColegiadoOrgaoJulgador> geraListaComposicaoColegiadoOrdenadaPorMagistradosTitulares(Map<PessoaMagistrado, List<OrgaoJulgadorColegiadoOrgaoJulgador>> mapTitularesOJ) {
		List<OrgaoJulgadorColegiadoOrgaoJulgador> composicaoColegiadoOrdenada = new ArrayList<>();
		List<Map<PessoaMagistrado, OrgaoJulgadorColegiadoOrgaoJulgador>> listTitularesOJs = new ArrayList<>();
		
		for (PessoaMagistrado magistrado : mapTitularesOJ.keySet()) {
			List<OrgaoJulgadorColegiadoOrgaoJulgador> ojcojList = mapTitularesOJ.get(magistrado);
			if(ojcojList != null) {
				for (OrgaoJulgadorColegiadoOrgaoJulgador ojcoj : ojcojList) {
					Map<PessoaMagistrado, OrgaoJulgadorColegiadoOrgaoJulgador> mapOJ = new HashMap<>();
					mapOJ.put(magistrado, ojcoj);
					listTitularesOJs.add(mapOJ);
				}
			}
		}

		Collections.sort(listTitularesOJs, new Comparator<Map<PessoaMagistrado, OrgaoJulgadorColegiadoOrgaoJulgador>>() {
			public int compare(Map<PessoaMagistrado, OrgaoJulgadorColegiadoOrgaoJulgador> mag1, Map<PessoaMagistrado, OrgaoJulgadorColegiadoOrgaoJulgador> mag2) {
				List<PessoaMagistrado> magistrado1List = new ArrayList<PessoaMagistrado>(mag1.keySet());
				PessoaMagistrado pessoaMagistrado1 = magistrado1List.get(0);
				OrgaoJulgadorColegiadoOrgaoJulgador ojcoj1 = mag1.get(pessoaMagistrado1);
				
				List<PessoaMagistrado> magistrado2List = new ArrayList<PessoaMagistrado>(mag2.keySet());
				PessoaMagistrado pessoaMagistrado2 = magistrado2List.get(0);
				OrgaoJulgadorColegiadoOrgaoJulgador ojcoj2 = mag2.get(pessoaMagistrado2);
				
				int comparacao = 0;
				if(pessoaMagistrado1 == null && pessoaMagistrado2 != null) {
					comparacao = 1;
				}else if(pessoaMagistrado1 != null && pessoaMagistrado2 == null) {
					comparacao = -1;					
				}else {
					if (comparacao == 0 && pessoaMagistrado1 != null && pessoaMagistrado2 != null && pessoaMagistrado1.getDataPosse() != null) {
						if(pessoaMagistrado2.getDataPosse() == null){
							comparacao = -1;
						}else{
							comparacao = pessoaMagistrado1.getDataPosse().compareTo(pessoaMagistrado2.getDataPosse());
						}
					}
					if(comparacao == 0 && ojcoj1.getDataInicial() != null) {
						if(ojcoj2.getDataInicial() == null){
							comparacao = -1;
						}else{
							comparacao = ojcoj1.getDataInicial().compareTo(ojcoj2.getDataInicial());
						}
					}
					if(comparacao == 0) {
						if(pessoaMagistrado1 != null && pessoaMagistrado2 != null && pessoaMagistrado1.getDataNascimento() != null) {
							if(pessoaMagistrado2.getDataNascimento() == null){
								comparacao = -1;
							}else{
								comparacao = pessoaMagistrado1.getDataNascimento().compareTo(pessoaMagistrado2.getDataNascimento());
							}
						} else {
							comparacao = -1;
						}
					}
				}
				return comparacao;
			}
		});
		
		for (Map<PessoaMagistrado, OrgaoJulgadorColegiadoOrgaoJulgador> map : listTitularesOJs) {
			List<PessoaMagistrado> magistradoList = new ArrayList<PessoaMagistrado>(map.keySet());
			PessoaMagistrado pessoaMagistrado = magistradoList.get(0);
			OrgaoJulgadorColegiadoOrgaoJulgador ojcoj = map.get(pessoaMagistrado);

			composicaoColegiadoOrdenada.add(ojcoj);
		}
		
		return composicaoColegiadoOrdenada;
	}
}
