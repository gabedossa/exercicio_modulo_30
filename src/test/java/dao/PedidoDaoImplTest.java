package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import model.Cliente;
import model.Pedido;

class PedidoDaoImplTest {

    private final PedidoDaoImpl dao = new PedidoDaoImpl();
    private final ClienteDaoImpl clienteDao = new ClienteDaoImpl();
    private final List<Pedido> criados = new ArrayList<>();
    private final List<Cliente> clientesCriados = new ArrayList<>();

    private Pedido novoPedido(BigDecimal total) throws Exception {
        Pedido pedido = new Pedido();
        pedido.setTotal(total);
        Integer id = dao.cadastrar(pedido);
        pedido.setId(id.longValue());
        criados.add(pedido);
        return pedido;
    }

    private Cliente novoCliente() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setCodigo("TESTE-" + UUID.randomUUID());
        cliente.setNome("Cliente do Pedido");
        Integer id = clienteDao.cadastrar(cliente);
        cliente.setId(id.longValue());
        clientesCriados.add(cliente);
        return cliente;
    }

    @AfterEach
    void limparRegistrosCriados() {
        for (Pedido pedido : criados) {
            try {
                dao.excluir(pedido);
            } catch (Exception e) {

            }
        }
        criados.clear();

        for (Cliente cliente : clientesCriados) {
            try {
                clienteDao.excluir(cliente);
            } catch (Exception e) {

            }
        }
        clientesCriados.clear();
    }

    @Test
    void cadastrarDeveGerarIdEPersistirPedidoComDataPreenchidaPeloBanco() throws Exception {
        Pedido pedido = novoPedido(new BigDecimal("150.00"));

        assertNotNull(pedido.getId());

        Pedido encontrado = dao.buscaPedido(pedido.getId());
        assertNotNull(encontrado);
        assertNotNull(encontrado.getDataPedido());
        assertNull(encontrado.getClienteId());
        assertEquals(0, new BigDecimal("150.00").compareTo(encontrado.getTotal()));
    }

    @Test
    void atualizarDeveAlterarTotal() throws Exception {
        Pedido pedido = novoPedido(new BigDecimal("100.00"));

        pedido.setTotal(new BigDecimal("250.50"));
        Integer linhasAfetadas = dao.atualizar(pedido);

        assertEquals(1, linhasAfetadas);

        Pedido encontrado = dao.buscaPedido(pedido.getId());
        assertNotNull(encontrado);
        assertEquals(0, new BigDecimal("250.50").compareTo(encontrado.getTotal()));
    }

    @Test
    void cadastrarDevePersistirClienteDoPedido() throws Exception {
        Cliente cliente = novoCliente();

        Pedido pedido = new Pedido();
        pedido.setTotal(new BigDecimal("30.00"));
        pedido.setClienteId(cliente.getId());
        Integer id = dao.cadastrar(pedido);
        pedido.setId(id.longValue());
        criados.add(pedido);

        Pedido encontrado = dao.buscaPedido(pedido.getId());
        assertNotNull(encontrado);
        assertEquals(cliente.getId(), encontrado.getClienteId());
    }

    @Test
    void atualizarDeveAlterarERemoverClienteDoPedido() throws Exception {
        Pedido pedido = novoPedido(new BigDecimal("40.00"));
        Cliente cliente = novoCliente();

        pedido.setClienteId(cliente.getId());
        dao.atualizar(pedido);
        assertEquals(cliente.getId(), dao.buscaPedido(pedido.getId()).getClienteId());

        pedido.setClienteId(null);
        dao.atualizar(pedido);
        assertNull(dao.buscaPedido(pedido.getId()).getClienteId());
    }

    @Test
    void excluirClienteComPedidoDeveFalhar() throws Exception {
        Cliente cliente = novoCliente();
        Pedido pedido = novoPedido(new BigDecimal("50.00"));
        pedido.setClienteId(cliente.getId());
        dao.atualizar(pedido);

        assertThrows(Exception.class, () -> clienteDao.excluir(cliente));
    }

    @Test
    void buscaPedidoDeveRetornarNuloQuandoIdNaoExiste() throws Exception {
        Pedido encontrado = dao.buscaPedido(Long.MAX_VALUE);
        assertNull(encontrado);
    }

    @Test
    void buscatodosDeveIncluirPedidosCadastrados() throws Exception {
        Pedido pedido1 = novoPedido(new BigDecimal("10.00"));
        Pedido pedido2 = novoPedido(new BigDecimal("20.00"));

        List<Pedido> todos = dao.buscatodos();

        assertTrue(todos.stream().anyMatch(p -> p.getId().equals(pedido1.getId())));
        assertTrue(todos.stream().anyMatch(p -> p.getId().equals(pedido2.getId())));
    }

    @Test
    void excluirDeveRemoverPedidoDoBanco() throws Exception {
        Pedido pedido = novoPedido(new BigDecimal("5.00"));

        Integer linhasAfetadas = dao.excluir(pedido);
        assertEquals(1, linhasAfetadas);

        Pedido encontrado = dao.buscaPedido(pedido.getId());
        assertNull(encontrado);

        criados.remove(pedido);
    }
}
