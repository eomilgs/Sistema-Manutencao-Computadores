package br.edu.projeto.model;

import java.time.LocalDate;

public record Computador(long id, String modelo, String marca, String defeito,
                         StatusManutencao status, LocalDate dataEntrada, String observacao) {
    public Computador {
        if (id <= 0) throw new IllegalArgumentException("O ID deve ser positivo.");
        if (modelo == null || marca == null || defeito == null || status == null || dataEntrada == null || observacao == null)
            throw new IllegalArgumentException("Os campos do computador não podem ser nulos.");
    }

    public Computador comDados(String modelo, String marca, String defeito, String observacao) {
        return new Computador(id, modelo, marca, defeito, status, dataEntrada, observacao);
    }

    public Computador concluir() {
        return new Computador(id, modelo, marca, defeito, StatusManutencao.CONCLUIDO, dataEntrada, observacao);
    }
}
