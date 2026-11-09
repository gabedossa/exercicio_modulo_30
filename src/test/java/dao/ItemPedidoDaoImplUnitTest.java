package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import factory.ConnectionFactory;
import model.ItemPedido;

class ItemPedidoDaoImplUnitTest {

    private final ItemPedidoDaoImpl dao = new ItemPedidoDaoImpl();

    @Test
    void cadastrarDeveInserirPedidoIdProdutoIdQuantidadePrecoEDescontoEFecharConexao() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        ResultSet generatedKeys = mock(ResultSet.class);

        when(conn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(stm);
        when(stm.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getInt(1)).thenReturn(21);

        ItemPedido item = new ItemPedido();
        item.setPedidoId(1L);
        item.setProdutoId(2L);
        item.setQuantidade(3);
        item.setPrecoUnitario(new BigDecimal("10.50"));
        item.setDesconto(new BigDecimal("1.00"));

        Integer id;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            id = dao.cadastrar(item);
        }

        assertEquals(21, id);
        verify(conn).prepareStatement(
                "INSERT INTO item_pedido (pedido_id, produto_id, quantidade, preco_unitario, desconto) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
        verify(stm).setLong(1, 1L);
        verify(stm).setLong(2, 2L);
        verify(stm).setInt(3, 3);
        verify(stm).setBigDecimal(4, new BigDecimal("10.50"));
        verify(stm).setBigDecimal(5, new BigDecimal("1.00"));
        verify(stm).close();
        verify(conn).close();
    }

    @Test
    void atualizarDeveAlterarApenasQuantidadePrecoUnitarioEDescontoPorId() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(stm);
        when(stm.executeUpdate()).thenReturn(1);

        ItemPedido item = new ItemPedido();
        item.setId(6L);
        item.setQuantidade(9);
        item.setPrecoUnitario(new BigDecimal("7.25"));
        item.setDesconto(new BigDecimal("0.75"));

        Integer linhas;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            linhas = dao.atualizar(item);
        }

        assertEquals(1, linhas);
        verify(stm).setInt(1, 9);
        verify(stm).setBigDecimal(2, new BigDecimal("7.25"));
        verify(stm).setBigDecimal(3, new BigDecimal("0.75"));
        verify(stm).setLong(4, 6L);
        verify(stm).close();
        verify(conn).close();
    }

    @Test
    void buscaPorPedidoDeveMontarItensAPartirDoResultSet() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(stm);
        when(stm.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getLong("id")).thenReturn(1L);
        when(rs.getLong("pedido_id")).thenReturn(4L);
        when(rs.getLong("produto_id")).thenReturn(2L);
        when(rs.getInt("quantidade")).thenReturn(3);
        when(rs.getBigDecimal("preco_unitario")).thenReturn(new BigDecimal("15.00"));
        when(rs.getBigDecimal("desconto")).thenReturn(new BigDecimal("2.00"));

        List<ItemPedido> itens;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            itens = dao.buscaPorPedido(4L);
        }

        assertEquals(1, itens.size());
        ItemPedido item = itens.get(0);
        assertEquals(1L, item.getId());
        assertEquals(4L, item.getPedidoId());
        assertEquals(2L, item.getProdutoId());
        assertEquals(3, item.getQuantidade());
        assertEquals(0, new BigDecimal("15.00").compareTo(item.getPrecoUnitario()));
        assertEquals(0, new BigDecimal("2.00").compareTo(item.getDesconto()));
        verify(stm).setLong(1, 4L);
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
        when(rs.getLong("pedido_id")).thenReturn(10L, 20L);
        when(rs.getLong("produto_id")).thenReturn(100L, 200L);
        when(rs.getInt("quantidade")).thenReturn(1, 2);
        when(rs.getBigDecimal("preco_unitario")).thenReturn(new BigDecimal("1.00"), new BigDecimal("2.00"));

        List<ItemPedido> todos;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            todos = dao.buscatodos();
        }

        assertEquals(2, todos.size());
        assertEquals(10L, todos.get(0).getPedidoId());
        assertEquals(20L, todos.get(1).getPedidoId());
    }

    @Test
    void excluirDeveFecharConexaoMesmoQuandoExecuteUpdateFalha() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(stm);
        when(stm.executeUpdate()).thenThrow(new SQLException("falha simulada"));

        ItemPedido item = new ItemPedido();
        item.setId(1L);

        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            assertThrows(SQLException.class, () -> dao.excluir(item));
        }

        verify(stm).close();
        verify(conn).close();
    }
}
