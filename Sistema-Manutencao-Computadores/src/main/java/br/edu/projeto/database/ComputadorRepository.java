package br.edu.projeto.database;

import br.edu.projeto.model.Computador;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ComputadorRepository implements AutoCloseable {
    private static final String NOME_CACHE = "computadores";
    private final DefaultCacheManager cacheManager;
    private final Cache<String, byte[]> cache;
    private final ComputadorCodec codec = new ComputadorCodec();

    public ComputadorRepository(Path diretorio) throws IOException {
        Path local = diretorio.toAbsolutePath().normalize();
        Files.createDirectories(local);

        GlobalConfigurationBuilder global = new GlobalConfigurationBuilder();
        global.globalState().enable().persistentLocation(local.toString());
        DefaultCacheManager manager = new DefaultCacheManager(global.build());
        try {
            Configuration configuracao = new ConfigurationBuilder()
                    .persistence()
                        .passivation(false)
                        .addSoftIndexFileStore()
                            .shared(false)
                            .dataLocation("computadores/dados")
                            .indexLocation("computadores/indices")
                    .build();
            manager.defineConfiguration(NOME_CACHE, configuracao);
            cacheManager = manager;
            cache = manager.getCache(NOME_CACHE);
        } catch (RuntimeException e) {
            manager.stop();
            throw e;
        }
    }

    public synchronized long proximoId() {
        long maior = 0;
        for (String chave : cache.keySet()) {
            try { maior = Math.max(maior, Long.parseLong(chave)); }
            catch (NumberFormatException e) { throw new IllegalStateException("Chave inválida encontrada no cache.", e); }
        }
        if (maior == Long.MAX_VALUE) throw new IllegalStateException("Não há mais IDs disponíveis.");
        return maior + 1;
    }

    public synchronized void salvar(Computador computador) {
        String chave = chave(computador.id());
        if (cache.putIfAbsent(chave, codec.codificar(computador)) != null)
            throw new IllegalStateException("Já existe um computador com esse ID.");
    }

    public synchronized Optional<Computador> buscar(long id) {
        byte[] valor = cache.get(chave(id));
        return valor == null ? Optional.empty() : Optional.of(codec.decodificar(valor));
    }

    public synchronized List<Computador> listar() {
        List<Computador> resultado = new ArrayList<>();
        for (Map.Entry<String, byte[]> entrada : cache.entrySet())
            resultado.add(codec.decodificar(entrada.getValue()));
        resultado.sort(Comparator.comparingLong(Computador::id));
        return resultado;
    }

    public synchronized void atualizar(Computador computador) {
        if (cache.replace(chave(computador.id()), codec.codificar(computador)) == null)
            throw new IllegalStateException("Computador não encontrado para atualização.");
    }

    public synchronized boolean excluir(long id) { return cache.remove(chave(id)) != null; }

    private String chave(long id) { return Long.toString(id); }

    @Override public synchronized void close() { cacheManager.stop(); }
}
