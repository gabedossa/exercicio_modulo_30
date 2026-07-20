package dao;

import java.util.List;

import model.Pedido;

public interface PedidoDAO {
    Integer cadastrar(Pedido pedido) throws Exception;
    Integer atualizar(Pedido pedido) throws Exception;
    Pedido buscaPedido(Long id) throws Exception;
    List<Pedido> buscatodos() throws Exception;
    Integer excluir(Pedido pedido) throws Exception;
}
