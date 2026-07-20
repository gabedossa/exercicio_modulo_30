package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import factory.ConnectionFactory;
import model.ItemPedido;

public class ItemPedidoDaoImpl implements ItemPedidoDAO {
    @Override
    public Integer cadastrar(ItemPedido item) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement(getSqlInsert(), Statement.RETURN_GENERATED_KEYS);
            adicionarParametrosInsert(stm, item);
            stm.executeUpdate();

            try (ResultSet rs = stm.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return null;
        } finally {
            if (stm != null) {
                stm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    private String getSqlInsert() {
        return "INSERT INTO item_pedido (pedido_id, produto_id, quantidade, preco_unitario) VALUES (?, ?, ?, ?)";
    }

    private void adicionarParametrosInsert(PreparedStatement stm, ItemPedido item) throws Exception {
        stm.setLong(1, item.getPedidoId());
        stm.setLong(2, item.getProdutoId());
        stm.setInt(3, item.getQuantidade());
        stm.setBigDecimal(4, item.getPrecoUnitario());
    }

    @Override
    public Integer atualizar(ItemPedido item) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement(getSqlUpdate());
            adicionarParametrosUpdate(stm, item);
            return stm.executeUpdate();
        } finally {
            if (stm != null) {
                stm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    private String getSqlUpdate() {
        return "UPDATE item_pedido SET quantidade = ?, preco_unitario = ? WHERE id = ?";
    }

    private void adicionarParametrosUpdate(PreparedStatement stm, ItemPedido item) throws Exception {
        stm.setInt(1, item.getQuantidade());
        stm.setBigDecimal(2, item.getPrecoUnitario());
        stm.setLong(3, item.getId());
    }

    @Override
    public List<ItemPedido> buscaPorPedido(Long pedidoId) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;
        List<ItemPedido> itens = new ArrayList<>();

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement(
                    "SELECT id, pedido_id, produto_id, quantidade, preco_unitario FROM item_pedido WHERE pedido_id = ?");
            stm.setLong(1, pedidoId);

            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    itens.add(montaItemPedido(rs));
                }
            }
            return itens;
        } finally {
            if (stm != null) {
                stm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Override
    public List<ItemPedido> buscatodos() throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;
        List<ItemPedido> itens = new ArrayList<>();

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement("SELECT id, pedido_id, produto_id, quantidade, preco_unitario FROM item_pedido");

            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    itens.add(montaItemPedido(rs));
                }
            }
            return itens;
        } finally {
            if (stm != null) {
                stm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Override
    public Integer excluir(ItemPedido item) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement("DELETE FROM item_pedido WHERE id = ?");
            stm.setLong(1, item.getId());
            return stm.executeUpdate();
        } finally {
            if (stm != null) {
                stm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    private ItemPedido montaItemPedido(ResultSet rs) throws Exception {
        ItemPedido item = new ItemPedido();
        item.setId(rs.getLong("id"));
        item.setPedidoId(rs.getLong("pedido_id"));
        item.setProdutoId(rs.getLong("produto_id"));
        item.setQuantidade(rs.getInt("quantidade"));
        item.setPrecoUnitario(rs.getBigDecimal("preco_unitario"));
        return item;
    }
}
