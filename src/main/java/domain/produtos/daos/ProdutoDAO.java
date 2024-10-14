package domain.produtos.daos;

import domain.produtos.models.Produto;
import domain.produtos.models.Estoque;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {

    private Connection connection;

    public ProdutoDAO(Connection connection) {
        this.connection = connection;
    }

    // C
    public void adicionarProduto(Produto produto) throws SQLException {
        String sql = "INSERT INTO produto (id, nome, descricao, quantidade, preco, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, produto.getId());
            pstmt.setString(2, produto.getNome());
            pstmt.setString(3, produto.getDescricao());
            pstmt.setInt(4, produto.getQuantidade());
            pstmt.setBigDecimal(5, produto.getPreco());
            pstmt.setString(6, produto.getStatus().name());  // Converte Enum para String
            pstmt.executeUpdate();
        }
    }

    // R
    public Produto buscarProdutoPorCodigo(Integer id) throws SQLException {
        String sql = "SELECT * FROM produto WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Produto(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("descricao"),
                            rs.getInt("quantidade"),
                            rs.getBigDecimal("preco"),
                            Estoque.valueOf(rs.getString("status").toUpperCase()),
                            null  // Você precisaria buscar a promoção separadamente
                    );
                }
            }
        }
        return null;
    }

    public List<Produto> listarTodosProdutos() throws SQLException {
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT * FROM produto";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Produto produto = new Produto(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("descricao"),
                        rs.getInt("quantidade"),
                        rs.getBigDecimal("preco"),
                        Estoque.valueOf(rs.getString("status")),
                        null // Promoção seria buscada separadamente
                );
                produtos.add(produto);
            }
        }
        return produtos;
    }

    // U
    public void atualizarProduto(Produto produto) throws SQLException {
        String sql = "UPDATE produto SET nome = ?, descricao = ?, quantidade = ?, preco = ?, status = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, produto.getNome());
            pstmt.setString(2, produto.getDescricao());
            pstmt.setInt(3, produto.getQuantidade());
            pstmt.setBigDecimal(4, produto.getPreco());
            pstmt.setString(5, produto.getStatus().name());
            pstmt.setInt(6, produto.getId());
            pstmt.executeUpdate();
        }
    }

    // D
    public void deletarProduto(int id) throws SQLException {
        String sql = "DELETE FROM produto WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

}
