package com.maestria.agenda.agendamento;

import com.maestria.agenda.cliente.Cliente;
import com.maestria.agenda.profissional.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    
    List<Agendamento> findByCliente(Cliente cliente);
    
    List<Agendamento> findByProfissional(Profissional profissional);

    List<Agendamento> findByDataHoraBetween(LocalDateTime dataInicio, LocalDateTime dataFim);

    // Método para verificar se há conflito de horários para um profissional
    boolean existsByProfissionalAndDataHoraBetween(
        Profissional profissional, LocalDateTime inicio, LocalDateTime fim
    );
}
