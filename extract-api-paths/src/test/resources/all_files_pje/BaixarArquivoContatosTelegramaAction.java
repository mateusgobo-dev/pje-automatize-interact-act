package br.jus.cnj.pje.view.fluxo;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.LazyInitializationException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.TransactionPropagationType;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.home.ProcessoTrfLogDistribuicaoHome;
import br.com.infox.cliente.home.ProcessoTrfRedistribuicaoHome;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.extensao.PontoExtensaoException;
import br.jus.cnj.pje.extensao.auxiliar.DestinatarioECT;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.CepManager;
import br.jus.cnj.pje.nucleo.manager.EstadoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoExpedienteManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.view.ConsultaExpedienteAction;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpedienteEndereco;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfLogDistribuicao;
import br.jus.pje.nucleo.entidades.ProcessoTrfRedistribuicao;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name("baixarArquivoContatosTelegramaAction")
@Scope(ScopeType.CONVERSATION)
public class BaixarArquivoContatosTelegramaAction implements Serializable {

	private static final long serialVersionUID = 492843500454144492L;

	public static final String PARAMETRO_INSTANCIA_JUSTICA = "aplicacaoSistema";
    
	@Logger
    private Log logger;
    
    @In
    private ProcessoExpedienteManager processoExpedienteManager;
    
    @In(create = true)
    private ParametroService parametroService;
    
    @In(create = true)
    private ProcessoTrfLogDistribuicaoHome processoTrfLogDistribuicaoHome;
    
    @In(create = true)
    private ProcessoTrfRedistribuicaoHome processoTrfRedistribuicaoHome;
    
    @In
    private EstadoManager estadoManager;
    
    @In
    private CepManager cepManager;
    
    private int instanciaJustica = -1;
    
    private String dataString;
	public String getDataString() { return dataString; }
	public void setDataString(String dataString) { this.dataString = dataString; }
	
	private Date data;
    public Date getData() { return data; }
	public void setData(Date data) { this.data = data; }
	

	@SuppressWarnings("unused")
	public void testeRecuperarExpedientesTelegrama(String dataString, String formato) {
		logger.debug("BaixarArquivoExpedientesTelegrama.testeRecuperarExpedientesTelegrama() BEGIN");
		List<ProcessoExpediente> listaExpedientes = recuperarExpedientesTelegrama(dataString, formato, null);
		logger.debug("BaixarArquivoExpedientesTelegrama.testeRecuperarExpedientesTelegrama() END");
	}
	
