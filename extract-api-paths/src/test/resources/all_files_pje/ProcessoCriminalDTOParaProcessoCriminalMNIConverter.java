/**
 * ProcessoCriminalDTOParaProcessoCriminalMNIConverter
 * 
 * Data de cria??o: 18/01/2018
 */
package br.jus.cnj.pje.intercomunicacao.v223.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.intercomunicacao.v223.beans.CadastroIdentificador;
import br.jus.cnj.intercomunicacao.v223.beans.Endereco;
import br.jus.cnj.intercomunicacao.v223.criminal.CaracteristicaFisica;
import br.jus.cnj.intercomunicacao.v223.criminal.Denuncia;
import br.jus.cnj.intercomunicacao.v223.criminal.Dispositivo;
import br.jus.cnj.intercomunicacao.v223.criminal.EventoCriminal;
import br.jus.cnj.intercomunicacao.v223.criminal.FatoCriminal;
import br.jus.cnj.intercomunicacao.v223.criminal.Filiacao;
import br.jus.cnj.intercomunicacao.v223.criminal.Fuga;
import br.jus.cnj.intercomunicacao.v223.criminal.MotivoPrisao;
import br.jus.cnj.intercomunicacao.v223.criminal.MotivoSoltura;
import br.jus.cnj.intercomunicacao.v223.criminal.OrgaoProcedimentoOrigem;
import br.jus.cnj.intercomunicacao.v223.criminal.Parte;
import br.jus.cnj.intercomunicacao.v223.criminal.Prisao;
import br.jus.cnj.intercomunicacao.v223.criminal.ProcedimentoOrigem;
import br.jus.cnj.intercomunicacao.v223.criminal.Processo;
import br.jus.cnj.intercomunicacao.v223.criminal.Soltura;
import br.jus.cnj.intercomunicacao.v223.criminal.UnidadePrisional;
import br.jus.cnj.pje.intercomunicacao.v223.util.ConversorUtil;
import br.jus.cnj.pje.nucleo.manager.CaracteristicaFisicaManager;
import br.jus.cnj.pje.nucleo.manager.PessoaFiliacaoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaManager;
import br.jus.cnj.pje.nucleo.manager.PessoaNomeAlternativoManager;
import br.jus.pje.nucleo.beans.criminal.ConteudoInformacaoCriminalBean;
import br.jus.pje.nucleo.beans.criminal.EventoCriminalBean;
import br.jus.pje.nucleo.beans.criminal.FugaBean;
import br.jus.pje.nucleo.beans.criminal.IncidenciaPenalBean;
import br.jus.pje.nucleo.beans.criminal.PrisaoBean;
import br.jus.pje.nucleo.beans.criminal.SolturaBean;
import br.jus.pje.nucleo.beans.criminal.UnidadePrisionalBean;
import br.jus.pje.nucleo.dto.DispositivoDTO;
import br.jus.pje.nucleo.dto.InformacaoCriminalDTO;
import br.jus.pje.nucleo.dto.MotivoPrisaoDTO;
import br.jus.pje.nucleo.dto.MotivoSolturaDTO;
import br.jus.pje.nucleo.dto.MunicipioDTO;
import br.jus.pje.nucleo.dto.NormaDTO;
import br.jus.pje.nucleo.dto.ParteDTO;
import br.jus.pje.nucleo.dto.ProcessoCriminalDTO;
import br.jus.pje.nucleo.dto.ProcessoProcedimentoOrigemDTO;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFiliacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaNomeAlternativo;
import br.jus.pje.nucleo.enums.TipoFiliacaoEnum;
import br.jus.pje.nucleo.enums.TipoNomeAlternativoEnum;

/**
 * Conversor de ProcessoCriminalDTO para Processo (criminal mni).
 * 
 * @author Adriano Pamplona
 */
