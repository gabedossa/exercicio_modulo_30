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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import factory.ConnectionFactory;
import model.Pedido;

class PedidoDaoImplUnitTest {

    private final PedidoDaoImpl dao = new PedidoDaoImpl();

    @Test
    void cadastrarDeveInserirTotalEClienteEFecharConexao() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        ResultSet generatedKeys = mock(ResultSet.class);

        when(conn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(stm);
        when(stm.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getInt(1)).thenReturn(7);

        Pedido pedido = new Pedido();
        pedido.setTotal(new BigDecimal("150.00"));
        pedido.setClienteId(4L);

        Integer id;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            id = dao.cadastrar(pedido);
        }

        assertEquals(7, id);
        verify(conn).prepareStatement("INSERT INTO pedido (total, cliente_id) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
        verify(stm).setBigDecimal(1, new BigDecimal("150.00"));
        verify(stm).setObject(2, 4L, Types.BIGINT);
        verify(stm).close();
        verify(conn).close();
    }

    @Test
    void atualizarDeveAtualizarTotalEClientePorId() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(stm);
        when(stm.executeUpdate()).thenReturn(1);

        Pedido pedido = new Pedido();
        pedido.setId(9L);
        pedido.setTotal(new BigDecimal("200.00"));
        pedido.setClienteId(6L);

        Integer linhas;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            linhas = dao.atualizar(pedido);
        }

        assertEquals(1, linhas);
        verify(stm).setBigDecimal(1, new BigDecimal("200.00"));
        verify(stm).setObject(2, 6L, Types.BIGINT);
        verify(stm).setLong(3, 9L);
        verify(stm).close();
        verify(conn).close();
    }

    @Test
    void buscaPedidoDeveMontarObjetoAPartirDoResultSet() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(stm);
        when(stm.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong("id")).thenReturn(3L);
        LocalDateTime dataPedido = LocalDateTime.of(2026, 7, 20, 10, 30);
        when(rs.getTimestamp("data_pedido")).thenReturn(Timestamp.valueOf(dataPedido));
        when(rs.getBigDecimal("total")).thenReturn(new BigDecimal("99.90"));
        when(rs.getObject("cliente_id", Long.class)).thenReturn(2L);

        Pedido encontrado;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            encontrado = dao.buscaPedido(3L);
        }

        assertNotNull(encontrado);
        assertEquals(3L, encontrado.getId());
        assertEquals(dataPedido, encontrado.getDataPedido());
        assertEquals(0, new BigDecimal("99.90").compareTo(encontrado.getTotal()));
        assertEquals(2L, encontrado.getClienteId());
        verify(stm).setLong(1, 3L);
    }

    @Test
    void buscaPedidoDeveRetornarNuloQuandoResultSetVazio() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conn.prepareStatement(anyString())).thenReturn(stm);
        when(stm.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        Pedido encontrado;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            encontrado = dao.buscaPedido(99L);
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
        LocalDateTime dataPedido = LocalDateTime.of(2026, 7, 20, 9, 0);
        when(rs.getTimestamp("data_pedido")).thenReturn(Timestamp.valueOf(dataPedido));
        when(rs.getBigDecimal("total")).thenReturn(new BigDecimal("10.00"), new BigDecimal("20.00"));

        List<Pedido> todos;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            todos = dao.buscatodos();
        }

        assertEquals(2, todos.size());
        assertEquals(1L, todos.get(0).getId());
        assertEquals(2L, todos.get(1).getId());
    }

    @Test
    void excluirDeveFecharConexaoMesmoQuandoExecuteUpdateFalha() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(stm);
        when(stm.executeUpdate()).thenThrow(new SQLException("falha simulada"));

        Pedido pedido = new Pedido();
        pedido.setId(5L);

        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            assertThrows(SQLException.class, () -> dao.excluir(pedido));
        }

        verify(stm).close();
        verify(conn).close();
    }
}
