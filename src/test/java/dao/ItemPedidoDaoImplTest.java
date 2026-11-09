package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import model.ItemPedido;
import model.Pedido;
import model.Produto;

class ItemPedidoDaoImplTest {

    private final ItemPedidoDaoImpl dao = new ItemPedidoDaoImpl();
    private final PedidoDaoImpl pedidoDao = new PedidoDaoImpl();
    private final ProdutoDaoImpl produtoDao = new ProdutoDaoImpl();

    private final List<ItemPedido> itensCriados = new ArrayList<>();
    private final List<Pedido> pedidosCriados = new ArrayList<>();
    private final List<Produto> produtosCriados = new ArrayList<>();

    private Pedido novoPedido() throws Exception {
        Pedido pedido = new Pedido();
        pedido.setTotal(BigDecimal.ZERO);
        Integer id = pedidoDao.cadastrar(pedido);
        pedido.setId(id.longValue());
        pedidosCriados.add(pedido);
        return pedido;
    }

    private Produto novoProduto() throws Exception {
        Produto produto = new Produto();
        produto.setCodigo("TESTE-" + UUID.randomUUID());
        produto.setNome("Produto de teste");
        produto.setPreco(new BigDecimal("10.00"));
        produto.setQuantidade(100);
        Integer id = produtoDao.cadastrar(produto);
        produto.setId(id.longValue());
        produtosCriados.add(produto);
        return produto;
    }

    private ItemPedido novoItem(Long pedidoId, Long produtoId, Integer quantidade, BigDecimal precoUnitario)
            throws Exception {
        ItemPedido item = new ItemPedido();
        item.setPedidoId(pedidoId);
        item.setProdutoId(produtoId);
        item.setQuantidade(quantidade);
        item.setPrecoUnitario(precoUnitario);
        Integer id = dao.cadastrar(item);
        item.setId(id.longValue());
        itensCriados.add(item);
        return item;
    }

    @AfterEach
    void limparRegistrosCriados() {
        for (ItemPedido item : itensCriados) {
            try {
                dao.excluir(item);
            } catch (Exception e) {

            }
        }
        itensCriados.clear();

        for (Pedido pedido : pedidosCriados) {
            try {
                pedidoDao.excluir(pedido);
            } catch (Exception e) {

            }
        }
        pedidosCriados.clear();

        for (Produto produto : produtosCriados) {
            try {
                produtoDao.excluir(produto);
            } catch (Exception e) {

            }
        }
        produtosCriados.clear();
    }

    @Test
    void cadastrarDeveGerarIdEPersistirItem() throws Exception {
        Pedido pedido = novoPedido();
        Produto produto = novoProduto();

        ItemPedido item = novoItem(pedido.getId(), produto.getId(), 3, new BigDecimal("10.50"));

        assertNotNull(item.getId());

        List<ItemPedido> itensDoPedido = dao.buscaPorPedido(pedido.getId());
        assertTrue(itensDoPedido.stream().anyMatch(i -> i.getId().equals(item.getId())
                && i.getProdutoId().equals(produto.getId())
                && i.getQuantidade().equals(3)
                && new BigDecimal("10.50").compareTo(i.getPrecoUnitario()) == 0));
    }

    @Test
    void atualizarDeveAlterarQuantidadeEPrecoUnitario() throws Exception {
        Pedido pedido = novoPedido();
        Produto produto = novoProduto();
        ItemPedido item = novoItem(pedido.getId(), produto.getId(), 1, new BigDecimal("5.00"));

        item.setQuantidade(9);
        item.setPrecoUnitario(new BigDecimal("7.25"));
        item.setDesconto(new BigDecimal("0.75"));
        Integer linhasAfetadas = dao.atualizar(item);

        assertEquals(1, linhasAfetadas);

        List<ItemPedido> itensDoPedido = dao.buscaPorPedido(pedido.getId());
        ItemPedido atualizado = itensDoPedido.stream().filter(i -> i.getId().equals(item.getId())).findFirst()
                .orElse(null);
        assertNotNull(atualizado);
        assertEquals(9, atualizado.getQuantidade());
        assertEquals(0, new BigDecimal("7.25").compareTo(atualizado.getPrecoUnitario()));
        assertEquals(0, new BigDecimal("0.75").compareTo(atualizado.getDesconto()));
    }

