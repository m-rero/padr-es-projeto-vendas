package domain.produtos.models;

import utils.Pair;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiConsumer;

public class Carrinho {

    private Map<Produto, Integer> produtoQnt;

    private Map<Integer, Produto> idProduto;

    public Carrinho() {
        this.produtoQnt = new HashMap<>();
    }


    public void adicionarProduto(Produto produto, int quantidade) {
        Integer previousValue = produtoQnt.put(produto, produtoQnt.getOrDefault(produto, 0) + quantidade);

        if (previousValue == null) {
            idProduto.put(produto.getId(), produto);
        }
    }

    public void removerProduto(Produto produto, int quantidade) {
        Integer newValue = produtoQnt.computeIfPresent(produto, (k, v) ->
            (v-quantidade) == 0 ? null : (v-quantidade)
        );

        if (newValue == null) {
            idProduto.remove(produto.getId());
        }
    }

    public void removerProduto(int id, int quantidade) {
        removerProduto(idProduto.get(id), quantidade);
    }

    public int getQuantidade(Produto produto) {
        return produtoQnt.getOrDefault(produto, 0);
    }

    /**
     * @return lista de {@link Pair} com os produtoQnt e suas respectivas quantidades
     */
    public List<Pair<Produto, Integer>> getProdutoQnt() {
        List<Pair<Produto, Integer>> list = new ArrayList<>();

        for (Map.Entry<Produto, Integer> entry : produtoQnt.entrySet()) {
            list.add(Pair.of(entry.getKey(), entry.getValue()));
        }

        return list;
    }

    public Optional<Pair<Produto, Integer>> getProdutoQnt(int id) {
        Produto produto = idProduto.get(id);

        if (produto == null) return Optional.empty();

        return Optional.of(Pair.of(produto, produtoQnt.get(produto)));
    }

    /**
     * Realiza uma operação para cada par (produto-quantidade) do carrinho
     * @param biConsumer {@link BiConsumer} que recebe o produto e sua quantidade como
     *                   parâmetros e realiza a operação desejada
     */
    public void forEach(BiConsumer<Produto, Integer> biConsumer) {
        produtoQnt.forEach(biConsumer);
    }

    public BigDecimal calcularPreco() {
        return produtoQnt.keySet().stream()
            .map(Produto::getPreco)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void esvazear() {
        produtoQnt.clear();
    }

    public boolean vazio() {
        return produtoQnt.isEmpty();
    }

}
