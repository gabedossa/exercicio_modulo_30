package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import factory.ConnectionFactory;
import model.Pedido;

public class PedidoDaoImpl implements PedidoDAO {
    @Override
    public Integer cadastrar(Pedido pedido) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement(getSqlInsert(), Statement.RETURN_GENERATED_KEYS);
            adicionarParametrosInsert(stm, pedido);
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
        return "INSERT INTO pedido (total) VALUES (?)";
    }

    private void adicionarParametrosInsert(PreparedStatement stm, Pedido pedido) throws Exception {
        stm.setBigDecimal(1, pedido.getTotal());
    }

    @Override
    public Integer atualizar(Pedido pedido) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement(getSqlUpdate());
            adicionarParametrosUpdate(stm, pedido);
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
        return "UPDATE pedido SET total = ? WHERE id = ?";
    }

    private void adicionarParametrosUpdate(PreparedStatement stm, Pedido pedido) throws Exception {
        stm.setBigDecimal(1, pedido.getTotal());
        stm.setLong(2, pedido.getId());
    }

    @Override
    public Pedido buscaPedido(Long id) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement("SELECT id, data_pedido, total FROM pedido WHERE id = ?");
            stm.setLong(1, id);

            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    return montaPedido(rs);
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

    @Override
    public List<Pedido> buscatodos() throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;
        List<Pedido> pedidos = new ArrayList<>();

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement("SELECT id, data_pedido, total FROM pedido");

            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(montaPedido(rs));
                }
            }
            return pedidos;
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
    public Integer excluir(Pedido pedido) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement("DELETE FROM pedido WHERE id = ?");
            stm.setLong(1, pedido.getId());
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

    private Pedido montaPedido(ResultSet rs) throws Exception {
        Pedido pedido = new Pedido();
        pedido.setId(rs.getLong("id"));
        pedido.setDataPedido(rs.getTimestamp("data_pedido").toLocalDateTime());
        pedido.setTotal(rs.getBigDecimal("total"));
        return pedido;
    }
}
