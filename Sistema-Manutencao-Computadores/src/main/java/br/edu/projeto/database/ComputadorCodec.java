package br.edu.projeto.database;

import br.edu.projeto.model.Computador;
import br.edu.projeto.model.StatusManutencao;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

final class ComputadorCodec {
    private static final int VERSAO = 1;
    private static final int MAX_CAMPO = 16_384;

    byte[] codificar(Computador computador) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream out = new DataOutputStream(bytes)) {
                out.writeInt(VERSAO);
                out.writeLong(computador.id());
                escreverTexto(out, computador.modelo()); escreverTexto(out, computador.marca());
                escreverTexto(out, computador.defeito()); out.writeUTF(computador.status().name());
                out.writeUTF(computador.dataEntrada().toString()); escreverTexto(out, computador.observacao());
            }
            return bytes.toByteArray();
        } catch (IOException e) { throw new IllegalStateException("Falha ao codificar computador.", e); }
    }

    Computador decodificar(byte[] bytes) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            int versao = in.readInt();
            if (versao != VERSAO) throw new IOException("Versão de registro não suportada: " + versao);
            Computador computador = new Computador(in.readLong(), lerTexto(in), lerTexto(in), lerTexto(in),
                    StatusManutencao.valueOf(in.readUTF()), LocalDate.parse(in.readUTF()), lerTexto(in));
            if (in.available() != 0) throw new IOException("Registro contém bytes excedentes.");
            return computador;
        } catch (IOException | RuntimeException e) { throw new IllegalStateException("Registro de computador inválido.", e); }
    }

    private void escreverTexto(DataOutputStream out, String texto) throws IOException {
        byte[] bytes = texto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_CAMPO) throw new IllegalArgumentException("Campo excede o limite de 16 KB.");
        out.writeInt(bytes.length); out.write(bytes);
    }

    private String lerTexto(DataInputStream in) throws IOException {
        int tamanho = in.readInt();
        if (tamanho < 0 || tamanho > MAX_CAMPO) throw new IOException("Tamanho de campo inválido.");
        byte[] bytes = in.readNBytes(tamanho);
        if (bytes.length != tamanho) throw new EOFException("Campo incompleto.");
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
