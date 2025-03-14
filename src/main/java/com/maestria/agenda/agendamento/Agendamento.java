package com.maestria.agenda.agendamento;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.util.Objects;
import com.maestria.agenda.cliente.Cliente;
import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.servicos.Servicos;
import jakarta.persistence.*;

@Entity
@Table(name = "agendamento")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Tipo Long (classe wrapper)

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @Enumerated(EnumType.STRING)
    private Servicos servico;

    private LocalDate data;
    private LocalTime hora;

    @Convert(converter = DurationConverter.class) // Aplica o conversor
    private Duration duracao;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    // Construtor com DadosCadastroAgendamento
    public Agendamento(DadosCadastroAgendamento dados, Cliente cliente, Profissional profissional) {
        this.cliente = cliente;
        this.profissional = profissional;
        this.servico = dados.servico();
        this.data = dados.data();
        this.hora = dados.hora();
        this.duracao = Duration.parse(dados.duracao()); // Converte a string para Duration
        this.observacao = dados.observacao();
    }

    // Construtor padrão necessário para o JPA
    public Agendamento() {}

    // Getters e Setters
    public Long getId() { // Retorna Long, não long
        return id;
    }

    public void setId(Long id) { // Aceita Long, não long
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public Servicos getServico() {
        return servico;
    }

    public void setServico(Servicos servico) {
        this.servico = servico;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public Duration getDuracao() {
        return duracao;
    }

    public void setDuracao(Duration duracao) {
        this.duracao = duracao;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    // Método para formatar a duração
    public String getDuracaoFormatada() {
        if (duracao == null) {
            return "0 min";
        }
        long minutos = duracao.toMinutes();
        return minutos + " min";
    }

    @Override
    public String toString() {
        return "Agendamento{" +
                "id=" + id +
                ", cliente=" + cliente +
                ", profissional=" + profissional +
                ", servico=" + servico +
                ", data=" + data +
                ", hora=" + hora +
                ", duracao=" + getDuracaoFormatada() + // Usando o método formatado
                ", observacao='" + observacao + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agendamento that = (Agendamento) o;
        return Objects.equals(id, that.id) && // Usando Objects.equals para comparar Long
                Objects.equals(cliente, that.cliente) &&
                Objects.equals(profissional, that.profissional) &&
                servico == that.servico &&
                Objects.equals(data, that.data) &&
                Objects.equals(hora, that.hora) &&
                Objects.equals(duracao, that.duracao) &&
                Objects.equals(observacao, that.observacao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cliente, profissional, servico, data, hora, duracao, observacao);
    }
}
