package br.jus.pje.api.converters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.pje.manager.PessoaProcuradorProcuradoriaManager;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.Endereco;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.Parte;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.Pessoa;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.RepresentanteProcessual;
import br.jus.pje.nucleo.dto.TipoParteDTO;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;

public class ParteConverter {
	public Parte convertParteFrom(ProcessoParte processoParte) {
		Parte parte = new Parte();
		List<ProcessoParteRepresentante> parteRepresentanteListAtivos = processoParte.getProcessoParteRepresentanteListAtivos();
		
		parte.setPessoa(this.buildPessoaFrom(processoParte));		
		parte.setAdvogado(this.buildListRepresentanteProcessual(parteRepresentanteListAtivos));
		parte.setPessoaProcessualRelacionada(this.buildListParteRepresentante(parteRepresentanteListAtivos));		
		
		parte.setAny(buildAny(processoParte));

		return parte;
	}	

	public Parte convertParteFrom(ProcessoParteRepresentante processoParteRepresentante) {
		Parte parte = new Parte();
		parte.setPessoa(this.buildPessoaFrom(processoParteRepresentante.getProcessoParte()));
		return parte;
	}			

	private Pessoa buildPessoaFrom(ProcessoParte processoParte) {
		Pessoa pessoa = new Pessoa();
		
		PessoaConverter pessoaConverter = new PessoaConverter();
		pessoa = pessoaConverter.convertFrom(processoParte.getPessoa());
		
		EnderecoConverter enderecoConverter = new EnderecoConverter();
		
		List<Endereco> enderecos = enderecoConverter.convertFrom(processoParte.getProcessoParteEnderecoList());
		pessoa.setEndereco(enderecos);
		
		if(pessoa.getAny() == null) {
			pessoa.setAny(new ArrayList<Object>());	
		}
		
		Map<String, String> atraiCompetenciaMap = new HashMap<String, String>();
		atraiCompetenciaMap.put("AtraiCompetencia", (processoParte.getPessoa().getAtraiCompetencia() != null && processoParte.getPessoa().getAtraiCompetencia() ? "S":"N"));
		pessoa.getAny().add(atraiCompetenciaMap);
		
		Map<String, String> ParteAssistidaPorProcuradorMap = new HashMap<String, String>();
		atraiCompetenciaMap.put("ParteAssistidaPorProcurador", (isParteAssistidaPorProcurador(processoParte.getPessoa().getIdPessoa()) ? "S":"N"));
		pessoa.getAny().add(ParteAssistidaPorProcuradorMap);
		
		return pessoa;
	}
	
	private boolean isParteAssistidaPorProcurador(int idPessoa){
		PessoaProcuradorProcuradoriaManager procuradorProcuradoriaManager = ComponentUtil.getComponent(PessoaProcuradorProcuradoriaManager.NAME);
		return procuradorProcuradoriaManager.existeProcuradorEntidade(idPessoa);
	}
	
	private List<RepresentanteProcessual> buildListRepresentanteProcessual(List<ProcessoParteRepresentante> listOfProcessoParteRepresentante){
		List<RepresentanteProcessual> representantesProcessuais = new ArrayList<>();
		RepresentanteProcessualConverter converter = new RepresentanteProcessualConverter();
		for (ProcessoParteRepresentante processoParteRepresentante : listOfProcessoParteRepresentante) {
			if(isTipoParteAdvogado(processoParteRepresentante)) {
				RepresentanteProcessual representanteProcessual = converter.convertFrom(processoParteRepresentante);
				representantesProcessuais.add(representanteProcessual);
			}
		}		
		return representantesProcessuais;
	}
	
	private List<Parte> buildListParteRepresentante(List<ProcessoParteRepresentante> listOfProcessoParteRepresentante){
		List<Parte> representantesLegais = new ArrayList<>();		
		for (ProcessoParteRepresentante processoParteRepresentante : listOfProcessoParteRepresentante) {			
			Parte parte = this.convertParteFrom(processoParteRepresentante.getParteRepresentante());
			representantesLegais.add(parte);
		}		
		return representantesLegais;
	}
	
	private boolean isTipoParteAdvogado(ProcessoParteRepresentante processoParteRepresentante) {
		return ParametroUtil.instance().getTipoParteAdvogado().equals(processoParteRepresentante.getTipoRepresentante());
	}
	
	private List<Object> buildAny(ProcessoParte processoParte){
		List<Object> any = new ArrayList<>();
		Map<String,Object> outros = new HashMap<>();
		
		outros.put("tipoParte", new TipoParteDTO(processoParte.getTipoParte()));
		
		any.add(outros);		
		
		return any;
	}
}
