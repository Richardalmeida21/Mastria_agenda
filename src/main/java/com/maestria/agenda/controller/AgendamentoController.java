package com.maestria.agenda.controller;

import com.maestria.agenda.agendamento.Agendamento;
import com.maestria.agenda.agendamento.AgendamentoRepository;
import com.maestria.agenda.agendamento.DadosCadastroAgendamento;
import com.maestria.agenda.cliente.Cliente;
import com.maestria.agenda.cliente.ClienteRepository;
import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.profissional.ProfissionalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime; // Importação necessária
import java.util.List;

@RestController
@RequestMapping("/agendamento")
@CrossOrigin(origins = "*")
public class AgendamentoController {

    private static final Logger logger = LoggerFactory.getLogger(AgendamentoController.class);

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final ProfissionalRepository profissionalRepository;

    public AgendamentoController(AgendamentoRepository agendamentoRepository, ClienteRepository clienteRepository, ProfissionalRepository profissionalRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.profissionalRepository = profissionalRepository;
    }

    // ✅ ADMIN vê todos os agendamentos, PROFISSIONAL vê apenas os seus
    @GetMapping
    public ResponseEntity<?> listarAgendamentos(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitando lista de agendamentos para: {}", userDetails.getUsername());

        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            logger.info("✅ ADMIN solicitou todos os agendamentos.");
            return ResponseEntity.ok(agendamentoRepository.findAll());
        } else {
            Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());
            if (profissional == null) {
                logger.warn("❌ Profissional não encontrado: {}", userDetails.getUsername());
                return ResponseEntity.status(403).body("Profissional não encontrado.");
            }
            logger.info("✅ PROFISSIONAL {} solicitou seus agendamentos.", profissional.getNome());
            return ResponseEntity.ok(agendamentoRepository.findByProfissional(profissional));
        }
    }

    // ✅ NOVA ROTA: PROFISSIONAL pode ver apenas seus próprios agendamentos
    @GetMapping("/profissional")
    public ResponseEntity<?> listarAgendamentosProfissional(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 PROFISSIONAL {} solicitando seus agendamentos.", userDetails.getUsername());

        Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());

        if (profissional == null) {
            logger.warn("❌ Profissional não encontrado.");
            return ResponseEntity.status(403).body("Profissional não encontrado.");
        }

        List<Agendamento> agendamentos = agendamentoRepository.findByProfissional(profissional);
        logger.info("✅ Retornando {} agendamentos para PROFISSIONAL {}", agendamentos.size(), profissional.getNome());
        return ResponseEntity.ok(agendamentos);
    }

    @GetMapping("/dia")
public ResponseEntity<List<Agendamento>> listarPorData(@RequestParam String data, @AuthenticationPrincipal UserDetails userDetails) {
    if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")) &&
        !userDetails.getAuthorities().contains(new SimpleGrantedAuthority("PROFISSIONAL"))) {
        logger.warn("❌ Acesso negado a {} para listar agendamentos por data", userDetails.getUsername());
        return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN e PROFISSIONAL podem acessar agendamentos por data.");
    }

    // Parse da data para LocalDate
    LocalDate dataFormatada = LocalDate.parse(data);

    // Definindo o início e o fim do dia como LocalDateTime
    LocalDateTime dataInicio = dataFormatada.atStartOfDay();  // Início do dia (00:00)
    LocalDateTime dataFim = dataFormatada.atTime(23, 59, 59);  // Final do dia (23:59:59)

    // Consultando os agendamentos dentro do intervalo de tempo do dia
    List<Agendamento> agendamentos = agendamentoRepository.findByDataBetween(dataInicio, dataFim);

    logger.info("🔍 Agendamentos para o dia {}: {}", dataFormatada, agendamentos.size());

    return ResponseEntity.ok(agendamentos);
}




    // ✅ Apenas ADMIN pode criar agendamentos
    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody DadosCadastroAgendamento dados, @AuthenticationPrincipal UserDetails userDetails) {
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            logger.warn("❌ Tentativa de criação de agendamento sem permissão por {}", userDetails.getUsername());
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode criar agendamentos.");
        }

        if (dados.clienteId() == null || dados.profissionalId() == null) {
            return ResponseEntity.badRequest().body("Erro: Cliente e Profissional devem ser informados.");
        }

        try {
            Cliente cliente = clienteRepository.findById(dados.clienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

            Profissional profissional = profissionalRepository.findById(dados.profissionalId())
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

            Agendamento agendamento = new Agendamento(dados, cliente, profissional);
            agendamentoRepository.save(agendamento);
            logger.info("✅ Agendamento criado com sucesso: {}", agendamento);
            return ResponseEntity.ok("Agendamento criado com sucesso.");
        } catch (Exception e) {
            logger.error("❌ Erro ao criar agendamento", e);
            return ResponseEntity.status(500).body("Erro ao criar agendamento.");
        }
    }

    // ✅ Apenas ADMIN pode excluir agendamentos
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirAgendamento(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            logger.warn("❌ Tentativa de exclusão de agendamento sem permissão por {}", userDetails.getUsername());
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode excluir agendamentos.");
        }

        if (!agendamentoRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Erro: Agendamento não encontrado.");
        }

        agendamentoRepository.deleteById(id);
        logger.info("✅ Agendamento excluído com sucesso. ID: {}", id);
        return ResponseEntity.ok("Agendamento excluído com sucesso.");
    }
}
