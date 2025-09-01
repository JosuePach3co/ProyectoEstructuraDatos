package com.example.estructuradatos;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class AvlTree<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private static class Node<T> implements Serializable {
        private static final long serialVersionUID = 1L;
        T key;
        Node<T> left, right;
        int height;

        Node(T k) {
            this.key = k;
            this.height = 1; // altura de hoja
        }
    }

    // Comparator no serializable por defecto (lambdas/clases anónimas suelen no serlo)
    private transient Comparator<T> cmp;
    private Node<T> root;

    public AvlTree(Comparator<T> comparator) {
        this.cmp = comparator;
    }

    public void setComparator(Comparator<T> comparator) {
        this.cmp = comparator;
    }

    public void insert(T key) {
        ensureComparator();
        root = insertRec(root, key);
    }

    public void delete(T key) {
        ensureComparator();
        root = deleteRec(root, key);
    }



    public List<T> toSortedList() {
        List<T> res = new ArrayList<>();
        inorder(root, res);
        return res;
    }

    public void clear() {
        root = null;
    }

    public boolean isEmpty() {
        return root == null;
    }


    private void ensureComparator() {
        if (cmp == null) {
            throw new IllegalStateException(
                    "Comparator es null. Llama a setComparator(...) antes de usar el AVL (tras deserializar, por ejemplo)."
            );
        }
    }

    private int h(Node<T> n) { return (n == null) ? 0 : n.height; }

    private int balance(Node<T> n) { return (n == null) ? 0 : h(n.left) - h(n.right); }

    private void update(Node<T> n) {
        n.height = 1 + Math.max(h(n.left), h(n.right));
    }

    private Node<T> rotateRight(Node<T> y) {
        Node<T> x = y.left;
        Node<T> T2 = (x != null) ? x.right : null;

        // rotar
        x.right = y;
        y.left = T2;

        // actualizar alturas
        update(y);
        update(x);
        return x;
    }

    private Node<T> rotateLeft(Node<T> x) {
        Node<T> y = x.right;
        Node<T> T2 = (y != null) ? y.left : null;

        // rotar
        y.left = x;
        x.right = T2;

        // actualizar alturas
        update(x);
        update(y);
        return y;
    }

    private Node<T> insertRec(Node<T> n, T key) {
        if (n == null) return new Node<>(key);

        int c = cmp.compare(key, n.key);
        if (c < 0) {
            n.left = insertRec(n.left, key);
        } else if (c > 0) {
            n.right = insertRec(n.right, key);
        } else {
            return n;
        }

        update(n);
        int b = balance(n);

        // Caso LL
        if (b > 1 && cmp.compare(key, n.left.key) < 0) {
            return rotateRight(n);
        }
        // Caso RR
        if (b < -1 && cmp.compare(key, n.right.key) > 0) {
            return rotateLeft(n);
        }
        // Caso LR
        if (b > 1 && cmp.compare(key, n.left.key) > 0) {
            n.left = rotateLeft(n.left);
            return rotateRight(n);
        }
        // Caso RL
        if (b < -1 && cmp.compare(key, n.right.key) < 0) {
            n.right = rotateRight(n.right);
            return rotateLeft(n);
        }

        return n;
    }

    private Node<T> deleteRec(Node<T> n, T key) {
        if (n == null) return null;

        int c = cmp.compare(key, n.key);
        if (c < 0) {
            n.left = deleteRec(n.left, key);
        } else if (c > 0) {
            n.right = deleteRec(n.right, key);
        } else {
            // encontrado
            if (n.left == null || n.right == null) {
                n = (n.left != null) ? n.left : n.right;
            } else {
                // dos hijos: usar sucesor (mínimo del subárbol derecho)
                Node<T> succ = minNode(n.right);
                n.key = succ.key;
                n.right = deleteRec(n.right, succ.key);
            }
        }

        if (n == null) return null;

        update(n);
        int b = balance(n);

        // Rebalanceos tras borrado
        // LL
        if (b > 1 && balance(n.left) >= 0) return rotateRight(n);
        // LR
        if (b > 1 && balance(n.left) < 0) {
            n.left = rotateLeft(n.left);
            return rotateRight(n);
        }
        // RR
        if (b < -1 && balance(n.right) <= 0) return rotateLeft(n);
        // RL
        if (b < -1 && balance(n.right) > 0) {
            n.right = rotateRight(n.right);
            return rotateLeft(n);
        }

        return n;
    }

    private Node<T> minNode(Node<T> n) {
        Node<T> cur = n;
        while (cur != null && cur.left != null) {
            cur = cur.left;
        }
        return cur;
    }

    private void inorder(Node<T> n, List<T> out) {
        if (n == null) return;
        inorder(n.left, out);
        out.add(n.key);
        inorder(n.right, out);
    }



    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
    }
}

