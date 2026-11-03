package br.edu.projeto;

import br.edu.projeto.database.ComputadorRepository;
import br.edu.projeto.model.Computador;
import br.edu.projeto.model.StatusManutencao;
import br.edu.projeto.service.ComputadorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ComputadorServiceTest {
    @TempDir Path diretorio;

    @Test void crudConclusaoEPersistencia() throws Exception {
        long id;
        try (ComputadorRepository repo = new ComputadorRepository(diretorio)) {
            ComputadorService service = new ComputadorService(repo);
            Computador criado = service.cadastrar("IdeaPad", "Lenovo", "Tela apagada", "HDMI funciona"); id = criado.id();
            assertEquals(criado, service.buscar(id).orElseThrow());
            assertEquals(1, service.listar().size());
            Computador atualizado = service.atualizar(id, "IdeaPad 3", "Lenovo", "Tela trincada", "Orçamento aprovado");
            assertEquals("IdeaPad 3", atualizado.modelo());
            assertEquals(StatusManutencao.CONCLUIDO, service.concluir(id).status());
        }
        try (ComputadorRepository repo = new ComputadorRepository(diretorio)) {
            ComputadorService service = new ComputadorService(repo);
            assertEquals("IdeaPad 3", service.buscar(id).orElseThrow().modelo());
            assertTrue(service.excluir(id));
            assertTrue(service.buscar(id).isEmpty());
        }
    }

    @Test void rejeitaCamposObrigatoriosVazios() throws Exception {
        try (ComputadorRepository repo = new ComputadorRepository(diretorio)) {
            ComputadorService service = new ComputadorService(repo);
            assertThrows(IllegalArgumentException.class, () -> service.cadastrar(" ", "Marca", "Falha", ""));
            assertThrows(IllegalArgumentException.class, () -> service.buscar(0));
        }
    }
}
