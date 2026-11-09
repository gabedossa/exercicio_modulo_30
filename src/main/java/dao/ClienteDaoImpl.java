package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import factory.ConnectionFactory;
import model.Cliente;

public class ClienteDaoImpl implements ClienteDAO {
    @Override
    public Integer cadastrar(Cliente cli) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement(getSqlInsert(), Statement.RETURN_GENERATED_KEYS);
            adicionarParametrosInsert(stm, cli);
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
        return "INSERT INTO cliente (codigo, nome, email) VALUES (?, ?, ?)";
    }

    private void adicionarParametrosInsert(PreparedStatement stm, Cliente cliente) throws Exception {
        stm.setString(1, cliente.getCodigo());
        stm.setString(2, cliente.getNome());
        stm.setString(3, cliente.getEmail());
    }

    @Override
    public Integer atualizar(Cliente cliente) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement(getSqlUpdate());
            adicionarParametrosUpdate(stm, cliente);
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
        return "UPDATE cliente SET codigo = ?, nome = ?, email = ? WHERE id = ?";
    }

    private void adicionarParametrosUpdate(PreparedStatement stm, Cliente cliente) throws Exception {
        stm.setString(1, cliente.getCodigo());
        stm.setString(2, cliente.getNome());
        stm.setString(3, cliente.getEmail());
        stm.setLong(4, cliente.getId());
    }

    @Override
    public Cliente buscaCliente(String codigo) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement("SELECT id, codigo, nome, email FROM cliente WHERE codigo = ?");
            stm.setString(1, codigo);

            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    return montaCliente(rs);
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
    public List<Cliente> buscatodos() throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;
        List<Cliente> clientes = new ArrayList<>();

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement("SELECT id, codigo, nome, email FROM cliente");

            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    clientes.add(montaCliente(rs));
                }
            }
            return clientes;
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
    public Integer excluir(Cliente cli) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement("DELETE FROM cliente WHERE id = ?");
            stm.setLong(1, cli.getId());
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

    private Cliente montaCliente(ResultSet rs) throws Exception {
        Cliente cliente = new Cliente();
        cliente.setId(rs.getLong("id"));
        cliente.setCodigo(rs.getString("codigo"));
        cliente.setNome(rs.getString("nome"));
        cliente.setEmail(rs.getString("email"));
        return cliente;
    }
}
