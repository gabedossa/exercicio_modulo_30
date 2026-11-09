package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import model.Produto;

class ProdutoDaoImplTest {

    private final ProdutoDaoImpl dao = new ProdutoDaoImpl();
    private final List<Produto> criados = new ArrayList<>();

    private Produto novoProduto(String nome, BigDecimal preco, Integer quantidade) throws Exception {
        Produto produto = new Produto();
        produto.setCodigo("TESTE-" + UUID.randomUUID());
        produto.setNome(nome);
        produto.setDescricao("Descricao de teste");
        produto.setPreco(preco);
        produto.setQuantidade(quantidade);
        Integer id = dao.cadastrar(produto);
        produto.setId(id.longValue());
        criados.add(produto);
        return produto;
    }

    @AfterEach
    void limparRegistrosCriados() {
        for (Produto produto : criados) {
            try {
                dao.excluir(produto);
            } catch (Exception e) {

            }
        }
        criados.clear();
    }

    @Test
    void cadastrarDeveGerarIdEPersistirProduto() throws Exception {
        Produto produto = novoProduto("Teclado", new BigDecimal("199.90"), 10);

        assertNotNull(produto.getId());

        Produto encontrado = dao.buscaProduto(produto.getCodigo());
        assertNotNull(encontrado);
        assertEquals(produto.getCodigo(), encontrado.getCodigo());
        assertEquals("Teclado", encontrado.getNome());
        assertEquals(0, new BigDecimal("199.90").compareTo(encontrado.getPreco()));
        assertEquals(10, encontrado.getQuantidade());
        assertEquals("Descricao de teste", encontrado.getDescricao());
    }

    @Test
    void atualizarDeveAlterarCodigoNomePrecoEQuantidade() throws Exception {
        Produto produto = novoProduto("Mouse", new BigDecimal("59.90"), 20);

        produto.setNome("Mouse Atualizado");
        produto.setDescricao("Descricao atualizada");
        produto.setCodigo("TESTE-" + UUID.randomUUID());
        produto.setPreco(new BigDecimal("69.90"));
        produto.setQuantidade(15);
        Integer linhasAfetadas = dao.atualizar(produto);

        assertEquals(1, linhasAfetadas);

        Produto encontrado = dao.buscaProduto(produto.getCodigo());
        assertNotNull(encontrado);
        assertEquals("Mouse Atualizado", encontrado.getNome());
        assertEquals(0, new BigDecimal("69.90").compareTo(encontrado.getPreco()));
        assertEquals(15, encontrado.getQuantidade());
        assertEquals("Descricao atualizada", encontrado.getDescricao());
    }

    @Test
    void buscaProdutoDeveRetornarNuloQuandoCodigoNaoExiste() throws Exception {
        Produto encontrado = dao.buscaProduto("CODIGO-INEXISTENTE-" + UUID.randomUUID());
        assertNull(encontrado);
    }

    @Test
    void buscatodosDeveIncluirProdutosCadastrados() throws Exception {
        Produto produto1 = novoProduto("Produto Um", new BigDecimal("10.00"), 1);
        Produto produto2 = novoProduto("Produto Dois", new BigDecimal("20.00"), 2);

        List<Produto> todos = dao.buscatodos();

        assertTrue(todos.stream().anyMatch(p -> p.getCodigo().equals(produto1.getCodigo())));
        assertTrue(todos.stream().anyMatch(p -> p.getCodigo().equals(produto2.getCodigo())));
    }

    @Test
    void excluirDeveRemoverProdutoDoBanco() throws Exception {
        Produto produto = novoProduto("Produto Para Excluir", new BigDecimal("5.00"), 3);

        Integer linhasAfetadas = dao.excluir(produto);
        assertEquals(1, linhasAfetadas);

        Produto encontrado = dao.buscaProduto(produto.getCodigo());
        assertNull(encontrado);

        criados.remove(produto);
    }

    public ProdutoDaoImpl getDao() {
        return dao;
    }
}