	public List<ProcessoExpediente> recuperarExpedientesTelegrama(String dataString, String formato, List<ProcessoExpediente> expedientesComErro) {
		logger.debug("BaixarArquivoExpedientesTelegrama.recuperarExpedientesTelegrama(String, String) BEGIN");
		SimpleDateFormat format = new SimpleDateFormat(formato);
		Date data = new Date();
		try {
			data = format.parse(dataString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		List<ProcessoExpediente> listaProcessoExpediente = recuperarExpedientesTelegrama(data, expedientesComErro);
		logger.debug("BaixarArquivoExpedientesTelegrama.recuperarExpedientesTelegrama(String, String) END");
		return listaProcessoExpediente;
	}
	
	@Transactional(TransactionPropagationType.REQUIRED)
    public List<ProcessoExpediente> recuperarExpedientesTelegrama(Date data, List<ProcessoExpediente> expedientesComErro) {
		logger.debug("BaixarArquivoExpedientesTelegrama.recuperarExpedientesTelegrama(Date) BEGIN");
    	List<ProcessoExpediente> listaProcessoExpediente = processoExpedienteManager.getAtosComunicacaoTelegrama(data);
		logger.debug("\tgetAtosComunicacaoTelegrama() - listaProcessoExpediente.size() = " + listaProcessoExpediente.size());
    	List<ProcessoExpediente> listaProcessoExpedienteFiltrada = new ArrayList<ProcessoExpediente>();
		Integer idOrgaoJulgadorUsuarioAtual = getIdOrgaoUsuarioAtual();
		logger.debug("\tVerificando expedientes:");
    	for (ProcessoExpediente processoExpediente : listaProcessoExpediente) {
		try {
			logger.debug("\t\tprocessando idProcessoExpediente = " + processoExpediente.getIdProcessoExpediente());
	    		Integer idOrgaoCriacaoExpediente = determinaIdOrgaoJulgadorDaCriacaoExpediente(processoExpediente);
	    		logger.debug("\t\tdeterminaIdOrgaoJulgadorDaCriacaoExpediente() = " + idOrgaoCriacaoExpediente);
	    		if (idOrgaoJulgadorUsuarioAtual.equals(idOrgaoCriacaoExpediente)) {
	    			listaProcessoExpedienteFiltrada.add(processoExpediente);
	    			logger.debug("\t\tAdicionado expediente " + processoExpediente.getIdProcessoExpediente());
	    		}
		} catch (Throwable e) {
			logger.error("\t\tErro ao processar o expediente " + processoExpediente.getIdProcessoExpediente());
			logger.info("\t\tO Expediente " + processoExpediente.getIdProcessoExpediente() + " não foi exportado.");
			if (expedientesComErro != null) {
				expedientesComErro.add(processoExpediente);
			}
		}
    	}
		logger.debug("\tlistaProcessoExpedienteFiltrada.size() = " + listaProcessoExpedienteFiltrada.size());
		logger.debug("BaixarArquivoExpedientesTelegrama.recuperarExpedientesTelegrama(Date) END");
    	return listaProcessoExpedienteFiltrada;
    }

	private Integer getIdOrgaoUsuarioAtual() {
		logger.debug("BaixarArquivoExpedientesTelegrama.getIdOrgaoUsuarioAtual() BEGIN");
		Integer idOrgao;
		if (getInstanciaJustica() == 1) {
			idOrgao = Authenticator.getIdOrgaoJulgadorAtual();
		} else {
			idOrgao = Authenticator.getIdOrgaoJulgadorColegiadoAtual();
		}
		logger.debug("BaixarArquivoExpedientesTelegrama.getIdOrgaoUsuarioAtual() END");
		return idOrgao;
	}

	@Transactional
	private Integer determinaIdOrgaoJulgadorDaCriacaoExpediente(ProcessoExpediente processoExpediente) {
		logger.debug("BaixarArquivoExpedientesTelegrama.determinaIdOrgaoJulgadorDaCriacaoExpediente() BEGIN");
		if (processoExpediente == null) {
			logger.warn("processoExpediente == null, retornando null");
			return null;
		}
		if (processoExpediente.getOrgaoJulgador() != null) {
			int idOrgaoJulgador = processoExpediente.getOrgaoJulgador().getIdOrgaoJulgador(); 
			logger.debug("Expediente possui Órgão Julgador associado, retornando " + idOrgaoJulgador);
			return idOrgaoJulgador;
		}
		logger.warn("Expediente não possui Órgao Julgador associado, tentando buscar pelo histórico de distribuições...");
		Date dataExpediente = processoExpediente.getDtCriacao();
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		logger.debug("dataExpediente = " + format.format(dataExpediente));
		ProcessoTrf processoTrf = processoExpediente.getProcessoTrf();
		List<ProcessoTrfRedistribuicao> listaRedistribuicao = processoTrfRedistribuicaoHome.recuperarPorProcesso(processoTrf);
		ProcessoTrfLogDistribuicao processoTrfLogDistribuicao = processoTrfLogDistribuicaoHome.recuperarPorProcesso(processoTrf);
		Date dataDistribuicao = processoTrfLogDistribuicao.getDataLog();
		Date dataConsiderada = dataDistribuicao;
		logger.debug("\tPercorre listaRedistribuicao:");
		Integer idOrgaoConsiderado = getIdOrgao(processoTrfLogDistribuicao);
		for (ProcessoTrfRedistribuicao processoTrfRedistribuicao : listaRedistribuicao) {
			Date dataRedistribuicao = processoTrfRedistribuicao.getDataRedistribuicao();
			logger.debug("\t\tdataExpediente = " + format.format(dataExpediente) + 
					", dataRedistribuicao = " + format.format(dataRedistribuicao) + 
					", dataConsiderada = " + format.format(dataConsiderada));
			if (dataExpediente.after(dataRedistribuicao) 
				&& (DateUtil.diferencaDias(dataRedistribuicao, dataConsiderada) >= 0)
			) {
				dataConsiderada = dataRedistribuicao;
				Integer idOrgao = getIdOrgao(processoTrfRedistribuicao);
				if (idOrgao != null) {
					idOrgaoConsiderado = idOrgao;
				}
				logger.debug("\t\t----> idOrgaoConsiderado = " + idOrgaoConsiderado);
			}
		}
		logger.debug("BaixarArquivoExpedientesTelegrama.determinaIdOrgaoJulgadorDaCriacaoExpediente() END");
		return idOrgaoConsiderado;
	}

	private Integer getIdOrgao(ProcessoTrfLogDistribuicao processoTrfLogDistribuicao) {
		logger.debug("BaixarArquivoExpedientesTelegrama.getIdOrgao() BEGIN");
		Integer idOrgaoConsiderado;
		if (getInstanciaJustica() == 1) {
			if (processoTrfLogDistribuicao == null || processoTrfLogDistribuicao.getOrgaoJulgador() == null) {
				return null;
			}
			idOrgaoConsiderado = processoTrfLogDistribuicao.getOrgaoJulgador().getIdOrgaoJulgador();
		} else {
			if (processoTrfLogDistribuicao == null || processoTrfLogDistribuicao.getOrgaoJulgadorColegiado() == null) {
				return null;
			}
			idOrgaoConsiderado = processoTrfLogDistribuicao.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado();
		}
		logger.debug("BaixarArquivoExpedientesTelegrama.getIdOrgao() END");
		return idOrgaoConsiderado;
	}
    
	private Integer getIdOrgao(ProcessoTrfRedistribuicao processoTrfRedistribuicao) {
		logger.debug("BaixarArquivoExpedientesTelegrama.getIdOrgao() BEGIN");
		Integer idOrgaoConsiderado;
		if (getInstanciaJustica() == 1) {
			if (processoTrfRedistribuicao == null || processoTrfRedistribuicao.getOrgaoJulgador() == null) {
				return null;
			}
			idOrgaoConsiderado = processoTrfRedistribuicao.getOrgaoJulgador().getIdOrgaoJulgador();
		} else {
			if (processoTrfRedistribuicao == null || processoTrfRedistribuicao.getOrgaoJulgadorColegiado() == null) {
				return null;
			}
			idOrgaoConsiderado = processoTrfRedistribuicao.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado();
		}
		logger.debug("BaixarArquivoExpedientesTelegrama.getIdOrgao() END");
		return idOrgaoConsiderado;
	}
    
	private int getInstanciaJustica() {
		if (instanciaJustica < 0) {
			logger.debug("BaixarArquivoExpedientesTelegrama.getInstanciaJustica() BEGIN");
	        String parametroInstanciaJusticaString = parametroService.valueOf(PARAMETRO_INSTANCIA_JUSTICA);
	        instanciaJustica = Integer.parseInt(parametroInstanciaJusticaString);
	        logger.debug("\tparametroInstanciaJustica = " + instanciaJustica);
			logger.debug("BaixarArquivoExpedientesTelegrama.getInstanciaJustica() END");
		}
        return instanciaJustica;
	}

	private class InformacaoContato {
	    protected String apelido;
	    protected String auxiliar1;
	    protected String auxiliar2;
	    protected String auxiliar3;
	    protected String auxiliar4;
	    protected String auxiliar5;
	    protected String auxiliar6;
	    protected String auxiliar7;
	    protected String auxiliar8;
	    protected String auxiliar9;
	    protected String bairro;
	    protected String caixaPostal;
	    protected String cep;
	    protected String cepCaixaPostal;
	    protected String cidade;
	    protected String complemento;
	    protected String ddd;
	    protected String email;
	    protected String endereco;
	    protected String nome;
	    protected String numero;
	    protected String numeroFax;
	    protected String dddFax;
	    protected String pais;
	    protected String provincia;
	    protected String siglaUF;
	    protected String telefone;
	    protected String titulo;

	    public String getApelido() { return apelido; }
	    public void setApelido(String value) { this.apelido = value; }

	    public String getAuxiliar1() { return auxiliar1; }
	    public void setAuxiliar1(String value) { this.auxiliar1 = value; }

	    public String getAuxiliar2() { return auxiliar2; }
	    public void setAuxiliar2(String value) { this.auxiliar2 = value; }

	    public String getAuxiliar3() { return auxiliar3; }
	    public void setAuxiliar3(String value) { this.auxiliar3 = value; }

	    public String getAuxiliar4() { return auxiliar4; }
	    public void setAuxiliar4(String value) { this.auxiliar4 = value; }

	    public String getAuxiliar5() { return auxiliar5; }
	    public void setAuxiliar5(String value) { this.auxiliar5 = value; }

	    public String getAuxiliar6() { return auxiliar6; }
	    public void setAuxiliar6(String value) { this.auxiliar6 = value; }

	    public String getAuxiliar7() { return auxiliar7; }
	    public void setAuxiliar7(String value) { this.auxiliar7 = value; }

	    public String getAuxiliar8() { return auxiliar8; }
	    public void setAuxiliar8(String value) { this.auxiliar8 = value; }

	    public String getAuxiliar9() { return auxiliar9; }
	    public void setAuxiliar9(String value) { this.auxiliar9 = value; }

	    public String getBairro() { return bairro; }
	    public void setBairro(String value) { this.bairro = value; }

	    public String getCaixaPostal() { return caixaPostal; }
	    public void setCaixaPostal(String value) { this.caixaPostal = value; }

	    public String getCep() { return cep; }
	    public void setCep(String value) { this.cep = value; }

	    public String getCepCaixaPostal() { return cepCaixaPostal; }
	    public void setCepCaixaPostal(String value) { this.cepCaixaPostal = value; }

	    public String getCidade() { return cidade; }
	    public void setCidade(String value) { this.cidade = value; }

	    public String getComplemento() { return complemento; }
	    public void setComplemento(String value) { this.complemento = value; }

	    public String getDdd() { return ddd; }
	    public void setDdd(String value) { this.ddd = value; }

	    public String getEmail() { return email; }
	    public void setEmail(String value) { this.email = value; }

	    public String getEndereco() { return endereco; }
	    public void setEndereco(String value) { this.endereco = value; }

	    public String getNome() { return nome; }
	    public void setNome(String value) { this.nome = value; }

	    public String getNumero() { return numero; }
	    public void setNumero(String value) { this.numero = value; }

	    public String getNumeroFax() { return numeroFax; }
	    public void setNumeroFax(String value) { this.numeroFax = value; }

	    public String getDddFax() { return dddFax; }
	    public void setDddFax(String value) { this.dddFax = value; }

	    public String getPais() { return pais; }
	    public void setPais(String value) { this.pais = value; }

	    public String getProvincia() { return provincia; }
	    public void setProvincia(String value) { this.provincia = value; }

	    public String getSiglaUF() { return siglaUF; }
	    public void setSiglaUF(String value) { this.siglaUF = value; }

	    public String getTelefone() { return telefone; }
	    public void setTelefone(String value) { this.telefone = value; }

	    public String getTitulo() { return titulo; }
	    public void setTitulo(String value) { this.titulo = value; }

		private static final char SEPARADOR = '#';
		
		private String replaceNull(String str) {
			return (str == null) ? "" : str;
		}
		
		private String truncateString(int maxLength, String str) {
			return str.substring(0, (maxLength > str.length() ? str.length() : maxLength));
		}
		
	    public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(truncateString(60, replaceNull(this.getApelido()))); sb.append(SEPARADOR);
			sb.append(truncateString(40, replaceNull(this.getTitulo()))); sb.append(SEPARADOR);
			sb.append(truncateString(50, replaceNull(this.getNome()))); sb.append(SEPARADOR);
			sb.append(truncateString(72, replaceNull(this.getEndereco()))); sb.append(SEPARADOR);
			sb.append(truncateString(72, replaceNull(this.getBairro()))); sb.append(SEPARADOR);
			sb.append(truncateString(30, replaceNull(this.getComplemento()))); sb.append(SEPARADOR);
			sb.append(truncateString( 6, replaceNull(this.getNumero()))); sb.append(SEPARADOR);
			sb.append(truncateString(72, replaceNull(this.getCidade()))); sb.append(SEPARADOR);
			sb.append(truncateString( 2, replaceNull(this.getSiglaUF()))); sb.append(SEPARADOR);
			sb.append(truncateString( 9, replaceNull(this.getCep()))); sb.append(SEPARADOR);
			sb.append(truncateString(50, replaceNull(this.getPais()))); sb.append(SEPARADOR);
			sb.append(truncateString(40, replaceNull(this.getProvincia()))); sb.append(SEPARADOR);
			sb.append(truncateString( 9, replaceNull(this.getCepCaixaPostal()))); sb.append(SEPARADOR);
			sb.append(truncateString(30, replaceNull(this.getCaixaPostal()))); sb.append(SEPARADOR);
			sb.append(truncateString( 4, replaceNull(this.getDdd()))); sb.append(SEPARADOR);
			sb.append(truncateString(20, replaceNull(this.getTelefone()))); sb.append(SEPARADOR);
			sb.append(truncateString(50, replaceNull(this.getEmail()))); sb.append(SEPARADOR);
			sb.append(truncateString( 4, replaceNull(this.getDddFax()))); sb.append(SEPARADOR);
			sb.append(truncateString( 8, replaceNull(this.getNumeroFax()))); sb.append(SEPARADOR);
			sb.append(truncateString(60, replaceNull(this.getAuxiliar1()))); sb.append(SEPARADOR);
			sb.append(truncateString(60, replaceNull(this.getAuxiliar2()))); sb.append(SEPARADOR);
			sb.append(truncateString(60, replaceNull(this.getAuxiliar3()))); sb.append(SEPARADOR);
			sb.append(truncateString(60, replaceNull(this.getAuxiliar4()))); sb.append(SEPARADOR);
			sb.append(truncateString(60, replaceNull(this.getAuxiliar5()))); sb.append(SEPARADOR);
			sb.append(truncateString(60, replaceNull(this.getAuxiliar6()))); sb.append(SEPARADOR);
			sb.append(truncateString(60, replaceNull(this.getAuxiliar7()))); sb.append(SEPARADOR);
			sb.append(truncateString(60, replaceNull(this.getAuxiliar8()))); sb.append(SEPARADOR);
			sb.append(truncateString(60, replaceNull(this.getAuxiliar9()))); sb.append("\r\n");
			return sb.toString();
		}
	}

	private String montaNomeIdExpediente(String nome, String idExpediente, int tamMaxNome) {
		tamMaxNome -= idExpediente.length() + 1;
		String nomeIdExpediente = 
			nome.substring(0, (tamMaxNome > nome.length() ? nome.length() : tamMaxNome)) 
			+ " " 
			+ idExpediente;
		return nomeIdExpediente;
	}
	
	private InformacaoContato montarInformacaoContato(
		int idProcesso, int idProcessoParteExpediente, String numeroProcesso, 
		DestinatarioECT destinatario, String[] auxiliar
	) throws PontoExtensaoException {
		InformacaoContato contato = new InformacaoContato();
		contato.setTitulo("PROCESSO " + numeroProcesso); // titulo;      
		contato.setNome(montaNomeIdExpediente(destinatario.getNome(), auxiliar[8], 50)); // nome e id do expediente;        
		contato.setEndereco(destinatario.getLogradouro()); // logradouro;  
		contato.setBairro(destinatario.getBairro()); // bairro;      
		contato.setComplemento(destinatario.getComplemento()); // complemento; 
		contato.setNumero(destinatario.getNumero()); // numero;      
		contato.setCidade(destinatario.getCidade()); // cidade;      
		contato.setSiglaUF(destinatario.getEstado()); // sigla_uf;    
		contato.setCep(destinatario.getCep()); // cep;         
		contato.setPais("Brasil"); // pais;
		if (auxiliar.length < 10) {
			auxiliar = Arrays.copyOf(auxiliar, 10);
		}
		contato.setAuxiliar1(auxiliar[1]); // auxiliar1;   
		contato.setAuxiliar2(auxiliar[2]); // auxiliar2; // (INATIVADO) link de acesso às peças  
		contato.setAuxiliar3(auxiliar[3]); // auxiliar3; // (INATIVADO) código de acesso às peças  
		contato.setAuxiliar4(auxiliar[4]); // auxiliar4;   
		contato.setAuxiliar5(auxiliar[5]); // auxiliar5;   
		contato.setAuxiliar6(auxiliar[6]); // auxiliar6;   
		contato.setAuxiliar7(auxiliar[7]); // auxiliar7; // código do expediente  
		contato.setAuxiliar8(auxiliar[8]); // auxiliar8; // número (id) do expediente   
		contato.setAuxiliar9(auxiliar[9]); // auxiliar9; // (INATIVADO) nome de usuário do solicitante do telegrama
		return contato;
	}

	private void escrever(OutputStream out, String str) {
		try {
			out.write(str.getBytes("ISO-8859-1"));
		} catch (UnsupportedEncodingException e) {
			logger.error("Exceção ao escrever na saída do arquivo de contatos (encoding).");
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("Exceção ao baixar a informação do contato.");
			e.printStackTrace();
		}
	}

	private void escreverInformacaoContato(InformacaoContato info, OutputStream out) {
		escrever(out, info.toString());
	}
	
	private void escreverExpedientesTelegrama(List<ProcessoExpediente> expedientes, String prefixo, OutputStream out) {
        for (ProcessoExpediente pe : expedientes) {
            if (pe.getMeioExpedicaoExpediente().equals(ExpedicaoExpedienteEnum.G)) {
                for (ProcessoParteExpediente ppe : pe.getProcessoParteExpedienteList()) {
        		    ppe = EntityUtil.refreshEntity(ppe);
                	escreverParteExpedienteTelegrama(pe, ppe, prefixo, out);
                }
            }
        }
	}
	
	private void escreverParteExpedienteTelegrama(ProcessoExpediente pe, ProcessoParteExpediente ppe, String prefixo, OutputStream out) {
		try {
			List<ProcessoParteExpedienteEndereco> enderecos = ppe.getProcessoParteExpedienteEnderecoList();
			if (enderecos == null || enderecos.size() == 0) {
				escrever(out, "=====> ERRO: Parte \"" + ppe.getNomePessoaParte() + "\" do processo " + pe.getProcessoTrf().getNumeroProcesso() + " não possui endereço associado.\r\n");
			}
			for (ProcessoParteExpedienteEndereco e : enderecos) {
				escreverParteExpedienteEnderecoTelegrama(pe, ppe, e, prefixo, out);
			}
		} catch (PontoExtensaoException e) {
			e.printStackTrace();
			logger.error("Erro ao baixar contato [" + ppe.getNomePessoaParte() + " ERRO: " + e.getLocalizedMessage() + "]");
			escrever(out, "=====> ERRO: Ao baixar contato [" + ppe.getNomePessoaParte() + " ERRO: " + e.getLocalizedMessage() + "]\r\n");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Erro inesperado ao baixar contato [" + ppe.getNomePessoaParte() + " ERRO: " + e.getLocalizedMessage() + "]");
			escrever(out, "=====> ERRO: Erro inesperado ao baixar contato [" + ppe.getNomePessoaParte() + " ERRO: " + e.getLocalizedMessage() + "]\r\n");
		}
		return;
	}
	
	private void escreverParteExpedienteEnderecoTelegrama(ProcessoExpediente pe, ProcessoParteExpediente ppe,
			ProcessoParteExpedienteEndereco e, String prefixo, OutputStream out) throws PontoExtensaoException {
		if(e.getNumeroAr() == null || e.getNumeroAr().isEmpty() || e.getNumeroAr().toLowerCase().equals("null")){
			DestinatarioECT destinatario = geraDestinatario(ppe, e);
		
			Usuario usuario = Authenticator.getUsuarioLogado();
			
			String[] auxiliar = new String[10];
			auxiliar[1] = " - ";
			auxiliar[7] = ConsultaExpedienteAction.criptografaIdProcessoParteExpediente(ppe.getIdProcessoParteExpediente());
			auxiliar[8] = String.valueOf(pe.getIdProcessoExpediente());
			
			InformacaoContato info = montarInformacaoContato(
				ppe.getProcessoJudicial().getIdProcessoTrf(),
				ppe.getIdProcessoParteExpediente(),
				ppe.getProcessoJudicial().getNumeroProcesso(),
				destinatario,
				auxiliar
		    );
			if (info.getNumero() == null || info.getNumero().isEmpty()) {
				escrever(out, "=====> ERRO: Parte \"" + ppe.getNomePessoaParte() + "\" do processo " + pe.getProcessoTrf().getNumeroProcesso() + " não possui número no endereço associado: ");
			}
			if (prefixo != null && !prefixo.isEmpty()) {
				escrever(out, prefixo);
			}
			escreverInformacaoContato(info, out);
		}
	}
	
	private static String trataNulo(final String s) {
		return (s != null && !s.trim().isEmpty()) ? s : "";
	}
	
	@Transactional
	private DestinatarioECT geraDestinatario(ProcessoParteExpediente ppe, ProcessoParteExpedienteEndereco e) {
		DestinatarioECT destinatario = new DestinatarioECT();
		
		if (ppe != null && ppe.getPessoaParte() != null && ppe.getPessoaParte().getNome() != null && !ppe.getPessoaParte().getNome().isEmpty()) {
			destinatario.setNome(ppe.getPessoaParte().getNome());
		}
		
		if (e == null || e.getEndereco() == null) {
			return destinatario;
		}
		Endereco end = e.getEndereco();
		Cep cepEnd = end.getCep();
		Cep cep;
		try {
			cep = cepManager.findById(cepEnd.getIdCep());
		} catch (PJeBusinessException pjeBusinessException) {
			cep = null;
			pjeBusinessException.printStackTrace();
		}
		
		String numeroCep = (cep != null) ? cep.getNumeroCep() : null;
		Municipio municipio = (cep != null) ? cep.getMunicipio() : null;
		
		Estado estado = null;
		try {
			estado = (municipio != null) ? municipio.getEstado() : null;
		} catch (LazyInitializationException lazyInitializationException) {
			try {
				int idEstado = municipio.getEstado().getIdEstado();
				estado = estadoManager.findById(idEstado);
			} catch (PJeBusinessException pjeBusinessException) {
				// ignora, assume que não conhece o estado.
			}
		}
		
		String codEstado = (estado != null) ? estado.getCodEstado() : null;
		destinatario.setNumero(trataNulo(end.getNumeroEndereco()));
		destinatario.setBairro(trataNulo(end.getNomeBairro()));
		destinatario.setCep(trataNulo(numeroCep));
		destinatario.setCidade(trataNulo(municipio.getMunicipio()));
		destinatario.setComplemento(trataNulo(end.getComplemento()));
		destinatario.setEstado(trataNulo(codEstado));
		destinatario.setLogradouro(trataNulo(end.getNomeLogradouro()));
		destinatario.setPontoReferencia("");
		
		return destinatario;
	}

	@Transactional
	private void baixarArquivoContatosTelegrama(List<ProcessoExpediente> expedientes, List<ProcessoExpediente> expedientesComErro) {
		if (expedientes.size() <= 0) {
			String message = "Não há expedientes a serem baixados!";
			logger.warn(message);
			FacesMessages.instance().add(Severity.WARN, message);
			return;
		}
		
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse)facesContext.getExternalContext().getResponse();
		response.setContentType("text/plain");
		
		String filename = "contatos";
		String extensao = ".txt";
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + extensao + "\"");

