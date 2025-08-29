package br.com.infox.pje.service;

import java.io.Serializable;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;

import org.jboss.seam.annotations.In;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.mandadoprisao.webservices.MPWSAspectoFisicoPessoa;
import br.jus.cnj.mandadoprisao.webservices.MPWSComprovante;
import br.jus.cnj.mandadoprisao.webservices.MPWSDocumentoPessoa;
import br.jus.cnj.mandadoprisao.webservices.MPWSMandadoPrisao;
import br.jus.cnj.mandadoprisao.webservices.MPWSOrgaoJulgadorMandado;
import br.jus.cnj.mandadoprisao.webservices.MPWSPendencia;
import br.jus.cnj.mandadoprisao.webservices.MPWSPessoa;
import br.jus.cnj.mandadoprisao.webservices.MPWSProcedimentoOrigem;
import br.jus.cnj.mandadoprisao.webservices.MPWSProcessoMandadoPrisao;
import br.jus.cnj.mandadoprisao.webservices.MPWSSituacaoMandado;
import br.jus.cnj.mandadoprisao.webservices.MPWSTipoAspectoFisicoPessoa;
import br.jus.cnj.mandadoprisao.webservices.MPWSTipoDocumentoPessoa;
import br.jus.cnj.mandadoprisao.webservices.MPWSTipoMagistradoMandado;
import br.jus.cnj.mandadoprisao.webservices.MPWSTipoPrisaoMandado;
import br.jus.cnj.mandadoprisao.webservices.MPWSTipoProcedimentoOrigem;
import br.jus.cnj.mandadoprisao.webservices.MPWSTipoSituacaoMandado;
import br.jus.cnj.mandadoprisao.webservices.MandadoPrisaoService;
import br.jus.cnj.mandadoprisao.webservices.MandadoPrisaoServiceWS;
import br.jus.cnj.pje.nucleo.BNMPException;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.CaracteristicaFisica;
import br.jus.pje.nucleo.entidades.MandadoPrisao;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoProcedimentoOrigem;
import br.jus.pje.nucleo.enums.CaracteristicaFisicaEnum;

public class BNMPService implements Serializable{

	private static final long serialVersionUID = 1509411346446524882L;

	private static final String chave = "412345ddc56fa1f1511f7ade5dd35767b47766a5718dc9d73757b09ef9951190";

	@In
	private ProcessoDocumentoBinManager processoDocumentoBinManager;

	public MPWSComprovante atualizarSituacaoBNMP(MPWSSituacaoMandado situacao) throws BNMPException{
		try{
			/* INICIO comentar em produção */
			System.setProperty("javax.net.debug", "ssl");

			System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
			System.setProperty("javax.net.ssl.keyStore", MandadoPrisaoServiceWS.class.getResource("/META-INF/wsmandadoprisao/certKeysN1.pfx")
					.getFile());
			
			System.setProperty("javax.net.ssl.keyStorePassword", "123456");

			System.setProperty("javax.net.ssl.trustStoreType", "JKS");
			System.setProperty("javax.net.ssl.trustStore",
					MandadoPrisaoServiceWS.class.getResource("/META-INF/wsmandadoprisao/clientTrustStore.jks").getFile());
			System.setProperty("javax.net.ssl.trustStorePassword", "123456");
			/* FIM comentar em produção */

			String endPoint = ParametroUtil.getFromContext("urlWsdlMandadoPrisao", false);// "https://wwwh.cnj.jus.br/MPWS/mandadoPrisaoService?wsdl";
			// // HOLOGAÇÃO
			MandadoPrisaoServiceWS client = new MandadoPrisaoServiceWS(new URL(endPoint), new QName(
					"http://www.cnj.jus.br/mpws", "MandadoPrisaoServiceWS"));
			MandadoPrisaoService service = client.getMandadoPrisaoPortWS();

			List<MPWSSituacaoMandado> situacoes = new ArrayList<MPWSSituacaoMandado>();
			situacoes.add(situacao);

			MPWSComprovante returnValue = service
					.enviarSituacaoMandado(situacoes, chave);

			for (MPWSPendencia p : returnValue.getPendencias()){
				throw new BNMPException("pje.bnmp.error.pendencias", null, p.getPendencias());
			}
			return returnValue;
		} catch (Exception e){
			throw new BNMPException(e);
		}
	}

