package br.jus.cnj.pje.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.infox.cliente.home.ClasseJudicialHome;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeRestException;
import br.jus.cnj.pje.nucleo.manager.ClasseJudicialManager;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.webservice.client.PjeApiClient;
import br.jus.pje.nucleo.beans.criminal.TipoProcessoEnum;
import br.jus.pje.nucleo.dto.criminal.TipoEventoCriminalDTO;
import br.jus.pje.nucleo.entidades.ClasseJudicial;

@Scope(ScopeType.EVENT)
@Name(DadosCriminaisClasseJudicialAction.NAME)
public class DadosCriminaisClasseJudicialAction {

	public static final String NAME = "dadosCriminaisClasseJudicialAction";	
	
	public static final String TIPOS_EVENTOS_CRIMINAIS_PATH = "/criminal/api/v2/tipos-eventos-criminais";
	
	@In(create = true)
	private PjeApiClient pjeApiClient;
	
	@In
	private ClasseJudicialManager classeJudicialManager;
	
	private ObjectMapper objectMapper;
	
	private List<TipoEventoCriminalDTO> listaEventosCriminais;
	
	private ClasseJudicial classeJudicial;
	
	private String codTipoEvento;
	
	@Create
	public void init() {
		try {
			if(ClasseJudicialHome.instance().getInstance() != null && ClasseJudicialHome.instance().getInstance().getIdClasseJudicial() != 0) {
				this.classeJudicial = ClasseJudicialHome.instance().getInstance();
			} else if(ProcessoTrfHome.instance().getInstance() != null){
				this.classeJudicial = ProcessoTrfHome.instance().getInstance().getClasseJudicial();
			}
			this.carregarEventosCriminais();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void salvar() {
		try {
			ClasseJudicial classeJudicial = this.classeJudicialManager.findById(ClasseJudicialHome.instance().getInstance().getIdClasseJudicial());
			classeJudicial.setTipoEventoCriminalInicial(this.classeJudicial.getTipoEventoCriminalInicial());
			this.classeJudicialManager.persistAndFlush(classeJudicial);
			FacesMessages.instance().add(StatusMessage.Severity.INFO, "Evento criminal inicial salvo com sucesso.");
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
	}
	
	public String getDescricaoTipoEventoCriminalInicial() {
		String ret = "";
		
		if(this.classeJudicial != null && !CollectionUtilsPje.isEmpty(this.listaEventosCriminais)) {
			ret = this.listaEventosCriminais.stream()
					.filter(tipoEventoCriminal -> tipoEventoCriminal.getCodTipoIc().equals(this.classeJudicial.getTipoEventoCriminalInicial()))
					.findFirst()
					.get().getDescricao();
		}
		
		return ret;
	}
	
	public List<TipoEventoCriminalDTO> getListaEventosCriminais() {
		return listaEventosCriminais;
	}
	
	public void setListaEventosCriminais(List<TipoEventoCriminalDTO> listaEventosCriminais) {
		this.listaEventosCriminais = listaEventosCriminais;
	}
	
	public String getCodTipoEvento() {
		return codTipoEvento;
	}

	public void setCodTipoEvento(String codTipoEvento) {
		this.codTipoEvento = codTipoEvento;
	}
	
	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}
	
	private TypeReference<List<TipoEventoCriminalDTO>> getType() {
		return new TypeReference<List<TipoEventoCriminalDTO>>() {};
	}
	
	private ObjectMapper getMapper() {
		if(this.objectMapper == null) {
			this.objectMapper = new ObjectMapper();
		}
		
		return objectMapper;
	}
	
	private void carregarEventosCriminais() throws PJeRestException, JsonParseException, JsonMappingException, IOException {
		String tipoProcesso = classeJudicialManager.isClasseCriminal(classeJudicial) ? TipoProcessoEnum.CRI.name() : TipoProcessoEnum.INF.name(); 
		String strResponse = this.pjeApiClient.getStringValueSimple("/criminal/api/v2/tipos-eventos-criminais" + "/tipo-processo/" + tipoProcesso);
		List<TipoEventoCriminalDTO> eventos = this.getMapper().readValue(strResponse, getType());
		this.listaEventosCriminais = !CollectionUtilsPje.isEmpty(eventos) ? eventos : new ArrayList<>(0);
	}	
	
}
