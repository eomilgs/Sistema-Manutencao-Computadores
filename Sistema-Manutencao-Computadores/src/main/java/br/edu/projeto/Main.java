package br.edu.projeto;

import br.edu.projeto.database.ComputadorRepository;
import br.edu.projeto.model.Computador;
import br.edu.projeto.service.ComputadorService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

public final class Main {
    private Main() { }
    public static void main(String[] args) {
        Path diretorio = args.length > 0 ? Path.of(args[0]) : Path.of("data", "infinispan");
        try (ComputadorRepository repo = new ComputadorRepository(diretorio); Scanner scanner = new Scanner(System.in)) {
            executar(new ComputadorService(repo), scanner, diretorio);
        } catch (IOException | RuntimeException e) {
            System.err.println("Não foi possível abrir ou usar o banco em " + diretorio + ": " + e.getMessage());
        }
    }

    private static void executar(ComputadorService service, Scanner scanner, Path diretorio) {
        System.out.println("Sistema de Manutenção de Computadores — dados em " + diretorio.toAbsolutePath());
        boolean continuar = true;
        while (continuar) {
            System.out.println("\n1 Cadastrar | 2 Listar | 3 Consultar | 4 Atualizar | 5 Excluir | 6 Concluir reparo | 0 Sair");
            String opcao = ler(scanner, "Opção: ");
            try {
                switch (opcao) {
                    case "1" -> cadastrar(service, scanner);
                    case "2" -> {
                        var computadores = service.listar();
                        if (computadores.isEmpty()) System.out.println("Nenhum computador cadastrado.");
                        else computadores.forEach(Main::mostrar);
                    }
                    case "3" -> service.buscar(lerId(scanner)).ifPresentOrElse(Main::mostrar, () -> System.out.println("Computador não encontrado."));
                    case "4" -> atualizar(service, scanner);
                    case "5" -> System.out.println(service.excluir(lerId(scanner)) ? "Excluído." : "Computador não encontrado.");
                    case "6" -> mostrar(service.concluir(lerId(scanner)));
                    case "0" -> continuar = false;
                    default -> System.out.println("Opção inválida.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) { System.out.println("Erro: " + e.getMessage()); }
        }
        System.out.println("Banco fechado. Até logo!");
    }

    private static void cadastrar(ComputadorService s, Scanner in) {
        Computador c = s.cadastrar(ler(in,"Modelo: "), ler(in,"Marca: "), ler(in,"Defeito: "), ler(in,"Observação (opcional): "));
        System.out.println("Cadastrado com ID " + c.id());
    }
    private static void atualizar(ComputadorService s, Scanner in) {
        Computador c = s.atualizar(lerId(in), ler(in,"Novo modelo: "), ler(in,"Nova marca: "), ler(in,"Novo defeito: "), ler(in,"Nova observação: "));
        System.out.println("Atualizado."); mostrar(c);
    }
    private static long lerId(Scanner in) {
        try { return Long.parseLong(ler(in,"ID: ")); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Digite um ID numérico válido."); }
    }
    private static String ler(Scanner in, String prompt) { System.out.print(prompt); return in.nextLine(); }
    private static void mostrar(Computador c) {
        System.out.printf("ID: %d | %s %s | Defeito: %s | Status: %s | Entrada: %s | Observação: %s%n",
                c.id(), c.marca(), c.modelo(), c.defeito(), c.status(), c.dataEntrada(), c.observacao());
    }
}