    @Test
    void cadastrarDeveUsarDescontoZeroQuandoNaoInformado() throws Exception {
        Pedido pedido = novoPedido();
        Produto produto = novoProduto();
        novoItem(pedido.getId(), produto.getId(), 1, new BigDecimal("5.00"));

        ItemPedido encontrado = dao.buscaPorPedido(pedido.getId()).get(0);

        assertEquals(0, BigDecimal.ZERO.compareTo(encontrado.getDesconto()));
    }

    @Test
    void cadastrarDevePersistirDescontoInformado() throws Exception {
        Pedido pedido = novoPedido();
        Produto produto = novoProduto();

        ItemPedido item = new ItemPedido();
        item.setPedidoId(pedido.getId());
        item.setProdutoId(produto.getId());
        item.setQuantidade(2);
        item.setPrecoUnitario(new BigDecimal("8.00"));
        item.setDesconto(new BigDecimal("1.25"));
        Integer id = dao.cadastrar(item);
        item.setId(id.longValue());
        itensCriados.add(item);

        ItemPedido encontrado = dao.buscaPorPedido(pedido.getId()).get(0);

        assertEquals(0, new BigDecimal("1.25").compareTo(encontrado.getDesconto()));
    }

    @Test
    void buscaPorPedidoDeveRetornarApenasItensDaquelePedido() throws Exception {
        Pedido pedido1 = novoPedido();
        Pedido pedido2 = novoPedido();
        Produto produto = novoProduto();

        ItemPedido itemPedido1 = novoItem(pedido1.getId(), produto.getId(), 2, new BigDecimal("15.00"));
        novoItem(pedido2.getId(), produto.getId(), 4, new BigDecimal("15.00"));

        List<ItemPedido> itensDoPedido1 = dao.buscaPorPedido(pedido1.getId());

        assertTrue(itensDoPedido1.stream().anyMatch(i -> i.getId().equals(itemPedido1.getId())));
        assertTrue(itensDoPedido1.stream().allMatch(i -> i.getPedidoId().equals(pedido1.getId())));
    }

    @Test
    void buscatodosDeveIncluirItensCadastrados() throws Exception {
        Pedido pedido = novoPedido();
        Produto produto = novoProduto();
        ItemPedido item = novoItem(pedido.getId(), produto.getId(), 1, new BigDecimal("1.00"));

        List<ItemPedido> todos = dao.buscatodos();

        assertTrue(todos.stream().anyMatch(i -> i.getId().equals(item.getId())));
    }

    @Test
    void excluirDeveRemoverItemDoBanco() throws Exception {
        Pedido pedido = novoPedido();
        Produto produto = novoProduto();
        ItemPedido item = novoItem(pedido.getId(), produto.getId(), 1, new BigDecimal("1.00"));

        Integer linhasAfetadas = dao.excluir(item);
        assertEquals(1, linhasAfetadas);

        List<ItemPedido> itensDoPedido = dao.buscaPorPedido(pedido.getId());
        assertFalse(itensDoPedido.stream().anyMatch(i -> i.getId().equals(item.getId())));

        itensCriados.remove(item);
    }

    @Test
    void excluirProdutoReferenciadoPorItemPedidoDeveFalhar() throws Exception {
        Pedido pedido = novoPedido();
        Produto produto = novoProduto();
        novoItem(pedido.getId(), produto.getId(), 1, new BigDecimal("1.00"));

        assertThrows(Exception.class, () -> produtoDao.excluir(produto));
    }
}
