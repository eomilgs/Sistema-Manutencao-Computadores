package br.edu.projeto.service;

import br.edu.projeto.database.ComputadorRepository;
import br.edu.projeto.model.Computador;
import br.edu.projeto.model.StatusManutencao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public final class ComputadorService {
    private final ComputadorRepository repository;
    public ComputadorService(ComputadorRepository repository) { this.repository = repository; }

    public Computador cadastrar(String modelo, String marca, String defeito, String observacao) {
        validarTexto("Modelo", modelo); validarTexto("Marca", marca); validarTexto("Defeito", defeito);
        if (observacao == null) observacao = "";
        Computador computador = new Computador(repository.proximoId(), limpar(modelo), limpar(marca), limpar(defeito),
                StatusManutencao.EM_ANDAMENTO, LocalDate.now(), limpar(observacao));
        repository.salvar(computador); return computador;
    }

    public List<Computador> listar() { return repository.listar(); }
    public Optional<Computador> buscar(long id) { validarId(id); return repository.buscar(id); }

    public Computador atualizar(long id, String modelo, String marca, String defeito, String observacao) {
        validarId(id); validarTexto("Modelo", modelo); validarTexto("Marca", marca); validarTexto("Defeito", defeito);
        Computador atual = repository.buscar(id).orElseThrow(() -> new IllegalArgumentException("Computador não encontrado."));
        Computador atualizado = atual.comDados(limpar(modelo), limpar(marca), limpar(defeito), limpar(observacao == null ? "" : observacao));
        repository.atualizar(atualizado); return atualizado;
    }

    public boolean excluir(long id) { validarId(id); return repository.excluir(id); }

    public Computador concluir(long id) {
        validarId(id);
        Computador atual = repository.buscar(id).orElseThrow(() -> new IllegalArgumentException("Computador não encontrado."));
        Computador concluido = atual.concluir(); repository.atualizar(concluido); return concluido;
    }

    private void validarId(long id) { if (id <= 0) throw new IllegalArgumentException("O ID deve ser positivo."); }
    private void validarTexto(String campo, String valor) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(campo + " é obrigatório.");
        if (valor.trim().length() > 2000) throw new IllegalArgumentException(campo + " deve ter até 2.000 caracteres.");
    }
    private String limpar(String valor) { return valor.trim(); }
}
