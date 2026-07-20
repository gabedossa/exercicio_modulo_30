package dao;

import java.util.List;

import model.ItemPedido;

public interface ItemPedidoDAO {
    Integer cadastrar(ItemPedido item) throws Exception;
    Integer atualizar(ItemPedido item) throws Exception;
    List<ItemPedido> buscaPorPedido(Long pedidoId) throws Exception;
    List<ItemPedido> buscatodos() throws Exception;
    Integer excluir(ItemPedido item) throws Exception;
}