	public List<MPWSMandadoPrisao> converter(List<MandadoPrisao> mandados, MPWSTipoSituacaoMandado tipoSituacaoMandado) throws PJeBusinessException{
		List<MPWSMandadoPrisao> returnValue = new ArrayList<MPWSMandadoPrisao>(0);
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		for (MandadoPrisao mandado : mandados){
			MPWSMandadoPrisao mandadoWS = new MPWSMandadoPrisao();
			
			//recolocando no contexto do hibernate
			//mandado = entityManager.find(MandadoPrisao.class, mandado.getId());
			
			mandadoWS.setAssuntoDelitoAlvara(mandado.getAssuntoPrincipal().getAssuntoTrfCompleto());
			if (mandado.getDataDelito() != null){
				mandadoWS.setDataDelito(sdf.format(mandado.getDataDelito()));
			}

			if (mandado.getDtCriacao() != null){
				mandadoWS.setDataMandado(sdf.format(mandado.getDtCriacao()));
			}

			if (mandado.getDataValidade() != null){
				mandadoWS.setDataValidade(sdf.format(mandado.getDataValidade()));
			}

			mandadoWS.setNomeMagistrado(mandado.getPessoaMagistrado().getNome());
			mandadoWS.setNumeroMandado(mandado.getNumeroExpediente());
			MPWSOrgaoJulgadorMandado orgaoJulgadorMandado = new MPWSOrgaoJulgadorMandado();

			if (mandado.getProcessoTrf().getOrgaoJulgador().getJurisdicao().getMunicipioSede() == null){
				throw new PJeBusinessException("pje.bnmp.error.sedeInexistente");
			}

			orgaoJulgadorMandado.setMunicipio(mandado.getProcessoTrf().getOrgaoJulgador().getJurisdicao()
					.getMunicipioSede().getMunicipio());

			orgaoJulgadorMandado.setNome(mandado.getProcessoTrf().getOrgaoJulgador().getOrgaoJulgador());
			orgaoJulgadorMandado.setNumeroGrauJurisdicao(Integer.parseInt(mandado.getProcessoTrf().getOrgaoJulgador().getInstancia()));
			orgaoJulgadorMandado.setUF(mandado.getProcessoTrf().getOrgaoJulgador().getJurisdicao().getMunicipioSede()
					.getEstado().getCodEstado());
			mandadoWS.setOrgaoJulgador(orgaoJulgadorMandado);
			mandadoWS.setPenaImposta(mandado.getDescricaoPena());

			mandadoWS.setPenaImposta(mandado.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento());

			MPWSPessoa pessoa = new MPWSPessoa();
			pessoa.getNomes().add(mandado.getPessoa().getNome());
			for (PessoaDocumentoIdentificacao item : mandado.getPessoa().getPessoaDocumentoIdentificacaoList()){
				if (item.getTipoDocumento().getCodTipo().equals("CPF") && item.getDocumentoPrincipal()){
					MPWSDocumentoPessoa documento = new MPWSDocumentoPessoa();
					documento.setTipoDocumento(MPWSTipoDocumentoPessoa.CPF);
					documento.setIdentificacao(item.getNumeroDocumento());
					documento.setOrgaoExpedidor(item.getOrgaoExpedidor());
					if (item.getEstado() != null && item.getEstado().getCodEstado() != null){
						documento.setUf(item.getEstado().getCodEstado());
					}
					else{
						documento.setUf(null);
					}
					pessoa.getDocumentos().add(documento);
					break;
				}
			}
			
			//setando características físicas
			pessoa.setObservacao(((PessoaFisica)mandado.getPessoa()).getOutrasCaracteristicasPessoais());
			if(mandado.getPessoa() instanceof PessoaFisica){
				for(CaracteristicaFisica aux : ((PessoaFisica)mandado.getPessoa()).getCaracteristicasFisicas()){
					MPWSAspectoFisicoPessoa aspectoFisicoPessoa = new MPWSAspectoFisicoPessoa();
					aspectoFisicoPessoa.setTipoAspectoFisico(getMPWSTipoAspectoFisicoPessoa(aux.getCaracteristicaFisica()));					
					pessoa.getAspectosFisicos().add(aspectoFisicoPessoa);
				}
			}

			mandadoWS.setPessoa(pessoa);
			mandadoWS.setPrazo(mandado.getDiasPrisaoTemporaria());

			MPWSProcessoMandadoPrisao processo = new MPWSProcessoMandadoPrisao();
			processo.setCodigoClasseProcesual(Long.parseLong(mandado.getProcessoTrf().getClasseJudicial()
					.getCodClasseJudicial()));
			processo.setNumeroProcessoR65(mandado.getProcessoTrf().getNumeroProcesso());
			mandadoWS.setProcesso(processo);
			mandadoWS.getProcesso().setCodigoClasseProcesual(
					Long.parseLong(mandado.getProcessoTrf().getClasseJudicial().getCodClasseJudicial()));
			for (AssuntoTrf assunto : mandado.getProcessoTrf().getAssuntoTrfList()){
				mandadoWS.getProcesso().getCodigoAssuntos().add(Long.parseLong(assunto.getCodAssuntoTrf()));
			}
			mandadoWS.setRecaptura(mandado.getRecaptura());
			if (mandado.getRegimePena() != null){
				mandadoWS.setRegimeCumprimentoPena(mandado.getRegimePena().getLabel());
			}
			mandadoWS.setSinteseDecisao(mandado.getSintese());

			// mandadoWS.setTipoMagistrado(MPWSTipoMagistradoMandado.JUIZ_DIREITO);

			// TODO criar constantes com essas profissoes para usar switch case
			String tipoJustica = ParametroUtil.getFromContext("tipoJustica", false);
			if (tipoJustica.toUpperCase().equals("JC")){
				if (mandado.getProcessoTrf().getOrgaoJulgador().getAplicacaoClasse().getAplicacaoClasse().trim().toLowerCase().contains("1º")){
					mandadoWS.setTipoMagistrado(MPWSTipoMagistradoMandado.JUIZ_DIREITO);
				}
				else if (mandado.getProcessoTrf().getOrgaoJulgador().getAplicacaoClasse().getAplicacaoClasse().trim().toLowerCase().contains("2º")){
					mandadoWS.setTipoMagistrado(MPWSTipoMagistradoMandado.DESEMBARGADOR);
				}
			}
			else if (tipoJustica.toUpperCase().equals("JF")){
				if (mandado.getProcessoTrf().getOrgaoJulgador().getAplicacaoClasse().getAplicacaoClasse().trim().toLowerCase().contains("1º")){
					mandadoWS.setTipoMagistrado(MPWSTipoMagistradoMandado.JUIZ_FEDERAL);
				}
				else if (mandado.getProcessoTrf().getOrgaoJulgador().getAplicacaoClasse().getAplicacaoClasse().trim().toLowerCase().contains("2º")){
					mandadoWS.setTipoMagistrado(MPWSTipoMagistradoMandado.DESEMBARGADOR);
				}
			}else{
				throw new PJeBusinessException("pje.bnmp.error.tipoJusticaNaoPrevista");
			}

			switch (mandado.getTipoPrisao()){
			case EXP:
				mandadoWS.setTipoPrisao(MPWSTipoPrisaoMandado.EXPULSAO);
				break;
			case EXT:
				mandadoWS.setTipoPrisao(MPWSTipoPrisaoMandado.EXTRADICAO);
				break;
			case DEF:
				mandadoWS.setTipoPrisao(MPWSTipoPrisaoMandado.DEFINITIVA);
				break;
			case DEP:
				mandadoWS.setTipoPrisao(MPWSTipoPrisaoMandado.DEPORTACAO);
				break;
			case PRV:
				mandadoWS.setTipoPrisao(MPWSTipoPrisaoMandado.PREVENTIVA);
				break;
			case PRVDM:
				mandadoWS.setTipoPrisao(MPWSTipoPrisaoMandado.PREVENTIVA_DECISAO_CONDENATORIA);
				break;
			case TMP:
				mandadoWS.setTipoPrisao(MPWSTipoPrisaoMandado.TEMPORARIA);
				break;
			}

			// TODO criar constantes com esses tipos de procedimento de origem
			// para usar switch case
			for (ProcessoProcedimentoOrigem item : mandado.getProcessoProcedimentoOrigemList()){
				MPWSProcedimentoOrigem procedimentoOrigem = new MPWSProcedimentoOrigem();
				if (item.getTipoProcedimentoOrigem() != null){
					procedimentoOrigem.setNumeroProcedimento(Long.parseLong(item.getNumero()));
					procedimentoOrigem.setNomeTipoProcedimento(item.getTipoProcedimentoOrigem().toString());
					if (item.getTipoProcedimentoOrigem().getDsTipoProcedimento().contains("Boletim de Ocorrência")){
						procedimentoOrigem.setTipoProcedimentoOrigem(MPWSTipoProcedimentoOrigem.BOLETIM_OCORRENCIA);
					}
					else if (item.getTipoProcedimentoOrigem().getDsTipoProcedimento().contains("Inquérito")){
						procedimentoOrigem.setTipoProcedimentoOrigem(MPWSTipoProcedimentoOrigem.INQUERITO_POLICIAL);
					}
					else if (item.getTipoProcedimentoOrigem().getDsTipoProcedimento().contains("Prisão em Flagrante")){
						procedimentoOrigem.setTipoProcedimentoOrigem(MPWSTipoProcedimentoOrigem.AUTO_PRISAO_FLAGRANTE);
					}
					else if (item.getTipoProcedimentoOrigem().getDsTipoProcedimento().contains("Termo Circunstânciado")){
						procedimentoOrigem.setTipoProcedimentoOrigem(MPWSTipoProcedimentoOrigem.TERMO_CIRCUNSTANCIADO_OCORRENCIA);
					}
					else{
						procedimentoOrigem.setTipoProcedimentoOrigem(MPWSTipoProcedimentoOrigem.OUTRO);
					}
					mandadoWS.getProcedimentosOrigem().add(procedimentoOrigem);
				}
			}

			try{
				List<ProcessoDocumentoBinPessoaAssinatura> assinaturas = processoDocumentoBinManager.obtemAssinaturas(mandado.getProcessoDocumento()
						.getProcessoDocumentoBin());

				if (assinaturas == null || assinaturas.isEmpty()){
					throw new PJeBusinessException("pje.bnmp.error.assinaturaInexistente");
				}

				Date dataAssinatura = sdf.parse(sdf.format(assinaturas.get(0).getDataAssinatura()));
				Date dataValidade = sdf.parse(sdf.format(mandado.getDataValidade()));
				Calendar c1 = Calendar.getInstance();
				Calendar c2 = Calendar.getInstance();
				c1.setTime(dataAssinatura);
				c2.setTime(dataValidade);
				int prazo = c2.get(Calendar.DAY_OF_YEAR) - c1.get(Calendar.DAY_OF_YEAR);
				mandadoWS.setPrazo(prazo);
			} catch (ParseException e){
				throw new BNMPException(e);
			} catch (PJeDAOException e){
				throw new PJeDAOException(e);
			}

			mandadoWS.setTipoSituacao(tipoSituacaoMandado);
			mandadoWS.setValorFianca(mandado.getValorFianca());
			returnValue.add(mandadoWS);
		}
		return returnValue;
	}
	
