package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import model.Pedido;

class PedidoDaoImplTest {

    private final PedidoDaoImpl dao = new PedidoDaoImpl();
    private final List<Pedido> criados = new ArrayList<>();

    private Pedido novoPedido(BigDecimal total) throws Exception {
        Pedido pedido = new Pedido();
        pedido.setTotal(total);
        Integer id = dao.cadastrar(pedido);
        pedido.setId(id.longValue());
        criados.add(pedido);
        return pedido;
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
    }

    @Test
    void cadastrarDeveGerarIdEPersistirPedidoComDataPreenchidaPeloBanco() throws Exception {
        Pedido pedido = novoPedido(new BigDecimal("150.00"));

        assertNotNull(pedido.getId());

        Pedido encontrado = dao.buscaPedido(pedido.getId());
        assertNotNull(encontrado);
        assertNotNull(encontrado.getDataPedido());
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
