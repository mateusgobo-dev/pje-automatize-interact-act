/**
 * 
 */
package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.util.DateUtil;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Iterators;

import br.com.infox.cliente.NumeroProcessoUtil;
import br.com.infox.pje.manager.PessoaFisicaManager;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ElasticDAO;
import br.jus.cnj.pje.entidades.vo.ConsultaDocumentoIndexadoVO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoEventoManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.pje.indexacao.Indexador;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Componente de controle da tela ConsultaPublica/consultapublica.seam
 * 
 * @author cristof
 *
 */
@Name("consultaPublicaEAction")
public class ConsultaPublicaEAction {

	@RequestParameter(value = "npg")
	private Integer page_;

	private String numero;

	private String nomeParte;

	private String nomeAdvogado;

	private String classeJudicial;

	private String inscricao;

	private String codigoOAB;
	
	private Integer total;
	
	private Integer elapsed;
	
	private TipoProcessoDocumento tpDoc;
	
	private List<TipoProcessoDocumento> tiposDocumentos;
	
	private ConsultaDocumentoIndexadoVO consulta;

	private List<JSONObject> processos;
	private List<JSONObject> documentos;
	
	@In
	private Indexador indexador;

	@In
	private ProcessoEventoManager processoEventoManager;
	
	@In
	private DocumentoJudicialService documentoJudicialService;
	
	@In
	private FacesMessages facesMessages;
	
	@In
 	private PessoaFisicaManager pessoaFisicaManager;
	
	@In
	private TipoProcessoDocumentoManager tipoProcessoDocumentoManager;
	
	@Logger
	private Log logger;
	
	public void pesquisarDocumento() throws Exception {
		if(tpDoc != null){
			getConsulta().getIdsTipoDocumento().add(tpDoc.getIdTipoProcessoDocumento());
		}
		documentos = documentoJudicialService.consultarDocumentosIndexados(getConsulta(), true);
	}

	public void pesquisar() {
		if(!validateFields()){
			facesMessages.add(Severity.WARN, "Pelo menos um dos campos de pesquisa deve estar preenchido corretamente.");
			return;
		}
		ElasticDAO<ProcessoTrf> processoDAO = new ElasticDAO<ProcessoTrf>() {
		};
		processoDAO.setIndexador(indexador);
		Search s = new Search(ProcessoTrf.class);
		s.setMax(30);
		try {
			carregarCriterioNumeroProcesso(s);
			carregarCriterioParte(s);
			carregarCriterioNomeAdvogado(s);
			carregarCriterioClasse(s);
			carregarCriterioOAB(s);
			s.addCriteria(Criteria.equals("segredoJustica", false).asFilter());
			processoDAO.setIndexador(indexador);
			if(s.getCriterias().isEmpty()){
				facesMessages.add(Severity.WARN, "Ao menos um dos critérios deve estar definido!");
				return;
			}
			JSONObject procs_ = processoDAO.search(s);
			if(procs_ != null && Iterators.size(procs_.keys()) > 0){
				JSONArray aux = ((JSONArray) ((JSONObject) procs_.get("hits")).get("hits"));
				total = ((JSONObject) procs_.get("hits")).getInt("total");
				if(total > 30){
					facesMessages.add(Severity.WARN, "Sua consulta retornou mais que 30 resultados. Por favor, refine-a.");
				}else if(total == 0){
					facesMessages.add(Severity.INFO, "Não foram encontrados resultados para a pesquisa.");
				}
				elapsed = procs_.getInt("took");
				List<JSONObject> ret = new ArrayList<JSONObject>(aux.length());
				for(int i = 0; i < aux.length(); i++){
					ret.add((JSONObject) aux.get(i));
				}
				processos = ret;
			}else{
				processos = Collections.emptyList();
			}
		} catch (Throwable e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao realizar a consulta.");
			e.printStackTrace();
		}
	}
	
	public List<TipoProcessoDocumento> getTiposDocumento(){
		if(tiposDocumentos == null){
			tiposDocumentos = tipoProcessoDocumentoManager.getTipoDocumentoAtoMagistradoList();
		}
		return tiposDocumentos;
	}
	
	/**
	 * Insere na pesquisa os critérios referente ao nome e documento da parte.
	 * A função restringe a pesquisa apenas a pessoas maiores de idade.
	 * 
	 * 
	 * @link	http://www.cnj.jus.br/jira/browse/PJESPRTII-2
	 * @param   s	Objeto Search onde deve ser inseridas as Criterias
	*/
	