		OutputStream out = null;
		try {
			out = response.getOutputStream();
			escreverExpedientesTelegrama(expedientes, null, out);
			if (expedientesComErro != null && expedientesComErro.size() > 0) {
				escrever(out, "=====> Expedientes com ERRO:\r\n");
				escreverExpedientesTelegrama(expedientesComErro, "=====> ERRO: Localização do processo não corresponde ao usuário solicitante: ", out);
			}
			out.flush();
			facesContext.responseComplete();
		} catch (IOException e) {
			String errorMessage = "Erro ao baixar o arquivo: " + filename;
			logger.error(errorMessage);
			e.printStackTrace();
			FacesMessages.instance().add(Severity.ERROR, errorMessage);
		} catch (Exception e) {
			String errorMessage = "Erro inesperado ao baixar o arquivo: " + filename;
			logger.error(errorMessage);
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void baixarArquivoContatosTelegrama(String dataString, String formato) {
		List<ProcessoExpediente> expedientesComErro = new ArrayList<ProcessoExpediente>();
		List<ProcessoExpediente> listaExpedientes = recuperarExpedientesTelegrama(dataString, formato, expedientesComErro);
		baixarArquivoContatosTelegrama(listaExpedientes, expedientesComErro);
	}
	
	public void baixarArquivoContatosTelegrama(Date data) {
		List<ProcessoExpediente> expedientesComErro = new ArrayList<ProcessoExpediente>();
		List<ProcessoExpediente> listaExpedientes = recuperarExpedientesTelegrama(data, expedientesComErro);
		baixarArquivoContatosTelegrama(listaExpedientes, expedientesComErro);
	}
	
	public void download() {
		baixarArquivoContatosTelegrama(getData());
	}

}
