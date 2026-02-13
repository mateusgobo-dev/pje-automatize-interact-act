package br.jus.pje.nucleo.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = ProtocoloExternoMni.TABLE_NAME)
@SequenceGenerator(allocationSize = 1, name = "gen_protocolo_externo_mni", sequenceName = "sq_tb_protocolo_externo_mni")
public class ProtocoloExternoMni implements java.io.Serializable{
	
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "tb_protocolo_externo_mni";
	
	private int idProtocoloExternoMNI;
	private Integer idProcesso;
	private String numeroIdentificadorSistemaExterno;
	private Procuradoria procuradoria;
	private Usuario usuario;
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_protocolo_externo_mni")
	@Column(name = "id_protoc_externo_mni", unique = true, nullable = false)
	public int getIdProtocoloExternoMNI() {
		return this.idProtocoloExternoMNI;
	}

	public void setIdProtocoloExternoMNI(int idProtocoloExternoMNI) {
		this.idProtocoloExternoMNI = idProtocoloExternoMNI;
	}
	
	@Column(name = "nr_ident_sistema_externo", nullable = false)
	public String getNumeroIdentificadorSistemaExterno() {
		return this.numeroIdentificadorSistemaExterno;
	}

	public void setNumeroIdentificadorSistemaExterno(String numeroIdentificadorSistemaExterno) {
		this.numeroIdentificadorSistemaExterno = numeroIdentificadorSistemaExterno;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_procuradoria")
	public Procuradoria getProcuradoria() {
		return procuradoria;
	}

	public void setProcuradoria(Procuradoria procuradoria) {
		this.procuradoria = procuradoria;
	}
	
	@ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "id_usuario") 
    public Usuario getUsuario() { 
      return usuario; 
    } 
   
    public void setUsuario(Usuario usuario) { 
      this.usuario = usuario; 
    }

	@Column(name = "id_processo", nullable = false)
	public Integer getIdProcesso() {
		return idProcesso;
	}

	public void setIdProcesso(Integer idProcesso) {
		this.idProcesso = idProcesso;
	}
    

}
