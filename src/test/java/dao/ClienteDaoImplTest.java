package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import model.Cliente;

class ClienteDaoImplTest {

    private final ClienteDaoImpl dao = new ClienteDaoImpl();
    private final List<Cliente> criados = new ArrayList<>();

    private Cliente novoCliente(String nome) throws Exception {
        Cliente cliente = new Cliente();
        cliente.setCodigo("TESTE-" + UUID.randomUUID());
        cliente.setNome(nome);
        Integer id = dao.cadastrar(cliente);
        cliente.setId(id.longValue());
        criados.add(cliente);
        return cliente;
    }

    @AfterEach
    void limparRegistrosCriados() {
        for (Cliente cliente : criados) {
            try {
                dao.excluir(cliente);
            } catch (Exception e) {

            }
        }
        criados.clear();
    }

    @Test
    void cadastrarDeveGerarIdEPersistirCliente() throws Exception {
        Cliente cliente = novoCliente("Joao da Silva");

        assertNotNull(cliente.getId());

        Cliente encontrado = dao.buscaCliente(cliente.getCodigo());
        assertNotNull(encontrado);
        assertEquals(cliente.getCodigo(), encontrado.getCodigo());
        assertEquals("Joao da Silva", encontrado.getNome());
    }

    @Test
    void atualizarDeveAlterarCodigoENome() throws Exception {
        Cliente cliente = novoCliente("Maria Souza");

        cliente.setNome("Maria Souza Atualizada");
        cliente.setCodigo("TESTE-" + UUID.randomUUID());
        Integer linhasAfetadas = dao.atualizar(cliente);

        assertEquals(1, linhasAfetadas);

        Cliente encontrado = dao.buscaCliente(cliente.getCodigo());
        assertNotNull(encontrado);
        assertEquals("Maria Souza Atualizada", encontrado.getNome());
    }

    @Test
    void buscaClienteDeveRetornarNuloQuandoCodigoNaoExiste() throws Exception {
        Cliente encontrado = dao.buscaCliente("CODIGO-INEXISTENTE-" + UUID.randomUUID());
        assertNull(encontrado);
    }

    @Test
    void buscatodosDeveIncluirClientesCadastrados() throws Exception {
        Cliente cliente1 = novoCliente("Cliente Um");
        Cliente cliente2 = novoCliente("Cliente Dois");

        List<Cliente> todos = dao.buscatodos();

        assertTrue(todos.stream().anyMatch(c -> c.getCodigo().equals(cliente1.getCodigo())));
        assertTrue(todos.stream().anyMatch(c -> c.getCodigo().equals(cliente2.getCodigo())));
    }

    @Test
    void excluirDeveRemoverClienteDoBanco() throws Exception {
        Cliente cliente = novoCliente("Cliente Para Excluir");

        Integer linhasAfetadas = dao.excluir(cliente);
        assertEquals(1, linhasAfetadas);

        Cliente encontrado = dao.buscaCliente(cliente.getCodigo());
        assertNull(encontrado);

        criados.remove(cliente);
    }

    public ClienteDaoImpl getDao() {
        return dao;
    }
}
