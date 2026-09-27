package br.com.estetica.dao;

import br.com.estetica.modelo.Produto;
import java.util.List;
import java.util.Locale;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

@ApplicationScoped
public class ProdutoDAO {

    private static final EntityManagerFactory FACTORY =
            Persistence.createEntityManagerFactory("maverickShiftPU");

    public void salvar(Produto produto) {
        EntityManager em = FACTORY.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(produto);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void excluir(String id) {
        EntityManager em = FACTORY.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Produto produto = em.find(Produto.class, id);
            if (produto != null) {
                em.remove(produto);
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Produto buscarPorId(String id) {
        EntityManager em = FACTORY.createEntityManager();
        try {
            return em.find(Produto.class, id);
        } finally {
            em.close();
        }
    }

    public List<Produto> listarTodos() {
        EntityManager em = FACTORY.createEntityManager();
        try {
            return em.createQuery("SELECT p FROM Produto p", Produto.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Produto> buscarPorNome(String nome) {
        EntityManager em = FACTORY.createEntityManager();
        try {
            return em.createQuery("SELECT p FROM Produto p WHERE LOWER(p.nome) LIKE :nome ORDER BY p.nome", Produto.class)
                    .setParameter("nome", "%" + nome.toLowerCase(Locale.ROOT) + "%")
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