	private MPWSTipoAspectoFisicoPessoa getMPWSTipoAspectoFisicoPessoa(CaracteristicaFisicaEnum cf){
		return MPWSTipoAspectoFisicoPessoa.valueOf(cf.toString());
	}


	protected MPWSComprovante enviarParaBNMP(List<MandadoPrisao> mandados, MPWSTipoSituacaoMandado tipoSituacaoMandado)
			throws BNMPException{
		try{
			/* INICIO comentar em produção */
			System.setProperty("javax.net.debug", "ssl");

			System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
			System.setProperty("javax.net.ssl.keyStore", MandadoPrisaoServiceWS.class.getResource("/META-INF/wsmandadoprisao/certKeysN1.pfx")
					.getFile());
			
			System.setProperty("javax.net.ssl.keyStorePassword", "123456");

			System.setProperty("javax.net.ssl.trustStoreType", "JKS");
			System.setProperty("javax.net.ssl.trustStore",
					MandadoPrisaoServiceWS.class.getResource("/META-INF/wsmandadoprisao/clientTrustStore.jks").getFile());
			System.setProperty("javax.net.ssl.trustStorePassword", "123456");
			/* FIM comentar em produção */

			String endPoint = ParametroUtil.getFromContext("urlWsdlMandadoPrisao", false);// "https://wwwh.cnj.jus.br/MPWS/mandadoPrisaoService?wsdl";
			// HOMOLOGAÇÃO
			MandadoPrisaoServiceWS client = new MandadoPrisaoServiceWS(new URL(endPoint), new QName(
					"http://www.cnj.jus.br/mpws", "MandadoPrisaoServiceWS"));
			MandadoPrisaoService service = client.getMandadoPrisaoPortWS();

			MPWSComprovante returnValue = service.enviarMandado(converter(mandados, tipoSituacaoMandado), chave);
			String erros = null;
			for (MPWSPendencia p : returnValue.getPendencias()){
				//throw new BNMPException("pje.bnmp.error.pendencias", null, p.getPendencias());
				erros = p.getPendencias()+".";
			}
			
			if(erros != null){
				throw new BNMPException("pje.bnmp.error.pendencias", null, erros);
			}
			
			return returnValue;
		} catch (Exception e){
			e.printStackTrace();
			throw new BNMPException(e);
		}
	}
}
