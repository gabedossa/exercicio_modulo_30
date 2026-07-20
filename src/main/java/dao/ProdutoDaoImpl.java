package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import factory.ConnectionFactory;
import model.Produto;

public class ProdutoDaoImpl implements ProdutoDAO {
    @Override
    public Integer cadastrar(Produto produto) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement(getSqlInsert(), Statement.RETURN_GENERATED_KEYS);
            adicionarParametrosInsert(stm, produto);
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
        return "INSERT INTO produto (codigo, nome, preco, quantidade) VALUES (?, ?, ?, ?)";
    }

    private void adicionarParametrosInsert(PreparedStatement stm, Produto produto) throws Exception {
        stm.setString(1, produto.getCodigo());
        stm.setString(2, produto.getNome());
        stm.setBigDecimal(3, produto.getPreco());
        stm.setInt(4, produto.getQuantidade());
    }

    @Override
    public Integer atualizar(Produto produto) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement(getSqlUpdate());
            adicionarParametrosUpdate(stm, produto);
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
        return "UPDATE produto SET codigo = ?, nome = ?, preco = ?, quantidade = ? WHERE id = ?";
    }

    private void adicionarParametrosUpdate(PreparedStatement stm, Produto produto) throws Exception {
        stm.setString(1, produto.getCodigo());
        stm.setString(2, produto.getNome());
        stm.setBigDecimal(3, produto.getPreco());
        stm.setInt(4, produto.getQuantidade());
        stm.setLong(5, produto.getId());
    }

    @Override
    public Produto buscaProduto(String codigo) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement("SELECT id, codigo, nome, preco, quantidade FROM produto WHERE codigo = ?");
            stm.setString(1, codigo);

            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    return montaProduto(rs);
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
    public List<Produto> buscatodos() throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;
        List<Produto> produtos = new ArrayList<>();

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement("SELECT id, codigo, nome, preco, quantidade FROM produto");

            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    produtos.add(montaProduto(rs));
                }
            }
            return produtos;
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
    public Integer excluir(Produto produto) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement("DELETE FROM produto WHERE id = ?");
            stm.setLong(1, produto.getId());
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

    private Produto montaProduto(ResultSet rs) throws Exception {
        Produto produto = new Produto();
        produto.setId(rs.getLong("id"));
        produto.setCodigo(rs.getString("codigo"));
        produto.setNome(rs.getString("nome"));
        produto.setPreco(rs.getBigDecimal("preco"));
        produto.setQuantidade(rs.getInt("quantidade"));
        return produto;
    }
}