	private void carregarCriterioParte(Search s) throws Exception {
		String inscricaoMF = inscricao;
		if(!StringUtils.isEmpty(inscricao)){
			inscricaoMF = StringUtil.removeNaoNumericos(StringUtil.fullTrim(inscricao));
		}
		
		
		ProcessoTrfManager processoTrfManager = ComponentUtil.getComponent(ProcessoTrfManager.NAME);
		s.addCriteria(processoTrfManager.getCriteriasPesquisaNomeDocumento(nomeParte, inscricaoMF));
		s.addCriteria(Criteria.not(Criteria.in("processoParteList.pessoa.idPessoa", pessoaFisicaManager.getMenores(nomeParte, inscricaoMF).toArray()))); //exclui os menores de idade da pesquisa
	}

	private boolean validateFields(){
		boolean valid = false;
		if(!StringUtils.isEmpty(numero) && !StringUtil.removeNaoNumericos(numero).isEmpty()){
			if(StringUtil.removeNaoNumericos(numero).length() < 3){
				facesMessages.add(Severity.WARN, "São necessários pelo menos três dígitos (N-DV) para a realização da pesquisa por número do processo.");
				return false;
			}else{
				valid=true;
			}
		}
		if(!StringUtils.isEmpty(nomeParte)){
			if(StringUtil.fullTrim(nomeParte).split(" ").length < 2){
				facesMessages.add(Severity.WARN, "Somente é possível pesquisar por nomes quando são informadas ao menos duas partes do nome");
				return false;
			}else{
				valid = true;
			}
		}
		if(!StringUtils.isEmpty(nomeAdvogado)){
			if(StringUtil.fullTrim(nomeAdvogado).split(" ").length < 2){
				facesMessages.add(Severity.WARN, "Somente é possível pesquisar por nomes quando são informadas ao menos duas partes do nome");
			}else{
				valid = true;
			}
		}
		if(!valid){
			if(inscricao != null && !StringUtil.fullTrim(StringUtil.removeNaoNumericos(inscricao)).isEmpty()){
				valid = true;
			}
			if(classeJudicial != null && !StringUtil.fullTrim(classeJudicial).isEmpty()){
				valid = true;
			}
			if(codigoOAB != null && !StringUtil.fullTrim(StringUtil.removeNaoNumericos(codigoOAB)).isEmpty()){
				valid = true;
			}
		}
		return valid;
	}
	
	private void carregarCriterioNumeroProcesso(Search s) throws NoSuchFieldException, PJeBusinessException{
		if(!StringUtils.isEmpty(numero)){
			String nc = StringUtil.removeNaoNumericos(numero);
			nc = nc.substring(0, nc.length() > 20 ? 20 : nc.length());
			if(nc.length() == 20){
				s.addCriteria(						
						Criteria.equals("numeroProcesso", NumeroProcessoUtil.mascaraNumeroProcesso(nc)));
			}else if(nc.length() > 2){
				s.addCriteria(findByNumeracaoUnicaParcial("", nc));
			}else{
				facesMessages.add(Severity.WARN, "O número do processo deve ter pelo menos 3 caracteres (N-DV).");
			}
		}
	}
	
	private void carregarCriterioNomeAdvogado(Search s) throws NoSuchFieldException{
		if(!StringUtils.isEmpty(nomeAdvogado)){
			s.addCriteria(Criteria.contains("processoParteList.processoParteRepresentanteList.representante.pessoaDocumentoIdentificacaoList.nome", nomeAdvogado));
			s.addCriteria(Criteria.equals("processoParteList.processoParteRepresentanteList.tipoRepresentante.tipoParte", "ADVOGADO"));
		}
	}
	
	private void carregarCriterioClasse(Search s) throws NoSuchFieldException{
		if(!StringUtils.isEmpty(classeJudicial)){
			s.addCriteria(Criteria.or(
				Criteria.equals("classeJudicial.codClasseJudicial", classeJudicial).asFilter(),
				Criteria.contains("classeJudicial.classeJudicial", classeJudicial),
				Criteria.equals("classeJudicial.classeJudicialSigla", classeJudicial)));
		}
	}
	
