package br.jus.pje.api.controllers.v1;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.manager.PessoaManager;
import br.jus.cnj.pje.pjecommons.model.services.PjeResponse;
import br.jus.cnj.pje.pjecommons.model.services.PjeResponseStatus;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.Pessoa;
import br.jus.cnj.pje.webservice.PjeJSONProvider;
import br.jus.pje.api.converters.PessoaConverter;
import br.jus.pje.api.filter.ApiCondition;
import br.jus.pje.api.filter.ApiConditionOperatorEnum;
import br.jus.pje.api.filter.ApiCriteria;
import br.jus.pje.api.filter.ApiFilter;
import br.jus.pje.api.filter.ApiFilterUtil;
import br.jus.pje.nucleo.util.StringUtil;

@Name(PessoaRestController.NAME)
@Scope(ScopeType.EVENT)
@Path("/pje-legacy/api/v1")
@Restrict("#{identity.loggedIn}")
public class PessoaRestController implements Serializable {
	
	
	private static final long serialVersionUID = 1L;
	public static final String NAME = "pessoaRestController"; 
	
	@Logger
	private Log logger;	
	
	@In
	private PessoaManager pessoaManager; 
	
	private PessoaConverter pessoaConverter = new PessoaConverter();
	
	private ApiFilterUtil apiFilterUtil;
	
	private List<String> mensagens = new ArrayList<String>();
	
	@Create
	public void init() {
		this.apiFilterUtil = new ApiFilterUtil();
		this.pessoaConverter = new PessoaConverter();
	}	
	
	@GET
	@Path("/pessoas")
	@Produces(MediaType.APPLICATION_JSON)
	public PjeResponse<List<Pessoa>> recuperarPessoasFisicasComFiltro(@QueryParam("filter") String filter) {
		PjeResponse<List<Pessoa>> response = new PjeResponse<>(PjeResponseStatus.OK, "200", null, new ArrayList<>());
		
		if(!StringUtil.isEmpty(filter)) {
			try {
				List<Pessoa> listaPessoas = new ArrayList<>();
				JsonNode rootNode = this.getMapper().readTree(filter);
				ApiFilter apiFilter = this.apiFilterUtil.convertToApiFilter(rootNode);
				ApiCriteria criteriaDocumentosIdentificatorios = this.apiFilterUtil.findCriteriaByField(apiFilter, "dadosBasicos.numeroDocumentoPrincipal");
				ApiCriteria criteriaDocumentosIdentificatoriosNome = this.apiFilterUtil.findCriteriaByField(apiFilter, "dadosBasicos.nome");
				
				if(criteriaDocumentosIdentificatorios != null || criteriaDocumentosIdentificatoriosNome != null) {
					listaPessoas = filterByNomeOuDocumentoDeIdentificacao(criteriaDocumentosIdentificatorios,criteriaDocumentosIdentificatoriosNome);
				}
				response.setResult(listaPessoas);
			} catch (IOException e) {
				logger.error(e.getLocalizedMessage());
				mensagens.add("Não foi possível converter o filtro");
				response.setMessages(mensagens);
			}
		}
		
		return response;
	}

	private List<Pessoa> filterByNomeOuDocumentoDeIdentificacao(ApiCriteria criteriaDocumentosIdentificatorios, ApiCriteria criteriaDocumentosIdentificatoriosNome) {
		List<Pessoa> listaPessoas = new ArrayList<>();
		ApiCondition conditionContainsCpf = null;
		ApiCondition conditionContainsNome = null;
		conditionContainsCpf = this.apiFilterUtil.findConditionByOperator(criteriaDocumentosIdentificatorios, ApiConditionOperatorEnum.CONTAINS);
		conditionContainsNome = this.apiFilterUtil.findConditionByOperator(criteriaDocumentosIdentificatoriosNome, ApiConditionOperatorEnum.CONTAINS);					
		
		if (conditionContainsCpf != null || conditionContainsNome != null) {
			String nome = conditionContainsNome.getValue().replaceAll("\"", "");
			String documentoIdentificacao = conditionContainsCpf.getValue().replaceAll("\"", "");
			List<br.jus.pje.nucleo.entidades.Pessoa> listaPessoasPJe = new ArrayList<>();			
			listaPessoasPJe = findByNomeOrDocumentoIdentificacao(nome, documentoIdentificacao);		
			
			for (br.jus.pje.nucleo.entidades.Pessoa pf : listaPessoasPJe) {
				listaPessoas.add(this.pessoaConverter.convertFrom(pf));
			}
		}
		return listaPessoas;
	}

	private List<br.jus.pje.nucleo.entidades.Pessoa> findByNomeOrDocumentoIdentificacao(String nome,
			String documentoIdentificacao) {
		
		if(documentoIdentificacao.isEmpty() && !nome.isEmpty()) 
			return this.pessoaManager.findByName(nome, 0, 25);
		
		String tipoDocumentoIdentificacao = documentoIdentificacao.length() <= 14 ? "CPF" : "CPJ";
		documentoIdentificacao = InscricaoMFUtil.acrescentaMascaraMF(documentoIdentificacao);
		
		if(nome.isEmpty()) 
			return this.pessoaManager.findByDocument(documentoIdentificacao, tipoDocumentoIdentificacao);
		
		return this.pessoaManager.findByNomeAndDocumentoIdentificacao(nome, tipoDocumentoIdentificacao, documentoIdentificacao);
	}
	
	private ObjectMapper getMapper() {
		PjeJSONProvider jsonProvider = new PjeJSONProvider();
		return jsonProvider.getObjectMapper();
	}

}
