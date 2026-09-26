package br.com.oficina.controller;

import br.com.oficina.modelo.Produto;
import br.com.oficina.modelo.UnidadeMedida;
import br.com.oficina.service.ProdutoService;
import java.io.Serializable;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@ViewScoped
public class ProdutoBean implements Serializable {

    @Inject
    private ProdutoService produtoService;

    private List<Produto> produtos;
    private Produto produtoSelecionado;

    public void iniciar() {
        listar();
    }

    public void listar() {
        produtos = produtoService.listarTodos();
    }

    public void novo() {
        produtoSelecionado = new Produto();
    }

    public void salvar() {
        try {
            produtoService.salvar(produtoSelecionado);
            mensagem(FacesMessage.SEVERITY_INFO, "Produto salvo com sucesso.");
            listar();
            produtoSelecionado = new Produto();
        } catch (IllegalArgumentException e) {
            mensagem(FacesMessage.SEVERITY_WARN, e.getMessage());
        }
    }

    public void editar(Produto produto) {
        this.produtoSelecionado = produto;
    }

    public void excluir(Produto produto) {
        produtoService.excluir(produto.getId());
        mensagem(FacesMessage.SEVERITY_INFO, "Produto excluído.");
        listar();
    }

    private void mensagem(FacesMessage.Severity severidade, String texto) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(severidade, texto, null));
    }

    // ---- getters e setters ----

    public List<Produto> getProdutos() {
        return produtos;
    }

    public Produto getProdutoSelecionado() {
        return produtoSelecionado;
    }

    public void setProdutoSelecionado(Produto produtoSelecionado) {
        this.produtoSelecionado = produtoSelecionado;
    }

    public UnidadeMedida[] getUnidadesMedida() {
        return UnidadeMedida.values();
    }
}
