package service;

import model.Usuario;

import java.util.ArrayList;
import java.util.List;

public class Ranking {
    private final List<Usuario> usuarios = new ArrayList<>();

    public void adicionarUsuario(Usuario usuario) {
        usuarios.add(usuario);
    }

    public void ordenar() {
        for (int i = 0; i < usuarios.size() - 1; i++) {
            for (int j = 0; j < usuarios.size() - 1 - i; j++) {
                if (usuarios.get(j).getTotalPontos() < usuarios.get(j + 1).getTotalPontos()) {
                    Usuario atual = usuarios.get(j);
                    usuarios.set(j, usuarios.get(j + 1));
                    usuarios.set(j + 1, atual);
                }
            }
        }
    }

    public void exibirRanking() {
        ordenar();

        for (int i = 0; i < usuarios.size(); i++) {
            Usuario usuario = usuarios.get(i);
            System.out.println(
                    (i + 1) + "o - " + usuario.getNome()
                            + " - " + usuario.getTotalPontos() + " pontos"
            );
        }
    }
}