@Name(ProcessoCriminalDTOParaProcessoCriminalMNIConverter.NAME)
public class ProcessoCriminalDTOParaProcessoCriminalMNIConverter
		extends
		IntercomunicacaoConverterAbstrato<ProcessoCriminalDTO, Processo> {

	public static final String NAME = "processoCriminalDTOParaProcessoCriminalMNIConverter";
	
	@In
	private PessoaNomeAlternativoManager pessoaNomeAlternativoManager;
	
	@In
	private PessoaFiliacaoManager pessoaFiliacaoManager;
	
	@In
	private CaracteristicaFisicaManager caracteristicaFisicaManager;
	
	/**
	 * @return ProcessoCriminalDTOParaProcessoCriminalMNIConverter
	 */
	public static ProcessoCriminalDTOParaProcessoCriminalMNIConverter instance(){
		return ComponentUtil.getComponent(ProcessoCriminalDTOParaProcessoCriminalMNIConverter.class);
	}
	
	@Override
	public Processo converter(ProcessoCriminalDTO dto) {
		Processo resultado = null;
		
		if (isNotNull(dto)) {
			resultado = new Processo();
			
			Endereco enderecoFatoCriminal = new Endereco();
			enderecoFatoCriminal.setCep(dto.getCep());
			enderecoFatoCriminal.setComplemento(dto.getComplemento());
			enderecoFatoCriminal.setBairro(dto.getNmBairro());
			enderecoFatoCriminal.setLogradouro(dto.getNmLogradouro());
			enderecoFatoCriminal.setNumero(dto.getNmNumero());
			enderecoFatoCriminal.setEstado(obterEstado(dto.getMunicipio()));
			enderecoFatoCriminal.setCidade(obterCidade(dto));
			
			FatoCriminal fatoCriminal = new FatoCriminal();
			fatoCriminal.setEndereco(enderecoFatoCriminal);
			fatoCriminal.setLatitude(dto.getDsLatitude());
			fatoCriminal.setLongitude(dto.getDsLongitude());
			fatoCriminal.setLocal(dto.getDsLocalFato());
			fatoCriminal.setData(ConversorUtil.converterParaDataHora(dto.getDtLocalFato()));
			
			resultado.setFatoCriminal(fatoCriminal);
			resultado.setNumero(dto.getNrProcesso());
			resultado.getProcedimentosOrigens().addAll(obterColecaoProcedimentoOrigem(dto));
		}
		return resultado;
	}
	
	/**
	 * Converte um ProcessoCriminalDTO e lista de InformacaoCriminalDTO para um Processo criminal.
	 * @param dto
	 * @param informacoesCriminais
	 * @return Processo criminal.
	 */
	public Processo converter(ProcessoCriminalDTO dto, List<InformacaoCriminalDTO> informacoesCriminais) {
		Processo resultado = null;
		
		if (isNotNull(dto)) {
			resultado = converter(dto);
			resultado.getPartes().addAll(obterColecaoPartes(informacoesCriminais));
		}
		return resultado;
	}

	/**
	 * @param municipio
	 * @return UF
	 */
	private String obterEstado(MunicipioDTO municipio) {
		return (municipio != null ? municipio.getUf() : null);
	}

	/**
	 * Retorna uma cole??o de objetos do tipo ProcedimentoOrigem.
	 * 
	 * @param dto ProcessoCriminalDTO
	 * @return Cole??o de ProcedimentoOrigem.
	 */
	private Collection<ProcedimentoOrigem> obterColecaoProcedimentoOrigem(ProcessoCriminalDTO dto) {
		Collection<ProcedimentoOrigem> resultado = new ArrayList<ProcedimentoOrigem>();
		
		List<ProcessoProcedimentoOrigemDTO> procedimentosOrigens = dto.getProcessoProcedimentoOrigemList();
		if (procedimentosOrigens == null) {
			return Collections.emptyList();
		}

		for (ProcessoProcedimentoOrigemDTO procedimentoOrigem : procedimentosOrigens) {
			ProcedimentoOrigem procedimentoOrigemDto = new ProcedimentoOrigem();
			procedimentoOrigemDto.setId(converterParaString(procedimentoOrigem.getId()));
			procedimentoOrigemDto.setAno(procedimentoOrigem.getAno());
			procedimentoOrigemDto.setDataInstauracao(ConversorUtil.converterParaDataHora(procedimentoOrigem.getDataInstauracao()));
			procedimentoOrigemDto.setDataLavratura(ConversorUtil.converterParaDataHora(procedimentoOrigem.getDataLavratura()));
			procedimentoOrigemDto.setProtocolo(procedimentoOrigem.getNrProtocoloPolicia());
			
			if (procedimentoOrigem.getOrgaoProcedimentoOriginario() != null) {
				OrgaoProcedimentoOrigem orgaoProcedimentoOrigem = new OrgaoProcedimentoOrigem();
				orgaoProcedimentoOrigem.setId(converterParaString(procedimentoOrigem.getOrgaoProcedimentoOriginario().getId()));
				orgaoProcedimentoOrigem.setNome(procedimentoOrigem.getOrgaoProcedimentoOriginario().getDsNomeOrgao());
				procedimentoOrigemDto.setOrgaoProcedimentoOrigem(orgaoProcedimentoOrigem);
			}

			procedimentoOrigemDto.setTipoOrigem(obterTipoOrigem(procedimentoOrigem));
			procedimentoOrigemDto.setTipo(obterTipo(procedimentoOrigem));
			procedimentoOrigemDto.setEstado(procedimentoOrigem.getUf());
			procedimentoOrigemDto.setNumero(procedimentoOrigem.getNumero());
			
			resultado.add(procedimentoOrigemDto);
		}
		
		return resultado;
	}

	/**
	 * @param procedimentoOrigem
	 * @return Tipo do procedimento de origem
	 */
	private String obterTipo(ProcessoProcedimentoOrigemDTO procedimentoOrigem) {
		return (procedimentoOrigem.getTipoProcedimentoOrigem() != null ? 
				converterParaString(procedimentoOrigem.getTipoProcedimentoOrigem().getId()) : null);
	}

	/**
	 * @param procedimentoOrigem
	 * @return Tipo de origem
	 */
	private String obterTipoOrigem(ProcessoProcedimentoOrigemDTO procedimentoOrigem) {
		return (procedimentoOrigem.getTipoOrigem() != null ? 
				converterParaString(procedimentoOrigem.getTipoOrigem().getId()) : null);
	}
	
	/**
	 * @param informacaoCriminal
	 * @return Cole??o de partes
	 */
	private Collection<Parte> obterColecaoPartes(List<InformacaoCriminalDTO> informacoesCriminais) {
		Collection<Parte> resultado = new ArrayList<Parte>();
		
		if (isNotVazio(informacoesCriminais)) {
			for (InformacaoCriminalDTO informacaoCriminal : informacoesCriminais) {
				ConteudoInformacaoCriminalBean conteudo = informacaoCriminal.getConteudo();
				ParteDTO parteDTO = informacaoCriminal.getParte();
				if(parteDTO == null || parteDTO.getIdPessoaLegacy() == null){
					throw new RuntimeException("Não foi possível encontrar a parte no pje-legacy");
				}
				Pessoa pessoa = ComponentUtil.getComponent(PessoaManager.class)
						.findById(informacaoCriminal.getParte().getIdPessoaLegacy().intValue());
				if (pessoa == null) {
					throw new RuntimeException("Não foi possível encontrar a parte no pje-legacy: " + parteDTO.getIdPessoaLegacy());
				}

				Parte parte = new Parte();
				parte.setNome(pessoa.getNome());
				parte.setNumeroDocumentoPrincipal(obterNumeroDocumentoPrincipal(pessoa));
				
				preencherCaracteristicasFisicas(pessoa, parte);
				preencherNomesAlternativos(pessoa, parte);
				preencherFiliacoes(pessoa, parte);
				
				parte.getDenuncias().addAll(obterColecaoDenuncias(conteudo.getIndiciamento()));
				parte.getDenuncias().addAll(obterColecaoDenuncias(conteudo.getOferecimentoDenuncia()));
				//parte.getDenuncias().addAll(obterColecaoDenuncias(conteudo.getAditamentoDenuncia()));
				parte.getEventosCriminais().addAll(obterColecaoEventosCriminais(conteudo));
				
				resultado.add(parte);
			}
		}
		
		return resultado;
	}

	private void preencherCaracteristicasFisicas(Pessoa filtroPessoa, Parte parte) {
		List<br.jus.pje.nucleo.entidades.CaracteristicaFisica> lstCaracteristicasFisicas = 
				caracteristicaFisicaManager.recuperaCaracteristicasFisicas(filtroPessoa);
		if (isNotVazio(lstCaracteristicasFisicas)) {
			for (br.jus.pje.nucleo.entidades.CaracteristicaFisica cf : lstCaracteristicasFisicas) {
				parte.getCaracteristicasPessoais().add(CaracteristicaFisica.valueOf(cf.getCaracteristicaFisica().name()));
			}
		}
	}

	private void preencherNomesAlternativos(Pessoa filtroPessoa, Parte parte) {
		List<PessoaNomeAlternativo> lstPessoaNomeAlternativo = pessoaNomeAlternativoManager.recuperaNomesAlternativosProprietarios(filtroPessoa);
		if (isNotVazio(lstPessoaNomeAlternativo)) {
			for (PessoaNomeAlternativo pessoaNomeAlternativo : lstPessoaNomeAlternativo) {
				if (TipoNomeAlternativoEnum.O.equals(pessoaNomeAlternativo.getTipoNomeAlternativo())) {
					parte.getOutrosNomes().add(pessoaNomeAlternativo.getPessoaNomeAlternativo());
				} else if (TipoNomeAlternativoEnum.A.equals(pessoaNomeAlternativo.getTipoNomeAlternativo())) {
					parte.getAlcunhas().add(pessoaNomeAlternativo.getPessoaNomeAlternativo());
				}
			}
		}
	}
	
	private void preencherFiliacoes(Pessoa filtroPessoa, Parte parte) {
		if(filtroPessoa instanceof PessoaFisica){
			return ;
		}
		List<PessoaFiliacao> lstPessoaFiliacao = pessoaFiliacaoManager.recuperaFiliacoes(filtroPessoa);
		if (isNotVazio(lstPessoaFiliacao)) {
			for (PessoaFiliacao pessoaFiliacao : lstPessoaFiliacao) {
				Filiacao filiacao = new Filiacao();
				if (TipoFiliacaoEnum.M.equals(pessoaFiliacao.getTipoFiliacao())) {
					filiacao.setNomeMae(pessoaFiliacao.getFiliacao());
				} else {
					filiacao.setNomePai(pessoaFiliacao.getFiliacao());
				}
				parte.getFiliacoes().add(filiacao);
			}
		}
	}

	
	/**
	 * @param filtroPessoa
	 * @return Documento principal.
	 */
	private CadastroIdentificador obterNumeroDocumentoPrincipal(Pessoa filtroPessoa) {
		CadastroIdentificador resultado = null;
		
		if (isNotNull(filtroPessoa) && isNotVazio(filtroPessoa.getDocumentoCpfCnpj())) {
			resultado = new CadastroIdentificador();
			resultado.setValue(filtroPessoa.getDocumentoCpfCnpj());
		}
		return resultado;
	}
	
	/**
	 * @param conteudo
	 * @return Cole??o de eventos criminais.
	 */
	private Collection<EventoCriminal> obterColecaoEventosCriminais(ConteudoInformacaoCriminalBean conteudo) {
		Collection<EventoCriminal> resultado = new ArrayList<EventoCriminal>();
		
		if (isNotNull(conteudo)) {
			
			List<PrisaoBean> prisoes = conteudo.getPrisoes();
			if (isNotNull(prisoes)) {
				for (PrisaoBean prisao : prisoes) {
					MotivoPrisaoDTO motivoPrisaoDTO = prisao.getMotivoPrisao();
					MotivoPrisao motivoTemp = new MotivoPrisao();
					motivoTemp.setId(converterParaString(motivoPrisaoDTO.getId()));
					motivoTemp.setDescricao(motivoPrisaoDTO.getMotivoPrisao());
					
					Prisao prisaoTemp = new Prisao();
					prisaoTemp.setMotivo(motivoTemp);
					//TODO: Verificar, pois estamos fazendo o de-para de texto puro para Enum.
					prisaoTemp.setTipo(new TipoPrisaoDTOParaTipoPrisaoMNIConverter().converter(prisao.getTipoPrisao()));
					prisaoTemp.setPrazoDias(prisao.getPrazoDias());
					prisaoTemp.setPrazoMeses(prisao.getPrazoMeses());
					prisaoTemp.setPrazoAnos(prisao.getPrazoAnos());
					
					EventoCriminal evento = new EventoCriminal();
					evento.setData(ConversorUtil.converterParaDataHora(prisao.getDtPrisao()));
					evento.setUnidadePrisional(obterUnidadePrisional(prisao.getUnidadePrisional()));
					evento.setPrisao(prisaoTemp);
					
					resultado.add(evento);
				}
			}
			List<SolturaBean> solturas = conteudo.getSolturas();
			if (isNotNull(solturas)) {
				for (SolturaBean soltura : solturas) {
					MotivoSolturaDTO motivoSolturaDTO = soltura.getMotivo();
					motivoSolturaDTO.getMotivoSoltura();
					
					MotivoSoltura motivoSolturaTemp = new MotivoSoltura();
					motivoSolturaTemp.setId(converterParaString(motivoSolturaDTO.getId()));
					motivoSolturaTemp.setDescricao(motivoSolturaDTO.getMotivoSoltura());
					
					Soltura solturaTemp = new Soltura();
					solturaTemp.setMotivoSoltura(motivoSolturaTemp);
					
					EventoCriminal evento = new EventoCriminal();
					evento.setData(ConversorUtil.converterParaDataHora(soltura.getDtSoltura()));
					evento.setUnidadePrisional(obterUnidadePrisional(soltura.getUnidadePrisional()));
					evento.setSoltura(solturaTemp);
					
					resultado.add(evento);
				}
			}
			List<FugaBean> fugas = conteudo.getFugas();
			if (isNotNull(fugas)) {
				for (FugaBean fuga : fugas) {
					
					Fuga fugaTemp = new Fuga();
					fugaTemp.setDescricao(fuga.getDsFuga());
					
					EventoCriminal evento = new EventoCriminal();
					evento.setData(ConversorUtil.converterParaDataHora(fuga.getDtFuga()));
					evento.setUnidadePrisional(obterUnidadePrisional(fuga.getUnidadePrisional()));
					evento.setFuga(fugaTemp);
					
					resultado.add(evento);
				}
			}
		}
		
		return resultado;
	}

	/**
	 * @param unidadePrisional
	 * @return UnidadePrisional.
	 */
	private UnidadePrisional obterUnidadePrisional(UnidadePrisionalBean unidadePrisional) {
		UnidadePrisional resultado = null;
		
		if (isNotNull(unidadePrisional)) {
			resultado = new UnidadePrisional();
			resultado.setId(unidadePrisional.getId());
			resultado.setNome(unidadePrisional.getDsUnidadePrisional());
		}
		return resultado;
	}

	/**
	 * @param eventoCriminalBeanList
	 * @return Cole??o de den?ncias.
	 */
	private Collection<Denuncia> obterColecaoDenuncias(List<EventoCriminalBean> eventoCriminalBeanList) {
		Collection<Denuncia> resultado = new ArrayList<Denuncia>();
		

		if (isNotVazio(eventoCriminalBeanList)) {
			for (EventoCriminalBean eventoCriminal : eventoCriminalBeanList) {
				Denuncia temp = new Denuncia();
				temp.setData(ConversorUtil.converterParaDataHora(eventoCriminal.getData()));
				temp.setTipo(new TipoInformacaoCriminalParaTipoDenunciaConverter().converter(eventoCriminal.getTipoInformacaoCriminal()));
				temp.setObservacao(eventoCriminal.getObservacao());
				
				for (IncidenciaPenalBean incidenciaPenalBean : eventoCriminal.getTipificacoes()) {
					Dispositivo dispositivo = obterDispositivo(incidenciaPenalBean.getDispositivo());
					temp.getDispositivos().add(dispositivo);
				}

				resultado.add(temp);
			}
		}
		
		return resultado;
	}

	/**
	 * @param dispositivo
	 * @return Dispositivo.
	 */
	private Dispositivo obterDispositivo(DispositivoDTO dispositivo) {
		Dispositivo resultado = null;
		
		if (isNotNull(dispositivo)) {
			NormaDTO normaDTO = dispositivo.getNorma();
			
			resultado = new Dispositivo();
			resultado.setId(converterParaString(dispositivo.getId()));
			resultado.setIdentificador(dispositivo.getIdentificador());
			resultado.setNorma((normaDTO != null ? normaDTO.getNorma() : null));
			resultado.setTexto(dispositivo.getTextoFinal() != null ? dispositivo.getTextoFinal() : dispositivo.getTexto());
			
		}
		return resultado;
	}
	
	private String obterCidade(ProcessoCriminalDTO dto) {
		if(dto.getMunicipio() != null && dto.getMunicipio().getMunicipio() != null){
			return dto.getMunicipio().getMunicipio().toUpperCase();
		}
		return null;
	}
}