package dao;

import java.util.List;

import model.Produto;

public interface ProdutoDAO {
    Integer cadastrar(Produto produto) throws Exception;
    Integer atualizar(Produto produto) throws Exception;
    Produto buscaProduto(String codigo) throws Exception;
    List<Produto> buscatodos() throws Exception;
    Integer excluir(Produto produto) throws Exception;
}