	private void carregarCriterioOAB(Search s) throws NoSuchFieldException{
		if (!StringUtils.isEmpty(codigoOAB)){
			if(!codigoOAB.matches("\\d\\d\\d\\d\\d\\d")){
				facesMessages.add(Severity.WARN, "Por favor, insira o número da OAB no formato NNNNNN, colocando zeros à esquerda.");
				return;
			}
			String crit = codigoOAB.substring(codigoOAB.indexOf("-") + 1).replaceFirst("^0+(?!$)", "");
			s.addCriteria(Criteria.equals("processoParteList.processoParteRepresentanteList.representante.pessoaDocumentoIdentificacaoList.tipoDocumento.codTipo","OAB"));
			s.addCriteria(Criteria.startsWith("processoParteList.processoParteRepresentanteList.representante.pessoaDocumentoIdentificacaoList.numeroDocumento", crit));
		}
	}
	
	private Criteria findByNumeracaoUnicaParcial(String prefix, String numero) throws PJeBusinessException {
		String nu = numero.replaceAll("\\D", "");
		int comp = nu.length();
		if (comp >= 20){
			Integer numeroSequencia = Integer.parseInt(nu.substring(0, 7));
			Integer numeroDigitoVerificador = Integer.parseInt(nu.substring(7, 9));
			Integer ano = Integer.parseInt(nu.substring(9, 13));
			Integer segmento = Integer.parseInt(nu.substring(13, 14));
			Integer tribunal = Integer.parseInt(nu.substring(14, 16));
			Integer numeroOrigem = Integer.parseInt(nu.substring(16));
			return findByNumeracaoUnica(prefix, numeroSequencia, numeroDigitoVerificador, ano, segmento, tribunal, numeroOrigem);
		}else if (comp < 20 && comp > 13){
			Integer origem = Integer.parseInt(nu.substring(comp - 4));
			Integer tribunal = Integer.parseInt(nu.substring(comp - 6, comp - 4));
			Integer segmento = Integer.parseInt(nu.substring(comp - 7, comp - 6));
			Integer ano = Integer.parseInt(nu.substring(comp - 11, comp - 7));
			Integer dv = Integer.parseInt(nu.substring(comp - 13, comp - 11));
			Integer nnn = Integer.parseInt(nu.substring(0, comp - 13));
			return findByNumeracaoUnica(prefix, nnn, dv, ano, segmento, tribunal, origem);
		}else if (comp <= 13 && comp >= 7){
			List<Criteria> crits = new ArrayList<Criteria>();
			Integer ano = Integer.parseInt(nu.substring(comp - 4));
			Integer dv = Integer.parseInt(nu.substring(comp - 6, comp - 4));
			Integer nnn = Integer.parseInt(nu.substring(0, comp - 6));
			crits.add(findByNumeracaoUnica(prefix, nnn, dv, ano, null, null, null));
			if (comp < 10){
				dv = Integer.parseInt(nu.substring(comp - 2));
				nnn = Integer.parseInt(nu.substring(0, comp - 2));
				crits.add(findByNumeracaoUnica(prefix, nnn, dv, null, null, null, null));
			}
			return Criteria.or(crits.toArray(new Criteria[crits.size()])); 
		}else if (comp >= 3){
			List<Criteria> crits = new ArrayList<Criteria>();
			Integer dv = Integer.parseInt(nu.substring(comp - 2));
			Integer nnn = Integer.parseInt(nu.substring(0, comp - 2));
			crits.add(findByNumeracaoUnica(prefix, nnn, dv, null, null, null, null));
			crits.add(findByNumeracaoUnica(prefix, Integer.parseInt(nu), null, null, null, null, null));
			return Criteria.or(crits.toArray(new Criteria[crits.size()]));
		}else{
			throw new PJeBusinessException("É necessário inserir pelo menos três dígitos para pesquisar pelo número do processo (N-DV).");
		}
	}

	/**
	 * Monta um critério de consulta por número de processo, assegurando que campos nulos não sejam considerados.
	 * 
	 * @param prefix o caminho JavaBean até o processo judicial na pesquisa
	 * @param numero o campo NNNNNNN
	 * @param dv o campo DV
	 * @param ano o campo AAAA
	 * @param segmento o campo J
	 * @param tribunal o campo TR
	 * @param origem o campo OOOO
	 * @return o critério montado
	 */
	private Criteria findByNumeracaoUnica(String prefix, Integer numero, Integer dv, Integer ano, Integer segmento, Integer tribunal, Integer origem){
		if (numero == null && dv == null && ano == null && (segmento == null || tribunal == null) && origem == null){
			throw new IllegalArgumentException("A consulta segundo a numeração única exige ao menos um dos campos de sua composição.");
		}
		List<Criteria> crits = new ArrayList<Criteria>();
		if (numero != null)
			crits.add(Criteria.equals(prefix + "numeroSequencia", numero));
		if (dv != null)
			crits.add(Criteria.equals(prefix + "numeroDigitoVerificador", dv));
		Integer numeroOrgaoJustica = null;
		if (ano != null)
			crits.add(Criteria.equals(prefix + "ano", ano));
		if (segmento != null && tribunal != null){
			numeroOrgaoJustica = segmento * 100 + tribunal;
			crits.add(Criteria.equals(prefix + "numeroOrgaoJustica", numeroOrgaoJustica));
		}
		if (origem != null)
			crits.add(Criteria.equals(prefix + "numeroOrigem", origem));
		return Criteria.and(crits.toArray(new Criteria[crits.size()]));
	}
	
