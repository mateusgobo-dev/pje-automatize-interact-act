package br.jus.pje.nucleo.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.entidades.identidade.LogAcesso;
import br.jus.pje.nucleo.entidades.log.EntityLog;
import br.jus.pje.nucleo.enums.TiposObjetosUnificadosEnum;

@Entity
@Table(name = UnificacaoPessoasObjeto.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_unificacao_pessoas_objeto", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_unificacao_pessoas_objeto"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class UnificacaoPessoasObjeto implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<UnificacaoPessoasObjeto,Integer> {

	public static final String TABLE_NAME = "tb_unificacao_pessoas_objeto";
	private static final long serialVersionUID = 1L;

	private int idUnificacaoPessoasObjeto;
	private String id_objeto_unificado;
	private TiposObjetosUnificadosEnum inTipoObjetoUnificacao;
	private UnificacaoPessoas unificacaoPessoas;
	private String informacao;
	private Pessoa pessoaPrincipal;
	private Pessoa pessoaSecundaria;
	private boolean ativo;
	private Boolean encontradoDesunificacao = null;

	public UnificacaoPessoasObjeto() {}

	public UnificacaoPessoasObjeto(Pessoa pessoaPrincipal, Pessoa pessoaSecundaria, UnificacaoPessoas unifiPes, TiposObjetosUnificadosEnum tipoObjetoUnificado, Object objeto) {
		this.inTipoObjetoUnificacao = tipoObjetoUnificado;
		this.unificacaoPessoas = unifiPes;
		this.pessoaPrincipal = pessoaPrincipal;
		this.pessoaSecundaria = pessoaSecundaria;
		this.ativo = Boolean.TRUE;
		
		switch (tipoObjetoUnificado) {
			
			case LOG_ACESSO:
				LogAcesso logAcesso = (LogAcesso) objeto;
				this.id_objeto_unificado = logAcesso.getIdLogAcesso().toString();
				break;
			
			case CARACTERISTICA_FISICA:
				CaracteristicaFisica caractFis = (CaracteristicaFisica) objeto;
				this.id_objeto_unificado = caractFis.getId().toString();
				break;
			
			case MEIO_CONTATO_CADASTRADOS:
			case MEIO_CONTATO_PROPRIETARIA:
				MeioContato meioContato = (MeioContato) objeto;
				this.id_objeto_unificado = Integer.toString(meioContato.getIdMeioContato());
				this.informacao = meioContato.getValorMeioContato();
				break;
				
			case NOMES_ALTERNATIVOS_CADASTRADOS:
			case NOMES_ALTERNATIVOS_PROPRIETARIA:
				PessoaNomeAlternativo nomeAlternativo = (PessoaNomeAlternativo) objeto;
				this.id_objeto_unificado = Integer.toString(nomeAlternativo.getIdPessoaNomeAlternativo());
				this.informacao = nomeAlternativo.getPessoaNomeAlternativo();
				break;
				
			case CONEXOES_PREVENCAO:
				ProcessoTrfConexao conexaoPrevencao = (ProcessoTrfConexao) objeto;
				this.id_objeto_unificado = Integer.toString(conexaoPrevencao.getIdProcessoTrfConexao());
				break;
				
			case SEGREDO_PROCESSO:
				ProcessoSegredo segredoProcesso = (ProcessoSegredo) objeto;
				this.id_objeto_unificado = Integer.toString(segredoProcesso.getIdProcessoSegredo());
				break;
				
			case SIGILO_PROCESSO_PARTE:
				ProcessoParteSigilo sigiloProcessoParte = (ProcessoParteSigilo) objeto;
				this.id_objeto_unificado = Integer.toString(sigiloProcessoParte.getIdProcessoParteSigilo());
				break;
			
			case CAIXA_REPRESENTANTE:
				CaixaRepresentante caixa = (CaixaRepresentante) objeto;
				this.id_objeto_unificado = Integer.toString(caixa.getIdCaixaRepresentante());
				break;
				
			case SESSAO_ENTE_EXTERNO:
				SessaoEnteExterno sessEntExt = (SessaoEnteExterno) objeto;
				this.id_objeto_unificado = Integer.toString(sessEntExt.getIdSessaoEnteExterno());
				break;
				
			case PROCESSO_REDISTRIBUICAO:
				ProcessoTrfRedistribuicao procRedistrib = (ProcessoTrfRedistribuicao) objeto;
				this.id_objeto_unificado = Integer.toString(procRedistrib.getIdProcessoTrfRedistribuicao());
				break;
				
			case PROCESSO_PARTE_HISTORICO:
				ProcessoParteHistorico procParteHist = (ProcessoParteHistorico) objeto;
				this.id_objeto_unificado = Integer.toString(procParteHist.getIdProcessoParteHistorico());
				break;
				
			case PROCESSO_TAG:
				ProcessoTag procTag = (ProcessoTag) objeto;
				this.id_objeto_unificado = Integer.toString(procTag.getIdProcessoTag());
				break;
				
			case LEMBRETE:
				Lembrete lembrete = (Lembrete) objeto;
				this.id_objeto_unificado = Integer.toString(lembrete.getIdLembrete());
				break;
				
			case PERMISSAO_LEMBRETE:
				LembretePermissao lembretePermissao = (LembretePermissao) objeto;
				this.id_objeto_unificado = Integer.toString(lembretePermissao.getIdLembretePermissao());
				break;
				
			case PROCESSOS_PROTOCOLADOS:
				Processo processo = (Processo) objeto;
				this.id_objeto_unificado = Integer.toString(processo.getIdProcesso());
				break;
				
			case PARAMETROS_ALTERADOS:
				Parametro parametro = (Parametro) objeto;
				this.id_objeto_unificado = Integer.toString(parametro.getIdParametro());
				this.informacao = parametro.getValorVariavel();
				break;
				
			case ENTITY_LOGS:
				EntityLog entityLog = (EntityLog) objeto;
				this.id_objeto_unificado = Long.toString(entityLog.getIdLog());
				break;
				
			case SOLICITACAO_NO_DESVIO:
				SolicitacaoNoDesvio solicitacao = (SolicitacaoNoDesvio) objeto;
				this.id_objeto_unificado = Integer.toString(solicitacao.getIdSolicitacaoNoDesvio());
				break;
				
			case SESSAO_PAUTA_PROC_INCLUSORA:
			case SESSAO_PAUTA_PROC_EXCLUSORA:
				SessaoPautaProcessoTrf sessaoPautaProcesso = (SessaoPautaProcessoTrf) objeto;
				this.id_objeto_unificado = Integer.toString(sessaoPautaProcesso.getIdSessaoPautaProcessoTrf());
				break;
				
			case SESSAO_INCLUSORA:
			case SESSAO_EXCLUSORA:
				Sessao sessao = (Sessao) objeto;
				this.id_objeto_unificado = Integer.toString(sessao.getIdSessao());
				break;
				
			case QUADRO_AVISO:
				QuadroAviso aviso = (QuadroAviso) objeto;
				this.id_objeto_unificado = Integer.toString(aviso.getIdQuadroAviso());
				break;
				
			case PROCESSO_DOCUMENTO_FAVORITO:
				ProcessoDocumentoFavorito procDocFav = (ProcessoDocumentoFavorito) objeto;
				this.id_objeto_unificado = Integer.toString(procDocFav.getIdProcessoDocumentoFavorito());
				break;
				
			case NOTAS_SESSAO_JULG:
				NotaSessaoJulgamento notaSessJulg = (NotaSessaoJulgamento) objeto;
				this.id_objeto_unificado = Integer.toString(notaSessJulg.getIdNotaSessaoJulgamento());
				break;
				
			case MODELOS_PROCLAMACAO_JULG:
				ModeloProclamacaoJulgamento modProclaJulg = (ModeloProclamacaoJulgamento) objeto;
				this.id_objeto_unificado = Integer.toString(modProclaJulg.getId());
				break;
				
			case LOG_HIST_MOVIMENTACAO:
				LogHistoricoMovimentacao logHistMov = (LogHistoricoMovimentacao) objeto;
				this.id_objeto_unificado = Integer.toString(logHistMov.getIdLog());
				break;
				
			case VISIBILIDADE_DOC_IDENTIFICACAO:
				VisibilidadePessoaDocumentoIdentificacao visibDocIdent = (VisibilidadePessoaDocumentoIdentificacao) objeto;
				this.id_objeto_unificado = Long.toString(visibDocIdent.getId());
				break;
				
		}
	}

	@Id
	@GeneratedValue(generator = "gen_unificacao_pessoas_objeto")
	@Column(name = "id_unificacao_pessoas_objeto", unique = true, nullable = false)
	public int getIdUnificacaoPessoasObjeto() {
		return this.idUnificacaoPessoasObjeto;
	}

	public void setIdUnificacaoPessoasObjeto(int idUnificacaoPessoasObjeto) {
		this.idUnificacaoPessoasObjeto = idUnificacaoPessoasObjeto;
	}

	@Column(name = "ds_id_objeto_unificado", nullable = false)
	@NotNull
	public String getId_objeto_unificado() {
		return id_objeto_unificado;
	}

	public void setId_objeto_unificado(String id_objeto_unificado) {
		this.id_objeto_unificado = id_objeto_unificado;
	}
	
	@Column(name = "in_tipo_objeto_unificado", length = 20)
	@Enumerated(EnumType.STRING)
	public TiposObjetosUnificadosEnum getInTipoObjetoUnificacao() {
		return inTipoObjetoUnificacao;
	}

	public void setInTipoObjetoUnificacao(TiposObjetosUnificadosEnum inTipoObjetoUnificacao) {
		this.inTipoObjetoUnificacao = inTipoObjetoUnificacao;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_unificacao_pessoas", nullable = false)
	@NotNull
	public UnificacaoPessoas getUnificacaoPessoas() {
		return this.unificacaoPessoas;
	}

	public void setUnificacaoPessoas(UnificacaoPessoas unificacaoPessoas) {
		this.unificacaoPessoas = unificacaoPessoas;
	}
	
	@Column(name = "ds_informacao", length = 500)
	public String getInformacao() {
		return informacao;
	}

	public void setInformacao(String informacao) {
		this.informacao = informacao;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}
	
	@Column(name = "in_encontrado_desunificacao", nullable = true)
	public Boolean getEncontradoDesunificacao() {
		return encontradoDesunificacao;
	}

	public void setEncontradoDesunificacao(Boolean encontradoDesunificacao) {
		this.encontradoDesunificacao = encontradoDesunificacao;
	}
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_pessoa_principal", nullable = false)
	@NotNull
	public Pessoa getPessoaPrincipal() {
		return this.pessoaPrincipal;
	}

	public void setPessoaPrincipal(Pessoa pessoaPrincipal) {
		this.pessoaPrincipal = pessoaPrincipal;
	}
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_pessoa_secundaria", nullable = false)
	@NotNull
	public Pessoa getPessoaSecundaria() {
		return this.pessoaSecundaria;
	}

	public void setPessoaSecundaria(Pessoa pessoaSecundaria) {
		this.pessoaSecundaria = pessoaSecundaria;
	}

	@Override
	public String toString() {
		return "Objeto "+inTipoObjetoUnificacao;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends UnificacaoPessoasObjeto> getEntityClass() {
		return UnificacaoPessoasObjeto.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdUnificacaoPessoasObjeto());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
