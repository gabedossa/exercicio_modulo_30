package dao;

import java.util.List;

import model.Cliente;

public interface ClienteDAO {
    Integer cadastrar(Cliente cliente) throws Exception;
    Integer atualizar(Cliente cliente) throws Exception;
    Cliente buscaCliente(String codigo) throws Exception;
    List<Cliente> buscatodos() throws Exception;
    Integer excluir(Cliente cliente) throws Exception;
}
