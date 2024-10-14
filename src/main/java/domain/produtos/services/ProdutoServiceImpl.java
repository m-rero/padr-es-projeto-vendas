package domain.produtos.services;

import domain.produtos.daos.ProdutoDAO;
import domain.produtos.daos.PromocaoDAO;
import domain.produtos.models.Estoque;
import domain.produtos.models.Produto;
import domain.produtos.models.Promocao;
import infrastructure.notifications.Observer;
import infrastructure.notifications.Subject;
import infrastructure.notifications.changemanagers.ChangeManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProdutoServiceImpl implements ProdutoService, Subject<Promocao> {

    private ProdutoDAO produtoDAO;
    private PromocaoDAO promocaoDAO;
    private List<Produto> carrinho;
    private ChangeManager changeManager;

    public ProdutoServiceImpl(Connection connection, ChangeManager changeManager) {
        this.produtoDAO = new ProdutoDAO(connection); // Inicializa o DAO com a conexão
        this.carrinho = new ArrayList<>();// Inicializa o carrinho como uma lista vazia
        this.changeManager = changeManager;
    }

    @Override
    public List<Produto> getAllProdutos() {
        try {
            return produtoDAO.listarTodosProdutos();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar todos os produtos", e);
        }
    }

    @Override
    public void addProdutoToCarrinho(Produto produto) {
        try {
            Produto produtoNoBanco = produtoDAO.buscarProdutoPorCodigo(produto.getId());

            if (produtoNoBanco != null) {

                if (produtoNoBanco.getStatus() == Estoque.DISPONIVEL) {
                    carrinho.add(produtoNoBanco);
                    System.out.println("Produto adicionado ao carrinho: " + produto.getNome());
                } else {
                    System.out.println("Produto INDISPONÍVEL.");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar produto para adicionar ao carrinho.", e);
        }

    }

    @Override
    public List<Produto> getCarrinho() {
        return carrinho;
    }

    @Override
    public void deleteProdutoFromCarrinho(Produto produto) {
        Produto produtoEncontrado = null;

        //varrer
        for (Produto p : carrinho) {
            if (p.getId().equals(produto.getId())) {
                produtoEncontrado = p;
                break;
            }
        }

        // se o produto foi encontrado, remove
        if (produtoEncontrado != null) {
            carrinho.remove(produtoEncontrado);
            System.out.println("Produto removido do carrinho: " + produto.getNome());
        } else {
            System.out.println("Produto não está no carrinho.");
        }
    }

    @Override
    public void buyCarrinho() {
        //checa se ta vazio
        if (carrinho.isEmpty()) {
            System.out.println("Carrinho está vazio. Adicione produtos antes de comprar.");
            return;
        }

        BigDecimal total = BigDecimal.ZERO;
        //varre
        for (Produto produto : carrinho) {
            try {

                Produto produtoNoBanco = produtoDAO.buscarProdutoPorCodigo(produto.getId());

                if (produtoNoBanco.getStatus() == Estoque.DISPONIVEL) {
                    // calcula o preço final
                    BigDecimal precoFinal = produto.calcularPreco();
                    total = total.add(precoFinal);

                    //atualiza no banco a qntd
                    produtoNoBanco.setQuantidade(produtoNoBanco.getQuantidade() - 1);
                    produtoDAO.atualizarProduto(produtoNoBanco);
                    System.out.println("Comprado: " + produto.getNome() + " por " + precoFinal);
                } else {
                    System.out.println("Produto não disponível no estoque: " + produto.getNome());
                }
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao processar compra do produto: " + produto.getNome(), e);
            }
        }
        System.out.println("Total da compra: " + total);

        // esvazia o carro
        carrinho.clear();
    }

    @Override
    public void addPromocao(Integer idProduto, Promocao promocao) {

        try{
            promocaoDAO.atualizarPromocao(promocao);
            Produto produtoNoBanco = produtoDAO.buscarProdutoPorCodigo(idProduto);

            if (produtoNoBanco != null) {

                if (produtoNoBanco.getStatus() == Estoque.DISPONIVEL) {
                    produtoNoBanco.setPromocao(promocao);
                } else {
                    System.out.println("Produto INDISPONÍVEL para aplicar a promoção.");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar produto para adicionar promoção.", e);
        }
    }

    @Override
    public void anexar(Observer<Promocao> observer) {
        changeManager.adicionarObserver(this, observer);
    }

    @Override
    public void desanexar(Observer<Promocao> observer) {
        changeManager.removerObserver(this, observer);
    }

    @Override
    public void notificar(Promocao dados) {
        changeManager.notificar(this, dados);
    }
}