	public String recuperarAssunto(JSONObject processo){
		String retorno = "";
		
		if(processo.has("assuntos")){
			try {
				JSONArray  assuntos = processo.getJSONArray("assuntos");
				retorno = assuntos.getJSONObject(0).getString("assunto");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return retorno;
	}
	
	public String recuperarNomePartes(JSONObject processo){
		if(!processo.has("partes")){
			return "";
		}
		JSONArray partes;
		String nomeAutor = null;
		String nomeReu = null;
		try {
			partes = processo.getJSONArray("partes");
			for(int i = 0; i < partes.length(); i++){
				JSONObject p = partes.getJSONObject(i);
				if(!p.getBoolean("sigilosa") && nomeAutor == null && p.getString("polo").equalsIgnoreCase("ativo")){
					nomeAutor = p.getJSONObject("pessoa").getString("nome");
				}
				if(!p.getBoolean("sigilosa") && nomeReu == null && p.getString("polo").equalsIgnoreCase("passivo")){
					nomeReu = p.getJSONObject("pessoa").getString("nome");
				}
				if(nomeAutor != null && nomeReu != null){
					break;
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return String.format("%s X %s", nomeAutor, nomeReu);
	}
	
	public String recuperaMovimentacaoRecente(Integer idProcesso) {
		try{
			ProcessoEvento movimento = processoEventoManager.recuperaUltimaMovimentacaoPublica(idProcesso, new Date());
			if(movimento != null){
				return movimento.getTextoFinalExterno() + " (" + DateUtil.formatDate(movimento.getDataAtualizacao(),"dd/MM/yyyy HH:mm:ss") + ")";
			}
		}catch (PJeBusinessException e){
			facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar a movimentação mais recente.");
		}
		return null;
	}
	
	public List<JSONObject> getArray(JSONObject obj, String path){
		if(!obj.has(path)){
			return Collections.emptyList();
		}
		List<JSONObject> ret = new ArrayList<JSONObject>();
		try {
			JSONArray arr = obj.getJSONArray(path);
			ret = new ArrayList<JSONObject>(arr.length());
			for(int i = 0; i < arr.length(); i++){
				ret.add(arr.getJSONObject(i));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public String getNomeAdvogado() {
		return nomeAdvogado;
	}

	public void setNomeAdvogado(String nomeAdvogado) {
		this.nomeAdvogado = nomeAdvogado;
	}

	public String getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(String classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public String getInscricao() {
		return inscricao;
	}

	public void setInscricao(String inscricao) {
		this.inscricao = inscricao;
	}

	public String getCodigoOAB() {
		return codigoOAB;
	}

	public void setCodigoOAB(String codigoOAB) {
		this.codigoOAB = codigoOAB;
	}

	public Integer getPage_() {
		return page_;
	}

	public List<JSONObject> getProcessos() {
		return processos;
	}
	
	public List<JSONObject> getDocumentos() {
		return documentos;
	}

	public Integer getTotal() {
		return total;
	}
	
	public void setTotal(Integer total) {
		this.total = total;
	}

	public Integer getElapsed() {
		return elapsed;
	}

	public void setElapsed(Integer elapsed) {
		this.elapsed = elapsed;
	}

	public TipoProcessoDocumento getTpDoc() {
		return tpDoc;
	}

	public void setTpDoc(TipoProcessoDocumento tpDoc) {
		this.tpDoc = tpDoc;
	}

	public ConsultaDocumentoIndexadoVO getConsulta() {
		if(consulta == null){
			consulta = new ConsultaDocumentoIndexadoVO();
		}
		return consulta;
	}

	public void setConsulta(ConsultaDocumentoIndexadoVO consulta) {
		this.consulta = consulta;
	}
}
