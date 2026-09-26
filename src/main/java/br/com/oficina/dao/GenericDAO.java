package br.com.oficina.dao;

import br.com.oficina.util.JPAUtil;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * DAO genérico com as operações de CRUD comuns a todas as entidades.
 * Cada DAO concreto (ProdutoDAO, ClienteDAO, ServicoDAO...) apenas
 * estende esta classe informando a entidade e o tipo do id.
 */
public abstract class GenericDAO<T, ID extends Serializable> {

    private final Class<T> classe;

    protected GenericDAO(Class<T> classe) {
        this.classe = classe;
    }

    public void salvar(T entidade) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(entidade);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void excluir(ID id) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T entidade = em.find(classe, id);
            if (entidade != null) {
                em.remove(entidade);
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public T buscarPorId(ID id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(classe, id);
        } finally {
            em.close();
        }
    }

    public List<T> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT e FROM " + classe.getSimpleName() + " e";
            return em.createQuery(jpql, classe).getResultList();
        } finally {
            em.close();
        }
    }
}
