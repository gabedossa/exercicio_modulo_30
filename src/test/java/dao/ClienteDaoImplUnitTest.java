package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import factory.ConnectionFactory;
import model.Cliente;

class ClienteDaoImplUnitTest {

    private final ClienteDaoImpl dao = new ClienteDaoImpl();

    @Test
    void cadastrarDeveInserirClienteEFecharConexao() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        ResultSet generatedKeys = mock(ResultSet.class);

        when(conn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(stm);
        when(stm.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getInt(1)).thenReturn(7);

        Cliente cliente = new Cliente();
        cliente.setCodigo("CLI-001");
        cliente.setNome("Joao da Silva");

        Integer id;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            id = dao.cadastrar(cliente);
        }

        assertEquals(7, id);
        verify(conn).prepareStatement("INSERT INTO cliente (codigo, nome) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS);
        verify(stm).setString(1, "CLI-001");
        verify(stm).setString(2, "Joao da Silva");
        verify(stm).close();
        verify(conn).close();
    }

    @Test
    void atualizarDeveAtualizarCodigoNomeEId() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(stm);
        when(stm.executeUpdate()).thenReturn(1);

        Cliente cliente = new Cliente();
        cliente.setId(3L);
        cliente.setCodigo("CLI-002");
        cliente.setNome("Maria Souza");

        Integer linhas;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            linhas = dao.atualizar(cliente);
        }

        assertEquals(1, linhas);
        verify(stm).setString(1, "CLI-002");
        verify(stm).setString(2, "Maria Souza");
        verify(stm).setLong(3, 3L);
        verify(stm).close();
        verify(conn).close();
    }

    @Test
    void buscaClienteDeveMontarObjetoAPartirDoResultSet() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(stm);
        when(stm.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong("id")).thenReturn(5L);
        when(rs.getString("codigo")).thenReturn("CLI-005");
        when(rs.getString("nome")).thenReturn("Cliente Cinco");

        Cliente encontrado;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            encontrado = dao.buscaCliente("CLI-005");
        }

        assertNotNull(encontrado);
        assertEquals(5L, encontrado.getId());
        assertEquals("CLI-005", encontrado.getCodigo());
        assertEquals("Cliente Cinco", encontrado.getNome());
        verify(stm).setString(1, "CLI-005");
    }

    @Test
    void buscaClienteDeveRetornarNuloQuandoResultSetVazio() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conn.prepareStatement(anyString())).thenReturn(stm);
        when(stm.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        Cliente encontrado;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            encontrado = dao.buscaCliente("INEXISTENTE");
        }

        assertNull(encontrado);
    }

    @Test
    void buscatodosDeveMapearTodasAsLinhasDoResultSet() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(stm);
        when(stm.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getLong("id")).thenReturn(1L, 2L);
        when(rs.getString("codigo")).thenReturn("CLI-001", "CLI-002");
        when(rs.getString("nome")).thenReturn("Cliente Um", "Cliente Dois");

        List<Cliente> todos;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            todos = dao.buscatodos();
        }

        assertEquals(2, todos.size());
        assertEquals("CLI-001", todos.get(0).getCodigo());
        assertEquals("CLI-002", todos.get(1).getCodigo());
    }

    @Test
    void excluirDeveFecharConexaoMesmoQuandoExecuteUpdateFalha() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(stm);
        when(stm.executeUpdate()).thenThrow(new SQLException("falha simulada"));

        Cliente cliente = new Cliente();
        cliente.setId(1L);

        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            assertThrows(SQLException.class, () -> dao.excluir(cliente));
        }

        verify(stm).close();
        verify(conn).close();
    }
}
